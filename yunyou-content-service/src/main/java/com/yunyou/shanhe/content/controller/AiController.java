package com.yunyou.shanhe.content.controller;

import com.yunyou.shanhe.common.core.Result;
import com.yunyou.shanhe.content.dto.AiPlanRequest;
import com.yunyou.shanhe.content.service.AiPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/content/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiPlanService aiPlanService;

    @PostMapping("/plan")
    public Result<String> plan(@Valid @RequestBody AiPlanRequest request) {
        return Result.ok(aiPlanService.plan(request.query()));
    }

    @PostMapping(value = "/plan/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter planStream(@Valid @RequestBody AiPlanRequest request) {
        return aiPlanService.streamPlan(request.query());
    }
}
