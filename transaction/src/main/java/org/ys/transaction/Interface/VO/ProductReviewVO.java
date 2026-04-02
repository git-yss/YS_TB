package org.ys.transaction.Interface.VO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductReviewVO {
    private Long id;
    private Long orderId;
    private Long goodsId;
    private Long userId;
    private String username;
    private Integer rating;
    private String content;
    private String images;
    private Integer isAnonymous;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String replyContent;
    private LocalDateTime replyTime;
}
