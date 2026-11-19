package com.yunyou.shanhe.ticket.mq;

import com.yunyou.shanhe.common.event.NotifyEvent;
import com.yunyou.shanhe.common.event.OrderChangedEvent;
import com.yunyou.shanhe.common.event.RefundEvent;
import com.yunyou.shanhe.common.event.TicketIssuedEvent;
import com.yunyou.shanhe.common.mq.MqConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 票务事件发布器。每条消息设置唯一 MessageId，供消费端幂等去重。
 */
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private static final MessagePostProcessor SET_MESSAGE_ID = message -> {
        message.getMessageProperties().setMessageId(UUID.randomUUID().toString());
        return message;
    };

    private final RabbitTemplate rabbitTemplate;

    public void publishIssued(TicketIssuedEvent event) {
        rabbitTemplate.convertAndSend(MqConstants.TICKET_EXCHANGE, MqConstants.ROUTING_ISSUED, event, SET_MESSAGE_ID);
    }

    public void publishRefund(RefundEvent event) {
        rabbitTemplate.convertAndSend(MqConstants.TICKET_EXCHANGE, MqConstants.ROUTING_REFUND, event, SET_MESSAGE_ID);
    }

    public void publishOrderChanged(OrderChangedEvent event) {
        rabbitTemplate.convertAndSend(MqConstants.TICKET_EXCHANGE, MqConstants.ROUTING_ORDER_CHANGED, event, SET_MESSAGE_ID);
    }

    public void publishNotify(NotifyEvent event) {
        rabbitTemplate.convertAndSend(MqConstants.TICKET_EXCHANGE, MqConstants.ROUTING_NOTIFY, event, SET_MESSAGE_ID);
    }
}
