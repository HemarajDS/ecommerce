package com.mercora.gateway.filter;

import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

@Component
public class CorrelationIdFilter implements WebFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);
    private static final String HEADER = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.web.server.WebFilterChain chain) {
        String correlationId = Optional.ofNullable(exchange.getRequest().getHeaders().getFirst(HEADER))
                .filter(value -> !value.isBlank())
                .orElse(UUID.randomUUID().toString());

        ServerHttpRequest request = exchange.getRequest()
                .mutate()
                .header(HEADER, correlationId)
                .build();

        log.info("gateway_request method={} path={} correlationId={}",
                request.getMethod(), request.getPath(), correlationId);

        return chain.filter(exchange.mutate().request(request).build())
                .doOnSuccess(unused -> exchange.getResponse().getHeaders().set(HEADER, correlationId));
    }

    @Override
    public int getOrder() {
        return -10;
    }
}
