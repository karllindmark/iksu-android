package com.ninetwozero.iksu.common.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.ninetwozero.iksu.R;
import com.ninetwozero.iksu.app.IksuApp;
import com.ninetwozero.iksu.network.IksuLoginService;
import com.ninetwozero.iksu.network.LoginHelper;

import butterknife.ButterKnife;
import io.realm.Realm;

abstract public class BaseActivity extends AppCompatActivity {
    private final LoginStatusReceiver loginStatusReceiver = new LoginStatusReceiver();

    protected Realm realm;
    protected SharedPreferences sharedPreferences;
    protected LoginCallback loginCallback;

    protected static final int STATE_NORMAL = 0;
    protected static final int STATE_EMPTY = 1;
    protected static final int STATE_LOADING = 2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getLayoutId());
        ButterKnife.bind(this);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        realm = Realm.getDefaultInstance();
        LocalBroadcastManager.getInstance(this).registerReceiver(loginStatusReceiver, new IntentFilter(IksuLoginService.ACTION_LOGIN));

        if (!IksuApp.hasNetworkConnection()) {
            showNoInternetConnectionInformation();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(loginStatusReceiver);
        realm.close();
    }

    protected void showNoInternetConnectionInformation() {
        Snackbar.make(findViewById(android.R.id.content), R.string.msg_no_internet, Snackbar.LENGTH_SHORT)
                .setActionTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccentLight))
                .setAction(R.string.label_open_settings, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                })
                .show();
    }

    private class LoginStatusReceiver extends BroadcastReceiver {
        private LoginStatusReceiver() {}

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(IksuLoginService.ACTION_LOGIN)) {
                final boolean loggedIn = intent.getIntExtra(IksuLoginService.STATUS, -1) == LoginHelper.RESULT_OK;
                if (loggedIn) {
                    Toast.makeText(getApplicationContext(), getString(R.string.msg_signed_in_as_x, IksuApp.getActiveAccount().getUsername()), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.msg_sign_in_failed, Toast.LENGTH_SHORT).show();
                }

                if (loginCallback != null) {
                    loginCallback.onLoginStateChanged(loggedIn);
                }
            }

        }
    }

    protected void setUiState(int state) {
        // NO-OP
    }

    public interface LoginCallback {
        void onLoginStateChanged(boolean loggedIn);
    }

    protected abstract @LayoutRes int getLayoutId();
}
