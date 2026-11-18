package com.yunyou.shanhe.content.controller;

import com.yunyou.shanhe.common.core.Result;
import com.yunyou.shanhe.content.entity.Scenic;
import com.yunyou.shanhe.content.entity.ScenicRule;
import com.yunyou.shanhe.content.service.ScenicRuleService;
import com.yunyou.shanhe.content.service.ScenicService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/content/scenic")
@RequiredArgsConstructor
public class ScenicController {

    private final ScenicService scenicService;
    private final ScenicRuleService ruleService;

    @GetMapping("/list")
    public Result<List<Scenic>> list(@RequestParam(required = false) Long cityId) {
        return Result.ok(scenicService.listByCity(cityId));
    }

    @GetMapping("/{id}")
    public Result<Scenic> detail(@PathVariable Long id) {
        return Result.ok(scenicService.getById(id));
    }

    @PostMapping
    public Result<Scenic> save(@RequestBody Scenic scenic) {
        return Result.ok(scenicService.save(scenic));
    }

    @GetMapping("/nearby")
    public Result<List<Scenic>> nearby(@RequestParam Long scenicId,
                                       @RequestParam(defaultValue = "5") int limit) {
        return Result.ok(scenicService.nearby(scenicId, limit));
    }

    @GetMapping("/{id}/rule")
    public Result<ScenicRule> rule(@PathVariable Long id) {
        return Result.ok(ruleService.getByScenicId(id));
    }

    @PostMapping("/rule")
    public Result<ScenicRule> saveRule(@RequestBody ScenicRule rule) {
        return Result.ok(ruleService.save(rule));
    }
}
