package org.ys.transaction.Infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.ys.transaction.Infrastructure.pojo.YsCategory;

import java.util.List;

/**
 * (YsCategory)表数据库访问层
 *
 * @author makejava
 * @since 2025-07-16
 */
public interface YsCategoryDao extends BaseMapper<YsCategory> {

    /**
     * 根据父分类ID查询子分类
     */
    List<YsCategory> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 查询所有启用的分类
     */
    List<YsCategory> selectEnabled();
}
