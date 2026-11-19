package com.yunyou.shanhe.ticket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 票种价格。
 */
@Data
@TableName("ticket_type")
public class TicketType {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long scenicId;

    private String typeCode;

    private String typeName;

    private Long price;

    private Integer status;

    private Long createdAt;

    private Long updatedAt;
}
