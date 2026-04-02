package org.ys.transaction.domain.service;

/**
 * 订单命令服务（跨聚合协作）
 * <p>
 * 应用层只负责编排与事务边界；业务校验/流程控制尽量由聚合与领域服务完成。
 * </p>
 */
public interface OrderCommandService {

    void cancel(Long orderId, Long userId, String reason);

    void confirmReceipt(Long orderId, Long userId);

    void applyRefund(Long orderId, Long userId, String refundReason);

    void handleRefund(Long orderId, Boolean approve, String refundReason);

    void updateLogistics(Long orderId, String logisticsNo, String logisticsCompany);

    void shipOrder(Long orderId, String logisticsNo, String logisticsCompany);
}

