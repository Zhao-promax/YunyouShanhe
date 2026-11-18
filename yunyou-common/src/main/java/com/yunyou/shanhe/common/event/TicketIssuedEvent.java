package com.yunyou.shanhe.common.event;

/**
 * 出票成功事件（现货支付成功 / 候补中签）。
 */
public record TicketIssuedEvent(
        String orderNo,
        Long userId,
        Long scenicId,
        Long amount
) {
}
