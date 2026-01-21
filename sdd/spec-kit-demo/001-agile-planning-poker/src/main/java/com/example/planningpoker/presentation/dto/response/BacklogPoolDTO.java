package com.example.planningpoker.presentation.dto.response;

import com.example.planningpoker.domain.model.aggregate.BacklogPool;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 需求池响应DTO
 * 
 * <p>表现层DTO，用于向前端返回需求池信息。
 * 
 * @author 开发团队
 * @since 2.0.0
 * @date 2026-01-20
 */
public class BacklogPoolDTO {
    
    /**
     * 需求池ID
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    /**
     * 故事卡数量
     */
    private Integer storyCardCount;
    
    // Getters and Setters
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
    
    public Integer getStoryCardCount() {
        return storyCardCount;
    }
    
    public void setStoryCardCount(Integer storyCardCount) {
        this.storyCardCount = storyCardCount;
    }
    
    /**
     * 从领域模型转换
     * 
     * @param pool 需求池领域模型
     * @return BacklogPoolDTO
     */
    public static BacklogPoolDTO from(BacklogPool pool) {
        BacklogPoolDTO dto = new BacklogPoolDTO();
        dto.setId(pool.getId());
        dto.setName(pool.getName());
        dto.setDescription(pool.getDescription());
        dto.setCreatedBy(pool.getCreatedBy());
        dto.setCreatedAt(pool.getCreatedAt());
        return dto;
    }
    
    /**
     * 从领域模型转换（包含故事卡数量）
     * 
     * @param pool 需求池领域模型
     * @param storyCardCount 故事卡数量
     * @return BacklogPoolDTO
     */
    public static BacklogPoolDTO from(BacklogPool pool, int storyCardCount) {
        BacklogPoolDTO dto = from(pool);
        dto.setStoryCardCount(storyCardCount);
        return dto;
    }
}
