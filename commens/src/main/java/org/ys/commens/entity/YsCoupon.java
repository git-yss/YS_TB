package org.ys.commens.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 优惠券表实体
 *
 * @author makejava
 * @since 2025-07-16
 */
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
    private Date validStartTime;
    /** 有效期结束 */
    private Date validEndTime;
    /** 状态：0=禁用 1=有效 */
    private Integer status;
    /** 创建时间 */
    private Date createdAt;
    /** 更新时间 */
    private Date updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getMinAmount() { return minAmount; }
    public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }
    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
    public Integer getUsedCount() { return usedCount; }
    public void setUsedCount(Integer usedCount) { this.usedCount = usedCount; }
    public Integer getPerUserLimit() { return perUserLimit; }
    public void setPerUserLimit(Integer perUserLimit) { this.perUserLimit = perUserLimit; }
    public Date getValidStartTime() { return validStartTime; }
    public void setValidStartTime(Date validStartTime) { this.validStartTime = validStartTime; }
    public Date getValidEndTime() { return validEndTime; }
    public void setValidEndTime(Date validEndTime) { this.validEndTime = validEndTime; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
