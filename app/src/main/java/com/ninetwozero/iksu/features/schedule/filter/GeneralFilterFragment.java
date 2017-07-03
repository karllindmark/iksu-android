package com.ninetwozero.iksu.features.schedule.filter;

import android.os.Bundle;

import com.ninetwozero.iksu.app.IksuApp;
import com.ninetwozero.iksu.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GeneralFilterFragment extends BaseFilterFragment {
    public static GeneralFilterFragment newInstance() {
        final Bundle args = new Bundle();
        final GeneralFilterFragment fragment = new GeneralFilterFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected List<ScheduleFilterItem> getFilterItems() {
        final String connectedAccount = IksuApp.getActiveUsername();
        final Map<String, ScheduleFilterItem> activeFilterItems = getActiveFilterItems(connectedAccount, ScheduleFilterItem.ROW_FILTER_LOCATION);

        final List<ScheduleFilterItem> items = new ArrayList<>();
        items.add(new ScheduleFilterItem(Constants.LOCATION_IKSU_SPORT, ScheduleFilterItem.ROW_FILTER_LOCATION, activeFilterItems.containsKey(Constants.LOCATION_IKSU_SPORT), connectedAccount, "100"));
        items.add(new ScheduleFilterItem(Constants.LOCATION_IKSU_SPA, ScheduleFilterItem.ROW_FILTER_LOCATION, activeFilterItems.containsKey(Constants.LOCATION_IKSU_SPA), connectedAccount, "200"));
        items.add(new ScheduleFilterItem(Constants.LOCATION_IKSU_PLUS, ScheduleFilterItem.ROW_FILTER_LOCATION, activeFilterItems.containsKey(Constants.LOCATION_IKSU_PLUS), connectedAccount, "300"));
        return items;
    }
}
