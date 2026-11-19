package com.yunyou.shanhe.content.es;

import com.yunyou.shanhe.content.entity.Guide;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * 攻略搜索索引文档（中文检索使用 IK 分词器）。
 */
@Data
@Document(indexName = "guide")
public class GuideDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Long)
    private Long scenicId;

    @Field(type = FieldType.Long)
    private Long cityId;

    @Field(type = FieldType.Keyword)
    private String scenicName;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String summary;

    @Field(type = FieldType.Integer)
    private Integer guideType;

    @Field(type = FieldType.Keyword)
    private String season;

    @Field(type = FieldType.Integer)
    private Integer budgetLevel;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String tags;

    public static GuideDocument from(Guide guide) {
        GuideDocument doc = new GuideDocument();
        doc.setId(guide.getId());
        doc.setScenicId(guide.getScenicId());
        doc.setCityId(guide.getCityId());
        doc.setScenicName(guide.getScenicName());
        doc.setTitle(guide.getTitle());
        doc.setSummary(guide.getSummary());
        doc.setGuideType(guide.getGuideType());
        doc.setSeason(guide.getSeason());
        doc.setBudgetLevel(guide.getBudgetLevel());
        doc.setTags(guide.getTags());
        return doc;
    }
}
