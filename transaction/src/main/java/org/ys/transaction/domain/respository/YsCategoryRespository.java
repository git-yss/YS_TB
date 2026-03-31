package org.ys.transaction.domain.respository;

import org.ys.transaction.domain.aggregate.CateGoryAggregate;
import java.util.List;

/**
 * (YsCategory)表数据库访问层
 *
 * @author makejava
 * @since 2025-07-16
 */
public interface YsCategoryRespository  {

    /**
     * 根据父分类ID查询子分类
     */
    List<CateGoryAggregate> selectByParentId(CateGoryAggregate aggregate);

    /**
     * 查询所有启用的分类
     */
    List<CateGoryAggregate> selectEnabled(CateGoryAggregate aggregate);
}
