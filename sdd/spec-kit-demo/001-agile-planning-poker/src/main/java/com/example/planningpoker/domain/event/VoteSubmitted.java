package com.example.planningpoker.domain.event;

import java.time.LocalDateTime;

/**
 * 投票已提交领域事件
 * 
 * <p>当参与者提交投票时发布此事件，用于实时通知其他参与者投票进度。
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
public class VoteSubmitted {
    
    /**
     * 会话ID
     */
    private final Long sessionId;
    
    /**
     * 参与者用户名
     */
    private final String participantName;
    
    /**
     * 是否是更新投票（true=修改投票，false=新投票）
     */
    private final Boolean isUpdate;
    
    /**
     * 投票时间
     */
    private final LocalDateTime votedAt;
    
    /**
     * 总参与人数
     */
    private final Integer totalParticipants;
    
    /**
     * 已投票人数
     */
    private final Integer totalVotes;
    
    /**
     * 构造函数
     */
    public VoteSubmitted(Long sessionId, String participantName, Boolean isUpdate,
                        LocalDateTime votedAt, Integer totalParticipants, Integer totalVotes) {
        this.sessionId = sessionId;
        this.participantName = participantName;
        this.isUpdate = isUpdate;
        this.votedAt = votedAt;
        this.totalParticipants = totalParticipants;
        this.totalVotes = totalVotes;
    }
    
    public Long getSessionId() {
        return sessionId;
    }
    
    public String getParticipantName() {
        return participantName;
    }
    
    public Boolean getIsUpdate() {
        return isUpdate;
    }
    
    public LocalDateTime getVotedAt() {
        return votedAt;
    }
    
    public Integer getTotalParticipants() {
        return totalParticipants;
    }
    
    public Integer getTotalVotes() {
        return totalVotes;
    }
}
