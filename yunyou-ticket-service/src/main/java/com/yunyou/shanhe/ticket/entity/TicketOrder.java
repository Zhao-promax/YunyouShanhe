package com.yunyou.shanhe.ticket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

/**
 * 平台订单。
 */
@Data
@TableName("ticket_order")
public class TicketOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Integer bizType;

    private Long userId;

    private Long scenicId;

    private LocalDate visitDate;

    private String timeSlot;

    private String ticketType;

    private Integer quantity;

    private Long amount;

    private Integer status;

    private Long channelId;

    private String requestId;

    private String payOrderNo;

    private Long expireTime;

    private Long createdAt;

    private Long updatedAt;
}
