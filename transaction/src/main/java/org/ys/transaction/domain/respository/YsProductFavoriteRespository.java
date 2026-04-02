package org.ys.transaction.domain.respository;

import org.ys.transaction.domain.aggregate.ProductFavoriteAggregate;

import java.util.List;
import java.util.Map;

/**
 * (YsProductFavorite)表数据库访问层
 *
 * @author makejava
 * @since 2025-07-16
 */
public interface YsProductFavoriteRespository  {

    /**
     * 查询用户收藏列表
     */
    List<ProductFavoriteAggregate> selectByUserId(ProductFavoriteAggregate aggregate);

    /**
     * 检查是否已收藏
     */
    ProductFavoriteAggregate selectByUserAndGoods(ProductFavoriteAggregate aggregate);

    int insert(ProductFavoriteAggregate aggregate);

    int deleteById(ProductFavoriteAggregate aggregate);

    int deleteByUserAndGoods(ProductFavoriteAggregate aggregate);
}
