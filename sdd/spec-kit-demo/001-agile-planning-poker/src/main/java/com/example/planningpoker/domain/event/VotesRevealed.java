package com.example.planningpoker.domain.event;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 投票结果已揭示领域事件
 * 
 * <p>当所有参与者投票完成，投票结果揭示时发布此事件。
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
public class VotesRevealed {
    
    /**
     * 会话ID
     */
    private final Long sessionId;
    
    /**
     * 所有投票（参与者名称 → 点数值）
     */
    private final Map<String, Integer> votes;
    
    /**
     * 平均点数
     */
    private final Double averagePoint;
    
    /**
     * 最高点数
     */
    private final Integer maxPoint;
    
    /**
     * 最低点数
     */
    private final Integer minPoint;
    
    /**
     * 揭示时间
     */
    private final LocalDateTime revealedAt;
    
    /**
     * 构造函数
     */
    public VotesRevealed(Long sessionId, Map<String, Integer> votes, 
                        Double averagePoint, Integer maxPoint, Integer minPoint,
                        LocalDateTime revealedAt) {
        this.sessionId = sessionId;
        this.votes = votes;
        this.averagePoint = averagePoint;
        this.maxPoint = maxPoint;
        this.minPoint = minPoint;
        this.revealedAt = revealedAt;
    }
    
    public Long getSessionId() {
        return sessionId;
    }
    
    public Map<String, Integer> getVotes() {
        return votes;
    }
    
    public Double getAveragePoint() {
        return averagePoint;
    }
    
    public Integer getMaxPoint() {
        return maxPoint;
    }
    
    public Integer getMinPoint() {
        return minPoint;
    }
    
    public LocalDateTime getRevealedAt() {
        return revealedAt;
    }
}
