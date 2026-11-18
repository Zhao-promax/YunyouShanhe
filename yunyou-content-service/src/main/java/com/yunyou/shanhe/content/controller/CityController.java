package com.yunyou.shanhe.content.controller;

import com.yunyou.shanhe.common.core.Result;
import com.yunyou.shanhe.content.entity.City;
import com.yunyou.shanhe.content.service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/content/city")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;

    @GetMapping("/list")
    public Result<List<City>> list() {
        return Result.ok(cityService.list());
    }
}
