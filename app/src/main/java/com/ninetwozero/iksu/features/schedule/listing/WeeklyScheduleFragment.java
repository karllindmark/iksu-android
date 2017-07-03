package com.ninetwozero.iksu.features.schedule.listing;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ninetwozero.iksu.R;
import com.ninetwozero.iksu.app.IksuApp;
import com.ninetwozero.iksu.common.ui.BaseFragment;
import com.ninetwozero.iksu.common.ui.ViewPagerAdapter;
import com.ninetwozero.iksu.network.IksuWorkoutService;
import com.ninetwozero.iksu.utils.DateUtils;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;

public class WeeklyScheduleFragment extends BaseFragment {
    @BindView(R.id.toolbar)
    protected Toolbar toolbar;
    @BindView(R.id.tabs)
    protected TabLayout tabLayout;
    @BindView(R.id.container)
    protected ViewPager viewPager;

    private ViewPagerAdapter<DailyScheduleFragment> pagerAdapter;

    private boolean onlyRelevantToLogin;
    private boolean useFilterSettings;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

    private void bootstrap() {
        onlyRelevantToLogin = getArguments().getBoolean(IksuWorkoutService.ONLY_RELEVANT_TO_LOGIN, false);
        useFilterSettings = getArguments().getBoolean(DailyScheduleFragment.USE_FILTER_SETTINGS, false);
        pagerAdapter = new ViewPagerAdapter<>(getChildFragmentManager());
    }

    private int getToolbarTitleResource() {
        return R.string.label_workouts;
    }

    private void setupViewPager() {
        tabLayout.setupWithViewPager(viewPager);
        for (int i = 0; i < IksuWorkoutService.DAYS_TO_SHOW; i++) {
            pagerAdapter.addFragment(
                DailyScheduleFragment.newInstance(DateUtils.getDate(i), onlyRelevantToLogin, true),
                DateUtils.getWeekday(getContext(), i)
            );
        }
        viewPager.setAdapter(pagerAdapter);
    }

    public void reconfigureAllLists() {
        for (int i = 0, max = pagerAdapter.getCount(); i < max; i++) {
            if (pagerAdapter.getItem(i).isAdded()) {
                pagerAdapter.getItem(i).reconfigureListDataSource();
            }
        }
    }
}
