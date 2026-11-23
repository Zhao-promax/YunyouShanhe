package com.yunyou.shanhe.content.service;

import com.yunyou.shanhe.content.ai.AiChatClient;
import com.yunyou.shanhe.content.entity.City;
import com.yunyou.shanhe.content.entity.Scenic;
import com.yunyou.shanhe.content.es.ScenicDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * AI 旅行规划（RAG）：ES 检索相关景点/攻略 → 组装上下文 → LLM 生成建议。
 * 检索结果 grounding 模型，避免生成不存在的景点（幻觉）。
 */
@Service
@RequiredArgsConstructor
public class AiPlanService {

    private static final String SYSTEM_PROMPT = """
            你是「云游山河」平台的旅行规划助手。请基于提供的真实景点信息，为用户生成旅行建议。
            要求：
            1. 只推荐「参考信息」里真实存在的景点，不要编造景点名称、地址或评分。
            2. 参考信息不足时如实说明，引导用户补充目的地或偏好，不要杜撰。
            3. 输出结构：先给行程概览，再按天给安排（景点名称 + 推荐理由 + 建议游玩时长）。
            4. 用中文，简洁实用。
            """;

    private static final String EXTRACT_PROMPT = """
            从下面的用户问题中提取用于搜索景点/攻略的关键词。
            要求：
            1. 只输出关键词，多个关键词用空格分隔；
            2. 不要输出任何解释、标点、换行或其他内容。
            """;

    private final SearchService searchService;
    private final CityService cityService;
    private final ScenicService scenicService;
    private final AiChatClient aiChatClient;
    private final TaskExecutor aiExecutor;

    public String plan(String query) {
        return aiChatClient.chat(SYSTEM_PROMPT, buildUserMessage(query));
    }

    public SseEmitter streamPlan(String query) {
        String userMessage = buildUserMessage(query);
        SseEmitter emitter = new SseEmitter(120_000L);
        aiExecutor.execute(() -> {
            try {
                aiChatClient.stream(SYSTEM_PROMPT, userMessage, token -> safeSend(emitter, token));
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    private String buildUserMessage(String query) {
        City city = extractCity(query);
        Long cityId = city != null ? city.getId() : null;

        String keyword = extractKeyword(query);
        List<ScenicDocument> esScenics = searchService.searchScenic(keyword, cityId, null, null, null, null);
        String context = !esScenics.isEmpty()
                ? buildContextFromEs(esScenics)
                : buildContextFromDb(cityId != null ? scenicService.list(cityId, null, null, null, null) : List.of(), city);

        return "用户问题：" + query + "\n\n参考信息（真实数据）：\n" + context;
    }

    /** 用 LLM 抽取检索关键词（query 改写），失败回退为原始问题。 */
    private String extractKeyword(String query) {
        try {
            String keyword = aiChatClient.chat(EXTRACT_PROMPT, query);
            return (keyword == null || keyword.isBlank()) ? query : keyword.trim();
        } catch (Exception e) {
            return query;
        }
    }

    private void safeSend(SseEmitter emitter, String token) {
        try {
            emitter.send(SseEmitter.event().data(token));
        } catch (Exception e) {
            // 客户端断开时忽略
        }
    }

    private City extractCity(String query) {
        if (query == null) {
            return null;
        }
        return cityService.list().stream()
                .filter(c -> c.getName() != null && query.contains(c.getName()))
                .findFirst()
                .orElse(null);
    }

    private String buildContextFromEs(List<ScenicDocument> scenics) {
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (ScenicDocument s : scenics) {
            sb.append(i++).append(". ").append(s.getName())
                    .append("（城市：").append(s.getCityName())
                    .append("，分类：").append(s.getCategory())
                    .append("，评分：").append(s.getRating())
                    .append("，建议游玩：").append(s.getSuggestDays())
                    .append("，简介：").append(s.getIntro()).append("）\n");
        }
        return sb.toString();
    }

    private String buildContextFromDb(List<Scenic> scenics, City city) {
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (Scenic s : scenics) {
            sb.append(i++).append(". ").append(s.getName())
                    .append("（城市：").append(city != null ? city.getName() : "")
                    .append("，分类：").append(s.getCategory())
                    .append("，评分：").append(s.getRating())
                    .append("，建议游玩：").append(s.getSuggestDays())
                    .append("，简介：").append(s.getIntro()).append("）\n");
        }
        if (sb.isEmpty()) {
            sb.append("（未检索到相关景点，请引导用户补充目的地或关键词）");
        }
        return sb.toString();
    }
}
