package org.ys.commens.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

/**
 * 用户优惠券关联表实体
 *
 * @author makejava
 * @since 2025-07-16
 */
@TableName("ys_user_coupon")
public class YsUserCoupon {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long couponId;
    /** 状态：0=未使用 1=已使用 2=已过期 */
    private Integer status;
    private Date getTime;
    private Date expireTime;
    private Long orderId;
    private Date useTime;

    /** 关联优惠券详情（非表字段） */
    @TableField(exist = false)
    private YsCoupon coupon;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getCouponId() { return couponId; }
    public void setCouponId(Long couponId) { this.couponId = couponId; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Date getGetTime() { return getTime; }
    public void setGetTime(Date getTime) { this.getTime = getTime; }
    public Date getExpireTime() { return expireTime; }
    public void setExpireTime(Date expireTime) { this.expireTime = expireTime; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Date getUseTime() { return useTime; }
    public void setUseTime(Date useTime) { this.useTime = useTime; }
    public YsCoupon getCoupon() { return coupon; }
    public void setCoupon(YsCoupon coupon) { this.coupon = coupon; }
}
