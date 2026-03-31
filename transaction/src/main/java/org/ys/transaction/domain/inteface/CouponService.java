package org.ys.transaction.domain.inteface;

import org.ys.transaction.domain.vo.DomainResult;

import java.util.Map;

/**
 * 优惠券服务接口
 *
 * @author makejava
 * @since 2025-07-16
 */
public interface CouponService {

    /**
     * 创建优惠券（管理员）
     * @param params 优惠券参数
     * @return 创建结果
     */
    DomainResult createCoupon(Map<String, Object> params);

    /**
     * 领取优惠券
     * @param userId 用户ID
     * @param couponId 优惠券ID
     * @return 领取结果
     */
    DomainResult claimCoupon(Long userId, Long couponId);

    /**
     * 获取用户优惠券列表
     * @param userId 用户ID
     * @param status 状态（0=未使用，1=已使用，2=已过期）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 优惠券列表
     */
    DomainResult getUserCoupons(Long userId, Integer status, Integer pageNum, Integer pageSize);

    /**
     * 获取可用优惠券列表
     * @param userId 用户ID
     * @param amount 订单金额
     * @param goodsIds 商品ID列表
     * @return 可用优惠券列表
     */
    DomainResult getAvailableCoupons(Long userId, java.math.BigDecimal amount, String goodsIds);

    /**
     * 使用优惠券
     * @param userId 用户ID
     * @param couponId 优惠券ID
     * @param orderId 订单ID
     * @return 使用结果
     */
    DomainResult useCoupon(Long userId, Long couponId, Long orderId);

    /**
     * 获取优惠券详情
     * @param couponId 优惠券ID
     * @return 优惠券详情
     */
    DomainResult getCouponDetail(Long couponId);

    /**
     * 获取优惠券列表（管理员）
     * @param params 查询参数
     * @return 优惠券列表
     */
    DomainResult getCouponList(Map<String, Object> params);

    /**
     * 更新优惠券（管理员）
     * @param params 更新参数
     * @return 更新结果
     */
    DomainResult updateCoupon(Map<String, Object> params);

    /**
     * 删除优惠券（管理员）
     * @param couponId 优惠券ID
     * @return 删除结果
     */
    DomainResult deleteCoupon(Long couponId);
}
