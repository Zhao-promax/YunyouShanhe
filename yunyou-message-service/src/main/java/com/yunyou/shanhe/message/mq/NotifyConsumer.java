package com.yunyou.shanhe.message.mq;

import com.yunyou.shanhe.common.event.NotifyEvent;
import com.yunyou.shanhe.common.mq.MqConstants;
import com.yunyou.shanhe.message.service.IdempotencyService;
import com.yunyou.shanhe.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 通知事件消费者（幂等）。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotifyConsumer {

    private final MessageService messageService;
    private final IdempotencyService idempotencyService;

    @RabbitListener(queues = MqConstants.NOTIFY_QUEUE)
    public void onNotify(NotifyEvent event, Message message) {
        String eventId = message.getMessageProperties().getMessageId();
        if (!idempotencyService.tryAcquire(eventId)) {
            log.info("重复消息，跳过 eventId={}", eventId);
            return;
        }
        try {
            messageService.dispatch(event);
        } catch (Exception e) {
            idempotencyService.release(eventId);
            throw e;
        }
    }
}
