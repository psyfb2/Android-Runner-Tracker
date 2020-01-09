package com.example.runnertracker;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class JourneyProvider extends ContentProvider {
    DBHelper dbh;
    SQLiteDatabase db;

    private static final UriMatcher matcher;

    // map URI's to codes so we can decide which query to make based on the URI received
    static {
        matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(JourneyProviderContract.AUTHORITY, "journey", 1);
        matcher.addURI(JourneyProviderContract.AUTHORITY, "journey/#", 2);
        matcher.addURI(JourneyProviderContract.AUTHORITY, "location", 3);
        matcher.addURI(JourneyProviderContract.AUTHORITY, "location/#", 4);
    }

    @Override
    public boolean onCreate() {
        Log.d("mdp", "Journey Content Provider created");
        dbh = new DBHelper(this.getContext());
        db = dbh.getWritableDatabase();
        return (db != null);
    }

    @Override
    public String getType(Uri uri) {
        if (uri.getLastPathSegment() == null) {
            return "vnd.android.cursor.dir/JourneyProvider.data.text";
        } else {
            return "vnd.android.cursor.item/JourneyProvider.data.text";
        }
    }

    // implement CRUD database operations

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        String tableName;

        // given uri -> table name
        switch(matcher.match(uri)) {
            case 1:
                tableName = "journey";
                break;
            case 3:
                tableName = "location";
                break;
            default:
                tableName = "";
        }

        // insert the values into the table and return the same url but with the id appended
        long _id = db.insert(tableName, null, values);
        Uri newRowUri = ContentUris.withAppendedId(uri, _id);

        // notify any registered content observers that a change has been made to this table
        getContext().getContentResolver().notifyChange(newRowUri, null);
        return newRowUri;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[]
            selectionArgs, String sortOrder) {

        switch(matcher.match(uri)) {
            case 2:
                // gave /# URI so they want a specific row
                selection = "journeyID = " + uri.getLastPathSegment();
            case 1:
                return db.query("journey", projection, selection, selectionArgs, null, null, sortOrder);
            case 4:
                selection = "locationID = " + uri.getLastPathSegment();
            case 3:
                return db.query("location", projection, selection, selectionArgs, null, null, sortOrder);
            default:
                return null;
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[]
            selectionArgs) {
        String tableName;
        int count;

        // given uri -> table name
        switch(matcher.match(uri)) {
            case 2:
                // gave /# URI so they want a specific row
                selection = "journeyID = " + uri.getLastPathSegment();
            case 1:
                tableName = "journey";
                count = db.update(tableName, values, selection, selectionArgs);
                break;
            case 4:
                selection = "locationID = " + uri.getLastPathSegment();
            case 3:
                tableName = "location";
                count = db.update(tableName, values, selection, selectionArgs);
                break;
            default:
                return 0;
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String tableName;
        int count;

        // given uri -> table name
        switch(matcher.match(uri)) {
            case 2:
                // gave /# URI so they want a specific row
                selection = "journeyID = " + uri.getLastPathSegment();
            case 1:
                tableName = "journey";
                count = db.delete(tableName, selection, selectionArgs);
                break;
            case 4:
                selection = "locationID = " + uri.getLastPathSegment();
            case 3:
                tableName = "location";
                count = db.delete(tableName, selection, selectionArgs);
                break;
            default:
                return 0;
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

}
