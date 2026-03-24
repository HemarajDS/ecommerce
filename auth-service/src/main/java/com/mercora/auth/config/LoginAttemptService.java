package com.mercora.auth.config;

import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LoginAttemptService {

    private final StringRedisTemplate redisTemplate;
    private final AuthProperties authProperties;

    public LoginAttemptService(StringRedisTemplate redisTemplate, AuthProperties authProperties) {
        this.redisTemplate = redisTemplate;
        this.authProperties = authProperties;
    }

    public boolean isLocked(String email) {
        String value = redisTemplate.opsForValue().get(key(email));
        long attempts = value == null ? 0 : Long.parseLong(value);
        return attempts >= authProperties.getMaxFailedLogins();
    }

    public long incrementFailedAttempts(String email) {
        Long attempts = redisTemplate.opsForValue().increment(key(email));
        redisTemplate.expire(key(email), Duration.ofHours(1));
        return attempts == null ? 0 : attempts;
    }

    public void reset(String email) {
        redisTemplate.delete(key(email));
    }

    private String key(String email) {
        return "auth:failed-login:" + email.toLowerCase();
    }
}
