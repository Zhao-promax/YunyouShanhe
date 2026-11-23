package com.yunyou.shanhe.content.lock;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * 简单 Redis 分布式锁：setnx 加锁 + Lua 原子解锁，用于多实例下互斥执行定时任务。
 */
@Component
@RequiredArgsConstructor
public class DistributedLock {

    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);

    private final StringRedisTemplate redisTemplate;

    public boolean tryLock(String key, String value, Duration ttl) {
        Boolean ok = redisTemplate.opsForValue().setIfAbsent(key, value, ttl);
        return Boolean.TRUE.equals(ok);
    }

    public void unlock(String key, String value) {
        redisTemplate.execute(UNLOCK_SCRIPT, List.of(key), value);
    }
}
