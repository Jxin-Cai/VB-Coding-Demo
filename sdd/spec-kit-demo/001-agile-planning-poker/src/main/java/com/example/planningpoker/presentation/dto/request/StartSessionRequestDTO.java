package com.example.planningpoker.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * 开始估点会话请求DTO
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
public class StartSessionRequestDTO {
    
    /**
     * 故事卡ID
     */
    @NotNull(message = "故事卡ID不能为空")
    private Long storyCardId;
    
    public Long getStoryCardId() {
        return storyCardId;
    }
    
    public void setStoryCardId(Long storyCardId) {
        this.storyCardId = storyCardId;
    }
}
