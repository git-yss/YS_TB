package org.ys.commens.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ys.commens.enums.OrderStatusEnum;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;         // 订单 ID
    private long itemId;         // 商品SKU ID
    private BigDecimal price;   // 商品价格
    private Integer num;   // 数量
    private long userId;       // 用户
    private int statusEnum;       // 商品图片

    public CartItem(long itemId, BigDecimal price, int num) {
        this.itemId = itemId;
        this.price = price;
        this.num = num;
    }

}
