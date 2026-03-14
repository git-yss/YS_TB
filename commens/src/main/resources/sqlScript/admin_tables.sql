-- 后台管理系统数据库扩展脚本

USE `ys_tb`;

-- 1. 用户表扩展：添加状态和创建时间字段
ALTER TABLE `ys_user`
ADD COLUMN `status` char(1) DEFAULT '1' COMMENT '状态：1=正常，0=封禁' AFTER `tel`,
ADD COLUMN `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间' AFTER `status`;

-- 2. 订单表扩展：添加更多订单详情字段
ALTER TABLE `ys_order`
ADD COLUMN `quantity` int DEFAULT 1 COMMENT '商品数量' AFTER `addTime`,
ADD COLUMN `unit_price` decimal(18, 2) DEFAULT 0.00 COMMENT '商品单价' AFTER `quantity`,
ADD COLUMN `total_amount` decimal(18, 2) DEFAULT 0.00 COMMENT '订单总金额' AFTER `unit_price`,
ADD COLUMN `pay_method` varchar(20) DEFAULT 'balance' COMMENT '支付方式：balance=余额支付，wechat=微信支付，alipay=支付宝' AFTER `total_amount`,
ADD COLUMN `logistics_no` varchar(50) DEFAULT NULL COMMENT '物流单号' AFTER `pay_method`,
ADD COLUMN `logistics_company` varchar(50) DEFAULT NULL COMMENT '物流公司' AFTER `logistics_no`,
ADD COLUMN `ship_time` datetime DEFAULT NULL COMMENT '发货时间' AFTER `logistics_company`,
ADD COLUMN `pay_time` datetime DEFAULT NULL COMMENT '支付时间' AFTER `ship_time`,
ADD COLUMN `finish_time` datetime DEFAULT NULL COMMENT '完成时间' AFTER `pay_time`,
ADD COLUMN `refund_time` datetime DEFAULT NULL COMMENT '退款时间' AFTER `finish_time`,
ADD COLUMN `refund_reason` varchar(200) DEFAULT NULL COMMENT '退款原因' AFTER `refund_time`,
ADD COLUMN `refund_amount` decimal(18, 2) DEFAULT 0.00 COMMENT '退款金额' AFTER `refund_reason`;

-- 3. 更新订单状态枚举注释
-- 订单状态：0=待支付，1=已支付，2=已发货，3=已完成，4=退款中，5=已退款，6=已取消

-- 4. 创建商品分类表
CREATE TABLE IF NOT EXISTS `ys_category` (
    `id` bigint NOT NULL COMMENT '分类ID',
    `name` varchar(50) NOT NULL COMMENT '分类名称',
    `parent_id` bigint DEFAULT 0 COMMENT '父分类ID，0表示顶级分类',
    `level` int DEFAULT 1 COMMENT '分类层级',
    `sort` int DEFAULT 0 COMMENT '排序',
    `icon` varchar(200) DEFAULT NULL COMMENT '图标',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_parent_id` (`parent_id`),
    INDEX `idx_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- 插入初始分类数据
INSERT INTO `ys_category` (`id`, `name`, `parent_id`, `level`, `sort`) VALUES
(1, '手机', 0, 1, 1),
(2, '电脑', 0, 1, 2),
(3, '数码配件', 0, 1, 3),
(4, '智能设备', 0, 1, 4);

-- 5. 创建商品收藏表
CREATE TABLE IF NOT EXISTS `ys_product_favorite` (
    `id` bigint NOT NULL COMMENT '收藏ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `goods_id` bigint NOT NULL COMMENT '商品ID',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_goods` (`user_id`, `goods_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_goods_id` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品收藏表';

-- 6. 创建商品评价表
CREATE TABLE IF NOT EXISTS `ys_product_review` (
    `id` bigint NOT NULL COMMENT '评价ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `goods_id` bigint NOT NULL COMMENT '商品ID',
    `order_id` bigint NOT NULL COMMENT '订单ID',
    `rating` int NOT NULL COMMENT '评分：1-5星',
    `content` text COMMENT '评价内容',
    `images` varchar(1000) DEFAULT NULL COMMENT '评价图片，多个图片用逗号分隔',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
    `is_anonymous` tinyint(1) DEFAULT 0 COMMENT '是否匿名评价：0=否，1=是',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_goods_id` (`goods_id`),
    INDEX `idx_order_id` (`order_id`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品评价表';

-- 7. 创建优惠券表
CREATE TABLE IF NOT EXISTS `ys_coupon` (
    `id` bigint NOT NULL COMMENT '优惠券ID',
    `name` varchar(100) NOT NULL COMMENT '优惠券名称',
    `type` tinyint NOT NULL COMMENT '优惠券类型：1=满减券，2=折扣券，3=无门槛券',
    `discount_amount` decimal(10, 2) DEFAULT NULL COMMENT '减免金额（满减券、无门槛券）',
    `discount_rate` decimal(3, 2) DEFAULT NULL COMMENT '折扣率（折扣券），如0.85表示8.5折',
    `min_amount` decimal(10, 2) DEFAULT 0.00 COMMENT '最低消费金额',
    `max_discount` decimal(10, 2) DEFAULT NULL COMMENT '最高优惠金额',
    `total_count` int NOT NULL COMMENT '发放总数',
    `used_count` int DEFAULT 0 COMMENT '已使用数量',
    `per_user_limit` int DEFAULT 1 COMMENT '每人限领数量',
    `valid_days` int NOT NULL COMMENT '有效天数',
    `status` tinyint DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `start_time` datetime DEFAULT NULL COMMENT '开始时间',
    `end_time` datetime DEFAULT NULL COMMENT '结束时间',
    PRIMARY KEY (`id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优惠券表';

-- 8. 创建用户优惠券表
CREATE TABLE IF NOT EXISTS `ys_user_coupon` (
    `id` bigint NOT NULL COMMENT 'ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `coupon_id` bigint NOT NULL COMMENT '优惠券ID',
    `status` tinyint DEFAULT 0 COMMENT '状态：0=未使用，1=已使用，2=已过期',
    `order_id` bigint DEFAULT NULL COMMENT '使用的订单ID',
    `receive_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
    `use_time` datetime DEFAULT NULL COMMENT '使用时间',
    `expire_time` datetime NOT NULL COMMENT '过期时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_coupon` (`user_id`, `coupon_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户优惠券表';

-- 9. 创建秒杀活动表
CREATE TABLE IF NOT EXISTS `ys_seckill` (
    `id` bigint NOT NULL COMMENT '秒杀活动ID',
    `goods_id` bigint NOT NULL COMMENT '商品ID',
    `seckill_price` decimal(18, 2) NOT NULL COMMENT '秒杀价格',
    `stock_count` int NOT NULL COMMENT '秒杀库存',
    `start_time` datetime NOT NULL COMMENT '开始时间',
    `end_time` datetime NOT NULL COMMENT '结束时间',
    `status` tinyint DEFAULT 0 COMMENT '状态：0=未开始，1=进行中，2=已结束，3=已取消',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_goods` (`goods_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_start_time` (`start_time`),
    INDEX `idx_end_time` (`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀活动表';

-- 10. 创建秒杀订单表
CREATE TABLE IF NOT EXISTS `ys_seckill_order` (
    `id` bigint NOT NULL COMMENT '订单ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `seckill_id` bigint NOT NULL COMMENT '秒杀活动ID',
    `goods_id` bigint NOT NULL COMMENT '商品ID',
    `order_no` varchar(64) NOT NULL COMMENT '订单号',
    `quantity` int DEFAULT 1 COMMENT '购买数量',
    `seckill_price` decimal(18, 2) NOT NULL COMMENT '秒杀价格',
    `total_amount` decimal(18, 2) NOT NULL COMMENT '订单总金额',
    `status` tinyint DEFAULT 0 COMMENT '状态：0=待支付，1=已支付，2=已取消',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_seckill_id` (`seckill_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀订单表';

-- 11. 创建系统配置表
CREATE TABLE IF NOT EXISTS `ys_system_config` (
    `id` bigint NOT NULL COMMENT '配置ID',
    `config_key` varchar(100) NOT NULL COMMENT '配置键',
    `config_value` text COMMENT '配置值',
    `config_desc` varchar(200) DEFAULT NULL COMMENT '配置描述',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 插入初始系统配置
INSERT INTO `ys_system_config` (`id`, `config_key`, `config_value`, `config_desc`) VALUES
(1, 'site_name', '智能电商平台', '网站名称'),
(2, 'enable_registration', '1', '是否开放用户注册：0=否，1=是'),
(3, 'enable_seckill', '1', '是否启用秒杀功能：0=否，1=是'),
(4, 'enable_coupon', '1', '是否启用优惠券功能：0=否，1=是'),
(5, 'order_timeout_minutes', '5', '秒杀订单超时时间（分钟）'),
(6, 'normal_order_timeout_days', '7', '普通订单超时时间（天）'),
(7, 'shopping_cart_timeout_days', '30', '购物车过期时间（天）');

-- 12. 创建操作日志表
CREATE TABLE IF NOT EXISTS `ys_admin_log` (
    `id` bigint NOT NULL COMMENT '日志ID',
    `admin_id` bigint NOT NULL COMMENT '管理员ID',
    `admin_name` varchar(50) NOT NULL COMMENT '管理员名称',
    `action` varchar(100) NOT NULL COMMENT '操作类型',
    `module` varchar(50) NOT NULL COMMENT '操作模块',
    `method` varchar(200) DEFAULT NULL COMMENT '请求方法',
    `params` text COMMENT '请求参数',
    `ip` varchar(50) DEFAULT NULL COMMENT 'IP地址',
    `status` tinyint DEFAULT 1 COMMENT '状态：0=失败，1=成功',
    `error_msg` varchar(500) DEFAULT NULL COMMENT '错误信息',
    `execute_time` int DEFAULT NULL COMMENT '执行时间（毫秒）',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_admin_id` (`admin_id`),
    INDEX `idx_action` (`action`),
    INDEX `idx_module` (`module`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员操作日志表';

-- 13. 创建管理员表
CREATE TABLE IF NOT EXISTS `ys_admin` (
    `id` bigint NOT NULL COMMENT '管理员ID',
    `username` varchar(50) NOT NULL COMMENT '用户名',
    `password` varchar(100) NOT NULL COMMENT '密码（加密）',
    `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
    `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
    `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
    `role` varchar(50) DEFAULT 'admin' COMMENT '角色',
    `status` tinyint DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
    `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录IP',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- 插入默认管理员（密码：admin123，使用BCrypt加密）
INSERT INTO `ys_admin` (`id`, `username`, `password`, `real_name`, `role`, `status`) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '超级管理员', 'super_admin', 1);

-- 14. 创建角色表
CREATE TABLE IF NOT EXISTS `ys_role` (
    `id` bigint NOT NULL COMMENT '角色ID',
    `role_name` varchar(50) NOT NULL COMMENT '角色名称',
    `role_code` varchar(50) NOT NULL COMMENT '角色编码',
    `description` varchar(200) DEFAULT NULL COMMENT '描述',
    `status` tinyint DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 插入初始角色
INSERT INTO `ys_role` (`id`, `role_name`, `role_code`, `description`) VALUES
(1, '超级管理员', 'super_admin', '拥有所有权限'),
(2, '商品管理员', 'goods_admin', '负责商品管理'),
(3, '订单管理员', 'order_admin', '负责订单管理'),
(4, '用户管理员', 'user_admin', '负责用户管理');

-- 15. 创建角色权限表
CREATE TABLE IF NOT EXISTS `ys_role_permission` (
    `id` bigint NOT NULL COMMENT 'ID',
    `role_id` bigint NOT NULL COMMENT '角色ID',
    `permission` varchar(100) NOT NULL COMMENT '权限标识',
    `description` varchar(200) DEFAULT NULL COMMENT '权限描述',
    PRIMARY KEY (`id`),
    INDEX `idx_role_id` (`role_id`),
    INDEX `idx_permission` (`permission`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限表';

-- 插入超级管理员权限
INSERT INTO `ys_role_permission` (`id`, `role_id`, `permission`, `description`) VALUES
(1, 1, 'admin:goods:*', '商品管理全部权限'),
(2, 1, 'admin:order:*', '订单管理全部权限'),
(3, 1, 'admin:user:*', '用户管理全部权限'),
(4, 1, 'admin:marketing:*', '营销管理全部权限'),
(5, 1, 'admin:statistics:*', '统计数据全部权限'),
(6, 1, 'admin:system:*', '系统管理全部权限');
