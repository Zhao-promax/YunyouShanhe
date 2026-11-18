package com.yunyou.shanhe.common.event;

/**
 * 退款请求事件（候补失败/取消/熔断、退票）。
 */
public record RefundEvent(
        String bizOrderNo,
        String payOrderNo,
        Long userId,
        Long amount,
        String reason
) {
}
