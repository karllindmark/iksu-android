package com.ninetwozero.iksu.network.dto;

import com.squareup.moshi.Json;

public class BookingResponse {
    @Json(name = "ReservationId") private long reservationId;
    @Json(name = "Message") private String message;

    public long getReservationId() {
        return reservationId;
    }

    public void setReservationId(long reservationId) {
        this.reservationId = reservationId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
