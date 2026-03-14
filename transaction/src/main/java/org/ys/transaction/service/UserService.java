package org.ys.transaction.service;

import org.ys.commens.pojo.CommentResult;

import java.util.Map;

/**
 * 用户服务接口
 *
 * @author makejava
 * @since 2025-07-16
 */
public interface UserService {

    /**
     * 用户注册
     * @param params 注册参数（用户名、密码、邮箱、手机号等）
     * @return 注册结果
     */
    CommentResult register(Map<String, Object> params);

    /**
     * 发送密码重置邮件
     * @param email 邮箱地址
     * @return 发送结果
     */
    CommentResult sendPasswordResetEmail(String email);

    /**
     * 重置密码
     * @param token 重置令牌
     * @param newPassword 新密码
     * @return 重置结果
     */
    CommentResult resetPassword(String token, String newPassword);

    /**
     * 修改密码
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改结果
     */
    CommentResult changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 更新用户信息
     * @param params 用户信息
     * @return 更新结果
     */
    CommentResult updateUserInfo(Map<String, Object> params);

    /**
     * 更新头像
     * @param userId 用户ID
     * @param avatarUrl 头像URL
     * @return 更新结果
     */
    CommentResult updateAvatar(Long userId, String avatarUrl);

    /**
     * 获取用户详情
     * @param userId 用户ID
     * @return 用户信息
     */
    CommentResult getUserInfo(Long userId);
}
