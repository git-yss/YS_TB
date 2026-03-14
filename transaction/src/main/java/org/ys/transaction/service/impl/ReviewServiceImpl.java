package org.ys.transaction.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ys.commens.dao.YsGoodsDao;
import org.ys.commens.dao.YsOrderDao;
import org.ys.commens.dao.YsProductReviewDao;
import org.ys.commens.entity.YsGoods;
import org.ys.commens.entity.YsOrder;
import org.ys.commens.entity.YsProductReview;
import org.ys.commens.pojo.CommentResult;
import org.ys.transaction.service.ReviewService;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品评价服务实现类
 *
 * @author makejava
 * @since 2025-07-16
 */
@Service
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReviewServiceImpl.class);

    @Resource
    private YsProductReviewDao reviewDao;

    @Resource
    private YsOrderDao orderDao;

    @Resource
    private YsGoodsDao goodsDao;

    @Override
    public CommentResult addReview(Map<String, Object> params) {
        try {
            Long orderId = Long.valueOf(params.get("orderId").toString());
            Long goodsId = Long.valueOf(params.get("goodsId").toString());
            Long userId = Long.valueOf(params.get("userId").toString());
            Integer rating = Integer.valueOf(params.get("rating").toString());
            String content = (String) params.get("content");
            String images = (String) params.get("images");
            Integer isAnonymous = params.get("isAnonymous") != null ?
                Integer.valueOf(params.get("isAnonymous").toString()) : 0;

            // 参数校验
            if (rating < 1 || rating > 5) {
                return CommentResult.error("评分必须在1-5之间");
            }

            // 检查订单是否存在
            YsOrder order = orderDao.selectById(orderId);
            if (order == null) {
                return CommentResult.error("订单不存在");
            }

            // 检查订单状态（必须是已完成）
            if (!order.getStatus().equals("4")) { // 假设4是已完成状态
                return CommentResult.error("只能对已完成的订单进行评价");
            }

            // 检查是否已经评价过
            QueryWrapper<YsProductReview> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("order_id", orderId).eq("goods_id", goodsId).eq("user_id", userId);
            YsProductReview existingReview = reviewDao.selectOne(queryWrapper);
            if (existingReview != null) {
                return CommentResult.error("您已经评价过该商品");
            }

            // 查询用户信息
            org.ys.commens.dao.YsUserDao userDao = ...; // 需要注入
            YsUser user = userDao.selectById(userId);
            if (user == null) {
                return CommentResult.error("用户不存在");
            }

            // 创建评价
            YsProductReview review = new YsProductReview();
            review.setOrderId(orderId);
            review.setGoodsId(goodsId);
            review.setUserId(userId);
            review.setUsername(isAnonymous == 1 ? "匿名用户" : user.getUsername());
            review.setRating(rating);
            review.setContent(content);
            review.setImages(images);
            review.setIsAnonymous(isAnonymous);

            // 保存评价
            int result = reviewDao.insert(review);
            if (result > 0) {
                log.info("用户评价成功: orderId={}, goodsId={}, userId={}, rating={}", 
                    orderId, goodsId, userId, rating);
                return CommentResult.ok("评价成功");
            } else {
                return CommentResult.error("评价失败");
            }
        } catch (Exception e) {
            log.error("添加评价失败: {}", e.getMessage(), e);
            return CommentResult.error("添加评价失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult getReviewsByGoodsId(Long goodsId, Integer pageNum, Integer pageSize) {
        try {
            // 分页参数
            if (pageNum == null || pageNum < 1) {
                pageNum = 1;
            }
            if (pageSize == null || pageSize < 1) {
                pageSize = 20;
            }

            Page<YsProductReview> page = new Page<>(pageNum, pageSize);

            // 分页查询评价
            IPage<YsProductReview> reviewPage = reviewDao.selectByGoodsId(page, goodsId);

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("list", reviewPage.getRecords());
            result.put("total", reviewPage.getTotal());
            result.put("pageNum", reviewPage.getCurrent());
            result.put("pageSize", reviewPage.getSize());
            result.put("pages", reviewPage.getPages());

            return CommentResult.ok(result);
        } catch (Exception e) {
            log.error("获取商品评价失败: goodsId={}, error={}", goodsId, e.getMessage(), e);
            return CommentResult.error("获取评价失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult getReviewsByUserId(Long userId, Integer pageNum, Integer pageSize) {
        try {
            // 分页参数
            if (pageNum == null || pageNum < 1) {
                pageNum = 1;
            }
            if (pageSize == null || pageSize < 1) {
                pageSize = 20;
            }

            Page<YsProductReview> page = new Page<>(pageNum, pageSize);
            QueryWrapper<YsProductReview> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            queryWrapper.orderByDesc("created_at");

            // 分页查询评价
            IPage<YsProductReview> reviewPage = reviewDao.selectPage(page, queryWrapper);

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("list", reviewPage.getRecords());
            result.put("total", reviewPage.getTotal());
            result.put("pageNum", reviewPage.getCurrent());
            result.put("pageSize", reviewPage.getSize());
            result.put("pages", reviewPage.getPages());

            return CommentResult.ok(result);
        } catch (Exception e) {
            log.error("获取用户评价失败: userId={}, error={}", userId, e.getMessage(), e);
            return CommentResult.error("获取评价失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult replyReview(Long reviewId, String replyContent) {
        try {
            // 参数校验
            if (replyContent == null || replyContent.trim().isEmpty()) {
                return CommentResult.error("回复内容不能为空");
            }

            // 查询评价
            YsProductReview review = reviewDao.selectById(reviewId);
            if (review == null) {
                return CommentResult.error("评价不存在");
            }

            // 检查是否已经回复过
            if (review.getReplyContent() != null && !review.getReplyContent().isEmpty()) {
                return CommentResult.error("已经回复过该评价");
            }

            // 更新回复
            review.setReplyContent(replyContent);
            review.setReplyTime(new java.util.Date());
            int result = reviewDao.updateById(review);

            if (result > 0) {
                log.info("商家回复评价成功: reviewId={}", reviewId);
                return CommentResult.ok("回复成功");
            } else {
                return CommentResult.error("回复失败");
            }
        } catch (Exception e) {
            log.error("回复评价失败: reviewId={}, error={}", reviewId, e.getMessage(), e);
            return CommentResult.error("回复失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult getReviewStats(Long goodsId) {
        try {
            // 查询评价总数
            Integer totalCount = reviewDao.countByGoodsId(goodsId);

            // 查询平均评分
            Double avgRating = reviewDao.avgRatingByGoodsId(goodsId);

            // 查询各评分数量
            Map<String, Object> stats = new HashMap<>();
            stats.put("total", totalCount);
            stats.put("avgRating", avgRating != null ? String.format("%.1f", avgRating) : "0.0");

            // 返回统计信息
            return CommentResult.ok(stats);
        } catch (Exception e) {
            log.error("获取评价统计失败: goodsId={}, error={}", goodsId, e.getMessage(), e);
            return CommentResult.error("获取统计失败: " + e.getMessage());
        }
    }
}
