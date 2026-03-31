package org.ys.transaction.domain.respository;

import org.ys.transaction.domain.aggregate.ProductReviewAggregate;

import java.util.List;

/**
 * (YsProductReview)表数据库访问层
 *
 * @author system
 * @since 2025-03-14
 */
public interface YsProductReviewRespository  {

    /**
     * 根据商品ID查询评价列表（分页）
     */
    List<ProductReviewAggregate> queryReviewsByGoodsId(ProductReviewAggregate aggregate);

    /**
     * 根据用户ID查询评价列表（分页）
     */
    List<ProductReviewAggregate> queryReviewsByUserId(ProductReviewAggregate aggregate);

    /**
     * 查询商品的平均评分
     */
    ProductReviewAggregate getGoodsRatingStats(ProductReviewAggregate aggregate);

    /**
     * 检查用户是否已评价过该订单商品
     */
    ProductReviewAggregate checkReviewed(ProductReviewAggregate aggregate);
}
