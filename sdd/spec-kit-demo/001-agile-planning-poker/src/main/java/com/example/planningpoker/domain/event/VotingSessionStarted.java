package com.example.planningpoker.domain.event;

import java.time.LocalDateTime;

/**
 * 估点会话已开始领域事件
 * 
 * <p>当估点会话开始时发布此事件，用于：
 * <ul>
 *   <li>通过WebSocket通知所有在线用户</li>
 *   <li>记录审计日志</li>
 *   <li>触发其他业务流程</li>
 * </ul>
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
public class VotingSessionStarted {
    
    /**
     * 会话ID
     */
    private final Long sessionId;
    
    /**
     * 故事卡ID
     */
    private final Long storyCardId;
    
    /**
     * 故事卡标题
     */
    private final String storyCardTitle;
    
    /**
     * 发起者用户名
     */
    private final String startedBy;
    
    /**
     * 开始时间
     */
    private final LocalDateTime startedAt;
    
    /**
     * 构造函数
     * 
     * @param sessionId 会话ID
     * @param storyCardId 故事卡ID
     * @param storyCardTitle 故事卡标题
     * @param startedBy 发起者用户名
     * @param startedAt 开始时间
     */
    public VotingSessionStarted(Long sessionId, Long storyCardId, String storyCardTitle, 
                               String startedBy, LocalDateTime startedAt) {
        this.sessionId = sessionId;
        this.storyCardId = storyCardId;
        this.storyCardTitle = storyCardTitle;
        this.startedBy = startedBy;
        this.startedAt = startedAt;
    }
    
    public Long getSessionId() {
        return sessionId;
    }
    
    public Long getStoryCardId() {
        return storyCardId;
    }
    
    public String getStoryCardTitle() {
        return storyCardTitle;
    }
    
    public String getStartedBy() {
        return startedBy;
    }
    
    public LocalDateTime getStartedAt() {
        return startedAt;
    }
    
    @Override
    public String toString() {
        return "VotingSessionStarted{" +
                "sessionId=" + sessionId +
                ", storyCardId=" + storyCardId +
                ", storyCardTitle='" + storyCardTitle + '\'' +
                ", startedBy='" + startedBy + '\'' +
                ", startedAt=" + startedAt +
                '}';
    }
}
