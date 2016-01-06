package com.ucschackathon.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
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
    private static final String DB_NAME = "trails.sqlite", TAG = "TrailDatabaseHelper";
    private static final int VERSION = 1;

    private static final String TABLE_TRAIL = "trail", TABLE_LOC = "location", TABLE_MARKER = "marker", COL_TRAIL_COLOR = "color",
        COL_LOC_LAT = "lat", COL_LOC_LONG = "long", COL_TRAIL_ID = "_id", COL_ID = "trail_id", COL_MARKER_TYPE = "type", COL_MARKER_TITLE = "title";

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
        db.execSQL("create table " + TABLE_TRAIL + " ( _id integer primary key autoincrement, color integer)");
        //location table, each entry has a reference to the trail database ID, lat and long
        db.execSQL("create table " + TABLE_LOC + " ( trail_id integer references trail(_id), lat real, long real)");
        //marker table, with entries lat and long (not associated with trails for now)
        db.execSQL("create table " + TABLE_MARKER + " ( type integer, lat real, long real, title text)");
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

        //Set essential information about trail
        cv.put(COL_TRAIL_COLOR, t.getColor());
        SQLiteDatabase DB = getWritableDatabase();
        long id = DB.insert(TABLE_TRAIL, null, cv);//returns ID of row, which we use for the trail ID

        //Set all locations into its table with the ID. The transaction speeds things up a ton
        DB.beginTransaction();
        SQLiteStatement stmt = DB.compileStatement("insert into " + TABLE_LOC + " (trail_id, lat, long) values (?, ?, ?);");
        for(LatLng l: t.getTrailCoords())
            insertLoc(id, l, stmt);

        DB.setTransactionSuccessful();
        DB.endTransaction();

        Log.d(TAG, "Successfully inserted trail with ID " + Long.toString(id));
    }

    public void insertMarkers(ArrayList<Marker> a) {
        SQLiteDatabase DB = getWritableDatabase();

        DB.beginTransaction();
        SQLiteStatement st = DB.compileStatement("insert into " + TABLE_MARKER + " (type, lat, long, title) values (?, ?, ?, ?);");
        for(Marker m: a)
            insertMarker(m.getType(), m.getLoc(), m.getTitle(), st);
        insertPointsOfInterest(st);

        DB.setTransactionSuccessful();
        DB.endTransaction();
    }

    /**
     * Inserts a trail into the database
     * @param trailId the Trail to store
     * @param loc The coordinates
     * @param st the statement to bind data with
     */

    private void insertLoc(long trailId, LatLng loc, SQLiteStatement st) {
        st.bindLong(1, trailId);
        st.bindDouble(2, loc.latitude);
        st.bindDouble(3, loc.longitude);

        st.executeInsert(); //the entry ID (unused)
        st.clearBindings();
    }

    /**
     * Insert a marker into the database - same thing as insertLoc
     * @param type The type of the marker
     * @param loc The marker's coordinates
     * @param st the statement to bind data with
     * @param title The marker's title
     */

    private void insertMarker(int type, LatLng loc, String title, SQLiteStatement st) {
        st.bindLong(1, type);
        st.bindDouble(2, loc.latitude);
        st.bindDouble(3, loc.longitude);
        st.bindString(4, title);

        st.executeInsert(); //the entry ID (unused)
        st.clearBindings();
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
        Cursor marker_c = getReadableDatabase().query(TABLE_MARKER, null, null, null, null, null, COL_MARKER_TYPE + " desc");
        marker_c.moveToFirst();

        while(!marker_c.isAfterLast()) {
            Marker m = new Marker(marker_c.getInt(marker_c.getColumnIndex(COL_MARKER_TYPE)), marker_c.getString(marker_c.getColumnIndex(COL_MARKER_TITLE)), marker_c.getDouble(marker_c.getColumnIndex(COL_LOC_LAT)),marker_c.getDouble(marker_c.getColumnIndex(COL_LOC_LONG)));
            markers.add(m);
            marker_c.moveToNext();
        }

        marker_c.close();
        return markers;
    }

    /**
     * Insert data about markers of interest, for setting up detailed info views
     */

    public void insertPointsOfInterest(SQLiteStatement st) {
        insertMarker(Marker.INFO, new LatLng(36.911, -121.803), "Nest of Osprey", st);
        insertMarker(Marker.INFO, new LatLng(36.905060, -121.785410), "DFG Outlook", st);
        insertMarker(Marker.INFO, new LatLng(36.913525, -121.780813), "Struve Slough", st);
        insertMarker(Marker.INFO, new LatLng(36.911101, -121.776457), "Tarplant Hill", st);
        insertMarker(Marker.INFO, new LatLng(36.913507, -121.768687), "Wetland restoration", st);
        insertMarker(Marker.INFO, new LatLng(36.912601, -121.770290), "Cattails", st);
        insertMarker(Marker.INFO, new LatLng(36.9016682,-121.7845458), "Harkins Slough", st);
    }
}
