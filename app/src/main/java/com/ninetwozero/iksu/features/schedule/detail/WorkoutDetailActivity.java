package com.ninetwozero.iksu.features.schedule.detail;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import com.ninetwozero.iksu.R;
import com.ninetwozero.iksu.app.IksuApp;
import com.ninetwozero.iksu.common.ui.BaseActivity;
import com.ninetwozero.iksu.models.Workout;
import com.ninetwozero.iksu.utils.Constants;


public class WorkoutDetailActivity extends BaseActivity {
    private WorkoutDetailFragment fragment;
    private String workoutId;
    private String workoutTitle;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        workoutId = getIntent().getStringExtra(WorkoutDetailFragment.WORKOUT_ID);
        workoutTitle = getIntent().getStringExtra(WorkoutDetailFragment.WORKOUT_TITLE);

        final long matchingWorkoutCount = realm.where(Workout.class)
            .equalTo(Constants.ID, workoutId)
            .equalTo(Constants.CONNECTED_ACCOUNT, IksuApp.getActiveUsername())
            .count();

        if (matchingWorkoutCount == 0) {
            finish();
            return;
        }

        showDetailFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_schedule_detail;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDetailFragment() {
        fragment = WorkoutDetailFragment.newInstance(workoutId, workoutTitle);
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragment, fragment)
            .commit();
    }
}
