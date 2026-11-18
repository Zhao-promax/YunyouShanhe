package com.yunyou.shanhe.ticket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 渠道交互报文留存（原始请求/响应）。
 */
@Data
@TableName("channel_interaction_log")
public class ChannelInteractionLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long channelId;

    private String requestId;

    private Integer direction;

    private String url;

    private String method;

    private String requestBody;

    private String responseBody;

    private Integer httpStatus;

    private Integer durationMs;

    private Long createdAt;
}
