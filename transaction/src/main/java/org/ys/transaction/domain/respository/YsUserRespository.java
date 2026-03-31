package org.ys.transaction.domain.respository;

import org.ys.transaction.domain.aggregate.LoginAggregate;
import org.ys.transaction.domain.aggregate.UserAggregate;

public interface YsUserRespository {
    LoginAggregate queryUser(LoginAggregate aggregate);

    UserAggregate selectAggregateById(UserAggregate aggregate);

    UserAggregate selectByName(UserAggregate aggregate);

    UserAggregate selectByEmail(UserAggregate aggregate);

    int updateBalanceById(UserAggregate aggregate);

    int insert(UserAggregate aggregate);
}
