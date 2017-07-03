package com.ninetwozero.iksu.network;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.ninetwozero.iksu.app.IksuApp;
import com.ninetwozero.iksu.utils.DateUtils;
import com.ninetwozero.iksu.utils.WorkoutServiceHelper;

public class IksuWorkoutService extends BaseIksuService {
    public static final int  DAYS_TO_SHOW = 8;

    public static final String ACTION = "com.ninetwozero.iksu.action.LOAD_WORKOUTS";
    public static final String ONLY_RELEVANT_TO_LOGIN = ACTION + ".ONLY_RELEVANT_TO_LOGIN";
    public static final String WORKOUT_ID = ACTION + ".WORKOUT_ID";

    public static Intent newIntent(final Context context, final String action, final String id) {
        return new Intent(context, IksuWorkoutService.class).setAction(action).putExtra(WORKOUT_ID, id);
    }

    public static Intent newIntent(final Context context, final String action, final boolean onlyLogin) {
        return new Intent(context, IksuWorkoutService.class).setAction(action).putExtra(ONLY_RELEVANT_TO_LOGIN, onlyLogin);
    }

    public IksuWorkoutService() {
        super(IksuWorkoutService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }

        int status;
        broadcastEvent(ACTION, LOADING);

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final WorkoutServiceHelper workoutServiceHelper = new WorkoutServiceHelper(getApplicationContext());

        if (intent.getStringExtra(WORKOUT_ID) != null) {
            status = workoutServiceHelper.fetchAndStoreWorkout(intent.getStringExtra(WORKOUT_ID));
        } else {
            final boolean shouldDoUserCall = IksuApp.hasSelectedAccount() && intent.getBooleanExtra(ONLY_RELEVANT_TO_LOGIN, true);
            status = workoutServiceHelper.fetchAndStoreWorkouts(shouldDoUserCall);
        }

        sharedPreferences.edit().putLong(IksuApp.getLatestRefreshKey(), DateUtils.nowInMillis()).apply();
        broadcastStatus(intent.getAction(), status);
    }
}
