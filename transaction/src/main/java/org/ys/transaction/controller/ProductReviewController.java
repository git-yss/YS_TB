package org.ys.transaction.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.ys.commens.pojo.CommentResult;
import org.ys.transaction.service.ProductReviewService;

import javax.annotation.Resource;
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
    private ProductReviewService productReviewService;

    /**
     * 发表商品评价
     * @param map 包含orderId, goodsId, userId, rating, content, images, isAnonymous
     * @return 评价结果
     */
    @RequestMapping("add")
    @ResponseBody
    public CommentResult addReview(@RequestBody Map<String, Object> map) {
        Long orderId = Long.valueOf(map.get("orderId").toString());
        Long goodsId = Long.valueOf(map.get("goodsId").toString());
        Long userId = Long.valueOf(map.get("userId").toString());
        Integer rating = Integer.valueOf(map.get("rating").toString());
        String content = map.get("content") != null ? map.get("content").toString() : null;
        String images = map.get("images") != null ? map.get("images").toString() : null;
        Integer isAnonymous = map.get("isAnonymous") != null ? Integer.valueOf(map.get("isAnonymous").toString()) : null;
        return productReviewService.addReview(orderId, goodsId, userId, rating, content, images, isAnonymous);
    }

    /**
     * 获取商品评价列表（分页）
     * @param map 包含goodsId, pageNum, pageSize
     * @return 评价列表
     */
    @RequestMapping("listByGoods")
    @ResponseBody
    public CommentResult getReviewsByGoodsId(@RequestBody Map<String, Object> map) {
        Long goodsId = Long.valueOf(map.get("goodsId").toString());
        Integer pageNum = map.get("pageNum") != null ? Integer.valueOf(map.get("pageNum").toString()) : null;
        Integer pageSize = map.get("pageSize") != null ? Integer.valueOf(map.get("pageSize").toString()) : null;
        return productReviewService.getReviewsByGoodsId(goodsId, pageNum, pageSize);
    }

    /**
     * 获取用户评价列表（分页）
     * @param map 包含userId, pageNum, pageSize
     * @return 评价列表
     */
    @RequestMapping("listByUser")
    @ResponseBody
    public CommentResult getReviewsByUserId(@RequestBody Map<String, Object> map) {
        Long userId = Long.valueOf(map.get("userId").toString());
        Integer pageNum = map.get("pageNum") != null ? Integer.valueOf(map.get("pageNum").toString()) : null;
        Integer pageSize = map.get("pageSize") != null ? Integer.valueOf(map.get("pageSize").toString()) : null;
        return productReviewService.getReviewsByUserId(userId, pageNum, pageSize);
    }

    /**
     * 获取商品评价统计信息
     * @param map 包含goodsId
     * @return 评价统计
     */
    @RequestMapping("stats")
    @ResponseBody
    public CommentResult getReviewStats(@RequestBody Map<String, Object> map) {
        Long goodsId = Long.valueOf(map.get("goodsId").toString());
        return productReviewService.getReviewStats(goodsId);
    }

    /**
     * 商家回复评价
     * @param map 包含reviewId, replyContent
     * @return 回复结果
     */
    @RequestMapping("reply")
    @ResponseBody
    public CommentResult replyReview(@RequestBody Map<String, Object> map) {
        Long reviewId = Long.valueOf(map.get("reviewId").toString());
        String replyContent = map.get("replyContent").toString();
        return productReviewService.replyReview(reviewId, replyContent);
    }
}
