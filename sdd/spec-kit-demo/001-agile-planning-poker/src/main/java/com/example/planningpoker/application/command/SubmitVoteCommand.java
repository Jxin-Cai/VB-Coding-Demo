package com.example.planningpoker.application.command;

/**
 * 提交投票命令
 * 
 * <p>应用层命令对象，封装提交投票所需的参数。
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
public class SubmitVoteCommand {
    
    /**
     * 会话ID
     */
    private final Long sessionId;
    
    /**
     * 参与者用户名
     */
    private final String participantName;
    
    /**
     * 故事点数值（1/2/3/5/8）
     */
    private final Integer storyPointValue;
    
    /**
     * 构造函数
     * 
     * @param sessionId 会话ID
     * @param participantName 参与者用户名
     * @param storyPointValue 故事点数值
     */
    public SubmitVoteCommand(Long sessionId, String participantName, Integer storyPointValue) {
        this.sessionId = sessionId;
        this.participantName = participantName;
        this.storyPointValue = storyPointValue;
    }
    
    public Long getSessionId() {
        return sessionId;
    }
    
    public String getParticipantName() {
        return participantName;
    }
    
    public Integer getStoryPointValue() {
        return storyPointValue;
    }
}
