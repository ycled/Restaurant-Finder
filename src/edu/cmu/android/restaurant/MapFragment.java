/*
 * Copyright (C) 2011 Ievgenii Nazaruk
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.cmu.android.restaurant;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.LocalActivityManager;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import edu.cmu.android.restaurant.listeners.OnMapInfoRequestedListener;
import edu.cmu.android.restaurant.models.Deal;

/**
 * This code is adapted by Rui Gao for the restaurant project
 * 
 * @author rgao
 * 
 */

public class MapFragment extends Fragment implements SensorEventListener {
	private static final String TAG = "MapFragment";

	/*
	 * Listener to communicate with hosting activity and fetch map info
	 */
	private LocalActivityManager mLocalActivityManager;

	private static final String ABOUT_CONTENT = "CMU 18641 Java Smart Phone\nGroup: E&M\n- Shenghao Huang\n- Yuchen Yang\n- Rui Gao";
	private static final int TAKE_PICTURE = 1;
	private OnMapInfoRequestedListener mListener;

	private SensorManager sensorManager = null;
	private Vibrator vibrator = null;

	private ArrayList<Deal> dealList = null;
	private Deal deal = null;

	protected LocalActivityManager getLocalActivityManager() {
		return mLocalActivityManager;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle state = null;
		if (savedInstanceState != null) {
			state = savedInstanceState.getBundle(TAG);
		}
		mLocalActivityManager = new LocalActivityManager(getActivity(), true);
		mLocalActivityManager.dispatchCreate(state);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Intent intent = new Intent(getActivity(), MyMapActivity.class);

		final Window w = getLocalActivityManager().startActivity("tag", intent);
		final View wd = w != null ? w.getDecorView() : null;

		if (wd != null) {
			ViewParent parent = wd.getParent();
			if (parent != null) {
				ViewGroup v = (ViewGroup) parent;
				v.removeView(wd);
			}

			wd.setVisibility(View.VISIBLE);
			wd.setFocusableInTouchMode(true);
			if (wd instanceof ViewGroup) {
				((ViewGroup) wd)
						.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
			}
		}

		((MyMapActivity) mLocalActivityManager.getActivity("tag"))
				.registerListener(mListener);

		setHasOptionsMenu(true); // add menu in fragment
		sensorManager = (SensorManager) getActivity().getSystemService(
				Activity.SENSOR_SERVICE);
		vibrator = (Vibrator) getActivity().getSystemService(
				Service.VIBRATOR_SERVICE);
		dealList = mListener.onDealsRequested();

		return wd;

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBundle(TAG, mLocalActivityManager.saveInstanceState());
	}

	@Override
	public void onResume() {
		super.onResume();
		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
		mLocalActivityManager.dispatchResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		sensorManager.unregisterListener(this);
		mLocalActivityManager.dispatchPause(getActivity().isFinishing());
	}

	@Override
	public void onStop() {
		super.onStop();
		mLocalActivityManager.dispatchStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mLocalActivityManager.dispatchDestroy(getActivity().isFinishing());
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

	/**
	 * Safe the photo in sdcard
	 * */

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
		Random generator = new Random();
		if (dealList != null && dealList.size() != 0) {
			int randomnumber = generator.nextInt(dealList.size());
			deal = dealList.get(randomnumber);
		} else {
			setNoDeal();
		}

	}

	/**
	 * dealDisplay() - display the couple of the restaurant.
	 */
	public void dealDisplay() {

		Builder builder = new AlertDialog.Builder(getActivity());
		if (deal.getTitle().equalsIgnoreCase("Not available")) {

			builder.setTitle("Sorry");
			builder.setMessage("No available coupon for " + " right now.");

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

				findDeal();
				dealDisplay();
				/* audio play */
				MediaPlayer mp = MediaPlayer.create(getActivity(),
						R.raw.coupon_audio);
				mp.seekTo(0);
				mp.start();
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// Do nothing.
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
			findDeal();
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
