package com.mercora.inventory.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mercora.inventory")
public class InventoryProperties {

    private String lowStockTopic;
    private String orderPlacedTopic;
    private long reservationTtlMinutes;
    private long lockTtlSeconds;
    private int defaultSafetyStock;

    public String getLowStockTopic() {
        return lowStockTopic;
    }

    public void setLowStockTopic(String lowStockTopic) {
        this.lowStockTopic = lowStockTopic;
    }

    public String getOrderPlacedTopic() {
        return orderPlacedTopic;
    }

    public void setOrderPlacedTopic(String orderPlacedTopic) {
        this.orderPlacedTopic = orderPlacedTopic;
    }

    public long getReservationTtlMinutes() {
        return reservationTtlMinutes;
    }

    public void setReservationTtlMinutes(long reservationTtlMinutes) {
        this.reservationTtlMinutes = reservationTtlMinutes;
    }

    public long getLockTtlSeconds() {
        return lockTtlSeconds;
    }

    public void setLockTtlSeconds(long lockTtlSeconds) {
        this.lockTtlSeconds = lockTtlSeconds;
    }

    public int getDefaultSafetyStock() {
        return defaultSafetyStock;
    }

    public void setDefaultSafetyStock(int defaultSafetyStock) {
        this.defaultSafetyStock = defaultSafetyStock;
    }
}
