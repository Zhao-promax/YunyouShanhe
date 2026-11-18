package com.yunyou.shanhe.user.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 签发与解析。
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long accessExpireSeconds;
    private final long refreshExpireSeconds;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.access-expire-seconds:1800}") long accessExpireSeconds,
                   @Value("${jwt.refresh-expire-seconds:604800}") long refreshExpireSeconds) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpireSeconds = accessExpireSeconds;
        this.refreshExpireSeconds = refreshExpireSeconds;
    }

    public String createAccessToken(Long userId, String phone) {
        return createToken(userId, phone, accessExpireSeconds);
    }

    public String createRefreshToken(Long userId, String phone) {
        return createToken(userId, phone, refreshExpireSeconds);
    }

    public long getAccessExpireSeconds() {
        return accessExpireSeconds;
    }

    private String createToken(Long userId, String phone, long expireSeconds) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("phone", phone)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireSeconds * 1000))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public Long getUserId(String token) {
        return Long.valueOf(parse(token).getSubject());
    }
}
