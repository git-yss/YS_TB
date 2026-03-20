package org.ys.commens.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

/**
 * (YsProductFavorite)表实体类
 * 商品收藏表
 *
 * @author makejava
 * @since 2025-07-16
 */
@TableName("ys_product_favorite")
public class YsProductFavorite {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long goodsId;

    private Date createdAt;

    /** 关联商品详情（非表字段，用于列表展示） */
    private transient YsGoods goods;

    // Getters and Setters
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

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public YsGoods getGoods() {
        return goods;
    }

    public void setGoods(YsGoods goods) {
        this.goods = goods;
    }
}
