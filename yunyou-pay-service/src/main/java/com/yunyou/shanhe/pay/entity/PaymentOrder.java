package com.yunyou.shanhe.pay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 支付单。
 */
@Data
@TableName("payment_order")
public class PaymentOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String payOrderNo;

    private String bizOrderNo;

    private Long userId;

    private Long amount;

    private Integer payChannel;

    private Integer status;

    private String transactionId;

    private Long paidTime;

    private Long createdAt;

    private Long updatedAt;
}
