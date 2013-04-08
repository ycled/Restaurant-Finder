package edu.cmu.android.restaurant.utils;

/**
 * Exception related to parsing xml
 * 
 * @author Shenghao Huang
 * 
 */
public class XMLParseException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public XMLParseException() {
	}

	public XMLParseException(String msg) {
		super(msg);
	}

	public XMLParseException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
