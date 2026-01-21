package com.example.planningpoker.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket配置类
 * 
 * <p>配置Spring WebSocket和STOMP消息代理，用于实现估点会话的实时通知功能。
 * 
 * <p>功能：
 * <ul>
 *   <li>配置STOMP端点：/ws</li>
 *   <li>配置消息代理：/topic（用于广播）</li>
 *   <li>配置应用前缀：/app（用于客户端发送消息）</li>
 *   <li>支持SockJS降级</li>
 * </ul>
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 配置消息代理
     * 
     * @param config 消息代理注册器
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单消息代理，用于广播消息给订阅的客户端
        // 客户端订阅 /topic/voting-session/{sessionId} 接收实时更新
        config.enableSimpleBroker("/topic");
        
        // 设置应用目的地前缀，客户端发送消息到服务器的目的地以 /app 开头
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * 注册STOMP端点
     * 
     * @param registry STOMP端点注册器
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册 /ws 端点，用于WebSocket连接
        registry.addEndpoint("/ws")
                // 允许所有源（局域网访问）
                .setAllowedOriginPatterns("*")
                // 启用SockJS降级支持（当WebSocket不可用时）
                .withSockJS();
    }
}
