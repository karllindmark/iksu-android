package com.ninetwozero.iksu.features.schedule.filter;

import android.os.Bundle;
import android.text.TextUtils;

import com.ninetwozero.iksu.app.IksuApp;
import com.ninetwozero.iksu.models.Workout;
import com.ninetwozero.iksu.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import io.realm.RealmResults;
import io.realm.Sort;

public class InstructorFilterFragment extends BaseFilterFragment {
    public static InstructorFilterFragment newInstance() {
        final Bundle bundle = new Bundle();
        final InstructorFilterFragment fragment = new InstructorFilterFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected List<ScheduleFilterItem> getFilterItems() {
        final List<ScheduleFilterItem> filterItems = new ArrayList<>();

        final String connectedAccount = IksuApp.getActiveUsername();
        final Map<String, ScheduleFilterItem> activeFilterItems = getActiveFilterItems(connectedAccount, ScheduleFilterItem.ROW_FILTER_INSTRUCTOR);

        final RealmResults<Workout> allWorkouts = realm.where(Workout.class).equalTo(Constants.CONNECTED_ACCOUNT, connectedAccount).distinct("instructorKey").sort("instructor", Sort.ASCENDING);
        for (Workout workout : allWorkouts) {
            if (TextUtils.isEmpty(workout.getInstructor().trim())) {
                continue;
            }

            filterItems.add(
                    new ScheduleFilterItem(
                            workout.getInstructorKey(),
                            ScheduleFilterItem.ROW_FILTER_INSTRUCTOR,
                            (activeFilterItems.containsKey(workout.getInstructorKey())),
                            connectedAccount,
                            workout.getInstructor()
                    )
            );
        }

        Collections.sort(filterItems, new Comparator<ScheduleFilterItem>() {
            @Override
            public int compare(ScheduleFilterItem o1, ScheduleFilterItem o2) {
                return o1.getExtra().compareTo(o2.getExtra());
            }
        });
        return filterItems;
    }
}
