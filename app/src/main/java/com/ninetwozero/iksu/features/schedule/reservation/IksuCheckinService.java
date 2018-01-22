package com.ninetwozero.iksu.features.schedule.reservation;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.ninetwozero.iksu.BuildConfig;
import com.ninetwozero.iksu.app.IksuApp;
import com.ninetwozero.iksu.models.Workout;
import com.ninetwozero.iksu.network.ApiErrorResponse;
import com.ninetwozero.iksu.network.BaseIksuService;
import com.ninetwozero.iksu.utils.ApiHelper;
import com.ninetwozero.iksu.utils.Constants;

import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import okhttp3.RequestBody;
import retrofit2.Response;

public class IksuCheckinService extends BaseIksuService {
    public static final int STATUS_CHECKIN_OK = -1;
    public static final int STATUS_NOID = 0;
    public static final int STATUS_CHECKIN_FAILED = 1;

    public static final String ACTION = BuildConfig.APPLICATION_ID + ".CHECK_IN";

    public IksuCheckinService() {
        super(IksuCheckinService.class.getSimpleName());
    }

    public static Intent newInstance(final Context context, final String id) {
        return new Intent(context, IksuCheckinService.class).putExtra(Constants.ID, id);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) {
            broadcastStatus(ACTION, STATUS_NOID);
            return;
        }

        final String id = intent.getStringExtra(Constants.ID);
        if (TextUtils.isEmpty(id)) {
            broadcastStatus(ACTION, STATUS_NOID);
            return;
        }

        String errorKey = "";
        try (Realm realm = Realm.getDefaultInstance()) {
            final Workout workout = realm.where(Workout.class).equalTo("pkId", id).findFirst();

            final Response<String> checkinResponse = IksuApp.getApi()
                .checkin(workout.getReservationId(), createRequestBodyForCheckin(workout))
                .execute();

            if (checkinResponse.isSuccessful()) {
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        workout.setCheckedIn(true);
                        realm.copyToRealmOrUpdate(workout);
                    }
                });
                broadcastStatus(ACTION, STATUS_CHECKIN_OK);
                return;
            } else {
                final ApiErrorResponse apiError = IksuApp.getMoshi()
                    .adapter(ApiErrorResponse.class)
                    .fromJson(checkinResponse.errorBody().source());
                if (apiError != null) {
                    errorKey = apiError.getKey();
                }
            }
        } catch (SocketTimeoutException ignored) {
            errorKey = "NetworkError";
        } catch (IOException ignored) {}
        broadcastStatus(ACTION, STATUS_CHECKIN_FAILED, new Intent().putExtra(Constants.ERROR_KEY, errorKey).getExtras());
    }

    private RequestBody createRequestBodyForCheckin(final Workout workout) {
        final Map<String, Object> checkinParams = new HashMap<String, Object>() {{
            put("SessionId", IksuApp.getActiveAccount().getSessionId());
            put("ClassId", workout.getId());
            put("LocationId", workout.getFacilityId());
        }};
        return ApiHelper.createRequestBody((new JSONObject(checkinParams)).toString().getBytes());
    }
}
