package com.ninetwozero.iksu.features.schedule.reservation;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ninetwozero.iksu.R;
import com.ninetwozero.iksu.common.ui.BaseFragment;
import com.ninetwozero.iksu.common.ui.ViewPagerAdapter;

import butterknife.BindView;

public class ReservationTabFragment extends BaseFragment {
    @BindView(R.id.toolbar)
    protected Toolbar toolbar;
    @BindView(R.id.tabs)
    protected TabLayout tabLayout;
    @BindView(R.id.container)
    protected ViewPager viewPager;

    private ViewPagerAdapter<ReservationListFragment> pagerAdapter;

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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupToolbar(toolbar, getString(R.string.label_reservations), null);
    }

    private void bootstrap() {
        pagerAdapter = new ViewPagerAdapter<>(getChildFragmentManager());
    }

    private void setupViewPager() {
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);

        pagerAdapter.addFragment(ReservationListFragment.newInstance(false), getString(R.string.label_upcoming));
        pagerAdapter.addFragment(ReservationListFragment.newInstance(true), getString(R.string.label_past));

        viewPager.setAdapter(pagerAdapter);
    }


}
