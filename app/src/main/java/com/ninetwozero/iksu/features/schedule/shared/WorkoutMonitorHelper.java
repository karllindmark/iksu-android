package com.ninetwozero.iksu.features.schedule.shared;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.PersistableBundle;

import com.ninetwozero.iksu.app.IksuApp;
import com.ninetwozero.iksu.models.Workout;
import com.ninetwozero.iksu.utils.Constants;

import java.util.concurrent.TimeUnit;

import io.realm.Realm;

public class WorkoutMonitorHelper {
    private static final long PERIODIC_INTERVAL = TimeUnit.MINUTES.toMillis(30);

    private final Context context;
    private final JobScheduler jobScheduler;

    public WorkoutMonitorHelper(final Context context) {
        this.context = context;
        this.jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
    }

    public void schedule(final Workout workout) {
        final long now = System.currentTimeMillis();
        final PersistableBundle bundle = new PersistableBundle();
        bundle.putString(Constants.USERNAME, IksuApp.getActiveUsername());

        final ComponentName monitorService = new ComponentName(context, IksuMonitorService.class);
        final JobInfo periodicWorkoutChecker = new JobInfo.Builder(IksuMonitorService.PERIODIC_JOB_ID, monitorService)
            .setExtras(bundle)
            .setPeriodic(PERIODIC_INTERVAL)
            .build();

        final long workoutCancellationDeadline = workout.getStartDate() - now - TimeUnit.HOURS.toMillis(1);
        final JobInfo oneHourBeforeWorkoutChecker = new JobInfo.Builder(Integer.parseInt(workout.getId()), monitorService)
            .setExtras(bundle)
            .setMinimumLatency(workoutCancellationDeadline - TimeUnit.MINUTES.toMillis(2))
            .setOverrideDeadline(workoutCancellationDeadline + TimeUnit.MINUTES.toMillis(2))
            .build();

        jobScheduler.schedule(periodicWorkoutChecker);
        jobScheduler.schedule(oneHourBeforeWorkoutChecker);
    }

    // TODO: Unschedule any monitors after a reservation is made OR the workout has started
    public void unschedule(final Workout workout) {
        jobScheduler.cancel(Integer.parseInt(workout.getId()));

        try (Realm realm = Realm.getDefaultInstance()){
            final long monitorsLeft = realm.where(Workout.class)
                .equalTo(Constants.CONNECTED_ACCOUNT, IksuApp.getActiveUsername())
                .equalTo(Constants.MONITORING, true)
                .notEqualTo(Constants.ID, workout.getId())
                .count();

            if (monitorsLeft == 0) {
                jobScheduler.cancel(IksuMonitorService.PERIODIC_JOB_ID);
            }
        }
    }
}
