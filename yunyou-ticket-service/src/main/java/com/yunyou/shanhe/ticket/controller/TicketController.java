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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

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

    /**
     * 现货支付确认（开发期 Mock 支付回调；生产由支付服务回调触发）。
     */
    @PostMapping("/order/{orderNo}/confirm-pay")
    public Result<Void> confirmPay(@PathVariable String orderNo) {
        bookingService.confirmPayment(orderNo);
        return Result.ok();
    }

    @PostMapping("/order/{orderNo}/refund")
    public Result<Void> refund(@RequestHeader("X-User-Id") Long userId, @PathVariable String orderNo) {
        bookingService.refundTicket(userId, orderNo);
        return Result.ok();
    }

    /**
     * 现货订单超时关闭（内部/定时任务调用）。
     */
    @PostMapping("/order/{orderNo}/close")
    public Result<Void> close(@PathVariable String orderNo) {
        bookingService.closeExpiredOrder(orderNo);
        return Result.ok();
    }

    /**
     * 候补熔断（内部/定时任务调用）。
     */
    @PostMapping("/waitlist/cutoff")
    public Result<Integer> cutoff(@RequestParam Long scenicId,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate visitDate,
                                  @RequestParam String slot) {
        return Result.ok(bookingService.cutoffWaitlists(scenicId, visitDate, slot));
    }
}
