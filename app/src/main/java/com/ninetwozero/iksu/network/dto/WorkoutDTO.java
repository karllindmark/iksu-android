package com.ninetwozero.iksu.network.dto;

import com.squareup.moshi.Json;

public class WorkoutDTO {
    @Json(name = "ClassId") private String id;
    @Json(name = "ObjectCode") private String key;
    @Json(name = "Description") private String title;
    @Json(name = "ObjectClass") private String type;
    @Json(name = "SpecialRemark") private String notice;

    @Json(name = "InstructorCode") private String instructorKey;
    @Json(name = "InstructorName") private String instructor;
    @Json(name = "InstructorCode2") private String coInstructorKey;
    @Json(name = "InstructorName2") private String coInstructor;

    // NOTE: "2017-04-25T06:30:00+02:00"
    @Json(name = "StartTime") private String startTime;
    @Json(name = "EndTime") private String endTime;

    @Json(name = "ReservationQuantity") private int bookedSlotCount;
    @Json(name = "MaxReservationQuantity") private int totalSlotCount;

    @Json(name = "Location") private String facilityId;
    @Json(name = "LocationName") private String facility;
    @Json(name = "Space") private String roomId;
    @Json(name = "SpaceName") private String room;

    @Json(name = "IsDropIn") private boolean dropin;
    @Json(name = "AllowReservations") private boolean openForReservations;
    @Json(name = "CancellationStatus") private int cancellationStatus;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public String getInstructorKey() {
        return instructorKey;
    }

    public void setInstructorKey(String instructorKey) {
        this.instructorKey = instructorKey;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public String getCoInstructorKey() {
        return coInstructorKey;
    }

    public void setCoInstructorKey(String coInstructorKey) {
        this.coInstructorKey = coInstructorKey;
    }

    public String getCoInstructor() {
        return coInstructor;
    }

    public void setCoInstructor(String coInstructor) {
        this.coInstructor = coInstructor;
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

    public int getBookedSlotCount() {
        return bookedSlotCount;
    }

    public void setBookedSlotCount(int bookedSlotCount) {
        this.bookedSlotCount = bookedSlotCount;
    }

    public int getTotalSlotCount() {
        return totalSlotCount;
    }

    public void setTotalSlotCount(int totalSlotCount) {
        this.totalSlotCount = totalSlotCount;
    }

    public String getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(String facilityId) {
        this.facilityId = facilityId;
    }

    public String getFacility() {
        return facility;
    }

    public void setFacility(String facility) {
        this.facility = facility;
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

    public boolean isDropin() {
        return dropin;
    }

    public void setDropin(boolean dropin) {
        this.dropin = dropin;
    }

    public boolean isOpenForReservations() {
        return openForReservations;
    }

    public void setOpenForReservations(boolean openForReservations) {
        this.openForReservations = openForReservations;
    }

    public int getCancellationStatus() {
        return cancellationStatus;
    }

    public void setCancellationStatus(int cancellationStatus) {
        this.cancellationStatus = cancellationStatus;
    }
}
