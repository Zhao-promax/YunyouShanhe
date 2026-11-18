package com.yunyou.shanhe.content.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("scenic")
public class Scenic {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long cityId;

    private String name;

    private String coverUrl;

    private String address;

    private Double lng;

    private Double lat;

    private String intro;

    private String category;

    private String bestSeason;

    private Integer budgetLevel;

    private String suggestDays;

    private Double rating;

    private String tags;

    private Integer sort;

    private Integer status;

    private Long createdAt;

    private Long updatedAt;
}
