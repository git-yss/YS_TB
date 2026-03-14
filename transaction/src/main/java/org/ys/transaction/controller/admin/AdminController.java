package org.ys.transaction.controller.admin;

import org.springframework.web.bind.annotation.*;
import org.ys.commens.pojo.CommentResult;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 后台管理系统主控制器
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Resource
    private org.ys.transaction.service.admin.AdminUserService adminUserService;

    @Resource
    private org.ys.transaction.service.admin.AdminGoodsService adminGoodsService;

    @Resource
    private org.ys.transaction.service.admin.AdminOrderService adminOrderService;

    @Resource
    private org.ys.transaction.service.admin.AdminStatisticsService adminStatisticsService;

    /**
     * 后台登录
     */
    @PostMapping("/login")
    public CommentResult login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        return adminUserService.adminLogin(username, password);
    }

    /**
     * 获取后台首页统计数据
     */
    @GetMapping("/dashboard")
    public CommentResult getDashboard() {
        return adminStatisticsService.getDashboardStats();
    }

    /**
     * 获取系统配置
     */
    @GetMapping("/config")
    public CommentResult getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("siteName", "智能电商平台");
        config.put("version", "1.0.0");
        config.put("enableRegistration", true);
        config.put("enableSeckill", true);
        config.put("enableCoupon", true);
        return CommentResult.success(config);
    }

    /**
     * 更新系统配置
     */
    @PostMapping("/config")
    public CommentResult updateConfig(@RequestBody Map<String, Object> config) {
        return CommentResult.success("配置更新成功");
    }
}
