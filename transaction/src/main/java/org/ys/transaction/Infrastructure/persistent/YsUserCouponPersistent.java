package org.ys.transaction.Infrastructure.persistent;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.ys.transaction.domain.aggregate.UserCouponAggregate;
import org.ys.transaction.domain.respository.YsUserCouponRespository;
import org.ys.transaction.Infrastructure.dao.YsUserCouponDao;

@Repository
@RequiredArgsConstructor
public class YsUserCouponPersistent implements YsUserCouponRespository {
    private final YsUserCouponDao ysUserCouponDao;

    @Override
    public int insert(UserCouponAggregate aggregate) {
        return ysUserCouponDao.insert(aggregate.getUserCoupon());
    }
}
