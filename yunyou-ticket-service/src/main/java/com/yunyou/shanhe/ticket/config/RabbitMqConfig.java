package com.yunyou.shanhe.ticket.config;

import com.yunyou.shanhe.common.mq.MqConstants;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 拓扑配置。队列由消费方（支付/消息服务）声明。
 */
@Configuration
public class RabbitMqConfig {

    @Bean
    public TopicExchange ticketExchange() {
        return new TopicExchange(MqConstants.TICKET_EXCHANGE, true, false);
    }
}
