package com.yunyou.shanhe.ticket.mq;

import com.yunyou.shanhe.common.event.NotifyEvent;
import com.yunyou.shanhe.common.event.OrderChangedEvent;
import com.yunyou.shanhe.common.event.RefundEvent;
import com.yunyou.shanhe.common.event.TicketIssuedEvent;
import com.yunyou.shanhe.common.mq.MqConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 票务事件发布器。
 */
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishIssued(TicketIssuedEvent event) {
        rabbitTemplate.convertAndSend(MqConstants.TICKET_EXCHANGE, MqConstants.ROUTING_ISSUED, event);
    }

    public void publishRefund(RefundEvent event) {
        rabbitTemplate.convertAndSend(MqConstants.TICKET_EXCHANGE, MqConstants.ROUTING_REFUND, event);
    }

    public void publishOrderChanged(OrderChangedEvent event) {
        rabbitTemplate.convertAndSend(MqConstants.TICKET_EXCHANGE, MqConstants.ROUTING_ORDER_CHANGED, event);
    }

    public void publishNotify(NotifyEvent event) {
        rabbitTemplate.convertAndSend(MqConstants.TICKET_EXCHANGE, MqConstants.ROUTING_NOTIFY, event);
    }
}
