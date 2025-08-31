package org.ys.commens.entity;

import java.math.BigDecimal;

/**
 * (YsUser)表实体类
 *
 * @author makejava
 * @since 2025-07-16 19:41:46
 */
@SuppressWarnings("serial")
public class YsUser  {
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


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

}

