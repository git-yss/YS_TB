package org.ys.transaction.service;

import org.ys.commens.pojo.CommentResult;

import java.util.Map;

/**
 * 订单增强服务接口
 *
 * @author system
 * @since 2025-03-14
 */
public interface OrderEnhancedService {

    /**
     * 获取订单详情（包含商品信息、物流信息等）
     * @param orderId 订单ID
     * @return 订单详情
     */
    CommentResult getOrderDetail(Long orderId);

    /**
     * 用户申请退款
     * @param orderId 订单ID
     * @param userId 用户ID
     * @param refundReason 退款原因
     * @return 退款申请结果
     */
    CommentResult applyRefund(Long orderId, Long userId, String refundReason);

    /**
     * 商家处理退款申请
     * @param orderId 订单ID
     * @param approve 是否同意退款（true=同意，false=拒绝）
     * @param refundReason 退款说明
     * @return 处理结果
     */
    CommentResult handleRefund(Long orderId, Boolean approve, String refundReason);

    /**
     * 更新物流信息
     * @param orderId 订单ID
     * @param logisticsNo 物流单号
     * @param logisticsCompany 物流公司
     * @return 更新结果
     */
    CommentResult updateLogistics(Long orderId, String logisticsNo, String logisticsCompany);

    /**
     * 获取物流追踪信息（模拟）
     * @param orderId 订单ID
     * @return 物流追踪信息
     */
    CommentResult trackLogistics(Long orderId);

    /**
     * 确认收货
     * @param orderId 订单ID
     * @param userId 用户ID
     * @return 确认收货结果
     */
    CommentResult confirmReceipt(Long orderId, Long userId);

    /**
     * 取消订单
     * @param orderId 订单ID
     * @param userId 用户ID
     * @param cancelReason 取消原因
     * @return 取消订单结果
     */
    CommentResult cancelOrder(Long orderId, Long userId, String cancelReason);

    /**
     * 商家发货
     * @param orderId 订单ID
     * @param logisticsNo 物流单号
     * @param logisticsCompany 物流公司
     * @return 发货结果
     */
    CommentResult shipOrder(Long orderId, String logisticsNo, String logisticsCompany);
}
