package com.example.planningpoker.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 登录请求DTO
 * 
 * <p>用户通过用户名登录，无需密码（适合内部团队环境）。
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
public class LoginRequestDTO {
    
    /**
     * 用户名（2-20字符）
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 20, message = "用户名长度必须在2-20之间")
    private String name;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}
