package org.ys.transaction.Infrastructure.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * (YsShoppingHistory)表实体类
 *
 * @author makejava
 * @since 2025-07-16 19:41:25
 */
@SuppressWarnings("serial")
@TableName("ys_shopping_history")
@Data
public class YsShoppingHistory  {
//订单ID
@TableId(value = "id", type = IdType.INPUT)
    private Long id;
//用户id
    private Long userId;
//商品id
    private Long goodsId;


}

