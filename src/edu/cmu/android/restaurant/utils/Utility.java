package edu.cmu.android.restaurant.utils;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.AlertDialog;
import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.util.Log;

import com.google.android.maps.GeoPoint;

import edu.cmu.android.restaurant.models.Restaurant;

/**
 * Utility Class that has static methods to perform certain actions
 * 
 * @author Shenghao Huang
 * 
 */
public class Utility {
	private static final String GoogleAPIKey = "AIzaSyAN5COBrPBwVOMgYKUR072B7_fL5BRLlyo";
	private static final String GrouponAPIKey = "229ff45e4c9946a9efd03a458e051cfab49ef578";

	/**
	 * Transform an andorid location object into a google GeoPoint object
	 * 
	 * @param loc
	 *            the passed in location
	 * @return transformed GeoPoint
	 */
	public static GeoPoint location2GeoPoint(Location loc) {
		if (loc == null)
			return null;
		return new GeoPoint((int) (loc.getLatitude() * 1E6),
				(int) (loc.getLongitude() * 1E6));
	}

	/**
	 * Make lat lng into a Geo point
	 * 
	 * @param lat
	 * @param lng
	 * @return
	 */
	public static GeoPoint geoPointFromLatLng(double lat, double lng) {
		return new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6));
	}

	/**
	 * Transform a geo point into an location object
	 * 
	 * @param gp
	 *            the GeoPoint to the transformed
	 * @return
	 */
	public static Location geoPoint2Location(GeoPoint gp) {
		Location loc = new Location("GoogleMap");
		loc.setLatitude(gp.getLatitudeE6() / 1E6d);
		loc.setLongitude(gp.getLongitudeE6() / 1E6d);
		return loc;
	}

	/**
	 * Transform an address object into a Location Object
	 * 
	 * @param addr
	 *            the address object to be transformed
	 * @return the transformed location object
	 */
	public static Location address2Location(Address addr) {
		Location loc = new Location("GeoCoder");
		loc.setLatitude(addr.getLatitude());
		loc.setLongitude(addr.getLongitude());
		return loc;
	}

	/**
	 * Given a url that will reply XML, fetch the XML
	 * 
	 * @return a Document with that XML, else null
	 */
	public static Document getRemoteXML(String url) {
		final String TAG = "Utility - getRemoteXML";
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			InputSource is = new InputSource(url);
			Document rst = db.parse(is);
			return rst;
		} catch (ParserConfigurationException e) {
			Log.e(TAG, "Parser configuration error", e);
		} catch (SAXException e) {
			Log.e(TAG, "SAX exception", e);
		} catch (IOException e) {
			Log.e(TAG, "Connectioin exception", e);
		}
		return null;

	}

	/**
	 * Format the Url to access the Google place API.
	 * 
	 * @param GoogleAPIKey
	 *            is the API key
	 * @param center
	 *            is the center of the search
	 * @param radius
	 *            is the how far we should search
	 * @param sensor
	 *            whether the result is read from a sensor
	 * @return the formatted String
	 */
	public static String formatFoodPOIUrl(Location center, int radius,
			boolean sensor) {

		final String baseUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/xml?key=%s&location=%f,%f&types=%s&radius=%d&sensor=%b";
		final String types = "food|restaurant|cafe";
		return String.format(baseUrl, GoogleAPIKey, center.getLatitude(),
				center.getLongitude(), types, radius, sensor);
	}

	/**
	 * Format the url of the detail of a page
	 * 
	 * @param ref
	 *            the reference of the restaurant
	 * @param sensor
	 *            if is from a sensor
	 * @return the formatted Url for the detail API
	 */
	public static String formatDetailUrl(String ref, boolean sensor) {
		final String baseUrl = "https://maps.googleapis.com/maps/api/place/details/xml?key=%s&reference=%s&sensor=%b";
		return String.format(baseUrl, GoogleAPIKey, ref, sensor);
	}

	/**
	 * Format a photo url from the restaurant
	 * 
	 * @param tar
	 *            the target restaurant
	 * @return the String to query street view API
	 */
	public static String formatPhotoUrl(Restaurant tar) {
		Location there = geoPoint2Location(tar.getCoordinates());
		final int w = 400, h = 400;
		String baseUrl = "http://maps.googleapis.com/maps/api/streetview?key=%s&size=%dx%d&location=%f,%f&sensor=%b";
		// since place is from the location (nearby) api, it is not from a
		// sensor
		String ret = String.format(baseUrl, GoogleAPIKey, w, h,
				there.getLatitude(), there.getLongitude(), false);
		return ret;
	}

	/**
	 * Format a photo url from geo point
	 * 
	 * @param tar
	 *            the target geo point
	 * @return the formatted URL
	 */
	public static String formatPhotoUrl(GeoPoint tar) {
		Location there = geoPoint2Location(tar);
		final int w = 400, h = 400;
		String baseUrl = "http://maps.googleapis.com/maps/api/streetview?key=%s&size=%dx%d&location=%f,%f&sensor=%b";
		// since place is from the location (nearby) api, it is not from a
		// sensor
		String ret = String.format(baseUrl, GoogleAPIKey, w, h,
				there.getLatitude(), there.getLongitude(), false);
		return ret;
	}

	/**
	 * Format a groupon api url to based on the restaurant supplied
	 * 
	 * @param restaurant
	 *            the target restaurant
	 * @return Groupon API URL
	 */
	public static String formatDealUrl(Restaurant restaurant) {
		Location there = geoPoint2Location(restaurant.getCoordinates());
		String baseUrl = "http://api.groupon.com/v2/deals?client_id=%s&lat=%f&lng=%f";
		String ret = String.format(baseUrl, GrouponAPIKey, there.getLatitude(),
				there.getLongitude());
		return ret;
	}

	/**
	 * Create a simple warning dialog for the current context
	 * 
	 * @param ctx
	 *            the current context
	 * @param titleId
	 *            resource ID of the title
	 * @param msg
	 *            message
	 */
	public static void simpleDialog(Context ctx, int titleId, String msg) {
		final String TAG = "Warning Dialog";
		Log.w(TAG, "Warning dialog with message: " + msg);
		AlertDialog.Builder dialog = new AlertDialog.Builder(ctx);
		dialog.setTitle(titleId);
		dialog.setMessage(msg);
		dialog.setNeutralButton("OK", null);
		dialog.show();
	}

	/**
	 * Transform Address into a standard format address string
	 * 
	 * @param add
	 * @return
	 */
	public static String address2String(Address add) {
		// final String TAG = "Utlity: Address to String";
		StringBuilder sb = new StringBuilder();
		// Log.d(TAG, add.toString());
		int max = add.getMaxAddressLineIndex();
		if (max > -1) {
			sb.append(add.getAddressLine(0));
			for (int i = 1; i <= max; i++)
				sb.append(", ").append(add.getAddressLine(i));

		}
		return sb.toString();
	}
}
