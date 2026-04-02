package org.ys.transaction.domain.aggregate;

import lombok.Getter;
import org.ys.transaction.domain.entity.YsUser;
import org.ys.transaction.domain.entity.YsUserAddr;

import java.util.Collections;
import java.util.List;

@Getter
public class UserAggregate {
    private final YsUser user;
    private final List<YsUserAddr> addresses;

    public UserAggregate(YsUser user, List<YsUserAddr> addresses) {
        if (user == null) {
            throw new IllegalArgumentException("user cannot be null");
        }
        this.user = user;
        this.addresses = addresses == null ? Collections.emptyList() : Collections.unmodifiableList(addresses);
    }

    public void checkPassword(String password) {
        if (user == null) {
            throw new IllegalStateException("账号或密码错误");
        }
        if (!password.equals(user.getPassword())) {
            throw new IllegalStateException("账号或密码错误");
        }
        if (!"1".equals(user.getStatus())) {
            throw new IllegalStateException("账号已封禁");
        }
    }

    public void checkNameDump() {
        if (user != null) {
            throw new IllegalStateException("用户名已存在");
        }
    }
    public void isEmp() {
        if (user == null) {
            throw new IllegalStateException("用户不存在");
        }
    }

    public void checkEmailDump(String email) {
        if (user != null && email.equals(user.getEmail())) {
            throw new IllegalStateException("邮箱已存在");
        }
    }

    public void checkBaseInfo() {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().length() < 6) {
            throw new IllegalArgumentException("密码长度不能少于6位");
        }
        if (user.getEmail() == null || !user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }

    }

    public void checkPwsInfo(String newPassword) {

        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("密码长度不能少于6位");
        }


    }
}
