package com.example.runnertracker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class RecordJourney extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_journey);

        GifPlayer gif = findViewById(R.id.gif);
        gif.setGifImageResource(R.drawable.runninggif);
    }
}
