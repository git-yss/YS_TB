package org.ys.transaction.service.impl;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ys.commens.dao.YsUserDao;
import org.ys.commens.entity.YsUser;
import org.ys.commens.pojo.CommentResult;
import org.ys.commens.utils.PasswordEncoderUtil;
import org.ys.transaction.service.UserService;

import javax.annotation.Resource;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务实现类
 *
 * @author makejava
 * @since 2025-07-16
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserServiceImpl.class);

    @Resource
    private YsUserDao userDao;

    @Resource
    private JavaMailSender mailSender;

    @Override
    public CommentResult register(Map<String, Object> params) {
        try {
            String username = (String) params.get("username");
            String password = (String) params.get("password");
            String email = (String) params.get("email");
            String tel = (String) params.get("tel");

            // 参数校验
            if (username == null || username.isEmpty()) {
                return CommentResult.error("用户名不能为空");
            }
            if (password == null || password.length() < 6) {
                return CommentResult.error("密码长度不能少于6位");
            }
            if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                return CommentResult.error("邮箱格式不正确");
            }

            // 检查用户名是否已存在
            YsUser existUser = userDao.selectByName(username);
            if (existUser != null) {
                return CommentResult.error("用户名已存在");
            }

            // 检查邮箱是否已存在
            existUser = userDao.selectByEmail(email);
            if (existUser != null) {
                return CommentResult.error("邮箱已被注册");
            }

            // 创建新用户
            YsUser user = new YsUser();
            user.setUsername(username);
            // 密码加密
            String encodedPassword = PasswordEncoderUtil.encode(password);
            user.setPassword(encodedPassword);
            user.setEmail(email);
            user.setTel(tel);
            user.setBalance(java.math.BigDecimal.ZERO);
            user.setStatus(1);
            user.setRegisterTime(new java.util.Date());

            // 生成用户ID（简单实现，实际应该使用IDUtils）
            user.setId(System.currentTimeMillis());

            // 保存用户
            int result = userDao.insert(user);
            if (result > 0) {
                log.info("用户注册成功: username={}", username);
                return CommentResult.ok("注册成功");
            } else {
                return CommentResult.error("注册失败");
            }
        } catch (Exception e) {
            log.error("用户注册失败: {}", e.getMessage(), e);
            return CommentResult.error("注册失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult sendPasswordResetEmail(String email) {
        try {
            // 检查邮箱是否存在
            YsUser user = userDao.selectByEmail(email);
            if (user == null) {
                return CommentResult.error("该邮箱未注册");
            }

            // 生成重置令牌（有效期30分钟）
            String token = generateResetToken(user.getId());

            // 发送邮件
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("密码重置");
            message.setText("您好，\n\n" +
                    "您请求重置密码，请点击以下链接重置密码：\n" +
                    "http://localhost:8080/reset-password?token=" + token + "\n\n" +
                    "该链接30分钟后失效，请尽快重置。\n\n" +
                    "如果您没有请求重置密码，请忽略此邮件。");

            mailSender.send(message);

            log.info("密码重置邮件已发送: email={}", email);
            return CommentResult.ok("重置邮件已发送到您的邮箱，请查收");
        } catch (Exception e) {
            log.error("发送密码重置邮件失败: {}", e.getMessage(), e);
            return CommentResult.error("发送邮件失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult resetPassword(String token, String newPassword) {
        try {
            // 验证令牌并获取用户ID
            Long userId = validateResetToken(token);
            if (userId == null) {
                return CommentResult.error("重置链接已失效或无效");
            }

            // 参数校验
            if (newPassword == null || newPassword.length() < 6) {
                return CommentResult.error("密码长度不能少于6位");
            }

            // 更新密码
            YsUser user = userDao.selectById(userId);
            if (user == null) {
                return CommentResult.error("用户不存在");
            }

            String encodedPassword = PasswordEncoderUtil.encode(newPassword);
            user.setPassword(encodedPassword);
            userDao.updateById(user);

            // 标记令牌已使用
            markTokenUsed(token);

            log.info("密码重置成功: userId={}", userId);
            return CommentResult.ok("密码重置成功");
        } catch (Exception e) {
            log.error("密码重置失败: {}", e.getMessage(), e);
            return CommentResult.error("密码重置失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult changePassword(Long userId, String oldPassword, String newPassword) {
        try {
            // 参数校验
            if (newPassword == null || newPassword.length() < 6) {
                return CommentResult.error("新密码长度不能少于6位");
            }

            // 查询用户
            YsUser user = userDao.selectById(userId);
            if (user == null) {
                return CommentResult.error("用户不存在");
            }

            // 验证旧密码
            if (!PasswordEncoderUtil.matches(oldPassword, user.getPassword())) {
                return CommentResult.error("旧密码错误");
            }

            // 更新密码
            String encodedPassword = PasswordEncoderUtil.encode(newPassword);
            user.setPassword(encodedPassword);
            userDao.updateById(user);

            log.info("密码修改成功: userId={}", userId);
            return CommentResult.ok("密码修改成功");
        } catch (Exception e) {
            log.error("密码修改失败: {}", e.getMessage(), e);
            return CommentResult.error("密码修改失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult updateUserInfo(Map<String, Object> params) {
        try {
            Long userId = Long.valueOf(params.get("userId").toString());
            String age = (String) params.get("age");
            String sex = (String) params.get("sex");

            // 查询用户
            YsUser user = userDao.selectById(userId);
            if (user == null) {
                return CommentResult.error("用户不存在");
            }

            // 更新用户信息
            if (age != null) {
                user.setAge(age);
            }
            if (sex != null) {
                user.setSex(sex);
            }

            userDao.updateById(user);

            log.info("用户信息更新成功: userId={}", userId);
            return CommentResult.ok("信息更新成功");
        } catch (Exception e) {
            log.error("用户信息更新失败: {}", e.getMessage(), e);
            return CommentResult.error("更新失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult updateAvatar(Long userId, String avatarUrl) {
        try {
            // 查询用户
            YsUser user = userDao.selectById(userId);
            if (user == null) {
                return CommentResult.error("用户不存在");
            }

            // 更新头像
            user.setAvatar(avatarUrl);
            userDao.updateById(user);

            log.info("头像更新成功: userId={}", userId);
            return CommentResult.ok("头像更新成功");
        } catch (Exception e) {
            log.error("头像更新失败: {}", e.getMessage(), e);
            return CommentResult.error("更新失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult getUserInfo(Long userId) {
        try {
            YsUser user = userDao.selectById(userId);
            if (user == null) {
                return CommentResult.error("用户不存在");
            }

            // 返回用户信息（不包含密码）
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("age", user.getAge());
            userInfo.put("sex", user.getSex());
            userInfo.put("balance", user.getBalance());
            userInfo.put("email", user.getEmail());
            userInfo.put("tel", user.getTel());
            userInfo.put("avatar", user.getAvatar());
            userInfo.put("registerTime", user.getRegisterTime());
            userInfo.put("lastLoginTime", user.getLastLoginTime());
            userInfo.put("status", user.getStatus());

            return CommentResult.ok(userInfo);
        } catch (Exception e) {
            log.error("获取用户信息失败: {}", e.getMessage(), e);
            return CommentResult.error("获取信息失败: " + e.getMessage());
        }
    }

    /**
     * 生成密码重置令牌
     */
    private String generateResetToken(Long userId) {
        // 简单实现：使用Base64编码用户ID和时间戳
        String tokenData = userId + ":" + System.currentTimeMillis() + ":" + generateRandomString(16);
        return Base64.getEncoder().encodeToString(tokenData.getBytes());
    }

    /**
     * 验证密码重置令牌
     */
    private Long validateResetToken(String token) {
        try {
            String decoded = new String(Base64.getDecoder().decode(token));
            String[] parts = decoded.split(":");
            if (parts.length != 3) {
                return null;
            }

            Long userId = Long.parseLong(parts[0]);
            long timestamp = Long.parseLong(parts[1]);

            // 检查令牌是否过期（30分钟）
            long currentTime = System.currentTimeMillis();
            if (currentTime - timestamp > 30 * 60 * 1000) {
                return null;
            }

            return userId;
        } catch (Exception e) {
            log.error("验证令牌失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 标记令牌已使用
     */
    private void markTokenUsed(String token) {
        // 实际应用中应该将令牌存储在Redis或数据库中并标记为已使用
        log.info("令牌已标记为已使用: token={}", token);
    }

    /**
     * 生成随机字符串
     */
    private String generateRandomString(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
