package com.ucschackathon.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.design.widget.Snackbar;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.kml.KmlLayer;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * This is the mobile application for Android developed by Team Trail Hackers for the HACK UCSC 2015 Competition
 * (see hackucsc.com)
 */

public class TrailActivity extends AppCompatActivity {
	private static final String PREFS_FILE = "settings", PREFS_HAVE_TRAIL_DB = "TrailActivity.mHaveTrailDB";

	private static final LatLng[] WATSONVILLE = {
			new LatLng(36.911, -121.803),
			new LatLng(36.905060, -121.785410),
			new LatLng(36.913525, -121.780813),
			new LatLng(36.911101, -121.776457),
			new LatLng(36.913507, -121.768687),
			new LatLng(36.913690, -121.770600),
			new LatLng(36.9016682,-121.7845458),
	};

	private DrawerLayout mDrawerLayout;
	private CoordinatorLayout mCoordinatorLayout;
	private GoogleMap mMap;
	private boolean mInSatellite, mHaveTrailDB;
	private Toolbar mToolbar;
	private KmlLayer mKmlLayer; //to show or hide KML layer
	private Marker[] markers;
	private TrailDatabaseHelper mHelper;
	private SharedPreferences mPrefs;

	//Change the default behaviour of centering the map to the user's location to center over Watsonville
	private GoogleMap.OnMyLocationButtonClickListener mMyLocationListener = new GoogleMap.OnMyLocationButtonClickListener() {
		@Override
		public boolean onMyLocationButtonClick() {
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(WATSONVILLE[3], 14));
			return true;
		}
	};

	//When the map is ready, save and configure it
	private OnMapReadyCallback mMapReadyCallback = new OnMapReadyCallback() {
		@Override
		public void onMapReady(GoogleMap googleMap) {
			mMap = googleMap;
			mMap.setMyLocationEnabled(true); //allow a user to center map to Watsonville
			mMap.setOnMyLocationButtonClickListener(mMyLocationListener);

			// Move the camera instantly to Watsonville with a zoom of 14.
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(WATSONVILLE[3], 14));
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		//Set up toolbar bar and layout
		mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.thelayout);
		mToolbar = (Toolbar) findViewById(R.id.tool_bar);
		setSupportActionBar(mToolbar);

		//set up the map
		getFragmentManager().findFragmentById(R.id.map).setRetainInstance(true); //allow map to survive screen rotation
		((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMapAsync(mMapReadyCallback); //get the map

		//Setup drawer
		// Create Navigation drawer and inflate layout
		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);

		// Adding menu icon to Toolbar
		ActionBar supportActionBar = getSupportActionBar();
		if (supportActionBar != null) {
			supportActionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
			supportActionBar.setDisplayHomeAsUpEnabled(true);
		}

		// Set behavior of Navigation drawer
		navigationView.setNavigationItemSelectedListener( new NavigationView.OnNavigationItemSelectedListener() {
			// This method will trigger on item Click of navigation menu
			@Override
			public boolean onNavigationItemSelected(MenuItem menuItem) {
				switch(menuItem.getItemId()) {
					case R.id.showTrailsNav:
						menuItem.setChecked(true);
						mDrawerLayout.closeDrawers();
						try {
							showTrails();
						} catch (InterruptedException | ExecutionException e) {
							e.printStackTrace();
						}
						break;
					default:
						break;
				}
				return true;
			}
		});

		//Setup DB, Determine if we have init the trail database by checking key-value pair flag
		Context c = getApplicationContext();
		mHelper = new TrailDatabaseHelper(c);
		mPrefs = c.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
		mHaveTrailDB = mPrefs.getBoolean(PREFS_HAVE_TRAIL_DB, false); //return false if it doesn't exist
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
			case android.R.id.home: //Drawer button on toolbar
				mDrawerLayout.openDrawer(GravityCompat.START);
				return true;
			case R.id.satellite_enabled:
				toggleMapLayer();
				return true;
			case R.id.showMarkers:
				showMarkers();
				return true;
			case R.id.showTrails: //Show the trails by parsing them
				try {
					showTrails();
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
				return true;
			case R.id.showTrailsAlt: //Show the trails by parsing the KML layer with Google Maps utils
				try { //We could recycle mKmlLayer but not all data (the text overlays) is preserved
					showTrailsAlt();
				} catch (ExecutionException | InterruptedException e) {
					e.printStackTrace();
				}
				return true;
			case R.id.hideData: //Remove all drawn data from the map
				if(mKmlLayer != null)
					mKmlLayer.removeLayerFromMap();
				mMap.clear();
				markers = null;
				return true;
			case R.id.about: //Show about screen
				Intent intent = new Intent(this, AboutActivity.class);
				startActivity(intent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public void showMarkers() {
		markers = new Marker[]{
			mMap.addMarker(new MarkerOptions().position(WATSONVILLE[0]).title("Nest of Osprey")),
			mMap.addMarker(new MarkerOptions().position(WATSONVILLE[1]).title("DFG Outlook")),
			mMap.addMarker(new MarkerOptions().position(WATSONVILLE[2]).title("Struve Slough")),
			mMap.addMarker(new MarkerOptions().position(WATSONVILLE[3]).title("Tarplant Hill")),
			mMap.addMarker(new MarkerOptions().position(WATSONVILLE[4]).title("Wetland restoration")),
			mMap.addMarker(new MarkerOptions().position(WATSONVILLE[5]).title("Nature Center")),
			mMap.addMarker(new MarkerOptions().position(WATSONVILLE[6]).title("Harkins Slough")),
		};
	}

	public void toggleMapLayer() {
		//Enable Satellite mode or disable and display a snackbar indicating this. If the snackbar Toggle is triggered, function is run again
		if(mInSatellite) {
			mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			mInSatellite = false;
			Snackbar.make(mCoordinatorLayout, R.string.satelliteOff, Snackbar.LENGTH_LONG).setAction(R.string.snackBarToggle, new View.OnClickListener() {
				@Override
				public void onClick(View view) {
				toggleMapLayer();
				}
			}).show();
		}
		else {
			mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
			mInSatellite = true;
			Snackbar.make(mCoordinatorLayout, R.string.satelliteOn, Snackbar.LENGTH_LONG).setAction(R.string.snackBarToggle, new View.OnClickListener() {
				@Override
				public void onClick(View view) {
				toggleMapLayer();
				}
			}).show();
		}
	}

	public void showTrails() throws ExecutionException, InterruptedException {

		if(mHaveTrailDB) {
			setupTrailsFromDB();
		} else {
			//Check Internet Connection
			ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

			if (networkInfo != null && networkInfo.isConnected())
				new TrailData().execute().get(); //Need to use ASyncTask class cannot do this on main UI thread TODO: replace with Handler
			else
				Snackbar.make(mCoordinatorLayout, R.string.noconnectionID, Snackbar.LENGTH_SHORT).show();
		}
	}

	public void showTrailsAlt() throws ExecutionException, InterruptedException {
		//Check Internet Connection
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

		if (networkInfo != null && networkInfo.isConnected())
			new TrailLayer().execute().get();
		else
			Snackbar.make(mCoordinatorLayout, R.string.noconnectionID, Snackbar.LENGTH_SHORT).show();
	}

	/*
		Private method to download the KML file and return it as an IO stream
	 */

	private InputStream getKMLasStream() {
		final String TAG = "TrailData", theKML = "http://www.watsonvillewetlandswatch.org/sloughs/EntireMapWeb.kml";
		InputStream is = null;
		try {
			HttpURLConnection conn = (HttpURLConnection) new URL(theKML).openConnection();
			conn.setReadTimeout(500); // milliseconds
			conn.setConnectTimeout(1000); // milliseconds. If you get null object exceptions, ensure socket has not time out
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			// Starts the query to get the KML
			conn.connect();
			Log.d(TAG, "The response is: " + conn.getResponseCode());
			is = conn.getInputStream();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		return is;
	}

	private class TrailData extends AsyncTask<Void, Void, Document> {
		private Document document;

		@Override
		protected Document doInBackground(Void... params) {
			try {
				InputStream inputStream = getKMLasStream();
				//Parse the KML input stream
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				document = db.parse(inputStream);
				return document;
			}
			catch (IOException | ParserConfigurationException | SAXException e) {
				e.printStackTrace();
			}
			return null;
		}

		/**
		 * All the work to parse the KML data is done in the below method. The Document object is a
		 * DOM presentation of the KML which allows us to use Javascript techniques to navigate through the KML file and
		 * get what we want.
		 */

		@Override
		protected void onPostExecute(Document result) {  // result is data returned by doInBackground
			if (result.getElementsByTagName("coordinates").getLength() >= 0) {
				//Get the path data to parse
				NodeList coordinates = result.getElementsByTagName("coordinates");
				int index = 0;
				ArrayList<LatLng> coords = new ArrayList<>();

				for ( ; index < 2; index++) { //Place Markers for Parking
					String coord = coordinates.item(index).getFirstChild().getNodeValue();
					placeMarkers(coord, coords, "Parking");
				}

				for (index = 8; index < 42; index++) { //Markers for trail entrances
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
					//Create a Trail object to serialize to the database
					Trail t = new Trail(coords, c);
					mHelper.insertTrail(t);

					PolylineOptions ops = new PolylineOptions().addAll(coords).color(c);
					Polyline line = mMap.addPolyline(ops);
					line.setWidth(5.0F);

					//Save onto Preferences that database is init
					mPrefs.edit().putBoolean(PREFS_HAVE_TRAIL_DB, true).apply();
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

			for(LatLng l: coords)  //add the markers using the trail access icon found in the organization's website. also add marker info to the sqlite database
				if(iconName != null)
					if(iconName.compareTo("Parking") == 0) {
						mHelper.insertMarker(TrailDatabaseHelper.MARKER_PARKING, l);
						mMap.addMarker(new MarkerOptions().position(l).title("Parking").icon(BitmapDescriptorFactory.fromResource(R.drawable.sloughtrailparking)));
					} else if(iconName.compareTo("Trail Entrance") == 0) {
						mHelper.insertMarker(TrailDatabaseHelper.MARKER_ENTRANCE, l);
						mMap.addMarker(new MarkerOptions().position(l).title("Trail Entrance").icon(BitmapDescriptorFactory.fromResource(R.drawable.sloughtrailentrances)));
					} else if(iconName.compareTo("Restrooms") == 0) {
						mHelper.insertMarker(TrailDatabaseHelper.MARKER_RESTROOM, l);
						mMap.addMarker(new MarkerOptions().position(l).title("Restroom").icon(BitmapDescriptorFactory.fromResource(R.drawable.bathrooms)));
					}
		}
	}

	/**
		Another AsyncTask object to provide an alternative to viewing the KML geodata by using Google Maps utils
	 */

	private class TrailLayer extends AsyncTask<Void, Void, byte[]> {

		@Override
		protected byte[] doInBackground(Void... params) {
			try {
				InputStream inputStream = getKMLasStream();
				ByteArrayOutputStream buffer = new ByteArrayOutputStream();
				int nRead;
				byte[] data = new byte[16384];
				//Can't use the inputstream in the main thread, so convert it to a byte array
				while ((nRead = inputStream.read(data, 0, data.length)) != -1)
					buffer.write(data, 0, nRead);

				buffer.flush();
				return buffer.toByteArray();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(byte[] byteArr) {  // result is data returned by doInBackground
			try {
				mKmlLayer = new KmlLayer(mMap, new ByteArrayInputStream(byteArr), getApplicationContext());
				mKmlLayer.addLayerToMap();
			} catch (XmlPullParserException | IOException e) {
				e.printStackTrace();
			}
		}
	}

	//For each trail, get its collection of LatLngs then make the polyline. Get its type to set the color
	private void setupTrailsFromDB() {
		ArrayList<Trail> trails = mHelper.queryTrails();
		ArrayList<com.ucschackathon.app.Marker> markers = mHelper.queryMarkers();

		for(Trail t: trails) {
			PolylineOptions ops = new PolylineOptions().addAll(t.getTrailCoords()).color(t.getColor());
			Polyline line = mMap.addPolyline(ops);
			line.setWidth(5.0F);
		}

		for(com.ucschackathon.app.Marker m: markers) {
			String label; int resourceID;

			switch(m.getType()) {
				case TrailDatabaseHelper.MARKER_ENTRANCE:
					label = "Trail Entrance"; resourceID = R.drawable.sloughtrailentrances;
					break;
				case TrailDatabaseHelper.MARKER_PARKING:
					label = "Parking"; resourceID = R.drawable.sloughtrailparking;
					break;
				case TrailDatabaseHelper.MARKER_RESTROOM:
					label = "Restroom"; resourceID = R.drawable.bathrooms;
					break;
				default: //For completeness
					label = "Unknown marker"; resourceID = R.drawable.common_ic_googleplayservices;
					break;
			}
			mMap.addMarker(new MarkerOptions().position(m.getLoc()).title(label).icon(BitmapDescriptorFactory.fromResource(resourceID)));
		}
	}
}
