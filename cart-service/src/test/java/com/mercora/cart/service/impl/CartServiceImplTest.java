package com.mercora.cart.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.mercora.cart.config.CartProperties;
import com.mercora.cart.dto.CartItemRequest;
import com.mercora.cart.dto.CouponDefinition;
import com.mercora.cart.dto.ProductPriceResponse;
import com.mercora.cart.exception.ResourceNotFoundException;
import com.mercora.cart.model.CartDocument;
import com.mercora.cart.model.CouponType;
import com.mercora.cart.repository.CartRepository;
import com.mercora.cart.service.ProductCatalogClient;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private ProductCatalogClient productCatalogClient;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private CartServiceImpl cartService;

    private CartProperties cartProperties;

    @BeforeEach
    void setUp() {
        cartProperties = new CartProperties();
        cartProperties.setCartTtlMinutes(30);
        cartProperties.setCouponPrefix("coupon:");
        cartProperties.setPaymentSessionPrefix("payment-session:");
        cartProperties.setCheckoutExpiryMinutes(15);
        cartService = new CartServiceImpl(cartRepository, productCatalogClient, redisTemplate, cartProperties);
    }

    @Test
    void addItemShouldCreateCartAndComputeTotals() {
        when(cartRepository.findByUserId("user-1")).thenReturn(Optional.empty());
        when(productCatalogClient.getPrice("prod-1", "SKU-1"))
                .thenReturn(new ProductPriceResponse("prod-1", "SKU-1", "Trail Shoes", BigDecimal.valueOf(1999)));
        when(cartRepository.save(any(CartDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = cartService.addItem("user-1", new CartItemRequest("prod-1", "SKU-1", 2));

        assertEquals(1, response.items().size());
        assertEquals(BigDecimal.valueOf(3998), response.grandTotal());
    }

    @Test
    void applyCouponShouldFailWhenCouponMissing() {
        CartDocument cart = new CartDocument();
        cart.setUserId("user-1");
        cart.setItems(new ArrayList<>());
        cart.setSubtotal(BigDecimal.ZERO);
        cart.setDiscountTotal(BigDecimal.ZERO);
        cart.setGrandTotal(BigDecimal.ZERO);
        cart.setUpdatedAt(Instant.now());
        cart.setExpiresAt(Instant.now());

        when(cartRepository.findByUserId("user-1")).thenReturn(Optional.of(cart));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("coupon:SAVE10")).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> cartService.applyCoupon("user-1", new com.mercora.cart.dto.ApplyCouponRequest("SAVE10")));
    }
}
