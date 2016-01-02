package com.ucschackathon.app;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.google.android.gms.maps.model.PolylineOptions;

public class TrailParser extends HandlerThread {
    private static final int TRAIL_PARSED = 0;
    public static final String TAG = "TrailParser";

    Handler mHandler, mResponseHandler;
    Listener mListener;
    TrailDatabaseHelper myHelper;

    public interface Listener {
        void onDataSaved(PolylineOptions theTrail);
    }

    public TrailParser(Handler h, TrailDatabaseHelper helper) {
        super(TAG);
        mResponseHandler = h;
        myHelper = helper;
    }

    public void setListener(Listener l) {
        mListener = l;
    }

    //Called from onPostExecute after a trail object is made to send a message to start the save
    public synchronized void saveTrail(Trail t) {
        mHandler.obtainMessage(TRAIL_PARSED, t).sendToTarget();
    }

    public void clearQueue() {
        mHandler.removeMessages(TRAIL_PARSED);
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected synchronized void onLooperPrepared() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == TRAIL_PARSED && msg.obj instanceof Trail)
                    handleRequest((Trail) msg.obj);
            }
        };
    }

    private synchronized void handleRequest(final Trail t) {
        mResponseHandler.post(new Runnable() {
            @Override
            public synchronized void run() {
                //Form polyline ops, save trail
                myHelper.insertTrail(t);
                PolylineOptions ops = new PolylineOptions().addAll(t.getTrailCoords()).color(t.getColor()).width(TrailActivity.LINE_WIDTH);

                //Give this back to the main UI thread?
                mListener.onDataSaved(ops);
            }
        });
    }
}