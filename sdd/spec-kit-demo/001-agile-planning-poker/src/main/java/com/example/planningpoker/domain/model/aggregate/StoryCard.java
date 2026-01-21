package com.example.planningpoker.domain.model.aggregate;

import com.example.planningpoker.domain.model.CardStatus;
import com.example.planningpoker.domain.model.valueobject.StoryPoint;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 故事卡聚合根
 * 
 * <p>表示一个用户故事或需求项，是估点的对象。
 * 
 * <p>职责：
 * <ul>
 *   <li>管理故事卡的基本信息（标题、描述）</li>
 *   <li>控制估点流程的状态转换（未估点→估点中→已估点）</li>
 *   <li>记录估点结果和时间</li>
 * </ul>
 * 
 * <p>不变量：
 * <ul>
 *   <li>故事卡必须有非空标题</li>
 *   <li>标题长度不超过100个字符</li>
 *   <li>描述长度不超过2000个字符</li>
 *   <li>估点状态必须是有效枚举值之一</li>
 * </ul>
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
public class StoryCard {
    
    /**
     * 故事卡唯一标识
     */
    private Long id;
    
    /**
     * 故事卡标题（1-100字符）
     */
    private String title;
    
    /**
     * 故事卡描述（最多2000字符）
     */
    private String description;
    
    /**
     * 估点状态
     */
    private CardStatus status;
    
    /**
     * 估点结果（仅在已估点时有值）
     */
    private StoryPoint storyPoint;
    
    /**
     * 创建者用户名
     */
    private String createdBy;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 估点完成时间
     */
    private LocalDateTime estimatedAt;
    
    /**
     * 所属需求池ID
     */
    private Long poolId;
    
    /**
     * 主持人用户名（创建者自动成为主持人）
     */
    private String hostName;
    
    /**
     * 绑定的估点会话ID（可为空，未估点时为null）
     */
    private Long votingSessionId;
    
    /**
     * 私有构造函数 - 使用Builder模式创建
     */
    private StoryCard() {
    }
    
    // ========== Getter和Setter方法 ==========
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    public CardStatus getStatus() {
        return status;
    }
    
    public void setStatus(CardStatus status) {
        this.status = status;
    }
    
    public StoryPoint getStoryPoint() {
        return storyPoint;
    }
    
    public void setStoryPoint(StoryPoint storyPoint) {
        this.storyPoint = storyPoint;
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
    
    public LocalDateTime getEstimatedAt() {
        return estimatedAt;
    }
    
    public void setEstimatedAt(LocalDateTime estimatedAt) {
        this.estimatedAt = estimatedAt;
    }
    
    public Long getPoolId() {
        return poolId;
    }
    
    public void setPoolId(Long poolId) {
        this.poolId = poolId;
    }
    
    public String getHostName() {
        return hostName;
    }
    
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
    
    public Long getVotingSessionId() {
        return votingSessionId;
    }
    
    public void setVotingSessionId(Long votingSessionId) {
        this.votingSessionId = votingSessionId;
    }
    
    // ========== 领域方法 ==========
    
    /**
     * 检查用户是否为主持人
     * 
     * @param username 用户名
     * @return 如果是主持人返回true
     */
    public boolean isHost(String username) {
        if (username == null || this.hostName == null) {
            return false;
        }
        return this.hostName.equals(username);
    }
    
    /**
     * 绑定估点会话
     * 
     * <p>将故事卡与估点会话关联，每个故事卡只能绑定一次。
     * 
     * @param sessionId 估点会话ID
     * @throws IllegalStateException 如果已绑定估点会话
     * @throws IllegalArgumentException 如果会话ID为空
     */
    public void bindVotingSession(Long sessionId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("估点会话ID不能为空");
        }
        if (this.votingSessionId != null) {
            throw new IllegalStateException(
                String.format("该故事卡已绑定估点会话(ID: %d)，不能重复估点", this.votingSessionId)
            );
        }
        this.votingSessionId = sessionId;
        this.status = CardStatus.ESTIMATING;
    }
    
    /**
     * 开始估点
     * 
     * <p>将故事卡状态从未估点变更为估点中。
     * 
     * @throws IllegalStateException 如果已在估点中或已估点
     */
    public void startEstimation() {
        if (this.status == CardStatus.ESTIMATING) {
            throw new IllegalStateException("故事卡已在估点中");
        }
        if (this.status == CardStatus.ESTIMATED) {
            throw new IllegalStateException("故事卡已估点完成，请先取消估点");
        }
        this.status = CardStatus.ESTIMATING;
    }
    
    /**
     * 完成估点
     * 
     * <p>将故事卡状态变更为已估点，并记录最终点数和完成时间。
     * 
     * @param finalPoint 最终故事点数
     * @throws IllegalStateException 如果未在估点中
     * @throws IllegalArgumentException 如果最终点数为空
     */
    public void completeEstimation(StoryPoint finalPoint) {
        if (this.status != CardStatus.ESTIMATING) {
            throw new IllegalStateException("故事卡未在估点中");
        }
        if (finalPoint == null) {
            throw new IllegalArgumentException("最终点数不能为空");
        }
        
        this.status = CardStatus.ESTIMATED;
        this.storyPoint = finalPoint;
        this.estimatedAt = LocalDateTime.now();
    }
    
    /**
     * 取消估点
     * 
     * <p>将故事卡状态恢复为未估点。
     * 
     * @throws IllegalStateException 如果未在估点中
     */
    public void cancelEstimation() {
        if (this.status != CardStatus.ESTIMATING) {
            throw new IllegalStateException("故事卡未在估点中");
        }
        
        this.status = CardStatus.NOT_ESTIMATED;
    }
    
    /**
     * 更新故事卡信息
     * 
     * <p>只能在未估点或已估点状态下更新，估点中不允许更新。
     * 
     * @param newTitle 新标题
     * @param newDescription 新描述
     * @throws IllegalStateException 如果正在估点中
     * @throws IllegalArgumentException 如果标题无效
     */
    public void update(String newTitle, String newDescription) {
        if (this.status == CardStatus.ESTIMATING) {
            throw new IllegalStateException("故事卡正在估点中，不能更新");
        }
        
        validateTitle(newTitle);
        validateDescription(newDescription);
        
        this.title = newTitle;
        this.description = newDescription;
    }
    
    /**
     * 验证标题
     * 
     * @param title 标题
     * @throws IllegalArgumentException 如果标题无效
     */
    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("故事卡标题不能为空");
        }
        if (title.length() > 100) {
            throw new IllegalArgumentException("故事卡标题长度不能超过100个字符");
        }
    }
    
    /**
     * 验证描述
     * 
     * @param description 描述
     * @throws IllegalArgumentException 如果描述无效
     */
    private void validateDescription(String description) {
        if (description != null && description.length() > 2000) {
            throw new IllegalArgumentException("故事卡描述长度不能超过2000个字符");
        }
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
        StoryCard storyCard = (StoryCard) o;
        return Objects.equals(id, storyCard.id);
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
        return "StoryCard{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", storyPoint=" + storyPoint +
                '}';
    }
    
    /**
     * Builder模式 - 创建故事卡
     * 
     * <p>Effective Java Item 2: 遇到多个构造器参数时要考虑使用构建器
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
        private final StoryCard card = new StoryCard();
        
        public Builder id(Long id) {
            card.id = id;
            return this;
        }
        
        public Builder title(String title) {
            card.title = title;
            return this;
        }
        
        public Builder description(String description) {
            card.description = description;
            return this;
        }
        
        public Builder status(CardStatus status) {
            card.status = status;
            return this;
        }
        
        public Builder storyPoint(StoryPoint storyPoint) {
            card.storyPoint = storyPoint;
            return this;
        }
        
        public Builder createdBy(String createdBy) {
            card.createdBy = createdBy;
            return this;
        }
        
        public Builder createdAt(LocalDateTime createdAt) {
            card.createdAt = createdAt;
            return this;
        }
        
        public Builder estimatedAt(LocalDateTime estimatedAt) {
            card.estimatedAt = estimatedAt;
            return this;
        }
        
        public Builder poolId(Long poolId) {
            card.poolId = poolId;
            return this;
        }
        
        public Builder hostName(String hostName) {
            card.hostName = hostName;
            return this;
        }
        
        public Builder votingSessionId(Long votingSessionId) {
            card.votingSessionId = votingSessionId;
            return this;
        }
        
        /**
         * 构建故事卡对象
         * 
         * @return 故事卡实例
         * @throws IllegalArgumentException 如果必填字段缺失或无效
         */
        public StoryCard build() {
            // 验证必填字段
            card.validateTitle(card.title);
            card.validateDescription(card.description);
            
            if (card.createdBy == null || card.createdBy.isBlank()) {
                throw new IllegalArgumentException("创建者不能为空");
            }
            
            if (card.poolId == null) {
                throw new IllegalArgumentException("需求池ID不能为空");
            }
            
            if (card.hostName == null || card.hostName.isBlank()) {
                throw new IllegalArgumentException("主持人不能为空");
            }
            
            // 设置默认值
            if (card.status == null) {
                card.status = CardStatus.NOT_ESTIMATED;
            }
            if (card.createdAt == null) {
                card.createdAt = LocalDateTime.now();
            }
            
            return card;
        }
    }
}
