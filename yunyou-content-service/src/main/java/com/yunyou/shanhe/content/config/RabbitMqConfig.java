package com.yunyou.shanhe.content.config;

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

/**
 * 内容服务 RabbitMQ 拓扑：内容变更交换机 + 同步队列 + 死信队列。
 * 注意：死信交换机 yunyou.dlx 为跨服务共享，类型必须为 direct（与 pay/message 一致）。
 */
@Configuration
public class RabbitMqConfig {

    @Bean
    public TopicExchange contentExchange() {
        return new TopicExchange(MqConstants.CONTENT_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(MqConstants.DEAD_LETTER_EXCHANGE, true, false);
    }

    @Bean
    public Queue contentSyncQueue() {
        return QueueBuilder.durable(MqConstants.CONTENT_SYNC_QUEUE)
                .deadLetterExchange(MqConstants.DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(MqConstants.CONTENT_SYNC_DLQ)
                .build();
    }

    @Bean
    public Queue contentSyncDlq() {
        return QueueBuilder.durable(MqConstants.CONTENT_SYNC_DLQ).build();
    }

    @Bean
    public Binding contentSyncBinding(Queue contentSyncQueue, TopicExchange contentExchange) {
        return BindingBuilder.bind(contentSyncQueue).to(contentExchange).with(MqConstants.ROUTING_CONTENT_CHANGED);
    }

    @Bean
    public Binding contentSyncDlqBinding(Queue contentSyncDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(contentSyncDlq).to(deadLetterExchange).with(MqConstants.CONTENT_SYNC_DLQ);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
