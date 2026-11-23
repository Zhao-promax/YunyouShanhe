package com.yunyou.shanhe.common.event;

/**
 * 内容变更事件（景点/攻略），由内容服务发 MQ、ES 同步消费端处理。
 */
public record ContentChangedEvent(
        String type,
        Long id
) {
}
