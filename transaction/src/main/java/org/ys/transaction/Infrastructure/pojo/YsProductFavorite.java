package org.ys.transaction.Infrastructure.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * (YsProductFavorite)表实体类
 * 商品收藏表
 *
 * @author makejava
 * @since 2025-07-16
 */
@Data
@TableName("ys_product_favorite")
public class YsProductFavorite {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long goodsId;

    private Date createdAt;

}
