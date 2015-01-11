package com.ucschackathon.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import android.view.MenuItem;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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
	private GoogleMap map;
	private MapView mapView;
	private InputStream is;
	private boolean inSatellite;
	private Document doc;

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
			case R.id.showTrails:
				Toast.makeText(this,R.string.wait, Toast.LENGTH_SHORT).show();
				try {
					showTrails();
				} catch (ExecutionException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return true;
			case R.id.about: //Show about screen
				Intent intent = new Intent(this, AboutActivity.class);
				startActivity(intent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * All the work to parse the KML data is done in the below method. The Document object is a
	 * DOM presentation of the KML which allows us to use Javascript techniques to navigate through the KML file and
	 * get what we want.
	 */

	public void showTrails() throws ExecutionException, InterruptedException {
		//Check Internet Connection
		ConnectivityManager connMgr = (ConnectivityManager)
				getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			TrailData getURL = (TrailData) new TrailData(); //Need to use ASyncTask class cannot do this on main UI thread
			doc = getURL.execute().get();

		}
	}

	private class TrailData extends AsyncTask<Void, Void, Document> {
		private Exception exception;
		private Document document;
		private static final String TAG = "TrailData";
		private static final String theKML = "http://www.watsonvillewetlandswatch.org/sloughs/EntireMapWeb.kml";

		@Override
		protected Document doInBackground(Void... params) {
			try {
				HttpURLConnection conn = (HttpURLConnection) new URL(theKML).openConnection();
				conn.setReadTimeout(200); // milliseconds
				conn.setConnectTimeout(500); // milliseconds
				conn.setRequestMethod("GET");
				conn.setDoInput(true);
				// Starts the query to get the KML
				conn.connect();
				int response = conn.getResponseCode();
				Log.d(TAG, "The response is: " + response);
				InputStream inputStream = conn.getInputStream();

				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				document = db.parse(inputStream);
				return document;
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
			catch (SAXException e) {
				e.printStackTrace();
			}
			return null;
		}

		protected void onPostExecute(Document result) {  // result is data returned by doInBackground
			CircleOptions circleOptions = new CircleOptions()
					.center(WATSONVILLE[0])
					.radius(200)
					.fillColor(0x40ff0000)  //semi-transparent
					.strokeColor(Color.BLUE)
					.strokeWidth(5);

			// Get back the mutable Circle
			Circle circle = map.addCircle(circleOptions);

			if (result.getElementsByTagName("coordinates").getLength() >= 0) {
				String path = result.getElementsByTagName("coordinates").item(48).getFirstChild().getNodeValue();
				NodeList coordinates = result.getElementsByTagName("coordinates");

				ArrayList<LatLng> coords = new ArrayList<LatLng>();
				String[] lngLat = path.split(",");
				for (int i = 0; i < lngLat.length - 2; i = i + 2) { //lat actually comes second
					String lat = lngLat[i + 1], lng = lngLat[i].substring(lngLat[i].indexOf('-'));
					//We can obtain the coordinates by doing some simple String operations and parsing the strings to numbers
					LatLng obj = new LatLng(Double.parseDouble(lat),Double.parseDouble(lng));
					coords.add(obj);
				}
				//Now we can draw the polyline in red!
				PolylineOptions ops = new PolylineOptions().addAll(coords).color(Color.RED);
				Polyline polyline = map.addPolyline(ops);
			}
		}
	}
}
