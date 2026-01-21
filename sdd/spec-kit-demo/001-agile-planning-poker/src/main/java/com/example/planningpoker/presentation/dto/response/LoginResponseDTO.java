package com.example.planningpoker.presentation.dto.response;

/**
 * 登录响应DTO
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
public class LoginResponseDTO {
    
    /**
     * 会话令牌
     */
    private String sessionToken;
    
    /**
     * 用户名
     */
    private String userName;
    
    public LoginResponseDTO() {
    }
    
    public LoginResponseDTO(String sessionToken, String userName) {
        this.sessionToken = sessionToken;
        this.userName = userName;
    }
    
    public String getSessionToken() {
        return sessionToken;
    }
    
    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
}
