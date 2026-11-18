package com.yunyou.shanhe.message.config;

import com.yunyou.shanhe.common.mq.MqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
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
    public Queue notifyQueue() {
        return new Queue(MqConstants.NOTIFY_QUEUE, true);
    }

    @Bean
    public Binding notifyBinding(Queue notifyQueue, TopicExchange ticketExchange) {
        return BindingBuilder.bind(notifyQueue).to(ticketExchange).with(MqConstants.ROUTING_NOTIFY);
    }
}
