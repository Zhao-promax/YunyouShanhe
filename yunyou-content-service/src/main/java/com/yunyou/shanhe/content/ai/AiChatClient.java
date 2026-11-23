package com.yunyou.shanhe.content.ai;

import java.util.function.Consumer;

/**
 * AI 对话渠道抽象。开发/离线用 Mock，生产接入 DeepSeek 等真实大模型，
 * 业务代码（AiPlanService）不感知具体模型提供方。
 */
public interface AiChatClient {

    /** 生成回复（非流式）。 */
    String chat(String systemPrompt, String userMessage);

    /** 流式生成：每产生一段增量内容回调一次 onToken。 */
    void stream(String systemPrompt, String userMessage, Consumer<String> onToken);
}
