package org.ys.transaction.domain.inteface;

import org.ys.transaction.domain.vo.DomainResult;

import java.util.Map;

/**
 * 商品评价服务接口
 *
 * @author makejava
 * @since 2025-07-16
 */
public interface ReviewService {

    /**
     * 发表评价
     * @param params 评价参数（订单ID、商品ID、用户ID、评分、内容、图片、是否匿名）
     * @return 评价结果
     */
    DomainResult addReview(Map<String, Object> params);

    /**
     * 查询商品评价列表
     * @param goodsId 商品ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 评价列表
     */
    DomainResult getReviewsByGoodsId(Long goodsId, Integer pageNum, Integer pageSize);

    /**
     * 查询用户评价列表
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 评价列表
     */
    DomainResult getReviewsByUserId(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 商家回复评价
     * @param reviewId 评价ID
     * @param replyContent 回复内容
     * @return 回复结果
     */
    DomainResult replyReview(Long reviewId, String replyContent);

    /**
     * 获取商品评价统计
     * @param goodsId 商品ID
     * @return 统计信息（平均评分、评价总数等）
     */
    DomainResult getReviewStats(Long goodsId);
}
