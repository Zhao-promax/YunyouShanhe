-- 云游山河 · 支付库 pay_db（资金域独立隔离）
-- 约定：时间字段统一为 UTC+8 毫秒时间戳（BIGINT）；金额单位为分（BIGINT）。

CREATE DATABASE IF NOT EXISTS `pay_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `pay_db`;

-- 支付单
CREATE TABLE IF NOT EXISTS `payment_order` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `pay_order_no` VARCHAR(40) NOT NULL COMMENT '支付单号',
  `biz_order_no` VARCHAR(40) NOT NULL COMMENT '业务单号（订单号/候补单号）',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `amount` BIGINT NOT NULL COMMENT '支付金额（分）',
  `pay_channel` TINYINT NOT NULL COMMENT '支付渠道：1微信 2支付宝',
  `status` TINYINT NOT NULL COMMENT '状态：1待支付 2支付成功 3已退款 4已关闭',
  `transaction_id` VARCHAR(64) DEFAULT NULL COMMENT '渠道交易流水号',
  `paid_time` BIGINT DEFAULT NULL COMMENT '支付成功时间',
  `created_at` BIGINT NOT NULL COMMENT '创建时间',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pay_order_no` (`pay_order_no`),
  KEY `idx_biz_order_no` (`biz_order_no`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付单';

-- 退款单
CREATE TABLE IF NOT EXISTS `refund_order` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `refund_no` VARCHAR(40) NOT NULL COMMENT '退款单号',
  `pay_order_no` VARCHAR(40) NOT NULL COMMENT '关联支付单号',
  `amount` BIGINT NOT NULL COMMENT '退款金额（分）',
  `reason` VARCHAR(200) DEFAULT NULL COMMENT '退款原因',
  `status` TINYINT NOT NULL COMMENT '状态：1处理中 2退款成功 3退款失败',
  `channel_refund_id` VARCHAR(64) DEFAULT NULL COMMENT '渠道退款流水号',
  `refunded_time` BIGINT DEFAULT NULL COMMENT '退款成功时间',
  `created_at` BIGINT NOT NULL COMMENT '创建时间',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refund_no` (`refund_no`),
  KEY `idx_pay_order_no` (`pay_order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款单';

-- 资金流水台账
CREATE TABLE IF NOT EXISTS `fund_ledger` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `ledger_no` VARCHAR(40) NOT NULL COMMENT '流水号',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `pay_order_no` VARCHAR(40) DEFAULT NULL COMMENT '支付单号',
  `biz_type` TINYINT NOT NULL COMMENT '业务类型：1支付 2退款',
  `amount` BIGINT NOT NULL COMMENT '变动金额（分，收入为正、支出为负）',
  `balance_after` BIGINT NOT NULL COMMENT '变动后余额（分）',
  `created_at` BIGINT NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ledger_no` (`ledger_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_pay_order_no` (`pay_order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资金流水台账';
