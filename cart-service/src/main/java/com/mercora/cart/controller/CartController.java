package com.mercora.cart.controller;

import com.mercora.cart.dto.ApplyCouponRequest;
import com.mercora.cart.dto.CartItemRequest;
import com.mercora.cart.dto.CartResponse;
import com.mercora.cart.dto.CheckoutRequest;
import com.mercora.cart.dto.CheckoutResponse;
import com.mercora.cart.dto.UpdateCartItemRequest;
import com.mercora.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Cart", description = "Cart and checkout APIs")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/cart")
    @Operation(summary = "Get the current user's cart")
    public CartResponse getCart(@RequestHeader("X-User-Id") String userId) {
        return cartService.getCart(userId);
    }

    @PostMapping("/cart/items")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add an item to the cart")
    public CartResponse addItem(@RequestHeader("X-User-Id") String userId, @Valid @RequestBody CartItemRequest request) {
        return cartService.addItem(userId, request);
    }

    @PatchMapping("/cart/items")
    @Operation(summary = "Update a cart item quantity")
    public CartResponse updateItem(@RequestHeader("X-User-Id") String userId, @Valid @RequestBody UpdateCartItemRequest request) {
        return cartService.updateItem(userId, request);
    }

    @DeleteMapping("/cart/items/{sku}")
    @Operation(summary = "Remove a cart item")
    public CartResponse removeItem(@RequestHeader("X-User-Id") String userId, @PathVariable String sku) {
        return cartService.removeItem(userId, sku);
    }

    @PostMapping("/cart/coupon")
    @Operation(summary = "Apply a coupon to the cart")
    public CartResponse applyCoupon(@RequestHeader("X-User-Id") String userId, @Valid @RequestBody ApplyCouponRequest request) {
        return cartService.applyCoupon(userId, request);
    }

    @PostMapping("/checkout")
    @Operation(summary = "Create a checkout payment session")
    public CheckoutResponse checkout(@RequestHeader("X-User-Id") String userId, @Valid @RequestBody CheckoutRequest request) {
        return cartService.checkout(userId, request);
    }
}
