package com.ninetwozero.iksu.features.schedule.listing;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ninetwozero.iksu.R;
import com.ninetwozero.iksu.app.IksuApp;
import com.ninetwozero.iksu.common.ui.BaseFragment;
import com.ninetwozero.iksu.common.ui.ViewPagerAdapter;
import com.ninetwozero.iksu.features.schedule.filter.ScheduleFilterActivity;
import com.ninetwozero.iksu.network.IksuWorkoutService;
import com.ninetwozero.iksu.utils.DateUtils;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;

public class WeeklyScheduleFragment extends BaseFragment {
    public static final String USE_FILTER_SETTINGS = "useFilterSettings";
    private static final int REQUEST_CODE_FILTER = 4001;

    @BindView(R.id.toolbar)
    protected Toolbar toolbar;
    @BindView(R.id.tabs)
    protected TabLayout tabLayout;
    @BindView(R.id.container)
    protected ViewPager viewPager;

    private ViewPagerAdapter<DailyScheduleFragment> pagerAdapter;

    private boolean onlyRelevantToLogin;
    private boolean useFilterSettings;

    public static WeeklyScheduleFragment newInstance(boolean onlyRelevantToLogin, boolean useFilterSettings) {
        final Bundle arguments = new Bundle();
        arguments.putBoolean(IksuWorkoutService.ONLY_RELEVANT_TO_LOGIN, onlyRelevantToLogin);
        arguments.putBoolean(USE_FILTER_SETTINGS, useFilterSettings);

        final WeeklyScheduleFragment fragment = new WeeklyScheduleFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        setRetainInstance(false);

        bootstrap();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_general_tab, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViewPager();
    }

    @Override
    public void onResume() {
        super.onResume();

        final long fiveMinutesAgo = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5);
        final String latestRefreshKey = IksuApp.getLatestRefreshKey();
        if (sharedPreferences.getLong(latestRefreshKey, 0) < fiveMinutesAgo) {
            getActivity().startService(IksuWorkoutService.newIntent(getActivity(), IksuWorkoutService.ACTION, onlyRelevantToLogin));
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupToolbar(toolbar, getString(getToolbarTitleResource()), null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_weekly_schedule, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_filter_toggle).setChecked(useFilterSettings);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_filter_setup) {
            startActivityForResult(new Intent(getContext(), ScheduleFilterActivity.class), REQUEST_CODE_FILTER);
            return true;
        } else if (item.getItemId() == R.id.menu_filter_toggle) {
            final boolean newState = !item.isChecked();
            item.setChecked(newState);
            onFilterSettingsChanged(newState);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_FILTER) {
            onFilterSettingsChanged(useFilterSettings);
        }
    }

    protected void bootstrap() {
        final Bundle arguments = getArguments();
        if (arguments != null) {
            onlyRelevantToLogin = arguments.getBoolean(IksuWorkoutService.ONLY_RELEVANT_TO_LOGIN, false);
            useFilterSettings = arguments.getBoolean(WeeklyScheduleFragment.USE_FILTER_SETTINGS, true);
        }
        pagerAdapter = new ViewPagerAdapter<>(getChildFragmentManager());
    }

    private int getToolbarTitleResource() {
        return R.string.label_workouts;
    }

    private void setupViewPager() {
        tabLayout.setupWithViewPager(viewPager);
        for (int i = 0; i < IksuWorkoutService.DAYS_TO_SHOW; i++) {
            pagerAdapter.addFragment(
                DailyScheduleFragment.newInstance(
                    DateUtils.getDate(i),
                    onlyRelevantToLogin,
                    useFilterSettings
                ),
                DateUtils.getWeekday(getContext(), i)
            );
        }
        viewPager.setAdapter(pagerAdapter);
    }

    private void onFilterSettingsChanged(boolean newState) {
        useFilterSettings = newState;
        sharedPreferences.edit().putBoolean(IksuApp.getFiltersActiveKey(), newState).apply();
        reconfigureAllLists(newState);
    }

    private void reconfigureAllLists(boolean newState) {
        for (int i = 0, max = pagerAdapter.getCount(); i < max; i++) {
            if (pagerAdapter.getItem(i).isAdded()) {
                pagerAdapter.getItem(i).reconfigureListDataSource(newState);
            }
        }
    }
}
