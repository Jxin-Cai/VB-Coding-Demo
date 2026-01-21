package com.example.planningpoker.domain.exception;

/**
 * 估点会话未找到异常
 * 
 * <p>当根据ID查询估点会话但不存在时抛出此异常。
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
public class SessionNotFoundException extends RuntimeException {
    
    /**
     * 构造函数
     * 
     * @param sessionId 会话ID
     */
    public SessionNotFoundException(Long sessionId) {
        super("估点会话不存在，ID: " + sessionId);
    }
}
