package com.yunyou.shanhe.ticket.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 候补单状态。
 */
@Getter
@AllArgsConstructor
public enum WaitlistStatus {

    QUEUING(1, "排队中"),
    WON(2, "已中签"),
    CANCELLED(3, "已取消"),
    CUTOFF_FAILED(4, "熔断失败");

    private final int code;
    private final String desc;
}
