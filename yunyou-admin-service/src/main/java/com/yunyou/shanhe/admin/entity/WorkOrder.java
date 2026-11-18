package com.yunyou.shanhe.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("work_order")
public class WorkOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String workOrderNo;

    private Integer type;

    private String bizOrderNo;

    private Integer status;

    private Long handlerId;

    private String remark;

    private Long createdAt;

    private Long updatedAt;
}
