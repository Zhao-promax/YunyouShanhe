package com.yunyou.shanhe.pay.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentCallbackRequest(
        @NotBlank(message = "支付单号不能为空") String payOrderNo,
        @NotBlank(message = "渠道流水号不能为空") String transactionId
) {
}
