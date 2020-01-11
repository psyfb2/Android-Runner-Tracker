package com.example.runnertracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class EditJourney extends AppCompatActivity {
    private final int RESULT_LOAD_IMG = 1;

    private ImageView journeyImg;
    private EditText titleET;
    private EditText commentET;
    private EditText ratingET;
    private long journeyID;

    private Uri selectedJourneyImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_journey);

        Bundle bundle = getIntent().getExtras();

        journeyImg = findViewById(R.id.journeyImg);
        titleET = findViewById(R.id.titleEditText);
        commentET = findViewById(R.id.commentEditText);
        ratingET = findViewById(R.id.ratingEditText);
        journeyID = bundle.getLong("journeyID");

        selectedJourneyImg = null;

        populateEditText();
    }

    /* Save the new title, comment, image and rating to the DB */
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

        if(selectedJourneyImg != null) {
            cv.put(JourneyProviderContract.J_IMAGE, selectedJourneyImg.toString());
        }

        getContentResolver().update(rowQueryUri, cv, null, null);
        finish();
    }

    /* Load activity to choose an image */
    public void onClickChangeImage(View v) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                journeyImg.setImageBitmap(selectedImage);
                selectedJourneyImg = imageUri;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }else {
            Toast.makeText(EditJourney.this, "You didn't pick an Image",Toast.LENGTH_LONG).show();
        }
    }

    /* Give the edit texts some initial text from what they were, get this by accessing DB */
    private void populateEditText() {
        Cursor c = getContentResolver().query(Uri.withAppendedPath(JourneyProviderContract.JOURNEY_URI,
                journeyID + ""), null, null, null, null);

        if(c.moveToFirst()) {
            titleET.setText(c.getString(c.getColumnIndex(JourneyProviderContract.J_NAME)));
            commentET.setText(c.getString(c.getColumnIndex(JourneyProviderContract.J_COMMENT)));
            ratingET.setText(c.getString(c.getColumnIndex(JourneyProviderContract.J_RATING)));

            // if an image has been set by user display it, else default image is displayed
            String strUri = c.getString(c.getColumnIndex(JourneyProviderContract.J_IMAGE));
            if(strUri != null) {
                try {
                    final Uri imageUri = Uri.parse(strUri);
                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    journeyImg.setImageBitmap(selectedImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
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
