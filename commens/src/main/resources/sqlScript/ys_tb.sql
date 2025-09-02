
-- 创建数据库
CREATE DATABASE IF NOT EXISTS `ys_tb` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- 选择数据库
USE `ys_tb`;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE `user`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `age` int(20) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, '杨顺', 22);
INSERT INTO `user` VALUES (2, '李卓', 23);

SET FOREIGN_KEY_CHECKS = 1;
