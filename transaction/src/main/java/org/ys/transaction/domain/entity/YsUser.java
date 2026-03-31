package org.ys.transaction.domain.entity;

import lombok.Getter;

import java.math.BigDecimal;

/**
 * (YsUser)表实体类
 *
 * @author makejava
 * @since 2025-07-16 19:41:46
 */

@Getter
public class YsUser {
    //用户ID
    private Long id;
    //用户名
    private String username;
    //密码
    private String password;
    //年龄
    private String age;
    //性别
    private String sex;
    //余额
    private BigDecimal balance;
    //电子邮件
    private String email;
    //电话号码
    private String tel;
    //状态：1=正常，0=封禁
    private String status;
    //注册时间
    private java.time.LocalDateTime createTime;

    private YsUser(Long id,
                   String username,
                   String password,
                   String age,
                   String sex,
                   BigDecimal balance,
                   String email,
                   String tel,
                   String status,
                   java.time.LocalDateTime createTime) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.age = age;
        this.sex = sex;
        this.balance = balance;
        this.email = email;
        this.tel = tel;
        this.status = status;
        this.createTime = createTime;
    }

    public static YsUser rehydrate(Long id,
                                  String username,
                                  String password,
                                  String age,
                                  String sex,
                                  BigDecimal balance,
                                  String email,
                                  String tel,
                                  String status,
                                  java.time.LocalDateTime createTime) {
        return new YsUser(id, username, password, age, sex, balance, email, tel, status, createTime);
    }

    public static YsUser identify(Long id) {
        return new YsUser(id, null, null, null, null, null, null, null, null, null);
    }

    public void register(String username, String password, String email) {
        if (username == null ) {
            throw new IllegalArgumentException("username cannot be empty");
        }
        if (password == null) {
            throw new IllegalArgumentException("password cannot be empty");
        }
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public void changeBalance(BigDecimal newBalance) {
        if (newBalance == null || newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("balance cannot be negative");
        }
        this.balance = newBalance;
    }

    public void debitBalance(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be greater than 0");
        }
        BigDecimal current = balance == null ? BigDecimal.ZERO : balance;
        BigDecimal next = current.subtract(amount);
        if (next.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("insufficient balance");
        }
        this.balance = next;
    }

    public void creditBalance(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be greater than 0");
        }
        BigDecimal current = balance == null ? BigDecimal.ZERO : balance;
        this.balance = current.add(amount);
    }

    public void updateContact(String tel, String email) {
        this.tel = tel;
        this.email = email;
    }

    public void ban() {
        this.status = "0";
    }

    public void enable() {
        this.status = "1";
    }


}

