package com.example.planningpoker.infrastructure.persistence;

import java.time.LocalDateTime;

/**
 * 投票持久化对象（对应vote表）
 *
 * <p>注意：这是基础设施层的PO类，仅用于数据库映射，不传递到领域层。
 *
 * <p>storyCardId关联到voting_session.story_card_id，实现1:1会话关系。
 *
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
public class VotePO {

    private Long id;
    private Long storyCardId;
    private String participantName;
    private Integer storyPoint;
    private LocalDateTime votedAt;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStoryCardId() { return storyCardId; }
    public void setStoryCardId(Long storyCardId) { this.storyCardId = storyCardId; }

    public String getParticipantName() { return participantName; }
    public void setParticipantName(String participantName) { this.participantName = participantName; }

    public Integer getStoryPoint() { return storyPoint; }
    public void setStoryPoint(Integer storyPoint) { this.storyPoint = storyPoint; }

    public LocalDateTime getVotedAt() { return votedAt; }
    public void setVotedAt(LocalDateTime votedAt) { this.votedAt = votedAt; }
}
