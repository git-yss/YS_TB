package org.ys.transaction.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ys.commens.dao.YsCouponDao;
import org.ys.commens.dao.YsUserCouponDao;
import org.ys.commens.dao.YsUserDao;
import org.ys.commens.entity.YsCoupon;
import org.ys.commens.entity.YsUser;
import org.ys.commens.entity.YsUserCoupon;
import org.ys.commens.pojo.CommentResult;
import org.ys.transaction.service.CouponService;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 优惠券服务实现类
 *
 * @author makejava
 * @since 2025-07-16
 */
@Service
@Transactional
public class CouponServiceImpl implements CouponService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CouponServiceImpl.class);

    @Resource
    private YsCouponDao couponDao;

    @Resource
    private YsUserCouponDao userCouponDao;

    @Resource
    private YsUserDao userDao;

    @Override
    public CommentResult createCoupon(Map<String, Object> params) {
        try {
            YsCoupon coupon = new YsCoupon();
            coupon.setName(params.get("name") != null ? params.get("name").toString() : null);
            coupon.setType(params.get("type") != null ? Integer.valueOf(params.get("type").toString()) : 1);
            coupon.setDiscountAmount(params.get("discountAmount") != null ? new BigDecimal(params.get("discountAmount").toString()) : BigDecimal.ZERO);
            coupon.setMinAmount(params.get("minAmount") != null ? new BigDecimal(params.get("minAmount").toString()) : BigDecimal.ZERO);
            coupon.setTotalCount(params.get("totalCount") != null ? Integer.valueOf(params.get("totalCount").toString()) : 0);
            coupon.setUsedCount(0);
            coupon.setPerUserLimit(params.get("perUserLimit") != null ? Integer.valueOf(params.get("perUserLimit").toString()) : 1);
            coupon.setValidStartTime(params.get("validStartTime") != null ? (Date) params.get("validStartTime") : new Date());
            coupon.setValidEndTime(params.get("validEndTime") != null ? (Date) params.get("validEndTime") : new Date());
            coupon.setStatus(params.get("status") != null ? Integer.valueOf(params.get("status").toString()) : 1);
            Date now = new Date();
            coupon.setCreatedAt(now);
            coupon.setUpdatedAt(now);

            int result = couponDao.insert(coupon);
            if (result > 0) {
                return CommentResult.ok("优惠券创建成功");
            } else {
                return CommentResult.error("优惠券创建失败");
            }
        } catch (Exception e) {
            log.error("创建优惠券失败: {}", e.getMessage(), e);
            return CommentResult.error("创建失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult claimCoupon(Long userId, Long couponId) {
        try {
            // 检查用户是否存在
            YsUser user = userDao.selectById(userId);
            if (user == null) {
                return CommentResult.error("用户不存在");
            }

            // 检查优惠券是否存在
            YsCoupon coupon = couponDao.selectById(couponId);
            if (coupon == null) {
                return CommentResult.error("优惠券不存在");
            }

            // 检查优惠券状态
            if (coupon.getStatus() != 1) {
                return CommentResult.error("优惠券已失效");
            }

            // 检查优惠券是否在有效期内
            Date now = new Date();
            if (coupon.getValidStartTime() != null && now.before(coupon.getValidStartTime())) {
                return CommentResult.error("优惠券不在有效期内");
            }
            if (coupon.getValidEndTime() != null && now.after(coupon.getValidEndTime())) {
                return CommentResult.error("优惠券不在有效期内");
            }

            // 检查优惠券库存
            int totalCount = coupon.getTotalCount() != null ? coupon.getTotalCount() : 0;
            int usedCount = coupon.getUsedCount() != null ? coupon.getUsedCount() : 0;
            if (totalCount <= usedCount) {
                return CommentResult.error("优惠券已领完");
            }

            // 检查用户是否已领取
            int perUserLimit = coupon.getPerUserLimit() != null ? coupon.getPerUserLimit() : 1;
            QueryWrapper<YsUserCoupon> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId).eq("coupon_id", couponId);
            List<YsUserCoupon> userCoupons = userCouponDao.selectList(queryWrapper);
            if (userCoupons != null && userCoupons.size() >= perUserLimit) {
                return CommentResult.error("您已达到领取上限");
            }

            // 创建用户优惠券
            YsUserCoupon userCoupon = new YsUserCoupon();
            userCoupon.setUserId(userId);
            userCoupon.setCouponId(couponId);
            userCoupon.setStatus(0); // 未使用
            userCoupon.setGetTime(new Date());
            userCoupon.setExpireTime(coupon.getValidEndTime());

            int result = userCouponDao.insert(userCoupon);
            if (result > 0) {
                // 更新优惠券已领取数量
                coupon.setUsedCount(usedCount + 1);
                couponDao.updateById(coupon);

                log.info("用户领取优惠券成功: userId={}, couponId={}", userId, couponId);
                return CommentResult.ok("领取成功");
            } else {
                return CommentResult.error("领取失败");
            }
        } catch (Exception e) {
            log.error("领取优惠券失败: userId={}, couponId={}, error={}", userId, couponId, e.getMessage(), e);
            return CommentResult.error("领取失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult getUserCoupons(Long userId, Integer status, Integer pageNum, Integer pageSize) {
        try {
            // 分页参数
            if (pageNum == null || pageNum < 1) {
                pageNum = 1;
            }
            if (pageSize == null || pageSize < 1) {
                pageSize = 20;
            }

            Page<YsUserCoupon> page = new Page<>(pageNum, pageSize);
            QueryWrapper<YsUserCoupon> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);

            // 状态筛选
            if (status != null) {
                queryWrapper.eq("status", status);
            }

            queryWrapper.orderByDesc("get_time");

            // 分页查询
            IPage<YsUserCoupon> couponPage = userCouponDao.selectPage(page, queryWrapper);

            // 查询优惠券详情
            List<YsUserCoupon> userCoupons = couponPage.getRecords();
            for (YsUserCoupon userCoupon : userCoupons) {
                YsCoupon coupon = couponDao.selectById(userCoupon.getCouponId());
                userCoupon.setCoupon(coupon);

                // 检查是否过期
                if (new Date().after(userCoupon.getExpireTime()) && userCoupon.getStatus() == 0) {
                    userCoupon.setStatus(2); // 标记为已过期
                }
            }

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("list", userCoupons);
            result.put("total", couponPage.getTotal());
            result.put("pageNum", couponPage.getCurrent());
            result.put("pageSize", couponPage.getSize());
            result.put("pages", couponPage.getPages());

            return CommentResult.ok(result);
        } catch (Exception e) {
            log.error("获取用户优惠券失败: userId={}, error={}", userId, e.getMessage(), e);
            return CommentResult.error("获取优惠券失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult getAvailableCoupons(Long userId, BigDecimal amount, String goodsIds) {
        try {
            // 查询用户可用的优惠券
            QueryWrapper<YsUserCoupon> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            queryWrapper.eq("status", 0); // 未使用
            queryWrapper.gt("expire_time", new Date()); // 未过期

            List<YsUserCoupon> userCoupons = userCouponDao.selectList(queryWrapper);

            // 过滤可用的优惠券
            List<YsUserCoupon> availableCoupons = new java.util.ArrayList<>();
            for (YsUserCoupon userCoupon : userCoupons) {
                YsCoupon coupon = couponDao.selectById(userCoupon.getCouponId());
                userCoupon.setCoupon(coupon);

                // 检查最低消费金额
                BigDecimal minAmount = coupon.getMinAmount() != null ? coupon.getMinAmount() : BigDecimal.ZERO;
                if (amount != null && amount.compareTo(minAmount) >= 0) {
                    availableCoupons.add(userCoupon);
                }
            }

            return CommentResult.ok(availableCoupons);
        } catch (Exception e) {
            log.error("获取可用优惠券失败: userId={}, error={}", userId, e.getMessage(), e);
            return CommentResult.error("获取优惠券失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult useCoupon(Long userId, Long couponId, Long orderId) {
        try {
            // 查询用户优惠券
            QueryWrapper<YsUserCoupon> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId).eq("coupon_id", couponId).eq("status", 0);
            YsUserCoupon userCoupon = userCouponDao.selectOne(queryWrapper);

            if (userCoupon == null) {
                return CommentResult.error("优惠券不存在或已使用");
            }

            // 更新优惠券状态为已使用
            userCoupon.setStatus(1);
            userCoupon.setOrderId(orderId);
            userCoupon.setUseTime(new Date());
            userCouponDao.updateById(userCoupon);

            log.info("使用优惠券成功: userId={}, couponId={}, orderId={}", userId, couponId, orderId);
            return CommentResult.ok("优惠券使用成功");
        } catch (Exception e) {
            log.error("使用优惠券失败: userId={}, couponId={}, error={}", userId, couponId, e.getMessage(), e);
            return CommentResult.error("使用失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult getCouponDetail(Long couponId) {
        try {
            YsCoupon coupon = couponDao.selectById(couponId);
            if (coupon == null) {
                return CommentResult.error("优惠券不存在");
            }

            return CommentResult.ok(coupon);
        } catch (Exception e) {
            log.error("获取优惠券详情失败: couponId={}, error={}", couponId, e.getMessage(), e);
            return CommentResult.error("获取详情失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult getCouponList(Map<String, Object> params) {
        try {
            Integer pageNum = (Integer) params.get("pageNum");
            Integer pageSize = (Integer) params.get("pageSize");

            if (pageNum == null || pageNum < 1) {
                pageNum = 1;
            }
            if (pageSize == null || pageSize < 1) {
                pageSize = 20;
            }

            Page<YsCoupon> page = new Page<>(pageNum, pageSize);
            QueryWrapper<YsCoupon> queryWrapper = new QueryWrapper<>();
            queryWrapper.orderByDesc("created_at");

            IPage<YsCoupon> couponPage = couponDao.selectPage(page, queryWrapper);

            Map<String, Object> result = new HashMap<>();
            result.put("list", couponPage.getRecords());
            result.put("total", couponPage.getTotal());
            result.put("pageNum", couponPage.getCurrent());
            result.put("pageSize", couponPage.getSize());
            result.put("pages", couponPage.getPages());

            return CommentResult.ok(result);
        } catch (Exception e) {
            log.error("获取优惠券列表失败: error={}", e.getMessage(), e);
            return CommentResult.error("获取列表失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult updateCoupon(Map<String, Object> params) {
        try {
            Long couponId = Long.valueOf(params.get("id").toString());
            YsCoupon coupon = couponDao.selectById(couponId);

            if (coupon == null) {
                return CommentResult.error("优惠券不存在");
            }

            if (params.get("name") != null) coupon.setName(params.get("name").toString());
            if (params.get("type") != null) coupon.setType(Integer.valueOf(params.get("type").toString()));
            if (params.get("discountAmount") != null) coupon.setDiscountAmount(new BigDecimal(params.get("discountAmount").toString()));
            if (params.get("minAmount") != null) coupon.setMinAmount(new BigDecimal(params.get("minAmount").toString()));
            if (params.get("totalCount") != null) coupon.setTotalCount(Integer.valueOf(params.get("totalCount").toString()));
            if (params.get("perUserLimit") != null) coupon.setPerUserLimit(Integer.valueOf(params.get("perUserLimit").toString()));
            if (params.get("validStartTime") != null) coupon.setValidStartTime((Date) params.get("validStartTime"));
            if (params.get("validEndTime") != null) coupon.setValidEndTime((Date) params.get("validEndTime"));
            if (params.get("status") != null) coupon.setStatus(Integer.valueOf(params.get("status").toString()));
            coupon.setUpdatedAt(new Date());

            int result = couponDao.updateById(coupon);
            if (result > 0) {
                return CommentResult.ok("更新成功");
            } else {
                return CommentResult.error("更新失败");
            }
        } catch (Exception e) {
            log.error("更新优惠券失败: error={}", e.getMessage(), e);
            return CommentResult.error("更新失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult deleteCoupon(Long couponId) {
        try {
            int result = couponDao.deleteById(couponId);
            if (result > 0) {
                return CommentResult.ok("删除成功");
            } else {
                return CommentResult.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除优惠券失败: couponId={}, error={}", couponId, e.getMessage(), e);
            return CommentResult.error("删除失败: " + e.getMessage());
        }
    }
}
