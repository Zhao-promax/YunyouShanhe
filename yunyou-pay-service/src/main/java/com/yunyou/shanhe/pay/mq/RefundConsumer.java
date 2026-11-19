package com.yunyou.shanhe.pay.mq;

import com.yunyou.shanhe.common.event.RefundEvent;
import com.yunyou.shanhe.common.mq.MqConstants;
import com.yunyou.shanhe.pay.service.IdempotencyService;
import com.yunyou.shanhe.pay.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 退款事件消费者（幂等）。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RefundConsumer {

    private final PaymentService paymentService;
    private final IdempotencyService idempotencyService;

    @RabbitListener(queues = MqConstants.REFUND_QUEUE)
    public void onRefund(RefundEvent event, Message message) {
        String eventId = message.getMessageProperties().getMessageId();
        if (!idempotencyService.tryAcquire(eventId)) {
            log.info("重复消息，跳过 eventId={}", eventId);
            return;
        }
        try {
            paymentService.refund(event.payOrderNo(), event.amount(), event.reason());
        } catch (Exception e) {
            idempotencyService.release(eventId);
            throw e;
        }
    }
}
