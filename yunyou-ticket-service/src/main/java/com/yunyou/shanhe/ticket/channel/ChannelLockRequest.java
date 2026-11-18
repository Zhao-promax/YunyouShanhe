package com.yunyou.shanhe.ticket.channel;

import java.time.LocalDate;

public record ChannelLockRequest(
        String requestId,
        Long scenicId,
        LocalDate visitDate,
        String timeSlot,
        String ticketType,
        Integer quantity
) {
}
