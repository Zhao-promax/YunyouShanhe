package com.yunyou.shanhe.pay.controller;

import com.yunyou.shanhe.common.core.Result;
import com.yunyou.shanhe.pay.dto.CreatePaymentRequest;
import com.yunyou.shanhe.pay.dto.PaymentCallbackRequest;
import com.yunyou.shanhe.pay.entity.PaymentOrder;
import com.yunyou.shanhe.pay.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pay")
@RequiredArgsConstructor
public class PayController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    public Result<PaymentOrder> create(@RequestHeader("X-User-Id") Long userId,
                                       @Valid @RequestBody CreatePaymentRequest request) {
        return Result.ok(paymentService.createPayment(userId, request));
    }

    @PostMapping("/callback/mock")
    public Result<Void> callback(@Valid @RequestBody PaymentCallbackRequest request) {
        paymentService.mockCallback(request);
        return Result.ok();
    }

    @GetMapping("/order/{payOrderNo}")
    public Result<PaymentOrder> query(@PathVariable String payOrderNo) {
        return Result.ok(paymentService.getByPayOrderNo(payOrderNo));
    }
}
