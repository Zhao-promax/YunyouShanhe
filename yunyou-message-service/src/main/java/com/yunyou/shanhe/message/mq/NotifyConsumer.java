package com.yunyou.shanhe.message.mq;

import com.rabbitmq.client.Channel;
import com.yunyou.shanhe.common.event.NotifyEvent;
import com.yunyou.shanhe.common.mq.MqConstants;
import com.yunyou.shanhe.message.service.IdempotencyService;
import com.yunyou.shanhe.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 通知事件消费者（手动 ACK + 幂等 + 死信队列）。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotifyConsumer {

    private final MessageService messageService;
    private final IdempotencyService idempotencyService;

    @RabbitListener(queues = MqConstants.NOTIFY_QUEUE)
    public void onNotify(NotifyEvent event, Message message, Channel channel) throws Exception {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String eventId = message.getMessageProperties().getMessageId();
        try {
            if (!idempotencyService.tryAcquire(eventId)) {
                log.info("重复消息，跳过 eventId={}", eventId);
                channel.basicAck(deliveryTag, false);
                return;
            }
            messageService.dispatch(event);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("通知处理失败，进入死信队列 eventId={}", eventId, e);
            idempotencyService.release(eventId);
            channel.basicNack(deliveryTag, false, false);
        }
    }

    @RabbitListener(queues = MqConstants.NOTIFY_DLQ)
    public void onNotifyDlq(Message message, Channel channel) throws Exception {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        log.error("通知死信消息: {}", new String(message.getBody(), StandardCharsets.UTF_8));
        channel.basicAck(deliveryTag, false);
    }
}
