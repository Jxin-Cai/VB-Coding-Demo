package com.example.planningpoker.domain.repository;

import com.example.planningpoker.domain.model.aggregate.BacklogPool;

import java.util.List;
import java.util.Optional;

/**
 * 需求池仓储接口
 * 
 * <p>领域层定义的仓储接口，由基础设施层实现。
 * 
 * <p>职责：
 * <ul>
 *   <li>持久化需求池聚合根</li>
 *   <li>查询需求池（按ID、按创建者）</li>
 *   <li>删除需求池</li>
 * </ul>
 * 
 * @author 开发团队
 * @since 2.0.0
 * @date 2026-01-20
 */
public interface BacklogPoolRepository {
    
    /**
     * 保存需求池
     * 
     * <p>如果ID为空则插入新记录，否则更新现有记录。
     * 
     * @param pool 需求池聚合根
     * @return 保存后的需求池（包含生成的ID）
     */
    BacklogPool save(BacklogPool pool);
    
    /**
     * 根据ID查询需求池
     * 
     * @param id 需求池ID
     * @return 需求池Optional（不存在时为空）
     */
    Optional<BacklogPool> findById(Long id);
    
    /**
     * 查询所有需求池
     * 
     * @return 需求池列表（按创建时间降序）
     */
    List<BacklogPool> findAll();
    
    /**
     * 根据创建者查询需求池
     * 
     * @param createdBy 创建者用户名
     * @return 需求池列表
     */
    List<BacklogPool> findByCreatedBy(String createdBy);
    
    /**
     * 删除需求池
     * 
     * <p>级联删除该需求池下的所有故事卡。
     * 
     * @param id 需求池ID
     */
    void deleteById(Long id);
    
    /**
     * 检查需求池是否存在
     * 
     * @param id 需求池ID
     * @return 如果存在返回true
     */
    boolean existsById(Long id);
}
