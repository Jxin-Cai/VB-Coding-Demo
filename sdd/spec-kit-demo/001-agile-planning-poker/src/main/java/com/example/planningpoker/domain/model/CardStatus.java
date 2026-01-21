package com.example.planningpoker.domain.model;

/**
 * 故事卡估点状态枚举
 * 
 * <p>表示故事卡在估点流程中的状态。
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
public enum CardStatus {
    
    /**
     * 未估点 - 故事卡尚未开始估点
     */
    NOT_ESTIMATED("未估点"),
    
    /**
     * 估点中 - 故事卡正在进行估点
     */
    ESTIMATING("估点中"),
    
    /**
     * 已估点 - 故事卡估点已完成，有最终点数
     */
    ESTIMATED("已估点");
    
    /**
     * 状态描述（中文）
     */
    private final String description;
    
    /**
     * 私有构造函数
     * 
     * @param description 状态描述
     */
    CardStatus(String description) {
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
