package com.example.planningpoker.application.command;

/**
 * 创建需求池命令
 * 
 * <p>封装创建需求池所需的所有输入参数。
 * 
 * @author 开发团队
 * @since 2.0.0
 * @date 2026-01-20
 */
public class CreatePoolCommand {
    
    /**
     * 需求池名称（1-100字符）
     */
    private final String name;
    
    /**
     * 需求池描述（最多500字符）
     */
    private final String description;
    
    /**
     * 创建者用户名
     */
    private final String createdBy;
    
    private CreatePoolCommand(String name, String description, String createdBy) {
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    /**
     * 静态工厂方法
     * 
     * @param name 需求池名称
     * @param description 需求池描述
     * @param createdBy 创建者用户名
     * @return CreatePoolCommand实例
     */
    public static CreatePoolCommand of(String name, String description, String createdBy) {
        return new CreatePoolCommand(name, description, createdBy);
    }
}
