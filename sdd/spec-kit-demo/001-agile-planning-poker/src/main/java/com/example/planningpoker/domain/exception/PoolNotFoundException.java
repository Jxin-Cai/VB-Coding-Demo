package com.example.planningpoker.domain.exception;

/**
 * 需求池未找到异常
 *
 * <p>当尝试访问不存在的需求池时抛出此异常。
 *
 * @author 开发团队
 * @since 2.0.0
 * @date 2026-01-20
 */
public class PoolNotFoundException extends RuntimeException {

    private final Long poolId;

    /**
     * 构造需求池未找到异常
     *
     * @param poolId 需求池ID
     */
    public PoolNotFoundException(Long poolId) {
        super(String.format("需求池不存在: ID=%d", poolId));
        this.poolId = poolId;
    }

    /**
     * 构造需求池未找到异常（带自定义消息）
     *
     * @param poolId 需求池ID
     * @param message 自定义错误消息
     */
    public PoolNotFoundException(Long poolId, String message) {
        super(message);
        this.poolId = poolId;
    }

    /**
     * 获取需求池ID
     *
     * @return 需求池ID
     */
    public Long getPoolId() {
        return poolId;
    }
}
