package com.ninetwozero.iksu.features.schedule.detail;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import com.ninetwozero.iksu.R;
import com.ninetwozero.iksu.common.ui.BaseActivity;


public class WorkoutDetailActivity extends BaseActivity {
    private WorkoutDetailFragment fragment;
    private String workoutId;
    private String workoutTitle;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        workoutId = getIntent().getStringExtra(WorkoutDetailFragment.WORKOUT_ID);
        workoutTitle = getIntent().getStringExtra(WorkoutDetailFragment.WORKOUT_TITLE);

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
