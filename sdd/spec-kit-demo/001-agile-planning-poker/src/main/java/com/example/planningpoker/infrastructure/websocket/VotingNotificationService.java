package com.example.planningpoker.infrastructure.websocket;

import com.example.planningpoker.domain.event.VoteSubmitted;
import com.example.planningpoker.domain.event.VotesRevealed;
import com.example.planningpoker.domain.event.VotingSessionCompleted;
import com.example.planningpoker.domain.event.VotingSessionStarted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 估点会话WebSocket通知服务
 * 
 * <p>监听领域事件，通过WebSocket实时推送给所有在线客户端。
 * 
 * <p>功能：
 * <ul>
 *   <li>监听VotingSessionStarted事件 → 广播会话开始通知</li>
 *   <li>监听VoteSubmitted事件 → 广播投票进度更新</li>
 *   <li>监听VotesRevealed事件 → 广播投票结果揭示</li>
 *   <li>监听VotingSessionCompleted事件 → 广播会话完成通知</li>
 * </ul>
 * 
 * <p>WebSocket主题：/topic/voting-session/{sessionId}
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
@Service
public class VotingNotificationService {
    
    private static final Logger log = LoggerFactory.getLogger(VotingNotificationService.class);
    
    private final SimpMessagingTemplate messagingTemplate;
    
    public VotingNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    
    /**
     * 处理会话开始事件
     * 
     * @param event 会话开始事件
     */
    @EventListener
    public void handleSessionStarted(VotingSessionStarted event) {
        log.info("处理会话开始事件，会话ID: {}", event.getSessionId());
        
        // 构建通知消息
        Map<String, Object> message = new HashMap<>();
        message.put("type", "SESSION_STARTED");
        message.put("sessionId", event.getSessionId());
        message.put("storyCardId", event.getStoryCardId());
        message.put("storyCardTitle", event.getStoryCardTitle());
        message.put("startedBy", event.getStartedBy());
        message.put("startedAt", event.getStartedAt().toString());
        
        // 广播到所有订阅者
        String destination = "/topic/voting-session/" + event.getSessionId();
        messagingTemplate.convertAndSend(destination, message);
        
        log.info("会话开始通知已发送，目标: {}", destination);
    }
    
    /**
     * 处理投票提交事件
     * 
     * @param event 投票提交事件
     */
    @EventListener
    public void handleVoteSubmitted(VoteSubmitted event) {
        log.info("处理投票提交事件，会话ID: {}, 参与者: {}", 
                event.getSessionId(), event.getParticipantName());
        
        // 构建通知消息
        Map<String, Object> message = new HashMap<>();
        message.put("type", "VOTE_SUBMITTED");
        message.put("sessionId", event.getSessionId());
        message.put("participantName", event.getParticipantName());
        message.put("isUpdate", event.getIsUpdate());
        message.put("totalParticipants", event.getTotalParticipants());
        message.put("totalVotes", event.getTotalVotes());
        message.put("votedAt", event.getVotedAt().toString());
        
        // 广播投票进度
        String destination = "/topic/voting-session/" + event.getSessionId();
        messagingTemplate.convertAndSend(destination, message);
        
        log.info("投票进度通知已发送，已投票: {}/{}", 
                event.getTotalVotes(), event.getTotalParticipants());
    }
    
    /**
     * 处理投票结果揭示事件
     * 
     * @param event 投票结果揭示事件
     */
    @EventListener
    public void handleVotesRevealed(VotesRevealed event) {
        log.info("处理投票结果揭示事件，会话ID: {}", event.getSessionId());
        
        // 构建通知消息
        Map<String, Object> message = new HashMap<>();
        message.put("type", "VOTES_REVEALED");
        message.put("sessionId", event.getSessionId());
        message.put("votes", event.getVotes());
        message.put("statistics", Map.of(
                "average", event.getAveragePoint(),
                "max", event.getMaxPoint(),
                "min", event.getMinPoint()
        ));
        message.put("revealedAt", event.getRevealedAt().toString());
        
        // 广播投票结果
        String destination = "/topic/voting-session/" + event.getSessionId();
        messagingTemplate.convertAndSend(destination, message);
        
        log.info("投票结果揭示通知已发送，平均值: {}", event.getAveragePoint());
    }
    
    /**
     * 处理会话完成事件
     * 
     * @param event 会话完成事件
     */
    @EventListener
    public void handleSessionCompleted(VotingSessionCompleted event) {
        log.info("处理会话完成事件，会话ID: {}, 最终点数: {}", 
                event.getSessionId(), event.getFinalStoryPoint());
        
        // 构建通知消息
        Map<String, Object> message = new HashMap<>();
        message.put("type", "SESSION_COMPLETED");
        message.put("sessionId", event.getSessionId());
        message.put("storyCardId", event.getStoryCardId());
        message.put("finalStoryPoint", event.getFinalStoryPoint());
        message.put("completedBy", event.getCompletedBy());
        message.put("completedAt", event.getCompletedAt().toString());
        
        // 广播会话完成通知
        String destination = "/topic/voting-session/" + event.getSessionId();
        messagingTemplate.convertAndSend(destination, message);
        
        log.info("会话完成通知已发送");
    }
}
