package org.ys.transaction.domain.respository;

import org.ys.transaction.domain.aggregate.GoodsAggregate;

import java.util.List;

/**
 * (YsGoods)表数据库访问层
 *
 * @author makejava
 * @since 2025-07-16 19:37:18
 */

public interface YsGoodsRespository  {

    int decreaseStock(GoodsAggregate aggregate);

    GoodsAggregate selectGoodById(GoodsAggregate aggregate);

    int increaseStock(GoodsAggregate aggregate);

    List<GoodsAggregate> queryAllGoodsPage(GoodsAggregate aggregate);
}

