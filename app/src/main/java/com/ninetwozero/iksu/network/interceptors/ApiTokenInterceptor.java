package com.ninetwozero.iksu.network.interceptors;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.ninetwozero.iksu.BuildConfig;
import com.ninetwozero.iksu.app.IksuApp;
import com.ninetwozero.iksu.network.dto.TokenResponse;
import com.ninetwozero.iksu.utils.Constants;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class ApiTokenInterceptor implements Interceptor {
    private static final String HEADER_AUTH = "Authorization";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (!request.url().toString().endsWith("/token")) {
            if (TextUtils.isEmpty(request.header(HEADER_AUTH))) {
                final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(IksuApp.getContext());

                String accessToken = sharedPreferences.getString(Constants.API_TOKEN, "");
                if (TextUtils.isEmpty(accessToken) || (System.currentTimeMillis() > sharedPreferences.getLong(Constants.API_TOKEN_EXPIRATION, Long.MAX_VALUE))) {
                    final TokenResponse tokenResponse = generateNewAccessTokenSynchronously();
                    if (tokenResponse == null || TextUtils.isEmpty(tokenResponse.getAccessToken())) {
                        return new Response.Builder().code(400).request(request).build();
                    }

                    final SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(Constants.API_TOKEN, tokenResponse.getAccessToken());
                    editor.putLong(Constants.API_TOKEN_EXPIRATION, System.currentTimeMillis() + (tokenResponse.getExpiresIn() * 1000));
                    editor.apply();

                    accessToken = tokenResponse.getAccessToken();
                }
                request = request.newBuilder().header(HEADER_AUTH, "Bearer " + accessToken).build();
            }
        }
        return chain.proceed(request);
    }

    private static TokenResponse generateNewAccessTokenSynchronously() throws IOException {
        return IksuApp.getApi().getAccessToken(BuildConfig.API_USER, BuildConfig.API_PASSWORD, BuildConfig.API_GRANT).execute().body();
    }
}
