package org.ys.transaction.controller.admin;

import org.ys.commens.entity.YsUser;
import org.ys.commens.pojo.CommentResult;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/admin/user")
public class AdminUserController {

    @Resource
    private org.ys.transaction.service.admin.AdminUserService adminUserService;

    /**
     * 获取用户列表（分页）
     */
    @PostMapping("/list")
    public CommentResult getUserList(@RequestBody Map<String, Object> params) {
        String keyword = params.get("keyword") != null ? params.get("keyword").toString() : null;
        String status = params.get("status") != null ? params.get("status").toString() : null;
        Integer pageNum = params.get("pageNum") != null ? Integer.parseInt(params.get("pageNum").toString()) : 1;
        Integer pageSize = params.get("pageSize") != null ? Integer.parseInt(params.get("pageSize").toString()) : 10;
        return adminUserService.getUserList(keyword, status, pageNum, pageSize);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/detail/{id}")
    public CommentResult getUserDetail(@PathVariable Long id) {
        return adminUserService.getUserDetail(id);
    }

    /**
     * 封禁用户
     */
    @PostMapping("/ban/{id}")
    public CommentResult banUser(@PathVariable Long id, @RequestBody Map<String, String> params) {
        String reason = params.get("reason");
        return adminUserService.banUser(id, reason);
    }

    /**
     * 解封用户
     */
    @PostMapping("/unban/{id}")
    public CommentResult unbanUser(@PathVariable Long id) {
        return adminUserService.unbanUser(id);
    }

    /**
     * 更新用户余额
     */
    @PostMapping("/balance")
    public CommentResult updateBalance(@RequestBody Map<String, Object> params) {
        Long userId = Long.parseLong(params.get("userId").toString());
        Double amount = Double.parseDouble(params.get("amount").toString());
        String operation = params.get("operation") != null ? params.get("operation").toString() : "add";
        String remark = params.get("remark") != null ? params.get("remark").toString() : null;
        return adminUserService.updateBalance(userId, amount, operation, remark);
    }

    /**
     * 获取用户订单列表
     */
    @PostMapping("/{id}/orders")
    public CommentResult getUserOrders(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Integer pageNum = params.get("pageNum") != null ? Integer.parseInt(params.get("pageNum").toString()) : 1;
        Integer pageSize = params.get("pageSize") != null ? Integer.parseInt(params.get("pageSize").toString()) : 10;
        return adminUserService.getUserOrders(id, pageNum, pageSize);
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/statistics")
    public CommentResult getUserStatistics() {
        return adminUserService.getUserStatistics();
    }

    /**
     * 批量封禁用户
     */
    @PostMapping("/batchBan")
    public CommentResult batchBanUsers(@RequestBody List<Long> userIds) {
        return adminUserService.batchBanUsers(userIds);
    }

    /**
     * 批量解封用户
     */
    @PostMapping("/batchUnban")
    public CommentResult batchUnbanUsers(@RequestBody List<Long> userIds) {
        return adminUserService.batchUnbanUsers(userIds);
    }
}
