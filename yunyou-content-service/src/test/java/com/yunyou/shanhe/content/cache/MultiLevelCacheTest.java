package com.yunyou.shanhe.content.cache;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class MultiLevelCacheTest {

    private Cache<String, Object> localCache;
    private StringRedisTemplate redis;
    private ValueOperations<String, String> valueOps;
    private MultiLevelCache cache;
    private final ObjectMapper om = new ObjectMapper();

    @BeforeEach
    void setUp() {
        localCache = Caffeine.newBuilder().maximumSize(100).build();
        redis = mock(StringRedisTemplate.class);
        valueOps = mock(ValueOperations.class);
        cache = new MultiLevelCache(localCache, redis, om);
        ReflectionTestUtils.setField(cache, "cacheEnabled", true);
    }

    private JavaType stringType() {
        return om.constructType(String.class);
    }

    private JavaType listStringType() {
        return om.getTypeFactory().constructCollectionType(List.class, String.class);
    }

    @Test
    void shouldHitL1WithoutTouchingLoaderOrRedis() {
        localCache.put("k", "cached");
        AtomicInteger loaderCalls = new AtomicInteger();
        Supplier<String> loader = () -> {
            loaderCalls.incrementAndGet();
            return "db";
        };

        assertEquals("cached", cache.getOrLoad("k", stringType(), Duration.ofMinutes(1), loader));
        assertEquals(0, loaderCalls.get());
        verifyNoInteractions(redis);
    }

    @Test
    void shouldHitL2AndPopulateL1() {
        when(redis.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("k")).thenReturn("\"fromRedis\"");
        AtomicInteger loaderCalls = new AtomicInteger();

        assertEquals("fromRedis", cache.getOrLoad("k", stringType(), Duration.ofMinutes(1),
                () -> {
                    loaderCalls.incrementAndGet();
                    return "db";
                }));
        assertEquals(0, loaderCalls.get());
        assertEquals("fromRedis", localCache.getIfPresent("k"));
    }

    @Test
    void shouldLoadOnMissAndWriteBothLayers() {
        when(redis.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("k")).thenReturn(null);

        String result = cache.getOrLoad("k", stringType(), Duration.ofMinutes(1), () -> "db");

        assertEquals("db", result);
        assertEquals("db", localCache.getIfPresent("k"));
        verify(valueOps).set(eq("k"), eq("\"db\""), any(Duration.class));
    }

    @Test
    void shouldCacheNullMarkerForPenetrationProtection() {
        when(redis.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("k")).thenReturn(null);
        Supplier<String> nullLoader = () -> null;

        assertNull(cache.getOrLoad("k", stringType(), Duration.ofMinutes(1), nullLoader));
        verify(valueOps).set(eq("k"), eq("##NULL##"), any(Duration.class));

        // 第二次命中 L1 的 null 占位，不再回源
        assertNull(cache.getOrLoad("k", stringType(), Duration.ofMinutes(1), nullLoader));
        verify(valueOps, times(1)).set(eq("k"), eq("##NULL##"), any(Duration.class));
    }

    @Test
    void shouldReturnEmptyListNotNull() {
        when(redis.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("k")).thenReturn(null);

        List<String> result = cache.getOrLoad("k", listStringType(), Duration.ofMinutes(1), List::of);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(valueOps).set(eq("k"), eq("[]"), any(Duration.class));
    }

    @Test
    void shouldSingleFlightConcurrentLoads() throws Exception {
        when(redis.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("k")).thenReturn(null);
        AtomicInteger loaderCalls = new AtomicInteger();
        Supplier<String> loader = () -> {
            loaderCalls.incrementAndGet();
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "loaded";
        };

        Thread t = new Thread(() -> cache.getOrLoad("k", stringType(), Duration.ofMinutes(1), loader));
        t.start();
        Thread.sleep(100); // 确保 t 先进入回源并持有 future

        String result = cache.getOrLoad("k", stringType(), Duration.ofMinutes(1), loader);
        t.join();

        assertEquals("loaded", result);
        assertEquals(1, loaderCalls.get());
    }
}
