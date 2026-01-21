package com.example.planningpoker.infrastructure.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 故事卡Mapper接口
 * 
 * <p>MyBatis Mapper接口，定义故事卡相关的数据库操作。
 * 对应的SQL映射在 mapper/StoryCardMapper.xml 中定义。
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
@Mapper
public interface StoryCardMapper {
    
    /**
     * 插入故事卡
     * 
     * @param card 故事卡PO
     */
    void insert(StoryCardPO card);
    
    /**
     * 根据ID查询故事卡
     * 
     * @param id 故事卡ID
     * @return 故事卡PO，如果不存在返回null
     */
    StoryCardPO selectById(@Param("id") Long id);
    
    /**
     * 查询所有故事卡
     * 
     * @return 故事卡PO列表
     */
    List<StoryCardPO> selectAll();
    
    /**
     * 根据状态查询故事卡
     * 
     * @param status 故事卡状态
     * @return 故事卡PO列表
     */
    List<StoryCardPO> selectByStatus(@Param("status") String status);
    
    /**
     * 根据需求池ID查询故事卡
     * 
     * @param poolId 需求池ID
     * @return 故事卡PO列表
     */
    List<StoryCardPO> selectByPoolId(@Param("poolId") Long poolId);
    
    /**
     * 更新故事卡
     * 
     * @param card 故事卡PO
     */
    void update(StoryCardPO card);
    
    /**
     * 删除故事卡
     * 
     * @param id 故事卡ID
     */
    void delete(@Param("id") Long id);
}
