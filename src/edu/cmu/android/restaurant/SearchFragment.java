package edu.cmu.android.restaurant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import edu.cmu.android.restaurant.listeners.OnSearchEnteredListener;
import edu.cmu.android.restaurant.utils.Utility;

public class SearchFragment extends Fragment {
	private static final String TAG = "SearchFragment";

	/*
	 * Listener to communicate with hosting activity and fetch map info
	 */
	private OnSearchEnteredListener mListener;
	private AutoCompleteTextView searchText;
	private Button searchButton;
	private Button gpsButton;
	private ArrayAdapter<String> autoCompleteAdapter;
	private Geocoder geocoder;
	private InputMethodManager imm;
	public static final int MESSAGE_TEXT_CHANGED = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View mView = inflater.inflate(R.layout.fragment_search, container,
				false);

		// search bar
		searchText = (AutoCompleteTextView) mView
				.findViewById(R.id.search_field);
		searchButton = (Button) mView.findViewById(R.id.search_button);
		// initialize the auto completion feature
		setupAutoComplete(searchText);
		searchButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				String searchString = searchText.getText().toString();
				Log.d(TAG, "Search Button Clicked: string = " + searchString);
				hideKeyboard(searchText);
				showProgress();
				mListener.onSearchEntered(searchString);
			}
		});

		// gps button
		gpsButton = (Button) mView.findViewById(R.id.search_gps_button);

		gpsButton.setOnClickListener(new OnClickListener() {
			String gpsString = null;

			public void onClick(View view) {
				Log.d(TAG, "SPS Button Clicked: string = " + gpsString);
				hideKeyboard(searchText);
				showProgress();
				mListener.onSearchEntered(gpsString);

			}
		});

		return mView;
	}

	/**
	 * Helper method to hide the key board Called when any of the button is
	 * clicked
	 * 
	 * @param tv
	 *            the text view that brings the keyboard up
	 */
	private void hideKeyboard(TextView tv) {
		imm.hideSoftInputFromWindow(getView().getWindowToken(),
				InputMethodManager.HIDE_IMPLICIT_ONLY);
	}

	/**
	 * Public interface for hiding the keyboard
	 */
	public void hideKeyboard() {
		hideKeyboard(searchText);
	}

	/**
	 * Helper method to setup auto completion for text view
	 * 
	 * @param tv
	 *            the target text view
	 */
	private void setupAutoComplete(AutoCompleteTextView tv) {
		final int THRESHOLD = 3; // from which point to do the autocomplete
		if (tv != null) {

			autoCompleteAdapter = new ArrayAdapter<String>(getActivity(),
					android.R.layout.simple_dropdown_item_1line,
					new ArrayList<String>());
			autoCompleteAdapter.setNotifyOnChange(false);

			TextWatcher tw = new TextWatcher() {

				@Override
				public void afterTextChanged(Editable arg0) {
					// do nothing

				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
					// do nothing

				}

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
					String str = s.toString();
					if (!"".equals(str) && str.length() > THRESHOLD) {
						new UpdateAutoCompleteTask().execute(str);
					} else {
						autoCompleteAdapter.clear();
					}

				}

			};
			tv.setThreshold(THRESHOLD);
			tv.setAdapter(autoCompleteAdapter);
			tv.addTextChangedListener(tw);
		}
	}

	/**
	 * On attach call back method. Cast the host activity into
	 * OnMapInfoRequestedListener
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		geocoder = new Geocoder(activity);
		imm = (InputMethodManager) activity
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		// error checking
		if (imm == null)
			throw new AssertionError(
					"InputMethodService not present, cannot happen");

		try {
			mListener = (OnSearchEnteredListener) activity;
		} catch (ClassCastException e) {
			Log.e(TAG, activity + " must implement OnMapInfoRequestdListener",
					e);
		}
	}

	/**
	 * Helper method to update the autocompletion lst
	 * 
	 * @param lst
	 */
	private void updateAutoComplete(List<Address> lst) {
		autoCompleteAdapter.clear();
		for (Address addr : lst) {
			String addStr = Utility.address2String(addr);
			Log.d(TAG, "Add address: " + addStr);
			autoCompleteAdapter.add(addStr);

		}
		autoCompleteAdapter.notifyDataSetChanged();

	}

	/**
	 * Shows the progress UI for a lengthy operation.
	 */
	private void showProgress() {
		((TabHostActivity) getActivity()).showProgress(null);
	}

	/**
	 * Hides the progress UI for a lengthy operation.
	 */
	private void hideProgress() {
		((TabHostActivity) getActivity()).hideProgress();
	}

	/**
	 * 
	 * AsyncTask to update auto completion suggestion
	 * 
	 */
	private class UpdateAutoCompleteTask extends
			AsyncTask<String, Void, List<Address>> {
		private static final int MAX = 10;

		@Override
		protected List<Address> doInBackground(String... arg0) {
			String str = arg0[0];
			if (!TextUtils.isEmpty(str)) {
				Log.d(TAG, "Searching suggestion for " + str);
				List<Address> lst = null;
				try {
					lst = geocoder.getFromLocationName(str, MAX);
				} catch (IOException e) {
					Log.e(TAG, "Geocoder auto complete failed on IO", e);
				}
				return lst;
			} else {
				return null;
			}
		}

		@Override
		protected void onPostExecute(List<Address> addrLst) {
			if (addrLst != null) {
				Log.d(TAG, "Auto completion returned with results count: "
						+ addrLst.size());
				updateAutoComplete(addrLst);
			}
		}
	}

}
