package com.example.planningpoker.domain.exception;

/**
 * 未授权异常
 * 
 * <p>当用户尝试执行无权限的操作时抛出。
 * 
 * <p>使用场景：
 * <ul>
 *   <li>非主持人尝试开启估点</li>
 *   <li>非主持人尝试编辑/删除故事卡</li>
 *   <li>非主持人尝试完成估点</li>
 * </ul>
 * 
 * @author 开发团队
 * @since 2.0.0
 * @date 2026-01-20
 */
public class UnauthorizedException extends RuntimeException {
    
    /**
     * 构造函数
     * 
     * @param message 错误消息
     */
    public UnauthorizedException(String message) {
        super(message);
    }
    
    /**
     * 构造函数
     * 
     * @param message 错误消息
     * @param cause 原因异常
     */
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
