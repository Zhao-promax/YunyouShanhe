package com.yunyou.shanhe.user.sms;

/**
 * 短信渠道抽象。开发期使用 Mock 实现，上线前接入阿里云短信等真实实现。
 */
public interface SmsChannelClient {

    void send(String phone, String content);
}
