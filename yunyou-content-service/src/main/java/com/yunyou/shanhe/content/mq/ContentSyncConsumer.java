package com.yunyou.shanhe.content.mq;

import com.rabbitmq.client.Channel;
import com.yunyou.shanhe.common.event.ContentChangedEvent;
import com.yunyou.shanhe.common.mq.MqConstants;
import com.yunyou.shanhe.content.service.ContentIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 内容变更消费者：手动 ACK + 重读 MySQL 索引到 ES（幂等），失败进死信队列。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ContentSyncConsumer {

    private final ContentIndexService contentIndexService;

    @RabbitListener(queues = MqConstants.CONTENT_SYNC_QUEUE)
    public void onContentChanged(ContentChangedEvent event, Message message, Channel channel) throws Exception {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            if ("SCENIC".equals(event.type())) {
                contentIndexService.indexScenic(event.id());
            } else if ("GUIDE".equals(event.type())) {
                contentIndexService.indexGuide(event.id());
            }
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("ES 同步失败，进入死信队列 type={} id={}", event.type(), event.id(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }

    @RabbitListener(queues = MqConstants.CONTENT_SYNC_DLQ)
    public void onDlq(Message message, Channel channel) throws Exception {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        log.error("ES 同步死信消息: {}", new String(message.getBody(), StandardCharsets.UTF_8));
        channel.basicAck(deliveryTag, false);
    }
}
