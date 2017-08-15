package com.ninetwozero.iksu.network;

import com.squareup.moshi.Json;

class ApiErrorResponse {
    @Json(name = "Message") private String message;

    public String getMessage() {
        return message;
    }
}
