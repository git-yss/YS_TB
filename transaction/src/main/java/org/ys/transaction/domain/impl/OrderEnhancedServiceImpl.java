package org.ys.transaction.domain.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ys.transaction.domain.enums.OrderStatusEnum;
import org.ys.transaction.domain.inteface.OrderEnhancedService;
import org.ys.transaction.Infrastructure.dao.YsGoodsDao;
import org.ys.transaction.Infrastructure.dao.YsOrderDao;
import org.ys.transaction.Infrastructure.dao.YsUserDao;
import org.ys.transaction.Infrastructure.pojo.YsGoods;
import org.ys.transaction.Infrastructure.pojo.YsOrder;
import org.ys.transaction.Infrastructure.pojo.YsUser;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单增强服务实现类
 *
 * @author system
 * @since 2025-03-14
 */
@Service
@Transactional
public class OrderEnhancedServiceImpl implements OrderEnhancedService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OrderEnhancedServiceImpl.class);

    @Resource
    private YsOrderDao orderDao;

    @Resource
    private YsGoodsDao goodsDao;

    @Resource
    private YsUserDao userDao;

    @Override
    public Map<String, Object> getOrderDetail(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("订单ID不能为空");
        }

        List<YsOrder> orders = orderDao.selectsById(orderId);
        if (orders == null || orders.isEmpty()) {
            throw new IllegalStateException("订单不存在");
        }

        YsOrder order = orders.get(0);

        YsGoods goods = goodsDao.selectGoodById(order.getGoodsId());

        YsUser user = userDao.selectById(order.getUserId());

        List<Map<String, Object>> details = orderDao.selectDetailById(orderId);

        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("goods", goods);
        result.put("user", user);
        result.put("details", details);

        result.put("statusDesc", getStatusDesc(order.getStatus()));

        log.info("获取订单详情成功: orderId={}", orderId);
        return result;
    }

    @Override
    public void applyRefund(Long orderId, Long userId, String refundReason) {
        if (orderId == null || userId == null) {
            throw new IllegalArgumentException("订单ID和用户ID不能为空");
        }

            // 获取订单信息
        List<YsOrder> orders = orderDao.selectsById(orderId);
        if (orders == null || orders.isEmpty()) {
            throw new IllegalStateException("订单不存在");
        }

            YsOrder order = orders.get(0);

            // 验证订单所属用户
        if (!order.getUserId().equals(userId)) {
            throw new IllegalStateException("无权操作该订单");
        }

            // 检查订单状态，只有已支付和已发货的订单可以申请退款
            String status = order.getStatus();
        if (!String.valueOf(OrderStatusEnum.PAID.getCode()).equals(status)
                && !String.valueOf(OrderStatusEnum.SHIPPED.getCode()).equals(status)) {
            throw new IllegalStateException("当前订单状态不支持退款");
        }

            // 更新订单状态为退款中
            orderDao.updateStatusById(OrderStatusEnum.REFUNDING.getCode(), orderId);

            // 保存退款信息
            order.setRefundReason(refundReason);
            order.setRefundTime(LocalDateTime.now());
            order.setRefundAmount(order.getTotalAmount() != null ? order.getTotalAmount() :
                    order.getUnitPrice().multiply(BigDecimal.valueOf(order.getQuantity())));
            orderDao.updateById(order);

        log.info("用户申请退款成功: orderId={}, userId={}", orderId, userId);
    }

    @Override
    public void handleRefund(Long orderId, Boolean approve, String refundReason) {
        if (orderId == null || approve == null) {
            throw new IllegalArgumentException("订单ID和处理结果不能为空");
        }

        List<YsOrder> orders = orderDao.selectsById(orderId);
        if (orders == null || orders.isEmpty()) {
            throw new IllegalStateException("订单不存在");
        }

        YsOrder order = orders.get(0);
        if (!String.valueOf(OrderStatusEnum.REFUNDING.getCode()).equals(order.getStatus())) {
            throw new IllegalStateException("当前订单状态不支持退款处理");
        }

        if (approve) {
            orderDao.updateStatusById(OrderStatusEnum.REFUNDED.getCode(), orderId);
            YsUser user = userDao.selectById(order.getUserId());
            BigDecimal refundAmount = order.getRefundAmount();
            user.setBalance(user.getBalance().add(refundAmount));
            userDao.updateBalanceById(user);
            goodsDao.increaseStock(order.getGoodsId(), order.getQuantity());
            log.info("商家同意退款成功: orderId={}, refundAmount={}", orderId, refundAmount);
        } else {
            if (order.getShipTime() != null) {
                orderDao.updateStatusById(OrderStatusEnum.SHIPPED.getCode(), orderId);
            } else {
                orderDao.updateStatusById(OrderStatusEnum.PAID.getCode(), orderId);
            }
            log.info("商家拒绝退款: orderId={}", orderId);
        }
    }

    @Override
    public void updateLogistics(Long orderId, String logisticsNo, String logisticsCompany) {
        if (orderId == null || logisticsNo == null || logisticsNo.trim().isEmpty()) {
            throw new IllegalArgumentException("订单ID和物流单号不能为空");
        }
        List<YsOrder> orders = orderDao.selectsById(orderId);
        if (orders == null || orders.isEmpty()) {
            throw new IllegalStateException("订单不存在");
        }
        YsOrder order = orders.get(0);
        order.setLogisticsNo(logisticsNo.trim());
        order.setLogisticsCompany(logisticsCompany != null ? logisticsCompany.trim() : null);
        orderDao.updateById(order);
        log.info("更新物流信息成功: orderId={}, logisticsNo={}", orderId, logisticsNo);
    }

    @Override
    public Map<String, Object> trackLogistics(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("订单ID不能为空");
        }
        List<YsOrder> orders = orderDao.selectsById(orderId);
        if (orders == null || orders.isEmpty()) {
            throw new IllegalStateException("订单不存在");
        }
        YsOrder order = orders.get(0);

        List<Map<String, Object>> trackingInfo = new ArrayList<>();

        Map<String, Object> step1 = new HashMap<>();
        step1.put("time", order.getAddtime());
        step1.put("status", "订单创建");
        step1.put("description", "您的订单已创建，等待支付");
        trackingInfo.add(step1);

        if (order.getPayTime() != null) {
            Map<String, Object> step2 = new HashMap<>();
            step2.put("time", order.getPayTime());
            step2.put("status", "支付成功");
            step2.put("description", "您已成功支付订单");
            trackingInfo.add(step2);
        }

        if (order.getShipTime() != null) {
            Map<String, Object> step3 = new HashMap<>();
            step3.put("time", order.getShipTime());
            step3.put("status", "商家发货");
            step3.put("description", "商家已发货，物流公司：" + order.getLogisticsCompany() +
                    "，物流单号：" + order.getLogisticsNo());
            trackingInfo.add(step3);

            Map<String, Object> step4 = new HashMap<>();
            step4.put("time", order.getShipTime().plusHours(2));
            step4.put("status", "包裹已揽收");
            step4.put("description", "快递公司已揽收包裹");
            trackingInfo.add(step4);

            Map<String, Object> step5 = new HashMap<>();
            step5.put("time", order.getShipTime().plusHours(12));
            step5.put("status", "包裹运输中");
            step5.put("description", "包裹正在运输途中，请耐心等待");
            trackingInfo.add(step5);
        }

        if (order.getFinishTime() != null) {
            Map<String, Object> step6 = new HashMap<>();
            step6.put("time", order.getFinishTime());
            step6.put("status", "已签收");
            step6.put("description", "您已签收包裹，感谢您的购买");
            trackingInfo.add(step6);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orderId);
        result.put("logisticsNo", order.getLogisticsNo());
        result.put("logisticsCompany", order.getLogisticsCompany());
        result.put("tracking", trackingInfo);

        log.info("获取物流追踪信息成功: orderId={}", orderId);
        return result;
    }

    @Override
    public void confirmReceipt(Long orderId, Long userId) {
        if (orderId == null || userId == null) {
            throw new IllegalArgumentException("订单ID和用户ID不能为空");
        }
        List<YsOrder> orders = orderDao.selectsById(orderId);
        if (orders == null || orders.isEmpty()) {
            throw new IllegalStateException("订单不存在");
        }
        YsOrder order = orders.get(0);
        if (!order.getUserId().equals(userId)) {
            throw new IllegalStateException("无权操作该订单");
        }
        if (!String.valueOf(OrderStatusEnum.SHIPPED.getCode()).equals(order.getStatus())) {
            throw new IllegalStateException("只有已发货的订单可以确认收货");
        }
        orderDao.updateStatusById(OrderStatusEnum.COMPLETED.getCode(), orderId);
        order.setFinishTime(LocalDateTime.now());
        orderDao.updateById(order);
        log.info("用户确认收货成功: orderId={}, userId={}", orderId, userId);
    }

    @Override
    public void cancelOrder(Long orderId, Long userId, String cancelReason) {
        if (orderId == null || userId == null) {
            throw new IllegalArgumentException("订单ID和用户ID不能为空");
        }
        List<YsOrder> orders = orderDao.selectsById(orderId);
        if (orders == null || orders.isEmpty()) {
            throw new IllegalStateException("订单不存在");
        }
        YsOrder order = orders.get(0);
        if (!order.getUserId().equals(userId)) {
            throw new IllegalStateException("无权操作该订单");
        }
        if (!String.valueOf(OrderStatusEnum.PENDING_PAYMENT.getCode()).equals(order.getStatus())) {
            throw new IllegalStateException("当前订单状态不支持取消");
        }
        for (YsOrder o : orders) {
            Integer q = o.getQuantity() != null ? o.getQuantity() : 1;
            if (o.getGoodsId() != null && q > 0) {
                goodsDao.increaseStock(o.getGoodsId(), q);
            }
        }
        orderDao.updateStatusById(OrderStatusEnum.CANCELLED.getCode(), orderId);
        for (YsOrder o : orders) {
            o.setRefundReason(cancelReason);
            orderDao.updateById(o);
        }
        log.info("用户取消订单成功: orderId={}, userId={}", orderId, userId);
    }

    @Override
    public void shipOrder(Long orderId, String logisticsNo, String logisticsCompany) {
        if (orderId == null || logisticsNo == null || logisticsNo.trim().isEmpty()) {
            throw new IllegalArgumentException("订单ID和物流单号不能为空");
        }
        List<YsOrder> orders = orderDao.selectsById(orderId);
        if (orders == null || orders.isEmpty()) {
            throw new IllegalStateException("订单不存在");
        }
        YsOrder order = orders.get(0);
        if (!String.valueOf(OrderStatusEnum.PAID.getCode()).equals(order.getStatus())) {
            throw new IllegalStateException("当前订单状态不支持发货");
        }
        orderDao.updateStatusById(OrderStatusEnum.SHIPPED.getCode(), orderId);
        order.setShipTime(LocalDateTime.now());
        order.setLogisticsNo(logisticsNo.trim());
        order.setLogisticsCompany(logisticsCompany != null ? logisticsCompany.trim() : null);
        orderDao.updateById(order);
        log.info("商家发货成功: orderId={}, logisticsNo={}", orderId, logisticsNo);
    }

    @Override
    public DomainResult handleRefund(Long orderId, Boolean approve, String refundReason) {
        try {
            if (orderId == null || approve == null) {
                return DomainResult.error("订单ID和处理结果不能为空");
            }

            // 获取订单信息
            List<YsOrder> orders = orderDao.selectsById(orderId);
            if (orders == null || orders.isEmpty()) {
                return DomainResult.error("订单不存在");
            }

            YsOrder order = orders.get(0);

            // 检查订单状态
            if (!String.valueOf(OrderStatusEnum.REFUNDING.getCode()).equals(order.getStatus())) {
                return DomainResult.error("当前订单状态不支持退款处理");
            }

            if (approve) {
                // 同意退款
                // 1. 更新订单状态为已退款
                orderDao.updateStatusById(OrderStatusEnum.REFUNDED.getCode(), orderId);

                // 2. 返还用户余额
                YsUser user = userDao.selectById(order.getUserId());
                BigDecimal refundAmount = order.getRefundAmount();
                user.setBalance(user.getBalance().add(refundAmount));
                userDao.updateBalanceById(user);

                // 3. 恢复商品库存
                goodsDao.increaseStock(order.getGoodsId(), order.getQuantity());

                log.info("商家同意退款成功: orderId={}, refundAmount={}", orderId, refundAmount);
                return DomainResult.success("退款处理成功");
            } else {
                // 拒绝退款
                // 更新订单状态回退到已支付或已发货状态
                if (order.getShipTime() != null) {
                    orderDao.updateStatusById(OrderStatusEnum.SHIPPED.getCode(), orderId);
                } else {
                    orderDao.updateStatusById(OrderStatusEnum.PAID.getCode(), orderId);
                }

                log.info("商家拒绝退款: orderId={}", orderId);
                return DomainResult.success("已拒绝退款");
            }
        } catch (Exception e) {
            log.error("处理退款失败: orderId={}, error={}", orderId, e.getMessage(), e);
            return DomainResult.error("处理退款失败: " + e.getMessage());
        }
    }

    @Override
    public DomainResult updateLogistics(Long orderId, String logisticsNo, String logisticsCompany) {
        try {
            if (orderId == null || logisticsNo == null || logisticsNo.trim().isEmpty()) {
                return DomainResult.error("订单ID和物流单号不能为空");
            }

            // 获取订单信息
            List<YsOrder> orders = orderDao.selectsById(orderId);
            if (orders == null || orders.isEmpty()) {
                return DomainResult.error("订单不存在");
            }

            YsOrder order = orders.get(0);

            // 更新物流信息
            order.setLogisticsNo(logisticsNo.trim());
            order.setLogisticsCompany(logisticsCompany != null ? logisticsCompany.trim() : null);
            orderDao.updateById(order);

            log.info("更新物流信息成功: orderId={}, logisticsNo={}", orderId, logisticsNo);
            return DomainResult.success("物流信息更新成功");
        } catch (Exception e) {
            log.error("更新物流信息失败: orderId={}, error={}", orderId, e.getMessage(), e);
            return DomainResult.error("更新物流信息失败: " + e.getMessage());
        }
    }

    @Override
    public DomainResult trackLogistics(Long orderId) {
        try {
            if (orderId == null || orderId <= 0) {
                return DomainResult.error("订单ID不能为空");
            }

            // 获取订单信息
            List<YsOrder> orders = orderDao.selectsById(orderId);
            if (orders == null || orders.isEmpty()) {
                return DomainResult.error("订单不存在");
            }

            YsOrder order = orders.get(0);

            // 模拟物流追踪信息（实际项目中应调用物流公司API）
            List<Map<String, Object>> trackingInfo = new ArrayList<>();

            // 订单创建
            Map<String, Object> step1 = new HashMap<>();
            step1.put("time", order.getAddtime());
            step1.put("status", "订单创建");
            step1.put("description", "您的订单已创建，等待支付");
            trackingInfo.add(step1);

            // 支付成功
            if (order.getPayTime() != null) {
                Map<String, Object> step2 = new HashMap<>();
                step2.put("time", order.getPayTime());
                step2.put("status", "支付成功");
                step2.put("description", "您已成功支付订单");
                trackingInfo.add(step2);
            }

            // 发货
            if (order.getShipTime() != null) {
                Map<String, Object> step3 = new HashMap<>();
                step3.put("time", order.getShipTime());
                step3.put("status", "商家发货");
                step3.put("description", "商家已发货，物流公司：" + order.getLogisticsCompany() +
                        "，物流单号：" + order.getLogisticsNo());
                trackingInfo.add(step3);

                // 模拟物流节点
                Map<String, Object> step4 = new HashMap<>();
                step4.put("time", order.getShipTime().plusHours(2));
                step4.put("status", "包裹已揽收");
                step4.put("description", "快递公司已揽收包裹");
                trackingInfo.add(step4);

                Map<String, Object> step5 = new HashMap<>();
                step5.put("time", order.getShipTime().plusHours(12));
                step5.put("status", "包裹运输中");
                step5.put("description", "包裹正在运输途中，请耐心等待");
                trackingInfo.add(step5);
            }

            // 完成
            if (order.getFinishTime() != null) {
                Map<String, Object> step6 = new HashMap<>();
                step6.put("time", order.getFinishTime());
                step6.put("status", "已签收");
                step6.put("description", "您已签收包裹，感谢您的购买");
                trackingInfo.add(step6);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("orderId", orderId);
            result.put("logisticsNo", order.getLogisticsNo());
            result.put("logisticsCompany", order.getLogisticsCompany());
            result.put("tracking", trackingInfo);

            log.info("获取物流追踪信息成功: orderId={}", orderId);
            return DomainResult.success(result);
        } catch (Exception e) {
            log.error("获取物流追踪信息失败: orderId={}, error={}", orderId, e.getMessage(), e);
            return DomainResult.error("获取物流追踪信息失败: " + e.getMessage());
        }
    }

    @Override
    public DomainResult confirmReceipt(Long orderId, Long userId) {
        try {
            if (orderId == null || userId == null) {
                return DomainResult.error("订单ID和用户ID不能为空");
            }

            // 获取订单信息
            List<YsOrder> orders = orderDao.selectsById(orderId);
            if (orders == null || orders.isEmpty()) {
                return DomainResult.error("订单不存在");
            }

            YsOrder order = orders.get(0);

            // 验证订单所属用户
            if (!order.getUserId().equals(userId)) {
                return DomainResult.error("无权操作该订单");
            }

            // 检查订单状态
            if (!String.valueOf(OrderStatusEnum.SHIPPED.getCode()).equals(order.getStatus())) {
                return DomainResult.error("只有已发货的订单可以确认收货");
            }

            // 更新订单状态为已完成
            orderDao.updateStatusById(OrderStatusEnum.COMPLETED.getCode(), orderId);

            // 更新完成时间
            order.setFinishTime(LocalDateTime.now());
            orderDao.updateById(order);

            log.info("用户确认收货成功: orderId={}, userId={}", orderId, userId);
            return DomainResult.success("确认收货成功");
        } catch (Exception e) {
            log.error("确认收货失败: orderId={}, userId={}, error={}", orderId, userId, e.getMessage(), e);
            return DomainResult.error("确认收货失败: " + e.getMessage());
        }
    }

    @Override
    public DomainResult cancelOrder(Long orderId, Long userId, String cancelReason) {
        try {
            if (orderId == null || userId == null) {
                return DomainResult.error("订单ID和用户ID不能为空");
            }

            // 获取订单信息
            List<YsOrder> orders = orderDao.selectsById(orderId);
            if (orders == null || orders.isEmpty()) {
                return DomainResult.error("订单不存在");
            }

            YsOrder order = orders.get(0);

            // 验证订单所属用户
            if (!order.getUserId().equals(userId)) {
                return DomainResult.error("无权操作该订单");
            }

            // 检查订单状态，只有待支付订单可以取消
            if (!String.valueOf(OrderStatusEnum.PENDING_PAYMENT.getCode()).equals(order.getStatus())) {
                return DomainResult.error("当前订单状态不支持取消");
            }

            // 取消订单前，返还库存（同一订单可能多行商品）
            for (YsOrder o : orders) {
                Integer q = o.getQuantity() != null ? o.getQuantity() : 1;
                if (o.getGoodsId() != null && q > 0) {
                    goodsDao.increaseStock(o.getGoodsId(), q);
                }
            }

            // 更新订单状态为已取消
            orderDao.updateStatusById(OrderStatusEnum.CANCELLED.getCode(), orderId);

            // 保存取消原因
            for (YsOrder o : orders) {
                o.setRefundReason(cancelReason);
                orderDao.updateById(o);
            }

            log.info("用户取消订单成功: orderId={}, userId={}", orderId, userId);
            return DomainResult.success("订单已取消");
        } catch (Exception e) {
            log.error("取消订单失败: orderId={}, userId={}, error={}", orderId, userId, e.getMessage(), e);
            return DomainResult.error("取消订单失败: " + e.getMessage());
        }
    }

    @Override
    public DomainResult shipOrder(Long orderId, String logisticsNo, String logisticsCompany) {
        try {
            if (orderId == null || logisticsNo == null || logisticsNo.trim().isEmpty()) {
                return DomainResult.error("订单ID和物流单号不能为空");
            }

            // 获取订单信息
            List<YsOrder> orders = orderDao.selectsById(orderId);
            if (orders == null || orders.isEmpty()) {
                return DomainResult.error("订单不存在");
            }

            YsOrder order = orders.get(0);

            // 检查订单状态，只有已支付订单可以发货
            if (!String.valueOf(OrderStatusEnum.PAID.getCode()).equals(order.getStatus())) {
                return DomainResult.error("当前订单状态不支持发货");
            }

            // 更新订单状态为已发货
            orderDao.updateStatusById(OrderStatusEnum.SHIPPED.getCode(), orderId);

            // 更新发货信息和物流信息
            order.setShipTime(LocalDateTime.now());
            order.setLogisticsNo(logisticsNo.trim());
            order.setLogisticsCompany(logisticsCompany != null ? logisticsCompany.trim() : null);
            orderDao.updateById(order);

            log.info("商家发货成功: orderId={}, logisticsNo={}", orderId, logisticsNo);
            return DomainResult.success("发货成功");
        } catch (Exception e) {
            log.error("发货失败: orderId={}, error={}", orderId, e.getMessage(), e);
            return DomainResult.error("发货失败: " + e.getMessage());
        }
    }

    /**
     * 获取订单状态描述
     */
    private String getStatusDesc(String status) {
        if (status == null) {
            return "未知状态";
        }

        try {
            int code = Integer.parseInt(status);
            OrderStatusEnum statusEnum = OrderStatusEnum.fromCode(code);
            return statusEnum != null ? statusEnum.getDescription() : "未知状态";
        } catch (Exception e) {
            return "未知状态";
        }
    }
}
