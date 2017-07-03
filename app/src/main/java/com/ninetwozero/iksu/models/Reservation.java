package com.ninetwozero.iksu.models;

import com.squareup.moshi.Json;

class Reservation {
    @Json(name = "ClassId") private String id;
    @Json(name = "ObjectCode") private String key;
    @Json(name = "StartTime") private String startTime;
    @Json(name = "EndTime") private String endTime;
    @Json(name = "Location") private String locationId;
    @Json(name = "SpaceCode") private String roomId;
    @Json(name = "SpaceName") private String room;
    @Json(name = "InstructorCode") private String instructorId;
    @Json(name = "InstructorName") private String instructor;
    @Json(name = "ReservationId") private String reservationId;
    @Json(name = "IsCancellable") private boolean cancelable;
    @Json(name = "QueuePosition") private int queuePosition;
    @Json(name = "Description") private String description;
    @Json(name = "IsCheckedIn") private boolean checkedIn;
    @Json(name = "TypeOfClass") private String type;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(String instructorId) {
        this.instructorId = instructorId;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public boolean isCancelable() {
        return cancelable;
    }

    public void setCancelable(boolean cancelable) {
        this.cancelable = cancelable;
    }

    public int getQueuePosition() {
        return queuePosition;
    }

    public void setQueuePosition(int queuePosition) {
        this.queuePosition = queuePosition;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCheckedIn() {
        return checkedIn;
    }

    public void setCheckedIn(boolean checkedIn) {
        this.checkedIn = checkedIn;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
