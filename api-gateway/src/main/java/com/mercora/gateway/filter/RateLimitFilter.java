package com.mercora.gateway.filter;

import com.mercora.gateway.config.GatewayProperties;
import java.net.InetSocketAddress;
import java.time.Duration;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

@Component
public class RateLimitFilter implements WebFilter, Ordered {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final GatewayProperties gatewayProperties;

    public RateLimitFilter(ReactiveStringRedisTemplate redisTemplate, GatewayProperties gatewayProperties) {
        this.redisTemplate = redisTemplate;
        this.gatewayProperties = gatewayProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.web.server.WebFilterChain chain) {
        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
        String ipAddress = remoteAddress == null ? "unknown" : remoteAddress.getAddress().getHostAddress();
        String key = "gateway:ratelimit:" + ipAddress;

        return redisTemplate.opsForValue().increment(key)
                .flatMap(count -> {
                    if (count == 1) {
                        return redisTemplate.expire(key, Duration.ofMinutes(1)).thenReturn(count);
                    }
                    return Mono.just(count);
                })
                .flatMap(count -> {
                    if (count > gatewayProperties.getRateLimit().getBurstCapacity()) {
                        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                        return exchange.getResponse().setComplete();
                    }
                    return chain.filter(exchange);
                });
    }

    @Override
    public int getOrder() {
        return -8;
    }
}
