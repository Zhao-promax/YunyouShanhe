package com.yunyou.shanhe.pay.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 退款单状态。
 */
@Getter
@AllArgsConstructor
public enum RefundStatus {

    PROCESSING(1, "处理中"),
    SUCCESS(2, "退款成功"),
    FAILED(3, "退款失败");

    private final int code;
    private final String desc;
}
