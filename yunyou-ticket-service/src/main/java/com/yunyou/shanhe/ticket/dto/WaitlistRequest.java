package com.yunyou.shanhe.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record WaitlistRequest(
        @NotNull(message = "景点不能为空") Long scenicId,
        @NotNull(message = "游玩日期不能为空") LocalDate visitDate,
        @NotBlank(message = "首选时段不能为空") String preferredSlot,
        String shiftSlots
) {
}
