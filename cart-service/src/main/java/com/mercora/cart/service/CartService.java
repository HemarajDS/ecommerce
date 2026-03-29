package com.mercora.cart.service;

import com.mercora.cart.dto.ApplyCouponRequest;
import com.mercora.cart.dto.CartItemRequest;
import com.mercora.cart.dto.CartResponse;
import com.mercora.cart.dto.CheckoutRequest;
import com.mercora.cart.dto.CheckoutResponse;
import com.mercora.cart.dto.UpdateCartItemRequest;

public interface CartService {

    CartResponse getCart(String userId);

    CartResponse addItem(String userId, CartItemRequest request);

    CartResponse updateItem(String userId, UpdateCartItemRequest request);

    CartResponse removeItem(String userId, String sku);

    CartResponse applyCoupon(String userId, ApplyCouponRequest request);

    CheckoutResponse checkout(String userId, CheckoutRequest request);
}
