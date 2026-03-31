package org.ys.transaction.domain.aggregate;

import lombok.Getter;
import org.ys.transaction.domain.entity.YsShoppingHistory;

@Getter
public class ShoppingHistoryAggregate {
    private final YsShoppingHistory shoppingHistory;

    public ShoppingHistoryAggregate(YsShoppingHistory shoppingHistory) {
        this.shoppingHistory = shoppingHistory;
    }
}
