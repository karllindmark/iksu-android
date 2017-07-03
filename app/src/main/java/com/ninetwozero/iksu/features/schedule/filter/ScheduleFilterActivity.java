package com.ninetwozero.iksu.features.schedule.filter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import com.ninetwozero.iksu.R;
import com.ninetwozero.iksu.common.ui.BaseSecondaryActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;


public class ScheduleFilterActivity extends BaseSecondaryActivity {
    @BindView(R.id.tabs)
    protected TabLayout tabLayout;
    @BindView(R.id.container)
    protected ViewPager viewPager;

    private ViewPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupViewPager();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_schedule_filter;
    }

    private void setupViewPager() {
        pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        tabLayout.setupWithViewPager(viewPager);
        pagerAdapter.addFragment(GeneralFilterFragment.newInstance(), getString(R.string.label_facilities));
        pagerAdapter.addFragment(CategoryFilterFragment.newInstance(), getString(R.string.label_categories));
        pagerAdapter.addFragment(InstructorFilterFragment.newInstance(), getString(R.string.label_instructors));
        viewPager.setAdapter(pagerAdapter);
    }

    class ViewPagerAdapter extends FragmentStatePagerAdapter {

        private final List<BaseFilterFragment> fragments = new ArrayList<>();
        private final List<String> fragmentTitles = new ArrayList<>();

        ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public BaseFilterFragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitles.get(position);
        }

        void addFragment(final BaseFilterFragment fragment, final String title) {
            fragments.add(fragment);
            fragmentTitles.add(title);
        }
    }
}
