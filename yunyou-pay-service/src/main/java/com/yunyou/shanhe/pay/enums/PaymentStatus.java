package com.yunyou.shanhe.pay.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付单状态。
 */
@Getter
@AllArgsConstructor
public enum PaymentStatus {

    PENDING(1, "待支付"),
    SUCCESS(2, "支付成功"),
    REFUNDED(3, "已退款"),
    CLOSED(4, "已关闭");

    private final int code;
    private final String desc;
}
