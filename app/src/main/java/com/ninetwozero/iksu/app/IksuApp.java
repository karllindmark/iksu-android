package com.ninetwozero.iksu.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.ninetwozero.iksu.R;
import com.ninetwozero.iksu.models.UserAccount;
import com.ninetwozero.iksu.network.IksuApi;
import com.ninetwozero.iksu.network.WorkoutMoshiAdapter;
import com.ninetwozero.iksu.network.interceptors.ApiTokenInterceptor;
import com.ninetwozero.iksu.network.interceptors.LoginSessionInterceptor;
import com.ninetwozero.iksu.utils.Constants;
import com.squareup.moshi.Moshi;

import java.util.UUID;

import io.realm.Realm;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

import static com.ninetwozero.iksu.utils.Constants.DEVELOPER_MODE;
import static com.ninetwozero.iksu.utils.Constants.KEY_UUID;

public class IksuApp extends Application {
    private static IksuApi api;
    private static Moshi moshi;

    private static Context applicationContext;
    private static SharedPreferences sharedPreferences;
    private static UserAccount activeAccount;

    private static boolean developerMode;

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        AndroidThreeTen.init(this);

        applicationContext = this;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(IksuApp.getContext());
        developerMode = sharedPreferences.getBoolean(Constants.DEVELOPER_MODE, false);

        final String currentUsername = sharedPreferences.getString(Constants.CURRENT_USER, "");
        if (!TextUtils.isEmpty(currentUsername)) {
            final UserAccount userAccount = Realm.getDefaultInstance().where(UserAccount.class).equalTo(Constants.USERNAME, currentUsername).findFirst();
            if (userAccount == null) {
                sharedPreferences.edit().remove(Constants.CURRENT_USER).apply();
            } else {
                IksuApp.activeAccount = Realm.getDefaultInstance().copyFromRealm(userAccount);
                if (IksuApp.activeAccount != null && IksuApp.activeAccount.isDisabled()) {
                    IksuApp.activeAccount = null;
                    Toast.makeText(getContext(), getString(R.string.msg_account_disabled, currentUsername), Toast.LENGTH_LONG).show();
                    sharedPreferences.edit().remove(Constants.CURRENT_USER).apply();
                }
            }

        }
        setupUuid();
    }

    public static IksuApi getApi() {
        if (api == null) {
            api = new Retrofit.Builder()
                .baseUrl(IksuApi.HOST)
                .client(createOkHttpClient())
                .addConverterFactory(MoshiConverterFactory.create(getMoshi()))
                .build()
                .create(IksuApi.class);
        }
        return api;
    }

    // Courtesy of https://stackoverflow.com/a/4239019
    public static boolean hasNetworkConnection() {
        final ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private static OkHttpClient createOkHttpClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(new ApiTokenInterceptor())
                .addInterceptor(new LoginSessionInterceptor())
                .build();
    }

    private void setupUuid() {
        if (sharedPreferences.contains(KEY_UUID)) {
            FirebaseAnalytics.getInstance(this).setUserProperty(KEY_UUID, sharedPreferences.getString(KEY_UUID, null));
        } else {
            final String uuid = UUID.randomUUID().toString();
            sharedPreferences.edit().putString(KEY_UUID, uuid).apply();
            FirebaseAnalytics.getInstance(this).setUserProperty(KEY_UUID, uuid);
        }
    }

    private static Moshi createMoshiInstance() {
        return new Moshi.Builder().add(new WorkoutMoshiAdapter()).build();
    }

    public static Moshi getMoshi() {
        if (moshi == null) {
            moshi = createMoshiInstance();
        }
        return moshi;
    }

    public static Context getContext() {
        return applicationContext;
    }

    public static SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public static void setActiveUser(UserAccount account) {
        IksuApp.activeAccount = account;
    }

    public static UserAccount getActiveAccount() {
        return IksuApp.activeAccount;
    }

    public static boolean hasSelectedAccount() {
        return activeAccount != null && (!TextUtils.isEmpty(sharedPreferences.getString(Constants.CURRENT_USER, "")));
    }

    public static String getActiveUsername() {
        return activeAccount == null ? null : activeAccount.getUsername();
    }

    public static String getLatestRefreshKey() {
        return Constants.LATEST_AUTO_REFRESH + (IksuApp.hasSelectedAccount() ? "_" + IksuApp.getActiveUsername() : "");
    }

    public static boolean hasEnabledDeveloperMode() {
        return developerMode;
    }

    public static void activateDeveloperMode() {
        developerMode = true;
        sharedPreferences.edit().putBoolean(DEVELOPER_MODE, true).apply();
    }
}
