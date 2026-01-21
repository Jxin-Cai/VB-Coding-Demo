package com.example.planningpoker.presentation.controller;

import com.example.planningpoker.application.command.CreateStoryCardCommand;
import com.example.planningpoker.application.command.DeleteStoryCardCommand;
import com.example.planningpoker.application.command.UpdateStoryCardCommand;
import com.example.planningpoker.application.query.GetBacklogQuery;
import com.example.planningpoker.application.service.StoryCardApplicationService;
import com.example.planningpoker.domain.model.aggregate.StoryCard;
import com.example.planningpoker.presentation.dto.request.CreateStoryCardRequestDTO;
import com.example.planningpoker.presentation.dto.request.UpdateStoryCardRequestDTO;
import com.example.planningpoker.presentation.dto.response.ResponseDTO;
import com.example.planningpoker.presentation.dto.response.StoryCardDTO;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 故事卡Controller
 * 
 * <p>提供故事卡管理的REST API端点：
 * <ul>
 *   <li>POST /api/v1/story-cards - 创建故事卡</li>
 *   <li>GET /api/v1/story-cards - 获取需求池列表</li>
 *   <li>GET /api/v1/story-cards/{id} - 获取故事卡详情</li>
 *   <li>PUT /api/v1/story-cards/{id} - 更新故事卡</li>
 *   <li>DELETE /api/v1/story-cards/{id} - 删除故事卡</li>
 * </ul>
 * 
 * <p>职责：
 * <ul>
 *   <li>接收HTTP请求，验证参数</li>
 *   <li>DTO转Command/Query对象</li>
 *   <li>调用应用服务</li>
 *   <li>领域对象转DTO响应</li>
 * </ul>
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
@RestController
@RequestMapping("/api/v1/story-cards")
public class StoryCardController {
    
    private static final Logger log = LoggerFactory.getLogger(StoryCardController.class);
    private final StoryCardApplicationService applicationService;
    
    public StoryCardController(StoryCardApplicationService applicationService) {
        this.applicationService = applicationService;
    }
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    
    /**
     * 创建故事卡
     * 
     * @param requestDTO 创建请求DTO
     * @return 创建的故事卡DTO
     */
    @PostMapping
    public ResponseDTO<StoryCardDTO> createStoryCard(
            @Valid @RequestBody CreateStoryCardRequestDTO requestDTO) {

        log.info("接收创建故事卡请求，标题: {}", requestDTO.getTitle());

        // 直接使用前端传递的createdBy字段
        String currentUser = requestDTO.getCreatedBy();
        if (currentUser == null || currentUser.isBlank()) {
            currentUser = "匿名用户";
            log.warn("未指定创建者，使用默认值: {}", currentUser);
        }

        // DTO转Command（表现层→应用层）
        CreateStoryCardCommand command = new CreateStoryCardCommand(
                requestDTO.getTitle(),
                requestDTO.getDescription(),
                currentUser,
                requestDTO.getPoolId()
        );

        // 调用应用服务
        StoryCard card = applicationService.createStoryCard(command);

        // 领域对象转DTO（领域层→表现层）
        StoryCardDTO dto = toDTO(card);

        log.info("故事卡创建成功: id={}, title={}, hostName={}",
                dto.getId(), dto.getTitle(), dto.getHostName());
        return ResponseDTO.success("创建成功", dto);
    }
    
    /**
     * 获取需求池列表
     * 
     * <p>支持分页和排序：
     * <ul>
     *   <li>sortBy: 排序字段（id/createdAt/status），默认 id</li>
     *   <li>sortOrder: 排序方向（asc/desc），默认 desc</li>
     * </ul>
     * 
     * @param sortBy 排序字段
     * @param sortOrder 排序方向
     * @return 故事卡列表
     */
    @GetMapping
    public ResponseDTO<List<StoryCardDTO>> getBacklog(
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        
        log.debug("接收获取需求池请求，排序: {} {}", sortBy, sortOrder);
        
        // 创建Query对象
        GetBacklogQuery query = new GetBacklogQuery();
        
        // 调用应用服务
        List<StoryCard> cards = applicationService.getBacklog(query);
        
        // 排序逻辑
        List<StoryCard> sortedCards = sortCards(cards, sortBy, sortOrder);
        
        // 领域对象列表转DTO列表
        List<StoryCardDTO> dtos = sortedCards.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        
        return ResponseDTO.success(dtos);
    }
    
    /**
     * 对故事卡列表排序
     * 
     * @param cards 原始列表
     * @param sortBy 排序字段
     * @param sortOrder 排序方向
     * @return 排序后的列表
     */
    private List<StoryCard> sortCards(List<StoryCard> cards, String sortBy, String sortOrder) {
        java.util.Comparator<StoryCard> comparator;
        
        switch (sortBy) {
            case "createdAt":
                comparator = java.util.Comparator.comparing(
                    StoryCard::getCreatedAt,
                    java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())
                );
                break;
            case "status":
                // 按状态排序：NOT_ESTIMATED > ESTIMATING > ESTIMATED
                comparator = java.util.Comparator.comparing(
                    card -> getStatusOrder(card.getStatus())
                );
                break;
            case "id":
            default:
                comparator = java.util.Comparator.comparing(StoryCard::getId);
                break;
        }
        
        // 降序
        if ("desc".equalsIgnoreCase(sortOrder)) {
            comparator = comparator.reversed();
        }
        
        return cards.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取状态的排序优先级
     * 
     * @param status 卡片状态
     * @return 优先级（越小越靠前）
     */
    private int getStatusOrder(com.example.planningpoker.domain.model.CardStatus status) {
        switch (status) {
            case NOT_ESTIMATED: return 1;
            case ESTIMATING: return 2;
            case ESTIMATED: return 3;
            default: return 4;
        }
    }
    
    /**
     * 获取故事卡详情
     * 
     * @param id 故事卡ID
     * @return 故事卡DTO
     */
    @GetMapping("/{id}")
    public ResponseDTO<StoryCardDTO> getStoryCardById(@PathVariable Long id) {
        log.debug("接收获取故事卡请求，ID: {}", id);
        
        // 调用应用服务（通过getBacklog然后过滤，或直接查询）
        // 这里简化实现，通过Repository直接查询
        GetBacklogQuery query = new GetBacklogQuery();
        List<StoryCard> cards = applicationService.getBacklog(query);
        
        StoryCard card = cards.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new com.example.planningpoker.domain.exception.StoryCardNotFoundException(id));
        
        StoryCardDTO dto = toDTO(card);
        return ResponseDTO.success(dto);
    }
    
    /**
     * 更新故事卡
     * 
     * @param id 故事卡ID
     * @param requestDTO 更新请求DTO
     * @return 更新后的故事卡DTO
     */
    @PutMapping("/{id}")
    public ResponseDTO<StoryCardDTO> updateStoryCard(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStoryCardRequestDTO requestDTO) {
        
        log.info("接收更新故事卡请求，ID: {}, 新标题: {}", id, requestDTO.getTitle());
        
        // 从请求属性获取当前用户
        String currentUser = "产品负责人"; // TODO: 从@RequestAttribute获取
        
        // DTO转Command
        UpdateStoryCardCommand command = new UpdateStoryCardCommand(
                id,
                requestDTO.getTitle(),
                requestDTO.getDescription()
        );
        
        // 调用应用服务（传递当前用户进行权限检查）
        StoryCard card = applicationService.updateStoryCard(command, currentUser);
        
        // 领域对象转DTO
        StoryCardDTO dto = toDTO(card);
        
        return ResponseDTO.success("更新成功", dto);
    }
    
    /**
     * 删除故事卡
     * 
     * @param id 故事卡ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    public ResponseDTO<Void> deleteStoryCard(@PathVariable Long id) {
        log.info("接收删除故事卡请求，ID: {}", id);
        
        // 从请求属性获取当前用户
        String currentUser = "产品负责人"; // TODO: 从@RequestAttribute获取
        
        // DTO转Command
        DeleteStoryCardCommand command = new DeleteStoryCardCommand(id);
        
        // 调用应用服务（传递当前用户进行权限检查）
        applicationService.deleteStoryCard(command, currentUser);
        
        return ResponseDTO.success("删除成功", null);
    }
    
    /**
     * 领域对象转DTO
     * 
     * <p>手动映射：
     * <ul>
     *   <li>枚举转字符串：CardStatus.name()</li>
     *   <li>值对象展开：StoryPoint.getValue()</li>
     *   <li>LocalDateTime转字符串：使用ISO 8601格式</li>
     * </ul>
     * 
     * @param card 故事卡领域对象
     * @return 故事卡DTO
     */
    private StoryCardDTO toDTO(StoryCard card) {
        return StoryCardDTO.builder()
                .id(card.getId())
                .title(card.getTitle())
                .description(card.getDescription())
                .status(card.getStatus().name())
                .storyPoint(card.getStoryPoint() != null ? 
                           card.getStoryPoint().getValue() : null)
                .createdBy(card.getCreatedBy())
                .createdAt(card.getCreatedAt() != null ? 
                          card.getCreatedAt().format(DATE_TIME_FORMATTER) : null)
                .estimatedAt(card.getEstimatedAt() != null ?
                            card.getEstimatedAt().format(DATE_TIME_FORMATTER) : null)
                .poolId(card.getPoolId())
                .hostName(card.getHostName())
                .votingSessionId(card.getVotingSessionId())
                .build();
    }
}
