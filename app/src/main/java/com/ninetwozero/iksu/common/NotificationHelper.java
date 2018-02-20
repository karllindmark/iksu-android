package com.ninetwozero.iksu.common;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.ninetwozero.iksu.R;
import com.ninetwozero.iksu.features.schedule.detail.WorkoutDetailActivity;
import com.ninetwozero.iksu.features.schedule.detail.WorkoutDetailFragment;
import com.ninetwozero.iksu.models.Workout;
import com.ninetwozero.iksu.utils.Constants;
import com.ninetwozero.iksu.utils.DateUtils;

import java.util.List;

import timber.log.Timber;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NotificationHelper {
    private static final int NOTIFICATION_ID = 1001;

    private final Application application;
    private final NotificationManager notificationManager;

    public NotificationHelper(final Application application) {
        this.application = application;
        this.notificationManager = ((NotificationManager) application.getSystemService(NOTIFICATION_SERVICE));
    }

    public void notify(final List<Workout> workouts) {
        final int workoutCount = workouts.size();
        if (workoutCount == 0) {
            return;
        }

        final TaskStackBuilder stackBuilder = TaskStackBuilder.create(application);
        stackBuilder.addParentStack(WorkoutDetailActivity.class);
        stackBuilder.addNextIntent(
            new Intent(application, WorkoutDetailActivity.class)
                .putExtra(WorkoutDetailFragment.WORKOUT_ID, workouts.get(0).getId())
        );

        final NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        for (int i = 0, max = workoutCount < 3 ? workoutCount : 3; i < max; i++) {
            inboxStyle.addLine(
                application.getString(
                    R.string.label_notification_line_text,
                    DateUtils.getDayAndTime(application, workouts.get(i).getStartDate()),
                    workouts.get(i).getTitle(),
                    workouts.get(i).getInstructorNames()
                )
            );
        }

        if (workoutCount > 3) {
            inboxStyle.setSummaryText(application.getString(R.string.msg_x_more, (workoutCount - 3)));
        }

        final PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        final Notification notification = new NotificationCompat.Builder(application, Constants.NOTIFICATION_MONITOR)
            .setContentTitle(application.getString(R.string.label_notification_slots_available))
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentIntent(pendingIntent)
            .setStyle(inboxStyle)
            .setAutoCancel(true)
            .setNumber(workoutCount)
            .setOnlyAlertOnce(true)
            .build();

        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    public void removeNotification() {
        Timber.i("No slots free in monitored workouts, removing the notification...");
        notificationManager.cancel(NOTIFICATION_ID);
    }
}
