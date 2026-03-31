package org.ys.transaction.domain.entity;

import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.Date;

/**
 * (YsCategory)表实体类
 * 商品分类表
 *
 * @author makejava
 * @since 2025-07-16
 */
@Getter
public class YsCategory {


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

    private YsCategory(Long id, Long parentId, String categoryName, String categoryCode, Integer sortOrder,
                       String icon, String description, Date createdAt, Date updatedAt, Integer status) {
        this.id = id;
        this.parentId = parentId;
        this.categoryName = categoryName;
        this.categoryCode = categoryCode;
        this.sortOrder = sortOrder;
        this.icon = icon;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.status = status;
    }

    public static YsCategory rehydrate(Long id, Long parentId, String categoryName, String categoryCode, Integer sortOrder,
                                       String icon, String description, Date createdAt, Date updatedAt, Integer status) {
        return new YsCategory(id, parentId, categoryName, categoryCode, sortOrder, icon, description, createdAt, updatedAt, status);
    }
    public void rename(String categoryName) {
        if (categoryName == null || StringUtils.isEmpty(categoryName)) {
            throw new IllegalArgumentException("分类名称不能为空");
        }
        this.categoryName = categoryName;
    }

    public void disable() {
        this.status = 0;
    }

    public void enable() {
        this.status = 1;
    }


}
