package com.example.planningpoker.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建需求池请求DTO
 *
 * <p>表现层DTO，仅在Controller和前端之间传递。
 *
 * @author 开发团队
 * @since 2.0.0
 * @date 2026-01-20
 */
public class CreatePoolRequestDTO {

    /**
     * 需求池名称（1-100字符）
     */
    @NotBlank(message = "需求池名称不能为空")
    @Size(min = 1, max = 100, message = "需求池名称长度必须在1-100字符之间")
    private String name;

    /**
     * 需求池描述（最多500字符）
     */
    @Size(max = 500, message = "需求池描述长度不能超过500字符")
    private String description;

    /**
     * 创建者用户名（从前端传递）
     */
    private String createdBy;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
