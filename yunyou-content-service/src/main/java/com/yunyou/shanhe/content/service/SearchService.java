package com.yunyou.shanhe.content.service;

import com.yunyou.shanhe.content.es.GuideDocument;
import com.yunyou.shanhe.content.es.ScenicDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Elasticsearch 检索服务。生产环境建议接入 IK 分词器并改用 match 查询。
 */
@Service
@RequiredArgsConstructor
public class SearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    public List<ScenicDocument> searchScenic(String keyword, Long cityId) {
        Criteria criteria = new Criteria("name").contains(keyword);
        if (cityId != null) {
            criteria = new Criteria("cityId").is(cityId).and("name").contains(keyword);
        }
        SearchHits<ScenicDocument> hits = elasticsearchOperations.search(
                new CriteriaQuery(criteria), ScenicDocument.class);
        return hits.getSearchHits().stream().map(SearchHit::getContent).toList();
    }

    public List<GuideDocument> searchGuide(String keyword) {
        Criteria criteria = new Criteria("title").contains(keyword);
        SearchHits<GuideDocument> hits = elasticsearchOperations.search(
                new CriteriaQuery(criteria), GuideDocument.class);
        return hits.getSearchHits().stream().map(SearchHit::getContent).toList();
    }
}
