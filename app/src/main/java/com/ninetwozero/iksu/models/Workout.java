package com.ninetwozero.iksu.models;

import com.ninetwozero.iksu.features.schedule.reservation.ReservationListItem;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

// TODO: How the hell did this interface implements get here???
public class Workout extends RealmObject implements ReservationListItem {
    @PrimaryKey
    private String pkId;
    private String id;
    private String key;
    private String title;
    private String type;
    private String notice;

    private String instructorKey;
    private String instructor;
    private String coInstructorKey;
    private String coInstructor;

    private long startDate;
    private String startDateString;
    private long endDate;
    private String endDateString;
    private String timeInterval;

    private int bookedSlotCount;
    private int totalSlotCount;

    private String facilityId;
    private String facility;
    private String roomId;
    private String room;

    private boolean dropin;
    private boolean openForReservations;
    private int cancellationStatus;

    // Non JSON fields
    private String connectedAccount;
    private long reservationDeadline;
    private long reservationId;
    private boolean ratedByUser;
    private boolean monitoring;
    private boolean checkedIn;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId;
    }

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

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public String getStartDateString() {
        return startDateString;
    }

    public void setStartDateString(String startTimeString) {
        this.startDateString = startTimeString;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public String getEndDateString() {
        return endDateString;
    }

    public void setEndDateString(String endDateString) {
        this.endDateString = endDateString;
    }

    public String getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(String timeInterval) {
        this.timeInterval = timeInterval;
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

    public String getConnectedAccount() {
        return connectedAccount;
    }

    public void setConnectedAccount(String connectedAccount) {
        this.connectedAccount = connectedAccount;
    }

    public long getReservationDeadline() {
        return reservationDeadline;
    }

    public void setReservationDeadline(long reservationDeadline) {
        this.reservationDeadline = reservationDeadline;
    }

    public long getReservationId() {
        return reservationId;
    }

    public void setReservationId(long reservationId) {
        this.reservationId = reservationId;
    }

    public boolean hasReservation() {
        return reservationId != 0;
    }

    public boolean isRatedByUser() {
        return ratedByUser;
    }

    public void setRatedByUser(boolean ratedByUser) {
        this.ratedByUser = ratedByUser;
    }

    public boolean isMonitoring() {
        return monitoring;
    }

    public void setMonitoring(boolean monitoring) {
        this.monitoring = monitoring;
    }

    public boolean hasCheckedIn() {
        return checkedIn;
    }

    public void setCheckedIn(boolean checkedIn) {
        this.checkedIn = checkedIn;
    }

    public String getInstructorNames() {
        if (!instructor.equals("") && !coInstructor.equals("")) {
            return instructor + " & " + coInstructor;
        } else if (!instructor.equals("")) {
            return instructor;
        } else if (!coInstructor.equals("")) {
            return coInstructor;
        } else {
            return "-";
        }
    }

    public boolean isFullyBooked() {
        return bookedSlotCount == totalSlotCount;
    }

    @Override
    public String toString() {
        return "Workout{" +
                "pkId='" + pkId + '\'' +
                "id='" + id + '\'' +
                ", key='" + key + '\'' +
                ", title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", notice='" + notice + '\'' +
                ", instructorKey='" + instructorKey + '\'' +
                ", instructor='" + instructor + '\'' +
                ", coInstructorKey='" + coInstructorKey + '\'' +
                ", coInstructor='" + coInstructor + '\'' +
                ", startDate=" + startDate +
                ", startDateString='" + startDateString + '\'' +
                ", endDate=" + endDate +
                ", endDateString='" + endDateString + '\'' +
                ", timeInterval='" + timeInterval + '\'' +
                ", bookedSlotCount=" + bookedSlotCount +
                ", totalSlotCount=" + totalSlotCount +
                ", facilityId='" + facilityId + '\'' +
                ", facility='" + facility + '\'' +
                ", roomId='" + roomId + '\'' +
                ", room='" + room + '\'' +
                ", dropin=" + dropin +
                ", openForReservations=" + openForReservations +
                ", cancellationStatus=" + cancellationStatus +
                ", connectedAccount='" + connectedAccount + '\'' +
                ", reservationDeadline=" + reservationDeadline +
                ", reservationId=" + reservationId +
                ", ratedByUser=" + ratedByUser +
                '}';
    }

    @Override
    public int getItemType() {
        return RESERVATION;
    }

}
