package org.ys.transaction.Infrastructure.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * (YsUserAddr)表实体类
 *
 * @author makejava
 * @since 2025-07-16 19:41:46
 */
@SuppressWarnings("serial")
@TableName("ys_user_addr")
@Data
public class YsUserAddr{
    //ID
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;
    //用户id
    private Long userId;
    //地址
    private String addr;
}

