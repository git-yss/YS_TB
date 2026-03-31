package org.ys.transaction.domain.respository;

import org.ys.transaction.domain.aggregate.UserCouponAggregate;

/**
 * 用户优惠券关联表 Dao
 *
 * @author makejava
 * @since 2025-07-16
 */
public interface YsUserCouponRespository  {
    int insert(UserCouponAggregate aggregate);
}
