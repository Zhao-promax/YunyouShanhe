package com.yunyou.shanhe.pay.config;

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
    public Queue refundQueue() {
        return new Queue(MqConstants.REFUND_QUEUE, true);
    }

    @Bean
    public Binding refundBinding(Queue refundQueue, TopicExchange ticketExchange) {
        return BindingBuilder.bind(refundQueue).to(ticketExchange).with(MqConstants.ROUTING_REFUND);
    }
}
