package com.example.runnertracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;


/*
TO DO
    1. Allow user to record a journey which is saved to a database. The path, time, distance, date via gps
    2. Allow user to stop, start or pause a journey. Stopping a journey causes it to be saved
    3. Allow user to attach image to a saved journey
    4. Allow user to rate a journey out of 5
    5. Allow user to add comments about a certain journey
    6. Allow user to see a list of recorded journeys
    7. Clicking on a recorded journey displays more information (rating, comments, picture, time, distance, average distance, path on google maps)
    8. Allow user to delete a journey
    9. Allow use to see stastics page which shows how far ran today, this week, this month, all time and could graph these
    10. Allow user to set a goal for km to run every week, display whether the goal has been reached or not in the app.
    11. Allow user to track not only running but also cycling

    Need
        - Service (for GPS tracking)
        - Activities (to display stats, journeys, single journeys, recording a journey, home page)
        - Database (to store journey information)
        - Content Provider (in order to access the database)
        - Broadcast Receiver to register callbacks
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void onClickRecord(View v) {
        // go to the record journey activity
        Intent journey = new Intent(MainActivity.this, RecordJourney.class);
        startActivity(journey);
    }

    public void onClickView(View v) {
        // go to the activity for displaying journeys

    }

    public void onClickStatistics(View v) {
        // go to the activity for displaying statistics
    }
}
