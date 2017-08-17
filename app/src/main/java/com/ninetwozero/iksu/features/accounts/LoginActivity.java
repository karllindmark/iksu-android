package com.ninetwozero.iksu.features.accounts;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.ninetwozero.iksu.BuildConfig;
import com.ninetwozero.iksu.R;
import com.ninetwozero.iksu.app.IksuApp;
import com.ninetwozero.iksu.common.ui.BaseActivity;
import com.ninetwozero.iksu.common.ui.BaseSecondaryActivity;
import com.ninetwozero.iksu.network.IksuLoginService;
import com.ninetwozero.iksu.utils.Constants;

import butterknife.BindView;

import static com.ninetwozero.iksu.network.IksuLoginService.ACTION_LOGIN;
import static com.ninetwozero.iksu.network.LoginHelper.RESULT_ERROR;
import static com.ninetwozero.iksu.network.LoginHelper.RESULT_FAIL;
import static com.ninetwozero.iksu.network.LoginHelper.RESULT_FAIL_BLOCKED;
import static com.ninetwozero.iksu.network.LoginHelper.RESULT_FAIL_CREDENTIALS;

public class LoginActivity extends BaseSecondaryActivity implements BaseActivity.LoginCallback {
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String STATUS = "login_status";

    private static final String STATE_FORM = "login_state_form";

    @BindView(R.id.username)
    protected EditText usernameField;
    @BindView(R.id.password)
    protected EditText passwordField;
    @BindView(R.id.button_login)
    protected TextView signInButton;
    @BindView(R.id.checkbox_accept_tos)
    protected CheckBox acceptCheckbox;
    @BindView(R.id.tos_text)
    protected TextView tosText;
    @BindView(R.id.state_loading_view)
    protected View progressView;
    @BindView(R.id.progress_text)
    protected TextView progressTextView;

    private LoginProgressReceiver loginProgressReceiver = new LoginProgressReceiver();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bootstrap();
        setupForm();

        setUiState(STATE_FORM);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    public void onLoginStateChanged(final boolean loggedIn, final int status) {
        if (!loggedIn) {
            passwordField.requestFocus();
            Snackbar.make(findViewById(android.R.id.content), getErrorMessageForLoginStatus(status), Snackbar.LENGTH_SHORT).show();
            setUiState(STATE_FORM);
            return;
        }
        setUiState(IksuLoginService.STATE_LOGIN_DONE);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            extras = new Bundle();
        }
        extras.putBoolean(STATUS, true);
        setResult(Activity.RESULT_OK, new Intent().putExtras(extras));
        finish();
    }

    private int getErrorMessageForLoginStatus(int status) {
        switch (status) {
            case RESULT_FAIL_CREDENTIALS:
                return R.string.msg_login_error;
            case RESULT_FAIL_BLOCKED:
                return R.string.msg_login_error_blocked;
            case RESULT_FAIL:
            case RESULT_ERROR:
            default:
                return R.string.msg_sign_in_failed;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(loginProgressReceiver, new IntentFilter(IksuLoginService.ACTION_LOGIN_PROGRESS));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(loginProgressReceiver);
    }

    private void bootstrap() {
        loginCallback = this;
        configureKeystoreIfNeeded();
    }

    private void configureKeystoreIfNeeded() {
        if (!sharedPreferences.getBoolean(Constants.HAS_CONFIGURED_KEYSTORE, false)) {
            try {
                new LoginUtil(getApplicationContext(), sharedPreferences.getString(Constants.KEY_UUID, null)).initKeystore();
                sharedPreferences.edit().putBoolean(Constants.HAS_CONFIGURED_KEYSTORE, true).apply();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                new MaterialDialog.Builder(getApplicationContext())
                    .title(R.string.label_unsupported_device)
                    .content(R.string.msg_unsupported_device_info)
                    .negativeText(R.string.label_no)
                    .positiveText(R.string.label_send_information)
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            if (which == DialogAction.POSITIVE) {
                                FirebaseAnalytics.getInstance(getApplicationContext()).logEvent("UNSUPPORTED_DEVICE", new Bundle());
                            }
                        }
                    })
                    .build()
                    .show();
            }
        }
    }

    private void setupForm() {
        acceptCheckbox.setChecked(sharedPreferences.getBoolean(Constants.HAS_ACCEPTED_TOS, false));
        signInButton.setEnabled(acceptCheckbox.isChecked());
        acceptCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharedPreferences.edit().putBoolean(Constants.HAS_ACCEPTED_TOS, isChecked).apply();
                signInButton.setEnabled(isChecked);
                FirebaseAnalytics.getInstance(LoginActivity.this).logEvent(isChecked ? "ACCEPTED_TOS" : "DECLINED_TOS", new Bundle());
            }
        });
        tosText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (acceptCheckbox.isChecked()) {
                    acceptCheckbox.setChecked(false);
                    signInButton.setEnabled(false);
                } else {
                    openTermsOfServiceDialog();
                }
            }
        });
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sharedPreferences.getBoolean(Constants.HAS_ACCEPTED_TOS, false)) {
                    onFormSubmitted();
                } else {
                    Snackbar.make(findViewById(android.R.id.content), R.string.msg_error_accept_tos_first, Snackbar.LENGTH_LONG).show();
                }
            }
        });

        final Intent intent = getIntent();
        if (intent.hasExtra(USERNAME)) {
            usernameField.setText(intent.getStringExtra(USERNAME));
        } else {
            usernameField.setText(BuildConfig.ACCOUNT_EMAIL);
        }

        if (intent.hasExtra(PASSWORD)) {
            passwordField.setText(intent.getStringExtra(PASSWORD));
        } else {
            passwordField.setText(BuildConfig.ACCOUNT_PASSWORD);
        }
    }

    private void openTermsOfServiceDialog() {
        new MaterialDialog.Builder(this)
            .title(R.string.label_tos)
            .content(R.string.msg_tos)
            .negativeText(R.string.label_cancel)
            .positiveText(R.string.label_accept)
            .onAny(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    acceptCheckbox.setChecked(which == DialogAction.POSITIVE);
                }
            })
            .build()
            .show();
    }

    private void onFormSubmitted() {
        View topMostFaultyView = null;
        boolean hasErrors = false;

        final String password = passwordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            topMostFaultyView = passwordField;
            passwordField.setError(getString(R.string.msg_error_invalid_password));
            hasErrors = true;
        }

        final String username = usernameField.getText().toString();
        if (TextUtils.isEmpty(username)) {
            usernameField.setError(getString(R.string.msg_error_empty_username));
            topMostFaultyView = usernameField;
            hasErrors = true;
        } else if (!username.contains("@")) {
            usernameField.setError(getString(R.string.msg_error_invalid_username));
            topMostFaultyView = usernameField;
            hasErrors = true;
        }

        if (hasErrors) {
            Snackbar.make(findViewById(android.R.id.content), R.string.msg_errors_found, Snackbar.LENGTH_SHORT).show();
            topMostFaultyView.requestFocus();
            return;
        }

        usernameField.setError(null);
        passwordField.setError(null);

        if (!IksuApp.hasNetworkConnection()) {
            showNoInternetConnectionInformation();
            return;
        }

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            extras = new Bundle();
        }

        extras.putString(IksuLoginService.USERNAME, username);
        extras.putString(
            IksuLoginService.PASSWORD,
            new LoginUtil(getApplicationContext(), sharedPreferences.getString(Constants.KEY_UUID, null))
                .encryptPassword(password)
        );

        startService(IksuLoginService.newIntent(this, ACTION_LOGIN, extras));
    }

    private void setUiState(String state) {
        switch (state) {
            case IksuLoginService.STATE_LOGIN_STARTED:
                progressView.setVisibility(View.VISIBLE);
                progressTextView.setText(R.string.msg_login_started);
                break;
            case IksuLoginService.STATE_LOGIN_OK_FETCHING_WORKOUTS:
                progressView.setVisibility(View.VISIBLE);
                progressTextView.setText(R.string.msg_login_ok_preparing);
                break;
            case IksuLoginService.STATE_LOGIN_DONE:
                progressView.setVisibility(View.VISIBLE);
                progressTextView.setText(R.string.msg_login_done);
                break;
            case STATE_FORM:
            default:
                progressView.setVisibility(View.GONE);
                break;
        }
    }

    private class LoginProgressReceiver extends BroadcastReceiver {
        private LoginProgressReceiver() {}

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(IksuLoginService.ACTION_LOGIN_PROGRESS)){
                if (intent.hasExtra(IksuLoginService.PROGRESS)) {
                    setUiState(intent.getStringExtra(IksuLoginService.PROGRESS));
                }
            }
        }
    }
}
