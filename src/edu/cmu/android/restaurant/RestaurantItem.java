package edu.cmu.android.restaurant;

import com.google.android.maps.OverlayItem;

import edu.cmu.android.restaurant.models.Restaurant;

public class RestaurantItem extends OverlayItem{
	
	Restaurant restaurant;
	public RestaurantItem(Restaurant restaurant) {
		super(restaurant.getCoordinates(), restaurant.getName(), restaurant.getAddress());
		// TODO Auto-generated constructor stub
		this.restaurant = restaurant;
	}
	
	public Restaurant getRestaurant() {
		return restaurant;
	}

}
