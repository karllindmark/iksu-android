package com.ninetwozero.iksu.network;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.ninetwozero.iksu.app.IksuApp;
import com.ninetwozero.iksu.models.Workout;
import com.ninetwozero.iksu.network.dto.BookingResponse;
import com.ninetwozero.iksu.network.dto.CancelReservationResponse;
import com.ninetwozero.iksu.utils.ApiHelper;
import com.ninetwozero.iksu.utils.Constants;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import okhttp3.RequestBody;
import retrofit2.Response;

public class IksuReservationService extends BaseIksuService {
    public static final String ACTION_CANCEL = "com.ninetwozero.iksu.action.CANCEL_RESERVATION";
    public static final String ACTION_CREATE = "com.ninetwozero.iksu.action.CREATE_RESERVATION";

    public static final String WORKOUT_ID = "workoutId";

    public static final int RESULT_OK = 0;
    public static final int RESULT_ERROR = 1;
    public static final int RESULT_RESERVATION_ERROR = 2;

    public static Intent newIntent(final Context context, final String action, final String id) {
        return new Intent(context, IksuReservationService.class).setAction(action).putExtra(WORKOUT_ID, id);

    }

    public IksuReservationService() {
        super(IksuReservationService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }

        final String action = intent.getAction();
        if (action == null) {
            // No action? No runtime!
            return;
        }

        final String workoutId = intent.getStringExtra(WORKOUT_ID);
        final Realm realm = Realm.getDefaultInstance();
        final Workout workout = realm.where(Workout.class).equalTo(Constants.ID, workoutId).equalTo(Constants.CONNECTED_ACCOUNT, IksuApp.getActiveUsername()).findFirst();

        int status = RESULT_ERROR;
        if (workout != null) {
            if (action.equals(ACTION_CREATE)) {
                status = doCreateReservation(workout, realm);
            } else if (action.equals(ACTION_CANCEL)) {
                status = doCancelReservation(workout, realm);
            }
        }

        broadcastStatus(intent.getAction(), status, intent.getExtras());
        realm.close();
    }

    private int doCreateReservation(final Workout workout, final Realm realm) {
        final Map<String, Object> bookingParams = new HashMap<String, Object>() {{
            put("SessionId", IksuApp.getActiveAccount().getSessionId());
            put("ClassId", workout.getId());
            put("LocationId", workout.getFacilityId());
        }};

        final RequestBody requestBody = ApiHelper.createRequestBody((new JSONObject(bookingParams)).toString().getBytes());
        try {
            Response<BookingResponse> response = IksuApp.getApi().createReservation(requestBody).execute();
            if (response.isSuccessful()) {
                final BookingResponse bookingResponse = response.body();
                if (bookingResponse != null) {
                    if (bookingResponse.getReservationId() > 0) {
                        final Workout updatedWorkout = refreshWorkout(workout.getId(), bookingResponse.getReservationId());
                        if (updatedWorkout != null) {
                            realm.beginTransaction();
                            realm.insertOrUpdate(updatedWorkout);
                            realm.commitTransaction();
                            return RESULT_OK;
                        }
                        return RESULT_RESERVATION_ERROR;
                    }
                }
            }
        } catch (IOException ignored) {}
        return RESULT_ERROR;
    }

    private int doCancelReservation(final Workout workout, final Realm realm) {
        try {
            final Response<CancelReservationResponse> response = IksuApp.getApi().cancelReservation(
                    IksuApp.getActiveAccount().getSessionId(),
                    workout.getId(),
                    workout.getReservationId(),
                    workout.getFacilityId()
            ).execute();

            if (response.isSuccessful()) {
                final CancelReservationResponse reservationResponse = response.body();
                if (reservationResponse != null && reservationResponse.getErrorCode() == 0) {
                    final Workout updatedWorkout = refreshWorkout(workout.getId(), 0);
                    if (updatedWorkout != null) {
                        realm.beginTransaction();
                        realm.insertOrUpdate(updatedWorkout);
                        realm.commitTransaction();
                        return RESULT_OK;
                    }
                    return RESULT_ERROR;
                }
            }
        } catch (IOException ignored) {}
        return RESULT_ERROR;
    }

    private Workout refreshWorkout(final String workoutId, final long reservationId) throws IOException {
        final Response<Workout> workoutResponse = IksuApp.getApi().getWorkoutById(workoutId).execute();
        if (workoutResponse.isSuccessful()) {
            final Workout updatedWorkout = workoutResponse.body();
            if (updatedWorkout != null) {
                updatedWorkout.setConnectedAccount(IksuApp.getActiveUsername());
                updatedWorkout.setReservationId(reservationId);
                updatedWorkout.setPkId(workoutId + "_" + updatedWorkout.getConnectedAccount());
            }
            return updatedWorkout;
        }
        return null;
    }
}
