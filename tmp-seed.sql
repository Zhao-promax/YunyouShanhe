INSERT INTO content_db.city (name, province, sort, status, created_at, updated_at)
VALUES ('北京', '北京', 1, 1, UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000);

INSERT INTO ticket_db.channel (scenic_id, name, app_key, sign_key_ref, qps_limit, poll_interval, status, created_at, updated_at)
VALUES (1, '故宫官方', 'mock-app-key', 'kms:mock-sign-key', 10, 60, 1, UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000);
