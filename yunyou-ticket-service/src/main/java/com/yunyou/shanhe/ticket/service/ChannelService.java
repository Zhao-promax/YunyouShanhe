package com.yunyou.shanhe.ticket.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yunyou.shanhe.common.core.BizException;
import com.yunyou.shanhe.ticket.channel.ScenicChannelClient;
import com.yunyou.shanhe.ticket.entity.Channel;
import com.yunyou.shanhe.ticket.mapper.ChannelMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 景区渠道服务：渠道配置查询 + 适配器路由。
 */
@Service
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelMapper channelMapper;
    private final ScenicChannelClient scenicChannelClient;

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
}
