package edu.cmu.android.restaurant.models;

/**
 * Model class representing a photo returned by Google Place API
 * 
 * @author Shenghao Huang
 * 
 */
public class Photo {

	private String reference;
	private int width, height;

	/*
	 * Getters of fields
	 */
	public String getReference() {
		return reference;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	/**
	 * Constructor, gives value to the fields
	 * 
	 * @param reference
	 * @param width
	 * @param height
	 */
	public Photo(String reference, int width, int height) {
		super();
		this.reference = reference;
		this.width = width;
		this.height = height;
	}

}
