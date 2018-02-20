package com.ninetwozero.iksu.features.schedule.shared;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.AsyncTask;

import com.ninetwozero.iksu.common.NotificationHelper;
import com.ninetwozero.iksu.models.Workout;
import com.ninetwozero.iksu.utils.Constants;
import com.ninetwozero.iksu.utils.WorkoutServiceHelper;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import timber.log.Timber;

public class IksuMonitorService extends JobService {
    public static final int PERIODIC_JOB_ID = 10092000;

    private MonitoringSetupTask task;

    @Override
    public boolean onStartJob(final JobParameters params) {
        final String username = params.getExtras().getString(Constants.USERNAME);
        Timber.i("Starting %s", IksuMonitorService.class.getSimpleName());
        task = new MonitoringSetupTask() {
            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                Timber.i(
                    "Done, found %d workouts to notify about, and %d to remove",
                    this.workoutsToNotifyAbout.size(),
                    this.workoutsToRemoveMonitoringFor.size()
                );

                if (result) {
                    handleWorkoutsWhereMonitoringIsObsolete(this.workoutsToRemoveMonitoringFor);
                    if (this.workoutsToNotifyAbout.size() > 0) {
                        notifyUserAboutAvailableSlots(this.workoutsToNotifyAbout);
                    } else {
                        removeNotifications();
                    }
                }
                jobFinished(params, !result);

                task = null;
            }
        };
        task.execute(username);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        if (task != null) {
            task.cancel(true);
        }

        // YAY for rescheduling
        return true;
    }

    private void notifyUserAboutAvailableSlots(final List<Workout> workouts) {
        new NotificationHelper(getApplication()).notify(workouts);
    }
    private void removeNotifications() {
        new NotificationHelper(getApplication()).removeNotification();
    }

    private void handleWorkoutsWhereMonitoringIsObsolete(final List<Workout> workouts) {
        new WorkoutMonitorHelper(getApplication()).unschedule(workouts);

        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    for (Workout workout : workouts) {
                        Timber.i("Setting monitoring to false for %s (%s)", workout.getTitle(), workout.getId());
                        workout.setMonitoring(false);
                    }

                    realm.insertOrUpdate(workouts);
                }
            });
        }
    }

    private RealmResults<Workout> loadMonitoredWorkouts(final Realm realm, final String username) {
        return realm.where(Workout.class)
            .equalTo(Constants.CONNECTED_ACCOUNT, username)
            .equalTo(Constants.MONITORING, true)
            .findAllSorted(Constants.START_DATE, Sort.ASCENDING);
    }

    private class MonitoringSetupTask extends AsyncTask<String, Void, Boolean> {
        List<Workout> workoutsToNotifyAbout = new ArrayList<>();
        List<Workout> workoutsToRemoveMonitoringFor = new ArrayList<>();

        @Override
        protected Boolean doInBackground(String... strings) {
            if (strings.length == 0) {
                return false;
            }

            final String username = strings[0];
            try (Realm realm = Realm.getDefaultInstance()) {
                final int monitoredWorkoutCount = loadMonitoredWorkouts(realm, username).size();
                if (monitoredWorkoutCount > 0) {
                    final WorkoutServiceHelper.Callback callback = new WorkoutServiceHelper.Callback() {
                        @Override
                        public void onNewState(int state) {
                            if (state == WorkoutServiceHelper.STATE_DONE) {
                                final RealmResults<Workout> monitoredWorkouts = loadMonitoredWorkouts(realm, username);
                                final long now = System.currentTimeMillis();
                                for (Workout workout : monitoredWorkouts) {
                                    if (now > workout.getStartDate()) {
                                        Timber.i("Adding " + workout.getId() + " to the removal list");
                                        workoutsToRemoveMonitoringFor.add(realm.copyFromRealm(workout));
                                    } else {
                                        if (!workout.isFullyBooked()) {
                                            Timber.i("Adding " + workout.getId() + " to the notification list");
                                            workoutsToNotifyAbout.add(realm.copyFromRealm(workout));
                                        }
                                    }
                                }
                            }
                        }
                    };
                    new WorkoutServiceHelper(getApplicationContext(), callback).fetchAndStoreWorkouts(true);
                    return true;
                }
            }
            return false;
        }
    }
}
