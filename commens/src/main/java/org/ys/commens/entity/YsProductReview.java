package org.ys.commens.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * (YsProductReview)表实体类
 *
 * @author system
 * @since 2025-03-14
 */
@SuppressWarnings("serial")
@TableName("ys_product_review")
public class YsProductReview {
    // 评价ID
    @TableId(value = "id", type = IdType.AUTO)
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public Integer getIsAnonymous() {
        return isAnonymous;
    }

    public void setIsAnonymous(Integer isAnonymous) {
        this.isAnonymous = isAnonymous;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getReplyContent() {
        return replyContent;
    }

    public void setReplyContent(String replyContent) {
        this.replyContent = replyContent;
    }

    public LocalDateTime getReplyTime() {
        return replyTime;
    }

    public void setReplyTime(LocalDateTime replyTime) {
        this.replyTime = replyTime;
    }
}
