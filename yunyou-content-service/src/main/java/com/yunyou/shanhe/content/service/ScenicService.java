package com.yunyou.shanhe.content.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yunyou.shanhe.content.cache.MultiLevelCache;
import com.yunyou.shanhe.content.entity.Scenic;
import com.yunyou.shanhe.content.mapper.ScenicMapper;
import com.yunyou.shanhe.content.mq.ContentEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;

/**
 * 景点服务：多维度筛选 + CRUD + 同片区顺路推荐。
 * <p>
 * 读路径走多级缓存；写路径写 MySQL + outbox（同事务），异步同步 ES。
 */
@Service
@RequiredArgsConstructor
public class ScenicService {

    private static final String KEY_PREFIX = "scenic:";
    private static final Duration TTL = Duration.ofMinutes(5);

    private final ScenicMapper scenicMapper;
    private final MultiLevelCache cache;
    private final ObjectMapper objectMapper;
    private final ContentEventPublisher contentEventPublisher;

    public List<Scenic> list(Long cityId, String category, String season, Integer budgetLevel, Double minRating) {
        String key = KEY_PREFIX + "list:" + nvl(cityId) + ":" + nvl(category) + ":" + nvl(season)
                + ":" + nvl(budgetLevel) + ":" + nvl(minRating);
        return cache.getOrLoad(key, listType(), TTL,
                () -> queryList(cityId, category, season, budgetLevel, minRating));
    }

    private List<Scenic> queryList(Long cityId, String category, String season, Integer budgetLevel, Double minRating) {
        return scenicMapper.selectList(Wrappers.<Scenic>lambdaQuery()
                .eq(cityId != null, Scenic::getCityId, cityId)
                .eq(category != null && !category.isBlank(), Scenic::getCategory, category)
                .eq(season != null && !season.isBlank(), Scenic::getBestSeason, season)
                .eq(budgetLevel != null, Scenic::getBudgetLevel, budgetLevel)
                .ge(minRating != null, Scenic::getRating, minRating)
                .eq(Scenic::getStatus, 1)
                .orderByAsc(Scenic::getSort));
    }

    public Scenic getById(Long id) {
        if (id == null) {
            return null;
        }
        return cache.getOrLoad(KEY_PREFIX + "detail:" + id, objectMapper.constructType(Scenic.class), TTL,
                () -> scenicMapper.selectById(id));
    }

    @Transactional
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
        // 异步同步 ES：落 outbox（与业务写同事务），由定时任务投递 MQ → 消费端索引
        contentEventPublisher.record("SCENIC", scenic.getId());
        // 写后主动失效，避免读旧数据
        cache.evictByPrefix(KEY_PREFIX);
        return scenic;
    }

    /**
     * 同片区顺路推荐：同城市内按地理距离升序。
     */
    public List<Scenic> nearby(Long scenicId, int limit) {
        if (scenicId == null) {
            return List.of();
        }
        String key = KEY_PREFIX + "nearby:" + scenicId + ":" + limit;
        return cache.getOrLoad(key, listType(), TTL, () -> queryNearby(scenicId, limit));
    }

    private List<Scenic> queryNearby(Long scenicId, int limit) {
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

    private JavaType listType() {
        return objectMapper.getTypeFactory().constructCollectionType(List.class, Scenic.class);
    }

    private String nvl(Object o) {
        return o == null ? "" : String.valueOf(o);
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
