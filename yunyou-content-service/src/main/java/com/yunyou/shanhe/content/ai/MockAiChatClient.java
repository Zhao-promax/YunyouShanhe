package com.yunyou.shanhe.content.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * AI 渠道 Mock 实现（离线 / 测试，不依赖外部模型）。
 * 通过 app.ai.provider=mock 启用。
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "mock")
public class MockAiChatClient implements AiChatClient {

    @Override
    public String chat(String systemPrompt, String userMessage) {
        log.info("[AI Mock] 收到问题：{}", userMessage);
        return "（Mock 旅行建议）这是一份示例行程，供开发调试使用；接入真实模型后此处由 LLM 基于检索结果生成。";
    }

    @Override
    public void stream(String systemPrompt, String userMessage, Consumer<String> onToken) {
        log.info("[AI Mock] 流式生成：{}", userMessage);
        onToken.accept("（Mock 流式回复）");
        onToken.accept("这是一份示例行程，");
        onToken.accept("接入真实模型后逐字生成。");
    }
}
