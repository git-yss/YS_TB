package org.ys.transaction.service;

import org.ys.commens.pojo.CommentResult;

import java.util.Map;

/**
 * 订单详情服务接口
 *
 * @author makejava
 * @since 2025-07-16
 */
public interface OrderDetailService {

    /**
     * 获取订单详情（包含订单商品列表）
     * @param orderId 订单ID
     * @return 订单详情
     */
    CommentResult getOrderDetail(Long orderId);

    /**
     * 获取用户订单列表（分页）
     * @param userId 用户ID
     * @param status 订单状态（可选）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 订单列表
     */
    CommentResult getUserOrders(Long userId, String status, Integer pageNum, Integer pageSize);

    /**
     * 申请退款
     * @param params 退款参数（订单ID、商品ID、退款原因、退款金额）
     * @return 退款结果
     */
    CommentResult applyRefund(Map<String, Object> params);

    /**
     * 取消退款申请
     * @param orderId 订单ID
     * @return 取消结果
     */
    CommentResult cancelRefund(Long orderId);

    /**
     * 确认收货
     * @param orderId 订单ID
     * @return 确认结果
     */
    CommentResult confirmReceipt(Long orderId);

    /**
     * 取消订单
     * @param orderId 订单ID
     * @param reason 取消原因
     * @return 取消结果
     */
    CommentResult cancelOrder(Long orderId, String reason);

    /**
     * 查询物流信息
     * @param orderId 订单ID
     * @return 物流信息
     */
    CommentResult getLogisticsInfo(Long orderId);

    /**
     * 重新购买（将订单商品加入购物车）
     * @param userId 用户ID
     * @param orderId 订单ID
     * @return 购买结果
     */
    CommentResult reorder(Long userId, Long orderId);

    /**
     * 删除订单（只能删除已完成或已取消的订单）
     * @param userId 用户ID
     * @param orderId 订单ID
     * @return 删除结果
     */
    CommentResult deleteOrder(Long userId, Long orderId);
}
