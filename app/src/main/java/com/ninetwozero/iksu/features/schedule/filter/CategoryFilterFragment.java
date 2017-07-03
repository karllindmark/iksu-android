package com.ninetwozero.iksu.features.schedule.filter;

import android.os.Bundle;

import com.ninetwozero.iksu.app.IksuApp;
import com.ninetwozero.iksu.features.schedule.shared.WorkoutUiHelper;
import com.ninetwozero.iksu.models.Workout;
import com.ninetwozero.iksu.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import io.realm.RealmResults;
import io.realm.Sort;

public class CategoryFilterFragment extends BaseFilterFragment {
    public static CategoryFilterFragment newInstance() {
        final Bundle bundle = new Bundle();
        final CategoryFilterFragment fragment = new CategoryFilterFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected List<ScheduleFilterItem> getFilterItems() {
        final List<ScheduleFilterItem> filterItems = new ArrayList<>();

        final String connectedAccount = IksuApp.getActiveUsername();
        final Map<String, ScheduleFilterItem> activeFilterItems = getActiveFilterItems(connectedAccount, ScheduleFilterItem.ROW_FILTER_TYPE);

        final RealmResults<Workout> allWorkouts = realm.where(Workout.class).equalTo(Constants.CONNECTED_ACCOUNT, connectedAccount).distinct("type").sort("type", Sort.ASCENDING);
        for (Workout workout : allWorkouts) {
            filterItems.add(
                new ScheduleFilterItem(
                        workout.getType(),
                        ScheduleFilterItem.ROW_FILTER_TYPE,
                        (activeFilterItems.containsKey(workout.getType())),
                        connectedAccount
                )
            );
        }

        final WorkoutUiHelper workoutUiHelper = new WorkoutUiHelper();
        Collections.sort(filterItems, new Comparator<ScheduleFilterItem>() {
            @Override
            public int compare(ScheduleFilterItem o1, ScheduleFilterItem o2) {
                return getString(workoutUiHelper.getTitleForFilter(o1.getId(), o1.getType())).compareTo(getString(workoutUiHelper.getTitleForFilter(o2.getId(), o2.getType())));
            }
        });
        return filterItems;
    }
}
