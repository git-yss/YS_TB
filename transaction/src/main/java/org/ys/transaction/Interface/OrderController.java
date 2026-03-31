package org.ys.transaction.Interface;

import org.springframework.web.bind.annotation.*;
import org.ys.transaction.domain.inteface.CartService;
import org.ys.transaction.domain.inteface.OrderEnhancedService;
import org.ys.transaction.Infrastructure.pojo.YsOrder;
import org.ys.transaction.Interface.VO.CommentResult;

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
        try {
            Long userId = Long.valueOf(body.get("userId").toString());
            return CommentResultAssembler.ok(cartService.showOrder(userId));
        } catch (Exception e) {
            return CommentResultAssembler.fail(e.getMessage());
        }
    }

    @PostMapping("detail")
    @ResponseBody
    public CommentResult detail(@RequestBody Map<String, Object> body) {
        try {
            Long orderId = Long.valueOf(body.get("orderId").toString());
            Long userId = Long.valueOf(body.get("userId").toString());
            Map<String, Object> data = orderEnhancedService.getOrderDetail(orderId);
            Object ord = data.get("order");
            if (ord instanceof YsOrder) {
                if (!userId.equals(((YsOrder) ord).getUserId())) {
                    return CommentResultAssembler.fail("无权查看该订单");
                }
            }
            return CommentResultAssembler.ok(data);
        } catch (Exception e) {
            return CommentResultAssembler.fail(e.getMessage());
        }
    }

    @PostMapping("cancel")
    @ResponseBody
    public CommentResult cancel(@RequestBody Map<String, Object> body) {
        try {
            Long orderId = Long.valueOf(body.get("orderId").toString());
            Long userId = Long.valueOf(body.get("userId").toString());
            String reason = body.get("cancelReason") != null ? body.get("cancelReason").toString() : "用户取消";
            orderEnhancedService.cancelOrder(orderId, userId, reason);
            return CommentResultAssembler.ok("订单已取消");
        } catch (Exception e) {
            return CommentResultAssembler.fail(e.getMessage());
        }
    }

    @PostMapping("confirmReceipt")
    @ResponseBody
    public CommentResult confirmReceipt(@RequestBody Map<String, Object> body) {
        try {
            Long orderId = Long.valueOf(body.get("orderId").toString());
            Long userId = Long.valueOf(body.get("userId").toString());
            orderEnhancedService.confirmReceipt(orderId, userId);
            return CommentResultAssembler.ok("确认收货成功");
        } catch (Exception e) {
            return CommentResultAssembler.fail(e.getMessage());
        }
    }
}
