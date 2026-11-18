package com.yunyou.shanhe.ticket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

/**
 * 候补单。数据库为权威状态，Redis ZSET 仅为排序快照。
 */
@Data
@TableName("waitlist_order")
public class WaitlistOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String waitlistNo;

    private Long userId;

    private Long scenicId;

    private LocalDate visitDate;

    private String preferredSlot;

    private String shiftSlots;

    private Integer status;

    private Long enqueueTime;

    private String payOrderNo;

    private Long orderId;

    private Long createdAt;

    private Long updatedAt;
}
