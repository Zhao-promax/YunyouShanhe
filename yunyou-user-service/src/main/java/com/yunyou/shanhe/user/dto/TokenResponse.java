package com.yunyou.shanhe.user.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        Long expiresIn
) {
}
