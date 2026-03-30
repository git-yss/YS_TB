package org.ys.transaction.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.ys.commens.entity.YsOrder;
import org.ys.commens.pojo.CommentResult;
import org.ys.transaction.service.CartService;
import org.ys.transaction.service.OrderEnhancedService;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 用户端订单接口（配合 Vue 前端）
 */
@RestController
@RequestMapping("order")
public class OrderController {

    @Resource
    private CartService cartService;

    @Resource
    private OrderEnhancedService orderEnhancedService;

    /**
     * 当前用户订单行列表（与 shoppingCar/showOrder 一致，路径更贴近前端路由）
     */
    @PostMapping("list")
    @ResponseBody
    public CommentResult list(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        return cartService.showOrder(userId);
    }

    @PostMapping("detail")
    @ResponseBody
    public CommentResult detail(@RequestBody Map<String, Object> body) {
        Long orderId = Long.valueOf(body.get("orderId").toString());
        Long userId = Long.valueOf(body.get("userId").toString());
        CommentResult res = orderEnhancedService.getOrderDetail(orderId);
        if (res.getCode() == null || res.getCode() != 200 || res.getData() == null) {
            return res;
        }
        if (!(res.getData() instanceof Map)) {
            return res;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) res.getData();
        Object ord = data.get("order");
        if (ord instanceof YsOrder) {
            if (!userId.equals(((YsOrder) ord).getUserId())) {
                return CommentResult.error("无权查看该订单");
            }
        }
        return res;
    }

    @PostMapping("cancel")
    @ResponseBody
    public CommentResult cancel(@RequestBody Map<String, Object> body) {
        Long orderId = Long.valueOf(body.get("orderId").toString());
        Long userId = Long.valueOf(body.get("userId").toString());
        String reason = body.get("cancelReason") != null ? body.get("cancelReason").toString() : "用户取消";
        return orderEnhancedService.cancelOrder(orderId, userId, reason);
    }

    @PostMapping("confirmReceipt")
    @ResponseBody
    public CommentResult confirmReceipt(@RequestBody Map<String, Object> body) {
        Long orderId = Long.valueOf(body.get("orderId").toString());
        Long userId = Long.valueOf(body.get("userId").toString());
        return orderEnhancedService.confirmReceipt(orderId, userId);
    }
}
