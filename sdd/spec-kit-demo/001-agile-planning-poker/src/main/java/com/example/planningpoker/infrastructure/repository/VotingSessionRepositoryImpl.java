package com.example.planningpoker.infrastructure.repository;

import com.example.planningpoker.domain.model.SessionStatus;
import com.example.planningpoker.domain.model.aggregate.VotingSession;
import com.example.planningpoker.domain.model.valueobject.StoryPoint;
import com.example.planningpoker.domain.model.valueobject.Vote;
import com.example.planningpoker.domain.repository.VotingSessionRepository;
import com.example.planningpoker.infrastructure.persistence.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 估点会话仓储实现
 * 
 * <p>使用MyBatis实现估点会话的持久化操作。
 * 负责聚合根（VotingSession）与持久化对象（VotingSessionPO, VotePO, ParticipantPO）之间的转换。
 * 
 * <p>复杂度：
 * <ul>
 *   <li>VotingSession包含集合（participants Set, votes Map）</li>
 *   <li>需要循环插入和查询关联表（vote表、participant表）</li>
 *   <li>需要重建领域对象的集合关系</li>
 * </ul>
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
@Repository
public class VotingSessionRepositoryImpl implements VotingSessionRepository {
    
    private final VotingSessionMapper mapper;
    
    public VotingSessionRepositoryImpl(VotingSessionMapper mapper) {
        this.mapper = mapper;
    }
    
    /**
     * 保存估点会话
     * 
     * <p>聚合保存策略：
     * <ol>
     *   <li>保存会话主表（voting_session）</li>
     *   <li>保存参与者（participant表）- 循环插入</li>
     *   <li>保存投票（vote表）- 循环upsert</li>
     * </ol>
     * 
     * @param session 估点会话聚合根
     */
    @Override
    public void save(VotingSession session) {
        VotingSessionPO po = toMainPO(session);

        // 使用MERGE实现upsert（插入或更新）
        mapper.insert(po);

        // 保存参与者（先删除再插入，简化实现）
        mapper.deleteParticipantsByStoryCardId(session.getStoryCardId());
        for (String participantName : session.getParticipants()) {
            ParticipantPO participantPO = new ParticipantPO();
            participantPO.setStoryCardId(session.getStoryCardId());
            participantPO.setParticipantName(participantName);
            participantPO.setJoinedAt(session.getCreatedAt());
            mapper.insertParticipant(participantPO);
        }

        // 保存投票（使用ON DUPLICATE KEY UPDATE）
        for (Vote vote : session.getVotes().values()) {
            VotePO votePO = new VotePO();
            votePO.setStoryCardId(session.getStoryCardId());
            votePO.setParticipantName(vote.getParticipantName());
            votePO.setStoryPoint(vote.getStoryPoint().getValue());
            votePO.setVotedAt(vote.getVotedAt());
            mapper.insertVote(votePO);
        }
    }
    
    /**
     * 根据ID查找会话
     *
     * <p>聚合加载策略：
     * <ol>
     *   <li>查询会话主表</li>
     *   <li>查询参与者列表</li>
     *   <li>查询投票列表</li>
     *   <li>重建聚合根（包含集合）</li>
     * </ol>
     *
     * @param id 会话ID
     * @return Optional包装的会话对象
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<VotingSession> findById(Long id) {
        // 1. 查询会话主表
        VotingSessionPO mainPO = mapper.selectById(id);
        if (mainPO == null) {
            return Optional.empty();
        }

        // 2. 查询参与者
        List<ParticipantPO> participantPOs = mapper.selectParticipantsByStoryCardId(mainPO.getStoryCardId());
        Set<String> participants = participantPOs.stream()
                .map(ParticipantPO::getParticipantName)
                .collect(Collectors.toSet());

        // 3. 查询投票
        List<VotePO> votePOs = mapper.selectVotesByStoryCardId(mainPO.getStoryCardId());
        Map<String, Vote> votes = votePOs.stream()
                .map(this::votePoToDomain)
                .collect(Collectors.toMap(
                        Vote::getParticipantName,
                        vote -> vote
                ));

        // 4. 重建聚合根
        VotingSession session = VotingSession.builder()
                .storyCardId(mainPO.getStoryCardId())
                .status(SessionStatus.valueOf(mainPO.getStatus()))
                .participants(participants)
                .votes(votes)
                .revealedAt(mainPO.getRevealedAt())
                .createdAt(mainPO.getCreatedAt())
                .completedAt(mainPO.getCompletedAt())
                .hostName(mainPO.getHostName())
                .votingStartedAt(mainPO.getVotingStartedAt())
                .votingDeadline(mainPO.getVotingDeadline())
                .build();

        return Optional.of(session);
    }
    
    /**
     * 查找进行中的会话
     *
     * @return Optional包装的会话对象
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<VotingSession> findActiveSession() {
        VotingSessionPO mainPO = mapper.selectActiveSession();
        if (mainPO == null) {
            return Optional.empty();
        }

        // 重用findById的逻辑
        return findById(mainPO.getStoryCardId());
    }
    
    /**
     * 删除会话
     * 
     * <p>数据库外键CASCADE会自动删除关联的投票和参与者。
     * 
     * @param id 会话ID
     */
    @Override
    public void delete(Long id) {
        mapper.delete(id);
    }
    
    /**
     * 根据故事卡ID和会话状态查找会话
     *
     * <p>用于支持多人加入同一估点会话。
     *
     * @param storyCardId 故事卡ID
     * @param status 会话状态
     * @return Optional包装的会话对象
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<VotingSession> findByStoryCardIdAndStatus(Long storyCardId, SessionStatus status) {
        return mapper.findByStoryCardIdAndStatus(storyCardId, status.name())
                .flatMap(mainPO -> findById(mainPO.getStoryCardId()));
    }
    
    /**
     * 根据状态查找所有会话
     *
     * <p>用于定时任务扫描进行中的会话，检查投票超时。
     *
     * @param status 会话状态
     * @return 会话列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<VotingSession> findByStatus(SessionStatus status) {
        List<VotingSessionPO> mainPOs = mapper.selectByStatus(status.name());

        return mainPOs.stream()
                .map(mainPO -> findById(mainPO.getStoryCardId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
    
    /**
     * 领域对象转PO（主表）
     * 
     * @param session 估点会话领域对象
     * @return 会话PO
     */
    private VotingSessionPO toMainPO(VotingSession session) {
        VotingSessionPO po = new VotingSessionPO();
        po.setStoryCardId(session.getStoryCardId());
        po.setStatus(session.getStatus().name());
        po.setRevealedAt(session.getRevealedAt());
        po.setCreatedAt(session.getCreatedAt());
        po.setCompletedAt(session.getCompletedAt());
        // 新增字段
        po.setHostName(session.getHostName());
        po.setVotingStartedAt(session.getVotingStartedAt());
        po.setVotingDeadline(session.getVotingDeadline());

        return po;
    }
    
    /**
     * VotePO转Vote值对象
     * 
     * @param po 投票PO
     * @return Vote值对象
     */
    private Vote votePoToDomain(VotePO po) {
        StoryPoint storyPoint = StoryPoint.of(po.getStoryPoint());
        return Vote.of(po.getParticipantName(), storyPoint, po.getVotedAt());
    }
}
