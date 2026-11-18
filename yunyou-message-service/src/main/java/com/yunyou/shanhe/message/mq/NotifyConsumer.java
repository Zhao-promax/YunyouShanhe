package com.yunyou.shanhe.message.mq;

import com.yunyou.shanhe.common.event.NotifyEvent;
import com.yunyou.shanhe.common.mq.MqConstants;
import com.yunyou.shanhe.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 通知事件消费者。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotifyConsumer {

    private final MessageService messageService;

    @RabbitListener(queues = MqConstants.NOTIFY_QUEUE)
    public void onNotify(NotifyEvent event) {
        log.info("收到通知事件: {}", event);
        messageService.dispatch(event);
    }
}
