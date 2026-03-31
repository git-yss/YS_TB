package org.ys.transaction.domain.impl;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ys.transaction.domain.inteface.UserService;
import org.ys.transaction.Infrastructure.dao.YsUserDao;
import org.ys.transaction.Infrastructure.pojo.YsUser;
import org.ys.transaction.Infrastructure.utils.PasswordEncoderUtil;

import javax.annotation.Resource;
import java.security.SecureRandom;
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
    public void register(Map<String, Object> params) {
        String username = (String) params.get("username");
        String password = (String) params.get("password");
        String email = (String) params.get("email");
        String tel = (String) params.get("tel");

        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("密码长度不能少于6位");
        }
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }

        YsUser existUser = userDao.selectByName(username);
        if (existUser != null) {
            throw new IllegalStateException("用户名已存在");
        }
        existUser = userDao.selectByEmail(email);
        if (existUser != null) {
            throw new IllegalStateException("邮箱已被注册");
        }

        YsUser user = new YsUser();
        user.setUsername(username);
        user.setPassword(PasswordEncoderUtil.encode(password));
        user.setEmail(email);
        user.setTel(tel);
        user.setBalance(java.math.BigDecimal.ZERO);
        user.setStatus(String.valueOf(1));
        user.setId(System.currentTimeMillis());

        int result = userDao.insert(user);
        if (result <= 0) {
            throw new IllegalStateException("注册失败");
        }
        log.info("用户注册成功: username={}", username);
    }

    @Override
    public void sendPasswordResetEmail(String email) {
        YsUser user = userDao.selectByEmail(email);
        if (user == null) {
            throw new IllegalStateException("该邮箱未注册");
        }
        String token = generateResetToken(user.getId());
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
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        Long userId = validateResetToken(token);
        if (userId == null) {
            throw new IllegalStateException("重置链接已失效或无效");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("密码长度不能少于6位");
        }
        YsUser user = userDao.selectById(userId);
        if (user == null) {
            throw new IllegalStateException("用户不存在");
        }
        user.setPassword(PasswordEncoderUtil.encode(newPassword));
        userDao.updateById(user);
        markTokenUsed(token);
        log.info("密码重置成功: userId={}", userId);
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("新密码长度不能少于6位");
        }
        YsUser user = userDao.selectById(userId);
        if (user == null) {
            throw new IllegalStateException("用户不存在");
        }
        if (!PasswordEncoderUtil.matches(oldPassword, user.getPassword())) {
            throw new IllegalStateException("旧密码错误");
        }
        user.setPassword(PasswordEncoderUtil.encode(newPassword));
        userDao.updateById(user);
        log.info("密码修改成功: userId={}", userId);
    }

    @Override
    public void updateUserInfo(Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        String age = (String) params.get("age");
        String sex = (String) params.get("sex");
        YsUser user = userDao.selectById(userId);
        if (user == null) {
            throw new IllegalStateException("用户不存在");
        }
        if (age != null) user.setAge(age);
        if (sex != null) user.setSex(sex);
        userDao.updateById(user);
        log.info("用户信息更新成功: userId={}", userId);
    }

    @Override
    public void updateAvatar(Long userId, String avatarUrl) {
        YsUser user = userDao.selectById(userId);
        if (user == null) {
            throw new IllegalStateException("用户不存在");
        }
        userDao.updateById(user);
        log.info("头像更新成功: userId={}", userId);
    }

    @Override
    public Map<String, Object> getUserInfo(Long userId) {
        YsUser user = userDao.selectById(userId);
        if (user == null) {
            throw new IllegalStateException("用户不存在");
        }
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("age", user.getAge());
        userInfo.put("sex", user.getSex());
        userInfo.put("balance", user.getBalance());
        userInfo.put("email", user.getEmail());
        userInfo.put("tel", user.getTel());
        userInfo.put("status", user.getStatus());
        return userInfo;
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
