package com.yunyou.shanhe.user.service;

import com.yunyou.shanhe.common.core.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

/**
 * 短信验证码服务。开发期仅日志输出验证码，上线前接入阿里云短信。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsCodeService {

    private static final String KEY_PREFIX = "sms:code:";
    private static final long EXPIRE_SECONDS = 300;

    private final StringRedisTemplate redisTemplate;

    public void send(String phone) {
        String code = String.format("%06d", new SecureRandom().nextInt(1000000));
        log.info("[短信Mock] 手机号 {} 的验证码：{}", phone, code);
        redisTemplate.opsForValue().set(KEY_PREFIX + phone, code, EXPIRE_SECONDS, TimeUnit.SECONDS);
    }

    public void verify(String phone, String code) {
        String cached = redisTemplate.opsForValue().get(KEY_PREFIX + phone);
        if (cached == null || !cached.equals(code)) {
            throw new BizException("验证码错误或已过期");
        }
        redisTemplate.delete(KEY_PREFIX + phone);
    }
}
