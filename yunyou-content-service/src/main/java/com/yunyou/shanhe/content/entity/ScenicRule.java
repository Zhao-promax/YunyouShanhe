package com.yunyou.shanhe.content.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("scenic_rule")
public class ScenicRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long scenicId;

    private Integer supportWaitlist;

    private Integer waitlistCutoffHour;

    private Integer supportSlotShift;

    private String ticketTime;

    private String openTime;

    private String refundRule;

    private String visitNotice;

    private Long createdAt;

    private Long updatedAt;
}
