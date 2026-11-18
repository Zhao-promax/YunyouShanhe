-- 云游山河 · 运营管理库 admin_db
-- 约定：时间字段统一为 UTC+8 毫秒时间戳（BIGINT）。

CREATE DATABASE IF NOT EXISTS `admin_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `admin_db`;

-- 运营账号
CREATE TABLE IF NOT EXISTS `admin_user` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL COMMENT '登录名',
  `password_hash` VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码哈希',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常 0禁用',
  `created_at` BIGINT NOT NULL COMMENT '创建时间',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运营账号';

-- 角色
CREATE TABLE IF NOT EXISTS `role` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL COMMENT '角色名',
  `code` VARCHAR(50) NOT NULL COMMENT '角色编码',
  `created_at` BIGINT NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色';

-- 权限
CREATE TABLE IF NOT EXISTS `permission` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL COMMENT '权限名',
  `code` VARCHAR(100) NOT NULL COMMENT '权限编码',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限';

-- 用户-角色关联
CREATE TABLE IF NOT EXISTS `admin_user_role` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `admin_user_id` BIGINT UNSIGNED NOT NULL,
  `role_id` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`admin_user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联';

-- 角色-权限关联
CREATE TABLE IF NOT EXISTS `role_permission` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `role_id` BIGINT UNSIGNED NOT NULL,
  `permission_id` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联';

-- 审计日志（不可篡改）
CREATE TABLE IF NOT EXISTS `audit_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `operator_id` BIGINT UNSIGNED NOT NULL COMMENT '操作人ID',
  `action` VARCHAR(100) NOT NULL COMMENT '操作动作',
  `target_type` VARCHAR(50) DEFAULT NULL COMMENT '操作对象类型',
  `target_id` VARCHAR(100) DEFAULT NULL COMMENT '操作对象ID',
  `detail` TEXT COMMENT '操作详情',
  `ip` VARCHAR(50) DEFAULT NULL COMMENT '来源IP',
  `created_at` BIGINT NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_operator_id` (`operator_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志';

-- 售后工单
CREATE TABLE IF NOT EXISTS `work_order` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `work_order_no` VARCHAR(40) NOT NULL COMMENT '工单号',
  `type` TINYINT NOT NULL COMMENT '类型：1异常订单 2退票申诉 3候补申诉 4其他',
  `biz_order_no` VARCHAR(40) DEFAULT NULL COMMENT '关联业务单号',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1待处理 2处理中 3已解决 4已关闭',
  `handler_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '处理人ID',
  `remark` TEXT COMMENT '处理备注',
  `created_at` BIGINT NOT NULL COMMENT '创建时间',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_work_order_no` (`work_order_no`),
  KEY `idx_biz_order_no` (`biz_order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='售后工单';
