package org.ys.transaction.domain.entity;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * (YsProductReview)表实体类
 *
 * @author system
 * @since 2025-03-14
 */
@Getter
public class YsProductReview {
    // 评价ID
    private Long id;
    // 订单ID
    private Long orderId;
    // 商品ID
    private Long goodsId;
    // 用户ID
    private Long userId;
    // 用户名
    private String username;
    // 评分：1-5星
    private Integer rating;
    // 评价内容
    private String content;
    // 评价图片（多个图片用逗号分隔）
    private String images;
    // 是否匿名：0=否，1=是
    private Integer isAnonymous;
    // 评价时间
    private LocalDateTime createdAt;
    // 更新时间
    private LocalDateTime updatedAt;
    // 商家回复
    private String replyContent;
    // 回复时间
    private LocalDateTime replyTime;

    private YsProductReview(Long id, Long orderId, Long goodsId, Long userId, String username, Integer rating,
                            String content, String images, Integer isAnonymous, LocalDateTime createdAt,
                            LocalDateTime updatedAt, String replyContent, LocalDateTime replyTime) {
        this.id = id;
        this.orderId = orderId;
        this.goodsId = goodsId;
        this.userId = userId;
        this.username = username;
        this.rating = rating;
        this.content = content;
        this.images = images;
        this.isAnonymous = isAnonymous;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.replyContent = replyContent;
        this.replyTime = replyTime;
    }

    public static YsProductReview rehydrate(Long id, Long orderId, Long goodsId, Long userId, String username, Integer rating,
                                            String content, String images, Integer isAnonymous, LocalDateTime createdAt,
                                            LocalDateTime updatedAt, String replyContent, LocalDateTime replyTime) {
        return new YsProductReview(id, orderId, goodsId, userId, username, rating, content, images, isAnonymous,
                createdAt, updatedAt, replyContent, replyTime);
    }

    public void submit(Long orderId, Long goodsId, Long userId, Integer rating, String content) {
        if (orderId == null || goodsId == null || userId == null) {
            throw new IllegalArgumentException("orderId/goodsId/userId are required");
        }
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("rating must be between 1 and 5");
        }
        this.orderId = orderId;
        this.goodsId = goodsId;
        this.userId = userId;
        this.rating = rating;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public void reply(String replyContent) {
        if (replyContent == null ) {
            throw new IllegalArgumentException("replyContent cannot be empty");
        }
        this.replyContent = replyContent;
        this.replyTime = LocalDateTime.now();
        this.updatedAt = this.replyTime;
    }


}
