-- 演示种子数据（城市 + 渠道）。
-- 注意：景点（scenic）需通过 API 创建（POST /api/content/scenic）以触发 ES 索引，此处不重复插入。

INSERT INTO content_db.city (name, province, sort, status, created_at, updated_at)
VALUES ('北京', '北京', 1, 1, UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000);

INSERT INTO ticket_db.channel (scenic_id, name, app_key, sign_key_ref, qps_limit, poll_interval, status, created_at, updated_at)
VALUES (1, '故宫官方', 'mock-app-key', 'kms:mock-sign-key', 10, 60, 1, UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000);

INSERT INTO ticket_db.ticket_type (scenic_id, type_code, type_name, price, status, created_at, updated_at) VALUES
(1, 'adult', '成人票', 6000, 1, UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000),
(1, 'child', '儿童票', 3000, 1, UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000),
(1, 'student', '学生票', 3000, 1, UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000),
(1, 'senior', '老年票', 3000, 1, UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000);
