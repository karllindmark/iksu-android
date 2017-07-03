package com.ninetwozero.iksu.features.shared;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.ninetwozero.iksu.R;
import com.ninetwozero.iksu.features.schedule.detail.WorkoutDetailActivity;
import com.ninetwozero.iksu.features.schedule.detail.WorkoutDetailFragment;
import com.ninetwozero.iksu.models.Workout;
import com.ninetwozero.iksu.network.IksuWorkoutService;
import com.ninetwozero.iksu.utils.Constants;
import com.ninetwozero.iksu.utils.WorkoutServiceHelper;

import io.realm.Realm;

public class DeepLinkingActivity extends AppCompatActivity {

    private String workoutId;
    private WorkoutBroadcastReceiver broadcastReceiver = new WorkoutBroadcastReceiver();


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            final Uri iksuUri = intent.getData();
            if (!"rd".equals(iksuUri.getQueryParameter("func"))) {
                Toast.makeText(getApplicationContext(), "Unsupported link: " + iksuUri.toString(), Toast.LENGTH_SHORT).show();
                finish();
            }

            workoutId = iksuUri.getQueryParameter("id");
            final Workout workout = Realm.getDefaultInstance().where(Workout.class).equalTo(Constants.ID, workoutId).findFirst();
            if (workout == null) {
                setContentView(R.layout.activity_deep_linking);
                startService(IksuWorkoutService.newIntent(getApplicationContext(), IksuWorkoutService.ACTION, workoutId));
            } else {
                openWorkoutDetailView(workout);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(IksuWorkoutService.ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }


    private Workout getWorkoutFromDatabase() {
        return Realm.getDefaultInstance().where(Workout.class).equalTo(Constants.ID, workoutId).findFirst();
    }

    private void openWorkoutDetailView(final Workout workout) {
        startActivity(
            new Intent(getApplicationContext(), WorkoutDetailActivity.class)
                .putExtra(WorkoutDetailFragment.WORKOUT_ID, workoutId)
                .putExtra(WorkoutDetailFragment.WORKOUT_TITLE, workout.getTitle())
        );
        finish();
    }

    private class WorkoutBroadcastReceiver extends BroadcastReceiver {
        private WorkoutBroadcastReceiver() {}

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(IksuWorkoutService.ACTION)) {
                if (intent.hasExtra(IksuWorkoutService.STATUS)) {
                    final int count = intent.getIntExtra(IksuWorkoutService.STATUS, WorkoutServiceHelper.RESULT_ERROR);
                    if (count == 1) {
                        openWorkoutDetailView(getWorkoutFromDatabase());
                    } else {
                        findViewById(R.id.progress).setVisibility(View.GONE);
                        findViewById(R.id.status_text).setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }
}
