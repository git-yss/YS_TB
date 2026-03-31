package org.ys.transaction.domain.inteface;

import java.util.Map;

/**
 * 商品评价服务接口
 *
 * @author system
 * @since 2025-03-14
 */
public interface ProductReviewService {

    /**
     * 发表商品评价
     * @param orderId 订单ID
     * @param goodsId 商品ID
     * @param userId 用户ID
     * @param rating 评分（1-5星）
     * @param content 评价内容
     * @param images 评价图片（多个用逗号分隔）
     * @param isAnonymous 是否匿名（0=否，1=是）
     * @return 评价结果
     */
    void addReview(Long orderId, Long goodsId, Long userId, Integer rating,
                   String content, String images, Integer isAnonymous);

    /**
     * 获取商品评价列表（分页）
     * @param goodsId 商品ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 评价列表
     */
    Map<String, Object> getReviewsByGoodsId(Long goodsId, Integer pageNum, Integer pageSize);

    /**
     * 获取用户评价列表（分页）
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 评价列表
     */
    Map<String, Object> getReviewsByUserId(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取商品评价统计信息
     * @param goodsId 商品ID
     * @return 评价统计（平均分、各星级数量等）
     */
    Map<String, Object> getReviewStats(Long goodsId);

    /**
     * 商家回复评价
     * @param reviewId 评价ID
     * @param replyContent 回复内容
     * @return 回复结果
     */
    void replyReview(Long reviewId, String replyContent);
}
