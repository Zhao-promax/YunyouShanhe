package com.yunyou.shanhe.ticket.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yunyou.shanhe.ticket.entity.WaitlistOrder;
import com.yunyou.shanhe.ticket.enums.WaitlistStatus;
import com.yunyou.shanhe.ticket.mapper.WaitlistOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 候补 FIFO 队列（Redis ZSET 排序快照，数据库为权威状态）。
 */
@Service
@RequiredArgsConstructor
public class WaitlistService {

    private static final String KEY_PREFIX = "waitlist:queue:";

    private final StringRedisTemplate redisTemplate;
    private final WaitlistOrderMapper waitlistOrderMapper;

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

    /**
     * 候补队列对账：以数据库为准，重建所有「排队中」候补单的 Redis 排序快照。
     * 用于 Redis 宕机/数据丢失后的补偿，防止排队状态错乱。
     *
     * @return 重建的队列分组数
     */
    public int reconcile() {
        List<WaitlistOrder> queuing = waitlistOrderMapper.selectList(
                Wrappers.<WaitlistOrder>lambdaQuery()
                        .eq(WaitlistOrder::getStatus, WaitlistStatus.QUEUING.getCode()));
        Map<String, List<WaitlistOrder>> grouped = queuing.stream()
                .collect(Collectors.groupingBy(w -> key(w.getScenicId(), w.getVisitDate(), w.getPreferredSlot())));
        for (Map.Entry<String, List<WaitlistOrder>> e : grouped.entrySet()) {
            redisTemplate.delete(e.getKey());
            for (WaitlistOrder w : e.getValue()) {
                redisTemplate.opsForZSet().add(e.getKey(), String.valueOf(w.getId()), w.getEnqueueTime());
            }
        }
        return grouped.size();
    }

    private String key(Long scenicId, LocalDate visitDate, String slot) {
        return KEY_PREFIX + scenicId + ":" + visitDate + ":" + slot;
    }
}
