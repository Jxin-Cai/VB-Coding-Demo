package com.example.planningpoker.presentation.dto.response;

/**
 * 故事卡响应DTO
 * 
 * <p>表现层DTO，用于HTTP响应。
 * 
 * <p>注意：DTO仅在表现层使用，不传递到应用层以下。
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
public class StoryCardDTO {
    
    private Long id;
    private String title;
    private String description;
    private String status;
    private Integer storyPoint;
    private String createdBy;
    private String createdAt;
    private String estimatedAt;
    private Long poolId;
    private String hostName;
    private Long votingSessionId;
    
    // Private constructor for Builder
    private StoryCardDTO() {
    }
    
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
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    
    public String getEstimatedAt() { return estimatedAt; }
    public void setEstimatedAt(String estimatedAt) { this.estimatedAt = estimatedAt; }
    
    public Long getPoolId() { return poolId; }
    public void setPoolId(Long poolId) { this.poolId = poolId; }
    
    public String getHostName() { return hostName; }
    public void setHostName(String hostName) { this.hostName = hostName; }
    
    public Long getVotingSessionId() { return votingSessionId; }
    public void setVotingSessionId(Long votingSessionId) { this.votingSessionId = votingSessionId; }
    
    // Builder
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final StoryCardDTO dto = new StoryCardDTO();
        
        public Builder id(Long id) { dto.id = id; return this; }
        public Builder title(String title) { dto.title = title; return this; }
        public Builder description(String description) { dto.description = description; return this; }
        public Builder status(String status) { dto.status = status; return this; }
        public Builder storyPoint(Integer storyPoint) { dto.storyPoint = storyPoint; return this; }
        public Builder createdBy(String createdBy) { dto.createdBy = createdBy; return this; }
        public Builder createdAt(String createdAt) { dto.createdAt = createdAt; return this; }
        public Builder estimatedAt(String estimatedAt) { dto.estimatedAt = estimatedAt; return this; }
        public Builder poolId(Long poolId) { dto.poolId = poolId; return this; }
        public Builder hostName(String hostName) { dto.hostName = hostName; return this; }
        public Builder votingSessionId(Long votingSessionId) { dto.votingSessionId = votingSessionId; return this; }
        
        public StoryCardDTO build() {
            return dto;
        }
    }
    
    /**
     * 从领域模型转换为DTO
     * 
     * @param card 故事卡领域模型
     * @return StoryCardDTO
     */
    public static StoryCardDTO from(com.example.planningpoker.domain.model.aggregate.StoryCard card) {
        Builder builder = builder()
                .id(card.getId())
                .title(card.getTitle())
                .description(card.getDescription())
                .status(card.getStatus().name())
                .createdBy(card.getCreatedBy())
                .poolId(card.getPoolId())
                .hostName(card.getHostName())
                .votingSessionId(card.getVotingSessionId());
        
        // 可选字段
        if (card.getStoryPoint() != null) {
            builder.storyPoint(card.getStoryPoint().getValue());
        }
        if (card.getCreatedAt() != null) {
            builder.createdAt(card.getCreatedAt().toString());
        }
        if (card.getEstimatedAt() != null) {
            builder.estimatedAt(card.getEstimatedAt().toString());
        }
        
        return builder.build();
    }
}
