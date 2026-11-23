package com.yunyou.shanhe.pay.channel;

/**
 * 支付渠道抽象。开发期使用 Mock 实现，签约微信/支付宝后新增真实实现并路由，
 * 业务代码（PaymentService）不感知具体渠道。
 */
public interface PayChannelClient {

    /** 统一下单，返回渠道预支付单号（真实场景用于唤起客户端支付）。 */
    String placeOrder(String payOrderNo, Long amount, Integer payChannel);

    /** 主动反查支付状态，用于回调后的二次核验金额与状态。 */
    boolean queryPaid(String transactionId);

    /** 原路退款。 */
    boolean refund(String transactionId, Long amount);
}
