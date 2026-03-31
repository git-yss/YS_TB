package org.ys.transaction.domain.aggregate;

import lombok.Getter;
import org.ys.transaction.domain.entity.YsUserCoupon;

@Getter
public class UserCouponAggregate {
    private final YsUserCoupon userCoupon;

    public UserCouponAggregate(YsUserCoupon userCoupon) {
        this.userCoupon = userCoupon;
    }
}
