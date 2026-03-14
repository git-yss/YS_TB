package org.ys.transaction.service.admin;

import org.ys.commens.pojo.CommentResult;

import java.util.List;
import java.util.Map;

/**
 * 后台订单管理服务接口
 */
public interface AdminOrderService {

    /**
     * 获取订单列表（分页）
     */
    CommentResult getOrderList(String status, Long userId, String keyword, Integer pageNum, Integer pageSize);

    /**
     * 获取订单详情
     */
    CommentResult getOrderDetail(Long id);

    /**
     * 发货
     */
    CommentResult shipOrder(Long orderId, String logisticsCompany, String logisticsNo);

    /**
     * 批量发货
     */
    CommentResult batchShipOrder(List<Map<String, Object>> orderList);

    /**
     * 确认退款
     */
    CommentResult refundOrder(Long orderId, String refundReason);

    /**
     * 取消订单
     */
    CommentResult cancelOrder(Long id);

    /**
     * 获取订单统计数据
     */
    CommentResult getOrderStatistics();

    /**
     * 导出订单
     */
    CommentResult exportOrders(String status);

    /**
     * 获取退款申请列表
     */
    CommentResult getRefundList(Integer pageNum, Integer pageSize);
}
