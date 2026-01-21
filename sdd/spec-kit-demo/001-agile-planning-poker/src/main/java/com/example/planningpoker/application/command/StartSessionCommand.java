package com.example.planningpoker.application.command;

/**
 * 开始估点会话命令
 * 
 * <p>应用层命令对象，封装开始估点会话所需的参数。
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
public class StartSessionCommand {
    
    /**
     * 故事卡ID
     */
    private final Long storyCardId;
    
    /**
     * 发起者用户名
     */
    private final String participantName;
    
    /**
     * 构造函数
     * 
     * @param storyCardId 故事卡ID
     * @param participantName 发起者用户名
     */
    public StartSessionCommand(Long storyCardId, String participantName) {
        this.storyCardId = storyCardId;
        this.participantName = participantName;
    }
    
    public Long getStoryCardId() {
        return storyCardId;
    }
    
    public String getParticipantName() {
        return participantName;
    }
}
