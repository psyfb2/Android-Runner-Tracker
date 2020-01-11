package com.example.runnertracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class EditJourney extends AppCompatActivity {
    private EditText titleET;
    private EditText commentET;
    private EditText ratingET;
    private long journeyID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_journey);

        Bundle bundle = getIntent().getExtras();

        titleET = findViewById(R.id.titleEditText);
        commentET = findViewById(R.id.commentEditText);
        ratingET = findViewById(R.id.ratingEditText);
        journeyID = bundle.getLong("journeyID");

        populateEditText();
    }

    /* Save the new title, comment and rating to the DB */
    public void onClickSave(View v) {
        int rating = checkRating(ratingET);
        if(rating == -1) {
            return;
        }

        Uri rowQueryUri = Uri.withAppendedPath(JourneyProviderContract.JOURNEY_URI, "" + journeyID);

        ContentValues cv = new ContentValues();
        cv.put(JourneyProviderContract.J_RATING, rating);
        cv.put(JourneyProviderContract.J_COMMENT, commentET.getText().toString());
        cv.put(JourneyProviderContract.J_NAME, titleET.getText().toString());

        getContentResolver().update(rowQueryUri, cv, null, null);
        finish();
    }

    /* Give the edit texts some initial text from what they were, get this by accessing DB */
    private void populateEditText() {
        Cursor c = getContentResolver().query(Uri.withAppendedPath(JourneyProviderContract.JOURNEY_URI,
                journeyID + ""), null, null, null, null);

        if(c.moveToFirst()) {
            titleET.setText(c.getString(c.getColumnIndex(JourneyProviderContract.J_NAME)));
            commentET.setText(c.getString(c.getColumnIndex(JourneyProviderContract.J_COMMENT)));
            ratingET.setText(c.getString(c.getColumnIndex(JourneyProviderContract.J_RATING)));
        }
    }

    /* Ensure a rating is between 1-5 */
    private int checkRating(EditText newRating) {
        int rating;
        try {
            rating = Integer.parseInt(newRating.getText().toString());
        } catch(Exception e) {
            Log.d("mdp", "The following is not a number: " + newRating.getText().toString());
            return -1;
        }

        if(rating < 0 || rating > 5) {
            Log.d("mdp", "Rating must be between 0-5");
            return -1;
        }
        return rating;
    }

}
