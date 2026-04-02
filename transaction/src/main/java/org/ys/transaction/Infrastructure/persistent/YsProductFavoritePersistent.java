package org.ys.transaction.Infrastructure.persistent;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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

    @Override
    public int insert(ProductFavoriteAggregate aggregate) {
        return ysProductFavoriteDao.insert(toPo(aggregate.getFavorite()));
    }

    @Override
    public int deleteById(ProductFavoriteAggregate aggregate) {
        return ysProductFavoriteDao.deleteById(aggregate.getFavorite().getId());
    }

    @Override
    public int deleteByUserAndGoods(ProductFavoriteAggregate aggregate) {
        QueryWrapper<YsProductFavorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", aggregate.getFavorite().getUserId())
                .eq("goods_id", aggregate.getFavorite().getGoodsId());
        return ysProductFavoriteDao.delete(queryWrapper);
    }

    private ProductFavoriteAggregate toAggregate(YsProductFavorite po) {
        return new ProductFavoriteAggregate(
                org.ys.transaction.domain.entity.YsProductFavorite.rehydrate(
                        po.getId(), po.getUserId(), po.getGoodsId(), po.getCreatedAt()));
    }

    private YsProductFavorite toPo(org.ys.transaction.domain.entity.YsProductFavorite favorite) {
        YsProductFavorite po = new YsProductFavorite();
        po.setId(favorite.getId());
        po.setUserId(favorite.getUserId());
        po.setGoodsId(favorite.getGoodsId());
        po.setCreatedAt(favorite.getCreatedAt());
        return po;
    }
}
