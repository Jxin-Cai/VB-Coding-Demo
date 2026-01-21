package com.example.planningpoker.application.service;

import com.example.planningpoker.application.command.CreatePoolCommand;
import com.example.planningpoker.application.query.GetPoolDetailQuery;
import com.example.planningpoker.application.query.GetPoolListQuery;
import com.example.planningpoker.domain.exception.PoolNotFoundException;
import com.example.planningpoker.domain.model.aggregate.BacklogPool;
import com.example.planningpoker.domain.model.aggregate.StoryCard;
import com.example.planningpoker.domain.repository.BacklogPoolRepository;
import com.example.planningpoker.domain.repository.StoryCardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 需求池应用服务
 * 
 * <p>应用层服务，负责编排需求池相关的用例流程。
 * 
 * <p>职责：
 * <ul>
 *   <li>编排用例流程（不包含业务逻辑）</li>
 *   <li>定义事务边界（@Transactional）</li>
 *   <li>协调多个聚合根的操作</li>
 * </ul>
 * 
 * @author 开发团队
 * @since 2.0.0
 * @date 2026-01-20
 */
@Service
public class BacklogPoolApplicationService {

    private static final Logger log = LoggerFactory.getLogger(BacklogPoolApplicationService.class);

    private final BacklogPoolRepository backlogPoolRepository;
    private final StoryCardRepository storyCardRepository;

    public BacklogPoolApplicationService(
            BacklogPoolRepository backlogPoolRepository,
            StoryCardRepository storyCardRepository) {
        this.backlogPoolRepository = backlogPoolRepository;
        this.storyCardRepository = storyCardRepository;
    }
    
    /**
     * 创建需求池
     *
     * @param command 创建需求池命令
     * @return 创建的需求池
     */
    @Transactional
    public BacklogPool createPool(CreatePoolCommand command) {
        log.info("开始创建需求池: name={}, createdBy={}",
                command.getName(), command.getCreatedBy());

        try {
            // 创建需求池聚合根
            BacklogPool pool = BacklogPool.builder()
                    .name(command.getName())
                    .description(command.getDescription())
                    .createdBy(command.getCreatedBy())
                    .build();

            // 持久化
            BacklogPool saved = backlogPoolRepository.save(pool);

            log.info("需求池创建成功: id={}, name={}", saved.getId(), saved.getName());
            return saved;

        } catch (Exception e) {
            log.error("创建需求池失败: name={}, createdBy={}, error={}",
                    command.getName(), command.getCreatedBy(), e.getMessage(), e);
            throw e; // 重新抛出，由全局异常处理器处理
        }
    }
    
    /**
     * 获取需求池列表
     *
     * @param query 查询参数
     * @return 需求池列表
     */
    @Transactional(readOnly = true)
    public List<BacklogPool> getPoolList(GetPoolListQuery query) {
        log.debug("查询需求池列表: createdBy={}", query.getCreatedBy());

        List<BacklogPool> pools;
        if (query.getCreatedBy() != null) {
            // 按创建者筛选
            pools = backlogPoolRepository.findByCreatedBy(query.getCreatedBy());
        } else {
            // 查询所有
            pools = backlogPoolRepository.findAll();
        }

        log.debug("查询到 {} 个需求池", pools.size());
        return pools;
    }
    
    /**
     * 获取需求池详情
     *
     * @param query 查询参数
     * @return 需求池详情（包含故事卡数量等信息）
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getPoolDetail(GetPoolDetailQuery query) {
        log.info("查询需求池详情: poolId={}, includeStoryCards={}",
                query.getPoolId(), query.isIncludeStoryCards());

        // 查询需求池基本信息
        BacklogPool pool = backlogPoolRepository.findById(query.getPoolId())
                .orElseThrow(() -> {
                    log.warn("需求池未找到: poolId={}", query.getPoolId());
                    return new PoolNotFoundException(query.getPoolId());
                });

        Map<String, Object> result = new HashMap<>();
        result.put("pool", pool);

        // 加载故事卡信息
        try {
            List<StoryCard> storyCards = storyCardRepository.findByPoolId(query.getPoolId());

            if (query.isIncludeStoryCards()) {
                result.put("storyCards", storyCards);
                log.debug("需求池包含 {} 张故事卡", storyCards.size());
            }

            result.put("storyCardCount", storyCards.size());

        } catch (Exception e) {
            log.error("加载需求池故事卡失败: poolId={}, error={}",
                    query.getPoolId(), e.getMessage(), e);
            // 即使加载故事卡失败，也返回需求池基本信息
            result.put("storyCardCount", 0);
        }

        return result;
    }
    
    /**
     * 获取需求池（包含故事卡数量）
     *
     * @param poolId 需求池ID
     * @return 需求池
     * @throws PoolNotFoundException 如果需求池不存在
     */
    @Transactional(readOnly = true)
    public BacklogPool getPoolById(Long poolId) {
        log.debug("查询需求池: poolId={}", poolId);

        return backlogPoolRepository.findById(poolId)
                .orElseThrow(() -> {
                    log.warn("需求池未找到: poolId={}", poolId);
                    return new PoolNotFoundException(poolId);
                });
    }
}
