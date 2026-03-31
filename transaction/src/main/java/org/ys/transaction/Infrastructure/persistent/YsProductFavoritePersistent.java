package org.ys.transaction.Infrastructure.persistent;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.ys.transaction.domain.aggregate.ProductFavoriteAggregate;
import org.ys.transaction.domain.respository.YsProductFavoriteRespository;
import org.ys.transaction.Infrastructure.dao.YsProductFavoriteDao;
import org.ys.transaction.Infrastructure.pojo.YsProductFavorite;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class YsProductFavoritePersistent implements YsProductFavoriteRespository {
    private final YsProductFavoriteDao ysProductFavoriteDao;

    @Override
    public List<ProductFavoriteAggregate> selectByUserId(ProductFavoriteAggregate aggregate) {
        Long userId = aggregate.getFavorite().getUserId();
        return ysProductFavoriteDao.selectByUserId(userId).stream()
                .map(this::toAggregate)
                .collect(Collectors.toList());
    }

    @Override
    public ProductFavoriteAggregate selectByUserAndGoods(ProductFavoriteAggregate aggregate) {
        Long userId = aggregate.getFavorite().getUserId();
        Long goodsId = aggregate.getFavorite().getGoodsId();
        YsProductFavorite po = ysProductFavoriteDao.selectByUserAndGoods(userId, goodsId);
        return po == null ? null : toAggregate(po);
    }

    private ProductFavoriteAggregate toAggregate(YsProductFavorite po) {
        return new ProductFavoriteAggregate(
                org.ys.transaction.domain.entity.YsProductFavorite.rehydrate(
                        po.getId(), po.getUserId(), po.getGoodsId(), po.getCreatedAt()));
    }
}
