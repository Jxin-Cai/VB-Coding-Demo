package com.example.planningpoker.presentation.controller;

import com.example.planningpoker.application.command.CompleteSessionCommand;
import com.example.planningpoker.application.command.StartSessionCommand;
import com.example.planningpoker.application.command.SubmitVoteCommand;
import com.example.planningpoker.application.service.VotingSessionApplicationService;
import com.example.planningpoker.domain.model.aggregate.StoryCard;
import com.example.planningpoker.domain.model.aggregate.VotingSession;
import com.example.planningpoker.domain.repository.StoryCardRepository;
import com.example.planningpoker.presentation.dto.request.CompleteSessionRequestDTO;
import com.example.planningpoker.presentation.dto.request.StartSessionRequestDTO;
import com.example.planningpoker.presentation.dto.request.SubmitVoteRequestDTO;
import com.example.planningpoker.presentation.dto.response.ResponseDTO;
import com.example.planningpoker.presentation.dto.response.VotingSessionDTO;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 估点会话Controller
 * 
 * <p>提供估点会话相关的REST API端点：
 * <ul>
 *   <li>POST /api/v1/voting-sessions/start - 开始估点会话</li>
 *   <li>POST /api/v1/voting-sessions/{sessionId}/votes - 提交投票</li>
 *   <li>GET /api/v1/voting-sessions/{sessionId} - 查询会话状态</li>
 *   <li>POST /api/v1/voting-sessions/{sessionId}/complete - 完成会话</li>
 * </ul>
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
@RestController
@RequestMapping("/api/v1/voting-sessions")
public class VotingSessionController {
    
    private static final Logger log = LoggerFactory.getLogger(VotingSessionController.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    
    private final VotingSessionApplicationService applicationService;
    private final StoryCardRepository storyCardRepository;
    
    public VotingSessionController(VotingSessionApplicationService applicationService,
                                  StoryCardRepository storyCardRepository) {
        this.applicationService = applicationService;
        this.storyCardRepository = storyCardRepository;
    }
    
    /**
     * 开始估点会话
     * 
     * @param requestDTO 开始会话请求DTO
     * @param userName 用户名（从Header获取）
     * @return 创建的估点会话DTO
     */
    @PostMapping("/start")
    public ResponseDTO<VotingSessionDTO> startSession(
            @Valid @RequestBody StartSessionRequestDTO requestDTO,
            @RequestHeader(value = "X-User-Name", defaultValue = "产品负责人") String userName) {
        
        log.info("接收开始估点会话请求，故事卡ID: {}, 发起者: {}", 
                requestDTO.getStoryCardId(), userName);
        
        String currentUser = userName;
        
        // DTO转Command
        StartSessionCommand command = new StartSessionCommand(
                requestDTO.getStoryCardId(),
                currentUser
        );
        
        // 调用应用服务（传递当前用户进行权限检查）
        VotingSession session = applicationService.startSession(command, currentUser);
        
        // 领域对象转DTO
        VotingSessionDTO dto = toDTO(session);
        
        return ResponseDTO.success("估点会话已开始", dto);
    }
    
    /**
     * 提交投票
     * 
     * @param sessionId 会话ID
     * @param requestDTO 提交投票请求DTO
     * @param userName 用户名（从Header获取）
     * @return 更新后的会话DTO
     */
    @PostMapping("/{sessionId}/votes")
    public ResponseDTO<VotingSessionDTO> submitVote(
            @PathVariable Long sessionId,
            @Valid @RequestBody SubmitVoteRequestDTO requestDTO,
            @RequestHeader(value = "X-User-Name", defaultValue = "张三") String userName) {
        
        log.info("接收提交投票请求，会话ID: {}, 参与者: {}, 点数: {}", 
                sessionId, userName, requestDTO.getStoryPoint());
        
        String currentUser = userName;
        
        // DTO转Command
        SubmitVoteCommand command = new SubmitVoteCommand(
                sessionId,
                currentUser,
                requestDTO.getStoryPoint()
        );
        
        // 调用应用服务
        VotingSession session = applicationService.submitVote(command);
        
        // 领域对象转DTO
        VotingSessionDTO dto = toDTO(session);
        
        return ResponseDTO.success("投票已提交", dto);
    }
    
    /**
     * 获取会话详情
     * 
     * @param sessionId 会话ID
     * @return 会话DTO
     */
    @GetMapping("/{sessionId}")
    public ResponseDTO<VotingSessionDTO> getSession(@PathVariable Long sessionId) {
        log.debug("接收获取会话请求，会话ID: {}", sessionId);
        
        // 调用应用服务
        VotingSession session = applicationService.getSession(sessionId);
        
        // 领域对象转DTO
        VotingSessionDTO dto = toDTO(session);
        
        return ResponseDTO.success(dto);
    }
    
    /**
     * 加入已有的估点会话
     * 
     * <p>用于支持多人加入同一估点会话。
     * 当一个故事卡已经有人开始估点时，其他用户可以通过此接口加入。
     * 
     * @param storyCardId 故事卡ID
     * @param userName 用户名（从Header获取）
     * @return 加入的会话DTO
     */
    @PostMapping("/join/{storyCardId}")
    public ResponseDTO<VotingSessionDTO> joinSession(
            @PathVariable Long storyCardId,
            @RequestHeader(value = "X-User-Name", defaultValue = "团队成员") String userName) {
        
        log.info("接收加入估点会话请求，故事卡ID: {}, 用户名: {}", storyCardId, userName);
        
        try {
            // 调用应用服务
            VotingSession session = applicationService.joinSession(storyCardId, userName);
            
            // 领域对象转DTO
            VotingSessionDTO dto = toDTO(session);

            log.info("用户成功加入估点会话，会话ID: {}, 用户名: {}", session.getStoryCardId(), userName);
            
            return ResponseDTO.success("成功加入估点会话", dto);
            
        } catch (com.example.planningpoker.domain.exception.SessionNotFoundException e) {
            log.warn("用户加入会话失败，故事卡ID: {}, 原因: {}", storyCardId, e.getMessage());
            return ResponseDTO.error(404, e.getMessage());
            
        } catch (IllegalStateException e) {
            log.warn("用户加入会话失败，故事卡ID: {}, 原因: {}", storyCardId, e.getMessage());
            return ResponseDTO.error(400, e.getMessage());
        }
    }
    
    /**
     * 取消估点会话
     * 
     * @param sessionId 会话ID
     * @param userName 用户名（从Header获取）
     * @return 取消的会话DTO
     */
    @PostMapping("/{sessionId}/cancel")
    public ResponseDTO<VotingSessionDTO> cancelSession(
            @PathVariable Long sessionId,
            @RequestHeader(value = "X-User-Name", defaultValue = "产品负责人") String userName) {
        
        log.info("接收取消会话请求，会话ID: {}, 操作者: {}", sessionId, userName);
        
        // 调用应用服务
        VotingSession session = applicationService.cancelSession(sessionId, userName);
        
        // 领域对象转DTO
        VotingSessionDTO dto = toDTO(session);
        
        return ResponseDTO.success("估点会话已取消", dto);
    }
    
    /**
     * 开始投票（启动倒计时）
     * 
     * @param sessionId 会话ID
     * @param userName 用户名（从Header获取）
     * @return 更新的会话DTO
     */
    @PostMapping("/{sessionId}/start-voting")
    public ResponseDTO<VotingSessionDTO> startVoting(
            @PathVariable Long sessionId,
            @RequestHeader(value = "X-User-Name", defaultValue = "产品负责人") String userName) {
        
        log.info("接收开始投票请求，会话ID: {}, 操作者: {}", sessionId, userName);
        
        String currentUser = userName;
        
        // 调用应用服务（传递当前用户进行权限检查）
        VotingSession session = applicationService.startVoting(sessionId, currentUser);
        
        // 领域对象转DTO
        VotingSessionDTO dto = toDTO(session);
        
        return ResponseDTO.success("投票已开始，倒计时30秒", dto);
    }
    
    /**
     * 完成估点会话
     * 
     * @param sessionId 会话ID
     * @param requestDTO 完成会话请求DTO
     * @param userName 用户名（从Header获取）
     * @return 完成的会话DTO
     */
    @PostMapping("/{sessionId}/complete")
    public ResponseDTO<VotingSessionDTO> completeSession(
            @PathVariable Long sessionId,
            @Valid @RequestBody CompleteSessionRequestDTO requestDTO,
            @RequestHeader(value = "X-User-Name", defaultValue = "产品负责人") String userName) {
        
        log.info("接收完成会话请求，会话ID: {}, 最终点数: {}, 操作者: {}", 
                sessionId, requestDTO.getFinalStoryPoint(), userName);
        
        String currentUser = userName;
        
        // DTO转Command
        CompleteSessionCommand command = new CompleteSessionCommand(
                sessionId,
                currentUser,
                requestDTO.getFinalStoryPoint()
        );
        
        // 调用应用服务（传递当前用户进行权限检查）
        VotingSession session = applicationService.completeSession(command, currentUser);
        
        // 领域对象转DTO
        VotingSessionDTO dto = toDTO(session);
        
        return ResponseDTO.success("估点会话已完成", dto);
    }
    
    /**
     * 领域对象转DTO
     * 
     * @param session 估点会话领域对象
     * @return 会话DTO
     */
    private VotingSessionDTO toDTO(VotingSession session) {
        VotingSessionDTO dto = new VotingSessionDTO();

        // 基本信息（会话ID就是故事卡ID）
        dto.setId(session.getStoryCardId());
        dto.setStoryCardId(session.getStoryCardId());
        dto.setStatus(session.getStatus().name());
        dto.setCreatedAt(session.getCreatedAt() != null ? 
                        session.getCreatedAt().format(DATE_TIME_FORMATTER) : null);
        dto.setHostName(session.getHostName());
        dto.setVotingStartedAt(session.getVotingStartedAt() != null ?
                        session.getVotingStartedAt().format(DATE_TIME_FORMATTER) : null);
        dto.setVotingDeadline(session.getVotingDeadline() != null ?
                        session.getVotingDeadline().format(DATE_TIME_FORMATTER) : null);
        
        // 计算剩余秒数
        if (session.getVotingDeadline() != null && !session.isVotingExpired()) {
            long remaining = java.time.Duration.between(
                    java.time.LocalDateTime.now(),
                    session.getVotingDeadline()
            ).getSeconds();
            dto.setRemainingSeconds((int) Math.max(0, remaining));
        } else {
            dto.setRemainingSeconds(0);
        }
        
        // 参与者和投票
        dto.setParticipants(new ArrayList<>(session.getParticipants()));
        dto.setTotalParticipants(session.getParticipants().size());
        dto.setTotalVotes(session.getVotes().size());
        
        // 投票详情
        List<VotingSessionDTO.VoteInfo> votes = new ArrayList<>();
        session.getVotes().forEach((name, vote) -> {
            votes.add(new VotingSessionDTO.VoteInfo(
                name,
                vote.getStoryPoint() != null ? vote.getStoryPoint().getValue() : null
            ));
        });
        dto.setVotes(votes);
        
        // 统计信息（如果已揭示）
        if (session.getRevealedAt() != null) {
            try {
                var statistics = session.getStatistics();
                dto.setAverageStoryPoint(statistics.get("average"));
                dto.setMaxStoryPoint(statistics.get("max").intValue());
                dto.setMinStoryPoint(statistics.get("min").intValue());
                dto.setRevealedAt(session.getRevealedAt().format(DATE_TIME_FORMATTER));
            } catch (IllegalStateException e) {
                // 投票数据不完整，忽略统计信息
                log.warn("无法计算统计信息，会话ID: {}", session.getStoryCardId());
            }
        }
        
        // 查询故事卡完整信息
        storyCardRepository.findById(session.getStoryCardId())
                .ifPresent(card -> {
                    VotingSessionDTO.StoryCardInfo storyCardInfo = new VotingSessionDTO.StoryCardInfo();
                    storyCardInfo.setId(card.getId());
                    storyCardInfo.setTitle(card.getTitle());
                    storyCardInfo.setDescription(card.getDescription());
                    dto.setStoryCard(storyCardInfo);
                });
        
        return dto;
    }
}
