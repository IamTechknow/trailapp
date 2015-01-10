package com.ucschackathon.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.*;

public class TrailActivity extends Activity {

	private static final LatLng WATSONVILLE = new LatLng(36.9154033, -121.7694327);
	private static final String theKML = "http://www.watsonvillewetlandswatch.org/sloughs/EntireMapWeb.kml";
	private GoogleMap map;
	private InputStream is;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
				.getMap();
		map.setMapType(GoogleMap.MAP_TYPE_HYBRID); //Enable satellite mode with roads

		Marker watsonville = map.addMarker(new MarkerOptions().position(WATSONVILLE)
				.title("Watsonville"));

		// Move the camera instantly to hamburg with a zoom of 15.
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(WATSONVILLE, 15));

		// Zoom in, animating the camera.
		map.animateCamera(CameraUpdateFactory.zoomTo(16), 2000, null);

		//Draw shapes onto the map
		CircleOptions circleOptions = new CircleOptions()
				.center(WATSONVILLE)
				.radius(100)
				.fillColor(0x40ff0000)  //semi-transparent
				.strokeColor(Color.BLUE)
				.strokeWidth(5);

		// Get back the mutable Circle
		Circle circle = map.addCircle(circleOptions);

		// Instantiates a new Polyline object and adds points to define a rectangle
		PolylineOptions rectOptions = new PolylineOptions()
				.add(new LatLng(37.35, -122.0))
				.add(new LatLng(37.45, -122.0))  // North of the previous point, but at the same longitude
				.add(new LatLng(37.45, -122.2))  // Same latitude, and 30km to the west
				.add(new LatLng(37.35, -122.2))  // Same longitude, and 16km to the south
				.add(new LatLng(37.35, -122.0)); // Closes the polyline.

		// Get back the mutable Polyline
		Polyline polyline = map.addPolyline(rectOptions);


		/*Get access to the KML file by setting up an URL connection

		ConnectivityManager connMgr = (ConnectivityManager)
				getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			try {
				URL url = new URL(theKML);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setReadTimeout(10000 // milliseconds );
				conn.setConnectTimeout(15000 // milliseconds );
				conn.setRequestMethod("GET");
				conn.setDoInput(true);
				// Starts the query
				conn.connect();
				int response = conn.getResponseCode();
				Log.d("TrailActivity", "The response is: " + response);
				is = conn.getInputStream();
				//Code to use the kml file here

			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		else //Let the user know there's no connection
			Toast.makeText(this,R.string.noconnectionID, Toast.LENGTH_SHORT).show(); */
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
