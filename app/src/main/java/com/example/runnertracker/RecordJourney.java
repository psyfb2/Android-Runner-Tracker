package com.example.runnertracker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;

public class RecordJourney extends AppCompatActivity {
    private long lastPause;
    private Chronometer timer;
    private Button pause;
    private GifPlayer gif;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_journey);

        gif = findViewById(R.id.gif);
        gif.setGifImageResource(R.drawable.runninggif);

        timer = findViewById(R.id.time);
        pause = findViewById(R.id.pauseButton);

        lastPause = 0;

        if(pause.getText().toString().toLowerCase().equals("pause")) {
            gif.pause();
        }
    }

    public void onClickPauseOrPlay(View view) {
        if(pause.getText().toString().toLowerCase().equals("pause")) {
            // timer was playing but now user wants to pause
            lastPause = SystemClock.elapsedRealtime();
            timer.stop();
            gif.pause();
            pause.setText("PLAY");
        } else {
            // timer was paused but now user wants to start timer again either from 0 or previous value
            if(lastPause != 0) {
                timer.setBase(timer.getBase() + SystemClock.elapsedRealtime() - lastPause);
            } else {
                // starting for the first time
                timer.setBase(SystemClock.elapsedRealtime());
            }
            gif.play();
            timer.start();
            pause.setText("PAUSE");
        }
    }
    
}
