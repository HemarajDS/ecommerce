package com.mercora.dealer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mercora.dealer")
public class DealerProperties {

    private String dealerCreatedTopic;
    private String poPendingApprovalTopic;
    private int paymentTermDays;
    private long creditLockSeconds;

    public String getDealerCreatedTopic() {
        return dealerCreatedTopic;
    }

    public void setDealerCreatedTopic(String dealerCreatedTopic) {
        this.dealerCreatedTopic = dealerCreatedTopic;
    }

    public String getPoPendingApprovalTopic() {
        return poPendingApprovalTopic;
    }

    public void setPoPendingApprovalTopic(String poPendingApprovalTopic) {
        this.poPendingApprovalTopic = poPendingApprovalTopic;
    }

    public int getPaymentTermDays() {
        return paymentTermDays;
    }

    public void setPaymentTermDays(int paymentTermDays) {
        this.paymentTermDays = paymentTermDays;
    }

    public long getCreditLockSeconds() {
        return creditLockSeconds;
    }

    public void setCreditLockSeconds(long creditLockSeconds) {
        this.creditLockSeconds = creditLockSeconds;
    }
}
