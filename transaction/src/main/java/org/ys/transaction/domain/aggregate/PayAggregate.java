package org.ys.transaction.domain.aggregate;

import lombok.Getter;
import org.ys.transaction.domain.entity.YsOrder;
import org.ys.transaction.domain.entity.YsUser;

@Getter
public class PayAggregate {
    private final YsOrder order;
    private final YsUser user;

    public PayAggregate(YsOrder order, YsUser user) {
        if (order == null || user == null) {
            throw new IllegalArgumentException("order and user cannot be null");
        }
        this.order = order;
        this.user = user;
    }
}
