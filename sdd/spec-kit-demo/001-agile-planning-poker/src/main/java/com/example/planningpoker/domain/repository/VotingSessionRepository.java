package com.example.planningpoker.domain.repository;

import com.example.planningpoker.domain.model.SessionStatus;
import com.example.planningpoker.domain.model.aggregate.VotingSession;

import java.util.List;
import java.util.Optional;

/**
 * 估点会话仓储接口
 * 
 * <p>定义估点会话聚合根的持久化操作。
 * 此接口在领域层定义，在基础设施层实现。
 * 
 * <p>注意：所有方法使用领域对象（VotingSession），不使用PO。
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
public interface VotingSessionRepository {
    
    /**
     * 保存估点会话
     * 
     * <p>如果会话是新创建的（ID为null），执行插入操作；
     * 如果会话已存在（ID不为null），执行更新操作。
     * 
     * @param session 估点会话聚合根
     */
    void save(VotingSession session);
    
    /**
     * 根据ID查找会话
     * 
     * @param id 会话ID
     * @return Optional包装的会话对象，如果不存在返回Optional.empty()
     */
    Optional<VotingSession> findById(Long id);
    
    /**
     * 查找进行中的会话
     * 
     * <p>系统设计为同一时间只能有一个进行中的会话。
     * 
     * @return Optional包装的会话对象，如果不存在返回Optional.empty()
     */
    Optional<VotingSession> findActiveSession();
    
    /**
     * 删除会话
     * 
     * <p>级联删除关联的投票和参与者记录。
     * 
     * @param id 会话ID
     */
    void delete(Long id);
    
    /**
     * 根据故事卡ID和会话状态查找会话
     * 
     * <p>用于查找特定故事卡的进行中会话，支持多人加入同一估点会话。
     * 
     * @param storyCardId 故事卡ID
     * @param status 会话状态
     * @return Optional包装的会话对象，如果不存在返回Optional.empty()
     */
    Optional<VotingSession> findByStoryCardIdAndStatus(Long storyCardId, SessionStatus status);
    
    /**
     * 根据状态查找所有会话
     * 
     * <p>用于定时任务扫描进行中的会话，检查投票超时。
     * 
     * @param status 会话状态
     * @return 会话列表
     */
    List<VotingSession> findByStatus(SessionStatus status);
}
