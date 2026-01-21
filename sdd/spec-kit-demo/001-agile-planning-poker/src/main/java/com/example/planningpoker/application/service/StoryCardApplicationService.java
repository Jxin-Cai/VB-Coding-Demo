package com.example.planningpoker.application.service;

import com.example.planningpoker.application.command.CreateStoryCardCommand;
import com.example.planningpoker.application.command.DeleteStoryCardCommand;
import com.example.planningpoker.application.command.UpdateStoryCardCommand;
import com.example.planningpoker.application.query.GetBacklogQuery;
import com.example.planningpoker.domain.exception.StoryCardNotFoundException;
import com.example.planningpoker.domain.exception.UnauthorizedException;
import com.example.planningpoker.domain.model.aggregate.StoryCard;
import com.example.planningpoker.domain.model.CardStatus;
import com.example.planningpoker.domain.repository.BacklogPoolRepository;
import com.example.planningpoker.domain.repository.StoryCardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 故事卡应用服务
 * 
 * <p>职责：协调故事卡管理的用例流程，包括创建、查询、更新和删除故事卡。
 * 
 * <p>应用层不包含业务逻辑，业务逻辑在领域层（StoryCard聚合根）中实现。
 * 应用层负责：
 * <ul>
 *   <li>验证输入参数</li>
 *   <li>协调领域对象和仓储</li>
 *   <li>定义事务边界</li>
 *   <li>返回结果</li>
 * </ul>
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
@Service
@Transactional
public class StoryCardApplicationService {
    
    private static final Logger log = LoggerFactory.getLogger(StoryCardApplicationService.class);
    private final StoryCardRepository storyCardRepository;
    private final BacklogPoolRepository backlogPoolRepository;
    
    public StoryCardApplicationService(
            StoryCardRepository storyCardRepository,
            BacklogPoolRepository backlogPoolRepository) {
        this.storyCardRepository = storyCardRepository;
        this.backlogPoolRepository = backlogPoolRepository;
    }
    
    /**
     * 创建故事卡
     * 
     * <p>用例流程：
     * <ol>
     *   <li>验证输入参数</li>
     *   <li>使用Builder模式创建故事卡聚合根</li>
     *   <li>持久化故事卡</li>
     *   <li>返回创建的故事卡</li>
     * </ol>
     * 
     * @param command 创建命令
     * @return 创建的故事卡
     */
    public StoryCard createStoryCard(CreateStoryCardCommand command) {
        log.info("创建故事卡，标题: {}, 创建者: {}, 需求池ID: {}", 
                command.getTitle(), command.getCreatedBy(), command.getPoolId());
        
        // 验证需求池是否存在
        if (!backlogPoolRepository.existsById(command.getPoolId())) {
            throw new IllegalArgumentException(
                    String.format("需求池不存在: %d", command.getPoolId())
            );
        }
        
        // 使用Builder模式创建故事卡（应用Effective Java Item 2）
        StoryCard card = StoryCard.builder()
                .title(command.getTitle())
                .description(command.getDescription())
                .createdBy(command.getCreatedBy())
                .poolId(command.getPoolId())
                .hostName(command.getCreatedBy()) // 创建者自动成为主持人
                .build();
        
        // 持久化
        storyCardRepository.save(card);
        
        log.info("故事卡创建成功，ID: {}, 主持人: {}", card.getId(), card.getHostName());
        return card;
    }
    
    /**
     * 获取需求池列表
     * 
     * <p>用例流程：
     * <ol>
     *   <li>查询所有故事卡</li>
     *   <li>返回列表</li>
     * </ol>
     * 
     * @param query 查询对象
     * @return 故事卡列表
     */
    @Transactional(readOnly = true)
    public List<StoryCard> getBacklog(GetBacklogQuery query) {
        log.debug("查询需求池列表");
        
        List<StoryCard> cards = storyCardRepository.findAll();
        
        log.debug("查询到 {} 张故事卡", cards.size());
        return cards;
    }
    
    /**
     * 更新故事卡
     * 
     * <p>用例流程：
     * <ol>
     *   <li>验证输入参数</li>
     *   <li>根据ID加载故事卡聚合根</li>
     *   <li>调用聚合根的update方法（业务逻辑在领域层）</li>
     *   <li>持久化更新</li>
     *   <li>返回更新后的故事卡</li>
     * </ol>
     * 
     * @param command 更新命令
     * @return 更新后的故事卡
     * @throws StoryCardNotFoundException 如果故事卡不存在
     */
    public StoryCard updateStoryCard(UpdateStoryCardCommand command, String currentUser) {
        log.info("更新故事卡，ID: {}, 新标题: {}, 操作者: {}", 
                command.getStoryCardId(), command.getTitle(), currentUser);
        
        // 加载故事卡
        StoryCard card = storyCardRepository.findById(command.getStoryCardId())
                .orElseThrow(() -> new StoryCardNotFoundException(command.getStoryCardId()));
        
        // 权限检查：仅主持人可修改
        if (!card.isHost(currentUser)) {
            throw new UnauthorizedException(
                    String.format("只有主持人 %s 可以修改该故事卡", card.getHostName())
            );
        }
        
        // 调用领域方法更新（业务规则在领域层）
        card.update(command.getTitle(), command.getDescription());
        
        // 持久化
        storyCardRepository.save(card);
        
        log.info("故事卡更新成功，ID: {}", card.getId());
        return card;
    }
    
    /**
     * 删除故事卡
     * 
     * <p>用例流程：
     * <ol>
     *   <li>验证输入参数</li>
     *   <li>根据ID加载故事卡</li>
     *   <li>检查故事卡状态（只能删除未估点的卡）</li>
     *   <li>删除故事卡</li>
     * </ol>
     * 
     * @param command 删除命令
     * @throws StoryCardNotFoundException 如果故事卡不存在
     * @throws IllegalStateException 如果故事卡正在估点中或已估点
     */
    public void deleteStoryCard(DeleteStoryCardCommand command, String currentUser) {
        log.info("删除故事卡，ID: {}, 操作者: {}", command.getStoryCardId(), currentUser);
        
        // 加载故事卡
        StoryCard card = storyCardRepository.findById(command.getStoryCardId())
                .orElseThrow(() -> new StoryCardNotFoundException(command.getStoryCardId()));
        
        // 权限检查：仅主持人可删除
        if (!card.isHost(currentUser)) {
            throw new UnauthorizedException(
                    String.format("只有主持人 %s 可以删除该故事卡", card.getHostName())
            );
        }
        
        // 业务规则：只能删除未估点的故事卡
        if (card.getStatus() == CardStatus.ESTIMATING) {
            throw new IllegalStateException("故事卡正在估点中，不能删除");
        }
        if (card.getStatus() == CardStatus.ESTIMATED) {
            throw new IllegalStateException("故事卡已估点，不能删除");
        }
        
        // 删除
        storyCardRepository.delete(command.getStoryCardId());
        
        log.info("故事卡删除成功，ID: {}", command.getStoryCardId());
    }
}
