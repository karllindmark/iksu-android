package com.ninetwozero.iksu.network;

import com.ninetwozero.iksu.models.Workout;
import com.ninetwozero.iksu.network.dto.WorkoutDTO;
import com.ninetwozero.iksu.utils.DateUtils;
import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

import java.util.concurrent.TimeUnit;

public class WorkoutMoshiAdapter {
    @FromJson
    Workout toJson(WorkoutDTO serverVersion) {
        final Workout appVersion = new Workout();
        appVersion.setId(serverVersion.getId().trim());
        appVersion.setKey(serverVersion.getKey().trim());
        appVersion.setTitle(serverVersion.getTitle().trim());
        appVersion.setType(serverVersion.getType().trim());
        appVersion.setNotice(serverVersion.getNotice().trim());

        appVersion.setInstructorKey(serverVersion.getInstructorKey().trim());
        appVersion.setInstructor(serverVersion.getInstructor().trim());
        appVersion.setCoInstructorKey(serverVersion.getCoInstructorKey().trim());
        appVersion.setCoInstructor(serverVersion.getCoInstructor().trim());

        appVersion.setStartDateString(serverVersion.getStartTime().trim());
        appVersion.setStartDate(DateUtils.convertDateStringToLong(appVersion.getStartDateString()));
        appVersion.setEndDateString(serverVersion.getEndTime().trim());
        appVersion.setEndDate(DateUtils.convertDateStringToLong(appVersion.getEndDateString()));
        appVersion.setTimeInterval(appVersion.getStartDateString().substring(11, 16) + " - " + appVersion.getEndDateString().substring(11, 16));

        appVersion.setBookedSlotCount(serverVersion.getBookedSlotCount());
        appVersion.setTotalSlotCount(serverVersion.getTotalSlotCount());

        appVersion.setFacilityId(serverVersion.getFacilityId().trim());
        appVersion.setFacility(serverVersion.getFacility().trim());
        appVersion.setRoomId(serverVersion.getRoomId().trim());
        appVersion.setRoom(serverVersion.getRoom().trim());

        appVersion.setDropin(serverVersion.isDropin());
        appVersion.setOpenForReservations(serverVersion.isOpenForReservations());
        appVersion.setCancellationStatus(serverVersion.getCancellationStatus());

        appVersion.setReservationDeadline(appVersion.getStartDate() - TimeUnit.MINUTES.toMillis(5));
        return appVersion;
    }

    @ToJson
    String toJson(Workout workout) {
        throw new UnsupportedOperationException();
    }
}
