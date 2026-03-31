package org.ys.transaction.domain.entity;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class YsUserCoupon {
    private Long id;
    private Long userId;
    private Long couponId;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime usedAt;

    private YsUserCoupon(Long id, Long userId, Long couponId, Integer status, LocalDateTime createdAt, LocalDateTime usedAt) {
        this.id = id;
        this.userId = userId;
        this.couponId = couponId;
        this.status = status;
        this.createdAt = createdAt;
        this.usedAt = usedAt;
    }

    public static YsUserCoupon rehydrate(Long id, Long userId, Long couponId, Integer status, LocalDateTime createdAt, LocalDateTime usedAt) {
        return new YsUserCoupon(id, userId, couponId, status, createdAt, usedAt);
    }
}
