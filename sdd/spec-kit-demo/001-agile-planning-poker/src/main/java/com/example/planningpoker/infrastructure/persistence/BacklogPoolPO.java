package com.example.planningpoker.infrastructure.persistence;

import java.time.LocalDateTime;

/**
 * 需求池持久化对象（PO）
 * 
 * <p>对应数据库表 backlog_pool。
 * 
 * <p>注意：PO仅在基础设施层使用，不传递到领域层。
 * 
 * @author 开发团队
 * @since 2.0.0
 * @date 2026-01-20
 */
public class BacklogPoolPO {
    
    /**
     * 需求池唯一标识
     */
    private Long id;
    
    /**
     * 需求池名称
     */
    private String name;
    
    /**
     * 需求池描述
     */
    private String description;
    
    /**
     * 创建者用户名
     */
    private String createdBy;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    // ========== Getter和Setter方法 ==========
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
