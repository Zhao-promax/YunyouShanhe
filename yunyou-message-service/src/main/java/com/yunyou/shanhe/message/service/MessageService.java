package com.yunyou.shanhe.message.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yunyou.shanhe.common.core.BizException;
import com.yunyou.shanhe.common.event.NotifyEvent;
import com.yunyou.shanhe.message.entity.MessageRecord;
import com.yunyou.shanhe.message.entity.Subscription;
import com.yunyou.shanhe.message.mapper.MessageRecordMapper;
import com.yunyou.shanhe.message.mapper.SubscriptionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 消息服务：通知分发（站内信落库 + 短信/推送 Mock）、收件箱、订阅。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private static final int CHANNEL_STATION = 2;

    private final MessageRecordMapper recordMapper;
    private final SubscriptionMapper subscriptionMapper;

    /**
     * 消费通知事件：站内信落库，短信/APP 推送走 Mock（上线接入阿里云短信 + 极光推送）。
     */
    public void dispatch(NotifyEvent event) {
        long now = System.currentTimeMillis();
        MessageRecord record = new MessageRecord();
        record.setUserId(event.userId());
        record.setTemplateCode(event.scene());
        record.setChannel(CHANNEL_STATION);
        record.setContent(event.content());
        record.setStatus(1);
        record.setRetryCount(0);
        record.setReadStatus(0);
        record.setSendTime(now);
        record.setCreatedAt(now);
        recordMapper.insert(record);

        log.info("[短信Mock] 用户 {} 收到短信: {}", event.userId(), event.content());
        log.info("[推送Mock] 用户 {} 收到推送: {}", event.userId(), event.content());
    }

    public List<MessageRecord> inbox(Long userId) {
        return recordMapper.selectList(Wrappers.<MessageRecord>lambdaQuery()
                .eq(MessageRecord::getUserId, userId)
                .eq(MessageRecord::getChannel, CHANNEL_STATION)
                .orderByDesc(MessageRecord::getId));
    }

    public void markRead(Long userId, Long id) {
        MessageRecord record = recordMapper.selectById(id);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BizException("消息不存在");
        }
        MessageRecord update = new MessageRecord();
        update.setId(id);
        update.setReadStatus(1);
        recordMapper.updateById(update);
    }

    public void subscribe(Long userId, Long scenicId) {
        Subscription existing = subscriptionMapper.selectOne(Wrappers.<Subscription>lambdaQuery()
                .eq(Subscription::getUserId, userId)
                .eq(Subscription::getScenicId, scenicId));
        long now = System.currentTimeMillis();
        if (existing != null) {
            if (existing.getStatus() != null && existing.getStatus() == 1) {
                return;
            }
            Subscription update = new Subscription();
            update.setId(existing.getId());
            update.setStatus(1);
            update.setUpdatedAt(now);
            subscriptionMapper.updateById(update);
            return;
        }
        Subscription sub = new Subscription();
        sub.setUserId(userId);
        sub.setScenicId(scenicId);
        sub.setStatus(1);
        sub.setCreatedAt(now);
        sub.setUpdatedAt(now);
        subscriptionMapper.insert(sub);
    }

    public void unsubscribe(Long userId, Long scenicId) {
        Subscription existing = subscriptionMapper.selectOne(Wrappers.<Subscription>lambdaQuery()
                .eq(Subscription::getUserId, userId)
                .eq(Subscription::getScenicId, scenicId));
        if (existing != null) {
            Subscription update = new Subscription();
            update.setId(existing.getId());
            update.setStatus(0);
            update.setUpdatedAt(System.currentTimeMillis());
            subscriptionMapper.updateById(update);
        }
    }
}
