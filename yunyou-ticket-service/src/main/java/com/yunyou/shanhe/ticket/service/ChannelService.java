package com.yunyou.shanhe.ticket.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yunyou.shanhe.common.core.BizException;
import com.yunyou.shanhe.ticket.channel.ChannelLockRequest;
import com.yunyou.shanhe.ticket.channel.ChannelLockResult;
import com.yunyou.shanhe.ticket.channel.ScenicChannelClient;
import com.yunyou.shanhe.ticket.entity.Channel;
import com.yunyou.shanhe.ticket.mapper.ChannelMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 景区渠道服务：渠道配置查询 + 适配器路由 + 交互报文留存。
 */
@Service
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelMapper channelMapper;
    private final ScenicChannelClient scenicChannelClient;
    private final ChannelInteractionLogService interactionLogService;

    public Channel getByScenicId(Long scenicId) {
        Channel channel = channelMapper.selectOne(
                Wrappers.<Channel>lambdaQuery().eq(Channel::getScenicId, scenicId));
        if (channel == null) {
            throw new BizException("景区渠道未配置");
        }
        return channel;
    }

    /**
     * 渠道适配层路由。目前仅有 Mock 实现，后续按 channel 配置路由到真实渠道实现。
     */
    public ScenicChannelClient getClient(Channel channel) {
        return scenicChannelClient;
    }

    /**
     * 锁票并留存交互报文。
     */
    public ChannelLockResult lockTicketWithLog(Channel channel, ChannelLockRequest request) {
        long start = System.currentTimeMillis();
        ChannelLockResult result = scenicChannelClient.lockTicket(request);
        interactionLogService.record(channel.getId(), request.requestId(), 1, "lockTicket", "POST",
                request.toString(), null, null, null);
        interactionLogService.record(channel.getId(), request.requestId(), 2, "lockTicket", "POST",
                null, result.toString(), null, (int) (System.currentTimeMillis() - start));
        return result;
    }

    /**
     * 释放锁票并留存交互报文。
     */
    public void releaseTicketWithLog(Channel channel, String channelOrderId) {
        long start = System.currentTimeMillis();
        scenicChannelClient.releaseTicket(channelOrderId);
        interactionLogService.record(channel.getId(), channelOrderId, 2, "releaseTicket", "POST",
                null, "released", null, (int) (System.currentTimeMillis() - start));
    }
}
