package com.example.runnertracker;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(Context context) {
        super(context, "localDB", null, 1);
    }

    // called when getWritableDatabase is called and the database file doesn't exist
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE journey (" +
                "journeyID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "duration BIGINT NOT NULL," +
                "distance FLOAT NOT NULL," +
                "rating INTEGER ," +
                "comment varchar(256)," +
                "date DATE ," +
                "image BLOB);");

        db.execSQL("CREATE TABLE location (" +
                " journeyID INT NOT NULL," +
                " altitude INT NOT NULL," +
                " longitude INT NOT NULL," +
                " latitude INT NOT NULL," +
                " CONSTRAINT fk1 FOREIGN KEY (journeyID) REFERENCES journey (journeyID) ON DELETE CASCADE," +
                " CONSTRAINT locationID PRIMARY KEY (journeyID) );");

        // add some test data
        db.execSQL("INSERT INTO journey VALUES (1, 75, 1.5, 3)");
    }

    // called when the database file exists but the version number stored in the db
    // is lower than that passed in the constructor
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
