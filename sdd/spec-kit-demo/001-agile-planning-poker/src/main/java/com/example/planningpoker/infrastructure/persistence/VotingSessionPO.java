package com.example.planningpoker.infrastructure.persistence;

import java.time.LocalDateTime;

/**
 * 估点会话持久化对象（对应voting_session表）
 *
 * <p>注意：这是基础设施层的PO类，仅用于数据库映射，不传递到领域层。
 *
 * <p>story_card_id是主键，与故事卡1:1关系。
 *
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
public class VotingSessionPO {

    private Long storyCardId;
    private String status;
    private LocalDateTime revealedAt;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String hostName;
    private LocalDateTime votingStartedAt;
    private LocalDateTime votingDeadline;

    // Getters and Setters
    public Long getStoryCardId() { return storyCardId; }
    public void setStoryCardId(Long storyCardId) { this.storyCardId = storyCardId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getRevealedAt() { return revealedAt; }
    public void setRevealedAt(LocalDateTime revealedAt) { this.revealedAt = revealedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public String getHostName() { return hostName; }
    public void setHostName(String hostName) { this.hostName = hostName; }

    public LocalDateTime getVotingStartedAt() { return votingStartedAt; }
    public void setVotingStartedAt(LocalDateTime votingStartedAt) { this.votingStartedAt = votingStartedAt; }

    public LocalDateTime getVotingDeadline() { return votingDeadline; }
    public void setVotingDeadline(LocalDateTime votingDeadline) { this.votingDeadline = votingDeadline; }
}
