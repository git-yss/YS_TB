package org.ys.transaction.domain.aggregate;

import lombok.Getter;
import org.ys.transaction.domain.enums.OrderStatusEnum;
import org.ys.transaction.domain.entity.YsProductReview;

@Getter
public class ProductReviewAggregate {
    private final YsProductReview review;

    public ProductReviewAggregate(YsProductReview review) {
        this.review = review;
    }

    public static void checkNotReviewed(ProductReviewAggregate existingReview) {
        if (existingReview != null && existingReview.getReview() != null) {
            throw new IllegalStateException("您已经评价过该商品");
        }
    }

    public static void checkOrderCanReview(org.ys.transaction.domain.aggregate.OrderAggregate orderAggregate) {
        if (orderAggregate == null || orderAggregate.getOrder() == null) {
            throw new IllegalStateException("订单不存在");
        }
        if (!String.valueOf(OrderStatusEnum.PAID.getCode()).equals(orderAggregate.getOrder().getStatus())) {
            throw new IllegalStateException("只能评价已支付的订单");
        }
    }

    public static void checkUserExists(org.ys.transaction.domain.aggregate.UserAggregate userAggregate) {
        if (userAggregate == null || userAggregate.getUser() == null) {
            throw new IllegalStateException("用户不存在");
        }
    }

    public static void checkReviewCanReply(ProductReviewAggregate reviewAggregate) {
        if (reviewAggregate == null || reviewAggregate.getReview() == null) {
            throw new IllegalStateException("评价不存在");
        }
        String replyContent = reviewAggregate.getReview().getReplyContent();
        if (replyContent != null && !replyContent.trim().isEmpty()) {
            throw new IllegalStateException("该评价已回复");
        }
    }
}
