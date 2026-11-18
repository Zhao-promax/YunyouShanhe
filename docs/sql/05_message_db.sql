-- 云游山河 · 消息通知库 message_db
-- 约定：时间字段统一为 UTC+8 毫秒时间戳（BIGINT）。

CREATE DATABASE IF NOT EXISTS `message_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `message_db`;

-- 消息模板
CREATE TABLE IF NOT EXISTS `message_template` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `template_code` VARCHAR(64) NOT NULL COMMENT '模板编码',
  `channel` TINYINT NOT NULL COMMENT '通道：1短信 2站内信 3APP推送',
  `title` VARCHAR(200) DEFAULT NULL COMMENT '标题',
  `content` TEXT COMMENT '模板内容（含占位符）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
  `created_at` BIGINT NOT NULL COMMENT '创建时间',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code_channel` (`template_code`, `channel`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息模板';

-- 消息发送记录
CREATE TABLE IF NOT EXISTS `message_record` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `template_code` VARCHAR(64) DEFAULT NULL COMMENT '模板编码',
  `channel` TINYINT NOT NULL COMMENT '通道：1短信 2站内信 3APP推送',
  `content` TEXT COMMENT '发送内容',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0待发送 1成功 2失败',
  `retry_count` INT NOT NULL DEFAULT 0 COMMENT '重试次数',
  `read_status` TINYINT NOT NULL DEFAULT 0 COMMENT '已读状态（站内信）：0未读 1已读',
  `send_time` BIGINT DEFAULT NULL COMMENT '发送时间',
  `created_at` BIGINT NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息发送记录';

-- 库存订阅提醒（仅提醒，不自动候补）
CREATE TABLE IF NOT EXISTS `subscription` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `scenic_id` BIGINT UNSIGNED NOT NULL COMMENT '景区ID',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1订阅中 0已退订',
  `created_at` BIGINT NOT NULL COMMENT '创建时间',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_scenic` (`user_id`, `scenic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存订阅提醒';
