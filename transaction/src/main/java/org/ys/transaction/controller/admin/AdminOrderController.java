package org.ys.transaction.controller.admin;

import org.springframework.web.bind.annotation.*;
import org.ys.commens.entity.YsOrder;
import org.ys.commens.pojo.CommentResult;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 订单管理控制器
 */
@RestController
@RequestMapping("/admin/order")
public class AdminOrderController {

    @Resource
    private org.ys.transaction.service.admin.AdminOrderService adminOrderService;

    /**
     * 获取订单列表（分页）
     */
    @PostMapping("/list")
    public CommentResult getOrderList(@RequestBody Map<String, Object> params) {
        String status = params.get("status") != null ? params.get("status").toString() : null;
        Long userId = params.get("userId") != null ? Long.parseLong(params.get("userId").toString()) : null;
        String keyword = params.get("keyword") != null ? params.get("keyword").toString() : null;
        Integer pageNum = params.get("pageNum") != null ? Integer.parseInt(params.get("pageNum").toString()) : 1;
        Integer pageSize = params.get("pageSize") != null ? Integer.parseInt(params.get("pageSize").toString()) : 10;
        return adminOrderService.getOrderList(status, userId, keyword, pageNum, pageSize);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/detail/{id}")
    public CommentResult getOrderDetail(@PathVariable Long id) {
        return adminOrderService.getOrderDetail(id);
    }

    /**
     * 发货
     */
    @PostMapping("/ship")
    public CommentResult shipOrder(@RequestBody Map<String, Object> params) {
        Long orderId = Long.parseLong(params.get("orderId").toString());
        String logisticsCompany = params.get("logisticsCompany") != null ? params.get("logisticsCompany").toString() : null;
        String logisticsNo = params.get("logisticsNo") != null ? params.get("logisticsNo").toString() : null;
        return adminOrderService.shipOrder(orderId, logisticsCompany, logisticsNo);
    }

    /**
     * 批量发货
     */
    @PostMapping("/batchShip")
    public CommentResult batchShipOrder(@RequestBody List<Map<String, Object>> orderList) {
        return adminOrderService.batchShipOrder(orderList);
    }

    /**
     * 确认退款
     */
    @PostMapping("/refund")
    public CommentResult refundOrder(@RequestBody Map<String, Object> params) {
        Long orderId = Long.parseLong(params.get("orderId").toString());
        String refundReason = params.get("refundReason") != null ? params.get("refundReason").toString() : null;
        return adminOrderService.refundOrder(orderId, refundReason);
    }

    /**
     * 取消订单
     */
    @PostMapping("/cancel/{id}")
    public CommentResult cancelOrder(@PathVariable Long id) {
        return adminOrderService.cancelOrder(id);
    }

    /**
     * 获取订单统计数据
     */
    @GetMapping("/statistics")
    public CommentResult getOrderStatistics() {
        return adminOrderService.getOrderStatistics();
    }

    /**
     * 导出订单
     */
    @GetMapping("/export")
    public CommentResult exportOrders(@RequestParam(required = false) String status) {
        return adminOrderService.exportOrders(status);
    }

    /**
     * 获取退款申请列表
     */
    @PostMapping("/refundList")
    public CommentResult getRefundList(@RequestBody Map<String, Object> params) {
        Integer pageNum = params.get("pageNum") != null ? Integer.parseInt(params.get("pageNum").toString()) : 1;
        Integer pageSize = params.get("pageSize") != null ? Integer.parseInt(params.get("pageSize").toString()) : 10;
        return adminOrderService.getRefundList(pageNum, pageSize);
    }
}
