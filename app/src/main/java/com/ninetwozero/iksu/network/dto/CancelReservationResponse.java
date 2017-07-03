package com.ninetwozero.iksu.network.dto;

import com.squareup.moshi.Json;

public class CancelReservationResponse {
    @Json(name = "ReservationId") private long reservationId;
    @Json(name = "ErrorStatus") private int errorCode;
    @Json(name = "ErrorMessage") private String errorMessage;

    public long getReservationId() {
        return reservationId;
    }

    public void setReservationId(long reservationId) {
        this.reservationId = reservationId;
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
