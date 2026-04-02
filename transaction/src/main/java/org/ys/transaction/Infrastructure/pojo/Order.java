package org.ys.transaction.Infrastructure.pojo;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.ys.transaction.domain.entity.YsUserAddr;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * (YsOrder)表实体类
 *
 * @author makejava
 * @since 2025-08-31 12:34:48
 */

@Data
public class Order {
    /** 订单 ID */
    private String orderId;
    // 以下为查询扩展字段 (来自关联表)
    /** 地址 */
    private String addr;
    /** 用户名 */
    private String username;
    /** 邮箱 */
    private String email;
    /** 电话 */
    private String tel;
    /** 商品介绍 */
    private String introduce;




}
