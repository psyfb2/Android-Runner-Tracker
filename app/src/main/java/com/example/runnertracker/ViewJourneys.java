package com.example.runnertracker;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ViewJourneys extends AppCompatActivity {
    private TextView dateText;
    private DatePickerDialog.OnDateSetListener dateListener;
    private ListView journeyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_journeys);

        setUpDateDialogue();

        journeyList.setClickable(true);
        journeyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                CursorWrapper o = (CursorWrapper) journeyList.getItemAtPosition(position);
                long journeyID = o.getLong(o.getColumnIndex("_id"));

                // start the single journey activity sending it the journeyID
                Bundle b = new Bundle();
                b.putLong("journeyID", journeyID);
                Intent singleJourney = new Intent(ViewJourneys.this, ViewSingleJourney.class);
                singleJourney.putExtras(b);
                startActivity(singleJourney);
            }
        });
    }

    private void setUpDateDialogue() {
        dateText = findViewById(R.id.selectDateText);
        journeyList = findViewById(R.id.journeysList);

        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int yyyy;
                int mm;
                int dd;

                // if first select choose current date, else last selected date
                if(dateText.getText().toString().toLowerCase().equals("select date")) {
                    Calendar calender = Calendar.getInstance();
                    yyyy = calender.get(Calendar.YEAR);
                    mm = calender.get(Calendar.MONTH);
                    dd = calender.get(Calendar.DAY_OF_MONTH);
                } else {
                    String[] dateParts = dateText.getText().toString().split("/");
                    yyyy = Integer.parseInt(dateParts[2]);
                    mm = Integer.parseInt(dateParts[1]) - 1;
                    dd = Integer.parseInt(dateParts[0]);
                }

                DatePickerDialog dialog = new DatePickerDialog(
                        ViewJourneys.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        dateListener,
                        yyyy, mm, dd);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        dateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int yyyy, int mm, int dd) {
                // user has selected a date on which to view journeys
                mm = mm + 1;
                String date;

                // format the date so its like dd/mm/yyyy
                if(mm < 10) {
                     date = dd + "/0" + mm + "/" + yyyy;
                } else {
                    date = dd + "/" + mm + "/" + yyyy;
                }

                if(dd < 10) {
                    date = "0" + date;
                }

                dateText.setText(date);

                listJourneys(date);
            }
        };
    }

    /* Query database to get all journeys in specified date in dd/mm/yyyy format and display them in listview */
    private void listJourneys(String date) {
        // sqlite server expects yyyy-mm-dd
        String[] dateParts = date.split("/");
        date = dateParts[2] + "-" + dateParts[1] + "-" + dateParts[0];

        Log.d("mdp", "Searching for date " + date);

        Cursor c = getContentResolver().query(JourneyProviderContract.JOURNEY_URI,
                new String[] {JourneyProviderContract.J_ID + " _id", JourneyProviderContract.J_NAME}, JourneyProviderContract.J_DATE + " = ?", new String[] {date}, JourneyProviderContract.J_NAME + " ASC");

        Log.d("mdp", "Journeys Loaded: " + c.getCount());

        String[] nameCol = new String[] {JourneyProviderContract.J_NAME};
        int[] textViewIds = new int[] {R.id.singleJourney};
        journeyList.setAdapter(new SimpleCursorAdapter(this, R.layout.journeylist, c, nameCol, textViewIds, 0));
    }
}
