package com.yunyou.shanhe.ticket.channel;

public record ChannelLockResult(boolean success, String channelOrderId, String message) {

    public static ChannelLockResult success(String channelOrderId) {
        return new ChannelLockResult(true, channelOrderId, "success");
    }

    public static ChannelLockResult fail(String message) {
        return new ChannelLockResult(false, null, message);
    }
}
