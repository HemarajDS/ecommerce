package com.mercora.product.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mercora.product")
public class ProductProperties {

    private String assetBaseUrl;
    private String productCreatedTopic;
    private String productUpdatedTopic;

    public String getAssetBaseUrl() {
        return assetBaseUrl;
    }

    public void setAssetBaseUrl(String assetBaseUrl) {
        this.assetBaseUrl = assetBaseUrl;
    }

    public String getProductCreatedTopic() {
        return productCreatedTopic;
    }

    public void setProductCreatedTopic(String productCreatedTopic) {
        this.productCreatedTopic = productCreatedTopic;
    }

    public String getProductUpdatedTopic() {
        return productUpdatedTopic;
    }

    public void setProductUpdatedTopic(String productUpdatedTopic) {
        this.productUpdatedTopic = productUpdatedTopic;
    }
}
