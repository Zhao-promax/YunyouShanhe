package com.yunyou.shanhe.pay.mq;

import com.yunyou.shanhe.common.event.RefundEvent;
import com.yunyou.shanhe.common.mq.MqConstants;
import com.yunyou.shanhe.pay.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 退款事件消费者。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RefundConsumer {

    private final PaymentService paymentService;

    @RabbitListener(queues = MqConstants.REFUND_QUEUE)
    public void onRefund(RefundEvent event) {
        log.info("收到退款事件: {}", event);
        paymentService.refund(event.payOrderNo(), event.amount(), event.reason());
    }
}
