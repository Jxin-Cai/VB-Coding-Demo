package com.example.planningpoker.domain.repository;

import com.example.planningpoker.domain.model.aggregate.StoryCard;
import java.util.List;
import java.util.Optional;

/**
 * 故事卡仓储接口
 * 
 * <p>定义故事卡聚合根的持久化操作。
 * 此接口在领域层定义，在基础设施层实现。
 * 
 * <p>注意：所有方法使用领域对象（StoryCard），不使用PO。
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
public interface StoryCardRepository {
    
    /**
     * 保存故事卡
     * 
     * <p>如果故事卡是新创建的（ID为null），执行插入操作；
     * 如果故事卡已存在（ID不为null），执行更新操作。
     * 
     * @param card 故事卡聚合根
     */
    void save(StoryCard card);
    
    /**
     * 根据ID查找故事卡
     * 
     * @param id 故事卡ID
     * @return Optional包装的故事卡对象，如果不存在返回Optional.empty()
     */
    Optional<StoryCard> findById(Long id);
    
    /**
     * 查找所有故事卡
     * 
     * @return 故事卡列表
     */
    List<StoryCard> findAll();
    
    /**
     * 根据需求池ID查找故事卡
     * 
     * @param poolId 需求池ID
     * @return 故事卡列表
     */
    List<StoryCard> findByPoolId(Long poolId);
    
    /**
     * 删除故事卡
     * 
     * @param id 故事卡ID
     */
    void delete(Long id);
}
