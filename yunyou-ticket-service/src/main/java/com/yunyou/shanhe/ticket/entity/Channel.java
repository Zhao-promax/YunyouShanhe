package com.yunyou.shanhe.ticket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 景区渠道配置。
 */
@Data
@TableName("channel")
public class Channel {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long scenicId;

    private String name;

    private String appKey;

    private String signKeyRef;

    private Integer qpsLimit;

    private Integer pollInterval;

    private String degradeParams;

    private Integer status;

    private Long createdAt;

    private Long updatedAt;
}
