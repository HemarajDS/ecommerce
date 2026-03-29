package com.mercora.cart.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mercora.cart")
public class CartProperties {

    private long cartTtlMinutes;
    private String couponPrefix;
    private String paymentSessionPrefix;
    private long checkoutExpiryMinutes;

    public long getCartTtlMinutes() {
        return cartTtlMinutes;
    }

    public void setCartTtlMinutes(long cartTtlMinutes) {
        this.cartTtlMinutes = cartTtlMinutes;
    }

    public String getCouponPrefix() {
        return couponPrefix;
    }

    public void setCouponPrefix(String couponPrefix) {
        this.couponPrefix = couponPrefix;
    }

    public String getPaymentSessionPrefix() {
        return paymentSessionPrefix;
    }

    public void setPaymentSessionPrefix(String paymentSessionPrefix) {
        this.paymentSessionPrefix = paymentSessionPrefix;
    }

    public long getCheckoutExpiryMinutes() {
        return checkoutExpiryMinutes;
    }

    public void setCheckoutExpiryMinutes(long checkoutExpiryMinutes) {
        this.checkoutExpiryMinutes = checkoutExpiryMinutes;
    }
}
