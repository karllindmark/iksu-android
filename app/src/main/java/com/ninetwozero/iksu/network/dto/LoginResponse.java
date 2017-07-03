package com.ninetwozero.iksu.network.dto;


import com.squareup.moshi.Json;

public class LoginResponse {
    @Json(name = "Name") private String name;
    @Json(name = "SessionId") private String sessionId;
    @Json(name = "Contract") private String contract;
    @Json(name = "ErrorCode") private int errorCode;
    @Json(name = "ErrorMessage") private String errorMessage;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getContract() {
        return contract;
    }

    public void setContract(String contract) {
        this.contract = contract;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
