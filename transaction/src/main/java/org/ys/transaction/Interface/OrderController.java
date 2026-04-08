package org.ys.transaction.Interface;

import org.springframework.web.bind.annotation.*;
import org.ys.transaction.application.OrderApplicationService;
import org.ys.transaction.Interface.VO.CommentResult;

import jakarta.annotation.Resource;
import java.util.Map;

/**
 * 用户端订单接口（配合 Vue 前端）
 */
@RestController
@RequestMapping("order")
public class OrderController {

    @Resource
    private OrderApplicationService orderApplicationService;

    /**
     * 当前用户订单行列表（与 shoppingCar/showOrder 一致，路径更贴近前端路由）
     */
    @PostMapping("list")
    @ResponseBody
    public CommentResult list(@RequestBody Map<String, Object> body) {
        try {
            if (body.get("userId") == null) {
                throw new IllegalArgumentException("用户ID不能为空");
            }
            Long userId = Long.valueOf(body.get("userId").toString());
            if (userId <= 0) {
                throw new IllegalArgumentException("用户ID不能为空");
            }
            return CommentResult.success(orderApplicationService.list(userId));
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }

    @PostMapping("detail")
    @ResponseBody
    public CommentResult detail(@RequestBody Map<String, Object> body) {
        try {
            Long orderId = Long.valueOf(body.get("orderId").toString());
            Long userId = Long.valueOf(body.get("userId").toString());
            if (orderId == null || orderId <= 0) {
                throw new IllegalArgumentException("订单ID不能为空");
            }
            if (userId == null) {
                throw new IllegalArgumentException("用户ID不能为空");
            }
            Map<String, Object> data = orderApplicationService.detail(orderId, userId);
            return CommentResult.success(data);
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }

    @PostMapping("cancel")
    @ResponseBody
    public CommentResult cancel(@RequestBody Map<String, Object> body) {
        try {
            if (body.get("orderId") == null) {
                throw new IllegalArgumentException("订单ID不能为空");
            }
            if (body.get("userId") == null) {
                throw new IllegalArgumentException("用户ID不能为空");
            }
            Long orderId = Long.valueOf(body.get("orderId").toString());
            Long userId = Long.valueOf(body.get("userId").toString());
            if (orderId <= 0) {
                throw new IllegalArgumentException("订单ID不能为空");
            }
            if (userId <= 0) {
                throw new IllegalArgumentException("用户ID不能为空");
            }
            String reason = body.get("cancelReason") != null ? body.get("cancelReason").toString() : "用户取消";
            orderApplicationService.cancel(orderId, userId, reason);
            return CommentResult.success("订单已取消");
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }

    @PostMapping("confirmReceipt")
    @ResponseBody
    public CommentResult confirmReceipt(@RequestBody Map<String, Object> body) {
        try {
            if (body.get("orderId") == null) {
                throw new IllegalArgumentException("订单ID不能为空");
            }
            if (body.get("userId") == null) {
                throw new IllegalArgumentException("用户ID不能为空");
            }
            Long orderId = Long.valueOf(body.get("orderId").toString());
            Long userId = Long.valueOf(body.get("userId").toString());
            if (orderId <= 0) {
                throw new IllegalArgumentException("订单ID不能为空");
            }
            if (userId <= 0) {
                throw new IllegalArgumentException("用户ID不能为空");
            }
            orderApplicationService.confirmReceipt(orderId, userId);
            return CommentResult.success("确认收货成功");
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }
}
