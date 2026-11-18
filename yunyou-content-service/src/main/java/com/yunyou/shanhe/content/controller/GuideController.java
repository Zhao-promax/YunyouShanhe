package com.yunyou.shanhe.content.controller;

import com.yunyou.shanhe.common.core.Result;
import com.yunyou.shanhe.content.entity.Guide;
import com.yunyou.shanhe.content.service.GuideService;
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
@RequestMapping("/api/content/guide")
@RequiredArgsConstructor
public class GuideController {

    private final GuideService guideService;

    @GetMapping("/list")
    public Result<List<Guide>> list(@RequestParam(required = false) Long scenicId,
                                    @RequestParam(required = false) Long cityId,
                                    @RequestParam(required = false) String season,
                                    @RequestParam(required = false) Integer budgetLevel,
                                    @RequestParam(required = false) Integer guideType) {
        return Result.ok(guideService.list(scenicId, cityId, season, budgetLevel, guideType));
    }

    @GetMapping("/{id}")
    public Result<Guide> detail(@PathVariable Long id) {
        return Result.ok(guideService.getById(id));
    }

    @PostMapping
    public Result<Guide> save(@RequestBody Guide guide) {
        return Result.ok(guideService.save(guide));
    }
}
