package com.ninetwozero.iksu.utils;

import com.ninetwozero.iksu.R;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class ApiHelper {
    public static String buildGravatarUrl(final String email, final int size) {
        return "https://www.gravatar.com/avatar/" + generateMd5(email) + "?s=" + size  + "&d=404";
    }

    public static RequestBody createRequestBody(byte[] bytes) {
        return RequestBody.create(MediaType.parse("application/json; charset=utf-8"), bytes);
    }

    public static int getStringResourceForErrorType(final String key) {
        switch (key) {
            case "UserCardLocked":
                return R.string.msg_error_card_locked;
            case "UserNotLoggedIn":
                return R.string.msg_error_not_logged_in;
            case "ClassCantCheckIn":
                return R.string.msg_error_checkin_failed;
            case "ClassAlreadyCheckedIn":
                return R.string.msg_error_already_checkedin;
            case "SystemsOverBurdened":
            case "NetworkError":
                return R.string.msg_error_iksu_backend;
            default:
                return R.string.msg_error_general;

// Known errors codes:
// - UserNotLoggedIn (Invalid session)
// -  (Too early to check-in?)
// -  (<--)
// - SystemsOverBurdened (Vaj-Sing on the line)
// - NetworkError (---^^---)
        }
    }

    // Based on https://stackoverflow.com/a/5470279/860212
    private static String generateMd5(String email) {
        try {
            return String.format("%032x", new BigInteger(1, MessageDigest.getInstance("MD5").digest(email.getBytes())));
        } catch (NoSuchAlgorithmException e) {
            return "nohash";
        }
    }

}
