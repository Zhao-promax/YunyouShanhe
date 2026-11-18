package com.yunyou.shanhe.common.event;

/**
 * 通用通知事件（由消息服务消费）。
 */
public record NotifyEvent(
        Long userId,
        String scene,
        String content
) {
}
