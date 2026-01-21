package com.example.planningpoker.application.query;

/**
 * 获取需求池列表查询
 * 
 * <p>封装查询需求池列表所需的参数。
 * 
 * @author 开发团队
 * @since 2.0.0
 * @date 2026-01-20
 */
public class GetPoolListQuery {
    
    /**
     * 创建者用户名（可选，用于筛选自己创建的需求池）
     */
    private final String createdBy;
    
    private GetPoolListQuery(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    /**
     * 查询所有需求池
     * 
     * @return GetPoolListQuery实例
     */
    public static GetPoolListQuery all() {
        return new GetPoolListQuery(null);
    }
    
    /**
     * 按创建者查询需求池
     * 
     * @param createdBy 创建者用户名
     * @return GetPoolListQuery实例
     */
    public static GetPoolListQuery byCreatedBy(String createdBy) {
        return new GetPoolListQuery(createdBy);
    }
}
