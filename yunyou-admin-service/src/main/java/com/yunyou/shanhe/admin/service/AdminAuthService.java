package com.yunyou.shanhe.admin.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yunyou.shanhe.admin.dto.AdminLoginRequest;
import com.yunyou.shanhe.admin.entity.AdminUser;
import com.yunyou.shanhe.admin.mapper.AdminUserMapper;
import com.yunyou.shanhe.common.core.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 运营账号认证。RBAC 权限与 Token 签发为后续迭代项。
 */
@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AdminUserMapper adminUserMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AdminUser login(AdminLoginRequest request) {
        AdminUser user = adminUserMapper.selectOne(Wrappers.<AdminUser>lambdaQuery()
                .eq(AdminUser::getUsername, request.username()));
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BizException("账号或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BizException("账号已被禁用");
        }
        // TODO 签发管理员 Token + RBAC 权限判定
        return user;
    }
}
