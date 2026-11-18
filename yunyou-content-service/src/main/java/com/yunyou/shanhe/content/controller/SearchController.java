package com.yunyou.shanhe.content.controller;

import com.yunyou.shanhe.common.core.Result;
import com.yunyou.shanhe.content.es.GuideDocument;
import com.yunyou.shanhe.content.es.ScenicDocument;
import com.yunyou.shanhe.content.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/content/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/scenic")
    public Result<List<ScenicDocument>> scenic(@RequestParam String keyword,
                                               @RequestParam(required = false) Long cityId) {
        return Result.ok(searchService.searchScenic(keyword, cityId));
    }

    @GetMapping("/guide")
    public Result<List<GuideDocument>> guide(@RequestParam String keyword) {
        return Result.ok(searchService.searchGuide(keyword));
    }
}
