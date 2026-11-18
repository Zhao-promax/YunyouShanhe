package com.yunyou.shanhe.ticket.controller;

import com.yunyou.shanhe.common.core.Result;
import com.yunyou.shanhe.ticket.dto.BookRequest;
import com.yunyou.shanhe.ticket.dto.CancelWaitlistRequest;
import com.yunyou.shanhe.ticket.dto.WaitlistRequest;
import com.yunyou.shanhe.ticket.entity.TicketOrder;
import com.yunyou.shanhe.ticket.entity.WaitlistOrder;
import com.yunyou.shanhe.ticket.service.BookingService;
import com.yunyou.shanhe.ticket.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * C 端票务接口。X-User-Id 由网关鉴权后注入（当前开发期由客户端直接传递）。
 */
@RestController
@RequestMapping("/api/ticket")
@RequiredArgsConstructor
public class TicketController {

    private final BookingService bookingService;
    private final OrderService orderService;

    @PostMapping("/book")
    public Result<TicketOrder> book(@RequestHeader("X-User-Id") Long userId,
                                    @Valid @RequestBody BookRequest request) {
        return Result.ok(bookingService.bookSpot(userId, request));
    }

    @PostMapping("/waitlist")
    public Result<WaitlistOrder> waitlist(@RequestHeader("X-User-Id") Long userId,
                                          @Valid @RequestBody WaitlistRequest request) {
        return Result.ok(bookingService.registerWaitlist(userId, request));
    }

    @PostMapping("/waitlist/cancel")
    public Result<Void> cancelWaitlist(@RequestHeader("X-User-Id") Long userId,
                                       @Valid @RequestBody CancelWaitlistRequest request) {
        bookingService.cancelWaitlist(userId, request.waitlistNo());
        return Result.ok();
    }

    @GetMapping("/order/{orderNo}")
    public Result<TicketOrder> order(@PathVariable String orderNo) {
        return Result.ok(orderService.getByOrderNo(orderNo));
    }
}
