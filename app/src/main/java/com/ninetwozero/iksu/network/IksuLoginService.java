package com.ninetwozero.iksu.network;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.ninetwozero.iksu.utils.WorkoutServiceHelper;

public class IksuLoginService extends BaseIksuService {
    public static final String ACTION_LOGIN = "com.ninetwozero.iksu.action.LOGIN";
    public static final String USERNAME = ACTION_LOGIN + ".USERNAME";
    public static final String PASSWORD = ACTION_LOGIN + ".PASSWORD";

    public static final String ACTION_LOGIN_PROGRESS = ACTION_LOGIN + ".PROGRESS";
    public static final String PROGRESS = ACTION_LOGIN_PROGRESS + ".STATE";
    public static final String STATE_LOGIN_STARTED = "state_login_started";
    public static final String STATE_LOGIN_OK_FETCHING_WORKOUTS = "state_login_fetching_workouts";
    public static final String STATE_LOGIN_DONE = "state_login_done";

    public static final int LOGIN_REQUESTED = 1001;

    public static Intent newIntent(final Context context, final String action, final Bundle extras) {
        return new Intent(context, IksuLoginService.class).setAction(action).putExtras(extras);
    }

    public IksuLoginService() {
        super(IksuLoginService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }

        broadcastEvent(ACTION_LOGIN_PROGRESS, PROGRESS, STATE_LOGIN_STARTED);

        final String username = intent.getStringExtra(USERNAME);
        final String password = intent.getStringExtra(PASSWORD);
        final int status = new LoginHelper(getApplicationContext()).doLogin(username, password);

        if (status == LoginHelper.RESULT_OK) {
            final WorkoutServiceHelper.Callback callback = new WorkoutServiceHelper.Callback() {
                @Override
                public void onNewState(int state) {
                    final String stateString = state == WorkoutServiceHelper.STATE_DONE ? STATE_LOGIN_DONE : STATE_LOGIN_OK_FETCHING_WORKOUTS;
                    broadcastEvent(ACTION_LOGIN_PROGRESS, PROGRESS, stateString);
                }
            };
            new WorkoutServiceHelper(getApplicationContext(), callback).fetchAndStoreWorkouts(true);
        }

        broadcastStatus(intent.getAction(), status);
    }
}
