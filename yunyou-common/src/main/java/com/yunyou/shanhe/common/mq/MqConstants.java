package com.yunyou.shanhe.common.mq;

/**
 * MQ 交换机与路由键常量（跨服务共享）。
 */
public final class MqConstants {

    private MqConstants() {
    }

    public static final String TICKET_EXCHANGE = "yunyou.ticket.topic";

    public static final String ROUTING_ISSUED = "ticket.issued";
    public static final String ROUTING_REFUND = "ticket.refund";
    public static final String ROUTING_ORDER_CHANGED = "ticket.order.changed";
    public static final String ROUTING_NOTIFY = "ticket.notify";

    public static final String REFUND_QUEUE = "yunyou.pay.refund.queue";
    public static final String NOTIFY_QUEUE = "yunyou.message.notify.queue";

    public static final String DEAD_LETTER_EXCHANGE = "yunyou.dlx";
    public static final String REFUND_DLQ = "yunyou.pay.refund.dlq";
    public static final String NOTIFY_DLQ = "yunyou.message.notify.dlq";

    public static final String CONTENT_EXCHANGE = "yunyou.content.topic";
    public static final String ROUTING_CONTENT_CHANGED = "content.changed";
    public static final String CONTENT_SYNC_QUEUE = "yunyou.content.sync.queue";
    public static final String CONTENT_SYNC_DLQ = "yunyou.content.sync.dlq";
}
