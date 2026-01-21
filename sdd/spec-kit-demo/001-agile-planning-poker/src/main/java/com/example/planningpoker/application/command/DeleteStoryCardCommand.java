package com.example.planningpoker.application.command;

/**
 * 删除故事卡命令
 * 
 * <p>应用层命令对象，封装删除故事卡所需的参数。
 * 
 * <p>注意：Command对象仅在应用层使用，不传递到领域层或表现层。
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
public class DeleteStoryCardCommand {
    
    /**
     * 故事卡ID
     */
    private final Long storyCardId;
    
    /**
     * 构造函数
     * 
     * @param storyCardId 故事卡ID
     */
    public DeleteStoryCardCommand(Long storyCardId) {
        this.storyCardId = storyCardId;
    }
    
    public Long getStoryCardId() {
        return storyCardId;
    }
}
