package org.ys.commens.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.ys.commens.entity.YsProductReview;

import java.util.List;
import java.util.Map;

/**
 * (YsProductReview)表数据库访问层
 *
 * @author system
 * @since 2025-03-14
 */
public interface YsProductReviewDao extends BaseMapper<YsProductReview> {

    /**
     * 根据商品ID查询评价列表（分页）
     */
    Page<YsProductReview> queryReviewsByGoodsId(@Param("page") IPage<YsProductReview> page, @Param("goodsId") Long goodsId);

    /**
     * 根据用户ID查询评价列表（分页）
     */
    Page<YsProductReview> queryReviewsByUserId(@Param("page") IPage<YsProductReview> page, @Param("userId") Long userId);

    /**
     * 查询商品的平均评分
     */
    Map<String, Object> getGoodsRatingStats(@Param("goodsId") Long goodsId);

    /**
     * 检查用户是否已评价过该订单商品
     */
    YsProductReview checkReviewed(@Param("orderId") Long orderId, @Param("userId") Long userId, @Param("goodsId") Long goodsId);
}
