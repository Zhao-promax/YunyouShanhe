package com.yunyou.shanhe.user.controller;

import com.yunyou.shanhe.common.core.BizException;
import com.yunyou.shanhe.common.core.ErrorCode;
import com.yunyou.shanhe.common.core.Result;
import com.yunyou.shanhe.user.dto.LoginByPasswordRequest;
import com.yunyou.shanhe.user.dto.LoginBySmsRequest;
import com.yunyou.shanhe.user.dto.RegisterRequest;
import com.yunyou.shanhe.user.dto.SendSmsRequest;
import com.yunyou.shanhe.user.dto.TokenResponse;
import com.yunyou.shanhe.user.entity.User;
import com.yunyou.shanhe.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sms/send")
    public Result<Void> sendSms(@Valid @RequestBody SendSmsRequest request) {
        authService.sendSmsCode(request);
        return Result.ok();
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return Result.ok();
    }

    @PostMapping("/login/password")
    public Result<TokenResponse> loginByPassword(@Valid @RequestBody LoginByPasswordRequest request) {
        return Result.ok(authService.loginByPassword(request));
    }

    @PostMapping("/login/sms")
    public Result<TokenResponse> loginBySms(@Valid @RequestBody LoginBySmsRequest request) {
        return Result.ok(authService.loginBySms(request));
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            authService.logout(authorization.substring(7));
        }
        return Result.ok();
    }

    @GetMapping("/me")
    public Result<User> me(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return Result.ok(authService.getMe(authorization.substring(7)));
    }
}
