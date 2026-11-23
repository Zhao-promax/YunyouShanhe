package com.yunyou.shanhe.content.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yunyou.shanhe.content.entity.Guide;
import com.yunyou.shanhe.content.entity.Scenic;
import com.yunyou.shanhe.content.mapper.GuideMapper;
import com.yunyou.shanhe.content.mapper.ScenicMapper;
import com.yunyou.shanhe.content.mq.ContentEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 攻略服务：多维度筛选 + CRUD + 异步同步 ES。
 */
@Service
@RequiredArgsConstructor
public class GuideService {

    private final GuideMapper guideMapper;
    private final ScenicMapper scenicMapper;
    private final ContentEventPublisher contentEventPublisher;

    public List<Guide> list(Long scenicId, Long cityId, String season, Integer budgetLevel, Integer guideType) {
        return guideMapper.selectList(Wrappers.<Guide>lambdaQuery()
                .eq(scenicId != null, Guide::getScenicId, scenicId)
                .eq(cityId != null, Guide::getCityId, cityId)
                .eq(season != null && !season.isBlank(), Guide::getSeason, season)
                .eq(budgetLevel != null, Guide::getBudgetLevel, budgetLevel)
                .eq(guideType != null, Guide::getGuideType, guideType)
                .eq(Guide::getStatus, 1)
                .orderByDesc(Guide::getPublishTime));
    }

    public Guide getById(Long id) {
        return guideMapper.selectById(id);
    }

    @Transactional
    public Guide save(Guide guide) {
        long now = System.currentTimeMillis();
        // 冗余城市与景点名，便于按城市筛选与展示
        if (guide.getScenicId() != null) {
            Scenic scenic = scenicMapper.selectById(guide.getScenicId());
            if (scenic != null) {
                guide.setCityId(scenic.getCityId());
                guide.setScenicName(scenic.getName());
            }
        }
        if (guide.getId() == null) {
            guide.setCreatedAt(now);
            guide.setUpdatedAt(now);
            guideMapper.insert(guide);
        } else {
            guide.setUpdatedAt(now);
            guideMapper.updateById(guide);
        }
        // 异步同步 ES
        contentEventPublisher.record("GUIDE", guide.getId());
        return guide;
    }
}
