ALTER TABLE `ys_order`
    ADD COLUMN `quantity` int NOT NULL DEFAULT 1 COMMENT '商品数量' AFTER `goods_id`,
ADD COLUMN `unit_price` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '商品单价' AFTER `quantity`,
ADD COLUMN `total_amount` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '订单总金额' AFTER `unit_price`,
ADD COLUMN `pay_method` varchar(20) DEFAULT NULL COMMENT '支付方式：balance=余额支付，wechat=微信支付，alipay=支付宝' AFTER `status`,
ADD COLUMN `logistics_no` varchar(50) DEFAULT NULL COMMENT '物流单号' AFTER `pay_method`,
ADD COLUMN `logistics_company` varchar(50) DEFAULT NULL COMMENT '物流公司' AFTER `logistics_no`,
ADD COLUMN `ship_time` datetime DEFAULT NULL COMMENT '发货时间' AFTER `addTime`,
ADD COLUMN `pay_time` datetime DEFAULT NULL COMMENT '支付时间' AFTER `ship_time`,
ADD COLUMN `finish_time` datetime DEFAULT NULL COMMENT '完成时间' AFTER `pay_time`,
ADD COLUMN `refund_time` datetime DEFAULT NULL COMMENT '退款时间' AFTER `finish_time`,
ADD COLUMN `refund_reason` varchar(500) DEFAULT NULL COMMENT '退款原因' AFTER `refund_time`,
ADD COLUMN `refund_amount` decimal(18, 2) DEFAULT NULL COMMENT '退款金额' AFTER `refund_reason`;

-- 2. 创建订单详情表（支持一个订单多个商品）
DROP TABLE IF EXISTS `ys_order_detail`;
CREATE TABLE `ys_order_detail`  (
                                    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单详情ID',
                                    `order_id` bigint NOT NULL COMMENT '订单ID',
                                    `goods_id` bigint NOT NULL COMMENT '商品ID',
                                    `goods_name` varchar(100) DEFAULT NULL COMMENT '商品名称',
                                    `goods_image` varchar(200) DEFAULT NULL COMMENT '商品图片',
                                    `quantity` int NOT NULL DEFAULT 1 COMMENT '购买数量',
                                    `unit_price` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '商品单价',
                                    `total_price` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '小计金额',
                                    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    PRIMARY KEY (`id`) USING BTREE,
                                    INDEX `IDX_ORDER_ID`(`order_id` ASC) USING BTREE,
                                    INDEX `IDX_GOODS_ID`(`goods_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic COMMENT = '订单详情表';

-- 3. 创建商品评价表
DROP TABLE IF EXISTS `ys_product_review`;
CREATE TABLE `ys_product_review`  (
                                      `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评价ID',
                                      `order_id` bigint NOT NULL COMMENT '订单ID',
                                      `goods_id` bigint NOT NULL COMMENT '商品ID',
                                      `user_id` bigint NOT NULL COMMENT '用户ID',
                                      `username` varchar(50) DEFAULT NULL COMMENT '用户名',
                                      `rating` tinyint NOT NULL DEFAULT 5 COMMENT '评分：1-5星',
                                      `content` varchar(1000) DEFAULT NULL COMMENT '评价内容',
                                      `images` varchar(1000) DEFAULT NULL COMMENT '评价图片（多个图片用逗号分隔）',
                                      `is_anonymous` tinyint NOT NULL DEFAULT 0 COMMENT '是否匿名：0=否，1=是',
                                      `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
                                      `updated_at` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                      `reply_content` varchar(1000) DEFAULT NULL COMMENT '商家回复',
                                      `reply_time` datetime DEFAULT NULL COMMENT '回复时间',
                                      PRIMARY KEY (`id`) USING BTREE,
                                      INDEX `IDX_ORDER_ID`(`order_id` ASC) USING BTREE,
                                      INDEX `IDX_GOODS_ID`(`goods_id` ASC) USING BTREE,
                                      INDEX `IDX_USER_ID`(`user_id` ASC) USING BTREE,
                                      INDEX `IDX_RATING`(`rating` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic COMMENT = '商品评价表';

-- 4. 创建商品收藏表
DROP TABLE IF EXISTS `ys_product_favorite`;
CREATE TABLE `ys_product_favorite`  (
                                        `id` bigint NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
                                        `user_id` bigint NOT NULL COMMENT '用户ID',
                                        `goods_id` bigint NOT NULL COMMENT '商品ID',
                                        `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
                                        PRIMARY KEY (`id`) USING BTREE,
                                        UNIQUE INDEX `UNQ_USER_GOODS`(`user_id`, `goods_id`) USING BTREE,
                                        INDEX `IDX_USER_ID`(`user_id` ASC) USING BTREE,
                                        INDEX `IDX_GOODS_ID`(`goods_id` ASC) USING BTREE,
                                        INDEX `IDX_CREATED_AT`(`created_at` DESC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic COMMENT = '商品收藏表';

-- 5. 创建商品分类表
DROP TABLE IF EXISTS `ys_category`;
CREATE TABLE `ys_category`  (
                                `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
                                `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父分类ID，0表示顶级分类',
                                `category_name` varchar(50) NOT NULL COMMENT '分类名称',
                                `category_code` varchar(20) NOT NULL COMMENT '分类编码',
                                `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
                                `icon` varchar(200) DEFAULT NULL COMMENT '分类图标',
                                `description` varchar(200) DEFAULT NULL COMMENT '分类描述',
                                `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `updated_at` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
                                PRIMARY KEY (`id`) USING BTREE,
                                UNIQUE INDEX `UNQ_CATEGORY_CODE`(`category_code` ASC) USING BTREE,
                                INDEX `IDX_PARENT_ID`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic COMMENT = '商品分类表';

-- 6. 创建优惠券表
DROP TABLE IF EXISTS `ys_coupon`;
CREATE TABLE `ys_coupon`  (
                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT '优惠券ID',
                              `coupon_name` varchar(100) NOT NULL COMMENT '优惠券名称',
                              `coupon_code` varchar(50) NOT NULL COMMENT '优惠券代码',
                              `coupon_type` tinyint NOT NULL DEFAULT 1 COMMENT '优惠券类型：1=满减券，2=折扣券，3=立减券',
                              `discount_amount` decimal(18, 2) DEFAULT NULL COMMENT '优惠金额（满减券、立减券）',
                              `discount_rate` decimal(5, 2) DEFAULT NULL COMMENT '折扣率（折扣券，如0.85表示85折）',
                              `min_amount` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '最低消费金额',
                              `max_discount` decimal(18, 2) DEFAULT NULL COMMENT '最大优惠金额',
                              `total_count` int NOT NULL DEFAULT 0 COMMENT '发放总数',
                              `used_count` int NOT NULL DEFAULT 0 COMMENT '已使用数量',
                              `per_user_limit` int NOT NULL DEFAULT 1 COMMENT '每人限领数量',
                              `valid_start_time` datetime NOT NULL COMMENT '有效期开始时间',
                              `valid_end_time` datetime NOT NULL COMMENT '有效期结束时间',
                              `apply_category` varchar(200) DEFAULT NULL COMMENT '适用分类（多个用逗号分隔，空表示全品类）',
                              `apply_goods` varchar(500) DEFAULT NULL COMMENT '适用商品ID（多个用逗号分隔，空表示全部）',
                              `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
                              `description` varchar(500) DEFAULT NULL COMMENT '优惠券说明',
                              `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `updated_at` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                              PRIMARY KEY (`id`) USING BTREE,
                              UNIQUE INDEX `UNQ_COUPON_CODE`(`coupon_code` ASC) USING BTREE,
                              INDEX `IDX_VALID_TIME`(`valid_start_time`, `valid_end_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic COMMENT = '优惠券表';

-- 7. 创建用户优惠券表
DROP TABLE IF EXISTS `ys_user_coupon`;
CREATE TABLE `ys_user_coupon`  (
                                   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户优惠券ID',
                                   `user_id` bigint NOT NULL COMMENT '用户ID',
                                   `coupon_id` bigint NOT NULL COMMENT '优惠券ID',
                                   `order_id` bigint DEFAULT NULL COMMENT '使用订单ID',
                                   `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0=未使用，1=已使用，2=已过期',
                                   `get_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
                                   `use_time` datetime DEFAULT NULL COMMENT '使用时间',
                                   `expire_time` datetime NOT NULL COMMENT '过期时间',
                                   PRIMARY KEY (`id`) USING BTREE,
                                   INDEX `IDX_USER_ID`(`user_id` ASC) USING BTREE,
                                   INDEX `IDX_COUPON_ID`(`coupon_id` ASC) USING BTREE,
                                   INDEX `IDX_STATUS`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic COMMENT = '用户优惠券表';

-- 8. 修复用户地址表的isMain字段默认值
ALTER TABLE `ys_user_addr`
    add COLUMN `is_main` char(1) DEFAULT '0' COMMENT '是否主地址 1=是 0=不是';

-- 9. 扩展用户表字段（支持找回密码等功能）
ALTER TABLE `ys_user`
    ADD COLUMN `salt` varchar(50) DEFAULT NULL COMMENT '密码盐值' AFTER `password`,
ADD COLUMN `register_time` datetime DEFAULT NULL COMMENT '注册时间' AFTER `tel`,
ADD COLUMN `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间' AFTER `register_time`,
ADD COLUMN `last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录IP' AFTER `last_login_time`,
ADD COLUMN `status` tinyint NOT NULL DEFAULT 1 COMMENT '账户状态：0=禁用，1=正常' AFTER `last_login_ip`,
ADD COLUMN `avatar` varchar(200) DEFAULT NULL COMMENT '头像URL' AFTER `status`;

-- 10. 创建密码重置记录表
DROP TABLE IF EXISTS `ys_password_reset`;
CREATE TABLE `ys_password_reset`  (
                                      `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
                                      `user_id` bigint NOT NULL COMMENT '用户ID',
                                      `email` varchar(100) NOT NULL COMMENT '邮箱',
                                      `token` varchar(100) NOT NULL COMMENT '重置令牌',
                                      `expire_time` datetime NOT NULL COMMENT '过期时间',
                                      `used` tinyint NOT NULL DEFAULT 0 COMMENT '是否已使用：0=否，1=是',
                                      `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      PRIMARY KEY (`id`) USING BTREE,
                                      UNIQUE INDEX `UNQ_TOKEN`(`token` ASC) USING BTREE,
                                      INDEX `IDX_USER_ID`(`user_id` ASC) USING BTREE,
                                      INDEX `IDX_EMAIL`(`email` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic COMMENT = '密码重置记录表';

-- 11. 插入初始商品分类数据
INSERT INTO `ys_category` (`id`, `parent_id`, `category_name`, `category_code`, `sort_order`, `description`) VALUES
                                                                                                                 (1, 0, '手机数码', 'DIGITAL', 1, '手机、平板、数码配件'),
                                                                                                                 (2, 0, '电脑办公', 'COMPUTER', 2, '笔记本、台式机、办公设备'),
                                                                                                                 (3, 0, '家用电器', 'APPLIANCE', 3, '冰箱、洗衣机、空调等'),
                                                                                                                 (4, 0, '生活日用', 'LIFESTYLE', 4, '家居、日用百货'),
                                                                                                                 (5, 0, '服装鞋帽', 'CLOTHING', 5, '男装、女装、鞋帽'),
                                                                                                                 (6, 1, '智能手机', 'PHONE', 1, '各品牌智能手机'),
                                                                                                                 (7, 1, '平板电脑', 'TABLET', 2, '平板电脑及配件'),
                                                                                                                 (8, 2, '笔记本', 'LAPTOP', 1, '笔记本电脑'),
                                                                                                                 (9, 2, '台式机', 'DESKTOP', 2, '台式机及显示器');

-- 12. 更新商品表关联分类
ALTER TABLE `ys_goods`
    ADD COLUMN `category_id` bigint DEFAULT NULL COMMENT '分类ID' AFTER `category`,
ADD INDEX `IDX_CATEGORY_ID`(`category_id`) USING BTREE;

-- 更新现有商品的分类ID
UPDATE `ys_goods` SET `category_id` = 6 WHERE `id` IN (1, 2, 3, 7);
UPDATE `ys_goods` SET `category_id` = 8 WHERE `id` IN (4, 5);
UPDATE `ys_goods` SET `category_id` = 4 WHERE `id` IN (6, 8);

-- 13. 创建系统配置表
DROP TABLE IF EXISTS `ys_sys_config`;
CREATE TABLE `ys_sys_config`  (
                                  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
                                  `config_key` varchar(100) NOT NULL COMMENT '配置键',
                                  `config_value` varchar(500) NOT NULL COMMENT '配置值',
                                  `config_desc` varchar(200) DEFAULT NULL COMMENT '配置描述',
                                  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `updated_at` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                  PRIMARY KEY (`id`) USING BTREE,
                                  UNIQUE INDEX `UNQ_CONFIG_KEY`(`config_key` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic COMMENT = '系统配置表';

-- 插入初始配置数据
INSERT INTO `ys_sys_config` (`config_key`, `config_value`, `config_desc`) VALUES
                                                                              ('site.name', '智能电商平台', '网站名称'),
                                                                              ('site.logo', '/static/images/logo.png', '网站Logo'),
                                                                              ('order.auto.cancel.hours', '24', '订单自动取消时间（小时）'),
                                                                              ('order.auto.receive.days', '7', '订单自动确认收货时间（天）'),
                                                                              ('user.register.reward', '0', '用户注册奖励金额'),
                                                                              ('invite.reward', '0', '邀请好友奖励金额');