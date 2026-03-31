package org.ys.transaction.domain.entity;


import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * (YsOrder)表实体类
 *
 * @author makejava
 * @since 2025-08-31 12:34:48
 */
@Getter
public class YsOrder {
    private Long id;
//用户id
    private Long userId;
//商品id
    private Long goodsId;
//订单状态
    private String status;
    private LocalDateTime addTime;
//订单时间
//商品数量
    private Integer quantity;
//商品单价
    private BigDecimal unitPrice;
//订单总金额
    private BigDecimal totalAmount;
//支付方式：balance=余额支付，wechat=微信支付，alipay=支付宝
    private String payMethod;
//物流单号
    private String logisticsNo;
//物流公司
    private String logisticsCompany;
//发货时间
    private LocalDateTime shipTime;
//支付时间
    private LocalDateTime payTime;
//完成时间
    private LocalDateTime finishTime;
//退款时间
    private LocalDateTime refundTime;
//退款原因
    private String refundReason;
//退款金额
    private BigDecimal refundAmount;

    private YsOrder(Long id,
                    Long userId,
                    Long goodsId,
                    String status,
                    LocalDateTime addTime,
                    Integer quantity,
                    BigDecimal unitPrice,
                    BigDecimal totalAmount,
                    String payMethod,
                    String logisticsNo,
                    String logisticsCompany,
                    LocalDateTime shipTime,
                    LocalDateTime payTime,
                    LocalDateTime finishTime,
                    LocalDateTime refundTime,
                    String refundReason,
                    BigDecimal refundAmount) {
        this.id = id;
        this.userId = userId;
        this.goodsId = goodsId;
        this.status = status;
        this.addTime = addTime;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalAmount = totalAmount;
        this.payMethod = payMethod;
        this.logisticsNo = logisticsNo;
        this.logisticsCompany = logisticsCompany;
        this.shipTime = shipTime;
        this.payTime = payTime;
        this.finishTime = finishTime;
        this.refundTime = refundTime;
        this.refundReason = refundReason;
        this.refundAmount = refundAmount;
    }

    public static YsOrder rehydrate(Long id,
                                   Long userId,
                                   Long goodsId,
                                   String status,
                                   LocalDateTime addTime,
                                   Integer quantity,
                                   BigDecimal unitPrice,
                                   BigDecimal totalAmount,
                                   String payMethod,
                                   String logisticsNo,
                                   String logisticsCompany,
                                   LocalDateTime shipTime,
                                   LocalDateTime payTime,
                                   LocalDateTime finishTime,
                                   LocalDateTime refundTime,
                                   String refundReason,
                                   BigDecimal refundAmount) {
        return new YsOrder(id, userId, goodsId, status, addTime, quantity, unitPrice, totalAmount, payMethod,
                logisticsNo, logisticsCompany, shipTime, payTime, finishTime, refundTime, refundReason, refundAmount);
    }

    public static YsOrder identify(Long id) {
        return new YsOrder(id, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null);
    }

    public void create(Long userId, Long goodsId, Integer quantity, BigDecimal unitPrice) {
        if (userId == null || goodsId == null) {
            throw new IllegalArgumentException("userId and goodsId are required");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("quantity must be greater than 0");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("unitPrice is invalid");
        }
        this.userId = userId;
        this.goodsId = goodsId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
        this.status = "CREATED";
        this.addTime = LocalDateTime.now();
    }

    public void markPaid(String payMethod) {
        this.status = "PAID";
        this.payMethod = payMethod;
        this.payTime = LocalDateTime.now();
    }

    public void markShipped(String logisticsNo, String logisticsCompany) {
        this.status = "SHIPPED";
        this.logisticsNo = logisticsNo;
        this.logisticsCompany = logisticsCompany;
        this.shipTime = LocalDateTime.now();
    }

}
