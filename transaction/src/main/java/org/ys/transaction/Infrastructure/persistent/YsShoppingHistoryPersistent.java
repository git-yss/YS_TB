package org.ys.transaction.Infrastructure.persistent;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.ys.transaction.domain.aggregate.ShoppingHistoryAggregate;
import org.ys.transaction.domain.respository.YsShoppingHistoryRespository;
import org.ys.transaction.Infrastructure.dao.YsShoppingHistoryDao;
import org.ys.transaction.Infrastructure.pojo.YsShoppingHistory;

@Repository
@RequiredArgsConstructor
public class YsShoppingHistoryPersistent implements YsShoppingHistoryRespository {
    private final YsShoppingHistoryDao ysShoppingHistoryDao;

    @Override
    public int insert(ShoppingHistoryAggregate aggregate) {
        YsShoppingHistory po = new YsShoppingHistory();
        po.setId(aggregate.getShoppingHistory().getId());
        po.setUserId(aggregate.getShoppingHistory().getUserId());
        po.setGoodsId(aggregate.getShoppingHistory().getGoodsId());
        return ysShoppingHistoryDao.insert(po);
    }
}
