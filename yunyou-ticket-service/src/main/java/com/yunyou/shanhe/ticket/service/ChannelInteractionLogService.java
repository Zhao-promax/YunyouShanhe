package com.yunyou.shanhe.ticket.service;

import com.yunyou.shanhe.ticket.entity.ChannelInteractionLog;
import com.yunyou.shanhe.ticket.mapper.ChannelInteractionLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 渠道交互报文留存。
 */
@Service
@RequiredArgsConstructor
public class ChannelInteractionLogService {

    private final ChannelInteractionLogMapper logMapper;

    public void record(Long channelId, String requestId, Integer direction, String url, String method,
                       String requestBody, String responseBody, Integer httpStatus, Integer durationMs) {
        ChannelInteractionLog log = new ChannelInteractionLog();
        log.setChannelId(channelId);
        log.setRequestId(requestId);
        log.setDirection(direction);
        log.setUrl(url);
        log.setMethod(method);
        log.setRequestBody(requestBody);
        log.setResponseBody(responseBody);
        log.setHttpStatus(httpStatus);
        log.setDurationMs(durationMs);
        log.setCreatedAt(System.currentTimeMillis());
        logMapper.insert(log);
    }
}
