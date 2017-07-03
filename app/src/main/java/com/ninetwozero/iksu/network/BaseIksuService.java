package com.ninetwozero.iksu.network;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.analytics.FirebaseAnalytics;

abstract public class BaseIksuService extends IntentService {
    private static final String ACTION_PREFIX = "com.ninetwozero.iksu.action.";
    public static final String STATUS = "status";
    public static final String LOADING = "loading";

    public BaseIksuService(String name) {
        super(name);
    }

    void broadcastEvent(final String action, final String event) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(action).putExtra(event, ""));
    }

    void broadcastEvent(final String action, final String event, String value) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(action).putExtra(event, value));
    }

    void broadcastStatus(final String action, final int status) {
        broadcastStatus(action, status, null);
    }

    void broadcastStatus(final String action, final int status, final Bundle data) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(action).putExtra(STATUS, status));

        final String tag = action == null ? getClass().getSimpleName() : action.replace(ACTION_PREFIX, "");
        if (data != null) {
            FirebaseAnalytics.getInstance(this).logEvent(tag.replace(ACTION_PREFIX, ""), data);
        }
    }
}
