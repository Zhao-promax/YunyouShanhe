package com.yunyou.shanhe.content.job;

import com.yunyou.shanhe.content.lock.DistributedLock;
import com.yunyou.shanhe.content.mq.ContentEventPublisher;
import com.yunyou.shanhe.content.service.ContentIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

/**
 * ES 同步定时任务：outbox 投递 + 全量重建兜底。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ContentSyncJob {

    private static final String REBUILD_LOCK_KEY = "lock:content:rebuild";

    private final ContentEventPublisher publisher;
    private final ContentIndexService indexService;
    private final DistributedLock distributedLock;

    /** 每 5 秒：投递待发送 outbox。 */
    @Scheduled(fixedDelay = 5000)
    public void relayOutbox() {
        int n = publisher.relayPending();
        if (n > 0) {
            log.info("outbox 投递 {} 条", n);
        }
    }

    /** 每 5 分钟：全量重建 ES（对账兜底）。多实例用分布式锁保证只有一个执行。 */
    @Scheduled(fixedDelay = 300000, initialDelay = 60000)
    public void fullRebuild() {
        String lockValue = UUID.randomUUID().toString();
        if (!distributedLock.tryLock(REBUILD_LOCK_KEY, lockValue, Duration.ofSeconds(60))) {
            return;
        }
        try {
            int n = indexService.rebuildAll();
            log.info("ES 全量重建 {} 条", n);
        } finally {
            distributedLock.unlock(REBUILD_LOCK_KEY, lockValue);
        }
    }
}
