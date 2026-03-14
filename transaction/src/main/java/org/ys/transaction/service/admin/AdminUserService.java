package org.ys.transaction.service.admin;

import org.ys.commens.pojo.CommentResult;

import java.util.List;
import java.util.Map;

/**
 * 后台用户管理服务接口
 */
public interface AdminUserService {

    /**
     * 后台登录
     */
    CommentResult adminLogin(String username, String password);

    /**
     * 获取用户列表（分页）
     */
    CommentResult getUserList(String keyword, String status, Integer pageNum, Integer pageSize);

    /**
     * 获取用户详情
     */
    CommentResult getUserDetail(Long id);

    /**
     * 封禁用户
     */
    CommentResult banUser(Long id, String reason);

    /**
     * 解封用户
     */
    CommentResult unbanUser(Long id);

    /**
     * 更新用户余额
     */
    CommentResult updateBalance(Long userId, Double amount, String operation, String remark);

    /**
     * 获取用户订单列表
     */
    CommentResult getUserOrders(Long id, Integer pageNum, Integer pageSize);

    /**
     * 获取用户统计信息
     */
    CommentResult getUserStatistics();

    /**
     * 批量封禁用户
     */
    CommentResult batchBanUsers(List<Long> userIds);

    /**
     * 批量解封用户
     */
    CommentResult batchUnbanUsers(List<Long> userIds);
}
