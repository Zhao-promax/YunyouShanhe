package com.yunyou.shanhe.user.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 短信渠道 Mock 实现（开发期仅日志输出，上线前接入阿里云短信）。
 */
@Slf4j
@Component
public class MockSmsChannelClient implements SmsChannelClient {

    @Override
    public void send(String phone, String content) {
        log.info("[短信Mock] 手机号 {} 发送内容：{}", phone, content);
    }
}
