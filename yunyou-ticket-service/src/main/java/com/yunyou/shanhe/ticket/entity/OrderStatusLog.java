package com.yunyou.shanhe.ticket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 订单状态变更日志。
 */
@Data
@TableName("order_status_log")
public class OrderStatusLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Integer fromStatus;

    private Integer toStatus;

    private String reason;

    private String operator;

    private Long createdAt;
}
