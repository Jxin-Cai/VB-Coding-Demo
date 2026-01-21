package com.example.planningpoker.domain.model.valueobject;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 投票值对象
 * 
 * <p>表示参与者的一次投票，包含参与者名称、故事点数和投票时间。
 * 
 * <p>设计原则：
 * <ul>
 *   <li>不可变性：所有字段为final，无setter方法</li>
 *   <li>自我验证：构造时验证参数合法性</li>
 *   <li>静态工厂方法：使用of()方法创建实例</li>
 * </ul>
 * 
 * <p>Effective Java应用：
 * <ul>
 *   <li>Item 1: 使用静态工厂方法而非构造器</li>
 *   <li>Item 17: 最小化可变性</li>
 * </ul>
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
public final class Vote {
    
    /**
     * 参与者用户名
     */
    private final String participantName;
    
    /**
     * 故事点数
     */
    private final StoryPoint storyPoint;
    
    /**
     * 投票时间
     */
    private final LocalDateTime votedAt;
    
    /**
     * 私有构造函数
     * 
     * @param participantName 参与者用户名
     * @param storyPoint 故事点数
     * @param votedAt 投票时间
     */
    private Vote(String participantName, StoryPoint storyPoint, LocalDateTime votedAt) {
        this.participantName = participantName;
        this.storyPoint = storyPoint;
        this.votedAt = votedAt;
    }
    
    /**
     * 静态工厂方法 - 创建投票对象
     * 
     * @param participantName 参与者用户名
     * @param storyPoint 故事点数
     * @return 投票对象
     * @throws IllegalArgumentException 如果参数无效
     */
    public static Vote of(String participantName, StoryPoint storyPoint) {
        if (participantName == null || participantName.isBlank()) {
            throw new IllegalArgumentException("参与者用户名不能为空");
        }
        if (storyPoint == null) {
            throw new IllegalArgumentException("故事点数不能为空");
        }
        
        return new Vote(participantName, storyPoint, LocalDateTime.now());
    }
    
    /**
     * 静态工厂方法 - 创建投票对象（指定投票时间）
     * 
     * @param participantName 参与者用户名
     * @param storyPoint 故事点数
     * @param votedAt 投票时间
     * @return 投票对象
     */
    public static Vote of(String participantName, StoryPoint storyPoint, LocalDateTime votedAt) {
        if (participantName == null || participantName.isBlank()) {
            throw new IllegalArgumentException("参与者用户名不能为空");
        }
        if (storyPoint == null) {
            throw new IllegalArgumentException("故事点数不能为空");
        }
        if (votedAt == null) {
            throw new IllegalArgumentException("投票时间不能为空");
        }
        
        return new Vote(participantName, storyPoint, votedAt);
    }
    
    /**
     * 获取参与者用户名
     * 
     * @return 参与者用户名
     */
    public String getParticipantName() {
        return participantName;
    }
    
    /**
     * 获取故事点数
     * 
     * @return 故事点数
     */
    public StoryPoint getStoryPoint() {
        return storyPoint;
    }
    
    /**
     * 获取投票时间
     * 
     * @return 投票时间
     */
    public LocalDateTime getVotedAt() {
        return votedAt;
    }
    
    /**
     * 相等性比较（基于参与者名称）
     * 
     * <p>同一参与者的投票视为相同（用于Map和Set操作）
     * 
     * @param o 比较对象
     * @return 如果参与者名称相同返回true
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vote vote = (Vote) o;
        return Objects.equals(participantName, vote.participantName);
    }
    
    /**
     * 哈希码（基于参与者名称）
     * 
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(participantName);
    }
    
    /**
     * 字符串表示
     * 
     * @return 格式化的字符串
     */
    @Override
    public String toString() {
        return "Vote{" +
                "participantName='" + participantName + '\'' +
                ", storyPoint=" + storyPoint +
                ", votedAt=" + votedAt +
                '}';
    }
}
