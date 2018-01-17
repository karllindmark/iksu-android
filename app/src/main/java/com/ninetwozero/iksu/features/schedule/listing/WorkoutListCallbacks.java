package com.ninetwozero.iksu.features.schedule.listing;

import android.view.View;

import com.ninetwozero.iksu.models.Workout;

public interface WorkoutListCallbacks {
    void onWorkoutClick(final View view, final Workout workout);
    void onItemCountChanged(int count);
}
