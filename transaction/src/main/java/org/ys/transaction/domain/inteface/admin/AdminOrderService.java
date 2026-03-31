package org.ys.transaction.domain.inteface.admin;

import org.ys.transaction.domain.vo.DomainResult;

import java.util.List;
import java.util.Map;

/**
 * 后台订单管理服务接口
 */
public interface AdminOrderService {

    /**
     * 获取订单列表（分页）
     */
    DomainResult getOrderList(String status, Long userId, String keyword, Integer pageNum, Integer pageSize);

    /**
     * 获取订单详情
     */
    DomainResult getOrderDetail(Long id);

    /**
     * 发货
     */
    DomainResult shipOrder(Long orderId, String logisticsCompany, String logisticsNo);

    /**
     * 批量发货
     */
    DomainResult batchShipOrder(List<Map<String, Object>> orderList);

    /**
     * 确认退款
     */
    DomainResult refundOrder(Long orderId, String refundReason);

    /**
     * 取消订单
     */
    DomainResult cancelOrder(Long id);

    /**
     * 获取订单统计数据
     */
    DomainResult getOrderStatistics();

    /**
     * 导出订单
     */
    DomainResult exportOrders(String status);

    /**
     * 获取退款申请列表
     */
    DomainResult getRefundList(Integer pageNum, Integer pageSize);
}
