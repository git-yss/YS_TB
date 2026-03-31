package org.ys.transaction.domain.entity;

import lombok.Getter;

import java.math.BigDecimal;

/**
     * (YsGoods)表实体类
     *
     * @author makejava
     * @since 2025-07-23 22:01:18
     */
@Getter
public class YsGoods {
        private Long id;
    //品牌
        private String brand;
    //商品名称
        private String name;
    //商品介绍
        private String introduce;
    //单价
        private BigDecimal price;
    //库存
        private Integer inventory;
    //图片地址
        private String image;
    //分类
        private String category;

    // 分类ID（用于商品多条件查询/分类过滤）
    private Long categoryId;

    private YsGoods(Long id, String brand, String name, String introduce, BigDecimal price, Integer inventory,
                    String image, String category, Long categoryId) {
        this.id = id;
        this.brand = brand;
        this.name = name;
        this.introduce = introduce;
        this.price = price;
        this.inventory = inventory;
        this.image = image;
        this.category = category;
        this.categoryId = categoryId;
    }

    public static YsGoods rehydrate(Long id, String brand, String name, String introduce, BigDecimal price, Integer inventory,
                                   String image, String category, Long categoryId) {
        return new YsGoods(id, brand, name, introduce, price, inventory, image, category, categoryId);
    }

    public static YsGoods identify(Long id) {
        return new YsGoods(id, null, null, null, null, null, null, null, null);
    }

    public void decreaseStock(Integer num) {
        if (num == null || num <= 0) {
            throw new IllegalArgumentException("num must be greater than 0");
        }
        if (inventory == null || inventory < num) {
            throw new IllegalStateException("insufficient inventory");
        }
        this.inventory -= num;
    }

    public void increaseStock(Integer num) {
        if (num == null || num <= 0) {
            throw new IllegalArgumentException("num must be greater than 0");
        }
        this.inventory = (inventory == null ? 0 : inventory) + num;
    }



}

