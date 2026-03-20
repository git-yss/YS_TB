package org.ys.transaction.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.ys.commens.dao.YsOrderDao;
import org.ys.commens.dao.YsProductReviewDao;
import org.ys.commens.dao.YsUserDao;
import org.ys.commens.entity.YsOrder;
import org.ys.commens.entity.YsProductReview;
import org.ys.commens.entity.YsUser;
import org.ys.commens.enums.OrderStatusEnum;
import org.ys.commens.pojo.CommentResult;
import org.ys.transaction.service.ProductReviewService;
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
    public CommentResult addReview(Long orderId, Long goodsId, Long userId, Integer rating,
                                   String content, String images, Integer isAnonymous) {
        try {
            // 参数校验
            if (orderId == null || goodsId == null || userId == null || rating == null) {
                return CommentResult.error("参数不完整");
            }
            if (rating < 1 || rating > 5) {
                return CommentResult.error("评分必须在1-5星之间");
            }

            // 检查用户是否已评价过该订单商品
            YsProductReview existingReview = productReviewDao.checkReviewed(orderId, userId, goodsId);
            if (existingReview != null) {
                return CommentResult.error("您已经评价过该商品");
            }

            // 检查订单是否存在且已支付
            YsOrder order = orderDao.selectById(orderId);
            if (order == null) {
                return CommentResult.error("订单不存在");
            }
            if (!String.valueOf(OrderStatusEnum.PAID.getCode()).equals(order.getStatus())) {
                return CommentResult.error("只能评价已支付的订单");
            }

            // 获取用户信息
            YsUser user = userDao.selectById(userId);
            if (user == null) {
                return CommentResult.error("用户不存在");
            }

            // 创建评价记录
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
            return CommentResult.success("评价成功");
        } catch (Exception e) {
            log.error("添加商品评价失败: {}", e.getMessage(), e);
            return CommentResult.error("添加商品评价失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult getReviewsByGoodsId(Long goodsId, Integer pageNum, Integer pageSize) {
        try {
            if (goodsId == null || goodsId <= 0) {
                return CommentResult.error("商品ID不能为空");
            }

            // 设置默认分页参数
            if (pageNum == null || pageNum < 1) {
                pageNum = 1;
            }
            if (pageSize == null || pageSize < 1) {
                pageSize = 10;
            }

            // 执行分页查询
            Page<YsProductReview> page = new Page<>(pageNum, pageSize);
            IPage<YsProductReview> resultPage = productReviewDao.queryReviewsByGoodsId(page, goodsId);

            // 匿名处理
            for (YsProductReview review : resultPage.getRecords()) {
                if (review.getIsAnonymous() != null && review.getIsAnonymous() == 1) {
                    // 匿名评价隐藏用户名
                    review.setUsername("匿名用户");
                    review.setUserId(null);
                }
            }

            // 构建返回结果
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("list", resultPage.getRecords());
            resultMap.put("total", resultPage.getTotal());
            resultMap.put("pageNum", resultPage.getCurrent());
            resultMap.put("pageSize", resultPage.getSize());
            resultMap.put("pages", resultPage.getPages());

            log.info("获取商品评价列表成功: goodsId={}, total={}", goodsId, resultPage.getTotal());
            return CommentResult.success(resultMap);
        } catch (Exception e) {
            log.error("获取商品评价列表失败: goodsId={}, error={}", goodsId, e.getMessage(), e);
            return CommentResult.error("获取商品评价列表失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult getReviewsByUserId(Long userId, Integer pageNum, Integer pageSize) {
        try {
            if (userId == null || userId <= 0) {
                return CommentResult.error("用户ID不能为空");
            }

            // 设置默认分页参数
            if (pageNum == null || pageNum < 1) {
                pageNum = 1;
            }
            if (pageSize == null || pageSize < 1) {
                pageSize = 10;
            }

            // 执行分页查询
            Page<YsProductReview> page = new Page<>(pageNum, pageSize);
            IPage<YsProductReview> resultPage = productReviewDao.queryReviewsByUserId(page, userId);

            // 构建返回结果
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("list", resultPage.getRecords());
            resultMap.put("total", resultPage.getTotal());
            resultMap.put("pageNum", resultPage.getCurrent());
            resultMap.put("pageSize", resultPage.getSize());
            resultMap.put("pages", resultPage.getPages());

            log.info("获取用户评价列表成功: userId={}, total={}", userId, resultPage.getTotal());
            return CommentResult.success(resultMap);
        } catch (Exception e) {
            log.error("获取用户评价列表失败: userId={}, error={}", userId, e.getMessage(), e);
            return CommentResult.error("获取用户评价列表失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult getReviewStats(Long goodsId) {
        try {
            if (goodsId == null || goodsId <= 0) {
                return CommentResult.error("商品ID不能为空");
            }

            Map<String, Object> stats = productReviewDao.getGoodsRatingStats(goodsId);

            log.info("获取商品评价统计成功: goodsId={}", goodsId);
            return CommentResult.success(stats);
        } catch (Exception e) {
            log.error("获取商品评价统计失败: goodsId={}, error={}", goodsId, e.getMessage(), e);
            return CommentResult.error("获取商品评价统计失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult replyReview(Long reviewId, String replyContent) {
        try {
            if (reviewId == null || reviewId <= 0) {
                return CommentResult.error("评价ID不能为空");
            }
            if (replyContent == null || replyContent.trim().isEmpty()) {
                return CommentResult.error("回复内容不能为空");
            }

            YsProductReview review = productReviewDao.selectById(reviewId);
            if (review == null) {
                return CommentResult.error("评价不存在");
            }
            if (review.getReplyContent() != null && !review.getReplyContent().trim().isEmpty()) {
                return CommentResult.error("该评价已回复");
            }

            // 更新回复内容
            review.setReplyContent(replyContent.trim());
            review.setReplyTime(LocalDateTime.now());
            review.setUpdatedAt(LocalDateTime.now());

            productReviewDao.updateById(review);

            log.info("商家回复评价成功: reviewId={}", reviewId);
            return CommentResult.success("回复成功");
        } catch (Exception e) {
            log.error("商家回复评价失败: reviewId={}, error={}", reviewId, e.getMessage(), e);
            return CommentResult.error("商家回复评价失败: " + e.getMessage());
        }
    }
}
