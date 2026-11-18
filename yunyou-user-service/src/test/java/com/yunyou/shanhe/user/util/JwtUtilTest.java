package com.yunyou.shanhe.user.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtUtilTest {

    private static final String SECRET = "yunyou-shanhe-jwt-secret-dev-only-change-in-prod-0123456789";
    private final JwtUtil jwtUtil = new JwtUtil(SECRET, 1800, 604800);

    @Test
    void shouldIssueAndParseAccessToken() {
        String token = jwtUtil.createAccessToken(100L, "13800138000");
        assertEquals(100L, jwtUtil.getUserId(token));
        Claims claims = jwtUtil.parse(token);
        assertEquals("13800138000", claims.get("phone"));
    }

    @Test
    void shouldReturnConfiguredAccessExpireSeconds() {
        assertEquals(1800L, jwtUtil.getAccessExpireSeconds());
    }

    @Test
    void shouldThrowWhenTokenIsInvalid() {
        assertThrows(JwtException.class, () -> jwtUtil.parse("not-a-valid-token"));
    }
}
