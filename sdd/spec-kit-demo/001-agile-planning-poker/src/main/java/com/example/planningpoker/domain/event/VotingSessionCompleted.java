package com.example.planningpoker.domain.event;

import java.time.LocalDateTime;

/**
 * 估点会话已完成领域事件
 * 
 * <p>当估点会话完成（选择最终点数）时发布此事件。
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
public class VotingSessionCompleted {
    
    /**
     * 会话ID
     */
    private final Long sessionId;
    
    /**
     * 故事卡ID
     */
    private final Long storyCardId;
    
    /**
     * 最终故事点数
     */
    private final Integer finalStoryPoint;
    
    /**
     * 完成者用户名
     */
    private final String completedBy;
    
    /**
     * 完成时间
     */
    private final LocalDateTime completedAt;
    
    /**
     * 构造函数
     */
    public VotingSessionCompleted(Long sessionId, Long storyCardId, Integer finalStoryPoint,
                                 String completedBy, LocalDateTime completedAt) {
        this.sessionId = sessionId;
        this.storyCardId = storyCardId;
        this.finalStoryPoint = finalStoryPoint;
        this.completedBy = completedBy;
        this.completedAt = completedAt;
    }
    
    public Long getSessionId() {
        return sessionId;
    }
    
    public Long getStoryCardId() {
        return storyCardId;
    }
    
    public Integer getFinalStoryPoint() {
        return finalStoryPoint;
    }
    
    public String getCompletedBy() {
        return completedBy;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}
