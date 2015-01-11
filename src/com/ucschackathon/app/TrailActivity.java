package com.ucschackathon.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

import android.view.MenuItem;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.*;

/**
 * This is the mobile application for Android developed by Team Trail Hackers for the HACK UCSC 2015 Competition
 * (see hackucsc.com)
 */

public class TrailActivity extends Activity {
	private static final LatLng[] WATSONVILLE = {new LatLng(36.9154033, -121.7694327),
			new LatLng(36.911, -121.803),
			new LatLng(36.9016682,-121.7845458),
			new LatLng(36.913525, -121.780813),
			new LatLng(36.911101, -121.776457),
			new LatLng(36.913507, -121.768687),
			new LatLng(36.912601, -121.770290),
			new LatLng(36.9016682,-121.7845458),
	};
	private static final String theKML = "http://www.watsonvillewetlandswatch.org/sloughs/EntireMapWeb.kml";
	private GoogleMap map;
	private MapView mapView;
	private InputStream is;
	private boolean inSatellite;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		map.setMyLocationEnabled(true); //allow a user to center map to their location
		map.getUiSettings().setZoomControlsEnabled(true); //allow zoom controls in this app

		Marker watsonville = map.addMarker(new MarkerOptions().position(WATSONVILLE[0])
				.title("Watsonville"));

		// Move the camera instantly to Watsonville with a zoom of 14.
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(WATSONVILLE[0], 14));

		//Draw shapes onto the map
		CircleOptions circleOptions = new CircleOptions()
				.center(WATSONVILLE[0])
				.radius(100)
				.fillColor(0x40ff0000)  //semi-transparent
				.strokeColor(Color.BLUE)
				.strokeWidth(5);

		// Get back the mutable Circle
		Circle circle = map.addCircle(circleOptions);

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

	public void showMarkers() {
		Marker[] markers = {
				map.addMarker(new MarkerOptions().position(WATSONVILLE[1]).title("Nest of Osprey")
				.snippet("Human beings have left their mark on the Watsonville Wetlands for thousands of years, " +
						"however, the pace of the transformations resulting from human contact has greatly " +
						"accelerated in the past 200 years. The first peoples, the Calendaruc and other " +
						"Ohlone tribes were hunters and gatherers. They left their mark on the land by " +
						"setting fire to the land after harvesting seeds in autumn thereby discourageing " +
						"the growth of large woody plants promoted regrowth of the perennial grasses and " +
						"other plants they used. (Watsonville Wetlands Watch, Wikipedia)")),
				map.addMarker(new MarkerOptions().position(WATSONVILLE[2]).title("In the 1800s")
				.snippet("")),
				map.addMarker(new MarkerOptions().position(WATSONVILLE[3]).title("American White Pelican")
				.snippet("")),
				map.addMarker(new MarkerOptions().position(WATSONVILLE[4]).title("Tarplant")
				.snippet("")),
				map.addMarker(new MarkerOptions().position(WATSONVILLE[5]).title("Wetland restoration")
				.snippet("")),
				map.addMarker(new MarkerOptions().position(WATSONVILLE[6]).title("Cattails")
				.snippet("This plant is one of the most common plants growing in freshwater wetlands.\n " +
						"It has long, flat light-green leaves and creeping roots. The flowers are at the\n " +
						"end of a long stalk that looks like a hot dog on a stick. The fuzzy down that hangs\n" +
						" on the cattails carries the seeds in the wind.\n " +
						"Cattails grow in fresh water in every finger of the Watsonville Slough system.\n")),
				map.addMarker(new MarkerOptions().position(WATSONVILLE[7]).title("Ohlone Indian")
				.snippet("")),
		};
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) { //Code to create menu
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.satellite_enabled: //Enable Satellite mode or disable
				if(inSatellite) {
					map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
					inSatellite = false;
				}
				else {
					map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
					inSatellite = true;
				}
				return true;
			case R.id.showMarkers:
				showMarkers();
				return true;
			case R.id.about: //Show about screen
				Intent intent = new Intent(this, AboutActivity.class);
				startActivity(intent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
