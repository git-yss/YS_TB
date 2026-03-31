package org.ys.transaction.domain.aggregate;

import lombok.Getter;
import org.ys.transaction.domain.entity.YsProductReview;

@Getter
public class ProductReviewAggregate {
    private final YsProductReview review;

    public ProductReviewAggregate(YsProductReview review) {
        this.review = review;
    }
}
