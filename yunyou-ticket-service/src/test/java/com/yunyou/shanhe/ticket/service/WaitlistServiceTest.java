package com.yunyou.shanhe.ticket.service;

import com.yunyou.shanhe.ticket.entity.WaitlistOrder;
import com.yunyou.shanhe.ticket.mapper.WaitlistOrderMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WaitlistServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private WaitlistOrderMapper waitlistOrderMapper;

    @InjectMocks
    private WaitlistService waitlistService;

    @Test
    void shouldRebuildQueueFromDb() {
        WaitlistOrder w = new WaitlistOrder();
        w.setId(1L);
        w.setScenicId(1L);
        w.setVisitDate(LocalDate.of(2026, 11, 20));
        w.setPreferredSlot("09:00-12:00");
        w.setEnqueueTime(1000L);
        when(waitlistOrderMapper.selectList(any())).thenReturn(List.of(w));

        @SuppressWarnings("unchecked")
        ZSetOperations<String, String> zsetOps = mock(ZSetOperations.class);
        when(redisTemplate.opsForZSet()).thenReturn(zsetOps);
        when(redisTemplate.delete(anyString())).thenReturn(Boolean.TRUE);

        int groups = waitlistService.reconcile();

        assertEquals(1, groups);
        String key = "waitlist:queue:1:2026-11-20:09:00-12:00";
        verify(redisTemplate).delete(key);
        verify(zsetOps).add(key, "1", 1000.0);
    }
}
