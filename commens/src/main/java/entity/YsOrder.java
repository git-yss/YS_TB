package entity;


import com.baomidou.mybatisplus.extension.activerecord.Model;

import java.io.Serializable;
import java.util.Date;

/**
 * (YsOrder)表实体类
 *
 * @author makejava
 * @since 2025-09-01 22:31:36
 */
@SuppressWarnings("serial")
public class YsOrder extends Model<YsOrder> {
    //ID
    private Integer id;
    //用户id
    private Integer userId;
    //商品id
    private String goodsId;
    //订单状态
    private String status;
    //订单时间
    private Date addtime;


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

    public Date getAddtime() {
        return addtime;
    }

    public void setAddtime(Date addtime) {
        this.addtime = addtime;
    }

}

