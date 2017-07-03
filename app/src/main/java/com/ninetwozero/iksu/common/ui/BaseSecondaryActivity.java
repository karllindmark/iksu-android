package com.ninetwozero.iksu.common.ui;

import android.support.v7.widget.Toolbar;

import com.ninetwozero.iksu.R;

import butterknife.BindView;

abstract public class BaseSecondaryActivity extends BaseActivity {
    @BindView(R.id.toolbar)
    protected Toolbar toolbar;

    public void setupToolbar(final int title) {
        toolbar.setTitle(title);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }
}
