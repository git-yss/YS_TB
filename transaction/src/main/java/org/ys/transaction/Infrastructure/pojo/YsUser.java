package org.ys.transaction.Infrastructure.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * (YsUser)表实体类
 *
 * @author makejava
 * @since 2025-07-16 19:41:46
 */
@SuppressWarnings("serial")
@TableName("ys_user")
@Data
public class YsUser  {
    //用户ID
    @TableId(value = "id", type = IdType.INPUT)
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


}

