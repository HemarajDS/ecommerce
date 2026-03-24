package com.mercora.gateway.filter;

import com.mercora.gateway.config.GatewayProperties;
import com.mercora.gateway.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements WebFilter, Ordered {

    private final GatewayProperties gatewayProperties;
    private final SecretKey signingKey;
    private final AntPathMatcher matcher = new AntPathMatcher();

    public JwtAuthenticationFilter(GatewayProperties gatewayProperties, JwtProperties jwtProperties) {
        this.gatewayProperties = gatewayProperties;
        byte[] keyBytes = jwtProperties.getSecret().length() >= 32
                ? jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
                : Decoders.BASE64.decode(jwtProperties.getSecret());
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.web.server.WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authorizationHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(authorizationHeader.substring(7))
                    .getPayload();

            if (claims.getExpiration() != null && claims.getExpiration().toInstant().isBefore(Instant.now())) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            ServerHttpRequest request = exchange.getRequest().mutate()
                    .header("X-User-Id", claims.getSubject())
                    .header("X-User-Role", String.valueOf(claims.get("role")))
                    .build();
            return chain.filter(exchange.mutate().request(request).build());
        } catch (Exception ex) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private boolean isPublicPath(String path) {
        List<String> publicPaths = gatewayProperties.getPublicPaths();
        return publicPaths.stream().anyMatch(pattern -> matcher.match(pattern, path));
    }

    @Override
    public int getOrder() {
        return -5;
    }
}
