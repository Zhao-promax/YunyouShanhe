-- 云游山河 · 用户库 user_db
-- 约定：时间字段统一为 UTC+8 毫秒时间戳（BIGINT）；实名证件号 AES 密文存储、对外脱敏。

CREATE DATABASE IF NOT EXISTS `user_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `user_db`;

-- 用户主表
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
  `password_hash` VARCHAR(100) DEFAULT NULL COMMENT 'BCrypt 密码哈希（三方登录用户可为空）',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `avatar_url` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `wechat_openid` VARCHAR(64) DEFAULT NULL COMMENT '微信 openid',
  `wechat_unionid` VARCHAR(64) DEFAULT NULL COMMENT '微信 unionid',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常 0禁用',
  `last_login_time` BIGINT DEFAULT NULL COMMENT '最后登录时间（毫秒时间戳）',
  `created_at` BIGINT NOT NULL COMMENT '创建时间（毫秒时间戳）',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间（毫秒时间戳）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_wechat_openid` (`wechat_openid`),
  KEY `idx_wechat_unionid` (`wechat_unionid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户主表';

-- 出行人实名档案
CREATE TABLE IF NOT EXISTS `real_name_profile` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '所属用户ID',
  `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
  `id_card_no_enc` VARCHAR(255) NOT NULL COMMENT '证件号 AES 密文',
  `id_card_type` TINYINT NOT NULL DEFAULT 1 COMMENT '证件类型：1身份证',
  `id_card_mask` VARCHAR(30) NOT NULL COMMENT '证件号脱敏展示',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1有效 0失效',
  `archived_at` BIGINT DEFAULT NULL COMMENT '归档时间（核销+退票窗口期后自动归档）',
  `created_at` BIGINT NOT NULL COMMENT '创建时间',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_id_card_mask` (`id_card_mask`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出行人实名档案';

-- 用户收藏
CREATE TABLE IF NOT EXISTS `user_favorite` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `target_type` TINYINT NOT NULL COMMENT '收藏类型：1景点 2攻略',
  `target_id` BIGINT UNSIGNED NOT NULL COMMENT '收藏对象ID',
  `created_at` BIGINT NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏';
