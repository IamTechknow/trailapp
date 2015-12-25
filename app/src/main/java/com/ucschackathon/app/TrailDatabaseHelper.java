package com.ucschackathon.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * <p>An interface to deserialize the trail data from the KML file to store it locally.
 * Creates a SQL table for trails, markers, and trail coordinates, and allows trail and marker objects to be created from querying
 * the database. Locations are bound to Trails, but Markers are independent from trails.</p>
 *
 * <p>The database structure for trails and coordinates is to associate every trail coordinate with a trail ID, so when a Trail object is rebuilt,
 * its list of coordinates can be queried on the location table and its trail ID compared with that of the current trail</p>
 */

public class TrailDatabaseHelper extends SQLiteOpenHelper {
    public static final int MARKER_PARKING = 0, MARKER_ENTRANCE = 1, MARKER_RESTROOM = 2;

    private static final String DB_NAME = "trails.sqlite", TAG = "TrailDatabaseHelper";
    private static final int VERSION = 1;

    private static final String TABLE_TRAIL = "trail", TABLE_LOC = "location", TABLE_MARKER = "marker",
        COL_TRAIL_START_LAT = "start_lat", COL_TRAIL_START_LONG = "start_long", COL_TRAIL_COLOR = "color",
        COL_LOC_LAT = "lat", COL_LOC_LONG = "long", COL_TRAIL_ID = "_id", COL_ID = "trail_id", COL_MARKER_TYPE = "type";

    public TrailDatabaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }


    /**
     * Upon creation of the database, three tables are made to represent serialized Locations, Markers, and Trails
     * @param db The database created
     */

    @Override
    public void onCreate(SQLiteDatabase db) {
        //create the "trail" table, each entry has a trail ID, a starting lat and long coordinate for polyline, and color
        db.execSQL("create table " + TABLE_TRAIL + " ( _id integer primary key autoincrement, start_lat real, start_long real, color integer)");
        //location table, each entry has a reference to the trail database ID, lat and long
        db.execSQL("create table " + TABLE_LOC + " ( trail_id integer references trail(_id), lat real, long real)");
        //marker table, with entries lat and long (not associated with trails for now)
        db.execSQL("create table " + TABLE_MARKER + " ( type integer, lat real, long real)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // implement schema changes and data massage here when upgrading
    }

    /**
     * Inserts a trail into the database
     * @param t the Trail to store
     */

    public void insertTrail(Trail t) {
        ContentValues cv = new ContentValues();
        LatLng temp = t.getTrailCoords().get(0);

        //Set essential information about trail
        cv.put(COL_TRAIL_START_LAT, temp.latitude);
        cv.put(COL_TRAIL_START_LONG, temp.longitude);
        cv.put(COL_TRAIL_COLOR, t.getColor());
        long id = getWritableDatabase().insert(TABLE_TRAIL, null, cv); //returns ID of row, which we use for the trail ID

        //Set all locations into its table with the ID
        for(LatLng l: t.getTrailCoords())
            insertLoc(id, l);
    }

    /**
     * Inserts a trail into the database
     * @param trailId the Trail to store
     * @param loc The coordinates
     */

    public long insertLoc(long trailId, LatLng loc) {
        ContentValues cv = new ContentValues();
        cv.put(COL_LOC_LAT, loc.latitude);
        cv.put(COL_LOC_LONG, loc.longitude);
        cv.put(COL_ID, trailId);

        return getWritableDatabase().insert(TABLE_LOC, null, cv);
    }

    /**
     * Insert a marker into the database
     * @param type The type of the marker
     * @param loc The marker's coordinates
     * @return the ID of the marker (unused)
     */

    public long insertMarker(int type, LatLng loc) {
        ContentValues cv = new ContentValues();
        cv.put(COL_LOC_LAT, loc.latitude);
        cv.put(COL_LOC_LONG, loc.longitude);
        cv.put(COL_MARKER_TYPE, type);

        return getWritableDatabase().insert(TABLE_MARKER, null, cv);
    }

    /**
     * Goes through the leg work of parsing all the trails from the database,
     * creating Trail objects for each trail, then populating them with the trail coordinates
     * @return ArrayList of the trails
     */

    public ArrayList<Trail> queryTrails() {
        // equivalent to "select * from trail order by start_lat asc"
        Cursor trail_c = getReadableDatabase().query(TABLE_TRAIL, null, null, null, null, null, "_id" + " asc"); //ascending from ID
        Cursor loc_c = getReadableDatabase().query(TABLE_LOC, null, null, null, null, null, null); //don't sort but sort by ID if needed
        ArrayList<Trail> trails = new ArrayList<>();

        //iterate through the locations table to populate the trail with the coordinates
        //First check for matching IDs with the trail
        trail_c.moveToFirst(); loc_c.moveToFirst();

        while(!trail_c.isAfterLast()) { //For each row in trail table
            Trail trail = new Trail();
            trail.setmId(trail_c.getLong(trail_c.getColumnIndex(COL_TRAIL_ID)));
            trail.setColor(trail_c.getInt(trail_c.getColumnIndex(COL_TRAIL_COLOR)));

            while (!loc_c.isAfterLast() && loc_c.getLong(loc_c.getColumnIndex(COL_ID)) == trail.getmId()) { //Check if inside the table and matching IDs
                trail.addCoord(loc_c.getDouble(loc_c.getColumnIndex(COL_LOC_LAT)), loc_c.getDouble(loc_c.getColumnIndex(COL_LOC_LONG)));
                loc_c.moveToNext();
            } //when out of the loop, the cursor should be at the first coordinate of the next trail
            trails.add(trail);

            trail_c.moveToNext();
        }

        trail_c.close(); loc_c.close();
        return trails;
    }

    /**
     * Goes through the leg work of parsing all the markers from the database,
     * creating Marker objects for each row, then populating them with the marker type and coordinates
     * @return ArrayList of the Markers
     */

    public ArrayList<Marker> queryMarkers() {
        ArrayList<Marker> markers = new ArrayList<>();
        Cursor marker_c = getReadableDatabase().query(TABLE_MARKER, null, null, null, null, null, COL_MARKER_TYPE + " asc");
        marker_c.moveToFirst();

        while(!marker_c.isAfterLast()) {
            Marker m = new Marker(marker_c.getInt(marker_c.getColumnIndex(COL_MARKER_TYPE)), marker_c.getDouble(marker_c.getColumnIndex(COL_LOC_LAT)),marker_c.getDouble(marker_c.getColumnIndex(COL_LOC_LONG)));
            markers.add(m);
            marker_c.moveToNext();
        }

        marker_c.close();
        return markers;
    }
}
