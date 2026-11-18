package com.yunyou.shanhe.pay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 退款单。
 */
@Data
@TableName("refund_order")
public class RefundOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String refundNo;

    private String payOrderNo;

    private Long amount;

    private String reason;

    private Integer status;

    private String channelRefundId;

    private Long refundedTime;

    private Long createdAt;

    private Long updatedAt;
}
