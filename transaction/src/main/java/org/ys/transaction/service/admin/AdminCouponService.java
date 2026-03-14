package org.ys.transaction.service.admin;

import org.ys.commens.pojo.CommentResult;

import java.util.Map;

/**
 * 后台优惠券管理服务接口
 */
public interface AdminCouponService {

    /**
     * 获取优惠券列表
     */
    CommentResult getCouponList(Integer pageNum, Integer pageSize);

    /**
     * 创建优惠券
     */
    CommentResult createCoupon(Map<String, Object> params);

    /**
     * 更新优惠券
     */
    CommentResult updateCoupon(Map<String, Object> params);

    /**
     * 删除优惠券
     */
    CommentResult deleteCoupon(Long id);

    /**
     * 发放优惠券
     */
    CommentResult distributeCoupon(Map<String, Object> params);

    /**
     * 获取优惠券使用统计
     */
    CommentResult getCouponStatistics();
}
