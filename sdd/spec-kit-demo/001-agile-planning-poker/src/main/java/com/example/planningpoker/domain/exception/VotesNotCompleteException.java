package com.example.planningpoker.domain.exception;

/**
 * 投票未全部完成异常
 * 
 * <p>当尝试完成会话，但不是所有参与者都已投票时抛出此异常。
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
public class VotesNotCompleteException extends RuntimeException {
    
    /**
     * 构造函数
     * 
     * @param votedCount 已投票人数
     * @param totalCount 总参与人数
     */
    public VotesNotCompleteException(int votedCount, int totalCount) {
        super(String.format("投票未全部完成，已投票: %d/%d", votedCount, totalCount));
    }
}
