package org.ys.transaction.domain.inteface.admin;

import org.ys.transaction.domain.vo.DomainResult;

import java.util.List;

/**
 * 后台用户管理服务接口
 */
public interface AdminUserService {

    /**
     * 后台登录
     */
    DomainResult adminLogin(String username, String password);

    /**
     * 获取用户列表（分页）
     */
    DomainResult getUserList(String keyword, String status, Integer pageNum, Integer pageSize);

    /**
     * 获取用户详情
     */
    DomainResult getUserDetail(Long id);

    /**
     * 封禁用户
     */
    DomainResult banUser(Long id, String reason);

    /**
     * 解封用户
     */
    DomainResult unbanUser(Long id);

    /**
     * 更新用户余额
     */
    DomainResult updateBalance(Long userId, Double amount, String operation, String remark);

    /**
     * 获取用户订单列表
     */
    DomainResult getUserOrders(Long id, Integer pageNum, Integer pageSize);

    /**
     * 获取用户统计信息
     */
    DomainResult getUserStatistics();

    /**
     * 批量封禁用户
     */
    DomainResult batchBanUsers(List<Long> userIds);

    /**
     * 批量解封用户
     */
    DomainResult batchUnbanUsers(List<Long> userIds);
}
