package com.example.planningpoker.domain.model.aggregate;

import com.example.planningpoker.domain.model.SessionStatus;
import com.example.planningpoker.domain.model.valueobject.StoryPoint;
import com.example.planningpoker.domain.model.valueobject.Vote;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 估点会话聚合根
 *
 * <p>表示一次估点会话的完整生命周期。
 *
 * <p>会话ID与故事卡ID 1:1绑定，每个故事卡只能有一个活跃的估点会话。
 *
 * <p>职责：
 * <ul>
 *   <li>管理会话基本信息（故事卡ID即会话ID、状态）</li>
 *   <li>管理参与者列表（谁加入了会话）</li>
 *   <li>管理投票集合（谁投了什么票）</li>
 *   <li>控制投票流程（添加投票、检查完成、揭示结果）</li>
 *   <li>计算投票统计（平均值、最高值、最低值）</li>
 * </ul>
 *
 * <p>不变量：
 * <ul>
 *   <li>会话必须关联一张故事卡（storyCardId即为主标识）</li>
 *   <li>参与者名称不能重复</li>
 *   <li>每个参与者只能有一次有效投票</li>
 *   <li>只有所有参与者投票完成后才能揭示结果</li>
 *   <li>会话完成后不能再添加投票</li>
 *   <li>已完成的会话不能重新开始（1:1关系，一次估点）</li>
 * </ul>
 *
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
public class VotingSession {

    /**
     * 故事卡ID（即会话ID）
     *
     * <p>每个故事卡对应一个会话，storyCardId作为唯一标识。
     * 数据库主键，1:1关系。
     */
    private Long storyCardId;
    
    /**
     * 会话状态
     */
    private SessionStatus status;
    
    /**
     * 参与者集合（用户名）
     */
    private final Set<String> participants;
    
    /**
     * 投票集合（参与者名称 → 投票）
     */
    private final Map<String, Vote> votes;
    
    /**
     * 结果揭示时间
     */
    private LocalDateTime revealedAt;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 完成时间
     */
    private LocalDateTime completedAt;
    
    /**
     * 主持人用户名
     */
    private String hostName;
    
    /**
     * 开始投票时间（用于倒计时）
     */
    private LocalDateTime votingStartedAt;
    
    /**
     * 投票截止时间（开始后30秒）
     */
    private LocalDateTime votingDeadline;
    
    /**
     * 私有构造函数 - 使用Builder模式创建
     */
    private VotingSession() {
        this.participants = new HashSet<>();
        this.votes = new HashMap<>();
    }
    
    // ========== Getter和Setter方法 ==========

    public Long getStoryCardId() {
        return storyCardId;
    }

    public void setStoryCardId(Long storyCardId) {
        this.storyCardId = storyCardId;
    }
    
    public SessionStatus getStatus() {
        return status;
    }
    
    public void setStatus(SessionStatus status) {
        this.status = status;
    }
    
    public Set<String> getParticipants() {
        return Collections.unmodifiableSet(participants);
    }
    
    public Map<String, Vote> getVotes() {
        return Collections.unmodifiableMap(votes);
    }
    
    public LocalDateTime getRevealedAt() {
        return revealedAt;
    }
    
    public void setRevealedAt(LocalDateTime revealedAt) {
        this.revealedAt = revealedAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public String getHostName() {
        return hostName;
    }
    
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
    
    public LocalDateTime getVotingStartedAt() {
        return votingStartedAt;
    }
    
    public void setVotingStartedAt(LocalDateTime votingStartedAt) {
        this.votingStartedAt = votingStartedAt;
    }
    
    public LocalDateTime getVotingDeadline() {
        return votingDeadline;
    }
    
    public void setVotingDeadline(LocalDateTime votingDeadline) {
        this.votingDeadline = votingDeadline;
    }
    
    // ========== 领域方法 ==========
    
    /**
     * 开始投票（启动倒计时）
     * 
     * <p>设置投票开始时间和截止时间（30秒后）。
     * 
     * @throws IllegalStateException 如果会话未进行中或已开始投票
     */
    public void startVoting() {
        if (this.status != SessionStatus.IN_PROGRESS) {
            throw new IllegalStateException("会话未进行中，不能开始投票");
        }
        if (this.votingStartedAt != null) {
            throw new IllegalStateException("投票已开始，不能重复开始");
        }
        
        this.votingStartedAt = LocalDateTime.now();
        this.votingDeadline = this.votingStartedAt.plusSeconds(30);
    }
    
    /**
     * 检查投票是否超时
     * 
     * @return 如果当前时间已超过截止时间返回true
     */
    public boolean isVotingExpired() {
        if (this.votingDeadline == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(this.votingDeadline);
    }
    
    /**
     * 标记未投票者为弃权
     * 
     * <p>倒计时结束后自动调用，将所有未投票的参与者标记为弃权。
     * 弃权者不添加投票记录，在统计时会被自动排除。
     * 
     * @throws IllegalStateException 如果投票未开始
     */
    public void forfeitAbsentVoters() {
        if (this.votingStartedAt == null) {
            throw new IllegalStateException("投票未开始，不能标记弃权");
        }
        
        // 找出所有未投票的参与者
        Set<String> votedParticipants = this.votes.keySet();
        Set<String> absentParticipants = new HashSet<>(this.participants);
        absentParticipants.removeAll(votedParticipants);
        
        // 记录弃权者但不添加投票（在前端显示时区分已投票和弃权）
        // 弃权者保留在participants集合中，但votes集合中没有其记录
        
        // 自动揭示结果（基于已投票者）
        if (!this.votes.isEmpty()) {
            this.revealVotes();
        }
    }
    
    /**
     * 添加参与者
     * 
     * <p>参与者加入会话时调用。
     * 
     * @param participantName 参与者用户名
     * @throws IllegalStateException 如果会话已完成
     * @throws IllegalArgumentException 如果用户名无效
     */
    public void addParticipant(String participantName) {
        if (this.status != SessionStatus.IN_PROGRESS) {
            throw new IllegalStateException("会话未进行中，不能添加参与者");
        }
        if (participantName == null || participantName.isBlank()) {
            throw new IllegalArgumentException("参与者用户名不能为空");
        }
        
        this.participants.add(participantName);
    }
    
    /**
     * 添加投票
     * 
     * <p>参与者提交投票时调用。如果参与者已投票，则更新为新的投票。
     * 
     * @param vote 投票对象
     * @throws IllegalStateException 如果会话已完成
     * @throws IllegalArgumentException 如果投票无效
     */
    public void addVote(Vote vote) {
        if (this.status != SessionStatus.IN_PROGRESS) {
            throw new IllegalStateException("会话未进行中，不能添加投票");
        }
        if (vote == null) {
            throw new IllegalArgumentException("投票不能为空");
        }
        
        String participantName = vote.getParticipantName();
        
        // 自动添加参与者（如果不存在）
        if (!this.participants.contains(participantName)) {
            this.participants.add(participantName);
        }
        
        // 添加或更新投票
        this.votes.put(participantName, vote);
    }
    
    /**
     * 检查所有人是否已投票
     * 
     * @return 如果所有参与者都已投票返回true
     */
    public boolean areAllVotesComplete() {
        // 至少有一个参与者
        if (this.participants.isEmpty()) {
            return false;
        }
        
        // 投票数等于参与者数
        return this.votes.size() == this.participants.size();
    }
    
    /**
     * 揭示投票结果
     * 
     * <p>所有人投票完成后自动调用。
     * 
     * @throws IllegalStateException 如果投票未全部完成
     */
    public void revealVotes() {
        if (!areAllVotesComplete()) {
            throw new IllegalStateException(
                String.format("投票未全部完成，已投票: %d/%d", 
                             this.votes.size(), this.participants.size())
            );
        }
        
        this.revealedAt = LocalDateTime.now();
    }
    
    /**
     * 完成会话
     * 
     * <p>选择最终故事点数，结束估点会话。
     * 
     * @param finalStoryPoint 最终故事点数
     * @throws IllegalStateException 如果会话未进行中或投票未揭示
     * @throws IllegalArgumentException 如果最终点数为空
     */
    public void complete(StoryPoint finalStoryPoint) {
        if (this.status != SessionStatus.IN_PROGRESS) {
            throw new IllegalStateException("会话未进行中");
        }
        if (this.revealedAt == null) {
            throw new IllegalStateException("投票结果尚未揭示，不能完成会话");
        }
        if (finalStoryPoint == null) {
            throw new IllegalArgumentException("最终故事点数不能为空");
        }
        
        this.status = SessionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
    
    /**
     * 获取投票统计信息
     * 
     * <p>计算平均值、最高值、最低值。
     * 
     * @return 统计信息Map（average, max, min）
     * @throws IllegalStateException 如果投票结果尚未揭示
     */
    public Map<String, Double> getStatistics() {
        if (this.revealedAt == null) {
            throw new IllegalStateException("投票结果尚未揭示");
        }
        if (this.votes.isEmpty()) {
            throw new IllegalStateException("没有投票数据");
        }
        
        // 收集所有点数值
        List<Integer> points = this.votes.values().stream()
                .map(vote -> vote.getStoryPoint().getValue())
                .toList();
        
        // 计算统计值
        double average = points.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
        
        int max = points.stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
        
        int min = points.stream()
                .mapToInt(Integer::intValue)
                .min()
                .orElse(0);
        
        Map<String, Double> statistics = new HashMap<>();
        statistics.put("average", average);
        statistics.put("max", (double) max);
        statistics.put("min", (double) min);
        
        return statistics;
    }
    
    /**
     * 相等性比较（基于storyCardId）
     *
     * @param o 比较对象
     * @return 如果storyCardId相等返回true
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VotingSession that = (VotingSession) o;
        return Objects.equals(storyCardId, that.storyCardId);
    }

    /**
     * 哈希码（基于storyCardId）
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(storyCardId);
    }
    
    /**
     * 字符串表示
     * 
     * @return 格式化的字符串
     */
    @Override
    public String toString() {
        return "VotingSession{" +
                "storyCardId=" + storyCardId +
                ", status=" + status +
                ", participants=" + participants.size() +
                ", votes=" + votes.size() +
                '}';
    }
    
    /**
     * Builder模式 - 创建估点会话
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
        private final VotingSession session = new VotingSession();

        public Builder storyCardId(Long storyCardId) {
            session.storyCardId = storyCardId;
            return this;
        }
        
        public Builder status(SessionStatus status) {
            session.status = status;
            return this;
        }
        
        public Builder participants(Set<String> participants) {
            if (participants != null) {
                session.participants.addAll(participants);
            }
            return this;
        }
        
        public Builder votes(Map<String, Vote> votes) {
            if (votes != null) {
                session.votes.putAll(votes);
            }
            return this;
        }
        
        public Builder revealedAt(LocalDateTime revealedAt) {
            session.revealedAt = revealedAt;
            return this;
        }
        
        public Builder createdAt(LocalDateTime createdAt) {
            session.createdAt = createdAt;
            return this;
        }
        
        public Builder completedAt(LocalDateTime completedAt) {
            session.completedAt = completedAt;
            return this;
        }
        
        public Builder hostName(String hostName) {
            session.hostName = hostName;
            return this;
        }
        
        public Builder votingStartedAt(LocalDateTime votingStartedAt) {
            session.votingStartedAt = votingStartedAt;
            return this;
        }
        
        public Builder votingDeadline(LocalDateTime votingDeadline) {
            session.votingDeadline = votingDeadline;
            return this;
        }
        
        /**
         * 构建会话对象
         * 
         * @return 估点会话实例
         * @throws IllegalArgumentException 如果必填字段缺失
         */
        public VotingSession build() {
            if (session.storyCardId == null) {
                throw new IllegalArgumentException("故事卡ID不能为空");
            }
            
            if (session.hostName == null || session.hostName.isBlank()) {
                throw new IllegalArgumentException("主持人不能为空");
            }
            
            // 设置默认值
            if (session.status == null) {
                session.status = SessionStatus.IN_PROGRESS;
            }
            if (session.createdAt == null) {
                session.createdAt = LocalDateTime.now();
            }
            
            return session;
        }
    }
}
