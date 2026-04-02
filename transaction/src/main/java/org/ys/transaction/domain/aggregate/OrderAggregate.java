package org.ys.transaction.domain.aggregate;

import lombok.Data;
import org.ys.transaction.domain.entity.YsGoods;
import org.ys.transaction.domain.entity.YsOrder;
import org.ys.transaction.domain.entity.YsUser;
import org.ys.transaction.domain.entity.YsUserAddr;
import org.ys.transaction.domain.enums.OrderStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderAggregate {
    private  YsOrder order;
    private  YsGoods goods;
    private  YsUser user;
    private  YsUserAddr addr;

    public OrderAggregate(YsOrder order, YsGoods goods, YsUser user, YsUserAddr addr) {
        this.addr = addr;
        if (order == null) {
            throw new IllegalArgumentException("order cannot be null");
        }
        this.order = order;
        this.goods = goods;
        this.user = user;
    }

    public void payByBalance() {
        if (user == null) {
            throw new IllegalStateException("user is required for payment");
        }
        BigDecimal amount = order.getTotalAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("order totalAmount is invalid");
        }
        user.debitBalance(amount);
        order.markPaid("balance");
    }

    public void checkBelongTo(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (order.getUserId() == null || !order.getUserId().equals(userId)) {
            throw new IllegalStateException("无权操作该订单");
        }
    }

    public void checkCanCancel() {
        if (!isStatus(OrderStatusEnum.PENDING_PAYMENT)) {
            throw new IllegalStateException("当前订单状态不支持取消");
        }
    }

    public void checkCanConfirmReceipt() {
        if (!isStatus(OrderStatusEnum.SHIPPED)) {
            throw new IllegalStateException("只有已发货的订单可以确认收货");
        }
    }

    public YsOrder buildCancelOrder(String reason) {
        return YsOrder.rehydrate(
                order.getId(), order.getUserId(), order.getGoodsId(),
                String.valueOf(OrderStatusEnum.CANCELLED.getCode()),
                order.getAddTime(), order.getQuantity(), order.getUnitPrice(), order.getTotalAmount(),
                order.getPayMethod(), order.getLogisticsNo(), order.getLogisticsCompany(),
                order.getShipTime(), order.getPayTime(), order.getFinishTime(), order.getRefundTime(),
                reason, order.getRefundAmount()
        );
    }

    public YsOrder buildConfirmedReceiptOrder() {
        return YsOrder.rehydrate(
                order.getId(), order.getUserId(), order.getGoodsId(),
                String.valueOf(OrderStatusEnum.COMPLETED.getCode()),
                order.getAddTime(), order.getQuantity(), order.getUnitPrice(), order.getTotalAmount(),
                order.getPayMethod(), order.getLogisticsNo(), order.getLogisticsCompany(),
                order.getShipTime(), order.getPayTime(), LocalDateTime.now(), order.getRefundTime(),
                order.getRefundReason(), order.getRefundAmount()
        );
    }

    public void checkCanApplyRefund() {
        if (!isStatus(OrderStatusEnum.PAID) && !isStatus(OrderStatusEnum.SHIPPED)) {
            throw new IllegalStateException("当前订单状态不支持退款");
        }
    }

    public YsOrder buildApplyingRefundOrder(String refundReason) {
        BigDecimal refundAmount = order.getTotalAmount();
        if (refundAmount == null && order.getUnitPrice() != null && order.getQuantity() != null) {
            refundAmount = order.getUnitPrice().multiply(BigDecimal.valueOf(order.getQuantity()));
        }
        return YsOrder.rehydrate(
                order.getId(), order.getUserId(), order.getGoodsId(),
                String.valueOf(OrderStatusEnum.REFUNDING.getCode()),
                order.getAddTime(), order.getQuantity(), order.getUnitPrice(), order.getTotalAmount(),
                order.getPayMethod(), order.getLogisticsNo(), order.getLogisticsCompany(),
                order.getShipTime(), order.getPayTime(), order.getFinishTime(), LocalDateTime.now(),
                refundReason, refundAmount
        );
    }

    public void checkCanHandleRefund() {
        if (!isStatus(OrderStatusEnum.REFUNDING)) {
            throw new IllegalStateException("当前订单状态不支持退款处理");
        }
    }

    public YsOrder buildRefundApprovedOrder() {
        return YsOrder.rehydrate(
                order.getId(), order.getUserId(), order.getGoodsId(),
                String.valueOf(OrderStatusEnum.REFUNDED.getCode()),
                order.getAddTime(), order.getQuantity(), order.getUnitPrice(), order.getTotalAmount(),
                order.getPayMethod(), order.getLogisticsNo(), order.getLogisticsCompany(),
                order.getShipTime(), order.getPayTime(), order.getFinishTime(), order.getRefundTime(),
                order.getRefundReason(), order.getRefundAmount()
        );
    }

    public YsOrder buildRefundRejectedOrder() {
        String target = order.getShipTime() != null
                ? String.valueOf(OrderStatusEnum.SHIPPED.getCode())
                : String.valueOf(OrderStatusEnum.PAID.getCode());
        return YsOrder.rehydrate(
                order.getId(), order.getUserId(), order.getGoodsId(),
                target,
                order.getAddTime(), order.getQuantity(), order.getUnitPrice(), order.getTotalAmount(),
                order.getPayMethod(), order.getLogisticsNo(), order.getLogisticsCompany(),
                order.getShipTime(), order.getPayTime(), order.getFinishTime(), order.getRefundTime(),
                order.getRefundReason(), order.getRefundAmount()
        );
    }

    public YsOrder buildUpdatedLogisticsOrder(String logisticsNo, String logisticsCompany) {
        if (logisticsNo == null || logisticsNo.trim().isEmpty()) {
            throw new IllegalArgumentException("物流单号不能为空");
        }
        return YsOrder.rehydrate(
                order.getId(), order.getUserId(), order.getGoodsId(), order.getStatus(),
                order.getAddTime(), order.getQuantity(), order.getUnitPrice(), order.getTotalAmount(),
                order.getPayMethod(), logisticsNo.trim(),
                logisticsCompany == null ? null : logisticsCompany.trim(),
                order.getShipTime(), order.getPayTime(), order.getFinishTime(), order.getRefundTime(),
                order.getRefundReason(), order.getRefundAmount()
        );
    }

    public void checkCanShip() {
        if (!isStatus(OrderStatusEnum.PAID)) {
            throw new IllegalStateException("当前订单状态不支持发货");
        }
    }

    public YsOrder buildShippedOrder(String logisticsNo, String logisticsCompany) {
        if (logisticsNo == null || logisticsNo.trim().isEmpty()) {
            throw new IllegalArgumentException("订单ID和物流单号不能为空");
        }
        return YsOrder.rehydrate(
                order.getId(), order.getUserId(), order.getGoodsId(), String.valueOf(OrderStatusEnum.SHIPPED.getCode()),
                order.getAddTime(), order.getQuantity(), order.getUnitPrice(), order.getTotalAmount(),
                order.getPayMethod(), logisticsNo.trim(),
                logisticsCompany == null ? null : logisticsCompany.trim(),
                LocalDateTime.now(), order.getPayTime(), order.getFinishTime(), order.getRefundTime(),
                order.getRefundReason(), order.getRefundAmount()
        );
    }

    private boolean isStatus(OrderStatusEnum statusEnum) {
        return order.getStatus() != null && order.getStatus().equals(String.valueOf(statusEnum.getCode()));
    }

    public String getStatusDesc() {
        if (order.getStatus() == null) {
            return "未知状态";
        }
        try {
            int code = Integer.parseInt(order.getStatus());
            return OrderStatusEnum.fromCode(code).getDescription();
        } catch (Exception e) {
            return "未知状态";
        }
    }
}
