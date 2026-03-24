package com.mercora.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mercora.auth")
public class AuthProperties {

    private long accessTokenMinutes;
    private long refreshTokenDays;
    private String jwtSecret;
    private long otpTtlMinutes;
    private int maxFailedLogins;

    public long getAccessTokenMinutes() {
        return accessTokenMinutes;
    }

    public void setAccessTokenMinutes(long accessTokenMinutes) {
        this.accessTokenMinutes = accessTokenMinutes;
    }

    public long getRefreshTokenDays() {
        return refreshTokenDays;
    }

    public void setRefreshTokenDays(long refreshTokenDays) {
        this.refreshTokenDays = refreshTokenDays;
    }

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public long getOtpTtlMinutes() {
        return otpTtlMinutes;
    }

    public void setOtpTtlMinutes(long otpTtlMinutes) {
        this.otpTtlMinutes = otpTtlMinutes;
    }

    public int getMaxFailedLogins() {
        return maxFailedLogins;
    }

    public void setMaxFailedLogins(int maxFailedLogins) {
        this.maxFailedLogins = maxFailedLogins;
    }
}
