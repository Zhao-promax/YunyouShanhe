package com.yunyou.shanhe.ticket.controller;

import com.yunyou.shanhe.common.core.Result;
import com.yunyou.shanhe.ticket.dto.InventoryReleaseRequest;
import com.yunyou.shanhe.ticket.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 景区回调接口（库存释放 → 触发候补消费）。上线前必须实现验签。
 */
@RestController
@RequestMapping("/api/ticket/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final BookingService bookingService;

    @PostMapping("/inventory")
    public Result<Integer> inventory(@RequestBody InventoryReleaseRequest request) {
        int issued = bookingService.consumeWaitlist(
                request.scenicId(), request.visitDate(), request.timeSlot(), request.count());
        return Result.ok(issued);
    }
}
