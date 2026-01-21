package com.example.planningpoker.application.command;

/**
 * 完成估点会话命令
 * 
 * <p>应用层命令对象，封装完成估点会话所需的参数。
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
public class CompleteSessionCommand {
    
    /**
     * 会话ID
     */
    private final Long sessionId;
    
    /**
     * 操作者用户名
     */
    private final String participantName;
    
    /**
     * 最终故事点数
     */
    private final Integer finalStoryPoint;
    
    /**
     * 构造函数
     * 
     * @param sessionId 会话ID
     * @param participantName 操作者用户名
     * @param finalStoryPoint 最终故事点数
     */
    public CompleteSessionCommand(Long sessionId, String participantName, Integer finalStoryPoint) {
        this.sessionId = sessionId;
        this.participantName = participantName;
        this.finalStoryPoint = finalStoryPoint;
    }
    
    public Long getSessionId() {
        return sessionId;
    }
    
    public String getParticipantName() {
        return participantName;
    }
    
    public Integer getFinalStoryPoint() {
        return finalStoryPoint;
    }
}
