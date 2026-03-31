package org.ys.transaction.domain.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.ys.transaction.domain.inteface.ProductReviewService;
import org.ys.transaction.domain.enums.OrderStatusEnum;
import org.ys.transaction.Infrastructure.dao.YsOrderDao;
import org.ys.transaction.Infrastructure.dao.YsProductReviewDao;
import org.ys.transaction.Infrastructure.dao.YsUserDao;
import org.ys.transaction.Infrastructure.pojo.YsOrder;
import org.ys.transaction.Infrastructure.pojo.YsProductReview;
import org.ys.transaction.Infrastructure.pojo.YsUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 商品评价服务实现类
 *
 * @author system
 * @since 2025-03-14
 */
@Service
@Transactional
public class ProductReviewServiceImpl implements ProductReviewService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProductReviewServiceImpl.class);

    @Resource
    private YsProductReviewDao productReviewDao;

    @Resource
    private YsUserDao userDao;

    @Resource
    private YsOrderDao orderDao;

    @Override
    public void addReview(Long orderId, Long goodsId, Long userId, Integer rating,
                          String content, String images, Integer isAnonymous) {
        if (orderId == null || goodsId == null || userId == null || rating == null) {
            throw new IllegalArgumentException("参数不完整");
        }
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("评分必须在1-5星之间");
        }

        YsProductReview existingReview = productReviewDao.checkReviewed(orderId, userId, goodsId);
        if (existingReview != null) {
            throw new IllegalStateException("您已经评价过该商品");
        }

        YsOrder order = orderDao.selectById(orderId);
        if (order == null) {
            throw new IllegalStateException("订单不存在");
        }
        if (!String.valueOf(OrderStatusEnum.PAID.getCode()).equals(order.getStatus())) {
            throw new IllegalStateException("只能评价已支付的订单");
        }

        YsUser user = userDao.selectById(userId);
        if (user == null) {
            throw new IllegalStateException("用户不存在");
        }

        YsProductReview review = new YsProductReview();
        review.setOrderId(orderId);
        review.setGoodsId(goodsId);
        review.setUserId(userId);
        review.setUsername(user.getUsername());
        review.setRating(rating);
        review.setContent(content);
        review.setImages(images);
        review.setIsAnonymous(isAnonymous != null ? isAnonymous : 0);
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());

        productReviewDao.insert(review);

        log.info("商品评价添加成功: userId={}, goodsId={}, orderId={}, rating={}",
                userId, goodsId, orderId, rating);
    }

    @Override
    public Map<String, Object> getReviewsByGoodsId(Long goodsId, Integer pageNum, Integer pageSize) {
        if (goodsId == null || goodsId <= 0) {
            throw new IllegalArgumentException("商品ID不能为空");
        }

        if (pageNum == null || pageNum < 1) pageNum = 1;
        if (pageSize == null || pageSize < 1) pageSize = 10;

        Page<YsProductReview> page = new Page<>(pageNum, pageSize);
        IPage<YsProductReview> resultPage = productReviewDao.queryReviewsByGoodsId(page, goodsId);

        for (YsProductReview review : resultPage.getRecords()) {
            if (review.getIsAnonymous() != null && review.getIsAnonymous() == 1) {
                review.setUsername("匿名用户");
                review.setUserId(null);
            }
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("list", resultPage.getRecords());
        resultMap.put("total", resultPage.getTotal());
        resultMap.put("pageNum", resultPage.getCurrent());
        resultMap.put("pageSize", resultPage.getSize());
        resultMap.put("pages", resultPage.getPages());

        log.info("获取商品评价列表成功: goodsId={}, total={}", goodsId, resultPage.getTotal());
        return resultMap;
    }

    @Override
    public Map<String, Object> getReviewsByUserId(Long userId, Integer pageNum, Integer pageSize) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        if (pageNum == null || pageNum < 1) pageNum = 1;
        if (pageSize == null || pageSize < 1) pageSize = 10;

        Page<YsProductReview> page = new Page<>(pageNum, pageSize);
        IPage<YsProductReview> resultPage = productReviewDao.queryReviewsByUserId(page, userId);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("list", resultPage.getRecords());
        resultMap.put("total", resultPage.getTotal());
        resultMap.put("pageNum", resultPage.getCurrent());
        resultMap.put("pageSize", resultPage.getSize());
        resultMap.put("pages", resultPage.getPages());

        log.info("获取用户评价列表成功: userId={}, total={}", userId, resultPage.getTotal());
        return resultMap;
    }

    @Override
    public Map<String, Object> getReviewStats(Long goodsId) {
        if (goodsId == null || goodsId <= 0) {
            throw new IllegalArgumentException("商品ID不能为空");
        }
        Map<String, Object> stats = productReviewDao.getGoodsRatingStats(goodsId);
        log.info("获取商品评价统计成功: goodsId={}", goodsId);
        return stats;
    }

    @Override
    public void replyReview(Long reviewId, String replyContent) {
        if (reviewId == null || reviewId <= 0) {
            throw new IllegalArgumentException("评价ID不能为空");
        }
        if (replyContent == null || replyContent.trim().isEmpty()) {
            throw new IllegalArgumentException("回复内容不能为空");
        }

        YsProductReview review = productReviewDao.selectById(reviewId);
        if (review == null) {
            throw new IllegalStateException("评价不存在");
        }
        if (review.getReplyContent() != null && !review.getReplyContent().trim().isEmpty()) {
            throw new IllegalStateException("该评价已回复");
        }

        review.setReplyContent(replyContent.trim());
        review.setReplyTime(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());

        productReviewDao.updateById(review);
        log.info("商家回复评价成功: reviewId={}", reviewId);
    }
}
