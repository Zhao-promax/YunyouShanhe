package com.yunyou.shanhe.content.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 本地消息表（outbox）：与业务写同事务落库，异步投递 MQ，失败退避重试。
 */
@Data
@TableName("outbox_message")
public class OutboxMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String eventId;

    private String eventType;

    private String payload;

    /** 0=待发送 1=已发送 2=发送失败 */
    private Integer status;

    /** 已重试次数 */
    private Integer retryCount;

    /** 下次重试时间戳（毫秒），0=立即可发 */
    private Long nextRetryAt;

    /** 最后一次发送失败原因 */
    private String errorMsg;

    private Long createdAt;

    private Long sentAt;
}
