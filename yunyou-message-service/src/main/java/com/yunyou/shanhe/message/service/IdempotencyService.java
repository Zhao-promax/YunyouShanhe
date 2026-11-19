package com.yunyou.shanhe.message.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 消息消费幂等：基于 Redis setnx 按 eventId 去重。
 */
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private static final String PREFIX = "mq:consumed:";
    private static final Duration TTL = Duration.ofDays(1);

    private final StringRedisTemplate redisTemplate;

    public boolean tryAcquire(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            return true;
        }
        Boolean ok = redisTemplate.opsForValue().setIfAbsent(PREFIX + eventId, "1", TTL);
        return Boolean.TRUE.equals(ok);
    }

    public void release(String eventId) {
        if (eventId != null && !eventId.isBlank()) {
            redisTemplate.delete(PREFIX + eventId);
        }
    }
}
