package com.yunyou.shanhe.message.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("subscription")
public class Subscription {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long scenicId;

    private Integer status;

    private Long createdAt;

    private Long updatedAt;
}
