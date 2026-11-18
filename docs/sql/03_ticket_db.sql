-- 云游山河 · 票务交易库 ticket_db（核心交易链路）
-- 约定：时间字段统一为 UTC+8 毫秒时间戳（BIGINT）；金额单位为分（BIGINT）。

CREATE DATABASE IF NOT EXISTS `ticket_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `ticket_db`;

-- 景区渠道配置
CREATE TABLE IF NOT EXISTS `channel` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `scenic_id` BIGINT UNSIGNED NOT NULL COMMENT '景区ID',
  `name` VARCHAR(100) NOT NULL COMMENT '渠道名称',
  `app_key` VARCHAR(100) NOT NULL COMMENT '渠道 AppKey',
  `sign_key_ref` VARCHAR(255) NOT NULL COMMENT '签名密钥引用（KMS key id / 别名，禁止明文）',
  `qps_limit` INT NOT NULL DEFAULT 1 COMMENT '约定 QPS 上限',
  `poll_interval` INT NOT NULL DEFAULT 60 COMMENT '降级轮询间隔（秒）',
  `degrade_params` JSON DEFAULT NULL COMMENT '降级参数（指数退避、熔断阈值等）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
  `created_at` BIGINT NOT NULL COMMENT '创建时间',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_scenic_id` (`scenic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='景区渠道配置';

-- 现货/候补主订单
CREATE TABLE IF NOT EXISTS `ticket_order` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_no` VARCHAR(40) NOT NULL COMMENT '平台订单号',
  `biz_type` TINYINT NOT NULL COMMENT '业务类型：1现货 2候补',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `scenic_id` BIGINT UNSIGNED NOT NULL COMMENT '景区ID',
  `visit_date` DATE NOT NULL COMMENT '游玩日期',
  `time_slot` VARCHAR(20) NOT NULL COMMENT '游玩时段',
  `ticket_type` VARCHAR(50) NOT NULL COMMENT '票种',
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
  `amount` BIGINT NOT NULL COMMENT '订单金额（分）',
  `status` TINYINT NOT NULL COMMENT '状态：1待支付 2已出票 3已关闭 4已退票',
  `channel_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '渠道ID',
  `request_id` VARCHAR(64) NOT NULL COMMENT '景区交互全局幂等号',
  `pay_order_no` VARCHAR(40) DEFAULT NULL COMMENT '支付单号',
  `expire_time` BIGINT DEFAULT NULL COMMENT '待支付超时时间（毫秒时间戳）',
  `created_at` BIGINT NOT NULL COMMENT '创建时间',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  UNIQUE KEY `uk_request_id` (`request_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_scenic_date` (`scenic_id`, `visit_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台订单';

-- 候补单（FIFO 排队，DB 为权威状态）
CREATE TABLE IF NOT EXISTS `waitlist_order` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `waitlist_no` VARCHAR(40) NOT NULL COMMENT '候补单号',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `scenic_id` BIGINT UNSIGNED NOT NULL COMMENT '景区ID',
  `visit_date` DATE NOT NULL COMMENT '游玩日期',
  `preferred_slot` VARCHAR(20) NOT NULL COMMENT '首选时段',
  `shift_slots` VARCHAR(200) DEFAULT NULL COMMENT '顺延时段（逗号分隔，可空）',
  `status` TINYINT NOT NULL COMMENT '状态：1排队中 2已中签 3已取消 4熔断失败',
  `active_flag` TINYINT GENERATED ALWAYS AS (CASE WHEN `status` IN (1,2) THEN 1 ELSE NULL END) STORED COMMENT '有效标记（有效=1，终态=NULL），用于唯一性约束',
  `enqueue_time` BIGINT NOT NULL COMMENT '入队时间（毫秒时间戳，FIFO 排序依据）',
  `pay_order_no` VARCHAR(40) NOT NULL COMMENT '支付单号（先付后退）',
  `order_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '中签后生成的订单ID',
  `created_at` BIGINT NOT NULL COMMENT '创建时间',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_waitlist_no` (`waitlist_no`),
  UNIQUE KEY `uk_user_scenic_date_slot_active` (`user_id`, `scenic_id`, `visit_date`, `preferred_slot`, `active_flag`),
  KEY `idx_scenic_date_slot_enqueue` (`scenic_id`, `visit_date`, `preferred_slot`, `enqueue_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='候补单';

-- 订单状态变更日志
CREATE TABLE IF NOT EXISTS `order_status_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_no` VARCHAR(40) NOT NULL COMMENT '订单号/候补单号',
  `from_status` TINYINT DEFAULT NULL COMMENT '变更前状态',
  `to_status` TINYINT NOT NULL COMMENT '变更后状态',
  `reason` VARCHAR(200) DEFAULT NULL COMMENT '变更原因',
  `operator` VARCHAR(50) DEFAULT NULL COMMENT '操作来源（用户/系统/景区/运营）',
  `created_at` BIGINT NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单状态变更日志';

-- 渠道交互报文留存（原始请求/响应，用于纠纷排查）
CREATE TABLE IF NOT EXISTS `channel_interaction_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `channel_id` BIGINT UNSIGNED NOT NULL COMMENT '渠道ID',
  `request_id` VARCHAR(64) NOT NULL COMMENT '幂等号',
  `direction` TINYINT NOT NULL COMMENT '方向：1请求 2响应',
  `url` VARCHAR(500) DEFAULT NULL COMMENT '接口URL',
  `method` VARCHAR(10) DEFAULT NULL COMMENT 'HTTP 方法',
  `request_body` MEDIUMTEXT COMMENT '请求报文',
  `response_body` MEDIUMTEXT COMMENT '响应报文',
  `http_status` INT DEFAULT NULL COMMENT 'HTTP 状态码',
  `duration_ms` INT DEFAULT NULL COMMENT '耗时（毫秒）',
  `created_at` BIGINT NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_request_id` (`request_id`),
  KEY `idx_channel_id` (`channel_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='渠道交互报文留存';
