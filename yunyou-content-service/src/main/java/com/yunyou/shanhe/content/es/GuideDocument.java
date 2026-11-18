package com.yunyou.shanhe.content.es;

import com.yunyou.shanhe.content.entity.Guide;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * 攻略搜索索引文档。
 */
@Data
@Document(indexName = "guide")
public class GuideDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Long)
    private Long scenicId;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String summary;

    @Field(type = FieldType.Integer)
    private Integer guideType;

    public static GuideDocument from(Guide guide) {
        GuideDocument doc = new GuideDocument();
        doc.setId(guide.getId());
        doc.setScenicId(guide.getScenicId());
        doc.setTitle(guide.getTitle());
        doc.setSummary(guide.getSummary());
        doc.setGuideType(guide.getGuideType());
        return doc;
    }
}
