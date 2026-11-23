package com.yunyou.shanhe.user.service;

import com.yunyou.shanhe.common.core.BizException;
import com.yunyou.shanhe.user.sms.SmsChannelClient;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

/**
 * 短信验证码服务。验证码存 Redis，发送走 SmsChannelClient 抽象（开发期 Mock，上线接入阿里云短信）。
 */
@Service
@RequiredArgsConstructor
public class SmsCodeService {

    private static final String KEY_PREFIX = "sms:code:";
    private static final long EXPIRE_SECONDS = 300;

    private final StringRedisTemplate redisTemplate;
    private final SmsChannelClient smsChannelClient;

    public void send(String phone) {
        String code = String.format("%06d", new SecureRandom().nextInt(1000000));
        redisTemplate.opsForValue().set(KEY_PREFIX + phone, code, EXPIRE_SECONDS, TimeUnit.SECONDS);
        smsChannelClient.send(phone, code);
    }

    public void verify(String phone, String code) {
        String cached = redisTemplate.opsForValue().get(KEY_PREFIX + phone);
        if (cached == null || !cached.equals(code)) {
            throw new BizException("验证码错误或已过期");
        }
        redisTemplate.delete(KEY_PREFIX + phone);
    }
}
