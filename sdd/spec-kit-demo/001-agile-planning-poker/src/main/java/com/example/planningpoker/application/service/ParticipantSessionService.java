package com.example.planningpoker.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 参与者会话管理服务
 * 
 * <p>简化实现的登录会话管理：
 * <ul>
 *   <li>生成唯一的sessionToken</li>
 *   <li>在内存中维护在线用户列表</li>
 *   <li>验证用户名有效性</li>
 * </ul>
 * 
 * <p>注意：这是简化实现，会话数据不持久化。
 * 未来版本可以集成Spring Security或JWT实现完整的认证授权。
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
@Service
public class ParticipantSessionService {
    
    private static final Logger log = LoggerFactory.getLogger(ParticipantSessionService.class);
    
    /**
     * 在线用户Map: sessionToken → userName
     */
    private final Map<String, String> onlineUsers = new ConcurrentHashMap<>();
    
    /**
     * 用户名到令牌的反向映射: userName → sessionToken
     */
    private final Map<String, String> userNameToToken = new ConcurrentHashMap<>();
    
    /**
     * 用户登录
     * 
     * <p>生成唯一的sessionToken，记录到在线用户列表。
     * 
     * @param userName 用户名
     * @return sessionToken
     * @throws IllegalArgumentException 如果用户名无效或已存在
     */
    public String login(String userName) {
        log.info("用户登录，用户名: {}", userName);
        
        // 验证用户名
        if (userName == null || userName.isBlank()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (userName.length() < 2 || userName.length() > 20) {
            throw new IllegalArgumentException("用户名长度必须在2-20之间");
        }
        
        // 检查用户名是否已存在（当前会话中）
        if (userNameToToken.containsKey(userName)) {
            log.info("用户已登录，返回现有令牌: {}", userName);
            return userNameToToken.get(userName);
        }
        
        // 生成sessionToken
        String sessionToken = UUID.randomUUID().toString();
        
        // 记录在线用户
        onlineUsers.put(sessionToken, userName);
        userNameToToken.put(userName, sessionToken);
        
        log.info("用户登录成功，用户名: {}, 令牌: {}", userName, sessionToken);
        return sessionToken;
    }
    
    /**
     * 根据令牌获取用户名
     * 
     * @param sessionToken 会话令牌
     * @return 用户名，如果令牌无效返回null
     */
    public String getUserName(String sessionToken) {
        return onlineUsers.get(sessionToken);
    }
    
    /**
     * 验证令牌是否有效
     * 
     * @param sessionToken 会话令牌
     * @return 如果有效返回true
     */
    public boolean isValidToken(String sessionToken) {
        return onlineUsers.containsKey(sessionToken);
    }
    
    /**
     * 用户登出
     * 
     * @param sessionToken 会话令牌
     */
    public void logout(String sessionToken) {
        String userName = onlineUsers.remove(sessionToken);
        if (userName != null) {
            userNameToToken.remove(userName);
            log.info("用户登出，用户名: {}", userName);
        }
    }
    
    /**
     * 获取在线用户数
     * 
     * @return 在线用户数
     */
    public int getOnlineUserCount() {
        return onlineUsers.size();
    }
}
