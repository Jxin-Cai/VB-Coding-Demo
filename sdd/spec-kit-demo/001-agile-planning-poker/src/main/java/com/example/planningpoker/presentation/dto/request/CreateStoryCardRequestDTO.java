package com.example.planningpoker.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 创建故事卡请求DTO
 *
 * <p>表现层DTO，用于接收HTTP请求参数。
 * 使用Bean Validation注解进行参数验证。
 *
 * <p>注意：DTO仅在表现层使用，不传递到应用层以下。
 *
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
public class CreateStoryCardRequestDTO {

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

    /**
     * 所属需求池ID
     */
    @NotNull(message = "需求池ID不能为空")
    private Long poolId;

    /**
     * 创建者用户名（从前端传递，用于标识故事卡主持人）
     */
    private String createdBy;

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

    public Long getPoolId() {
        return poolId;
    }

    public void setPoolId(Long poolId) {
        this.poolId = poolId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
