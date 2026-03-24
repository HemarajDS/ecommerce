package com.mercora.auth.service;

import com.mercora.auth.config.AuthProperties;
import java.time.Duration;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {

    private final StringRedisTemplate redisTemplate;
    private final AuthProperties authProperties;

    public RefreshTokenService(StringRedisTemplate redisTemplate, AuthProperties authProperties) {
        this.redisTemplate = redisTemplate;
        this.authProperties = authProperties;
    }

    public String create(String userId) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(key(token), userId, Duration.ofDays(authProperties.getRefreshTokenDays()));
        return token;
    }

    public String findUserId(String token) {
        return redisTemplate.opsForValue().get(key(token));
    }

    public void revoke(String token) {
        redisTemplate.delete(key(token));
    }

    private String key(String token) {
        return "auth:refresh:" + token;
    }
}
