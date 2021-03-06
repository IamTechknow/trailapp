package com.ucschackathon.app;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.kml.KmlLayer;
import com.ucschackathon.app.model.ListWrapper;
import com.ucschackathon.app.model.Trail;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
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
	public static final float LINE_WIDTH = 5.0F;
	private static final LatLng CENTER = new LatLng(36.911101, -121.776457);

	private DrawerLayout mDrawerLayout;
	private CoordinatorLayout mCoordinatorLayout;
	private GoogleMap mMap;
	private boolean mInSatellite, mHaveTrailDB;
	private KmlLayer mKmlLayer; //to show or hide KML layer
	private TrailDatabaseHelper mHelper;
	private SharedPreferences mPrefs;

	//When the map is ready, save and configure it
	private OnMapReadyCallback mMapReadyCallback = new OnMapReadyCallback() {
		@Override
		public void onMapReady(GoogleMap googleMap) {
			mMap = googleMap;

			// Move the camera instantly to Watsonville with a zoom of 14.
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CENTER, 14));

			if(mHaveTrailDB) //load data asynchronously
				getLoaderManager().initLoader(0, null, mLoaderCallbacks);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		//Set up toolbar bar and layout
		mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.thelayout);
		Toolbar mToolbar = (Toolbar) findViewById(R.id.tool_bar);
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
						mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(CENTER, 14));
						showTrails();
						break;
					case R.id.showLayerNav:
						//We could recycle mKmlLayer but not all data (the text overlays) is preserved
						mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(CENTER, 14));
						showTrailsAlt();
						break;
					case R.id.listMarkersNav:
						if(mHaveTrailDB)
							startActivity(new Intent(getApplicationContext(), ListMarkersActivity.class));
						else
							Snackbar.make(mCoordinatorLayout, R.string.needTrailData, Snackbar.LENGTH_SHORT).show();
						break;
					case R.id.satellite_enabled_nav:
						toggleMapLayer();
						break;
					case R.id.aboutNav:
						startActivity(new Intent(getApplicationContext(), AboutActivity.class));
						break;
					default:
						break;
				}
				mDrawerLayout.closeDrawers(); //always close drawer when option is selected
				return true;
			}
		});

		//Setup DB, Determine if we have init the trail database by checking key-value pair flag
		mHelper = new TrailDatabaseHelper(this);
		mPrefs = getSharedPreferences(PREFS_FILE, MODE_PRIVATE);
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
			case R.id.showTrails: //Show the trails by parsing them
					showTrails();
				return true;
			case R.id.showTrailsAlt: //Show the trails by parsing the KML layer with Google Maps utils
				//We could recycle mKmlLayer but not all data (the text overlays) is preserved
				showTrailsAlt();
				return true;
			case R.id.hideData: //Remove all drawn data from the map
				if(mKmlLayer != null)
					mKmlLayer.removeLayerFromMap();
				mMap.clear();
				return true;
			case R.id.about: //Show about screen
				Intent intent = new Intent(this, AboutActivity.class);
				startActivity(intent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private LoaderManager.LoaderCallbacks<ListWrapper> mLoaderCallbacks = new LoaderManager.LoaderCallbacks<ListWrapper>() {
		@Override
		public Loader<ListWrapper> onCreateLoader(int id, Bundle args) {
			return new TrailDataLoader(TrailActivity.this);
		}

		//Show the data onto the map!
		@Override
		public void onLoadFinished(Loader<ListWrapper> loader, ListWrapper lists) {
			for(Trail t: lists.trails)
				mMap.addPolyline(new PolylineOptions().addAll(t.getTrailCoords()).color(t.getColor()).width(LINE_WIDTH));

			for(com.ucschackathon.app.model.Marker m : lists.markers) {
				int resourceID = com.ucschackathon.app.model.Marker.getMarkerIconID(m);
				mMap.addMarker(new MarkerOptions().position(m.getLoc()).title(m.getTitle()).icon(BitmapDescriptorFactory.fromResource(resourceID)));
			}
		}

		@Override
		public void onLoaderReset(Loader<ListWrapper> loader) {

		}
	};

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

	public void showTrails() {
		if(!mHaveTrailDB) {
			//Check Internet Connection
			ConnectivityManager connMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

			if (networkInfo != null && networkInfo.isConnected())
				new TrailData().execute(); //Need to use ASyncTask class cannot do this on main UI thread
			else
				Snackbar.make(mCoordinatorLayout, R.string.noconnectionID, Snackbar.LENGTH_SHORT).show();
		} else
			getLoaderManager().restartLoader(0, null, mLoaderCallbacks);
	}

	public void showTrailsAlt() {
		//Check Internet Connection
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

		if (networkInfo != null && networkInfo.isConnected())
			new TrailLayer().execute();
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

	private class TrailData extends AsyncTask<Void, Void, ListWrapper> {

		@Override
		protected ListWrapper doInBackground(Void... params) {
			try {
				ArrayList<com.ucschackathon.app.model.Marker> markers = new ArrayList<>();
				ArrayList<Trail> trails = new ArrayList<>();
				InputStream inputStream = getKMLasStream();

				//Parse the KML input stream
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document document = db.parse(inputStream);

				NodeList coordinates = document.getElementsByTagName("coordinates");
				if (coordinates.getLength() > 0) {
					//Get the path data to parse. Start with Markers then trails. To get the title, access the Placemark parent node
					//instead of accessing the Folder groups. It is known the first element of a placemark element has the title
					int index = 0;
					for ( ; index < 2; index++) { //Markers for restrooms
						Node coordinate = coordinates.item(index).getFirstChild();
						String coord = coordinate.getNodeValue(), title = coordinate.getParentNode().getParentNode().getParentNode().getChildNodes().item(1).getFirstChild().getNodeValue();

						markers.add(parseMarker(coord, com.ucschackathon.app.model.Marker.RESTROOM, title));
					}

					for (index = 6 ; index < 8; index++) { //Markers for Nature Center
						Node coordinate = coordinates.item(index).getFirstChild();
						String coord = coordinate.getNodeValue(), title = coordinate.getParentNode().getParentNode().getParentNode().getChildNodes().item(1).getFirstChild().getNodeValue();

						markers.add(parseMarker(coord, com.ucschackathon.app.model.Marker.NATURECENTER, title));
					}

					for (index = 8; index < 42; index++) { //Markers for trail entrances
						Node coordinate = coordinates.item(index).getFirstChild();
						String coord = coordinate.getNodeValue(), title = coordinate.getParentNode().getParentNode().getParentNode().getChildNodes().item(1).getFirstChild().getNodeValue();

						markers.add(parseMarker(coord, com.ucschackathon.app.model.Marker.ENTRANCE, title));
					}

					for(index = 42; index < 48; index++) { //Markers for Parking
						Node coordinate = coordinates.item(index).getFirstChild();
						String coord = coordinate.getNodeValue(), title = coordinate.getParentNode().getParentNode().getParentNode().getChildNodes().item(1).getFirstChild().getNodeValue();

						markers.add(parseMarker(coord, com.ucschackathon.app.model.Marker.PARKING, title));
					}

					for(index = 48; index < 113; index++) { //Draw all the trails!
						ArrayList<LatLng> coords = new ArrayList<>();
						String path = coordinates.item(index).getFirstChild().getNodeValue();
						parseTrail(path, coords); //Not displaying markers

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

						Trail t = new Trail(coords, c);
						trails.add(t);
					}

					//Save all data to DB here
					for(Trail t: trails)
						mHelper.insertTrail(t);

					mHelper.insertMarkers(markers);
				} else
					return null;

				return new ListWrapper(markers, trails);
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
		protected void onPostExecute(ListWrapper result) {  // result is data returned by doInBackground
			if (result != null) {
				for(Trail t: result.trails)
					mMap.addPolyline(new PolylineOptions().addAll(t.getTrailCoords()).color(t.getColor()).width(LINE_WIDTH));

				for(com.ucschackathon.app.model.Marker m: result.markers) {
					int resourceID = com.ucschackathon.app.model.Marker.getMarkerIconID(m);
					mMap.addMarker(new MarkerOptions().position(m.getLoc()).title(m.getTitle()).icon(BitmapDescriptorFactory.fromResource(resourceID)));
				}

				//Done! Save onto Preferences that database is set
				mHaveTrailDB = true;
				mPrefs.edit().putBoolean(PREFS_HAVE_TRAIL_DB, mHaveTrailDB).apply();
			} else //NodeList had no coordinate entries. Why?
				Snackbar.make(mCoordinatorLayout, "Data not accessed. Try again", Snackbar.LENGTH_LONG).show();
		}

		private com.ucschackathon.app.model.Marker parseMarker(String path, int type, String title) {
			String[] lngLat = path.split(","); //split the coordinates by a comma to get individual coordinates
			String lat = lngLat[1], lng = lngLat[0].substring(lngLat[0].indexOf('-'));

			return new com.ucschackathon.app.model.Marker(type, title, Double.parseDouble(lat), Double.parseDouble(lng));
		}

		//Parse the coordinates and fill the arraylist
		private void parseTrail(String path, ArrayList<LatLng> coords) {
			coords.clear();
			String[] lngLat = path.split(","); //split the coordinates by a comma to get individual coordinates
			for (int i = 0; i < lngLat.length - 2; i = i + 2) { //lat actually comes second
				String lat = lngLat[i + 1], lng = lngLat[i].substring(lngLat[i].indexOf('-'));
				//We can obtain the coordinates by doing some simple String operations and parsing the strings to numbers
				LatLng obj = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
				coords.add(obj);
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
}
