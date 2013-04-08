package edu.cmu.android.restaurant.utils;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import edu.cmu.android.restaurant.models.Deal;

public class JsonParser {
	private static JsonParser instance = new JsonParser();

	// private constructor
	private JsonParser() {
	}

	public static JsonParser getInstance() {
		return instance;
	}

	public JSONObject getJSON(String url) throws IOException, JSONException {
		final String TAG = "JsonParser";
		Log.i(TAG, "Get Json for " + url);
		JSONObject jsa = null;
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);
		HttpResponse response = client.execute(request);
		String page = EntityUtils.toString(response.getEntity());
		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			jsa = new JSONObject(page);
		}
		return jsa;
	}

	public ArrayList<Deal> getDeals(String url) {
		final String TAG = "GetDeals";
		ArrayList<Deal> rst = new ArrayList<Deal>();
		try {
			JSONObject root = getJSON(url);
			JSONArray deals = root.getJSONArray("deals");

			int len = deals.length();
			for (int i = 0; i < len; i++) {
				rst.add(parseOneDeal(deals.getJSONObject(i)));
			}
		} catch (JSONException e) {
			Log.e(TAG, "Error when parsing deals", e);
		} catch (IOException e) {
			Log.e(TAG, "IO error when paring deals", e);
		}
		return rst;
	}

	private Deal parseOneDeal(JSONObject deal) throws JSONException {
		String divisionName = deal.getJSONObject("division").getString("name");
		String vendorName = deal.getString("type");
		String shortTitle = deal.has("shortAnnouncementTitle") ? deal
				.getString("shortAnnouncementTitle") : "";
		String title = deal.has("title") ? deal.getString("title") : "";
		String status = deal.has("status") ? deal.getString("status") : "";
		JSONArray options = deal.has("options") ? deal.getJSONArray("options")
				: null;
		String price = "", value = "";
		if (options != null) {
			JSONObject option = options.getJSONObject(0);
			price = option.has("price") ? option.getJSONObject("price")
					.getString("formattedAmount") : "";
			value = option.has("value") ? option.getJSONObject("value")
					.getString("formattedAmount") : "";
		}
		String imgUrl = deal.has("smallImageUrl") ? deal
				.getString("smallImageUrl") : "";
		String dealUrl = deal.has("dealUrl") ? deal.getString("dealUrl") : "";
		return new Deal(shortTitle, title, dealUrl, imgUrl, divisionName,
				vendorName, status, price, value);
	}

}
