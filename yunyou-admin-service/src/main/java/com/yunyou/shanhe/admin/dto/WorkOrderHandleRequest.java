package com.yunyou.shanhe.admin.dto;

import jakarta.validation.constraints.NotNull;

public record WorkOrderHandleRequest(
        @NotNull(message = "处理人不能为空") Long handlerId,
        @NotNull(message = "状态不能为空") Integer status,
        String remark
) {
}
