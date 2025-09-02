package org.ys.commens.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import java.util.Date;

/**
 * (YsOrder)表实体类
 *
 * @author makejava
 * @since 2025-08-31 12:34:48
 */
@SuppressWarnings("serial")
@TableName("ys_order")
public class YsOrder   {
//ID
@TableId(value = "id", type = IdType.INPUT)
    private Long id;
//用户id
    private Long userId;
//商品id
    private Long goodsId;
//订单状态
    private String status;
//订单时间
    private Date addtime;


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

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getAddtime() {
        return addtime;
    }

    public void setAddtime(Date addtime) {
        this.addtime = addtime;
    }

}

