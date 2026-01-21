package com.example.planningpoker.application.service;

import com.example.planningpoker.application.command.CompleteSessionCommand;
import com.example.planningpoker.application.command.StartSessionCommand;
import com.example.planningpoker.application.command.SubmitVoteCommand;
import com.example.planningpoker.domain.event.VoteSubmitted;
import com.example.planningpoker.domain.event.VotesRevealed;
import com.example.planningpoker.domain.event.VotingSessionCompleted;
import com.example.planningpoker.domain.event.VotingSessionStarted;
import com.example.planningpoker.domain.exception.SessionAlreadyExistsException;
import com.example.planningpoker.domain.exception.SessionNotFoundException;
import com.example.planningpoker.domain.exception.StoryCardNotFoundException;
import com.example.planningpoker.domain.exception.UnauthorizedException;
import com.example.planningpoker.domain.model.SessionStatus;
import com.example.planningpoker.domain.model.aggregate.StoryCard;
import com.example.planningpoker.domain.model.aggregate.VotingSession;
import com.example.planningpoker.domain.model.valueobject.StoryPoint;
import com.example.planningpoker.domain.model.valueobject.Vote;
import com.example.planningpoker.domain.repository.StoryCardRepository;
import com.example.planningpoker.domain.repository.VotingSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 估点会话应用服务
 * 
 * <p>职责：协调估点会话相关的用例流程，包括：
 * <ul>
 *   <li>开始估点会话</li>
 *   <li>提交投票</li>
 *   <li>查询会话状态</li>
 *   <li>完成会话</li>
 * </ul>
 * 
 * <p>应用层负责：
 * <ul>
 *   <li>验证输入参数</li>
 *   <li>协调领域对象和仓储</li>
 *   <li>定义事务边界</li>
 *   <li>发布领域事件</li>
 * </ul>
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
@Service
@Transactional
public class VotingSessionApplicationService {
    
    private static final Logger log = LoggerFactory.getLogger(VotingSessionApplicationService.class);
    
    private final VotingSessionRepository votingSessionRepository;
    private final StoryCardRepository storyCardRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    public VotingSessionApplicationService(VotingSessionRepository votingSessionRepository,
                                          StoryCardRepository storyCardRepository,
                                          ApplicationEventPublisher eventPublisher) {
        this.votingSessionRepository = votingSessionRepository;
        this.storyCardRepository = storyCardRepository;
        this.eventPublisher = eventPublisher;
    }
    
    /**
     * 开始估点会话
     * 
     * <p>用例流程：
     * <ol>
     *   <li>验证输入参数</li>
     *   <li>检查是否已有活跃会话（不允许并发）</li>
     *   <li>加载故事卡并验证存在性</li>
     *   <li>将故事卡状态变更为ESTIMATING</li>
     *   <li>创建估点会话聚合根</li>
     *   <li>添加发起者为参与者</li>
     *   <li>持久化会话和故事卡</li>
     *   <li>发布VotingSessionStarted事件</li>
     * </ol>
     * 
     * @param command 开始会话命令
     * @return 创建的估点会话
     * @throws StoryCardNotFoundException 如果故事卡不存在
     * @throws SessionAlreadyExistsException 如果已有活跃会话
     */
    public VotingSession startSession(StartSessionCommand command, String currentUser) {
        log.info("开始估点会话，故事卡ID: {}, 发起者: {}, 操作者: {}", 
                command.getStoryCardId(), command.getParticipantName(), currentUser);
        
        // 1. 加载故事卡并验证权限
        StoryCard storyCard = storyCardRepository.findById(command.getStoryCardId())
                .orElseThrow(() -> new StoryCardNotFoundException(command.getStoryCardId()));
        
        // 权限检查：仅主持人可开启估点
        if (!storyCard.isHost(currentUser)) {
            throw new UnauthorizedException(
                    String.format("只有主持人 %s 可以开启估点", storyCard.getHostName())
            );
        }

        // 2. 检查是否已有已完成的会话（已完成的会话不能重新开启）
        votingSessionRepository.findByStoryCardIdAndStatus(
                command.getStoryCardId(),
                SessionStatus.COMPLETED
        ).ifPresent(completedSession -> {
            throw new IllegalStateException(
                    "该故事卡的估点会话已完成，不能重新开启"
            );
        });

        // 3. 检查是否已有活跃会话
        votingSessionRepository.findActiveSession().ifPresent(existingSession -> {
            throw new SessionAlreadyExistsException(existingSession.getStoryCardId());
        });

        // 4. 将故事卡状态变更为ESTIMATING
        storyCard.startEstimation();

        // 5. 创建估点会话
        VotingSession session = VotingSession.builder()
                .storyCardId(storyCard.getId())
                .hostName(storyCard.getHostName()) // 设置主持人为故事卡主持人
                .build();

        // 6. 添加发起者为参与者
        session.addParticipant(command.getParticipantName());

        // 7. 持久化会话
        votingSessionRepository.save(session);

        // 8. 绑定会话到故事卡（会话ID就是故事卡ID）
        storyCard.bindVotingSession(session.getStoryCardId());
        storyCardRepository.save(storyCard);

        log.info("估点会话创建成功，ID: {}, 主持人: {}", session.getStoryCardId(), session.getHostName());

        // 9. 发布领域事件
        VotingSessionStarted event = new VotingSessionStarted(
                session.getStoryCardId(),
                storyCard.getId(),
                storyCard.getTitle(),
                command.getParticipantName(),
                LocalDateTime.now()
        );
        eventPublisher.publishEvent(event);
        
        return session;
    }
    
    /**
     * 提交投票
     * 
     * <p>用例流程：
     * <ol>
     *   <li>验证输入参数</li>
     *   <li>加载估点会话</li>
     *   <li>创建Vote值对象</li>
     *   <li>添加投票到会话（会话会自动添加参与者）</li>
     *   <li>检查是否所有人已投票</li>
     *   <li>如果所有人已投票，自动揭示结果</li>
     *   <li>持久化会话</li>
     *   <li>发布VoteSubmitted事件（和VotesRevealed事件，如果揭示了）</li>
     * </ol>
     * 
     * @param command 提交投票命令
     * @return 更新后的估点会话
     * @throws SessionNotFoundException 如果会话不存在
     */
    public VotingSession submitVote(SubmitVoteCommand command) {
        log.info("提交投票，会话ID: {}, 参与者: {}, 点数: {}", 
                command.getSessionId(), command.getParticipantName(), command.getStoryPointValue());
        
        // 1. 加载会话
        VotingSession session = votingSessionRepository.findById(command.getSessionId())
                .orElseThrow(() -> new SessionNotFoundException(command.getSessionId()));
        
        // 2. 检查是否是更新投票
        boolean isUpdate = session.getVotes().containsKey(command.getParticipantName());
        
        // 3. 创建Vote值对象
        StoryPoint storyPoint = StoryPoint.of(command.getStoryPointValue());
        Vote vote = Vote.of(command.getParticipantName(), storyPoint);
        
        // 4. 添加投票（领域方法）
        session.addVote(vote);
        
        // 5. 检查是否所有人已投票
        boolean allVotesComplete = session.areAllVotesComplete();
        
        // 6. 如果所有人已投票，自动揭示结果
        if (allVotesComplete && session.getRevealedAt() == null) {
            session.revealVotes();
            log.info("所有人已投票，自动揭示结果，会话ID: {}", session.getStoryCardId());
        }

        // 7. 持久化
        votingSessionRepository.save(session);

        log.info("投票提交成功，会话ID: {}, 参与者: {}", session.getStoryCardId(), command.getParticipantName());

        // 8. 发布VoteSubmitted事件
        VoteSubmitted voteEvent = new VoteSubmitted(
                session.getStoryCardId(),
                command.getParticipantName(),
                isUpdate,
                LocalDateTime.now(),
                session.getParticipants().size(),
                session.getVotes().size()
        );
        eventPublisher.publishEvent(voteEvent);

        // 9. 如果揭示了结果，发布VotesRevealed事件
        if (allVotesComplete && session.getRevealedAt() != null) {
            Map<String, Integer> votesMap = new HashMap<>();
            session.getVotes().forEach((name, voteObj) ->
                votesMap.put(name, voteObj.getStoryPoint().getValue())
            );

            Map<String, Double> statistics = session.getStatistics();

            VotesRevealed revealedEvent = new VotesRevealed(
                    session.getStoryCardId(),
                    votesMap,
                    statistics.get("average"),
                    statistics.get("max").intValue(),
                    statistics.get("min").intValue(),
                    session.getRevealedAt()
            );
            eventPublisher.publishEvent(revealedEvent);
        }
        
        return session;
    }
    
    /**
     * 获取会话详情
     * 
     * @param sessionId 会话ID
     * @return 估点会话
     * @throws SessionNotFoundException 如果会话不存在
     */
    @Transactional(readOnly = true)
    public VotingSession getSession(Long sessionId) {
        log.debug("获取会话详情，会话ID: {}", sessionId);
        
        return votingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));
    }
    
    /**
     * 根据故事卡ID查找进行中的估点会话
     * 
     * <p>用于支持多人加入同一估点会话。
     * 
     * @param storyCardId 故事卡ID
     * @return 进行中的估点会话
     * @throws IllegalStateException 如果该故事卡没有进行中的估点会话
     */
    @Transactional(readOnly = true)
    public VotingSession findInProgressSessionByStoryCardId(Long storyCardId) {
        log.debug("查找故事卡的进行中会话，故事卡ID: {}", storyCardId);
        
        return votingSessionRepository.findByStoryCardIdAndStatus(
                storyCardId, 
                com.example.planningpoker.domain.model.SessionStatus.IN_PROGRESS
        ).orElseThrow(() -> new IllegalStateException("该故事卡没有进行中的估点会话"));
    }
    
    /**
     * 加入已有的估点会话
     * 
     * <p>用例流程：
     * <ol>
     *   <li>查找故事卡的进行中会话</li>
     *   <li>添加参与者到会话</li>
     *   <li>持久化会话</li>
     *   <li>发布领域事件（通过WebSocket通知其他参与者）</li>
     * </ol>
     * 
     * @param storyCardId 故事卡ID
     * @param userName 参与者用户名
     * @return 加入的估点会话
     * @throws SessionNotFoundException 如果该故事卡没有进行中的估点会话
     */
    public VotingSession joinSession(Long storyCardId, String userName) {
        log.info("用户尝试加入估点会话，故事卡ID: {}, 用户名: {}", storyCardId, userName);
        
        // 1. 查找进行中的会话
        VotingSession session = findInProgressSessionByStoryCardId(storyCardId);
        
        // 2. 添加参与者（领域方法会检查是否重复）
        session.addParticipant(userName);
        
        // 3. 持久化
        votingSessionRepository.save(session);

        log.info("用户成功加入估点会话，会话ID: {}, 用户名: {}", session.getStoryCardId(), userName);

        // 4. 发布领域事件（通过WebSocket通知）
        // 这里复用VotingSessionStarted事件，让其他参与者知道有新人加入
        VotingSessionStarted event = new VotingSessionStarted(
                session.getStoryCardId(),
                storyCardId,
                "", // title可以为空，前端已有信息
                userName,
                LocalDateTime.now()
        );
        eventPublisher.publishEvent(event);
        
        return session;
    }
    
    /**
     * 取消估点会话
     * 
     * <p>用例流程：
     * <ol>
     *   <li>加载估点会话</li>
     *   <li>将会话状态改为CANCELLED</li>
     *   <li>加载故事卡并恢复状态为NOT_ESTIMATED</li>
     *   <li>持久化会话和故事卡</li>
     *   <li>发布领域事件（可选）</li>
     * </ol>
     * 
     * @param sessionId 会话ID
     * @param userName 操作者用户名
     * @return 取消的估点会话
     * @throws SessionNotFoundException 如果会话不存在
     * @throws StoryCardNotFoundException 如果故事卡不存在
     */
    public VotingSession cancelSession(Long sessionId, String userName) {
        log.info("取消估点会话，会话ID: {}, 操作者: {}", sessionId, userName);
        
        // 1. 加载会话
        VotingSession session = votingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));
        
        // 2. 将会话状态改为CANCELLED
        session.setStatus(com.example.planningpoker.domain.model.SessionStatus.CANCELLED);
        
        // 3. 加载故事卡并恢复状态
        StoryCard storyCard = storyCardRepository.findById(session.getStoryCardId())
                .orElseThrow(() -> new StoryCardNotFoundException(session.getStoryCardId()));
        
        // 恢复故事卡状态为NOT_ESTIMATED
        storyCard.setStatus(com.example.planningpoker.domain.model.CardStatus.NOT_ESTIMATED);
        
        // 4. 持久化
        votingSessionRepository.save(session);
        storyCardRepository.save(storyCard);

        log.info("估点会话已取消，会话ID: {}, 故事卡ID: {}", session.getStoryCardId(), storyCard.getId());

        // 5. 发布领域事件（可选）
        // VotingSessionCancelled event = new VotingSessionCancelled(...);
        // eventPublisher.publishEvent(event);
        
        return session;
    }
    
    /**
     * 完成估点会话
     * 
     * <p>用例流程：
     * <ol>
     *   <li>验证输入参数</li>
     *   <li>加载估点会话</li>
     *   <li>调用会话的complete方法（业务规则在领域层）</li>
     *   <li>加载故事卡</li>
     *   <li>调用故事卡的completeEstimation方法</li>
     *   <li>持久化会话和故事卡</li>
     *   <li>发布VotingSessionCompleted事件</li>
     * </ol>
     * 
     * @param command 完成会话命令
     * @return 完成的估点会话
     * @throws SessionNotFoundException 如果会话不存在
     * @throws StoryCardNotFoundException 如果故事卡不存在
     */
    public VotingSession completeSession(CompleteSessionCommand command, String currentUser) {
        log.info("完成估点会话，会话ID: {}, 最终点数: {}, 操作者: {}", 
                command.getSessionId(), command.getFinalStoryPoint(), currentUser);
        
        // 1. 加载会话
        VotingSession session = votingSessionRepository.findById(command.getSessionId())
                .orElseThrow(() -> new SessionNotFoundException(command.getSessionId()));
        
        // 权限检查：仅主持人可完成估点
        if (!session.getHostName().equals(currentUser)) {
            throw new UnauthorizedException(
                    String.format("只有主持人 %s 可以完成估点", session.getHostName())
            );
        }
        
        // 2. 创建最终点数值对象
        StoryPoint finalPoint = StoryPoint.of(command.getFinalStoryPoint());
        
        // 3. 完成会话（领域方法）
        session.complete(finalPoint);
        
        // 4. 加载故事卡并完成估点
        StoryCard storyCard = storyCardRepository.findById(session.getStoryCardId())
                .orElseThrow(() -> new StoryCardNotFoundException(session.getStoryCardId()));
        
        storyCard.completeEstimation(finalPoint);
        
        // 5. 持久化
        votingSessionRepository.save(session);
        storyCardRepository.save(storyCard);

        log.info("估点会话完成，会话ID: {}, 故事卡ID: {}", session.getStoryCardId(), storyCard.getId());

        // 6. 发布领域事件
        VotingSessionCompleted event = new VotingSessionCompleted(
                session.getStoryCardId(),
                storyCard.getId(),
                finalPoint.getValue(),
                command.getParticipantName(),
                session.getCompletedAt()
        );
        eventPublisher.publishEvent(event);
        
        return session;
    }
    
    /**
     * 开始投票（启动倒计时）
     * 
     * <p>主持人点击"开始投票"后调用，启动30秒倒计时。
     * 
     * @param sessionId 会话ID
     * @param currentUser 当前用户
     * @return 更新后的会话
     * @throws SessionNotFoundException 如果会话不存在
     * @throws UnauthorizedException 如果非主持人尝试操作
     */
    public VotingSession startVoting(Long sessionId, String currentUser) {
        log.info("开始投票，会话ID: {}, 操作者: {}", sessionId, currentUser);
        
        // 1. 加载会话
        VotingSession session = votingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));
        
        // 权限检查：仅主持人可开始投票
        if (!session.getHostName().equals(currentUser)) {
            throw new UnauthorizedException(
                    String.format("只有主持人 %s 可以开始投票", session.getHostName())
            );
        }
        
        // 2. 调用领域方法启动投票
        session.startVoting();
        
        // 3. 持久化
        votingSessionRepository.save(session);

        log.info("投票已开始，会话ID: {}, 截止时间: {}",
                session.getStoryCardId(), session.getVotingDeadline());

        // 4. 发布领域事件（通过WebSocket广播）
        // TODO: 创建VotingStartedEvent并广播给前端
        
        return session;
    }
    
    /**
     * 标记未投票者为弃权
     * 
     * <p>倒计时结束后由定时任务自动调用。
     * 
     * @param sessionId 会话ID
     * @return 更新后的会话
     * @throws SessionNotFoundException 如果会话不存在
     */
    public VotingSession forfeitAbsentVoters(Long sessionId) {
        log.info("标记未投票者为弃权，会话ID: {}", sessionId);
        
        // 1. 加载会话
        VotingSession session = votingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));
        
        // 2. 调用领域方法标记弃权
        session.forfeitAbsentVoters();
        
        // 3. 持久化
        votingSessionRepository.save(session);

        log.info("弃权处理完成，会话ID: {}, 结果已揭示: {}",
                session.getStoryCardId(), session.getRevealedAt() != null);

        // 4. 如果结果已揭示，发布事件
        if (session.getRevealedAt() != null) {
            Map<String, Integer> voteResults = new HashMap<>();
            session.getVotes().forEach((name, vote) ->
                    voteResults.put(name, vote.getStoryPoint().getValue())
            );

            Map<String, Double> statistics = session.getStatistics();

            VotesRevealed event = new VotesRevealed(
                    session.getStoryCardId(),
                    voteResults,
                    statistics.get("average"),
                    statistics.get("max").intValue(),
                    statistics.get("min").intValue(),
                    session.getRevealedAt()
            );
            eventPublisher.publishEvent(event);
        }
        
        return session;
    }
}
