package com.yunyou.shanhe.content.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yunyou.shanhe.content.entity.City;
import com.yunyou.shanhe.content.entity.Scenic;
import com.yunyou.shanhe.content.es.ScenicDocument;
import com.yunyou.shanhe.content.mapper.CityMapper;
import com.yunyou.shanhe.content.mapper.ScenicMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * 景点服务：CRUD + ES 索引同步 + 同片区顺路推荐。
 */
@Service
@RequiredArgsConstructor
public class ScenicService {

    private final ScenicMapper scenicMapper;
    private final CityMapper cityMapper;
    private final ElasticsearchOperations elasticsearchOperations;

    public List<Scenic> listByCity(Long cityId) {
        return scenicMapper.selectList(Wrappers.<Scenic>lambdaQuery()
                .eq(cityId != null, Scenic::getCityId, cityId)
                .eq(Scenic::getStatus, 1)
                .orderByAsc(Scenic::getSort));
    }

    public Scenic getById(Long id) {
        return scenicMapper.selectById(id);
    }

    public Scenic save(Scenic scenic) {
        long now = System.currentTimeMillis();
        if (scenic.getId() == null) {
            scenic.setCreatedAt(now);
            scenic.setUpdatedAt(now);
            scenicMapper.insert(scenic);
        } else {
            scenic.setUpdatedAt(now);
            scenicMapper.updateById(scenic);
        }
        indexToEs(scenic);
        return scenic;
    }

    /**
     * 同片区顺路推荐：同城市内按地理距离升序。
     */
    public List<Scenic> nearby(Long scenicId, int limit) {
        Scenic target = scenicMapper.selectById(scenicId);
        if (target == null) {
            return List.of();
        }
        List<Scenic> sameCity = scenicMapper.selectList(Wrappers.<Scenic>lambdaQuery()
                .eq(Scenic::getCityId, target.getCityId())
                .eq(Scenic::getStatus, 1)
                .ne(Scenic::getId, scenicId));
        return sameCity.stream()
                .sorted(Comparator.comparingDouble(s -> distance(target, s)))
                .limit(limit)
                .toList();
    }

    private void indexToEs(Scenic scenic) {
        City city = cityMapper.selectById(scenic.getCityId());
        String cityName = city != null ? city.getName() : "";
        elasticsearchOperations.save(ScenicDocument.from(scenic, cityName));
    }

    private double distance(Scenic a, Scenic b) {
        if (a.getLat() == null || a.getLng() == null || b.getLat() == null || b.getLng() == null) {
            return Double.MAX_VALUE;
        }
        double r = 6371000;
        double dLat = Math.toRadians(b.getLat() - a.getLat());
        double dLng = Math.toRadians(b.getLng() - a.getLng());
        double lat1 = Math.toRadians(a.getLat());
        double lat2 = Math.toRadians(b.getLat());
        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return 2 * r * Math.asin(Math.sqrt(h));
    }
}
