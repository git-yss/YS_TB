package org.ys.transaction.Infrastructure.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
     * (YsGoods)表实体类
     *
     * @author makejava
     * @since 2025-07-23 22:01:18
     */
    @SuppressWarnings("serial")
    @TableName("ys_goods")
    @Data
    public  class YsGoods  {
    //商品id
    @TableId(value = "id", type = IdType.INPUT)
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



}

