package org.ys.commens.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (YsShoppingHistory)表实体类
 *
 * @author makejava
 * @since 2025-07-16 19:41:25
 */
@SuppressWarnings("serial")
@TableName("ys_shopping_history")
public class YsShoppingHistory  {
//订单ID
@TableId(value = "id", type = IdType.INPUT)
    private Long id;
//用户id
    private Long userId;
//商品id
    private Long goodsId;


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


}

