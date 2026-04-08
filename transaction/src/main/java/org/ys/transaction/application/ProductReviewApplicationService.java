package org.ys.transaction.application;

import org.springframework.stereotype.Service;
import org.ys.transaction.application.conver.ProductReviewConver;
import org.ys.transaction.domain.aggregate.OrderAggregate;
import org.ys.transaction.domain.aggregate.ProductReviewAggregate;
import org.ys.transaction.domain.aggregate.UserAggregate;
import org.ys.transaction.domain.entity.YsOrder;
import org.ys.transaction.domain.entity.YsProductReview;
import org.ys.transaction.domain.entity.YsUser;
import org.ys.transaction.domain.respository.YsOrderRespository;
import org.ys.transaction.domain.respository.YsProductReviewRespository;
import org.ys.transaction.domain.respository.YsUserRespository;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductReviewApplicationService {

    @Resource
    private YsProductReviewRespository ysProductReviewRespository;

    @Resource
    private YsUserRespository ysUserRespository;

    @Resource
    private YsOrderRespository ysOrderRespository;

    public void addReview(Long orderId, Long goodsId, Long userId, Integer rating, String content, String images, Integer isAnonymous) {
        ProductReviewAggregate existingReview = ysProductReviewRespository.checkReviewed(
                new ProductReviewAggregate(YsProductReview.rehydrate(null, orderId, goodsId, userId, null, null, null, null, null, null, null, null, null))
        );
        ProductReviewAggregate.checkNotReviewed(existingReview);

        OrderAggregate orderAggregate = ysOrderRespository.selectAggregateById(
                new OrderAggregate(YsOrder.identify(orderId), null, null, null)
        );
        ProductReviewAggregate.checkOrderCanReview(orderAggregate);

        UserAggregate userAggregate = ysUserRespository.selectAggregateById(String.valueOf(userId));
        ProductReviewAggregate.checkUserExists(userAggregate);
        YsUser user = userAggregate.getUser();

        YsProductReview review = YsProductReview.rehydrate(
                null, orderId, goodsId, userId, user.getUsername(), rating, content, images,
                isAnonymous != null ? isAnonymous : 0, LocalDateTime.now(), LocalDateTime.now(), null, null
        );
        ysProductReviewRespository.insert(new ProductReviewAggregate(review));
    }

    public Map<String, Object> getReviewsByGoodsId(Long goodsId, Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum < 1) pageNum = 1;
        if (pageSize == null || pageSize < 1) pageSize = 10;

        List<ProductReviewAggregate> reviews = ysProductReviewRespository.queryReviewsByGoodsId(
                new ProductReviewAggregate(YsProductReview.rehydrate(null, null, goodsId, null, null, null, null, null, null, null, null, null, null))
        );
        List<org.ys.transaction.Interface.VO.ProductReviewVO> list = reviews.stream()
                .map(a -> ProductReviewConver.INSTANCE.toReviewVO(a.getReview()))
                .collect(Collectors.toList());

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("list", list);
        resultMap.put("total", list.size());
        resultMap.put("pageNum", pageNum.longValue());
        resultMap.put("pageSize", pageSize.longValue());
        resultMap.put("pages", 1L);
        return resultMap;
    }

    public Map<String, Object> getReviewsByUserId(Long userId, Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum < 1) pageNum = 1;
        if (pageSize == null || pageSize < 1) pageSize = 10;

        List<ProductReviewAggregate> reviews = ysProductReviewRespository.queryReviewsByUserId(
                new ProductReviewAggregate(YsProductReview.rehydrate(null, null, null, userId, null, null, null, null, null, null, null, null, null))
        );
        List<org.ys.transaction.Interface.VO.ProductReviewVO> list = reviews.stream()
                .map(a -> ProductReviewConver.INSTANCE.toReviewVO(a.getReview()))
                .collect(Collectors.toList());

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("list", list);
        resultMap.put("total", list.size());
        resultMap.put("pageNum", pageNum.longValue());
        resultMap.put("pageSize", pageSize.longValue());
        resultMap.put("pages", 1L);
        return resultMap;
    }

    public Map<String, Object> getReviewStats(Long goodsId) {
        return ysProductReviewRespository.getGoodsRatingStatsMap(
                new ProductReviewAggregate(YsProductReview.rehydrate(null, null, goodsId, null, null, null, null, null, null, null, null, null, null))
        );
    }

    public void replyReview(Long reviewId, String replyContent) {
        ProductReviewAggregate reviewAggregate = ysProductReviewRespository.selectById(
                new ProductReviewAggregate(YsProductReview.rehydrate(reviewId, null, null, null, null, null, null, null, null, null, null, null, null))
        );
        ProductReviewAggregate.checkReviewCanReply(reviewAggregate);
        YsProductReview review = reviewAggregate.getReview();

        YsProductReview updated = YsProductReview.rehydrate(
                review.getId(), review.getOrderId(), review.getGoodsId(), review.getUserId(), review.getUsername(),
                review.getRating(), review.getContent(), review.getImages(), review.getIsAnonymous(), review.getCreatedAt(),
                LocalDateTime.now(), replyContent.trim(), LocalDateTime.now()
        );
        ysProductReviewRespository.updateById(new ProductReviewAggregate(updated));
    }
}

