package org.ys.transaction.Infrastructure.persistent;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.ys.transaction.domain.aggregate.ProductReviewAggregate;
import org.ys.transaction.domain.respository.YsProductReviewRespository;
import org.ys.transaction.Infrastructure.dao.YsProductReviewDao;
import org.ys.transaction.Infrastructure.pojo.YsProductReview;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class YsProductReviewPersistent implements YsProductReviewRespository {
    private final YsProductReviewDao ysProductReviewDao;

    @Override
    public List<ProductReviewAggregate> queryReviewsByGoodsId(ProductReviewAggregate aggregate) {
        Long goodsId = aggregate.getReview().getGoodsId();
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<YsProductReview> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10);
        return ysProductReviewDao.queryReviewsByGoodsId(page, goodsId).getRecords()
                .stream().map(this::toAggregate).collect(Collectors.toList());
    }

    @Override
    public List<ProductReviewAggregate> queryReviewsByUserId(ProductReviewAggregate aggregate) {
        Long userId = aggregate.getReview().getUserId();
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<YsProductReview> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10);
        return ysProductReviewDao.queryReviewsByUserId(page, userId).getRecords()
                .stream().map(this::toAggregate).collect(Collectors.toList());
    }

    @Override
    public ProductReviewAggregate getGoodsRatingStats(ProductReviewAggregate aggregate) {
        ysProductReviewDao.getGoodsRatingStats(aggregate.getReview().getGoodsId());
        return aggregate;
    }

    @Override
    public ProductReviewAggregate checkReviewed(ProductReviewAggregate aggregate) {
        YsProductReview review = ysProductReviewDao.checkReviewed(
                aggregate.getReview().getOrderId(),
                aggregate.getReview().getUserId(),
                aggregate.getReview().getGoodsId());
        return review == null ? null : toAggregate(review);
    }

    @Override
    public int insert(ProductReviewAggregate aggregate) {
        return ysProductReviewDao.insert(toPo(aggregate.getReview()));
    }

    @Override
    public ProductReviewAggregate selectById(ProductReviewAggregate aggregate) {
        YsProductReview review = ysProductReviewDao.selectById(aggregate.getReview().getId());
        return review == null ? null : toAggregate(review);
    }

    @Override
    public int updateById(ProductReviewAggregate aggregate) {
        return ysProductReviewDao.updateById(toPo(aggregate.getReview()));
    }

    @Override
    public Map<String, Object> getGoodsRatingStatsMap(ProductReviewAggregate aggregate) {
        return ysProductReviewDao.getGoodsRatingStats(aggregate.getReview().getGoodsId());
    }

    private ProductReviewAggregate toAggregate(YsProductReview po) {
        return new ProductReviewAggregate(
                org.ys.transaction.domain.entity.YsProductReview.rehydrate(
                        po.getId(), po.getOrderId(), po.getGoodsId(), po.getUserId(), po.getUsername(),
                        po.getRating(), po.getContent(), po.getImages(), po.getIsAnonymous(), po.getCreatedAt(),
                        po.getUpdatedAt(), po.getReplyContent(), po.getReplyTime()
                )
        );
    }

    private YsProductReview toPo(org.ys.transaction.domain.entity.YsProductReview e) {
        YsProductReview po = new YsProductReview();
        po.setId(e.getId());
        po.setOrderId(e.getOrderId());
        po.setGoodsId(e.getGoodsId());
        po.setUserId(e.getUserId());
        po.setUsername(e.getUsername());
        po.setRating(e.getRating());
        po.setContent(e.getContent());
        po.setImages(e.getImages());
        po.setIsAnonymous(e.getIsAnonymous());
        po.setCreatedAt(e.getCreatedAt());
        po.setUpdatedAt(e.getUpdatedAt());
        po.setReplyContent(e.getReplyContent());
        po.setReplyTime(e.getReplyTime());
        return po;
    }
}
