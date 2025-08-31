CREATE TABLE `Ys_Order` (
                            `id` BIGINT NOT NULL COMMENT 'ID',
                            `user_id` BIGINT NOT NULL COMMENT '用户id',
                            `goods_id` BIGINT COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '商品id',
                            `status` char(1) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '订单状态',
                            `addTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '订单时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `ys_shopping_history` (
                                       `id` BIGINT NOT NULL COMMENT '订单ID',
                                       `user_id` BIGINT NOT NULL COMMENT '用户id',
                                       `goods_id` BIGINT COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '商品id',
                                       `status` char(1) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '订单状态'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci
