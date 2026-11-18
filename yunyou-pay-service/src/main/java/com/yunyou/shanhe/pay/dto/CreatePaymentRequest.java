package com.yunyou.shanhe.pay.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePaymentRequest(
        @NotBlank(message = "业务单号不能为空") String bizOrderNo,
        @NotNull(message = "金额不能为空") @Min(value = 1, message = "金额至少为1分") Long amount,
        @NotNull(message = "支付渠道不能为空") Integer payChannel
) {
}
