package org.ys.transaction.domain.aggregate;

import lombok.Getter;
import org.ys.transaction.domain.entity.YsGoods;
import org.ys.transaction.domain.entity.YsOrder;
import org.ys.transaction.domain.entity.YsUser;

import java.math.BigDecimal;

@Getter
public class OrderAggregate {
    private final YsOrder order;
    private final YsGoods goods;
    private final YsUser user;

    public OrderAggregate(YsOrder order, YsGoods goods, YsUser user) {
        if (order == null) {
            throw new IllegalArgumentException("order cannot be null");
        }
        this.order = order;
        this.goods = goods;
        this.user = user;
    }

    public void payByBalance() {
        if (user == null) {
            throw new IllegalStateException("user is required for payment");
        }
        BigDecimal amount = order.getTotalAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("order totalAmount is invalid");
        }
        user.debitBalance(amount);
        order.markPaid("balance");
    }
}
