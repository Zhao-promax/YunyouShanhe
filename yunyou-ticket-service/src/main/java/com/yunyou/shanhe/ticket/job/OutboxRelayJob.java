package com.yunyou.shanhe.ticket.job;

import com.yunyou.shanhe.ticket.mq.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * outbox 投递定时任务：把本地消息表中的待发送事件投递到 MQ。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxRelayJob {

    private final EventPublisher eventPublisher;

    @Scheduled(fixedDelay = 5000)
    public void relayOutbox() {
        int n = eventPublisher.relayPending();
        if (n > 0) {
            log.info("outbox 投递 {} 条", n);
        }
    }
}
