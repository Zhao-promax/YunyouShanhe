package com.yunyou.shanhe.content.es;

import com.yunyou.shanhe.content.entity.Scenic;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * 景点搜索索引文档。
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

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Text)
    private String intro;

    @Field(type = FieldType.Keyword)
    private String address;

    @Field(type = FieldType.Double)
    private Double lng;

    @Field(type = FieldType.Double)
    private Double lat;

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
        return doc;
    }
}
