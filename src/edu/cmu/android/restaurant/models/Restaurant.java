package edu.cmu.android.restaurant.models;

import java.util.ArrayList;

import com.google.android.maps.GeoPoint;

import edu.cmu.android.restaurant.utils.Utility;

/**
 * Model class representation of a single restaurant
 * 
 * @author Shenghao Huang
 * 
 */
public class Restaurant {
	private String name, address, reference, photoUrl;
	private GeoPoint coordinates;
	private Photo thumbNail;
	private double rating;

	// ===========additional member variables, optional =============
	private String phone, website;
	private ArrayList<Review> reviews;

	/**
	 * Getter of phone
	 * 
	 * @return the formatted phone string, null if not available
	 */
	public String getPhone() {
		return phone;
	}

	/**
	 * Setter provided for filling in detail
	 * 
	 * @param phone
	 *            the formatted phone string
	 */
	public void setPhone(String phone) {
		this.phone = phone;
	}

	/**
	 * Getter of website
	 * 
	 * @return the website url, null if not available
	 */
	public String getWebsite() {
		return website;
	}

	/**
	 * Setter provided for filling in detail
	 * 
	 * @param website
	 *            is the website of the restaurant
	 */
	public void setWebsite(String website) {
		this.website = website;
	}

	/**
	 * Since arraylist is mutable, only getter is provided for detail fill in
	 * 
	 * @return
	 */
	public ArrayList<Review> getReviews() {
		return reviews;
	}

	/**
	 * Constuctor with argument
	 * 
	 * @param name
	 *            is the name of the restaurant
	 * @param address
	 *            is the address of the restaurant
	 * @param phone
	 *            is the phone number
	 * @param website
	 *            is the website if any
	 * @param thumbNail
	 *            is the thumbNail if any
	 * @param coordinates
	 *            is where the restaurant is located on the map
	 */
	public Restaurant(String reference, String name, String address,
			Photo thumbNail, GeoPoint coordinates, double rating) {
		reviews = new ArrayList<Review>();
		this.name = name;
		this.address = address;
		this.reference = reference;
		this.thumbNail = thumbNail;
		this.coordinates = coordinates;
		this.rating = rating;
		// format the street view url from the coordinates
		photoUrl = Utility.formatPhotoUrl(coordinates);
	}

	public Restaurant() {
	}

	/**
	 * Getter for name
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Getter for rating
	 * 
	 * @return
	 */
	public double getRating() {
		return rating;
	}

	/**
	 * Getter for address
	 * 
	 * @return
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * Getter for website
	 * 
	 * @return
	 */
	public String getReference() {
		return reference;
	}

	/**
	 * Provide a good presentation of the restaurant object
	 */
	@Override
	public String toString() {
		return String.format("Restaurant [name=%s, address=%s, rating=%f",
				name, address, rating);
	}

	/**
	 * Getter for thumbNail link
	 * 
	 * @return
	 */
	public Photo getThumbNail() {
		return thumbNail;
	}

	/**
	 * Getter for coordinates
	 */
	public GeoPoint getCoordinates() {
		return coordinates;
	}

	/**
	 * Getter of photo url
	 * 
	 * @return
	 */
	public String getPhotoUrl() {
		return photoUrl;
	}

	/* Setter for name */
	public void setName(String name) {
		this.name = name;
	}

	/* Setter for address */
	public void setAddress(String address) {
		this.address = address;
	}

	/* Setter for photo url */
	public void setPhotoUrl(String photoUrl) {
		this.photoUrl = photoUrl;
	}

	/* Setter for rating */
	public void setRating(double rating) {
		this.rating = rating;
	}

}
