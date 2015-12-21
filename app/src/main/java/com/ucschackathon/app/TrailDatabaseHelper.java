package com.ucschackathon.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

public class TrailDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "trails.sqlite";
    private static final int VERSION = 1;

    //TODO: strings for common sql ops

    public TrailDatabaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //TODO: create the "trail" table
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // implement schema changes and data massage here when upgrading
    }
}
