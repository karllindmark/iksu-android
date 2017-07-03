package com.ninetwozero.iksu.network.dto;

import com.squareup.moshi.Json;

public class TokenResponse {
    @Json(name = "access_token") private String accessToken;
    @Json(name = "token_type") private String tokenType;
    @Json(name = "expires_in") private long expiresIn;
    @Json(name = "userName") private String username;
    @Json(name = ".issued") private String issueDate;
    @Json(name = ".expires") private String expiryDate;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(String issueDate) {
        this.issueDate = issueDate;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }
}
