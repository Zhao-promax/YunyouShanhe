package com.yunyou.shanhe.pay.config;

import com.yunyou.shanhe.common.mq.MqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public TopicExchange ticketExchange() {
        return new TopicExchange(MqConstants.TICKET_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(MqConstants.DEAD_LETTER_EXCHANGE, true, false);
    }

    @Bean
    public Queue refundQueue() {
        return QueueBuilder.durable(MqConstants.REFUND_QUEUE)
                .deadLetterExchange(MqConstants.DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(MqConstants.REFUND_DLQ)
                .build();
    }

    @Bean
    public Queue refundDlq() {
        return QueueBuilder.durable(MqConstants.REFUND_DLQ).build();
    }

    @Bean
    public Binding refundBinding(Queue refundQueue, TopicExchange ticketExchange) {
        return BindingBuilder.bind(refundQueue).to(ticketExchange).with(MqConstants.ROUTING_REFUND);
    }

    @Bean
    public Binding refundDlqBinding(Queue refundDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(refundDlq).to(deadLetterExchange).with(MqConstants.REFUND_DLQ);
    }
}
