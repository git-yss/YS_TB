package org.ys.transaction.Interface.admin;

import org.springframework.web.bind.annotation.*;
import org.ys.transaction.Interface.VO.CommentResult;


import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 后台管理系统主控制器
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Resource
    private org.ys.transaction.domain.inteface.admin.AdminUserService adminUserService;

    @Resource
    private org.ys.transaction.domain.inteface.admin.AdminGoodsService adminGoodsService;

    @Resource
    private org.ys.transaction.domain.inteface.admin.AdminOrderService adminOrderService;

    @Resource
    private org.ys.transaction.domain.inteface.admin.AdminStatisticsService adminStatisticsService;

    /**
     * 后台登录
     */
    @PostMapping("/login")
    public CommentResult login(@RequestBody Map<String, String> params, HttpServletRequest request) {
        String username = params.get("username");
        String password = params.get("password");
        CommentResult result = adminUserService.adminLogin(username, password);
        // AdminInterceptor 依赖 session 中的 adminUser 判断是否已登录
        if (result != null && result.getStatus() != null && result.getStatus() == 200 && result.getData() != null) {
            request.getSession().setAttribute("adminUser", result.getData());
        }
        return result;
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
