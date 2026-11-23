package com.yunyou.shanhe.content.service;

import com.yunyou.shanhe.content.es.GuideDocument;
import com.yunyou.shanhe.content.es.ScenicDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Elasticsearch 检索服务。中文检索使用 IK 分词 + match 查询。
 */
@Service
@RequiredArgsConstructor
public class SearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    public List<ScenicDocument> searchScenic(String keyword, Long cityId, String category,
                                             String season, Integer budgetLevel, Double minRating) {
        List<String> filters = new ArrayList<>();
        if (cityId != null) {
            filters.add("{\"term\":{\"cityId\":" + cityId + "}}");
        }
        if (category != null && !category.isBlank()) {
            filters.add("{\"term\":{\"category\":\"" + escape(category) + "\"}}");
        }
        if (season != null && !season.isBlank()) {
            filters.add("{\"term\":{\"bestSeason\":\"" + escape(season) + "\"}}");
        }
        if (budgetLevel != null) {
            filters.add("{\"term\":{\"budgetLevel\":" + budgetLevel + "}}");
        }
        if (minRating != null) {
            filters.add("{\"range\":{\"rating\":{\"gte\":" + minRating + "}}}");
        }
        String json = "{\"bool\":{\"must\":[{\"multi_match\":{\"query\":\"" + escape(keyword) + "\",\"fields\":[\"name\",\"intro\",\"tags\"]}}]"
                + (filters.isEmpty() ? "" : ",\"filter\":[" + String.join(",", filters) + "]")
                + "}}";
        SearchHits<ScenicDocument> hits = elasticsearchOperations.search(
                new StringQuery(json), ScenicDocument.class);
        return hits.getSearchHits().stream().map(SearchHit::getContent).toList();
    }

    public List<GuideDocument> searchGuide(String keyword, Long cityId, String season,
                                           Integer budgetLevel, Integer guideType) {
        List<String> filters = new ArrayList<>();
        if (cityId != null) {
            filters.add("{\"term\":{\"cityId\":" + cityId + "}}");
        }
        if (season != null && !season.isBlank()) {
            filters.add("{\"term\":{\"season\":\"" + escape(season) + "\"}}");
        }
        if (budgetLevel != null) {
            filters.add("{\"term\":{\"budgetLevel\":" + budgetLevel + "}}");
        }
        if (guideType != null) {
            filters.add("{\"term\":{\"guideType\":" + guideType + "}}");
        }
        String json = "{\"bool\":{\"must\":[{\"multi_match\":{\"query\":\"" + escape(keyword) + "\",\"fields\":[\"title\",\"summary\",\"tags\"]}}]"
                + (filters.isEmpty() ? "" : ",\"filter\":[" + String.join(",", filters) + "]")
                + "}}";
        SearchHits<GuideDocument> hits = elasticsearchOperations.search(
                new StringQuery(json), GuideDocument.class);
        return hits.getSearchHits().stream().map(SearchHit::getContent).toList();
    }

    private String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
