package com.example.planningpoker.domain.exception;

/**
 * 估点会话已存在异常
 * 
 * <p>当尝试开始新会话，但已有进行中的会话时抛出此异常。
 * 系统设计为同一时间只能有一个活跃的估点会话。
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
public class SessionAlreadyExistsException extends RuntimeException {
    
    /**
     * 构造函数
     */
    public SessionAlreadyExistsException() {
        super("已有进行中的估点会话");
    }
    
    /**
     * 构造函数（带现有会话ID）
     * 
     * @param existingSessionId 现有会话ID
     */
    public SessionAlreadyExistsException(Long existingSessionId) {
        super("已有进行中的估点会话，会话ID: " + existingSessionId);
    }
}
