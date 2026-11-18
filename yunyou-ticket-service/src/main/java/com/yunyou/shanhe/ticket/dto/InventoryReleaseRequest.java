package com.yunyou.shanhe.ticket.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record InventoryReleaseRequest(
        @NotNull(message = "景点不能为空") Long scenicId,
        @NotNull(message = "游玩日期不能为空") LocalDate visitDate,
        @NotBlank(message = "时段不能为空") String timeSlot,
        @NotNull(message = "释放数量不能为空") @Min(value = 1, message = "数量至少为1") Integer count
) {
}
