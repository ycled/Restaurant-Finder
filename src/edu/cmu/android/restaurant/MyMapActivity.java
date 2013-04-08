package edu.cmu.android.restaurant;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import edu.cmu.android.restaurant.listeners.OnMapInfoRequestedListener;
import edu.cmu.android.restaurant.models.Restaurant;

public class MyMapActivity extends MapActivity {

	private int zoomlevel = 14;
	private String TAG = "map";
	MapView mapView;
	MapController control;
	private ArrayList<Restaurant> getRestaurant;
	private OnMapInfoRequestedListener mListener;

	private Handler mHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);

		mHandler = new Handler();
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);

		control = mapView.getController();

	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	public void registerListener(OnMapInfoRequestedListener lst) {

		mListener = lst;
		control.setCenter(lst.onCenterPointRequested());
		control.setZoom(zoomlevel);

		getRestaurant = mListener.onMapInfoRequested();
		if (getRestaurant.size() != 0) {
			updateOverlay();
		} else {
			showProgressPull();
		}

	}

	public void updateOverlay() {

		List<Overlay> mapOverlays = mapView.getOverlays();
		Drawable drawable = this.getResources().getDrawable(
				R.drawable.direction11);
		HelloItemizedOverlay itemizedoverlay = new HelloItemizedOverlay(
				drawable, this, mListener);

		for (int i = 0; i < getRestaurant.size(); i++) {

			RestaurantItem overlayitem = new RestaurantItem(
					getRestaurant.get(i));
			itemizedoverlay.addOverlay(overlayitem);
		}
		mapOverlays.add(itemizedoverlay);
	}

	/**
	 * Shows the progress UI for a lengthy operation.
	 */
	public void showProgressPull() {

		Runnable waitTask = new Runnable() {
			final long interval = 500;

			@Override
			public void run() {
				Log.i(TAG, "Enter into the loop, waiting for return and update");
				try {
					ArrayList<Restaurant> ret = null;
					while ((ret = mListener.onMapInfoRequested()).size() == 0) {
						Thread.sleep(interval);
					}
					getRestaurant = ret;
					/*
					 * Post the update event to the UI thread
					 */
					mHandler.post(new Runnable() {

						@Override
						public void run() {
							updateOverlay();

						}

					});
					hideProgress();

				} catch (InterruptedException e) {
					Log.w(TAG, "Interrupted during loading");
				}

			}

		};
		((TabHostActivity) getActivity()).showProgress(waitTask);
	}
	
	
	/**
	 * Hides the progress UI for a lengthy operation.
	 */
	protected void hideProgress() {
		((TabHostActivity) getActivity()).hideProgress();

	}

	/**
	 * Helper that cast the activity
	 * 
	 * @return
	 */
	private Activity getActivity() {
		try {
			return (Activity) mListener;
		} catch (ClassCastException e) {
			Log.e(TAG, "Error casting tab hosting activity", e);
			throw new AssertionError("Cannot happen: " + e.getMessage());
		}
	}
}
