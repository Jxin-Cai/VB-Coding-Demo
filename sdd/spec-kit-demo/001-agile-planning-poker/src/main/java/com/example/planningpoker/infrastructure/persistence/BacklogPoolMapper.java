package com.example.planningpoker.infrastructure.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 需求池MyBatis映射器
 * 
 * <p>定义需求池表的CRUD操作。
 * 
 * @author 开发团队
 * @since 2.0.0
 * @date 2026-01-20
 */
@Mapper
public interface BacklogPoolMapper {
    
    /**
     * 插入需求池
     * 
     * @param pool 需求池PO
     * @return 插入的行数
     */
    int insert(BacklogPoolPO pool);
    
    /**
     * 更新需求池
     * 
     * @param pool 需求池PO
     * @return 更新的行数
     */
    int update(BacklogPoolPO pool);
    
    /**
     * 根据ID查询需求池
     * 
     * @param id 需求池ID
     * @return 需求池PO（不存在时为null）
     */
    BacklogPoolPO selectById(@Param("id") Long id);
    
    /**
     * 查询所有需求池
     * 
     * @return 需求池列表（按创建时间降序）
     */
    List<BacklogPoolPO> selectAll();
    
    /**
     * 根据创建者查询需求池
     * 
     * @param createdBy 创建者用户名
     * @return 需求池列表
     */
    List<BacklogPoolPO> selectByCreatedBy(@Param("createdBy") String createdBy);
    
    /**
     * 根据ID删除需求池
     * 
     * @param id 需求池ID
     * @return 删除的行数
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 检查需求池是否存在
     * 
     * @param id 需求池ID
     * @return 如果存在返回1，否则返回0
     */
    int existsById(@Param("id") Long id);
}
