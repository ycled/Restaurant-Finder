package edu.cmu.android.restaurant;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.android.maps.ItemizedOverlay;

import edu.cmu.android.restaurant.listeners.OnMapInfoRequestedListener;

public class HelloItemizedOverlay extends ItemizedOverlay<RestaurantItem> {

	private ArrayList<RestaurantItem> mOverlays = new ArrayList<RestaurantItem>();
	Context mContext;
	OnMapInfoRequestedListener mListener;

	public HelloItemizedOverlay(Drawable defaultMarker, Context context,
			OnMapInfoRequestedListener listener) {
		super(boundCenterBottom(defaultMarker));
		mContext = context;
		mListener = listener;
	}

	public void addOverlay(RestaurantItem overlay) {
		mOverlays.add(overlay);
		populate();
	}

	@Override
	protected boolean onTap(int index) {
		final RestaurantItem item = mOverlays.get(index);
		AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		dialog.setTitle(item.getTitle());
		dialog.setMessage(item.getSnippet());

		dialog.setPositiveButton("Details", new OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// Send the positive button event back to the host activity
				showProgress();
				mListener.onRestaurantSelected(item.getRestaurant());
			}
		});
		dialog.setNegativeButton("Cancel", new OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {

			}
		});

		dialog.show();
		return true;
	}

	/**
	 * Shows the progress UI for a lengthy operation.
	 */
	public void showProgress() {
		((TabHostActivity) getActivity()).showProgress(null);

	}

	@Override
	protected RestaurantItem createItem(int i) {
		return mOverlays.get(i);
	}

	@Override
	public int size() {
		return mOverlays.size();
	}

	/**
	 * Helper that cast the activity
	 * 
	 * @return
	 */
	private Activity getActivity() {
		final String TAG = "Inside Overlay";
		try {
			return (Activity) mListener;
		} catch (ClassCastException e) {
			Log.e(TAG, "Error casting tab hosting activity", e);
			throw new AssertionError("Cannot happen: " + e.getMessage());
		}
	}
}