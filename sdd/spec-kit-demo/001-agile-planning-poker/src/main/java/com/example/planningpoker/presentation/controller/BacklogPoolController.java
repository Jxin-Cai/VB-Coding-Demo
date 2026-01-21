package com.example.planningpoker.presentation.controller;

import com.example.planningpoker.application.command.CreatePoolCommand;
import com.example.planningpoker.application.query.GetPoolDetailQuery;
import com.example.planningpoker.application.query.GetPoolListQuery;
import com.example.planningpoker.application.service.BacklogPoolApplicationService;
import com.example.planningpoker.domain.model.aggregate.BacklogPool;
import com.example.planningpoker.domain.model.aggregate.StoryCard;
import com.example.planningpoker.domain.repository.StoryCardRepository;
import com.example.planningpoker.presentation.dto.request.CreatePoolRequestDTO;
import com.example.planningpoker.presentation.dto.response.BacklogPoolDTO;
import com.example.planningpoker.presentation.dto.response.ResponseDTO;
import com.example.planningpoker.presentation.dto.response.StoryCardDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 需求池控制器
 * 
 * <p>提供需求池管理的REST API接口。
 * 
 * @author 开发团队
 * @since 2.0.0
 * @date 2026-01-20
 */
@RestController
@RequestMapping("/api/v1/pools")
@Tag(name = "需求池管理", description = "需求池相关接口")
public class BacklogPoolController {
    
    private static final Logger log = LoggerFactory.getLogger(BacklogPoolController.class);
    
    private final BacklogPoolApplicationService backlogPoolApplicationService;
    private final StoryCardRepository storyCardRepository;
    
    public BacklogPoolController(
            BacklogPoolApplicationService backlogPoolApplicationService,
            StoryCardRepository storyCardRepository) {
        this.backlogPoolApplicationService = backlogPoolApplicationService;
        this.storyCardRepository = storyCardRepository;
    }
    
    /**
     * 创建需求池
     * 
     * @param requestDTO 创建需求池请求
     * @return 创建的需求池信息
     */
    @PostMapping
    @Operation(summary = "创建需求池", description = "创建一个新的需求池")
    public ResponseDTO<BacklogPoolDTO> createPool(
            @Valid @RequestBody CreatePoolRequestDTO requestDTO) {

        log.info("接收创建需求池请求: name={}, description={}",
                requestDTO.getName(), requestDTO.getDescription());

        try {
            // 确保createdBy不为空
            String createdBy = requestDTO.getCreatedBy();
            if (createdBy == null || createdBy.isBlank()) {
                createdBy = "匿名用户";
                log.warn("未指定创建者，使用默认值: {}", createdBy);
            }

            // 转换为Command（createdBy从前端传递）
            CreatePoolCommand command = CreatePoolCommand.of(
                    requestDTO.getName(),
                    requestDTO.getDescription(),
                    createdBy
            );

            // 调用应用服务
            BacklogPool pool = backlogPoolApplicationService.createPool(command);

            // 转换为DTO
            BacklogPoolDTO dto = BacklogPoolDTO.from(pool, 0);

            log.info("需求池创建成功返回: id={}, name={}", dto.getId(), dto.getName());
            return ResponseDTO.success("需求池创建成功", dto);

        } catch (Exception e) {
            log.error("创建需求池失败: name={}, error={}", requestDTO.getName(), e.getMessage(), e);
            // 不处理异常，交给全局异常处理器
            throw e;
        }
    }
    
    /**
     * 获取需求池列表
     * 
     * @return 需求池列表
     */
    @GetMapping
    @Operation(summary = "获取需求池列表", description = "获取所有需求池")
    public ResponseDTO<List<BacklogPoolDTO>> getPoolList() {
        log.info("接收获取需求池列表请求");

        try {
            // 查询所有需求池
            List<BacklogPool> pools = backlogPoolApplicationService.getPoolList(GetPoolListQuery.all());

            // 转换为DTO（包含故事卡数量）
            List<BacklogPoolDTO> dtoList = pools.stream()
                    .map(pool -> {
                        try {
                            List<StoryCard> cards = storyCardRepository.findByPoolId(pool.getId());
                            return BacklogPoolDTO.from(pool, cards.size());
                        } catch (Exception e) {
                            log.error("获取需求池故事卡数量失败: poolId={}, error={}",
                                    pool.getId(), e.getMessage());
                            // 即使失败也返回需求池，故事卡数量为0
                            return BacklogPoolDTO.from(pool, 0);
                        }
                    })
                    .collect(Collectors.toList());

            log.info("返回需求池列表: count={}", dtoList.size());
            return ResponseDTO.success(dtoList);

        } catch (Exception e) {
            log.error("获取需求池列表失败: error={}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 获取需求池详情
     * 
     * @param id 需求池ID
     * @return 需求池详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取需求池详情", description = "获取指定需求池的详情信息")
    public ResponseDTO<BacklogPoolDTO> getPoolById(@PathVariable Long id) {
        log.info("接收获取需求池详情请求: poolId={}", id);

        try {
            // 查询需求池
            BacklogPool pool = backlogPoolApplicationService.getPoolById(id);

            // 统计故事卡数量
            List<StoryCard> cards = storyCardRepository.findByPoolId(id);

            // 转换为DTO
            BacklogPoolDTO dto = BacklogPoolDTO.from(pool, cards.size());

            log.info("返回需求池详情: id={}, name={}, storyCardCount={}",
                    dto.getId(), dto.getName(), dto.getStoryCardCount());
            return ResponseDTO.success(dto);

        } catch (Exception e) {
            log.error("获取需求池详情失败: poolId={}, error={}", id, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 获取需求池的故事卡列表
     * 
     * @param id 需求池ID
     * @return 故事卡列表
     */
    @GetMapping("/{id}/story-cards")
    @Operation(summary = "获取需求池的故事卡", description = "获取指定需求池下的所有故事卡")
    public ResponseDTO<List<StoryCardDTO>> getStoryCardsByPoolId(@PathVariable Long id) {
        log.info("接收获取需求池故事卡请求: poolId={}", id);

        try {
            // 验证需求池存在
            BacklogPool pool = backlogPoolApplicationService.getPoolById(id);
            log.debug("需求池验证通过: id={}, name={}", pool.getId(), pool.getName());

            // 查询故事卡
            List<StoryCard> cards = storyCardRepository.findByPoolId(id);

            // 转换为DTO
            List<StoryCardDTO> dtoList = cards.stream()
                    .map(StoryCardDTO::from)
                    .collect(Collectors.toList());

            log.info("返回需求池故事卡列表: poolId={}, count={}", id, dtoList.size());
            return ResponseDTO.success(dtoList);

        } catch (Exception e) {
            log.error("获取需求池故事卡失败: poolId={}, error={}", id, e.getMessage(), e);
            throw e;
        }
    }
}
