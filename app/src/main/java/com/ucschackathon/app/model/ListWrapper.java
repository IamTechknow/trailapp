package com.ucschackathon.app.model;

import java.util.ArrayList;

public class ListWrapper {
    public ArrayList<Marker> markers;
    public ArrayList<Trail> trails;

    public ListWrapper(ArrayList<Marker> markers, ArrayList<Trail> trails) {
        this.markers = markers;
        this.trails = trails;
    }
}