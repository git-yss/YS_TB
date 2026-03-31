package org.ys.transaction.domain.respository;

import org.ys.transaction.domain.aggregate.ShoppingHistoryAggregate;


/**
 * (YsShoppingHistory)表数据库访问层
 *
 * @author makejava
 * @since 2025-07-16 19:41:25
 */
public interface YsShoppingHistoryRespository {
    int insert(ShoppingHistoryAggregate aggregate);

}

