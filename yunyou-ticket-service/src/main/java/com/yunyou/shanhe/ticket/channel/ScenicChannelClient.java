package com.yunyou.shanhe.ticket.channel;

/**
 * 景区渠道客户端抽象。目前仅有 Mock 实现，签约真实景区后按渠道路由到真实实现。
 */
public interface ScenicChannelClient {

    ChannelLockResult lockTicket(ChannelLockRequest request);

    ChannelQueryResult queryTicket(String channelOrderId);

    void releaseTicket(String channelOrderId);
}
