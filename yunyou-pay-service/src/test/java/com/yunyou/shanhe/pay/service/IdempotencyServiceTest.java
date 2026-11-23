package com.yunyou.shanhe.pay.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    @InjectMocks
    private IdempotencyService idempotencyService;

    @Test
    void shouldAcquireOnFirstAttempt() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);

        assertTrue(idempotencyService.tryAcquire("evt-1"));
    }

    @Test
    void shouldRejectDuplicate() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(false);

        assertFalse(idempotencyService.tryAcquire("evt-1"));
    }

    @Test
    void shouldAllowBlankEventIdWithoutTouchingRedis() {
        assertTrue(idempotencyService.tryAcquire(null));
        assertTrue(idempotencyService.tryAcquire("  "));
        verifyNoInteractions(redisTemplate);
    }

    @Test
    void shouldDeleteOnRelease() {
        idempotencyService.release("evt-1");

        verify(redisTemplate).delete("mq:consumed:evt-1");
    }
}
