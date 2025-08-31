package org.ys.commens.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;

import java.math.BigDecimal;

/**
     * (YsGoods)表实体类
     *
     * @author makejava
     * @since 2025-07-23 22:01:18
     */
    @SuppressWarnings("serial")
    public  class YsGoods extends Model<YsGoods> {
    //商品id
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


        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getBrand() {
            return brand;
        }

        public void setBrand(String brand) {
            this.brand = brand;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getIntroduce() {
            return introduce;
        }

        public void setIntroduce(String introduce) {
            this.introduce = introduce;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public Integer getInventory() {
            return inventory;
        }

        public void setInventory(Integer inventory) {
            this.inventory = inventory;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }
}

