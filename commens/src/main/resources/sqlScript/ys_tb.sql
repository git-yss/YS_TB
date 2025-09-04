
-- 创建数据库
CREATE DATABASE IF NOT EXISTS `ys_tb` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- 选择数据库
USE `ys_tb`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

/*
 Navicat Premium Dump SQL

 Source Server         : 个人Mysql
 Source Server Type    : MySQL
 Source Server Version : 80405 (8.4.5)
 Source Host           : localhost:3306
 Source Schema         : ys_tb

 Target Server Type    : MySQL
 Target Server Version : 80405 (8.4.5)
 File Encoding         : 65001

 Date: 02/09/2025 21:46:55
*/

-- ----------------------------
-- Table structure for ys_goods
-- ----------------------------
DROP TABLE IF EXISTS `ys_goods`;
CREATE TABLE `ys_goods`  (
                             `id` bigint NOT NULL COMMENT '商品id',
                             `brand` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '品牌',
                             `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商品名称',
                             `introduce` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商品介绍',
                             `price` decimal(18, 2) NULL DEFAULT NULL COMMENT '单价',
                             `inventory` int NULL DEFAULT NULL COMMENT '库存',
                             `image` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图片地址',
                             `category` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分类',
                             PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ys_goods
-- ----------------------------
INSERT INTO `ys_goods` VALUES (1, '苹果', 'Apple iPhone 13 ', 'Apple iPhone 13 Pro Max 远峰蓝色 256GB', 100.00, 98, 'https://img14.360buyimg.com/n5/s720x720_jfs/t1/317900/26/6095/94534/683ef68aF0f1e309b/9790c3aadc316f29.jpg.avif', '2');
INSERT INTO `ys_goods` VALUES (2, '华为', '华为 Mate 50 Pro', '华为 Mate 50 Pro 昆仑破晓 512GB', 6999.00, 100, 'https://img10.360buyimg.com/n5/s720x720_jfs/t1/311483/6/15185/95700/686ce8d2F63b69f90/17d192406da2a018.jpg.avif', '2');
INSERT INTO `ys_goods` VALUES (3, '小米', '小米13', '小米13 5G手机 12GB+256GB 白色', 4299.00, 100, 'https://img10.360buyimg.com/n1/s720x720_jfs/t1/240220/3/10125/24182/66d6e2bdFaa5497a2/2e60e3b6f0f7e88a.jpg.avif', '2');
INSERT INTO `ys_goods` VALUES (4, '苹果', 'Apple MacBook', 'Apple MacBook Pro 14英寸 M2 Pro芯片', 15999.00, 100, 'https://img10.360buyimg.com/n5/s720x720_jfs/t1/299221/1/22700/31811/6878a28bFa7ee0e91/abf9ad7c90f86815.jpg.avif', '3');
INSERT INTO `ys_goods` VALUES (5, '戴尔', '戴尔XPS笔记本', '戴尔XPS 13 9315 13.4英寸轻薄笔记本', 8999.00, 100, 'https://img10.360buyimg.com/n5/s720x720_jfs/t1/289343/32/3468/86262/68785c63F2b09baa1/6f54910d120c60fd.jpg.avif', '3');
INSERT INTO `ys_goods` VALUES (6, '索尼', '索尼PlayStation 5', '索尼 PlayStation 5 游戏主机', 3899.00, 100, 'https://img13.360buyimg.com/n5/s720x720_jfs/t1/306378/17/19054/107769/6879f6c2F1bb47f4e/d4ad924e580f3fd2.jpg.avif', '4');
INSERT INTO `ys_goods` VALUES (7, '苹果', 'Apple AirPods Pro', 'Apple AirPods Pro (第二代)', 1899.00, 100, 'https://img10.360buyimg.com/n5/s720x720_jfs/t1/299400/29/21933/19418/686f8453F0ad337a0/ecf2249c35d28b8b.png.avif', '2');
INSERT INTO `ys_goods` VALUES (8, '佳能', '佳能微单相机', '佳能 EOS R6 Mark II 全画幅微单相机', 15999.00, 100, 'https://img12.360buyimg.com/n5/s720x720_jfs/t1/310104/23/12553/106427/685cc733F64ee4313/75cbd2fe1e55a89c.png.avif', '4');

-- ----------------------------
-- Table structure for ys_order
-- ----------------------------
DROP TABLE IF EXISTS `ys_order`;
CREATE TABLE `ys_order`  (
                             `id` bigint NOT NULL COMMENT 'ID',
                             `user_id` bigint NOT NULL COMMENT '用户id',
                             `goods_id` bigint NULL DEFAULT NULL COMMENT '商品id',
                             `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '订单状态',
                             `addTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '订单时间',
                             UNIQUE INDEX `IDX_USER_ID`(`id` ASC, `user_id` ASC, `goods_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ys_order
-- ----------------------------


-- ----------------------------
-- Table structure for ys_shopping_history
-- ----------------------------
DROP TABLE IF EXISTS `ys_shopping_history`;
CREATE TABLE `ys_shopping_history`  (
                                        `id` bigint NOT NULL COMMENT '订单ID',
                                        `user_id` bigint NOT NULL COMMENT '用户id',
                                        `goods_id` bigint NOT NULL COMMENT '商品id',
    
                                        INDEX `IDX_USER_ID`(`user_id` ASC, `goods_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ys_shopping_history
-- ----------------------------
INSERT INTO `ys_shopping_history` VALUES (175682008056462, 12345, 1);

-- ----------------------------
-- Table structure for ys_user
-- ----------------------------
DROP TABLE IF EXISTS `ys_user`;
CREATE TABLE `ys_user`  (
                            `id` bigint NOT NULL COMMENT '用户ID',
                            `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名',
                            `password` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码',
                            `age` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '年龄',
                            `sex` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '性别',
                            `balance` decimal(18, 2) NOT NULL COMMENT '余额',
                            `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '电子邮件',
                            `tel` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '电话号码',
                            PRIMARY KEY (`id`) USING BTREE,
                            UNIQUE INDEX `IDX_USER_ID`(`id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ys_user
-- ----------------------------
INSERT INTO `ys_user` VALUES (12345, '杨顺', '123456', '25', '1', 5800.00, '811570083@qqcom', '17683273448');

-- ----------------------------
-- Table structure for ys_user_addr
-- ----------------------------
DROP TABLE IF EXISTS `ys_user_addr`;
CREATE TABLE `ys_user_addr`  (
                                 `id` bigint NOT NULL COMMENT 'ID',
                                 `user_id` bigint NOT NULL COMMENT '用户id',
                                 `addr` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '地址',
                                 PRIMARY KEY (`id`) USING BTREE,
                                 UNIQUE INDEX `IDX_USER_ID`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ys_user_addr
-- ----------------------------
INSERT INTO `ys_user_addr` VALUES (1, 12345, '四川省成都市武侯区星语双城5栋一单元420');

SET FOREIGN_KEY_CHECKS = 1;
