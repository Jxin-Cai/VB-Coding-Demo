package com.example.planningpoker.infrastructure.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Optional;

/**
 * 估点会话Mapper接口
 * 
 * <p>MyBatis Mapper接口，定义估点会话相关的数据库操作。
 * 对应的SQL映射在 mapper/VotingSessionMapper.xml 中定义。
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
@Mapper
public interface VotingSessionMapper {
    
    /**
     * 插入估点会话
     * 
     * @param session 会话PO
     */
    void insert(VotingSessionPO session);
    
    /**
     * 插入投票记录
     * 
     * <p>使用ON DUPLICATE KEY UPDATE实现upsert语义（插入或更新）。
     * 
     * @param vote 投票PO
     */
    void insertVote(VotePO vote);
    
    /**
     * 插入参与者记录
     * 
     * <p>使用INSERT IGNORE避免重复插入。
     * 
     * @param participant 参与者PO
     */
    void insertParticipant(ParticipantPO participant);
    
    /**
     * 根据ID查询会话
     * 
     * @param id 会话ID
     * @return 会话PO，如果不存在返回null
     */
    VotingSessionPO selectById(@Param("id") Long id);
    
    /**
     * 查询进行中的会话
     * 
     * <p>同一时间只能有一个进行中的会话。
     * 
     * @return 进行中的会话PO，如果不存在返回null
     */
    VotingSessionPO selectActiveSession();
    
    /**
     * 根据状态查询所有会话
     * 
     * <p>用于定时任务扫描进行中的会话。
     * 
     * @param status 会话状态
     * @return 会话PO列表
     */
    List<VotingSessionPO> selectByStatus(@Param("status") String status);
    
    /**
     * 根据故事卡ID和会话状态查询会话
     * 
     * <p>用于支持多人加入同一估点会话。
     * 
     * @param storyCardId 故事卡ID
     * @param status 会话状态
     * @return Optional包装的会话PO
     */
    Optional<VotingSessionPO> findByStoryCardIdAndStatus(@Param("storyCardId") Long storyCardId, @Param("status") String status);
    
    /**
     * 查询故事卡的所有投票
     *
     * @param storyCardId 故事卡ID
     * @return 投票PO列表
     */
    List<VotePO> selectVotesByStoryCardId(@Param("storyCardId") Long storyCardId);

    /**
     * 查询故事卡的所有参与者
     *
     * @param storyCardId 故事卡ID
     * @return 参与者PO列表
     */
    List<ParticipantPO> selectParticipantsByStoryCardId(@Param("storyCardId") Long storyCardId);
    
    /**
     * 更新会话
     * 
     * @param session 会话PO
     */
    void update(VotingSessionPO session);
    
    /**
     * 删除会话（级联删除投票和参与者）
     * 
     * @param id 会话ID
     */
    void delete(@Param("id") Long id);
    
    /**
     * 删除故事卡的所有投票
     *
     * @param storyCardId 故事卡ID
     */
    void deleteVotesByStoryCardId(@Param("storyCardId") Long storyCardId);

    /**
     * 删除故事卡的所有参与者
     *
     * @param storyCardId 故事卡ID
     */
    void deleteParticipantsByStoryCardId(@Param("storyCardId") Long storyCardId);
}
