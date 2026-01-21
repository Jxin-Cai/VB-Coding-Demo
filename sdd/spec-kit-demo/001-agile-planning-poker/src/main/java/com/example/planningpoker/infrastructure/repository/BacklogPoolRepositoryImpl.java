package com.example.planningpoker.infrastructure.repository;

import com.example.planningpoker.domain.model.aggregate.BacklogPool;
import com.example.planningpoker.domain.repository.BacklogPoolRepository;
import com.example.planningpoker.infrastructure.persistence.BacklogPoolMapper;
import com.example.planningpoker.infrastructure.persistence.BacklogPoolPO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 需求池仓储实现
 * 
 * <p>基础设施层实现，负责领域模型与持久化对象之间的转换。
 * 
 * @author 开发团队
 * @since 2.0.0
 * @date 2026-01-20
 */
@Repository
public class BacklogPoolRepositoryImpl implements BacklogPoolRepository {
    
    private final BacklogPoolMapper mapper;
    
    public BacklogPoolRepositoryImpl(BacklogPoolMapper mapper) {
        this.mapper = mapper;
    }
    
    @Override
    public BacklogPool save(BacklogPool pool) {
        BacklogPoolPO po = toPO(pool);
        
        if (pool.getId() == null) {
            // 插入新记录
            mapper.insert(po);
            pool.setId(po.getId());
        } else {
            // 更新现有记录
            mapper.update(po);
        }
        
        return pool;
    }
    
    @Override
    public Optional<BacklogPool> findById(Long id) {
        BacklogPoolPO po = mapper.selectById(id);
        return Optional.ofNullable(po).map(this::toDomain);
    }
    
    @Override
    public List<BacklogPool> findAll() {
        List<BacklogPoolPO> poList = mapper.selectAll();
        return poList.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<BacklogPool> findByCreatedBy(String createdBy) {
        List<BacklogPoolPO> poList = mapper.selectByCreatedBy(createdBy);
        return poList.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public void deleteById(Long id) {
        mapper.deleteById(id);
    }
    
    @Override
    public boolean existsById(Long id) {
        return mapper.existsById(id) > 0;
    }
    
    // ========== 领域模型与PO转换 ==========
    
    /**
     * 将领域模型转换为PO
     * 
     * @param pool 需求池领域模型
     * @return 需求池PO
     */
    private BacklogPoolPO toPO(BacklogPool pool) {
        BacklogPoolPO po = new BacklogPoolPO();
        po.setId(pool.getId());
        po.setName(pool.getName());
        po.setDescription(pool.getDescription());
        po.setCreatedBy(pool.getCreatedBy());
        po.setCreatedAt(pool.getCreatedAt());
        return po;
    }
    
    /**
     * 将PO转换为领域模型
     * 
     * @param po 需求池PO
     * @return 需求池领域模型
     */
    private BacklogPool toDomain(BacklogPoolPO po) {
        return BacklogPool.builder()
                .id(po.getId())
                .name(po.getName())
                .description(po.getDescription())
                .createdBy(po.getCreatedBy())
                .createdAt(po.getCreatedAt())
                .build();
    }
}
