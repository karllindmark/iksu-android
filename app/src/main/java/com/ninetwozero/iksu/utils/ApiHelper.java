package com.ninetwozero.iksu.utils;

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

    // Based on https://stackoverflow.com/a/5470279/860212
    private static String generateMd5(String email) {
        try {
            return String.format("%032x", new BigInteger(1, MessageDigest.getInstance("MD5").digest(email.getBytes())));
        } catch (NoSuchAlgorithmException e) {
            return "nohash";
        }
    }

}
