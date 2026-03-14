package org.ys.transaction.controller.admin;

import org.ys.commens.pojo.CommentResult;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

/**
 * 营销活动管理控制器
 */
@RestController
@RequestMapping("/admin/marketing")
public class AdminMarketingController {

    @Resource
    private org.ys.transaction.service.admin.AdminCouponService adminCouponService;

    @Resource
    private org.ys.transaction.service.admin.AdminSeckillService adminSeckillService;

    /**
     * ========== 优惠券管理 ==========
     */

    /**
     * 获取优惠券列表
     */
    @PostMapping("/coupon/list")
    public CommentResult getCouponList(@RequestBody Map<String, Object> params) {
        Integer pageNum = params.get("pageNum") != null ? Integer.parseInt(params.get("pageNum").toString()) : 1;
        Integer pageSize = params.get("pageSize") != null ? Integer.parseInt(params.get("pageSize").toString()) : 10;
        return adminCouponService.getCouponList(pageNum, pageSize);
    }

    /**
     * 创建优惠券
     */
    @PostMapping("/coupon/create")
    public CommentResult createCoupon(@RequestBody Map<String, Object> params) {
        return adminCouponService.createCoupon(params);
    }

    /**
     * 更新优惠券
     */
    @PostMapping("/coupon/update")
    public CommentResult updateCoupon(@RequestBody Map<String, Object> params) {
        return adminCouponService.updateCoupon(params);
    }

    /**
     * 删除优惠券
     @PostMapping("/coupon/delete/{id}")
     public CommentResult deleteCoupon(@PathVariable Long id) {
        return adminCouponService.deleteCoupon(id);
    }

    /**
     * 发放优惠券
     */
    @PostMapping("/coupon/distribute")
    public CommentResult distributeCoupon(@RequestBody Map<String, Object> params) {
        return adminCouponService.distributeCoupon(params);
    }

    /**
     * 获取优惠券使用统计
     */
    @GetMapping("/coupon/statistics")
    public CommentResult getCouponStatistics() {
        return adminCouponService.getCouponStatistics();
    }

    /**
     * ========== 秒杀管理 ==========
     */

    /**
     * 获取秒杀活动列表
     */
    @PostMapping("/seckill/list")
    public CommentResult getSeckillList(@RequestBody Map<String, Object> params) {
        Integer pageNum = params.get("pageNum") != null ? Integer.parseInt(params.get("pageNum").toString()) : 1;
        Integer pageSize = params.get("pageSize") != null ? Integer.parseInt(params.get("pageSize").toString()) : 10;
        return adminSeckillService.getSeckillList(pageNum, pageSize);
    }

    /**
     * 创建秒杀活动
     */
    @PostMapping("/seckill/create")
    public CommentResult createSeckill(@RequestBody Map<String, Object> params) {
        return adminSeckillService.createSeckill(params);
    }

    /**
     * 更新秒杀活动
     */
    @PostMapping("/seckill/update")
    public CommentResult updateSeckill(@RequestBody Map<String, Object> params) {
        return adminSeckillService.updateSeckill(params);
    }

    /**
     * 删除秒杀活动
     */
    @PostMapping("/seckill/delete/{id}")
    public CommentResult deleteSeckill(@PathVariable Long id) {
        return adminSeckillService.deleteSeckill(id);
    }

    /**
     * 启用秒杀活动
     */
    @PostMapping("/seckill/enable/{id}")
    public CommentResult enableSeckill(@PathVariable Long id) {
        return adminSeckillService.updateSeckillStatus(id, 1);
    }

    /**
     * 禁用秒杀活动
     */
    @PostMapping("/seckill/disable/{id}")
    public CommentResult disableSeckill(@PathVariable Long id) {
        return adminSeckillService.updateSeckillStatus(id, 0);
    }

    /**
     * 获取秒杀统计数据
     */
    @GetMapping("/seckill/statistics")
    public CommentResult getSeckillStatistics() {
        return adminSeckillService.getSeckillStatistics();
    }
}
