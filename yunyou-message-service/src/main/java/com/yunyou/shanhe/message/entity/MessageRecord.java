package com.yunyou.shanhe.message.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("message_record")
public class MessageRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String templateCode;

    private Integer channel;

    private String content;

    private Integer status;

    private Integer retryCount;

    private Integer readStatus;

    private Long sendTime;

    private Long createdAt;
}
