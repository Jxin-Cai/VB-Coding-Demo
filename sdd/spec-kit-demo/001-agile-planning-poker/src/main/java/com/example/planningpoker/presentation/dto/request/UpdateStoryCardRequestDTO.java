package com.example.planningpoker.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 更新故事卡请求DTO
 * 
 * <p>表现层DTO，用于接收HTTP请求参数。
 * 
 * <p>注意：DTO仅在表现层使用，不传递到应用层以下。
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
public class UpdateStoryCardRequestDTO {
    
    /**
     * 故事卡标题
     */
    @NotBlank(message = "标题不能为空")
    @Size(min = 1, max = 100, message = "标题长度必须在1-100之间")
    private String title;
    
    /**
     * 故事卡描述
     */
    @Size(max = 2000, message = "描述长度不能超过2000")
    private String description;
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}
