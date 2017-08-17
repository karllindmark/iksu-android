package com.ninetwozero.iksu.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.firebase.crash.FirebaseCrash;
import com.ninetwozero.iksu.app.IksuApp;
import com.ninetwozero.iksu.features.accounts.LoginUtil;
import com.ninetwozero.iksu.models.ApiSession;
import com.ninetwozero.iksu.models.UserAccount;
import com.ninetwozero.iksu.network.dto.LoginResponse;
import com.ninetwozero.iksu.utils.Constants;

import java.io.IOException;

import io.realm.Realm;
import retrofit2.Response;

public class LoginHelper {
    public static final int RESULT_OK = 0;
    public static final int RESULT_FAIL = 1;
    public static final int RESULT_ERROR = 2;
    public static final int RESULT_FAIL_CREDENTIALS = 3;
    public static final int RESULT_FAIL_BLOCKED = 4;

    private final Context context;

    public LoginHelper(Context context) {
        this.context = context;
    }

    public int doLogin(final String username, final String encryptedPassword) {
        int out = RESULT_FAIL;

        final LoginUtil loginUtil = new LoginUtil(context, PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_UUID, ""));
        try {
            final Response<LoginResponse> response = IksuApp.getApi().login(username, loginUtil.decryptPassword(encryptedPassword)).execute();
            if (response.isSuccessful()) {
                final LoginResponse loginResponseObj = response.body();
                if (loginResponseObj != null) {
                    final ApiSession session = new ApiSession(loginResponseObj.getSessionId());
                    session.setValidFrom(System.currentTimeMillis());

                    final UserAccount userAccount = new UserAccount();
                    userAccount.setName(loginResponseObj.getName());
                    userAccount.setUsername(username);
                    userAccount.setPassword(encryptedPassword);
                    userAccount.setSession(session);
                    userAccount.setSelected(true);

                    final SharedPreferences.Editor editor = IksuApp.getSharedPreferences().edit();
                    editor.putString(Constants.CURRENT_USER, username);
                    editor.apply();

                    final Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();

                    final UserAccount previousSelectedAccount = realm.where(UserAccount.class).equalTo("selected", true).findFirst();
                    if (previousSelectedAccount != null) {
                        previousSelectedAccount.setSelected(false);
                        realm.insertOrUpdate(previousSelectedAccount);
                    }

                    realm.copyToRealmOrUpdate(userAccount);
                    realm.commitTransaction();

                    IksuApp.setActiveUser(userAccount);
                    realm.close();
                    out = RESULT_OK;
                }
            } else if (response.code() == 412) {
                ApiErrorResponse errorResponse = IksuApp.getMoshi().adapter(ApiErrorResponse.class).fromJson(response.errorBody().string());
                if (errorResponse != null) {
                    switch (errorResponse.getMessage()) {
                        case "1024":
                            out = RESULT_FAIL_CREDENTIALS;
                            break;
                        case "2048":
                            out = RESULT_FAIL_BLOCKED;
                            break;
                        default:
                            out = RESULT_FAIL;
                            break;
                    }
                }
                IksuApp.setActiveUser(null);
            }
        } catch (IOException e) {
            FirebaseCrash.report(e);
            out = RESULT_ERROR;
        }
        return out;
    }
}
