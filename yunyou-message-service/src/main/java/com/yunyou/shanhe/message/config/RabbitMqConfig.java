package com.yunyou.shanhe.message.config;

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
    public Queue notifyQueue() {
        return QueueBuilder.durable(MqConstants.NOTIFY_QUEUE)
                .deadLetterExchange(MqConstants.DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(MqConstants.NOTIFY_DLQ)
                .build();
    }

    @Bean
    public Queue notifyDlq() {
        return QueueBuilder.durable(MqConstants.NOTIFY_DLQ).build();
    }

    @Bean
    public Binding notifyBinding(Queue notifyQueue, TopicExchange ticketExchange) {
        return BindingBuilder.bind(notifyQueue).to(ticketExchange).with(MqConstants.ROUTING_NOTIFY);
    }

    @Bean
    public Binding notifyDlqBinding(Queue notifyDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(notifyDlq).to(deadLetterExchange).with(MqConstants.NOTIFY_DLQ);
    }
}
