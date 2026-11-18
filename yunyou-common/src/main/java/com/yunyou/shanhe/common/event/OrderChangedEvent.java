package com.yunyou.shanhe.common.event;

/**
 * 订单状态变更事件。
 */
public record OrderChangedEvent(
        String orderNo,
        Integer fromStatus,
        Integer toStatus,
        String reason
) {
}
