package com.example.planningpoker.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * 完成估点会话请求DTO
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
public class CompleteSessionRequestDTO {
    
    /**
     * 最终故事点数（1/2/3/5/8）
     */
    @NotNull(message = "最终故事点数不能为空")
    private Integer finalStoryPoint;
    
    public Integer getFinalStoryPoint() {
        return finalStoryPoint;
    }
    
    public void setFinalStoryPoint(Integer finalStoryPoint) {
        this.finalStoryPoint = finalStoryPoint;
    }
}
