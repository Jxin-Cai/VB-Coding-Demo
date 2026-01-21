package com.example.planningpoker.application.command;

/**
 * 更新故事卡命令
 * 
 * <p>应用层命令对象，封装更新故事卡所需的参数。
 * 
 * <p>注意：Command对象仅在应用层使用，不传递到领域层或表现层。
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
public class UpdateStoryCardCommand {
    
    /**
     * 故事卡ID
     */
    private final Long storyCardId;
    
    /**
     * 新标题
     */
    private final String title;
    
    /**
     * 新描述
     */
    private final String description;
    
    /**
     * 构造函数
     * 
     * @param storyCardId 故事卡ID
     * @param title 新标题
     * @param description 新描述
     */
    public UpdateStoryCardCommand(Long storyCardId, String title, String description) {
        this.storyCardId = storyCardId;
        this.title = title;
        this.description = description;
    }
    
    public Long getStoryCardId() {
        return storyCardId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getDescription() {
        return description;
    }
}
