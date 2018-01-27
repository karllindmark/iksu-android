package com.ninetwozero.iksu.network;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.ninetwozero.iksu.models.Workout;

import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;

@SuppressLint("MissingPermission") // Handled via EasyPermissions
public class IksuLocationService extends Service {
    public static final String ACTION_VALIDATE = "com.ninetwozero.iksu.action.validate_location";
    public static final String ID = "workoutPkId";
    public static final String STATUS = ACTION_VALIDATE + ".STATUS";

    public static final int STATUS_VALID = -1;
    public static final int STATUS_NONE = 0;
    public static final int STATUS_INVALID = 1;

    private static final Map<String, FacilityLocation> AVAILABLE_FACILITIES = new HashMap<String, FacilityLocation>() {{
        put("100", FacilityLocation.IKSU_SPORT);
        put("200", FacilityLocation.IKSU_SPA);
        put("300", FacilityLocation.IKSU_PLUS);
    }};

    private String workoutPkId;
    private FusedLocationProviderClient locationClient;
    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (workoutPkId == null) {
                return;
            }

            onLocationUpdated(locationResult.getLastLocation(), true);
        }
    };

    public static Intent newIntent(final Context context, final Workout workout) {
        return new Intent(context, IksuLocationService.class).putExtra(ID, workout.getPkId());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.locationClient = LocationServices.getFusedLocationProviderClient(this);
        this.locationClient.requestLocationUpdates(createLocationRequest(), locationCallback, Looper.myLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        workoutPkId = intent.getStringExtra(ID);

        locationClient.getLastLocation().addOnCompleteListener(
            new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        onLocationUpdated(task.getResult());
                    } else {
                        broadcastStatus(STATUS_NONE);
                    }
                }
            }
        );

        return START_NOT_STICKY;
    }

    private LocationRequest createLocationRequest() {
        final LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    private void broadcastStatus(final int status) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_VALIDATE).putExtra(STATUS, status));
    }

    private void onLocationUpdated(Location lastLocation) {
        onLocationUpdated(lastLocation, false);
    }

    private void onLocationUpdated(Location lastLocation, boolean stopAfterCompletion) {
        if (lastLocation != null) {
            final Workout workout = Realm.getDefaultInstance().where(Workout.class).equalTo("pkId", workoutPkId).findFirst();
            if (workout != null) {
                if (AVAILABLE_FACILITIES.get(workout.getFacilityId()).contains(lastLocation)) {
                    broadcastStatus(STATUS_VALID);
                    shutdownService();
                    return;
                }
            }
        }
        broadcastStatus(STATUS_INVALID);

        if (stopAfterCompletion) {
            shutdownService();
        }
    }

    private void shutdownService() {
        locationClient.removeLocationUpdates(locationCallback);
        stopSelf();
    }

    // TODO: If no direct match, check the distance to any of the given corners (max distance == GPS accuracy)
    private static class FacilityLocation {
        private static final FacilityLocation IKSU_SPORT = new FacilityLocation(
            new Coordinate(63.817999d, 20.317111d),
            new Coordinate(63.817999d, 20.321166d),
            new Coordinate(63.819200d, 20.317111d),
            new Coordinate(63.819200d, 20.321166d)
        );
        private static final FacilityLocation IKSU_PLUS = new FacilityLocation(

            new Coordinate(63.820430d, 20.275515d),
            new Coordinate(63.820430d, 20.276580d),
            new Coordinate(63.820555d, 20.275515d),
            new Coordinate(63.820555d, 20.276580d)
        );

        private static final FacilityLocation IKSU_SPA = new FacilityLocation(
            new Coordinate(63.835900d, 20.165625d),
            new Coordinate(63.835900d, 20.167230d),
            new Coordinate(63.836597d, 20.165625d),
            new Coordinate(63.836597d, 20.167230d)
        );

        public final Coordinate nw;
        public final Coordinate ne;
        public final Coordinate se;
        public final Coordinate sw;

        private FacilityLocation(Coordinate nw, Coordinate ne, Coordinate sw, Coordinate se) {
            this.nw = nw;
            this.ne = ne;
            this.sw = sw;
            this.se = se;
        }

        public boolean contains(final Location location) {
            return (
                (nw.latitude <= location.getLatitude() && nw.longitude <= location.getLongitude()) &&
                (ne.latitude <= location.getLatitude() && ne.longitude >= location.getLongitude()) &&
                (sw.latitude >= location.getLatitude() && sw.longitude <= location.getLongitude()) &&
                (se.latitude >= location.getLatitude() && se.longitude >= location.getLongitude())
            );
        }

        private static class Coordinate {
            public final double latitude;
            public final double longitude;

            public Coordinate(double latitude, double longitude) {
                this.latitude = latitude;
                this.longitude = longitude;
            }

            @Override
            public String toString() {
                return latitude + "," + longitude;
            }
        }
    }
}
