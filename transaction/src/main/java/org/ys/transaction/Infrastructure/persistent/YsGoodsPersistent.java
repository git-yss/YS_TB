package org.ys.transaction.Infrastructure.persistent;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.ys.transaction.domain.aggregate.GoodsAggregate;
import org.ys.transaction.domain.respository.YsGoodsRespository;
import org.ys.transaction.Infrastructure.dao.YsGoodsDao;
import org.ys.transaction.Infrastructure.pojo.YsGoods;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class YsGoodsPersistent implements YsGoodsRespository {
    private final YsGoodsDao ysGoodsDao;

    @Override
    public int decreaseStock(GoodsAggregate aggregate) {
        long itemId = aggregate.getGoods().getId();
        Integer num = aggregate.getOrder() == null ? null : aggregate.getOrder().getQuantity();
        ysGoodsDao.decreaseStock(itemId, num);
        return 1;
    }

    @Override
    public GoodsAggregate selectGoodById(GoodsAggregate aggregate) {
        long itemId = aggregate.getGoods().getId();
        YsGoods po = ysGoodsDao.selectGoodById(itemId);
        if (po == null) return null;
        return new GoodsAggregate(
                aggregate.getOrder(),
                org.ys.transaction.domain.entity.YsGoods.rehydrate(
                        po.getId(), po.getBrand(), po.getName(), po.getIntroduce(), po.getPrice(),
                        po.getInventory(), po.getImage(), po.getCategory(), po.getCategoryId()),
                aggregate.getUser()
        );
    }

    @Override
    public int increaseStock(GoodsAggregate aggregate) {
        long itemId = aggregate.getGoods().getId();
        Integer num = aggregate.getOrder() == null ? null : aggregate.getOrder().getQuantity();
        ysGoodsDao.increaseStock(itemId, num);
        return 1;
    }

    @Override
    public List<GoodsAggregate> queryAllGoodsPage(GoodsAggregate aggregate) {
        GoodsAggregate.Query query = aggregate.getQuery();
        Map<String, Object> map = query == null ? Collections.emptyMap() : query.getFilters();
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<YsGoods> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                        query == null ? 1 : query.getPageNo(),
                        query == null ? 10 : query.getPageSize());
        List<YsGoods> records = ysGoodsDao.queryAllGoodsPage(page, map).getRecords();
        return records.stream()
                .map(po -> new GoodsAggregate(
                        aggregate.getOrder(),
                        org.ys.transaction.domain.entity.YsGoods.rehydrate(
                                po.getId(), po.getBrand(), po.getName(), po.getIntroduce(), po.getPrice(),
                                po.getInventory(), po.getImage(), po.getCategory(), po.getCategoryId()),
                        aggregate.getUser()))
                .collect(Collectors.toList());
    }
}
