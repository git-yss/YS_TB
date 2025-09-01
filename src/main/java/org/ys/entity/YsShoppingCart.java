package org.ys.entity;


import com.baomidou.mybatisplus.extension.activerecord.Model;

import java.io.Serializable;

/**
 * (YsShoppingCart)表实体类
 *
 * @author makejava
 * @since 2025-09-01 22:31:38
 */
@SuppressWarnings("serial")
public class YsShoppingCart extends Model<YsShoppingCart> {
    //ID
    private Integer id;
    //用户id
    private Integer userId;
    //商品id
    private String goodsId;


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

    public String getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(String goodsId) {
        this.goodsId = goodsId;
    }

}

