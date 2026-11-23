package com.yunyou.shanhe.pay.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yunyou.shanhe.common.core.BizException;
import com.yunyou.shanhe.pay.channel.PayChannelClient;
import com.yunyou.shanhe.pay.dto.CreatePaymentRequest;
import com.yunyou.shanhe.pay.dto.PaymentCallbackRequest;
import com.yunyou.shanhe.pay.entity.FundLedger;
import com.yunyou.shanhe.pay.entity.PaymentOrder;
import com.yunyou.shanhe.pay.entity.RefundOrder;
import com.yunyou.shanhe.pay.enums.PaymentStatus;
import com.yunyou.shanhe.pay.enums.RefundStatus;
import com.yunyou.shanhe.pay.mapper.FundLedgerMapper;
import com.yunyou.shanhe.pay.mapper.PaymentOrderMapper;
import com.yunyou.shanhe.pay.mapper.RefundOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 支付服务：支付下单、回调确认、原路退款、资金台账。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentOrderMapper paymentMapper;
    private final RefundOrderMapper refundMapper;
    private final FundLedgerMapper ledgerMapper;
    private final PayChannelClient payChannelClient;

    public PaymentOrder createPayment(Long userId, CreatePaymentRequest request) {
        long now = System.currentTimeMillis();
        PaymentOrder order = new PaymentOrder();
        order.setPayOrderNo(genNo("PAY"));
        order.setBizOrderNo(request.bizOrderNo());
        order.setUserId(userId);
        order.setAmount(request.amount());
        order.setPayChannel(request.payChannel());
        order.setStatus(PaymentStatus.PENDING.getCode());
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        paymentMapper.insert(order);
        String prepayId = payChannelClient.placeOrder(order.getPayOrderNo(), order.getAmount(), order.getPayChannel());
        log.info("生成支付单 payOrderNo={} prepayId={}", order.getPayOrderNo(), prepayId);
        return order;
    }

    public void mockCallback(PaymentCallbackRequest request) {
        PaymentOrder order = getByPayOrderNo(request.payOrderNo());
        if (order == null) {
            throw new BizException("支付单不存在");
        }
        if (order.getStatus() != PaymentStatus.PENDING.getCode()) {
            throw new BizException("支付单状态不允许回调");
        }
        if (!payChannelClient.queryPaid(request.transactionId())) {
            throw new BizException("支付核验失败");
        }
        long now = System.currentTimeMillis();
        PaymentOrder update = new PaymentOrder();
        update.setId(order.getId());
        update.setStatus(PaymentStatus.SUCCESS.getCode());
        update.setTransactionId(request.transactionId());
        update.setPaidTime(now);
        update.setUpdatedAt(now);
        paymentMapper.updateById(update);
        recordLedger(order.getUserId(), order.getPayOrderNo(), 1, order.getAmount());
    }

    /**
     * 原路退款。支付单不存在时跳过（幂等容错），生产需改为精确幂等 + 重试。
     */
    public void refund(String payOrderNo, Long amount, String reason) {
        PaymentOrder order = getByPayOrderNo(payOrderNo);
        if (order == null || order.getStatus() != PaymentStatus.SUCCESS.getCode()) {
            log.warn("退款跳过：支付单不存在或非成功状态 payOrderNo={}", payOrderNo);
            return;
        }
        long now = System.currentTimeMillis();
        Long refundAmount = amount != null && amount > 0 ? amount : order.getAmount();
        if (!payChannelClient.refund(order.getTransactionId(), refundAmount)) {
            throw new BizException("退款失败");
        }
        RefundOrder refund = new RefundOrder();
        refund.setRefundNo(genNo("RF"));
        refund.setPayOrderNo(payOrderNo);
        refund.setAmount(refundAmount);
        refund.setReason(reason);
        refund.setStatus(RefundStatus.SUCCESS.getCode());
        refund.setRefundedTime(now);
        refund.setCreatedAt(now);
        refund.setUpdatedAt(now);
        refundMapper.insert(refund);

        PaymentOrder update = new PaymentOrder();
        update.setId(order.getId());
        update.setStatus(PaymentStatus.REFUNDED.getCode());
        update.setUpdatedAt(now);
        paymentMapper.updateById(update);

        recordLedger(order.getUserId(), payOrderNo, 2, -(order.getAmount()));
    }

    public PaymentOrder getByPayOrderNo(String payOrderNo) {
        return paymentMapper.selectOne(Wrappers.<PaymentOrder>lambdaQuery()
                .eq(PaymentOrder::getPayOrderNo, payOrderNo));
    }

    private void recordLedger(Long userId, String payOrderNo, Integer bizType, Long amount) {
        long now = System.currentTimeMillis();
        FundLedger ledger = new FundLedger();
        ledger.setLedgerNo(genNo("LD"));
        ledger.setUserId(userId);
        ledger.setPayOrderNo(payOrderNo);
        ledger.setBizType(bizType);
        ledger.setAmount(amount);
        ledger.setBalanceAfter(0L); // TODO 计算变动后余额
        ledger.setCreatedAt(now);
        ledgerMapper.insert(ledger);
    }

    private String genNo(String prefix) {
        return prefix + System.currentTimeMillis() + (int) (Math.random() * 10000);
    }
}
