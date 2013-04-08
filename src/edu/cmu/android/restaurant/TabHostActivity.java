package edu.cmu.android.restaurant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import org.w3c.dom.Document;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.Menu;
import android.view.inputmethod.InputMethodManager;
import android.widget.TabHost;

import com.google.android.maps.GeoPoint;

import edu.cmu.android.restaurant.listeners.OnDetailRequestedListener;
import edu.cmu.android.restaurant.listeners.OnMapInfoRequestedListener;
import edu.cmu.android.restaurant.listeners.OnSearchEnteredListener;
import edu.cmu.android.restaurant.models.Deal;
import edu.cmu.android.restaurant.models.Restaurant;
import edu.cmu.android.restaurant.utils.DomParser;
import edu.cmu.android.restaurant.utils.JsonParser;
import edu.cmu.android.restaurant.utils.Utility;
import edu.cmu.android.restaurant.utils.XMLParseException;

/**
 * The hosting activity of the main application. It implements
 * OnDetailRequestedListener, OnMapInfoRequestedListener,
 * OnSearchEnteredListener to communicate with all the fragments hosted inside
 * it.
 * 
 * @author Shenghao Huang
 * 
 */
public class TabHostActivity extends FragmentActivity implements
		OnDetailRequestedListener, OnMapInfoRequestedListener,
		OnSearchEnteredListener {
	private static final String TAG = "TabHostActivity";
	public static final int SEARCHFRAG = 0, MAPFRAG = 1, LISTFRAG = 2,
			DETAILFRAG = 3;
	private static final Location DEFAULTLOCATION = Utility
			.geoPoint2Location(new GeoPoint(40444630, -79945500));
	private FragmentTabHost mTabHost;
	private LocationManager lMngr;
	private Geocoder geocoder;
	private InputMethodManager imm;
	private InnerLocListener locListener;

	private Location currentCenter;
	private final String CENTER_KEY = "currentCenter";
	// this is not saved in the bundle, always need to be generated
	private ArrayList<Restaurant> currentList;
	private Restaurant currentRestaurant;
	private ArrayList<Deal> currentDeals;
	private Thread waitingThd;

	/**
	 * Overrides activity's on create method
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initializeLocation();
		if (savedInstanceState == null) {
			// the app just started running
			currentCenter = null;
			currentRestaurant = null;
			currentList = new ArrayList<Restaurant>();
			initializeRestaurants(true);
		} else {
			currentCenter = (Location) savedInstanceState
					.getParcelable(CENTER_KEY);
			initializeRestaurants(false);

		}
		Log.i(TAG, "OnCreat called");
		setContentView(R.layout.activity_tab_host);

		imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		// error checking
		if (imm == null)
			throw new AssertionError(
					"InputMethodService not present, cannot happen");

		// adding tab
		Log.d(TAG, "Adding tabs to the tab host");
		mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
		mTabHost.addTab(mTabHost.newTabSpec("search").setIndicator("Search"),
				SearchFragment.class, null);
		mTabHost.addTab(mTabHost.newTabSpec("map").setIndicator("Map"),
				MapFragment.class, null);
		mTabHost.addTab(mTabHost.newTabSpec("list").setIndicator("List"),
				RestaurantListFragment.class, null);
		mTabHost.addTab(mTabHost.newTabSpec("detail").setIndicator("Detail"),
				DetailFragment.class, null);
		Log.d(TAG, "Successfully added tabs");
		mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {

			@Override
			public void onTabChanged(String tabId) {
				Log.i(TAG, "TAB switched, ID: " + tabId);
				final String key = "search";
				if (!tabId.equalsIgnoreCase(key)) {
					imm.hideSoftInputFromWindow(mTabHost.getWindowToken(),
							InputMethodManager.HIDE_IMPLICIT_ONLY);
				}
			}
		});
		Log.i(TAG, "onCreated finished");
	}

	/**
	 * Initialize the location manager
	 */
	private void initializeLocation() {
		Log.i(TAG,
				"initialize location manager and default location on location listener");
		// initialize location manager
		try {
			lMngr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		} catch (ClassCastException e) {
			Utility.simpleDialog(this, R.string.warning,
					"Cannot get Location service");
			Log.e(TAG, "Failed on getting system wide service", e);
		}

		// init location listener
		initializeLocationListener();

		// Geocoder

		if (!Geocoder.isPresent()) {
			Log.e(TAG,
					"Terribly wrong... Geocoder doesn't exist, set it back to null");
			Utility.simpleDialog(this, R.string.warning,
					"Geocoder doesn't exist");
			geocoder = null; // set it back to null
		} else {
			geocoder = new Geocoder(getBaseContext(), Locale.ENGLISH);
		}
		Log.i(TAG, "Initialize location finished");

	}

	/**
	 * Initialize the location listener
	 */
	private void initializeLocationListener() {
		// Get last known postion at initialization and use it to initialize the
		// listener, better than nothing
		Log.d(TAG,
				"Getting last known position and initialize location listener");
		Criteria criteria = new Criteria();
		String best = lMngr.getBestProvider(criteria, false);
		// Location listener
		Location lastKnown = lMngr.getLastKnownLocation(best);
		if (lastKnown == null) {
			Log.e(TAG, "No last known location, use default location");
			lastKnown = DEFAULTLOCATION;
		} else {
			Log.d(TAG, "Last known loacation: " + lastKnown);
		}
		locListener = new InnerLocListener(lastKnown);
	}

	private void initializeRestaurants(boolean updateCenter) {
		if (updateCenter)
			updateCenterLocation(null); // update using the last known position
		new SearchPOITask().execute(true, false);

	}

	/**
	 * Hide keyboard helper method
	 */

	private void hideKeyboard() {
		// hide the key board from search

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// getMenuInflater().inflate(R.menu.activity_tab_host, menu);
		return false;
	}

	/**
	 * Everytime on resume, request location updates from best provider that is
	 */
	@Override
	public void onResume() {
		super.onResume();
		registerLocListener();
	}

	/**
	 * When pause, unregister the listener from the provider to conserve some
	 * power
	 */
	@Override
	public void onPause() {
		super.onPause();
		unregisterLocListener();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(CENTER_KEY, currentCenter);
	}

	/**
	 * Helper method to switch to search tab
	 */
	private void switchToSearchTab() {
		hideProgress();
		mTabHost.setCurrentTab(SEARCHFRAG);
	}

	/**
	 * Helper method to switch to map tab
	 */
	private void switchToMapTab() {

		hideProgress();
		mTabHost.setCurrentTab(MAPFRAG);
	}

	/**
	 * Helper method to switch to List tab
	 */
	private void switchToListTab() {
		hideProgress();
		mTabHost.setCurrentTab(LISTFRAG);
	}

	/**
	 * Helper method to switch to Detail tab
	 */
	private void switchToDetailTab() {

		hideProgress();
		mTabHost.setCurrentTab(DETAILFRAG);
	}

	/**
	 * Unregister location listener
	 */
	private void unregisterLocListener() {
		Log.i(TAG, "Unregister listener from Location manager");
		// check if location listener is initialized:
		if (locListener == null || lMngr == null) {
			Log.e(TAG,
					"Terriblly wrong.. Location Listener / Manager supposed to be initialize",
					new NullPointerException());
			return;
		}
		lMngr.removeUpdates(locListener);
		Log.i(TAG, "Successfully unregistered listener");
	}

	/**
	 * Helper method, pick a best provide and bind the location listener to it
	 */
	private void registerLocListener() {
		Log.i(TAG, "Registering location listener to the location manager");

		// check if location listener is initialized:
		if (locListener == null || lMngr == null) {
			Log.e(TAG,
					"Terriblly wrong.. Location Listener / Manager supposed to be initialize",
					new NullPointerException());
			return;
		}
		Criteria criteria = new Criteria();
		String best = lMngr.getBestProvider(criteria, true);
		long minTime = 30000; // updates every half minute
		float minDistance = 10F;
		lMngr.requestLocationUpdates(best, minTime, minDistance, locListener);
		Log.i(TAG, "Location listener registered");
	}

	/**
	 * Helper method to updatePOIList
	 * 
	 * @param lst
	 *            the new list
	 */
	private void updatePOIList(boolean switchTab, ArrayList<Restaurant> lst) {
		Log.i(TAG, "Restaurant list update..");
		/*
		 * Error handling in updating the restaurant lists
		 */
		if (lst == null) {
			hideProgress();
			Utility.simpleDialog(this, R.string.warning,
					"Error occured in find nearby restaurants");
			return;
		}
		currentList = lst;
		// check if it is called during onCreated
		// if not, switch to the Map tab
		if (switchTab)
			switchToMapTab();
		// if currentRestaurant is not there and currentList is not empty
		if (currentRestaurant == null && currentList.size() > 0) {
			Restaurant that = currentList.get(0);
			new FillInDetailTask().execute(that, false, false);
			new FindDealTask().execute(that);
		} else
			Log.d(TAG, "Current restaurant not updated with the list: "
					+ (currentList.size() == 0 ? "it is already set"
							: "list is empty"));
	}

	/**
	 * Update the stored current restaurant
	 * 
	 * @param switchTab
	 *            whether to swtich tab
	 * @param tar
	 *            the restaurant to choose
	 */
	private void updateCurrentRestaurant(boolean switchTab, Restaurant tar) {
		Log.i(TAG, "Current Restaurant update");
		currentRestaurant = tar;
		if (switchTab) {
			switchToDetailTab();
		}

	}

	// ======================== inner class definition
	// =============================

	/**
	 * InnerLocListener listen to the location change and updates the current
	 * location accordingly
	 * 
	 * @author Shenghao Huang
	 * 
	 */
	private class InnerLocListener implements LocationListener {
		private Location currentLoc;

		/**
		 * Constructor, takes the current location
		 * 
		 * @param loc
		 */
		public InnerLocListener(Location loc) {
			currentLoc = loc;
		}

		/**
		 * Getter of the current location
		 * 
		 * @return
		 */
		public Location getCurrentLocation() {
			return currentLoc;
		}

		/**
		 * Updates the currentLoc.
		 */
		@Override
		public void onLocationChanged(Location arg0) {
			Log.d(TAG, "Location changed: " + arg0);
			currentLoc = arg0;

		}

		@Override
		public void onProviderDisabled(String provider) {
			// do nothing

		}

		@Override
		public void onProviderEnabled(String provider) {
			// do nothing

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// do nothing

		}

	}

	// ======================= implementation for all listeners
	// ========================
	/**
	 * When the search query is entered
	 */
	@Override
	public void onSearchEntered(String query) {
		Log.i(TAG, "Entered search stirng: " + query);
		if (query == null || query.equals("")) {
			// search the current location, check if location listener is there
			// if not, initialize it, otherwise do nothing
			if (locListener == null)
				initializeLocationListener();
			updateCenterLocation(null); // update the center location with
										// current location
			/*
			 * Search the restaurant near current location. This location is
			 * from the GPS sensor, so sensor=true Also, we want to switch to
			 * the map tab
			 */
			new SearchPOITask().execute(true, true);

		} else {
			// execute the async task
			new GeocodingTask().execute(query);
		}

	}

	@Override
	public ArrayList<Restaurant> onMapInfoRequested() {
		return currentList;
	}

	@Override
	public Restaurant onRestaurantRequested() {
		return currentRestaurant;
	}

	@Override
	public ArrayList<Deal> onDealsRequested() {
		return currentDeals;
	}

	/**
	 * Called when a restaurant is selected
	 */
	@Override
	public void onRestaurantSelected(Restaurant restaurant) {
		if (restaurant != null) {
			new FillInDetailTask().execute(restaurant, false, true);
			new FindDealTask().execute(restaurant);
		}

	}

	@Override
	public GeoPoint onCenterPointRequested() {
		return Utility.location2GeoPoint(currentCenter);
	}

	// ========================= end listener section
	// ====================================

	// helper to update the current location
	private void updateCenterLocation(Location loc) {
		if (loc == null) {
			loc = locListener.getCurrentLocation();
		}
		if (loc == null) {
			Log.e(TAG, "current location should at least have a default value",
					new NullPointerException());
		} else {
			currentCenter = loc;
		}
	}

	/**
	 * Update the current deal list
	 * 
	 * @param lst
	 */
	private void updateCurrentDealsList(ArrayList<Deal> lst) {
		currentDeals = lst;
	}

	// ======================= our async tasks
	// ========================================
	/**
	 * Async task to do geocoding
	 * 
	 */
	private class GeocodingTask extends AsyncTask<String, Void, Address> {

		@Override
		protected Address doInBackground(String... args) {
			// only accept the first string
			if (args != null && args[0] != null && !args[0].equals("")) {
				try {
					// return only one address (the closest one)
					Log.i(TAG, "Doing geocoding for: " + args[0]);
					return geocoder.getFromLocationName(args[0], 1).get(0);
				} catch (IOException e) {
					Log.e(TAG, "Failed when doing geoencoding", e);

				} catch (IndexOutOfBoundsException e) {
					Log.e(TAG, "No result encoding " + args[0], e);
					// TODO better have the user try again here
				}
			} else {
				Log.e(TAG, "No Input for geo encoding");

			}
			return null;
		}

		/**
		 * Override it, to be run after the do in background finished
		 */
		@Override
		protected void onPostExecute(Address address) {
			if (address != null) {
				// if finished, update and store the center location
				updateCenterLocation(Utility.address2Location(address));
				// start new SearchPOITask, update the restaurant list
				// source is from geocoding, sensor =false
				// We want to switch to map tab
				new SearchPOITask().execute(false, true);
			}
		}

	}

	/**
	 * 
	 * AsyncTask to search the nearby place around a center point
	 * 
	 */
	private class SearchPOITask extends
			AsyncTask<Boolean, Void, ArrayList<Restaurant>> {
		/*
		 * Configuration that can be externalized to a config file
		 */
		private final int radius = 1000; // 1000 meter as demonstration purpose
		private boolean switchTab;

		@Override
		protected ArrayList<Restaurant> doInBackground(Boolean... params) {
			Location center = currentCenter;
			if (params.length != 2)
				return null;
			boolean sensor = false;
			try {
				sensor = params[0];
				switchTab = params[1];
			} catch (Exception e) {
				Log.e(TAG, "Not possible", e);
			}
			Log.i(TAG, String.format(
					"Searching restaurant around %s, with sensor = %b", center,
					sensor));
			if (center != null) {
				String url = Utility.formatFoodPOIUrl(currentCenter, radius,
						sensor);
				Document dom = Utility.getRemoteXML(url);
				ArrayList<Restaurant> lst = null;
				try {
					lst = DomParser.getInstance().parsePOIList(dom);
				} catch (XMLParseException e) {
					Log.e(TAG,
							"NearBy: the XML got from the google place api cannot be parsed",
							e);
				}
				return lst;
			} else {
				Log.e(TAG, "Geocoding: No center was provided yet",
						new NullPointerException());
				return null;
				// TODO self healing mechanism here
			}

		}

		@Override
		protected void onPostExecute(ArrayList<Restaurant> ar) {
			updatePOIList(switchTab, ar);
		}

	}

	/**
	 * AsyncTask to fill in additional detail to the Restaurant object
	 * 
	 * Argument of execute: 1st - Restaurant object to be filled in detail 2nd -
	 * 
	 * 
	 */
	private class FillInDetailTask extends AsyncTask<Object, Void, Restaurant> {
		/*
		 * Configuration that can be externalized to a config file
		 */
		private boolean switchTab;

		@Override
		protected Restaurant doInBackground(Object... params) {
			// cast for arguments
			if (params.length != 3)
				return null; // check # of args
			Restaurant target = null;
			boolean sensor = false;
			try {
				target = (Restaurant) params[0];
				sensor = (Boolean) params[1];
				switchTab = (Boolean) params[2];
			} catch (ClassCastException e) {
				Log.e(TAG, "Parameter passed to FillInDetailTask is not valid",
						e);
				return null;
			}
			Log.i(TAG, String.format(
					"Fill in Restaurant detail of %s, with sensor=%b", target,
					sensor));
			if (target != null) {
				String url = Utility.formatDetailUrl(target.getReference(),
						sensor);
				Document dom = Utility.getRemoteXML(url);
				try {
					DomParser.getInstance().fillRestaurantDetail(target, dom);
				} catch (XMLParseException e) {
					Log.e(TAG,
							"Detail: the XML got from the google place api cannot be parsed",
							e);
				}
				return target;
			} else {
				Log.e(TAG, "Fill in Restaurant Detail: No Restaurant given",
						new NullPointerException());
				return null;
				// TODO self healing mechanism here
			}

		}

		@Override
		protected void onPostExecute(Restaurant tar) {
			if (tar != null) {
				updateCurrentRestaurant(switchTab, tar);

			}
		}

	}

	private class FindDealTask extends
			AsyncTask<Restaurant, Void, ArrayList<Deal>> {

		@Override
		protected ArrayList<Deal> doInBackground(Restaurant... arg0) {
			if (arg0.length != 1)
				return null;
			Restaurant tar = arg0[0];
			String url = Utility.formatDealUrl(tar);
			ArrayList<Deal> ret = JsonParser.getInstance().getDeals(url);
			return ret;
		}

		@Override
		protected void onPostExecute(ArrayList<Deal> lst) {
			if (lst != null) {
				updateCurrentDealsList(lst);

			}
		}

	}

	// === end async tasks ====
	/**
	 * Shows the progress UI for a lengthy operation.
	 */
	public void showProgress(Runnable toRun) {
		if (toRun != null) {
			waitingThd = new Thread(toRun);
			waitingThd.start();
		}
		showDialog(0);
	}

	/**
	 * Hides the progress UI for a lengthy operation.
	 */
	public void hideProgress() {
		if (waitingThd != null) {
			waitingThd = null;
		}
		dismissDialog(0);
	}

	/*
	 * {@inheritDoc}
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		final ProgressDialog dialog = new ProgressDialog(this);
		dialog.setMessage(getText(R.string.tabhost_activity_loading));
		dialog.setIndeterminate(true);
		dialog.setCancelable(true);
		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				Log.i(TAG, "dialog cancel has been invoked");
				if (waitingThd != null) {
					waitingThd.interrupt();
				}
			}
		});
		return dialog;
	}

}
