package com.yunyou.shanhe.ticket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * 候补 FIFO 队列（Redis ZSET 排序快照，数据库为权威状态）。
 */
@Service
@RequiredArgsConstructor
public class WaitlistService {

    private static final String KEY_PREFIX = "waitlist:queue:";

    private final StringRedisTemplate redisTemplate;

    public void enqueue(Long scenicId, LocalDate visitDate, String slot, Long waitlistId, Long enqueueTime) {
        redisTemplate.opsForZSet().add(key(scenicId, visitDate, slot), String.valueOf(waitlistId), enqueueTime);
    }

    /**
     * 取出队首前 k 个候补单 id（按入队时间升序）。
     */
    public List<Long> popTop(Long scenicId, LocalDate visitDate, String slot, int k) {
        Set<String> members = redisTemplate.opsForZSet().range(key(scenicId, visitDate, slot), 0, k - 1);
        return members == null ? List.of() : members.stream().map(Long::valueOf).toList();
    }

    public void remove(Long scenicId, LocalDate visitDate, String slot, Long waitlistId) {
        redisTemplate.opsForZSet().remove(key(scenicId, visitDate, slot), String.valueOf(waitlistId));
    }

    private String key(Long scenicId, LocalDate visitDate, String slot) {
        return KEY_PREFIX + scenicId + ":" + visitDate + ":" + slot;
    }
}
