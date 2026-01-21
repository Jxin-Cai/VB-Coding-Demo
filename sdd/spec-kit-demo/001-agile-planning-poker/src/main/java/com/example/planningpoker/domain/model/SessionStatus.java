package com.example.planningpoker.domain.model;

/**
 * 估点会话状态枚举
 * 
 * <p>表示估点会话的生命周期状态。
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
public enum SessionStatus {
    
    /**
     * 进行中 - 会话已开始，参与者正在投票
     */
    IN_PROGRESS("进行中"),
    
    /**
     * 已完成 - 会话已结束，最终点数已确定
     */
    COMPLETED("已完成"),
    
    /**
     * 已取消 - 会话被取消，未完成估点
     */
    CANCELLED("已取消");
    
    /**
     * 状态描述（中文）
     */
    private final String description;
    
    /**
     * 私有构造函数
     * 
     * @param description 状态描述
     */
    SessionStatus(String description) {
        this.description = description;
    }
    
    /**
     * 获取状态描述
     * 
     * @return 状态描述
     */
    public String getDescription() {
        return description;
    }
}
