package com.yunyou.shanhe.user.dto;

import jakarta.validation.constraints.NotBlank;

public record RealNameRequest(
        @NotBlank(message = "姓名不能为空")
        String realName,
        @NotBlank(message = "证件号不能为空")
        String idCardNo
) {
}
