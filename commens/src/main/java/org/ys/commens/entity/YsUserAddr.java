package org.ys.commens.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (YsUserAddr)表实体类
 *
 * @author makejava
 * @since 2025-07-16 19:41:46
 */
@SuppressWarnings("serial")
@TableName("ys_user_addr")
public class YsUserAddr{
    //ID
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;
    //用户id
    private Long userId;
    //地址
    private String addr;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

}

