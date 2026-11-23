package com.yunyou.shanhe.ticket.mq;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yunyou.shanhe.common.event.NotifyEvent;
import com.yunyou.shanhe.common.event.OrderChangedEvent;
import com.yunyou.shanhe.common.event.RefundEvent;
import com.yunyou.shanhe.common.event.TicketIssuedEvent;
import com.yunyou.shanhe.common.mq.MqConstants;
import com.yunyou.shanhe.ticket.entity.OutboxMessage;
import com.yunyou.shanhe.ticket.mapper.OutboxMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 票务事件发布器：outbox 落库（与业务同事务）+ 定时批量投递 MQ + 失败退避重试。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private static final int MAX_RETRY = 5;
    private static final int BATCH_SIZE = 100;
    private static final int STATUS_PENDING = 0;
    private static final int STATUS_SENT = 1;
    private static final int STATUS_FAILED = 2;

    private record Spec(String routingKey, Class<?> type) {
    }

    private static final Map<String, Spec> SPECS = Map.of(
            "TICKET_ISSUED", new Spec(MqConstants.ROUTING_ISSUED, TicketIssuedEvent.class),
            "REFUND", new Spec(MqConstants.ROUTING_REFUND, RefundEvent.class),
            "ORDER_CHANGED", new Spec(MqConstants.ROUTING_ORDER_CHANGED, OrderChangedEvent.class),
            "NOTIFY", new Spec(MqConstants.ROUTING_NOTIFY, NotifyEvent.class)
    );

    private final OutboxMessageMapper outboxMapper;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    /** 业务事务内调用：落 outbox（与业务写同事务，保证不丢）。 */
    public void record(String eventType, Object event) {
        try {
            OutboxMessage msg = new OutboxMessage();
            msg.setEventId(UUID.randomUUID().toString().replace("-", ""));
            msg.setEventType(eventType);
            msg.setPayload(objectMapper.writeValueAsString(event));
            msg.setStatus(STATUS_PENDING);
            msg.setRetryCount(0);
            msg.setNextRetryAt(0L);
            msg.setCreatedAt(System.currentTimeMillis());
            outboxMapper.insert(msg);
        } catch (Exception e) {
            throw new IllegalStateException("落 outbox 失败", e);
        }
    }

    /** 定时任务调用：批量投递到期待发送的 outbox，失败退避重试。 */
    public int relayPending() {
        long now = System.currentTimeMillis();
        List<OutboxMessage> pending = outboxMapper.selectList(
                Wrappers.<OutboxMessage>lambdaQuery()
                        .eq(OutboxMessage::getStatus, STATUS_PENDING)
                        .le(OutboxMessage::getNextRetryAt, now)
                        .orderByAsc(OutboxMessage::getCreatedAt)
                        .last("LIMIT " + BATCH_SIZE));
        for (OutboxMessage msg : pending) {
            Spec spec = SPECS.get(msg.getEventType());
            if (spec == null) {
                markFailed(msg, new IllegalStateException("未知事件类型: " + msg.getEventType()));
                continue;
            }
            try {
                Object event = objectMapper.readValue(msg.getPayload(), spec.type());
                MessagePostProcessor setId = m -> {
                    m.getMessageProperties().setMessageId(msg.getEventId());
                    return m;
                };
                rabbitTemplate.convertAndSend(MqConstants.TICKET_EXCHANGE, spec.routingKey(), event, setId);
                markSent(msg.getId());
            } catch (Exception e) {
                markFailed(msg, e);
            }
        }
        return pending.size();
    }

    private void markSent(Long id) {
        OutboxMessage update = new OutboxMessage();
        update.setId(id);
        update.setStatus(STATUS_SENT);
        update.setSentAt(System.currentTimeMillis());
        outboxMapper.updateById(update);
    }

    private void markFailed(OutboxMessage msg, Exception e) {
        int retried = (msg.getRetryCount() == null ? 0 : msg.getRetryCount()) + 1;
        OutboxMessage update = new OutboxMessage();
        update.setId(msg.getId());
        update.setRetryCount(retried);
        update.setErrorMsg(truncate(e.getMessage(), 512));
        if (retried >= MAX_RETRY) {
            update.setStatus(STATUS_FAILED);
        } else {
            update.setNextRetryAt(System.currentTimeMillis() + backoffMillis(retried));
        }
        outboxMapper.updateById(update);
        log.warn("outbox 发送失败 eventId={} retry={} err={}", msg.getEventId(), retried, e.getMessage());
    }

    /** 指数退避：2s / 4s / 8s / 16s，上限 60s。 */
    private long backoffMillis(int retried) {
        return Math.min(1000L << retried, 60_000L);
    }

    private String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}
