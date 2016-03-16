package com.ucschackathon.app;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.ucschackathon.app.model.*;

import java.util.ArrayList;

public class TrailDataLoader extends AsyncTaskLoader<ListWrapper> {
    private ArrayList<Trail> trails;
    private ArrayList<Marker> markers;
    private TrailDatabaseHelper mHelper;

    public TrailDataLoader(Context c) {
        super(c);
        mHelper = new TrailDatabaseHelper(c);
    }

    //If the data is cached, use it!
    @Override
    protected void onStartLoading() {
        if(trails != null && markers != null)
            deliverResult(new ListWrapper(markers, trails));
        else
            forceLoad();
    }

    //Load stuff in a background stuff, what we're doing before
    @Override
    public ListWrapper loadInBackground() {
        return new ListWrapper(mHelper.queryMarkers(), mHelper.queryTrails());
    }

    @Override
    public void deliverResult(ListWrapper data) {
        trails = data.trails; //cache data
        markers = data.markers;

        super.deliverResult(data);
    }
}
