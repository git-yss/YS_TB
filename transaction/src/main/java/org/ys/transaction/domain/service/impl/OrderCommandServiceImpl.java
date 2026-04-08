package org.ys.transaction.domain.service.impl;

import org.springframework.stereotype.Service;
import org.ys.transaction.domain.aggregate.GoodsAggregate;
import org.ys.transaction.domain.aggregate.OrderAggregate;
import org.ys.transaction.domain.aggregate.UserAggregate;
import org.ys.transaction.domain.entity.YsGoods;
import org.ys.transaction.domain.entity.YsOrder;
import org.ys.transaction.domain.entity.YsUser;
import org.ys.transaction.domain.respository.YsGoodsRespository;
import org.ys.transaction.domain.respository.YsOrderRespository;
import org.ys.transaction.domain.respository.YsUserRespository;
import org.ys.transaction.domain.service.OrderCommandService;

import jakarta.annotation.Resource;

import java.math.BigDecimal;

/**
 * 璁㈠崟鍛戒护鐢ㄤ緥瀹炵幇锛氳礋璐ｈ法鑱氬悎鍗忎綔銆佹牎楠屼笌鐘舵€佹祦杞€? */
@Service
public class OrderCommandServiceImpl implements OrderCommandService {

    @Resource
    private YsOrderRespository ysOrderRespository;

    @Resource
    private YsGoodsRespository ysGoodsRespository;

    @Resource
    private YsUserRespository ysUserRespository;

    @Override
    public void cancel(Long orderId, Long userId, String reason) {
        OrderAggregate orderAggregate = load(orderId);
        orderAggregate.checkBelongTo(userId);
        orderAggregate.checkCanCancel();
        restoreStock(orderAggregate);
        ysOrderRespository.updateStatusById(new OrderAggregate(orderAggregate.buildCancelOrder(reason), null, null, null));
    }

    @Override
    public void confirmReceipt(Long orderId, Long userId) {
        OrderAggregate orderAggregate = load(orderId);
        orderAggregate.checkBelongTo(userId);
        orderAggregate.checkCanConfirmReceipt();
        ysOrderRespository.updateStatusById(new OrderAggregate(orderAggregate.buildConfirmedReceiptOrder(), null, null, null));
    }

    @Override
    public void applyRefund(Long orderId, Long userId, String refundReason) {
        OrderAggregate aggregate = load(orderId);
        aggregate.checkBelongTo(userId);
        aggregate.checkCanApplyRefund();
        ysOrderRespository.updateById(new OrderAggregate(aggregate.buildApplyingRefundOrder(refundReason), null, null, null));
    }

    @Override
    public void handleRefund(Long orderId, Boolean approve, String refundReason) {
        if (approve == null) {
            throw new IllegalArgumentException("澶勭悊缁撴灉涓嶈兘涓虹┖");
        }
        OrderAggregate aggregate = load(orderId);
        aggregate.checkCanHandleRefund();

        if (approve) {
            ysOrderRespository.updateById(new OrderAggregate(aggregate.buildRefundApprovedOrder(), null, null, null));
            refundUserAndRestoreStock(aggregate);
        } else {
            ysOrderRespository.updateById(new OrderAggregate(aggregate.buildRefundRejectedOrder(), null, null, null));
        }
    }

    @Override
    public void updateLogistics(Long orderId, String logisticsNo, String logisticsCompany) {
        OrderAggregate aggregate = load(orderId);
        ysOrderRespository.updateById(new OrderAggregate(aggregate.buildUpdatedLogisticsOrder(logisticsNo, logisticsCompany), null, null, null));
    }

    @Override
    public void shipOrder(Long orderId, String logisticsNo, String logisticsCompany) {
        OrderAggregate aggregate = load(orderId);
        aggregate.checkCanShip();
        ysOrderRespository.updateById(new OrderAggregate(aggregate.buildShippedOrder(logisticsNo, logisticsCompany), null, null, null));
    }

    private OrderAggregate load(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("订单ID不能为空");
        }
        OrderAggregate aggregate = ysOrderRespository.selectAggregateById(
                new OrderAggregate(YsOrder.identify(orderId), null, null, null)
        );
        if (aggregate == null || aggregate.getOrder() == null) {
            throw new IllegalStateException("订单不存在");
        }
        return aggregate;
    }

    private void restoreStock(OrderAggregate orderAggregate) {
        YsOrder order = orderAggregate.getOrder();
        Integer quantity = order.getQuantity() == null ? 1 : order.getQuantity();
        if (order.getGoodsId() == null || quantity <= 0) {
            return;
        }
        ysGoodsRespository.increaseStock(new GoodsAggregate(
                YsOrder.rehydrate(null, null, null, null, null, quantity, null, null, null, null, null, null, null, null, null, null, null),
                YsGoods.identify(order.getGoodsId()),
                null
        ));
    }

    private void refundUserAndRestoreStock(OrderAggregate aggregate) {
        YsOrder order = aggregate.getOrder();
        if (order.getRefundAmount() == null) {
            throw new IllegalStateException("退款金额异常");
        }

        org.ys.transaction.domain.aggregate.UserAggregate userAggregate =
                ysUserRespository.selectAggregateById(String.valueOf(order.getUserId()));
        YsUser user = userAggregate == null ? null : userAggregate.getUser();
        if (user == null) {
            throw new IllegalStateException("用户不存在");
        }
        BigDecimal currentBalance = user.getBalance() == null ? BigDecimal.ZERO : user.getBalance();
        YsUser updatedUser = YsUser.rehydrate(
                user.getId(), user.getUsername(), user.getPassword(), user.getAge(), user.getSex(),
                currentBalance.add(order.getRefundAmount()),
                user.getEmail(), user.getTel(), user.getStatus(), user.getCreateTime()
        );
        ysUserRespository.updateBalanceById(new UserAggregate(updatedUser, null));

        Integer quantity = order.getQuantity() == null ? 1 : order.getQuantity();
        if (order.getGoodsId() != null && quantity > 0) {
            ysGoodsRespository.increaseStock(new GoodsAggregate(
                    YsOrder.rehydrate(null, null, null, null, null, quantity, null, null, null, null, null, null, null, null, null, null, null),
                    YsGoods.identify(order.getGoodsId()),
                    null
            ));
        }
    }
}


