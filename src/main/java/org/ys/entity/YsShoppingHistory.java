package org.ys.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.io.Serializable;

/**
 * (YsShoppingHistory)表实体类
 *
 * @author makejava
 * @since 2025-07-16 19:41:25
 */
@SuppressWarnings("serial")
public class YsShoppingHistory extends Model<YsShoppingHistory> {
//订单ID
    private Integer id;
//用户id
    private Integer userId;
//商品id
    private String goodsId;
//订单状态
    private String status;


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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}

