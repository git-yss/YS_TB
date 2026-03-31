package org.ys.transaction.domain.entity;

import lombok.Getter;

/**
 * (YsShoppingHistory)表实体类
 *
 * @author makejava
 * @since 2025-07-16 19:41:25
 */
@Getter
public class YsShoppingHistory {
//订单ID
    private Long id;
//用户id
    private Long userId;
//商品id
    private Long goodsId;

    private YsShoppingHistory(Long id, Long userId, Long goodsId) {
        this.id = id;
        this.userId = userId;
        this.goodsId = goodsId;
    }

    public static YsShoppingHistory rehydrate(Long id, Long userId, Long goodsId) {
        return new YsShoppingHistory(id, userId, goodsId);
    }

    public void create(Long userId, Long goodsId) {
        if (userId == null || goodsId == null) {
            throw new IllegalArgumentException("userId and goodsId are required");
        }
        this.userId = userId;
        this.goodsId = goodsId;
    }





}

