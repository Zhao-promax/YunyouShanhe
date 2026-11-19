package com.yunyou.shanhe.content.es;

import com.yunyou.shanhe.content.entity.Scenic;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * 景点搜索索引文档（中文检索使用 IK 分词器）。
 */
@Data
@Document(indexName = "scenic")
public class ScenicDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Long)
    private Long cityId;

    @Field(type = FieldType.Keyword)
    private String cityName;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String name;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String intro;

    @Field(type = FieldType.Keyword)
    private String address;

    @Field(type = FieldType.Double)
    private Double lng;

    @Field(type = FieldType.Double)
    private Double lat;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Keyword)
    private String bestSeason;

    @Field(type = FieldType.Integer)
    private Integer budgetLevel;

    @Field(type = FieldType.Keyword)
    private String suggestDays;

    @Field(type = FieldType.Double)
    private Double rating;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String tags;

    public static ScenicDocument from(Scenic scenic, String cityName) {
        ScenicDocument doc = new ScenicDocument();
        doc.setId(scenic.getId());
        doc.setCityId(scenic.getCityId());
        doc.setCityName(cityName);
        doc.setName(scenic.getName());
        doc.setIntro(scenic.getIntro());
        doc.setAddress(scenic.getAddress());
        doc.setLng(scenic.getLng());
        doc.setLat(scenic.getLat());
        doc.setCategory(scenic.getCategory());
        doc.setBestSeason(scenic.getBestSeason());
        doc.setBudgetLevel(scenic.getBudgetLevel());
        doc.setSuggestDays(scenic.getSuggestDays());
        doc.setRating(scenic.getRating());
        doc.setTags(scenic.getTags());
        return doc;
    }
}
