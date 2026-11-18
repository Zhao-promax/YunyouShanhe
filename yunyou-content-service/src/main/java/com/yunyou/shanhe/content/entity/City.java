package com.yunyou.shanhe.content.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("city")
public class City {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String province;

    private Integer sort;

    private Integer status;

    private Long createdAt;

    private Long updatedAt;
}
