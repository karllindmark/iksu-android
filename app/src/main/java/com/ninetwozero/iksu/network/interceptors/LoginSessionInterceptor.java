package com.ninetwozero.iksu.network.interceptors;

import com.ninetwozero.iksu.app.IksuApp;
import com.ninetwozero.iksu.models.UserAccount;
import com.ninetwozero.iksu.network.ApiErrorResponse;
import com.ninetwozero.iksu.network.LoginHelper;
import com.ninetwozero.iksu.utils.ApiHelper;
import com.ninetwozero.iksu.utils.Constants;
import com.squareup.moshi.JsonDataException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.realm.Realm;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

public class LoginSessionInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (IksuApp.hasSelectedAccount() && !request.url().encodedPath().endsWith("/login")) {
            final Response response = chain.proceed(request);
            boolean responseIsErrorObject = false;
            ApiErrorResponse errorObject = null;
            try {
                responseIsErrorObject = response.peekBody(20).string().matches("\\{\"(Message|message)\":\"([^\"]+)\".*");
                if (responseIsErrorObject) {
                    errorObject = IksuApp.getMoshi().adapter(ApiErrorResponse.class).fromJson(
                        response.peekBody(Integer.parseInt(response.header("Content-Length"))).source()
                    );
                }
            } catch (JsonDataException ignored) {}

            // TODO: Check for UserCardLocked
            if (responseIsErrorObject) {
                final Realm realm = Realm.getDefaultInstance();
                if ("UserNotLoggedIn".equals(errorObject.getKey())) {
                    UserAccount account = realm.where(UserAccount.class).equalTo(Constants.USERNAME, IksuApp.getActiveUsername()).findFirst();
                    if (account != null) {
                        final String oldSessionId = account.getSessionId();
                        int status = new LoginHelper(IksuApp.getContext()).doLogin(account.getUsername(), account.getPassword());
                        if (status == LoginHelper.RESULT_OK) {
                            switch (request.method()) {
                                case "PUT": // No known PUTs at this time though
                                case "POST":
                                    request = handlePostInterception(request, IksuApp.getActiveAccount().getSessionId());
                                    break;
                                case "DELETE":
                                case "GET":
                                default:
                                    request = handleInterceptionWhenDataInUrl(request, oldSessionId, IksuApp.getActiveAccount().getSessionId());
                                    break;
                            }
                        } else {
                            realm.beginTransaction();
                            realm.where(UserAccount.class).equalTo(Constants.CONNECTED_ACCOUNT, IksuApp.getActiveUsername()).findFirst().getSession().deleteFromRealm();
                            account.setDisabled(true);
                            realm.commitTransaction();
                        }
                    }
                }
                realm.close();
                return chain.proceed(request);
            }
            return response;
        }
        return chain.proceed(request);
    }

    private Request handleInterceptionWhenDataInUrl(Request request, String oldSessionId, String newSessionId) {
        final String url = request.url().toString().replace("sessionId=" + oldSessionId, "sessionId=" + newSessionId);
        return request.newBuilder().url(url).build();
    }

    private Request handlePostInterception(Request request, String newSessionId) {
        final String currentPayload = convertRequestBodyToString(request.body());
        String newPayload;
        try {
            final JSONObject temp = new JSONObject(currentPayload);
            if (temp.has("SessionId")) {
                temp.put("SessionId", newSessionId);
            }
            newPayload = temp.toString();
        } catch (JSONException e) {
            newPayload = currentPayload;
        }
        return request.newBuilder().post(ApiHelper.createRequestBody(newPayload.getBytes())).build();
    }

    // Inspired by https://stackoverflow.com/a/34791600/860212
    private String convertRequestBodyToString(final RequestBody requestBody) {
        try (Buffer buffer = new Buffer()){
            requestBody.writeTo(buffer);
            return buffer.readUtf8();
        } catch (IOException ignored) {}
        return "";
    }
}
