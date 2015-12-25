package com.ucschackathon.app;

import java.util.ArrayList;
import com.google.android.gms.maps.model.LatLng;

/**
 * A trail is a polyline drawn on the map. It is represented with a list of coordinates that represent the polyline.
 * The coordinates may also be accessed to be stored onto a SQLite database and can be reconstructed when the app starts
 * LatLng is used instead of the Location class because it is easier to work with to just store coordinates
 */

public class Trail {
    private ArrayList<LatLng> trailCoords;
    private int color;
    private long mId;

    public Trail() {
        trailCoords = new ArrayList<>();
    }

    public Trail(ArrayList<LatLng> a, int c) {
        trailCoords = a;
        color = c;
    }

    public void addCoord(float x, float y) {
        trailCoords.add(new LatLng(x, y));
    }

    public void setTrailCoords(ArrayList<LatLng> list) {
        trailCoords = list;
    }

    public ArrayList<LatLng> getTrailCoords() {
        return trailCoords;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int Color) {
        color = Color;
    }

    public long getmId() {
        return mId;
    }

    public void setmId(long id) {
        mId = id;
    }
}
