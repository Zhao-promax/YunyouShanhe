package com.yunyou.shanhe.content.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yunyou.shanhe.content.entity.ScenicRule;
import com.yunyou.shanhe.content.mapper.ScenicRuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 景点预约规则配置服务（供票务服务读取）。
 */
@Service
@RequiredArgsConstructor
public class ScenicRuleService {

    private final ScenicRuleMapper ruleMapper;

    public ScenicRule getByScenicId(Long scenicId) {
        return ruleMapper.selectOne(Wrappers.<ScenicRule>lambdaQuery()
                .eq(ScenicRule::getScenicId, scenicId));
    }

    public ScenicRule save(ScenicRule rule) {
        long now = System.currentTimeMillis();
        if (rule.getId() == null) {
            rule.setCreatedAt(now);
            rule.setUpdatedAt(now);
            ruleMapper.insert(rule);
        } else {
            rule.setUpdatedAt(now);
            ruleMapper.updateById(rule);
        }
        return rule;
    }
}
