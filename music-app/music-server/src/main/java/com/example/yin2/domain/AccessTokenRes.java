package com.example.yin2.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AccessTokenRes {
    @JsonProperty("scope")
    private String scope;
    @JsonProperty("token_type")
    private String tokenType;
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("expires_in")
    private Integer expiresIn;
    @JsonProperty("refresh_token")
    private String refreshToken;

    public AccessTokenRes() {
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Integer getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Integer expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Override
    public String toString() {
        return "AccessTokenRes{" +
                "scope='" + scope + '\'' +
                ", tokenType='" + tokenType + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", expiresIn='" + expiresIn + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                '}';
    }
}
