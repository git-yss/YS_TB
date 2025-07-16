package org.ys.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;

import java.io.Serializable;

/**
 * (YsGoods)表实体类
 *
 * @author makejava
 * @since 2025-07-16 19:37:18
 */
@SuppressWarnings("serial")
public class YsGoods  {
    //商品id
    private Integer id;
    //用户id
    private Integer userId;
    //商品名称
    private String name;
    //商品介绍
    private String introduce;
    //单价
    private Double price;
    //库存
    private Integer inventory;
    //图片地址
    private String picture;


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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIntroduce() {
        return introduce;
    }

    public void setIntroduce(String introduce) {
        this.introduce = introduce;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getInventory() {
        return inventory;
    }

    public void setInventory(Integer inventory) {
        this.inventory = inventory;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }


}

