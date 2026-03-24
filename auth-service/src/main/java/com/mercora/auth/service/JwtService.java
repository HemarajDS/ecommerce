package com.mercora.auth.service;

import com.mercora.auth.config.AuthProperties;
import com.mercora.auth.model.UserAccount;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final AuthProperties authProperties;
    private final SecretKey signingKey;

    public JwtService(AuthProperties authProperties) {
        this.authProperties = authProperties;
        byte[] keyBytes = authProperties.getJwtSecret().length() >= 32
                ? authProperties.getJwtSecret().getBytes(StandardCharsets.UTF_8)
                : Decoders.BASE64.decode(authProperties.getJwtSecret());
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(UserAccount user) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(authProperties.getAccessTokenMinutes(), ChronoUnit.MINUTES);
        return Jwts.builder()
                .subject(user.getId())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
    }

    public Instant getAccessTokenExpiry() {
        return Instant.now().plus(authProperties.getAccessTokenMinutes(), ChronoUnit.MINUTES);
    }

    public Instant getRefreshTokenExpiry() {
        return Instant.now().plus(authProperties.getRefreshTokenDays(), ChronoUnit.DAYS);
    }
}
