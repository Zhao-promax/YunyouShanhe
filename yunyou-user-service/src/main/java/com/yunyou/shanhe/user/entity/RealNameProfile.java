package com.yunyou.shanhe.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * 出行人实名档案。证件号 AES 密文存储，接口返回脱敏。
 */
@Data
@TableName("real_name_profile")
public class RealNameProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String realName;

    @JsonIgnore
    private String idCardNoEnc;

    private Integer idCardType;

    private String idCardMask;

    private Integer status;

    private Long archivedAt;

    private Long createdAt;

    private Long updatedAt;
}
