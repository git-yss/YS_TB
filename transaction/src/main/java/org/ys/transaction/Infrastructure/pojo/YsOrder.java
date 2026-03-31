package org.ys.transaction.Infrastructure.pojo;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * (YsOrder)表实体类
 *
 * @author makejava
 * @since 2025-08-31 12:34:48
 */
@SuppressWarnings("serial")
@TableName("ys_order")
@Data
public class YsOrder   {
//ID
@TableId(value = "id", type = IdType.INPUT)
    private Long id;
//用户id
    private Long userId;
//商品id
    private Long goodsId;
//订单状态
    private String status;
//订单时间
    private LocalDateTime addtime;
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

     // 以下字段用于订单详情查询（join 查询可能返回冗余字段）
    @TableField(exist = false)
    private String goodsName; // 商品名称（join 查询返回）
    @TableField(exist = false)
    private String goodsImage; // 商品图片（join 查询返回）
    @TableField(exist = false)
    private String username;   // 用户名（join 查询返回）
    @TableField(exist = false)
    private String userTel;    // 用户手机号（join 查询返回）


}
