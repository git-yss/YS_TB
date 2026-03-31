package org.ys.transaction.Infrastructure.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * (YsCategory)表实体类
 * 商品分类表
 *
 * @author makejava
 * @since 2025-07-16
 */
@Data
@TableName("ys_category")
public class YsCategory {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long parentId;

    private String categoryName;

    private String categoryCode;

    private Integer sortOrder;

    private String icon;

    private String description;

    private Date createdAt;

    private Date updatedAt;

    private Integer status;


}
