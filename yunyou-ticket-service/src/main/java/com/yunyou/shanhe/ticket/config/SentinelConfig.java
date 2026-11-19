package com.yunyou.shanhe.ticket.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.yunyou.shanhe.ticket.entity.Channel;
import com.yunyou.shanhe.ticket.mapper.ChannelMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Sentinel 限流配置：按渠道加载 QPS 规则（渠道 QPS 可不同）。
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class SentinelConfig {

    private final ChannelMapper channelMapper;

    @PostConstruct
    public void initFlowRules() {
        reloadRules();
    }

    public void reloadRules() {
        List<Channel> channels = channelMapper.selectList(null);
        List<FlowRule> rules = channels.stream()
                .filter(c -> c.getQpsLimit() != null && c.getQpsLimit() > 0)
                .map(c -> {
                    FlowRule rule = new FlowRule();
                    rule.setResource(resourceName(c.getId()));
                    rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
                    rule.setCount(c.getQpsLimit().doubleValue());
                    return rule;
                })
                .collect(Collectors.toList());
        FlowRuleManager.loadRules(rules);
        log.info("Sentinel 限流规则加载完成，共 {} 条", rules.size());
    }

    public static String resourceName(Long channelId) {
        return "channel:lock:" + channelId;
    }
}
