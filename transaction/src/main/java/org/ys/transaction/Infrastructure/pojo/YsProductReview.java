package org.ys.transaction.Infrastructure.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * (YsProductReview)表实体类
 *
 * @author system
 * @since 2025-03-14
 */
@Data
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

}
