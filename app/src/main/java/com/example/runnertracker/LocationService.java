package com.example.runnertracker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LocationService extends Service {
    private LocationManager locationManager;
    private MyLocationListener locationListener;
    private final IBinder binder = new LocationServiceBinder();

    private final String CHANNEL_ID = "100";
    private final int NOTIFICATION_ID = 001;
    private long startTime = 0;
    private long stopTime = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("mdp", "Location Service created");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();
        locationListener.recordLocations = false;

        try {
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 3, 3, locationListener);
        } catch(SecurityException e) {
            // don't have the permission to access GPS
            Log.d("mdp", "No Permissions for GPS");
        }
    }

    private void addNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Tracking Journey";
            String description = "Keep Running!";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name,
                    importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this,
                CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Tracking Journey")
                .setContentText("Keep Running!")
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // user has closed the application so cancel the current journey and stop tracking GPS
        locationManager.removeUpdates(locationListener);
        locationListener = null;
        locationManager = null;

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
        Log.d("mdp", "Location Service destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    protected float getDistance() {
        return locationListener.getDistanceOfJourney();
    }

    /* Display notification and start recording GPS locations for a new, also start timer */
    protected void playJourney() {
        addNotification();
        locationListener.newJourney();
        locationListener.recordLocations = true;
        startTime = SystemClock.elapsedRealtime();
        stopTime = 0;
    }

    /* Get the duration of the current journey */
    protected double getDuration() {
        if(startTime == 0) {
            return 0.0;
        }

        long endTime = SystemClock.elapsedRealtime();

        if(stopTime != 0) {
            // saveJourney has been called, until playJourney is called again display constant time
            endTime = stopTime;
        }

        long elapsedMilliSeconds = endTime - startTime;
        return elapsedMilliSeconds / 1000.0;
    }

    protected boolean currentlyTracking() {
        return startTime != 0;
    }

    /* Save journey to the database and stop saving GPS locations, also removes the notification */
    protected void saveJourney() {
        // save journey to database using content provider
        ContentValues journeyData = new ContentValues();
        journeyData.put(JourneyProviderContract.J_distance, getDistance());
        journeyData.put(JourneyProviderContract.J_DURATION, (long) getDuration());
        journeyData.put(JourneyProviderContract.J_DATE, getDateTime());

        long journeyID = Long.parseLong(getContentResolver().insert(JourneyProviderContract.JOURNEY_URI, journeyData).getLastPathSegment());

        // for each location belonging to this journey save it to the location table linked to this journey
        for(Location location : locationListener.getLocations()) {
            ContentValues locationData = new ContentValues();
            locationData.put(JourneyProviderContract.L_JID, journeyID);
            locationData.put(JourneyProviderContract.L_ALTITUDE, location.getAltitude());
            locationData.put(JourneyProviderContract.L_LATITUDE, location.getLatitude());
            locationData.put(JourneyProviderContract.L_LONGITUDE, location.getLongitude());

            getContentResolver().insert(JourneyProviderContract.LOCATION_URI, locationData);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);

        // reset state by clearing locations, stop recording, reset startTime
        locationListener.recordLocations = false;
        stopTime = SystemClock.elapsedRealtime();
        startTime = 0;
        locationListener.newJourney();

        Log.d("mdp", "Journey saved with id = " + journeyID);
    }

    public void notifyGPSEnabled() {
        try {
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 3, 3, locationListener);
        } catch(SecurityException e) {
            // don't have the permission to access GPS
            Log.d("mdp", "No Permissions for GPS");
        }
    }

    private String getDateTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        return formatter.format(date);
    }

    public class LocationServiceBinder extends Binder {
        // would like to get the distance in km for activity
        // the activity will keep track of the duration using chronometer
        public float getDistance() {
            return LocationService.this.getDistance();
        }

        public double getDuration() {
            // get duration in seconds
            return LocationService.this.getDuration();
        }

        public boolean currentlyTracking() {return LocationService.this.currentlyTracking();}

        public void playJourney() {
            LocationService.this.playJourney();
        }

        public void saveJourney() {
            LocationService.this.saveJourney();
        }

        public void notifyGPSEnabled() { LocationService.this.notifyGPSEnabled();}
    }
}
