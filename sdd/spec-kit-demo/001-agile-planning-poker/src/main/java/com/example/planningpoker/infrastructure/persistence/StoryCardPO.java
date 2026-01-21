package com.example.planningpoker.infrastructure.persistence;

import java.time.LocalDateTime;

/**
 * 故事卡持久化对象（对应story_card表）
 * 
 * <p>注意：这是基础设施层的PO类，仅用于数据库映射，不传递到领域层。
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
public class StoryCardPO {
    
    private Long id;
    private String title;
    private String description;
    private String status;
    private Integer storyPoint;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime estimatedAt;
    private Long poolId;
    private String hostName;
    private Long votingSessionId;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Integer getStoryPoint() { return storyPoint; }
    public void setStoryPoint(Integer storyPoint) { this.storyPoint = storyPoint; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getEstimatedAt() { return estimatedAt; }
    public void setEstimatedAt(LocalDateTime estimatedAt) { this.estimatedAt = estimatedAt; }
    
    public Long getPoolId() { return poolId; }
    public void setPoolId(Long poolId) { this.poolId = poolId; }
    
    public String getHostName() { return hostName; }
    public void setHostName(String hostName) { this.hostName = hostName; }
    
    public Long getVotingSessionId() { return votingSessionId; }
    public void setVotingSessionId(Long votingSessionId) { this.votingSessionId = votingSessionId; }
}
