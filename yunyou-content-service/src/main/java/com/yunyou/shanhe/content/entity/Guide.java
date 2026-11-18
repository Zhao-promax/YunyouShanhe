package com.yunyou.shanhe.content.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("guide")
public class Guide {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long scenicId;

    private String title;

    private String summary;

    private String content;

    private Integer guideType;

    private Integer status;

    private Long publishTime;

    private Long createdAt;

    private Long updatedAt;
}
