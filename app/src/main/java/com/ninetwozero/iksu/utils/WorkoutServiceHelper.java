package com.ninetwozero.iksu.utils;

import android.content.Context;
import android.content.Intent;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.ninetwozero.iksu.app.IksuApp;
import com.ninetwozero.iksu.models.UserAccount;
import com.ninetwozero.iksu.models.Workout;
import com.ninetwozero.iksu.models.WorkoutReservation;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.realm.Realm;
import retrofit2.Response;

import static com.ninetwozero.iksu.network.IksuWorkoutService.DAYS_TO_SHOW;

public class WorkoutServiceHelper {
    public static final int RESULT_ERROR = -1;

    public static final int STATE_DOWNLOADING = 1001;
    public static final int STATE_DONE = 1002;

    private final Context context;
    private final Callback callback;

    public WorkoutServiceHelper(Context context) {
        this.context = context;
        this.callback = null;
    }

    public WorkoutServiceHelper(Context context, final Callback callback) {
        this.context = context;
        this.callback = callback;
    }

    public int fetchAndStoreWorkout(final String id) {
        final Realm realm = Realm.getDefaultInstance();
        final UserAccount userAccount = realm.where(UserAccount.class).equalTo(Constants.USERNAME, IksuApp.getActiveUsername()).findFirst();

        if (callback != null) {
            callback.onNewState(STATE_DOWNLOADING);
        }

        int status = 0;
        try {
            final Response<Workout> response = IksuApp.getApi().getWorkoutById(id).execute();
            final Workout workout = response.body();
            if (workout != null) {
                long reservationId = 0;
                if (userAccount != null && !userAccount.isDisabled()) {
                    final List<WorkoutReservation> reservations = IksuApp.getApi().getUserReservations(userAccount.getSessionId()).execute().body();
                    if (reservations != null) {
                        for (WorkoutReservation reservation : reservations) {
                            if (reservation.getWorkoutId().equals(workout.getId())) {
                                reservationId = reservation.getId();
                                break;
                            }
                        }
                    }

                }
                status = handleResponseBodyForSingle(workout, reservationId, realm);
            }
        } catch (IOException ignored) {
            status = RESULT_ERROR;
        }

        if (callback != null) {
            callback.onNewState(STATE_DONE);
        }

        realm.close();
        return status;
    }

    public int fetchAndStoreWorkouts(final boolean shouldDoUserCall) {
        final String fromDate = DateUtils.getDate(0);
        final String toDate = DateUtils.getDate(DAYS_TO_SHOW);

        final Realm realm = Realm.getDefaultInstance();
        final UserAccount userAccount = shouldDoUserCall ? realm.where(UserAccount.class).equalTo(Constants.USERNAME, IksuApp.getActiveUsername()).findFirst() : null;

        if (callback != null) {
            callback.onNewState(STATE_DOWNLOADING);
        }

        int status;
        try {
            List<Workout> workouts;
            List<WorkoutReservation> reservations;
            if (shouldDoUserCall && !userAccount.isDisabled()) {
                workouts =  IksuApp.getApi().getUserWorkoutsBetweenDates(userAccount.getSessionId(), fromDate, toDate).execute().body();
                reservations = IksuApp.getApi().getUserReservations(userAccount.getSessionId()).execute().body();
            } else {
                workouts = IksuApp.getApi().getWorkoutsBetweenDates(fromDate, toDate).execute().body();
                reservations = null;
            }

            status = handleResponseBodyForMultiple(
                    workouts,
                    reservations,
                    realm,
                    userAccount,
                    shouldDoUserCall && !userAccount.isDisabled()
            );
        } catch (IOException ignored) {
            status = WorkoutServiceHelper.RESULT_ERROR;
        }

        if (callback != null) {
            callback.onNewState(STATE_DONE);
        }

        realm.close();
        return status;
    }

    private int handleResponseBodyForSingle(final Workout workout, long reservationId, Realm realm) {
        realm.beginTransaction();

        final Workout oldWorkout = realm.where(Workout.class)
                .equalTo(Constants.ID, workout.getId())
                .equalTo(Constants.CONNECTED_ACCOUNT, IksuApp.getActiveUsername())
                .findFirst();

        if (oldWorkout != null) {
            workout.setReservationId(reservationId);
            workout.setPkId(workout.getId() + "_" + oldWorkout.getConnectedAccount());
            workout.setConnectedAccount(oldWorkout.getConnectedAccount());
            workout.setRatedByUser(oldWorkout.isRatedByUser());
        } else {
            if (IksuApp.hasSelectedAccount()) {
                workout.setPkId(workout.getId() + "_" + IksuApp.getActiveUsername());
                workout.setConnectedAccount(IksuApp.getActiveUsername());
            } else {
                workout.setPkId(workout.getId());
            }
        }

        realm.insertOrUpdate(workout);
        realm.commitTransaction();
        return 1;
    }


    private int handleResponseBodyForMultiple(final List<Workout> workouts, final List<WorkoutReservation> reservations, Realm realm, UserAccount userAccount, boolean wasUserCall) {
        realm.beginTransaction();

        int result;
        if (workouts == null) {
            if (wasUserCall) {
                FirebaseAnalytics.getInstance(context).logEvent("CREDENTIALS_INVALIDATED", new Intent().putExtra(Constants.SESSION_ID, userAccount.getSessionId()).getExtras());
            }
            result = RESULT_ERROR;
        } else {
            final String connectedAccount = userAccount == null ? null : userAccount.getUsername();
            final Map<String, Long> workoutMap = new HashMap<>();
            if (reservations != null && reservations.size() > 0) {
                final Long[] reservationIds = new Long[reservations.size()];

                int i = 0;
                for (WorkoutReservation reservation : reservations) {
                    reservationIds[i] = reservation.getId();
                    workoutMap.put(reservation.getWorkoutId(), reservation.getId());
                    i++;
                }

                if (connectedAccount != null) {
                    removeNonExistentFutureReservations(realm, reservationIds, connectedAccount);
                }
            }

            final Set<String> newWorkoutIds = new HashSet<>(workouts.size());
            for (Workout workout : workouts) {
                if (wasUserCall) {
                    workout.setPkId(workout.getId() + "_" + connectedAccount);
                    workout.setConnectedAccount(connectedAccount);
                } else {
                    workout.setPkId(workout.getId());
                }

                if (workoutMap.containsKey(workout.getId())) {
                    workout.setReservationId(workoutMap.get(workout.getId()));
                }

                newWorkoutIds.add(workout.getId());
            }

            removeObsoleteWorkoutsWithoutReservations(realm, newWorkoutIds, connectedAccount);

            realm.insertOrUpdate(workouts);
            result = workouts.size();
        }
        realm.commitTransaction();
        return result;
    }

    private void removeObsoleteWorkoutsWithoutReservations(Realm realm, Set<String> newWorkoutIds, String connectedAccount) {
        realm.where(Workout.class)
            .beginGroup()
            .equalTo(Constants.CONNECTED_ACCOUNT, connectedAccount)
            .equalTo(Constants.RESERVATION_ID, 0)
            .endGroup()
            .not()
            .in(Constants.ID, newWorkoutIds.toArray(new String[newWorkoutIds.size()]))
            .findAll()
            .deleteAllFromRealm();
    }

    private void removeNonExistentFutureReservations(Realm realm, Long[] reservationIds, String connectedAccount) {
        realm.where(Workout.class)
                .beginGroup()
                .equalTo(Constants.CONNECTED_ACCOUNT, connectedAccount)
                .greaterThan(Constants.START_DATE, DateUtils.nowInMillis())
                .notEqualTo(Constants.RESERVATION_ID, 0)
                .endGroup()
                .not()
                .in(Constants.RESERVATION_ID, reservationIds)
                .findAll()
                .deleteAllFromRealm();
    }

    public interface Callback {
        void onNewState(int state);
    }
}
