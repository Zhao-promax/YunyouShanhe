package com.yunyou.shanhe.ticket.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yunyou.shanhe.common.core.BizException;
import com.yunyou.shanhe.ticket.channel.ChannelLockRequest;
import com.yunyou.shanhe.ticket.channel.ChannelLockResult;
import com.yunyou.shanhe.ticket.channel.ScenicChannelClient;
import com.yunyou.shanhe.ticket.entity.Channel;
import com.yunyou.shanhe.ticket.mapper.ChannelMapper;
import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.yunyou.shanhe.ticket.config.SentinelConfig;
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
        Entry entry = null;
        try {
            entry = SphU.entry(SentinelConfig.resourceName(channel.getId()));
            long start = System.currentTimeMillis();
            ChannelLockResult result = scenicChannelClient.lockTicket(request);
            interactionLogService.record(channel.getId(), request.requestId(), 1, "lockTicket", "POST",
                    request.toString(), null, null, null);
            interactionLogService.record(channel.getId(), request.requestId(), 2, "lockTicket", "POST",
                    null, result.toString(), null, (int) (System.currentTimeMillis() - start));
            return result;
        } catch (BlockException e) {
            throw new BizException("渠道繁忙，请稍后再试");
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
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
