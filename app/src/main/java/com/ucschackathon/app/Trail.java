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

    public Trail() {
        trailCoords = new ArrayList<>();
    }

    public void addCoord(float x, float y) {
        LatLng loc = new LatLng(x, y);


        trailCoords.add(loc);
    }

    public ArrayList<LatLng> getTrailCoords() {
        return trailCoords;
    }
}
