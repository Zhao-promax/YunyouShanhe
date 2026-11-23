package com.yunyou.shanhe.content.service;

import com.yunyou.shanhe.content.entity.City;
import com.yunyou.shanhe.content.entity.Guide;
import com.yunyou.shanhe.content.entity.Scenic;
import com.yunyou.shanhe.content.es.GuideDocument;
import com.yunyou.shanhe.content.es.ScenicDocument;
import com.yunyou.shanhe.content.mapper.CityMapper;
import com.yunyou.shanhe.content.mapper.GuideMapper;
import com.yunyou.shanhe.content.mapper.ScenicMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ES 索引同步服务：以 MySQL 为权威，重读数据后索引到 ES（幂等）。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ContentIndexService {

    private final ScenicMapper scenicMapper;
    private final GuideMapper guideMapper;
    private final CityMapper cityMapper;
    private final ElasticsearchOperations elasticsearchOperations;

    public void indexScenic(Long id) {
        Scenic scenic = scenicMapper.selectById(id);
        if (scenic == null) {
            log.warn("景点不存在，跳过 ES 同步 id={}", id);
            return;
        }
        City city = cityMapper.selectById(scenic.getCityId());
        elasticsearchOperations.save(ScenicDocument.from(scenic, city != null ? city.getName() : ""));
    }

    public void indexGuide(Long id) {
        Guide guide = guideMapper.selectById(id);
        if (guide == null) {
            log.warn("攻略不存在，跳过 ES 同步 id={}", id);
            return;
        }
        elasticsearchOperations.save(GuideDocument.from(guide));
    }

    /** 全量重建：兜底对账，修复所有增量同步漏掉/漏改的数据。 */
    public int rebuildAll() {
        int count = 0;
        List<Scenic> scenics = scenicMapper.selectList(null);
        for (Scenic scenic : scenics) {
            City city = cityMapper.selectById(scenic.getCityId());
            elasticsearchOperations.save(ScenicDocument.from(scenic, city != null ? city.getName() : ""));
            count++;
        }
        List<Guide> guides = guideMapper.selectList(null);
        for (Guide guide : guides) {
            elasticsearchOperations.save(GuideDocument.from(guide));
            count++;
        }
        return count;
    }
}
