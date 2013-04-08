package edu.cmu.android.restaurant.listeners;

import java.util.ArrayList;

import edu.cmu.android.restaurant.models.Deal;
import edu.cmu.android.restaurant.models.Restaurant;

/**
 * OnDetailRequestedListener This is the interface bridges the tab host activity
 * and the detail fragment
 * 
 * @author Shenghao Huang
 * 
 */
public interface OnDetailRequestedListener {

	/**
	 * Get the current restaurant based on user selection
	 * 
	 * @return
	 */
	public Restaurant onRestaurantRequested();

	/**
	 * Get the deal associated with the current restaurant selection
	 * 
	 * @return
	 */
	public ArrayList<Deal> onDealsRequested();
}
