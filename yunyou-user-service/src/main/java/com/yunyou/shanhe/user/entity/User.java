package com.yunyou.shanhe.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * 用户主表。
 */
@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String phone;

    @JsonIgnore
    private String passwordHash;

    private String nickname;

    private String avatarUrl;

    private String wechatOpenid;

    private String wechatUnionid;

    private Integer status;

    private Long lastLoginTime;

    private Long createdAt;

    private Long updatedAt;
}
