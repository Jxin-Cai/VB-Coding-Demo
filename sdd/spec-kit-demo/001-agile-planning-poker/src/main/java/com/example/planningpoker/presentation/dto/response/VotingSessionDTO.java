package com.example.planningpoker.presentation.dto.response;

import java.util.List;

/**
 * 估点会话响应DTO
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
public class VotingSessionDTO {
    
    private Long id;
    private Long storyCardId;
    private String status;
    private String createdAt;
    private String hostName;
    private String votingStartedAt;
    private String votingDeadline;
    private Integer remainingSeconds;
    
    // 故事卡信息
    private StoryCardInfo storyCard;
    
    // 参与者和投票
    private List<String> participants;
    private Integer totalParticipants;
    private Integer totalVotes;
    private List<VoteInfo> votes;
    
    // 统计信息
    private Double averageStoryPoint;
    private Integer maxStoryPoint;
    private Integer minStoryPoint;
    private String revealedAt;
    
    // Getters and Setters
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getStoryCardId() { return storyCardId; }
    public void setStoryCardId(Long storyCardId) { this.storyCardId = storyCardId; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    
    public StoryCardInfo getStoryCard() { return storyCard; }
    public void setStoryCard(StoryCardInfo storyCard) { this.storyCard = storyCard; }
    
    public List<String> getParticipants() { return participants; }
    public void setParticipants(List<String> participants) { this.participants = participants; }
    
    public Integer getTotalParticipants() { return totalParticipants; }
    public void setTotalParticipants(Integer totalParticipants) { this.totalParticipants = totalParticipants; }
    
    public Integer getTotalVotes() { return totalVotes; }
    public void setTotalVotes(Integer totalVotes) { this.totalVotes = totalVotes; }
    
    public List<VoteInfo> getVotes() { return votes; }
    public void setVotes(List<VoteInfo> votes) { this.votes = votes; }
    
    public Double getAverageStoryPoint() { return averageStoryPoint; }
    public void setAverageStoryPoint(Double averageStoryPoint) { this.averageStoryPoint = averageStoryPoint; }
    
    public Integer getMaxStoryPoint() { return maxStoryPoint; }
    public void setMaxStoryPoint(Integer maxStoryPoint) { this.maxStoryPoint = maxStoryPoint; }
    
    public Integer getMinStoryPoint() { return minStoryPoint; }
    public void setMinStoryPoint(Integer minStoryPoint) { this.minStoryPoint = minStoryPoint; }
    
    public String getRevealedAt() { return revealedAt; }
    public void setRevealedAt(String revealedAt) { this.revealedAt = revealedAt; }
    
    public String getHostName() { return hostName; }
    public void setHostName(String hostName) { this.hostName = hostName; }
    
    public String getVotingStartedAt() { return votingStartedAt; }
    public void setVotingStartedAt(String votingStartedAt) { this.votingStartedAt = votingStartedAt; }
    
    public String getVotingDeadline() { return votingDeadline; }
    public void setVotingDeadline(String votingDeadline) { this.votingDeadline = votingDeadline; }
    
    public Integer getRemainingSeconds() { return remainingSeconds; }
    public void setRemainingSeconds(Integer remainingSeconds) { this.remainingSeconds = remainingSeconds; }
    
    /**
     * 故事卡基本信息
     */
    public static class StoryCardInfo {
        private Long id;
        private String title;
        private String description;
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
    
    /**
     * 投票信息
     */
    public static class VoteInfo {
        private String participantName;
        private Integer storyPoint;
        
        public VoteInfo() {}
        
        public VoteInfo(String participantName, Integer storyPoint) {
            this.participantName = participantName;
            this.storyPoint = storyPoint;
        }
        
        public String getParticipantName() { return participantName; }
        public void setParticipantName(String participantName) { this.participantName = participantName; }
        
        public Integer getStoryPoint() { return storyPoint; }
        public void setStoryPoint(Integer storyPoint) { this.storyPoint = storyPoint; }
    }
}
