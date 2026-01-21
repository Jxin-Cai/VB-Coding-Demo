package com.example.planningpoker.infrastructure.repository;

import com.example.planningpoker.domain.model.aggregate.StoryCard;
import com.example.planningpoker.domain.model.CardStatus;
import com.example.planningpoker.domain.model.valueobject.StoryPoint;
import com.example.planningpoker.domain.repository.StoryCardRepository;
import com.example.planningpoker.infrastructure.persistence.StoryCardMapper;
import com.example.planningpoker.infrastructure.persistence.StoryCardPO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 故事卡仓储实现
 * 
 * <p>使用MyBatis实现故事卡的持久化操作。
 * 负责领域对象（StoryCard）与持久化对象（StoryCardPO）之间的转换。
 * 
 * <p>注意：
 * <ul>
 *   <li>PO仅在此类内部使用，不传递到领域层</li>
 *   <li>枚举需要手动转换为字符串存储</li>
 *   <li>值对象（StoryPoint）需要手动展开/重建</li>
 * </ul>
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
@Repository
public class StoryCardRepositoryImpl implements StoryCardRepository {
    
    private final StoryCardMapper mapper;
    
    public StoryCardRepositoryImpl(StoryCardMapper mapper) {
        this.mapper = mapper;
    }
    
    /**
     * 保存故事卡
     * 
     * <p>根据ID判断是插入还是更新。
     * 
     * @param card 故事卡聚合根
     */
    @Override
    public void save(StoryCard card) {
        StoryCardPO po = toPO(card);
        
        if (po.getId() == null) {
            // 插入
            mapper.insert(po);
            // 回填ID到领域对象
            card.setId(po.getId());
        } else {
            // 更新
            mapper.update(po);
        }
    }
    
    /**
     * 根据ID查找故事卡
     * 
     * @param id 故事卡ID
     * @return Optional包装的故事卡，如果不存在返回Optional.empty()
     */
    @Override
    public Optional<StoryCard> findById(Long id) {
        StoryCardPO po = mapper.selectById(id);
        
        if (po == null) {
            return Optional.empty();
        }
        
        return Optional.of(toDomain(po));
    }
    
    /**
     * 查找所有故事卡
     * 
     * @return 故事卡列表
     */
    @Override
    public List<StoryCard> findAll() {
        List<StoryCardPO> pos = mapper.selectAll();
        
        return pos.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据需求池ID查找故事卡
     * 
     * @param poolId 需求池ID
     * @return 故事卡列表
     */
    @Override
    public List<StoryCard> findByPoolId(Long poolId) {
        List<StoryCardPO> pos = mapper.selectByPoolId(poolId);
        
        return pos.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
    
    /**
     * 删除故事卡
     * 
     * @param id 故事卡ID
     */
    @Override
    public void delete(Long id) {
        mapper.delete(id);
    }
    
    /**
     * 领域对象转PO
     * 
     * <p>手动转换逻辑：
     * <ul>
     *   <li>枚举转字符串：CardStatus.name()</li>
     *   <li>值对象展开：StoryPoint.getValue()</li>
     * </ul>
     * 
     * @param card 故事卡领域对象
     * @return 故事卡PO
     */
    private StoryCardPO toPO(StoryCard card) {
        StoryCardPO po = new StoryCardPO();
        po.setId(card.getId());
        po.setTitle(card.getTitle());
        po.setDescription(card.getDescription());
        // 枚举转字符串
        po.setStatus(card.getStatus().name());
        // 值对象转Integer
        po.setStoryPoint(card.getStoryPoint() != null ? 
                         card.getStoryPoint().getValue() : null);
        po.setCreatedBy(card.getCreatedBy());
        po.setCreatedAt(card.getCreatedAt());
        po.setEstimatedAt(card.getEstimatedAt());
        // 新增字段
        po.setPoolId(card.getPoolId());
        po.setHostName(card.getHostName());
        po.setVotingSessionId(card.getVotingSessionId());
        
        return po;
    }
    
    /**
     * PO转领域对象
     * 
     * <p>手动转换逻辑：
     * <ul>
     *   <li>字符串转枚举：CardStatus.valueOf()</li>
     *   <li>Integer重建值对象：StoryPoint.of()</li>
     * </ul>
     * 
     * @param po 故事卡PO
     * @return 故事卡领域对象
     */
    private StoryCard toDomain(StoryCardPO po) {
        return StoryCard.builder()
                .id(po.getId())
                .title(po.getTitle())
                .description(po.getDescription())
                // 字符串转枚举
                .status(CardStatus.valueOf(po.getStatus()))
                // Integer重建值对象
                .storyPoint(po.getStoryPoint() != null ? 
                           StoryPoint.of(po.getStoryPoint()) : null)
                .createdBy(po.getCreatedBy())
                .createdAt(po.getCreatedAt())
                .estimatedAt(po.getEstimatedAt())
                // 新增字段
                .poolId(po.getPoolId())
                .hostName(po.getHostName())
                .votingSessionId(po.getVotingSessionId())
                .build();
    }
}
