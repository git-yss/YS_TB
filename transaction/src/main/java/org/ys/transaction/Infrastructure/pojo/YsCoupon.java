package org.ys.transaction.Infrastructure.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券表实体
 *
 * @author makejava
 * @since 2025-07-16
 */
@Data
@TableName("ys_coupon")
public class YsCoupon {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 优惠券名称 */
    private String name;
    /** 类型：1=满减 2=折扣 */
    private Integer type;
    /** 面额/折扣值 */
    private BigDecimal discountAmount;
    /** 最低消费金额 */
    private BigDecimal minAmount;
    /** 发放总量 */
    private Integer totalCount;
    /** 已领取/使用数量 */
    private Integer usedCount;
    /** 每人限领数量 */
    private Integer perUserLimit;
    /** 有效期开始 */
    private LocalDateTime validStartTime;
    /** 有效期结束 */
    private LocalDateTime validEndTime;
    /** 状态：0=禁用 1=有效 */
    private Integer status;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;


}
