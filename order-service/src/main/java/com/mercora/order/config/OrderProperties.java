package com.mercora.order.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mercora.order")
public class OrderProperties {

    private String orderPlacedTopic;
    private String orderCancelledTopic;
    private String orderDeliveredTopic;
    private String paymentSuccessTopic;

    public String getOrderPlacedTopic() {
        return orderPlacedTopic;
    }

    public void setOrderPlacedTopic(String orderPlacedTopic) {
        this.orderPlacedTopic = orderPlacedTopic;
    }

    public String getOrderCancelledTopic() {
        return orderCancelledTopic;
    }

    public void setOrderCancelledTopic(String orderCancelledTopic) {
        this.orderCancelledTopic = orderCancelledTopic;
    }

    public String getOrderDeliveredTopic() {
        return orderDeliveredTopic;
    }

    public void setOrderDeliveredTopic(String orderDeliveredTopic) {
        this.orderDeliveredTopic = orderDeliveredTopic;
    }

    public String getPaymentSuccessTopic() {
        return paymentSuccessTopic;
    }

    public void setPaymentSuccessTopic(String paymentSuccessTopic) {
        this.paymentSuccessTopic = paymentSuccessTopic;
    }
}
