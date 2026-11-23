package com.yunyou.shanhe.pay.channel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 支付渠道 Mock 实现（开发期）。接入真实渠道后新增实现，按 payChannel 路由。
 */
@Slf4j
@Component
public class MockPayChannelClient implements PayChannelClient {

    @Override
    public String placeOrder(String payOrderNo, Long amount, Integer payChannel) {
        String prepayId = "MOCK-PREPAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        log.info("[支付Mock] 下单 payOrderNo={} amount={} prepayId={}", payOrderNo, amount, prepayId);
        return prepayId;
    }

    @Override
    public boolean queryPaid(String transactionId) {
        log.info("[支付Mock] 反查支付状态 transactionId={} -> 成功", transactionId);
        return true;
    }

    @Override
    public boolean refund(String transactionId, Long amount) {
        log.info("[支付Mock] 退款 transactionId={} amount={} -> 成功", transactionId, amount);
        return true;
    }
}
