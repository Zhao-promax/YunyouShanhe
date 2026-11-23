package com.yunyou.shanhe.content.cache;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * 多级缓存：L1 Caffeine 本地缓存 + L2 Redis 分布式缓存。
 * <p>
 * 防穿透：null / 空集合用短 TTL 占位，避免无效 key 反复打到 DB；
 * 防击穿：single-flight，同一 key 同一时刻只有一个线程回源加载，其余等待复用结果；
 * 防雪崩：TTL 加随机抖动，避免批量 key 在同一时刻集体过期。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MultiLevelCache {

    private static final String NULL_MARKER = "##NULL##";
    private static final Object NULL_SENTINEL = new Object();

    private final Cache<String, Object> localCache;
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    /** 缓存总开关：线上可一键降级为直连 DB，也用于 A/B 压测对比。 */
    @Value("${app.cache.enabled:true}")
    private boolean cacheEnabled;

    private final Map<String, CompletableFuture<Object>> inFlight = new ConcurrentHashMap<>();

    public <T> T getOrLoad(String key, JavaType type, Duration ttl, Supplier<T> loader) {
        if (!cacheEnabled) {
            return loader.get();
        }
        // L1 本地缓存
        Object hit = localCache.getIfPresent(key);
        if (hit != null) {
            return hit == NULL_SENTINEL ? null : cast(hit);
        }

        // L2 分布式缓存
        String raw = redis.opsForValue().get(key);
        if (raw != null) {
            T val = decode(raw, type);
            localCache.put(key, val == null ? NULL_SENTINEL : val);
            return val;
        }

        // 回源：single-flight 防击穿
        CompletableFuture<Object> future = inFlight.get(key);
        if (future == null) {
            CompletableFuture<Object> created = new CompletableFuture<>();
            CompletableFuture<Object> existing = inFlight.putIfAbsent(key, created);
            if (existing == null) {
                future = created;
                try {
                    T val = loader.get();
                    write(key, val, ttl);
                    future.complete(val == null ? NULL_SENTINEL : val);
                } catch (Throwable t) {
                    future.completeExceptionally(t);
                } finally {
                    inFlight.remove(key, future);
                }
            } else {
                future = existing;
            }
        }
        Object result = future.join();
        return result == NULL_SENTINEL ? null : cast(result);
    }

    private void write(String key, Object val, Duration ttl) {
        if (val == null) {
            // 防穿透：null 占位，短 TTL
            redis.opsForValue().set(key, NULL_MARKER, jitter(Duration.ofSeconds(30)));
            localCache.put(key, NULL_SENTINEL);
            return;
        }
        // 空集合与 null 语义不同：空集合是「有结果但为空」，须返回 [] 而非 null；
        // 但同样用短 TTL，避免无效查询长期占用。
        boolean empty = val instanceof Collection<?> c && c.isEmpty();
        Duration realTtl = jitter(empty ? Duration.ofSeconds(30) : ttl);
        redis.opsForValue().set(key, encode(val), realTtl);
        localCache.put(key, val);
    }

    private Duration jitter(Duration base) {
        long baseMs = base.toMillis();
        long extra = ThreadLocalRandom.current().nextLong(0, Math.max(1, baseMs / 5));
        return Duration.ofMillis(baseMs + extra);
    }

    public void evict(String key) {
        localCache.invalidate(key);
        redis.delete(key);
    }

    public void evictByPrefix(String prefix) {
        localCache.asMap().keySet().removeIf(k -> k.startsWith(prefix));
        ScanOptions options = ScanOptions.scanOptions().match(prefix + "*").count(200).build();
        try (Cursor<String> cursor = redis.scan(options)) {
            while (cursor.hasNext()) {
                redis.delete(cursor.next());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T cast(Object o) {
        return (T) o;
    }

    private String encode(Object val) {
        try {
            return objectMapper.writeValueAsString(val);
        } catch (Exception e) {
            throw new IllegalStateException("cache serialize failed: " + val, e);
        }
    }

    private <T> T decode(String raw, JavaType type) {
        if (NULL_MARKER.equals(raw)) {
            return null;
        }
        try {
            return objectMapper.readValue(raw, type);
        } catch (Exception e) {
            throw new IllegalStateException("cache deserialize failed", e);
        }
    }
}
