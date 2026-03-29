package com.mercora.cart.repository;

import com.mercora.cart.config.CartProperties;
import com.mercora.cart.model.CartDocument;
import java.time.Duration;
import java.util.Optional;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CartRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private final CartProperties cartProperties;

    public CartRepository(RedisTemplate<String, Object> redisTemplate, CartProperties cartProperties) {
        this.redisTemplate = redisTemplate;
        this.cartProperties = cartProperties;
    }

    public Optional<CartDocument> findByUserId(String userId) {
        Object value = redisTemplate.opsForValue().get(key(userId));
        return value instanceof CartDocument cart ? Optional.of(cart) : Optional.empty();
    }

    public CartDocument save(CartDocument cartDocument) {
        redisTemplate.opsForValue().set(key(cartDocument.getUserId()), cartDocument, Duration.ofMinutes(cartProperties.getCartTtlMinutes()));
        return cartDocument;
    }

    public void delete(String userId) {
        redisTemplate.delete(key(userId));
    }

    private String key(String userId) {
        return "cart:" + userId;
    }
}
