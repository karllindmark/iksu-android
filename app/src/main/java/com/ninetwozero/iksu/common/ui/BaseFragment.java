package com.ninetwozero.iksu.common.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.ninetwozero.iksu.R;

import butterknife.ButterKnife;
import io.realm.Realm;

public class BaseFragment extends Fragment {
    protected Realm realm;
    protected SharedPreferences sharedPreferences;

    protected static final int STATE_NORMAL = 0;
    protected static final int STATE_EMPTY = 1;
    protected static final int STATE_LOADING = 2;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        realm = Realm.getDefaultInstance();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    protected void setupToolbar(final Toolbar toolbar, final String title, final String subtitle) {
        setupToolbar(toolbar, title, subtitle, R.drawable.ic_menu_black_24dp);
    }

    protected void setupToolbar(final Toolbar toolbar, final String title, final String subtitle, @DrawableRes final int drawerIcon) {
        toolbar.setTitle(title);
        toolbar.setSubtitle(subtitle);

        final BaseActivity activity = (BaseActivity) getActivity();
        activity.setSupportActionBar(toolbar);

        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setHomeAsUpIndicator(drawerIcon);
            activity.getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    protected void setUiState(int state) {
        // NO-OP
    }
}
