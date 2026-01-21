package com.example.planningpoker.application.query;

/**
 * 获取需求池详情查询
 * 
 * <p>封装查询需求池详情所需的参数。
 * 
 * @author 开发团队
 * @since 2.0.0
 * @date 2026-01-20
 */
public class GetPoolDetailQuery {
    
    /**
     * 需求池ID
     */
    private final Long poolId;
    
    /**
     * 是否包含故事卡列表
     */
    private final boolean includeStoryCards;
    
    private GetPoolDetailQuery(Long poolId, boolean includeStoryCards) {
        this.poolId = poolId;
        this.includeStoryCards = includeStoryCards;
    }
    
    public Long getPoolId() {
        return poolId;
    }
    
    public boolean isIncludeStoryCards() {
        return includeStoryCards;
    }
    
    /**
     * 静态工厂方法
     * 
     * @param poolId 需求池ID
     * @return GetPoolDetailQuery实例
     */
    public static GetPoolDetailQuery of(Long poolId) {
        return new GetPoolDetailQuery(poolId, false);
    }
    
    /**
     * 静态工厂方法（包含故事卡列表）
     * 
     * @param poolId 需求池ID
     * @return GetPoolDetailQuery实例
     */
    public static GetPoolDetailQuery withStoryCards(Long poolId) {
        return new GetPoolDetailQuery(poolId, true);
    }
}
