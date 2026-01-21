package com.example.planningpoker.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * 提交投票请求DTO
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
public class SubmitVoteRequestDTO {
    
    /**
     * 故事点数（1/2/3/5/8）
     */
    @NotNull(message = "故事点数不能为空")
    private Integer storyPoint;
    
    public Integer getStoryPoint() {
        return storyPoint;
    }
    
    public void setStoryPoint(Integer storyPoint) {
        this.storyPoint = storyPoint;
    }
}
