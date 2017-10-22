package com.ninetwozero.iksu.network.dto;

import com.squareup.moshi.Json;

public class GeneralApiErrorResponse {
    @Json(name = "Message") private String errorCode;
    @Json(name = "ShortDescription") private String errorKey;
    @Json(name = "Description") private String errorString;

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorKey() {
        return errorKey;
    }

    public void setErrorKey(String errorKey) {
        this.errorKey = errorKey;
    }

    public String getErrorString() {
        return errorString;
    }

    public void setErrorString(String errorString) {
        this.errorString = errorString;
    }
}
