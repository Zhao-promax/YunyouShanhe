package com.yunyou.shanhe.user.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginByPasswordRequest(
        @NotBlank(message = "手机号不能为空")
        String phone,
        @NotBlank(message = "密码不能为空")
        String password
) {
}
