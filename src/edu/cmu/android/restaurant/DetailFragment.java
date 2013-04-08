package edu.cmu.android.restaurant;

/**
 * DetailFragment: show the detail of a certain restaurant searched by the user
 * @author Yuchen Yang
 *
 */
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import edu.cmu.android.restaurant.listeners.OnDetailRequestedListener;
import edu.cmu.android.restaurant.models.Deal;
import edu.cmu.android.restaurant.models.Restaurant;

public class DetailFragment extends Fragment implements SensorEventListener {

	private static final String TAG = "DetailFragment";
	private static final String ABOUT_CONTENT = "CMU 18641 Java Smart Phone\nGroup: E&M\n- Shenghao Huang\n- Yuchen Yang\n- Rui Gao";
	private static final String STRING_DEFAULT = "Not available";
	private static final int TAKE_PICTURE = 1;

	/* Listener to communicate with hosting activity and fetch details */
	private OnDetailRequestedListener mListener;
	private SensorManager sensorManager = null;
	private Vibrator vibrator = null;
	private Restaurant res1 = new Restaurant();
	private ArrayList<Deal> dealList = null;
	private Deal deal = null;
	private View mView;

	private Handler mHandler;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		/* add menu in fragment */
		setHasOptionsMenu(true);
		/* inflate the detail.xml */

		mView = inflater.inflate(R.layout.fragment_detail, container, false);

		/* update for now */
		updatePageInfo();

		/* get the restaurant detail from mLister.onRestaurantRequested() */
		/* shack to find coupon */
		sensorManager = (SensorManager) getActivity().getSystemService(
				Activity.SENSOR_SERVICE);
		vibrator = (Vibrator) getActivity().getSystemService(
				Service.VIBRATOR_SERVICE);

		return mView;
	}

	/**
	 * Helper method to update the page information --to be reused
	 */
	private void updatePageInfo() {

		/*
		 * get the restaurant detail from mLister.onRestaurantRequested(), if
		 * not available, use the default value.
		 */
		if (mListener.onRestaurantRequested() != null) {
			res1 = mListener.onRestaurantRequested();
		}
		/* complement of the restaurant class the avoid error */
		resDetialRevice();

		/* find the available deal of the restaurant */
		if (mListener.onDealsRequested() != null) {
			dealList = mListener.onDealsRequested();
			findDeal();
			if (deal != null) {
				resDealRevice();
			} else {
				setNoDeal();
			}
		} else {
			setNoDeal();
		}

		/* get restaurant detail to display */
		getViewDisplay(mView);

	}

	/**
	 * resDealRevice - replace the null pointer to "" in the deal object to
	 * avoid null pointer error.
	 */
	private void resDealRevice() {
		if (deal.getDealUrl() == null) {
			deal.setDealUrl("");
		}
		if (deal.getDivisionName() == null) {
			deal.setDivisionName("");
		}
		if (deal.getImageUrl() == null) {
			deal.setImageUrl("");
		}
		if (deal.getPrice() == null) {
			deal.setPrice("");
		}
		if (deal.getStatus() == null) {
			deal.setStatus("");
		}
		if (deal.getTitle() == null) {
			deal.setTitle("");
		}
		if (deal.getValue() == null) {
			deal.setValue("");
		}
		if (deal.getVendorName() == null) {
			deal.setVendorName("");
		}
	}

	/**
	 * setNoDeal - if no deal of the restaurant is available, set default value
	 * for the deal class
	 */
	private void setNoDeal() {

		deal = new Deal("Not Available", "Not available", "Not available",
				"Not available", "Not available", "Not available",
				"Not available", "Not available", "Not available");
	}

	/**
	 * findDeal - find if there is available coupon
	 */
	private void findDeal() {
		for (int i = 0; i < dealList.size(); i++) {
			// if(dealList.get(i).getVendorName() == res1.getName()){
			if (dealList.get(i).getTitle().indexOf(res1.getName()) != -1) {
				deal = dealList.get(i);
				break;
			}
		}

	}

	/**
	 * getViewDisplay() - get restaurant detail to display
	 */
	private void getViewDisplay(View mView) {

		/* get restaurant detail to display */
		getViewResInfo(mView);
		/* get restaurant review to display */
		getViewResReview(mView);
		/* get restaurant image to display */
		getViewResImage(mView, res1.getPhotoUrl());

	}

	/**
	 * resDetialRevice - if the restaurant detail information is not completed,
	 * display "Not available" to avoid error caused by null pointer.
	 */
	private void resDetialRevice() {

		if (res1.getName() == null) {
			res1.setName(STRING_DEFAULT);
		}
		if (res1.getAddress() == null) {

		}
		if (res1.getWebsite() == null) {
			res1.setWebsite(STRING_DEFAULT);
		}
		if (res1.getPhone() == null) {
			res1.setPhone(STRING_DEFAULT);
		}
		if (res1.getPhotoUrl() == null) {
			res1.setPhotoUrl(STRING_DEFAULT);
		}

		/*
		 * get restaurant detail to display
		 */

		getViewResInfo(mView);

		/*
		 * get restaurant review to display
		 */

		getViewResReview(mView);
		/*
		 * get restaurant image to display
		 */
		getViewResImage(mView, res1.getPhotoUrl());

	}

	/**
	 * if no restaurant returned, show loading process until a restaurant
	 * returned.\
	 */

	@Override
	public void onStart() {
		super.onStart();
		if (mListener.onRestaurantRequested() == null) {
			/* no restaurant returned, pull it until it returns */
			showProgress();
		}
	}

	/**
	 * Shows the progress UI for a lengthy operation.
	 */
	public void showProgress() {

		Runnable waitTask = new Runnable() {
			final long interval = 500;

			@Override
			public void run() {
				Log.i(TAG, "Enter into the loop, waiting for return and update");
				try {
					Restaurant ret = null;
					while ((ret = mListener.onRestaurantRequested()) == null) {
						Thread.sleep(interval);
					}
					res1 = ret;
					/*
					 * Post the update event to the UI thread
					 */
					mHandler.post(new Runnable() {

						@Override
						public void run() {
							updatePageInfo();

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
	 * getViewResInfo - load restaurant detail to each view, including - name -
	 * address - phone number - website - rating
	 */
	private void getViewResInfo(View mView) {

		TextView resName = (TextView) mView.findViewById(R.id.detail_res_name);
		TextView resAddr = (TextView) mView
				.findViewById(R.id.detail_res_address);
		TextView resPhone = (TextView) mView
				.findViewById(R.id.detail_res_phone);
		TextView resWeb = (TextView) mView
				.findViewById(R.id.detail_res_website);
		TextView resRate = (TextView) mView.findViewById(R.id.detail_res_rate);

		resName.setText(res1.getName());
		resAddr.setText(res1.getAddress());
		resRate.setText(Double.toString(res1.getRating()));
		resWeb.setText(res1.getWebsite());
		resPhone.setText(res1.getPhone());

	}

	/**
	 * getViewResReview(View mView) - get restaurant review to display
	 */
	private void getViewResReview(View mView) {

		LinearLayout revBar;
		LinearLayout revBox;
		TextView revAuthor;
		TextView revAuthorUrl;
		TextView revContent;

		int count = 0;
		if (res1.getReviews() != null) {
			count = res1.getReviews().size();
		} else {
			count = 0;
		}
		if (count > 3) {
			count = 3;
		}

		int[] revBarId = { R.id.detail_rev_1_bar, R.id.detail_rev_2_bar,
				R.id.detail_rev_3_bar };
		int[] revBoxId = { R.id.detail_rev_1, R.id.detail_rev_2,
				R.id.detail_rev_3 };
		int[] autherId = { R.id.detail_rew_1_author, R.id.detail_rew_2_author,
				R.id.detail_rew_3_author };
		int[] autherUrlId = { R.id.detail_rew_1_url, R.id.detail_rew_2_url,
				R.id.detail_rew_3_url };
		int[] content = { R.id.detail_rew_1_content, R.id.detail_rew_2_content,
				R.id.detail_rew_3_content };

		for (int i = 0; i < count; i++) {
			revBar = (LinearLayout) mView.findViewById(revBarId[i]);
			revBox = (LinearLayout) mView.findViewById(revBoxId[i]);
			revAuthor = (TextView) mView.findViewById(autherId[i]);
			revAuthorUrl = (TextView) mView.findViewById(autherUrlId[i]);
			revContent = (TextView) mView.findViewById(content[i]);

			revAuthor.setText(res1.getReviews().get(i).getAuthor());
			revAuthorUrl.setText(res1.getReviews().get(i).getAuthorUrl());
			revContent.setText(res1.getReviews().get(i).getText());

			revBar.setVisibility(View.VISIBLE);
			revBox.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * getViewResImage - get restaurant image to display
	 */
	public void getViewResImage(View mView, final String imgUrl) {

		final ImageView mImageView = (ImageView) mView
				.findViewById(R.id.detail_res_img);

		/* open a new thread to load the image */
		new Thread(new Runnable() {

			Drawable drawable = loadImageFromNetwork(imgUrl);

			@Override
			public void run() {
				mImageView.post(new Runnable() {
					@Override
					public void run() {
						mImageView.setImageDrawable(drawable);
					}
				});
			}

		}).start();

		mImageView.setAdjustViewBounds(true);
		mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

	}

	/**
	 * On attach call back method. Cast the host activity into
	 * OnDetailRequestedListener
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mHandler = new Handler();
		try {
			mListener = (OnDetailRequestedListener) activity;
		} catch (ClassCastException e) {
			Log.e(TAG, activity + " must implement OnDetailRequestdListener", e);
		}
	}

	/**
	 * load the restaurant image
	 * */
	private Drawable loadImageFromNetwork(String imageUrl) {

		Drawable drawable = null;
		try {
			drawable = Drawable.createFromStream(
					new URL(imageUrl).openStream(), "image.jpg");
		} catch (IOException e) {
			Log.d("test", e.getMessage());
		}

		if (drawable == null) {
			Log.d("test", "null drawable");
		} else {
			Log.d("test", "not null drawable");
		}

		return drawable;
	}

	/**
	 * Safe the photo in sdcard
	 * */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == TAKE_PICTURE) {
			if (resultCode == Activity.RESULT_OK) {
				Bitmap bm = (Bitmap) data.getExtras().get("data");
				SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmssZ");
				String dateStr = sdf.format(new Date());
				File myCaptureFile = new File(Environment
						.getExternalStorageDirectory().getPath()
						+ "/"
						+ dateStr + ".jpg");
				try {

					BufferedOutputStream bos = new BufferedOutputStream(
							new FileOutputStream(myCaptureFile));
					bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);
					bos.flush();
					bos.close();

				} catch (FileNotFoundException e) {
					Log.e(TAG, e.getMessage(), e);

				} catch (IOException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * 
	 * Shaking: - User shake the mobile to find the coupon of the restaurant in
	 * the detail fragment page - After shaking, the mobile vibrates for 500 ms
	 * 
	 * */
	@Override
	public void onSensorChanged(SensorEvent event) {

		int sensorType = event.sensor.getType();
		float[] values = event.values;

		if (sensorType == Sensor.TYPE_ACCELEROMETER) {
			if ((Math.abs(values[0]) > 15 || Math.abs(values[1]) > 15 || Math
					.abs(values[2]) > 15)) {

				Log.d("sensor ", "X- values[0] = " + values[0]);
				Log.d("sensor ", "Y- values[1] = " + values[1]);
				Log.d("sensor ", "Z- values[2] = " + values[2]);

				/* display the deal in dialog */
				dealDisplay();
				/* mobile vibrate for 500 ms */
				vibrator.vibrate(500);
				/* audio play */
				MediaPlayer mp = MediaPlayer.create(getActivity(),
						R.raw.coupon_audio);
				mp.seekTo(0);
				mp.start();
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		sensorManager.unregisterListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// Do nothing.
	}

	/**
	 * dealDisplay() - display the couple of the restaurant.
	 */
	public void dealDisplay() {

		Builder builder = new AlertDialog.Builder(getActivity());
		if (deal.getTitle().equalsIgnoreCase("Not available")) {

			builder.setTitle("Sorry");
			builder.setMessage("No available coupon for " + res1.getName()
					+ " right now.");

		} else {
			builder.setTitle(deal.getTitle());
			builder.setMessage("Vendor: " + deal.getVendorName() + "\n"
					+ "Status: " + deal.getStatus() + "\n" + "Value:  "
					+ deal.getValue() + "\n" + "Price:  " + deal.getPrice()
					+ "\n");
		}

		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// finish();
			}
		}).show();

	}

	/**
	 * Menu - include 4 items 1. Camera : click to make a photo 2. Coupon :
	 * click to find the coupon of the restaurant 3. About : The developer's
	 * information
	 * */

	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		super.onCreateOptionsMenu(menu, inflater);
		// 1.group, 2.ID, 3.order, 4.txt
		menu.add(Menu.NONE, Menu.FIRST + 1, 1, "Camera").setIcon(
				android.R.drawable.ic_menu_camera);
		menu.add(Menu.NONE, Menu.FIRST + 2, 2, "Coupon").setIcon(
				R.drawable.ic_menu_emoticons);
		menu.add(Menu.NONE, Menu.FIRST + 3, 3, "About").setIcon(
				android.R.drawable.ic_menu_myplaces);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		/* menu : camera */
		case Menu.FIRST + 1:
			try {
				Intent i = new Intent("android.media.action.IMAGE_CAPTURE");
				startActivityForResult(i, Activity.DEFAULT_KEYS_DIALER);
			} catch (Exception e) {
				Log.d("Detail", "Cemara onClick error.");
			} finally {
				if (getActivity().getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT) {
					getActivity().setRequestedOrientation(
							Configuration.ORIENTATION_PORTRAIT);
				}
			}
			break;

		/* menu : coupon */
		case Menu.FIRST + 2:
			/* display the deal in a dialog */
			dealDisplay();

			/* audio play */
			MediaPlayer mp = MediaPlayer.create(getActivity(),
					R.raw.coupon_audio);
			mp.seekTo(0);
			mp.start();
			break;

		/* Menu : about */
		case Menu.FIRST + 3:
			Builder builder3 = new AlertDialog.Builder(getActivity());
			builder3.setTitle("About US");
			builder3.setMessage(ABOUT_CONTENT);
			builder3.setPositiveButton("Return",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					}).show();
			break;
		}
		return false;

	}

	@Override
	public void onOptionsMenuClosed(Menu menu) {
		// Toast.makeText(getActivity(), "menu closed",
		// Toast.LENGTH_LONG).show();

	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		// Toast.makeText(getActivity(), "menu prepare",
		// Toast.LENGTH_LONG).show();

	}

}