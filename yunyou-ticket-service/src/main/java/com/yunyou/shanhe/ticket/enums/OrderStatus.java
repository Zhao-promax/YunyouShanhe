package com.yunyou.shanhe.ticket.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 现货订单状态（单向流转）。
 */
@Getter
@AllArgsConstructor
public enum OrderStatus {

    PENDING_PAY(1, "待支付"),
    ISSUED(2, "已出票"),
    CLOSED(3, "已关闭"),
    REFUNDED(4, "已退票");

    private final int code;
    private final String desc;
}
