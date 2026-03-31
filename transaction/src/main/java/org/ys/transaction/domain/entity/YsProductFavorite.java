package org.ys.transaction.domain.entity;

import lombok.Getter;

import java.util.Date;

/**
 * (YsProductFavorite)表实体类
 * 商品收藏表
 *
 * @author makejava
 * @since 2025-07-16
 */
@Getter
public class YsProductFavorite {

    private Long id;

    private Long userId;

    private Long goodsId;

    private Date createdAt;

    private YsProductFavorite(Long id, Long userId, Long goodsId, Date createdAt) {
        this.id = id;
        this.userId = userId;
        this.goodsId = goodsId;
        this.createdAt = createdAt;
    }

    public static YsProductFavorite rehydrate(Long id, Long userId, Long goodsId, Date createdAt) {
        return new YsProductFavorite(id, userId, goodsId, createdAt);
    }

    public void create(Long userId, Long goodsId) {
        if (userId == null || goodsId == null) {
            throw new IllegalArgumentException("userId and goodsId are required");
        }
        this.userId = userId;
        this.goodsId = goodsId;
        this.createdAt = new Date();
    }



}
