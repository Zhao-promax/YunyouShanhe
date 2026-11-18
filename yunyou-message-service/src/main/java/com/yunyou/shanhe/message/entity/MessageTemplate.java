package com.yunyou.shanhe.message.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("message_template")
public class MessageTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String templateCode;

    private Integer channel;

    private String title;

    private String content;

    private Integer status;

    private Long createdAt;

    private Long updatedAt;
}
