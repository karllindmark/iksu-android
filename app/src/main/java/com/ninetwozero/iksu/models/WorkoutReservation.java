package com.ninetwozero.iksu.models;

import com.squareup.moshi.Json;

public class WorkoutReservation {
    @Json(name = "ReservationId") private long id;
    @Json(name = "ClassId") private String workoutId;
    @Json(name = "IsCancellable") private boolean cancelable;
    @Json(name = "IsCheckedIn") private boolean checkedIn;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getWorkoutId() {
        return workoutId;
    }

    public void setWorkoutId(String workoutId) {
        this.workoutId = workoutId;
    }

    public boolean isCancelable() {
        return cancelable;
    }

    public void setCancelable(boolean cancelable) {
        this.cancelable = cancelable;
    }

    public boolean isCheckedIn() {
        return checkedIn;
    }

    public void setCheckedIn(boolean checkedIn) {
        this.checkedIn = checkedIn;
    }
}
