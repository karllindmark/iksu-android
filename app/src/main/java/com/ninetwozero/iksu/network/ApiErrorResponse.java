package com.ninetwozero.iksu.network;

import com.squareup.moshi.Json;

public class ApiErrorResponse {
    @Json(name = "Message") private String code;
    @Json(name = "ShortDescription") private String key;
    @Json(name = "Description") private String message;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
