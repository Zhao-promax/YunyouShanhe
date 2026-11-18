package com.yunyou.shanhe.user.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yunyou.shanhe.common.core.BizException;
import com.yunyou.shanhe.user.dto.LoginByPasswordRequest;
import com.yunyou.shanhe.user.dto.LoginBySmsRequest;
import com.yunyou.shanhe.user.dto.RegisterRequest;
import com.yunyou.shanhe.user.dto.SendSmsRequest;
import com.yunyou.shanhe.user.dto.TokenResponse;
import com.yunyou.shanhe.user.entity.User;
import com.yunyou.shanhe.user.mapper.UserMapper;
import com.yunyou.shanhe.user.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证服务：注册、密码/短信登录、登出、当前用户。
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final SmsCodeService smsCodeService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public void sendSmsCode(SendSmsRequest request) {
        smsCodeService.send(request.phone());
    }

    public void register(RegisterRequest request) {
        Long count = userMapper.selectCount(
                Wrappers.<User>lambdaQuery().eq(User::getPhone, request.phone()));
        if (count != null && count > 0) {
            throw new BizException("该手机号已注册");
        }
        smsCodeService.verify(request.phone(), request.smsCode());

        long now = System.currentTimeMillis();
        User user = new User();
        user.setPhone(request.phone());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setStatus(1);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userMapper.insert(user);
    }

    public TokenResponse loginByPassword(LoginByPasswordRequest request) {
        User user = userMapper.selectOne(
                Wrappers.<User>lambdaQuery().eq(User::getPhone, request.phone()));
        if (user == null || user.getPasswordHash() == null
                || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BizException("手机号或密码错误");
        }
        checkStatus(user);
        return issueToken(user);
    }

    public TokenResponse loginBySms(LoginBySmsRequest request) {
        smsCodeService.verify(request.phone(), request.smsCode());
        User user = userMapper.selectOne(
                Wrappers.<User>lambdaQuery().eq(User::getPhone, request.phone()));
        if (user == null) {
            long now = System.currentTimeMillis();
            user = new User();
            user.setPhone(request.phone());
            user.setStatus(1);
            user.setCreatedAt(now);
            user.setUpdatedAt(now);
            userMapper.insert(user);
        }
        checkStatus(user);
        return issueToken(user);
    }

    public User getMe(String token) {
        Long userId = jwtUtil.getUserId(token);
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        return user;
    }

    public void logout(String token) {
        // TODO 将 token 加入 Redis 黑名单直至过期，实现主动注销下线
    }

    public TokenResponse refresh(String refreshToken) {
        Long userId = jwtUtil.getUserId(refreshToken);
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        checkStatus(user);
        return issueToken(user);
    }

    public TokenResponse wechatLogin(String code) {
        // TODO 接入微信开放平台：code 换 openid/unionid → 绑定或创建用户 → 签发 token
        throw new BizException("微信登录暂未接入");
    }

    private void checkStatus(User user) {
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BizException("账号已被禁用");
        }
    }

    private TokenResponse issueToken(User user) {
        String accessToken = jwtUtil.createAccessToken(user.getId(), user.getPhone());
        String refreshToken = jwtUtil.createRefreshToken(user.getId(), user.getPhone());
        return new TokenResponse(accessToken, refreshToken, jwtUtil.getAccessExpireSeconds());
    }
}
