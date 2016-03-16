package com.ucschackathon.app.model;

import com.google.android.gms.maps.model.LatLng;
import com.ucschackathon.app.R;

/**
 * A Marker represents an icon to be drawn on to the map as a bitmap.
 * Markers have a type and location and may be accessed to store its information
 * inside a SQLite database.
 * LatLng is used instead of the Location class because it is easier to work with to just store coordinates
 */

public class Marker {
    public static final int NONE = 0, PARKING = 1, ENTRANCE = 2, RESTROOM = 3, NATURECENTER = 4, INFO = 5;

    private int type;
    private LatLng loc;
    private String title;

    public Marker() {
        type = NONE;
    }

    public Marker(int t, String Title, double x, double y) {
        type = t;
        title = Title;
        loc = new LatLng(x, y);
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public LatLng getLoc() {
        return loc;
    }

    public void setLoc(LatLng loc) {
        this.loc = loc;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    //Obtains the resource ID for the given Marker type
    public static int getMarkerIconID(Marker m) {
        int resourceID;

        switch(m.getType()) {
            case ENTRANCE:
                resourceID = R.drawable.sloughtrailentrances;
                break;
            case PARKING:
                resourceID = R.drawable.sloughtrailparking;
                break;
            case RESTROOM:
                resourceID = R.drawable.bathrooms;
                break;
            case NATURECENTER:
                resourceID = R.drawable.naturecenters;
                break;
            case INFO:
                resourceID = android.R.drawable.ic_dialog_info;
                break;
            default: //For completeness
                resourceID = android.R.drawable.stat_notify_error;
        }
        return resourceID;
    }
}
