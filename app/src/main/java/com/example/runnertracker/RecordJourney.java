package com.example.runnertracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

public class RecordJourney extends AppCompatActivity {
    private GifPlayer gif;
    private LocationService.LocationServiceBinder locationService;

    private TextView distanceText;
    private TextView avgSpeedText;
    private TextView durationText;

    private Button playButton;
    private Button stopButton;

    // will poll the location service for distance and duration
    private Handler postBack = new Handler();

    private ServiceConnection lsc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            locationService = (LocationService.LocationServiceBinder) iBinder;

            // if currently tracking then enable stopButton and disable startButton
            if(locationService.currentlyTracking()) {
                stopButton.setEnabled(true);
                playButton.setEnabled(false);
            } else {
                stopButton.setEnabled(false);
                playButton.setEnabled(true);
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (locationService != null) {
                        // get the distance and duration from the surface
                        float d = (float) locationService.getDuration();
                        final long duration = (long) d;  // in seconds
                        float distance = locationService.getDistance();

                        long hours = duration / 3600;
                        long minutes = (duration % 3600) / 60;
                        long seconds = duration % 60;

                        float avgSpeed = 0;
                        if(d != 0) {
                            avgSpeed = distance / (d / 3600);
                        }

                        final String time = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                        final String dist = String.format("%.2f KM", distance);
                        final String avgs = String.format("%.2f KM/H", avgSpeed);

                        postBack.post(new Runnable() {
                            @Override
                            public void run() {
                                // post back changes to UI thread
                                durationText.setText(time);
                                avgSpeedText.setText(avgs);
                                distanceText.setText(dist);
                            }
                        });

                        try {
                            Thread.sleep(500);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            locationService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_journey);

        gif = findViewById(R.id.gif);
        gif.setGifImageResource(R.drawable.runninggif);
        gif.pause();

        distanceText = findViewById(R.id.distanceText);
        durationText = findViewById(R.id.durationText);
        avgSpeedText = findViewById(R.id.avgSpeedText);

        playButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);

        // connect to service to see if currently tracking before enabling a button
        stopButton.setEnabled(false);
        playButton.setEnabled(false);

        // start the service so that it persists outside of the lifetime of this activity
        // and also bind to it to gain control over the service
        startService(new Intent(this, LocationService.class));
        bindService(
                new Intent(this, LocationService.class), lsc, Context.BIND_AUTO_CREATE);
    }

    public void onClickPlay(View view) {
        gif.play();
        // start the timer and tracking GPS locations
        locationService.playJourney();
        playButton.setEnabled(false);
        stopButton.setEnabled(true);
    }


    public void onClickStop(View view) {
        // save the current journey to the database


    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // unbind to the service (if we are the only binding activity then the service will be destroyed)
        if(lsc != null) {
            unbindService(lsc);
            lsc = null;
        }
    }
    
}
