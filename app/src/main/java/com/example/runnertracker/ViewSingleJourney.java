package com.example.runnertracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class ViewSingleJourney extends AppCompatActivity {
    private ImageView journeyImg;
    private TextView distanceTV;
    private TextView avgSpeedTV;
    private TextView timeTV;
    private TextView dateTV;
    private TextView ratingTV;
    private TextView commentTV;
    private TextView titleTV;

    private long journeyID;

    private Handler h = new Handler();

    // observer is notified when insert or delete in the given URI occurs
    protected class MyObserver extends ContentObserver {

        public MyObserver(Handler handler) {
            super(handler);
        }
        @Override
        public void onChange(boolean selfChange) {
            this.onChange(selfChange, null);
        }
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            // called when something in the database wrapped by the content provider changes
            // update the view
            populateView();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_single_journey);

        Bundle bundle = getIntent().getExtras();

        journeyImg = findViewById(R.id.ViewSingleJourney_journeyImg);
        distanceTV = findViewById(R.id.ViewSingleJourney_distanceText);
        avgSpeedTV = findViewById(R.id.ViewSingleJourney_avgSpeed);
        timeTV     = findViewById(R.id.ViewSingleJourney_durationText);
        dateTV     = findViewById(R.id.ViewSingleJourney_dateText);
        ratingTV   = findViewById(R.id.ViewSingleJourney_ratingText);
        commentTV  = findViewById(R.id.ViewSingleJourney_commentText);
        titleTV    = findViewById(R.id.ViewSingleJourney_titleText);
        journeyID  = bundle.getLong("journeyID");

        populateView();
        getContentResolver().registerContentObserver(
                JourneyProviderContract.ALL_URI, true, new MyObserver(h));
    }

    public void onClickEdit(View v) {
        // take to activity for editing the fields for this single journey
        Intent editActivity = new Intent(ViewSingleJourney.this, EditJourney.class);
        Bundle b = new Bundle();
        b.putLong("journeyID", journeyID);
        editActivity.putExtras(b);
        startActivity(editActivity);
    }

    public void onClickMap(View v) {
        // display this journey on a google map activity
        Intent map = new Intent(ViewSingleJourney.this, MapsActivity.class);
        Bundle b = new Bundle();
        b.putLong("journeyID", journeyID);
        map.putExtras(b);
        startActivity(map);
    }

    private void populateView() {
        // use content provider to load data from the database and display on the text views
        Cursor c = getContentResolver().query(Uri.withAppendedPath(JourneyProviderContract.JOURNEY_URI,
                journeyID + ""), null, null, null, null);

        if(c.moveToFirst()) {
            double distance = c.getDouble(c.getColumnIndex(JourneyProviderContract.J_distance));
            long time       = c.getLong(c.getColumnIndex(JourneyProviderContract.J_DURATION));
            double avgSpeed = 0;

            if(time != 0) {
                avgSpeed = distance / (time / 3600.0);
            }

            long hours = time / 3600;
            long minutes = (time % 3600) / 60;
            long seconds = time % 60;

            distanceTV.setText(String.format("%.2f KM", distance));
            avgSpeedTV.setText(String.format("%.2f KM/H", avgSpeed));
            timeTV.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));

            // date will be stored as yyyy-mm-dd, convert to dd-mm-yyyy
            String date = c.getString(c.getColumnIndex(JourneyProviderContract.J_DATE));
            String[] dateParts = date.split("-");
            date = dateParts[2] + "/" + dateParts[1] + "/" + dateParts[0];

            dateTV.setText(date);
            ratingTV.setText(c.getInt(c.getColumnIndex(JourneyProviderContract.J_RATING)) + "");
            commentTV.setText(c.getString(c.getColumnIndex(JourneyProviderContract.J_COMMENT)));
            titleTV.setText(c.getString(c.getColumnIndex(JourneyProviderContract.J_NAME)));

            // if an image has been set by user display it, else default image is displayed
            String strUri = c.getString(c.getColumnIndex(JourneyProviderContract.J_IMAGE));
            if(strUri != null) {
                try {
                    final Uri imageUri = Uri.parse(strUri);
                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    journeyImg.setImageBitmap(selectedImage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
