package org.ys.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;

import java.io.Serializable;

/**
 * (YsUserAddr)表实体类
 *
 * @author makejava
 * @since 2025-07-16 19:41:46
 */
@SuppressWarnings("serial")
public class YsUserAddr extends Model<YsUserAddr> {
    //ID
    private Integer id;
    //用户id
    private Integer userId;
    //地址
    private String addr;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

}

