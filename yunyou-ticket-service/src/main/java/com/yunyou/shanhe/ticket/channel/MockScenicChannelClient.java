package com.yunyou.shanhe.ticket.channel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 景区渠道 Mock 实现（开发期）。签约真实景区后新增真实实现并路由。
 */
@Slf4j
@Component
public class MockScenicChannelClient implements ScenicChannelClient {

    @Override
    public ChannelLockResult lockTicket(ChannelLockRequest request) {
        String channelOrderId = "MOCK-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        log.info("[景区Mock] 锁票成功 requestId={} channelOrderId={}", request.requestId(), channelOrderId);
        return ChannelLockResult.success(channelOrderId);
    }

    @Override
    public ChannelQueryResult queryTicket(String channelOrderId) {
        log.info("[景区Mock] 查询票态 channelOrderId={}", channelOrderId);
        return new ChannelQueryResult(true, "SUCCESS", "success");
    }

    @Override
    public void releaseTicket(String channelOrderId) {
        log.info("[景区Mock] 释放锁票 channelOrderId={}", channelOrderId);
    }
}
