package com.ninetwozero.iksu.features.schedule.shared;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.ninetwozero.iksu.R;
import com.ninetwozero.iksu.features.schedule.detail.WorkoutDetailActivity;
import com.ninetwozero.iksu.features.schedule.detail.WorkoutDetailFragment;
import com.ninetwozero.iksu.models.Workout;
import com.ninetwozero.iksu.utils.Constants;
import com.ninetwozero.iksu.utils.WorkoutServiceHelper;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class IksuMonitorService extends JobService {
    public static final int PERIODIC_JOB_ID = 10092000;

    private MonitoringSetupTask task;

    @Override
    public boolean onStartJob(final JobParameters params) {
        final String username = params.getExtras().getString(Constants.USERNAME);
        Log.d("YOLO", "Running the monitoring for " + username);
        task = new MonitoringSetupTask() {
            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);

                if (result) {
                    notifyUserAboutAvailableSlots(this.workoutsToNotifyAbout);
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
        final int workoutCount = workouts.size();
        if (workoutCount == 0) {
            return;
        }

        final TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(WorkoutDetailActivity.class);
        stackBuilder.addNextIntent(
            new Intent(this, WorkoutDetailActivity.class)
                .putExtra(WorkoutDetailFragment.WORKOUT_ID, workouts.get(0).getPkId())
        );

        final NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        for (int i = 0, max = workoutCount < 3 ? workoutCount : 3; i < max; i++) {
            inboxStyle.addLine(workouts.get(i).getStartDateString() + ": " + workouts.get(i).getTitle());
        }

        if (workoutCount > 3) {
            inboxStyle.setSummaryText("+" + (workoutCount - 3) + " more");
        }


        final PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        final Notification notification = new NotificationCompat.Builder(this, Constants.NOTIFICATION_MONITOR)
            .setContentTitle(getString(R.string.label_notification_slots_available))
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentIntent(pendingIntent)
            .setStyle(inboxStyle)
            .build();

        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(10001, notification);
    }

    private RealmResults<Workout> loadMonitoredWorkouts(final Realm realm, final String username) {
        return realm.where(Workout.class)
            .equalTo(Constants.CONNECTED_ACCOUNT, username)
            .equalTo(Constants.MONITORING, true)
            .findAllSorted(Constants.START_DATE, Sort.ASCENDING);
    }

    private class MonitoringSetupTask extends AsyncTask<String, Void, Boolean> {
        List<Workout> workoutsToNotifyAbout = new ArrayList<>();

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
                                for (Workout workout : monitoredWorkouts) {
                                    if (!workout.isFullyBooked()) {
                                        workoutsToNotifyAbout.add(realm.copyFromRealm(workout));
                                    }
                                }
                            }
                        }
                    };
                    new WorkoutServiceHelper(getApplicationContext(), callback).fetchAndStoreWorkouts(true);
                    return workoutsToNotifyAbout.size() > 0;
                }
            }
            return false;
        }
    }
}
