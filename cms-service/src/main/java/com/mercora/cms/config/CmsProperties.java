package com.mercora.cms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mercora.cms")
public class CmsProperties {

    private String assetBaseUrl;
    private String cloudfrontDistributionId;

    public String getAssetBaseUrl() {
        return assetBaseUrl;
    }

    public void setAssetBaseUrl(String assetBaseUrl) {
        this.assetBaseUrl = assetBaseUrl;
    }

    public String getCloudfrontDistributionId() {
        return cloudfrontDistributionId;
    }

    public void setCloudfrontDistributionId(String cloudfrontDistributionId) {
        this.cloudfrontDistributionId = cloudfrontDistributionId;
    }
}
