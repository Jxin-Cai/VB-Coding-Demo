package com.example.planningpoker.domain.model.aggregate;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 需求池聚合根
 * 
 * <p>表示一个需求池（Backlog Pool），用于组织和管理一组故事卡。
 * 不同项目或团队可以拥有各自独立的需求池。
 * 
 * <p>职责：
 * <ul>
 *   <li>管理需求池基本信息（名称、描述、创建者）</li>
 *   <li>提供故事卡数量统计</li>
 *   <li>记录需求池创建时间</li>
 * </ul>
 * 
 * <p>不变量：
 * <ul>
 *   <li>需求池必须有非空名称</li>
 *   <li>名称长度在1-100字符之间</li>
 *   <li>描述长度不超过500字符</li>
 *   <li>创建者可以为null（默认需求池不归属任何人）</li>
 * </ul>
 * 
 * @author 开发团队
 * @since 2.0.0
 * @date 2026-01-20
 */
public class BacklogPool {
    
    /**
     * 需求池唯一标识
     */
    private Long id;
    
    /**
     * 需求池名称（1-100字符）
     */
    private String name;
    
    /**
     * 需求池描述（最多500字符）
     */
    private String description;
    
    /**
     * 创建者用户名
     */
    private String createdBy;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 私有构造函数 - 使用Builder模式创建
     */
    private BacklogPool() {
    }
    
    // ========== Getter和Setter方法 ==========
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // ========== 领域方法 ==========
    
    /**
     * 验证需求池名称是否有效
     * 
     * @param name 需求池名称
     * @throws IllegalArgumentException 如果名称无效
     */
    public void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("需求池名称不能为空");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("需求池名称长度不能超过100个字符");
        }
    }
    
    /**
     * 验证需求池描述是否有效
     * 
     * @param description 需求池描述
     * @throws IllegalArgumentException 如果描述无效
     */
    public void validateDescription(String description) {
        if (description != null && description.length() > 500) {
            throw new IllegalArgumentException("需求池描述长度不能超过500个字符");
        }
    }
    
    /**
     * 更新需求池名称
     * 
     * @param name 新名称
     * @throws IllegalArgumentException 如果名称无效
     */
    public void updateName(String name) {
        validateName(name);
        this.name = name;
    }
    
    /**
     * 更新需求池描述
     * 
     * @param description 新描述
     * @throws IllegalArgumentException 如果描述无效
     */
    public void updateDescription(String description) {
        validateDescription(description);
        this.description = description;
    }
    
    /**
     * 相等性比较（基于ID）
     * 
     * @param o 比较对象
     * @return 如果ID相等返回true
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BacklogPool that = (BacklogPool) o;
        return Objects.equals(id, that.id);
    }
    
    /**
     * 哈希码（基于ID）
     * 
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    /**
     * 字符串表示
     * 
     * @return 格式化的字符串
     */
    @Override
    public String toString() {
        return "BacklogPool{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
    
    /**
     * Builder模式 - 创建需求池
     * 
     * @return Builder实例
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder类
     */
    public static class Builder {
        private final BacklogPool pool = new BacklogPool();
        
        public Builder id(Long id) {
            pool.id = id;
            return this;
        }
        
        public Builder name(String name) {
            pool.name = name;
            return this;
        }
        
        public Builder description(String description) {
            pool.description = description;
            return this;
        }
        
        public Builder createdBy(String createdBy) {
            pool.createdBy = createdBy;
            return this;
        }
        
        public Builder createdAt(LocalDateTime createdAt) {
            pool.createdAt = createdAt;
            return this;
        }
        
        /**
         * 构建需求池对象
         * 
         * @return 需求池实例
         * @throws IllegalArgumentException 如果必填字段缺失或验证失败
         */
        public BacklogPool build() {
            // 验证必填字段
            if (pool.name == null || pool.name.isBlank()) {
                throw new IllegalArgumentException("需求池名称不能为空");
            }
            // createdBy允许为null（默认需求池不归属任何人）

            // 验证字段有效性
            pool.validateName(pool.name);
            pool.validateDescription(pool.description);

            // 设置默认值
            if (pool.createdAt == null) {
                pool.createdAt = LocalDateTime.now();
            }

            return pool;
        }
    }
}
