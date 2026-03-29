package com.mercora.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mercora.payment")
public class PaymentProperties {

    private String provider;
    private String paymentSuccessTopic;
    private String paymentFailedTopic;
    private String paymentRefundedTopic;
    private long idempotencyTtlHours;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getPaymentSuccessTopic() {
        return paymentSuccessTopic;
    }

    public void setPaymentSuccessTopic(String paymentSuccessTopic) {
        this.paymentSuccessTopic = paymentSuccessTopic;
    }

    public String getPaymentFailedTopic() {
        return paymentFailedTopic;
    }

    public void setPaymentFailedTopic(String paymentFailedTopic) {
        this.paymentFailedTopic = paymentFailedTopic;
    }

    public String getPaymentRefundedTopic() {
        return paymentRefundedTopic;
    }

    public void setPaymentRefundedTopic(String paymentRefundedTopic) {
        this.paymentRefundedTopic = paymentRefundedTopic;
    }

    public long getIdempotencyTtlHours() {
        return idempotencyTtlHours;
    }

    public void setIdempotencyTtlHours(long idempotencyTtlHours) {
        this.idempotencyTtlHours = idempotencyTtlHours;
    }
}
