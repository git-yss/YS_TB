package org.ys.transaction.domain.respository;

import org.ys.transaction.domain.aggregate.LoginAggregate;
import org.ys.transaction.domain.aggregate.UserAggregate;

public interface YsUserRespository {
    LoginAggregate queryUser(LoginAggregate aggregate);

    UserAggregate selectAggregateById(String userId);

    UserAggregate selectByName(String username );

    UserAggregate selectByEmail(UserAggregate aggregate);

    int updateBalanceById(UserAggregate aggregate);

    int updateById(UserAggregate aggregate);

    int insert(UserAggregate aggregate);
}
