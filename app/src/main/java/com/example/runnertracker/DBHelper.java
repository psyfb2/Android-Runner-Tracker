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
                "distance REAL NOT NULL," +
                "date DATETIME NOT NULL," +
                "name varchar(256) NOT NULL DEFAULT 'Recorded Journey'," +
                "rating INTEGER NOT NULL DEFAULT 1," +
                "comment varchar(256) NOT NULL DEFAULT ''," +
                "image varchar(256) DEFAULT NULL);");

        db.execSQL("CREATE TABLE location (" +
                " locationID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                " journeyID INTEGER NOT NULL," +
                " altitude REAL NOT NULL," +
                " longitude REAL NOT NULL," +
                " latitude REAL NOT NULL," +
                " CONSTRAINT fk1 FOREIGN KEY (journeyID) REFERENCES journey (journeyID) ON DELETE CASCADE);");
    }

    // called when the database file exists but the version number stored in the db
    // is lower than that passed in the constructor
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
