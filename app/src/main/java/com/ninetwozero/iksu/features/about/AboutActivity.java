package com.ninetwozero.iksu.features.about;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import com.ninetwozero.iksu.R;
import com.ninetwozero.iksu.common.ui.BaseFragment;
import com.ninetwozero.iksu.common.ui.BaseSecondaryActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;


public class AboutActivity extends BaseSecondaryActivity {
    public static final int REQUEST = 4001;

    @BindView(R.id.tabs)
    protected TabLayout tabLayout;
    @BindView(R.id.container)
    protected ViewPager viewPager;

    private ViewPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupViewPager();
        setupToolbar(R.string.label_about);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_about;
    }

    private void setupViewPager() {
        pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(AboutAppFragment.newInstance(), getString(R.string.label_about_app));
        pagerAdapter.addFragment(AboutLinksFragment.newInstance(), getString(R.string.label_about_links));
        pagerAdapter.addFragment(AboutDependenciesFragment.newInstance(), getString(R.string.label_about_dependencies));
        viewPager.setAdapter(pagerAdapter);

        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setupWithViewPager(viewPager);
    }

    class ViewPagerAdapter extends FragmentStatePagerAdapter {

        private final List<BaseFragment> fragments = new ArrayList<>();
        private final List<String> fragmentTitles = new ArrayList<>();

        ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public BaseFragment getItem(int position) {
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

        void addFragment(final BaseFragment fragment, final String title) {
            fragments.add(fragment);
            fragmentTitles.add(title);
        }
    }
}
