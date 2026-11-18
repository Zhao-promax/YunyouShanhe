package com.yunyou.shanhe.ticket.dto;

import jakarta.validation.constraints.NotBlank;

public record CancelWaitlistRequest(
        @NotBlank(message = "候补单号不能为空") String waitlistNo
) {
}
