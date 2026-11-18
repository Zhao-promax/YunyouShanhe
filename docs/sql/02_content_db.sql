-- 云游山河 · 内容攻略库 content_db
-- 约定：时间字段统一为 UTC+8 毫秒时间戳（BIGINT）。

CREATE DATABASE IF NOT EXISTS `content_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `content_db`;

-- 城市
CREATE TABLE IF NOT EXISTS `city` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL COMMENT '城市名称',
  `province` VARCHAR(50) DEFAULT NULL COMMENT '省份',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序权重',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
  `created_at` BIGINT NOT NULL COMMENT '创建时间',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='城市';

-- 景点
CREATE TABLE IF NOT EXISTS `scenic` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `city_id` BIGINT UNSIGNED NOT NULL COMMENT '所属城市ID',
  `name` VARCHAR(100) NOT NULL COMMENT '景点名称',
  `cover_url` VARCHAR(255) DEFAULT NULL COMMENT '封面图URL',
  `address` VARCHAR(255) DEFAULT NULL COMMENT '地址',
  `lng` DECIMAL(10,6) DEFAULT NULL COMMENT '经度',
  `lat` DECIMAL(10,6) DEFAULT NULL COMMENT '纬度',
  `intro` TEXT COMMENT '景点简介',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序权重',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1上架 0下架',
  `created_at` BIGINT NOT NULL COMMENT '创建时间',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_city_id` (`city_id`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='景点';

-- 结构化攻略
CREATE TABLE IF NOT EXISTS `guide` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `scenic_id` BIGINT UNSIGNED NOT NULL COMMENT '关联景点ID',
  `title` VARCHAR(200) NOT NULL COMMENT '攻略标题',
  `summary` VARCHAR(500) DEFAULT NULL COMMENT '摘要',
  `content` MEDIUMTEXT COMMENT '攻略正文（结构化字段可存 JSON）',
  `guide_type` TINYINT NOT NULL DEFAULT 1 COMMENT '类型：1开放时间 2放票时间 3入园须知 4游玩路线 5避坑提示 6退票规则',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1上架 0下架',
  `publish_time` BIGINT DEFAULT NULL COMMENT '发布时间',
  `created_at` BIGINT NOT NULL COMMENT '创建时间',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_scenic_id` (`scenic_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='结构化攻略';

-- 景点预约规则配置（供票务服务读取）
CREATE TABLE IF NOT EXISTS `scenic_rule` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `scenic_id` BIGINT UNSIGNED NOT NULL COMMENT '景点ID',
  `support_waitlist` TINYINT NOT NULL DEFAULT 0 COMMENT '是否开启候补：1是 0否',
  `waitlist_cutoff_hour` INT DEFAULT NULL COMMENT '候补截止（游玩前一日的小时数，如22）',
  `support_slot_shift` TINYINT NOT NULL DEFAULT 0 COMMENT '是否支持时段顺延：1是 0否',
  `ticket_time` VARCHAR(100) DEFAULT NULL COMMENT '放票时间描述',
  `open_time` VARCHAR(100) DEFAULT NULL COMMENT '开放时间描述',
  `refund_rule` VARCHAR(500) DEFAULT NULL COMMENT '退票规则',
  `visit_notice` VARCHAR(500) DEFAULT NULL COMMENT '入园须知',
  `created_at` BIGINT NOT NULL COMMENT '创建时间',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_scenic_id` (`scenic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='景点预约规则配置';
