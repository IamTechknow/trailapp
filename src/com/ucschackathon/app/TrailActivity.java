package com.ucschackathon.app;

import android.support.v7.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
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

public class TrailActivity extends AppCompatActivity {
	private static final LatLng[] WATSONVILLE = {
			new LatLng(36.911, -121.803),
			new LatLng(36.905060, -121.785410),
			new LatLng(36.913525, -121.780813),
			new LatLng(36.911101, -121.776457),
			new LatLng(36.913507, -121.768687),
			new LatLng(36.913690, -121.770600),
			new LatLng(36.9016682,-121.7845458),
	};
	private GoogleMap mMap;
	private boolean mInSatellite;
	private Toolbar mToolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		//Set up action bar
		mToolbar = (Toolbar) findViewById(R.id.tool_bar);
		setSupportActionBar(mToolbar);
		//set up the map
		getFragmentManager().findFragmentById(R.id.map).setRetainInstance(true); //allow map to survive screen rotation
		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		mMap.setMyLocationEnabled(true); //allow a user to center map to their location
		mMap.getUiSettings().setZoomControlsEnabled(true); //allow zoom controls in this app

		// Move the camera instantly to Watsonville with a zoom of 14.
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(WATSONVILLE[3], 14));
	}

	public void showMarkers() {
		Marker[] markers = {
				mMap.addMarker(new MarkerOptions().position(WATSONVILLE[0]).title("Nest of Osprey")),
				mMap.addMarker(new MarkerOptions().position(WATSONVILLE[1]).title("DFG Outlook")),
				mMap.addMarker(new MarkerOptions().position(WATSONVILLE[2]).title("Struve Slough")),
				mMap.addMarker(new MarkerOptions().position(WATSONVILLE[3]).title("Tarplant Hill")),
				mMap.addMarker(new MarkerOptions().position(WATSONVILLE[4]).title("Wetland restoration")),
				mMap.addMarker(new MarkerOptions().position(WATSONVILLE[5]).title("Nature Center")),
				mMap.addMarker(new MarkerOptions().position(WATSONVILLE[6]).title("Harkins Slough")),
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
				if(mInSatellite) {
					mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
					mInSatellite = false;
				}
				else {
					mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
					mInSatellite = true;
				}
				return true;
			case R.id.showMarkers:
				showMarkers();
				return true;
			case R.id.showTrails:
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
		Document doc;
		ConnectivityManager connMgr = (ConnectivityManager)
				getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected())
			doc = new TrailData().execute().get(); //Need to use ASyncTask class cannot do this on main UI thread
		else //Let the user know there's no internet access
			Toast.makeText(this, R.string.noconnectionID,Toast.LENGTH_SHORT).show();
	}

	private class TrailData extends AsyncTask<Void, Void, Document> {
		private Document document;
		private static final String TAG = "TrailData";
		private static final String theKML = "http://www.watsonvillewetlandswatch.org/sloughs/EntireMapWeb.kml";

		@Override
		protected Document doInBackground(Void... params) {
			try {
				HttpURLConnection conn = (HttpURLConnection) new URL(theKML).openConnection();
				conn.setReadTimeout(500); // milliseconds
				conn.setConnectTimeout(1000); // milliseconds. If you get null object exceptions, ensure socket has not time out
				conn.setRequestMethod("GET");
				conn.setDoInput(true);
				// Starts the query to get the KML
				conn.connect();
				int response = conn.getResponseCode();
				Log.d(TAG, "The response is: " + response);
				InputStream inputStream = conn.getInputStream();
				//Parse the KML input stream
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
			if (result.getElementsByTagName("coordinates").getLength() >= 0) {
				//Get the path data to parse
				NodeList coordinates = result.getElementsByTagName("coordinates");
				int index = 0;
				ArrayList<LatLng> coords = new ArrayList<LatLng>();

				for (;index < 2; index++) { //Place Markers for Parking
					String coord = coordinates.item(index).getFirstChild().getNodeValue();
					placeMarkers(coord, coords, "Parking");
				}

				for (index = 8;index < 42; index++) { //Markers for trail entrances
					String coord = coordinates.item(index).getFirstChild().getNodeValue();
					placeMarkers(coord, coords, "Trail Entrance");
				}

				for(index = 42; index < 48; index++) { //Markers for restrooms
					String coord = coordinates.item(index).getFirstChild().getNodeValue();
					placeMarkers(coord, coords, "Restrooms");
				}

				for(index = 48; index < 113; index++) { //Draw all the trails!
					String path = coordinates.item(index).getFirstChild().getNodeValue();
					placeMarkers(path,coords,null); //Not displaying markers

					//Now we can draw the polyline!
					int c;
					switch(index) { //Draw trails with right color
						case 62: case 64: case 74: case 75: case 76: case 77: case 78:
						case 86: case 87: case 110: //Watsonville Slough Trails
							c = Color.GREEN;
							break;
						case 112: //Watsonville Slough
							c = Color.BLUE;
							break;
						default:
							c = Color.RED;
							break;
					}
					PolylineOptions ops = new PolylineOptions().addAll(coords).color(c);
					Polyline line = mMap.addPolyline(ops);
					line.setWidth(5.0F);
				}
			}
		}
		//Parse the coordinates and fill the arraylist. If necessary, insert markers here.
		private void placeMarkers (String path, ArrayList<LatLng> coords, String iconName) {
			coords.clear();
			String[] lngLat = path.split(","); //split the coordinates by a comma to get individual coordinates
			for (int i = 0; i < lngLat.length - 2; i = i + 2) { //lat actually comes second
				String lat = lngLat[i + 1], lng = lngLat[i].substring(lngLat[i].indexOf('-'));
				//We can obtain the coordinates by doing some simple String operations and parsing the strings to numbers
				LatLng obj = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
				coords.add(obj);
			}

			for(LatLng l: coords)  //add the markers using the trail access icon found in the organization's website
				if(iconName != null)
					if(iconName.compareTo("Parking") == 0) {
						Marker m = mMap.addMarker(new MarkerOptions().position(l).title("Parking").icon(BitmapDescriptorFactory.fromResource(R.drawable.sloughtrailparking)));
					} else if(iconName.compareTo("Trail Entrance") == 0) {
						Marker m = mMap.addMarker(new MarkerOptions().position(l).title("Parking").icon(BitmapDescriptorFactory.fromResource(R.drawable.sloughtrailentrances)));
					} else if(iconName.compareTo("Restrooms") == 0) {
						Marker m = mMap.addMarker(new MarkerOptions().position(l).title("Parking").icon(BitmapDescriptorFactory.fromResource(R.drawable.bathrooms)));
					}
		}
	}
}
