package com.yunyou.shanhe.admin.controller;

import com.yunyou.shanhe.admin.dto.AdminLoginRequest;
import com.yunyou.shanhe.admin.entity.AdminUser;
import com.yunyou.shanhe.admin.service.AdminAuthService;
import com.yunyou.shanhe.common.core.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public Result<AdminUser> login(@Valid @RequestBody AdminLoginRequest request) {
        return Result.ok(adminAuthService.login(request));
    }
}
