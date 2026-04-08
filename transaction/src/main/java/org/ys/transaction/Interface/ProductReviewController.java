package org.ys.transaction.Interface;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.ys.transaction.Interface.VO.CommentResult;
import org.ys.transaction.application.ProductReviewApplicationService;

import jakarta.annotation.Resource;
import java.util.Map;

/**
 * 商品评价控制器
 *
 * @author system
 * @since 2025-03-14
 */
@RequestMapping("productReview")
@RestController
public class ProductReviewController {

    @Resource
    private ProductReviewApplicationService productReviewApplicationService;

    /**
     * 发表商品评价
     * @param map 包含orderId, goodsId, userId, rating, content, images, isAnonymous
     * @return 评价结果
     */
    @RequestMapping("add")
    @ResponseBody
    public CommentResult addReview(@RequestBody Map<String, Object> map) {
        try {
            if (map.get("orderId") == null || map.get("goodsId") == null || map.get("userId") == null || map.get("rating") == null) {
                throw new IllegalArgumentException("参数不完整");
            }
            Long orderId = Long.valueOf(map.get("orderId").toString());
            Long goodsId = Long.valueOf(map.get("goodsId").toString());
            Long userId = Long.valueOf(map.get("userId").toString());
            Integer rating = Integer.valueOf(map.get("rating").toString());
            if (rating < 1 || rating > 5) {
                throw new IllegalArgumentException("评分必须在1-5星之间");
            }
            String content = map.get("content") != null ? map.get("content").toString() : null;
            String images = map.get("images") != null ? map.get("images").toString() : null;
            Integer isAnonymous = map.get("isAnonymous") != null ? Integer.valueOf(map.get("isAnonymous").toString()) : null;
            productReviewApplicationService.addReview(orderId, goodsId, userId, rating, content, images, isAnonymous);
            return CommentResult.success("评价成功");
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }

    /**
     * 获取商品评价列表（分页）
     * @param map 包含goodsId, pageNum, pageSize
     * @return 评价列表
     */
    @RequestMapping("listByGoods")
    @ResponseBody
    public CommentResult getReviewsByGoodsId(@RequestBody Map<String, Object> map) {
        try {
            if (map.get("goodsId") == null) {
                throw new IllegalArgumentException("商品ID不能为空");
            }
            Long goodsId = Long.valueOf(map.get("goodsId").toString());
            if (goodsId <= 0) {
                throw new IllegalArgumentException("商品ID不能为空");
            }
            Integer pageNum = map.get("pageNum") != null ? Integer.valueOf(map.get("pageNum").toString()) : null;
            Integer pageSize = map.get("pageSize") != null ? Integer.valueOf(map.get("pageSize").toString()) : null;
            return CommentResult.success(productReviewApplicationService.getReviewsByGoodsId(goodsId, pageNum, pageSize));
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }

    /**
     * 获取用户评价列表（分页）
     * @param map 包含userId, pageNum, pageSize
     * @return 评价列表
     */
    @RequestMapping("listByUser")
    @ResponseBody
    public CommentResult getReviewsByUserId(@RequestBody Map<String, Object> map) {
        try {
            if (map.get("userId") == null) {
                throw new IllegalArgumentException("用户ID不能为空");
            }
            Long userId = Long.valueOf(map.get("userId").toString());
            if (userId <= 0) {
                throw new IllegalArgumentException("用户ID不能为空");
            }
            Integer pageNum = map.get("pageNum") != null ? Integer.valueOf(map.get("pageNum").toString()) : null;
            Integer pageSize = map.get("pageSize") != null ? Integer.valueOf(map.get("pageSize").toString()) : null;
            return CommentResult.success(productReviewApplicationService.getReviewsByUserId(userId, pageNum, pageSize));
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }

    /**
     * 获取商品评价统计信息
     * @param map 包含goodsId
     * @return 评价统计
     */
    @RequestMapping("stats")
    @ResponseBody
    public CommentResult getReviewStats(@RequestBody Map<String, Object> map) {
        try {
            if (map.get("goodsId") == null) {
                throw new IllegalArgumentException("商品ID不能为空");
            }
            Long goodsId = Long.valueOf(map.get("goodsId").toString());
            if (goodsId <= 0) {
                throw new IllegalArgumentException("商品ID不能为空");
            }
            return CommentResult.success(productReviewApplicationService.getReviewStats(goodsId));
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }

    /**
     * 商家回复评价
     * @param map 包含reviewId, replyContent
     * @return 回复结果
     */
    @RequestMapping("reply")
    @ResponseBody
    public CommentResult replyReview(@RequestBody Map<String, Object> map) {
        try {
            if (map.get("reviewId") == null) {
                throw new IllegalArgumentException("评价ID不能为空");
            }
            Long reviewId = Long.valueOf(map.get("reviewId").toString());
            if (reviewId <= 0) {
                throw new IllegalArgumentException("评价ID不能为空");
            }
            String replyContent = map.get("replyContent") == null ? "" : map.get("replyContent").toString();
            if (replyContent.trim().isEmpty()) {
                throw new IllegalArgumentException("回复内容不能为空");
            }
            productReviewApplicationService.replyReview(reviewId, replyContent);
            return CommentResult.success("回复成功");
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }
}

