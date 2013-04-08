package edu.cmu.android.restaurant;

import java.util.ArrayList;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import edu.cmu.android.restaurant.listeners.OnMapInfoRequestedListener;
import edu.cmu.android.restaurant.models.Restaurant;

public class RestaurantListFragment extends ListFragment {

	/*
	 * Listener to communicate with hosting activity and fetch map info
	 */

	/**
	 * On attach call back method. Cast the host activity into
	 * OnMapInfoRequestedListener
	 */
	private static String TAG = "MapFragment";

	/*
	 * Listener to communicate with hosting activity and fetch map info
	 */
	private LocalActivityManager mLocalActivityManager;
	private ArrayList<Restaurant> getRestaurant;
	private OnMapInfoRequestedListener mListener;

	protected LocalActivityManager getLocalActivityManager() {
		return mLocalActivityManager;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		/** Creating an array adapter to store the list of countries **/

		getRestaurant = mListener.onMapInfoRequested();
		String[] countries = new String[getRestaurant.size()];
		for (int i = 0; i < getRestaurant.size(); i++) {
			countries[i] = getRestaurant.get(i).getName();
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				inflater.getContext(), android.R.layout.simple_list_item_1,
				countries);

		/** Setting the list adapter for the ListFragment */
		setListAdapter(adapter);

		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

		// System.out.print(position);
		mListener.onRestaurantSelected(getRestaurant.get(position));
		showProgress();
	}

	/**
	 * On attach call back method. Cast the host activity into
	 * OnMapInfoRequestedListener
	 */

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnMapInfoRequestedListener) activity;
		} catch (ClassCastException e) {
			Log.e(TAG, activity + " must implement OnMapInfoRequestdListener",
					e);
		}
	}

	private void showProgress() {
		((TabHostActivity) getActivity()).showProgress(null);
	}

	/**
	 * Hides the progress UI for a lengthy operation.
	 */
	private void hideProgress() {
		((TabHostActivity) getActivity()).hideProgress();
	}

}
