package org.ys.transaction.domain.inteface.admin;

import org.ys.transaction.domain.vo.DomainResult;

import java.util.Map;

/**
 * 后台优惠券管理服务接口
 */
public interface AdminCouponService {

    /**
     * 获取优惠券列表
     */
    DomainResult getCouponList(Integer pageNum, Integer pageSize);

    /**
     * 创建优惠券
     */
    DomainResult createCoupon(Map<String, Object> params);

    /**
     * 更新优惠券
     */
    DomainResult updateCoupon(Map<String, Object> params);

    /**
     * 删除优惠券
     */
    DomainResult deleteCoupon(Long id);

    /**
     * 发放优惠券
     */
    DomainResult distributeCoupon(Map<String, Object> params);

    /**
     * 获取优惠券使用统计
     */
    DomainResult getCouponStatistics();
}
