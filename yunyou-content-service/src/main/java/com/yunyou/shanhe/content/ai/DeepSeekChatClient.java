package com.yunyou.shanhe.content.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * DeepSeek 真实实现（OpenAI 兼容接口）。
 * <p>
 * 通过 app.ai.provider=deepseek 启用，api-key 从环境变量 YUNYOU_AI_API_KEY 读取，
 * 不硬编码在配置文件（避免密钥入仓）。
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "deepseek", matchIfMissing = true)
public class DeepSeekChatClient implements AiChatClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String apiKey;
    private final String model;

    public DeepSeekChatClient(
            @Value("${app.ai.base-url:https://api.deepseek.com}") String baseUrl,
            @Value("${app.ai.api-key:}") String apiKey,
            @Value("${app.ai.model:deepseek-chat}") String model,
            ObjectMapper objectMapper) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public String chat(String systemPrompt, String userMessage) {
        ensureKey();
        Map<String, Object> body = buildBody(systemPrompt, userMessage, false);
        String response = restClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);
        try {
            JsonNode root = objectMapper.readTree(response);
            return root.path("choices").path(0).path("message").path("content").asText("");
        } catch (Exception e) {
            throw new IllegalStateException("解析 AI 响应失败：" + response, e);
        }
    }

    @Override
    public void stream(String systemPrompt, String userMessage, Consumer<String> onToken) {
        ensureKey();
        try {
            String body = objectMapper.writeValueAsString(buildBody(systemPrompt, userMessage, true));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("data:")) {
                        continue;
                    }
                    String data = line.substring(5).trim();
                    if (data.isEmpty() || "[DONE]".equals(data)) {
                        continue;
                    }
                    try {
                        String delta = objectMapper.readTree(data)
                                .path("choices").path(0).path("delta").path("content").asText("");
                        if (!delta.isEmpty()) {
                            onToken.accept(delta);
                        }
                    } catch (Exception ignored) {
                        // 忽略无法解析的行（keep-alive 注释等）
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("AI 流式调用失败", e);
        }
    }

    private Map<String, Object> buildBody(String systemPrompt, String userMessage, boolean stream) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userMessage)));
        body.put("stream", stream);
        body.put("temperature", 0.7);
        return body;
    }

    private void ensureKey() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("未配置 AI API Key（环境变量 YUNYOU_AI_API_KEY）");
        }
    }
}
