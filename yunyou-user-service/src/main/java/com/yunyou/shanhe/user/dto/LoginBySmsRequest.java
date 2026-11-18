package com.yunyou.shanhe.user.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginBySmsRequest(
        @NotBlank(message = "手机号不能为空")
        String phone,
        @NotBlank(message = "短信验证码不能为空")
        String smsCode
) {
}
