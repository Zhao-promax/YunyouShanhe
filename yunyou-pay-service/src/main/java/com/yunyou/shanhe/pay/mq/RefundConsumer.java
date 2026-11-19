package com.yunyou.shanhe.pay.mq;

import com.rabbitmq.client.Channel;
import com.yunyou.shanhe.common.event.RefundEvent;
import com.yunyou.shanhe.common.mq.MqConstants;
import com.yunyou.shanhe.pay.service.IdempotencyService;
import com.yunyou.shanhe.pay.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 退款事件消费者（手动 ACK + 幂等 + 死信队列）。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RefundConsumer {

    private final PaymentService paymentService;
    private final IdempotencyService idempotencyService;

    @RabbitListener(queues = MqConstants.REFUND_QUEUE)
    public void onRefund(RefundEvent event, Message message, Channel channel) throws Exception {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String eventId = message.getMessageProperties().getMessageId();
        try {
            if (!idempotencyService.tryAcquire(eventId)) {
                log.info("重复消息，跳过 eventId={}", eventId);
                channel.basicAck(deliveryTag, false);
                return;
            }
            paymentService.refund(event.payOrderNo(), event.amount(), event.reason());
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("退款处理失败，进入死信队列 eventId={}", eventId, e);
            idempotencyService.release(eventId);
            channel.basicNack(deliveryTag, false, false);
        }
    }

    @RabbitListener(queues = MqConstants.REFUND_DLQ)
    public void onRefundDlq(Message message, Channel channel) throws Exception {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        log.error("退款死信消息: {}", new String(message.getBody(), StandardCharsets.UTF_8));
        channel.basicAck(deliveryTag, false);
    }
}
