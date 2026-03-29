package com.mercora.cart.service.impl;

import com.mercora.cart.config.CartProperties;
import com.mercora.cart.dto.ApplyCouponRequest;
import com.mercora.cart.dto.CartItemRequest;
import com.mercora.cart.dto.CartItemResponse;
import com.mercora.cart.dto.CartResponse;
import com.mercora.cart.dto.CheckoutRequest;
import com.mercora.cart.dto.CheckoutResponse;
import com.mercora.cart.dto.CouponDefinition;
import com.mercora.cart.dto.CouponResponse;
import com.mercora.cart.dto.ProductPriceResponse;
import com.mercora.cart.dto.UpdateCartItemRequest;
import com.mercora.cart.exception.BusinessRuleException;
import com.mercora.cart.exception.ResourceNotFoundException;
import com.mercora.cart.model.CartDocument;
import com.mercora.cart.model.CartItem;
import com.mercora.cart.model.CouponSnapshot;
import com.mercora.cart.model.CouponType;
import com.mercora.cart.repository.CartRepository;
import com.mercora.cart.service.CartService;
import com.mercora.cart.service.ProductCatalogClient;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductCatalogClient productCatalogClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CartProperties cartProperties;

    public CartServiceImpl(
            CartRepository cartRepository,
            ProductCatalogClient productCatalogClient,
            RedisTemplate<String, Object> redisTemplate,
            CartProperties cartProperties) {
        this.cartRepository = cartRepository;
        this.productCatalogClient = productCatalogClient;
        this.redisTemplate = redisTemplate;
        this.cartProperties = cartProperties;
    }

    @Override
    public CartResponse getCart(String userId) {
        return toResponse(getOrCreateCart(userId));
    }

    @Override
    public CartResponse addItem(String userId, CartItemRequest request) {
        CartDocument cart = getOrCreateCart(userId);
        ProductPriceResponse product = fetchPrice(request.productId(), request.sku());

        Optional<CartItem> existing = cart.getItems().stream()
                .filter(item -> item.getSku().equalsIgnoreCase(request.sku()))
                .findFirst();

        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + request.quantity());
            item.setUnitPrice(product.retailPrice());
            item.setName(product.name());
            item.setLineTotal(product.retailPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        } else {
            CartItem item = new CartItem();
            item.setProductId(request.productId());
            item.setSku(request.sku());
            item.setName(product.name());
            item.setQuantity(request.quantity());
            item.setUnitPrice(product.retailPrice());
            item.setLineTotal(product.retailPrice().multiply(BigDecimal.valueOf(request.quantity())));
            cart.getItems().add(item);
        }

        recalculate(cart);
        return toResponse(cartRepository.save(cart));
    }

    @Override
    public CartResponse updateItem(String userId, UpdateCartItemRequest request) {
        CartDocument cart = getExistingCart(userId);
        CartItem item = cart.getItems().stream()
                .filter(entry -> entry.getSku().equalsIgnoreCase(request.sku()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        ProductPriceResponse product = fetchPrice(item.getProductId(), item.getSku());
        item.setQuantity(request.quantity());
        item.setUnitPrice(product.retailPrice());
        item.setName(product.name());
        item.setLineTotal(product.retailPrice().multiply(BigDecimal.valueOf(request.quantity())));

        recalculate(cart);
        return toResponse(cartRepository.save(cart));
    }

    @Override
    public CartResponse removeItem(String userId, String sku) {
        CartDocument cart = getExistingCart(userId);
        boolean removed = cart.getItems().removeIf(item -> item.getSku().equalsIgnoreCase(sku));
        if (!removed) {
            throw new ResourceNotFoundException("Cart item not found");
        }
        recalculate(cart);
        if (cart.getItems().isEmpty()) {
            cartRepository.delete(userId);
            return toResponse(cart);
        }
        return toResponse(cartRepository.save(cart));
    }

    @Override
    public CartResponse applyCoupon(String userId, ApplyCouponRequest request) {
        CartDocument cart = getExistingCart(userId);
        CouponDefinition definition = readCoupon(request.code());
        if (!definition.active()) {
            throw new BusinessRuleException("Coupon is inactive");
        }

        CouponSnapshot coupon = new CouponSnapshot();
        coupon.setCode(definition.code());
        coupon.setType(definition.type());
        coupon.setValue(definition.value());
        cart.setCoupon(coupon);
        recalculate(cart);
        return toResponse(cartRepository.save(cart));
    }

    @Override
    public CheckoutResponse checkout(String userId, CheckoutRequest request) {
        CartDocument cart = getExistingCart(userId);
        if (cart.getItems().isEmpty()) {
            throw new BusinessRuleException("Cart is empty");
        }

        // Always re-fetch live pricing before checkout.
        for (CartItem item : cart.getItems()) {
            ProductPriceResponse product = fetchPrice(item.getProductId(), item.getSku());
            item.setUnitPrice(product.retailPrice());
            item.setName(product.name());
            item.setLineTotal(product.retailPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        recalculate(cart);
        cartRepository.save(cart);

        String sessionId = UUID.randomUUID().toString();
        CheckoutResponse response = new CheckoutResponse(
                sessionId,
                request.paymentMethod(),
                request.currency(),
                cart.getGrandTotal(),
                Instant.now().plusSeconds(cartProperties.getCheckoutExpiryMinutes() * 60));
        redisTemplate.opsForValue().set(
                cartProperties.getPaymentSessionPrefix() + sessionId,
                response,
                Duration.ofMinutes(cartProperties.getCheckoutExpiryMinutes()));
        return response;
    }

    private CartDocument getOrCreateCart(String userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            CartDocument cart = new CartDocument();
            cart.setUserId(userId);
            cart.setItems(new ArrayList<>());
            cart.setSubtotal(BigDecimal.ZERO);
            cart.setDiscountTotal(BigDecimal.ZERO);
            cart.setGrandTotal(BigDecimal.ZERO);
            cart.setUpdatedAt(Instant.now());
            cart.setExpiresAt(Instant.now().plusSeconds(cartProperties.getCartTtlMinutes() * 60));
            return cart;
        });
    }

    private CartDocument getExistingCart(String userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
    }

    private ProductPriceResponse fetchPrice(String productId, String sku) {
        try {
            return productCatalogClient.getPrice(productId, sku);
        } catch (Exception ex) {
            throw new BusinessRuleException("Unable to retrieve live product pricing");
        }
    }

    private CouponDefinition readCoupon(String code) {
        Object value = redisTemplate.opsForValue().get(cartProperties.getCouponPrefix() + code.toUpperCase());
        if (value instanceof CouponDefinition definition) {
            return definition;
        }
        throw new ResourceNotFoundException("Coupon not found");
    }

    private void recalculate(CartDocument cart) {
        BigDecimal subtotal = cart.getItems().stream()
                .map(CartItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = BigDecimal.ZERO;
        CouponSnapshot coupon = cart.getCoupon();
        if (coupon != null) {
            if (coupon.getType() == CouponType.PERCENTAGE) {
                discount = subtotal.multiply(coupon.getValue())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            } else {
                discount = coupon.getValue().min(subtotal);
            }
            coupon.setDiscountAmount(discount);
        }

        cart.setSubtotal(subtotal);
        cart.setDiscountTotal(discount);
        cart.setGrandTotal(subtotal.subtract(discount).max(BigDecimal.ZERO));
        cart.setUpdatedAt(Instant.now());
        cart.setExpiresAt(Instant.now().plusSeconds(cartProperties.getCartTtlMinutes() * 60));
    }

    private CartResponse toResponse(CartDocument cart) {
        return new CartResponse(
                cart.getUserId(),
                cart.getItems() == null ? List.of() : cart.getItems().stream()
                        .map(item -> new CartItemResponse(
                                item.getProductId(),
                                item.getSku(),
                                item.getName(),
                                item.getQuantity(),
                                item.getUnitPrice(),
                                item.getLineTotal()))
                        .toList(),
                cart.getCoupon() == null ? null : new CouponResponse(
                        cart.getCoupon().getCode(),
                        cart.getCoupon().getType(),
                        cart.getCoupon().getValue(),
                        cart.getCoupon().getDiscountAmount()),
                cart.getSubtotal(),
                cart.getDiscountTotal(),
                cart.getGrandTotal(),
                cart.getUpdatedAt(),
                cart.getExpiresAt());
    }
}
