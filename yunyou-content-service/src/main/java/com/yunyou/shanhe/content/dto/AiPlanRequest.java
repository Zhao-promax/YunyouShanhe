package com.yunyou.shanhe.content.dto;

import jakarta.validation.constraints.NotBlank;

public record AiPlanRequest(
        @NotBlank(message = "问题不能为空") String query
) {
}
