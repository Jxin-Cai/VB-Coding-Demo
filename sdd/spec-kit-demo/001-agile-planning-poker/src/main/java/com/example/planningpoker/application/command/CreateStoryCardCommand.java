package com.example.planningpoker.application.command;

/**
 * 创建故事卡命令
 * 
 * <p>应用层命令对象，封装创建故事卡所需的参数。
 * 
 * <p>注意：Command对象仅在应用层使用，不传递到领域层或表现层。
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
public class CreateStoryCardCommand {
    
    /**
     * 故事卡标题
     */
    private final String title;
    
    /**
     * 故事卡描述
     */
    private final String description;
    
    /**
     * 创建者用户名
     */
    private final String createdBy;
    
    /**
     * 所属需求池ID
     */
    private final Long poolId;
    
    /**
     * 构造函数
     * 
     * @param title 故事卡标题
     * @param description 故事卡描述
     * @param createdBy 创建者用户名
     * @param poolId 所属需求池ID
     */
    public CreateStoryCardCommand(String title, String description, String createdBy, Long poolId) {
        this.title = title;
        this.description = description;
        this.createdBy = createdBy;
        this.poolId = poolId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public Long getPoolId() {
        return poolId;
    }
}
