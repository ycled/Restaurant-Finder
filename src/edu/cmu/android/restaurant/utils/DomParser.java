package edu.cmu.android.restaurant.utils;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.android.maps.GeoPoint;

import edu.cmu.android.restaurant.models.Photo;
import edu.cmu.android.restaurant.models.Restaurant;
import edu.cmu.android.restaurant.models.Review;

/**
 * DOM parser, trying to parse dom object into different domain objects
 * Inteneded to be used as a singleton
 * 
 * @author Shenghao Huang
 * 
 */
public class DomParser {
	private static DomParser instance = new DomParser();

	// private constructor
	private DomParser() {
	}

	public static DomParser getInstance() {
		return instance;
	}

	/**
	 * From the dom, parse a list of restaurant from it
	 * 
	 * @param dom
	 * @return
	 * @throws XMLParseException
	 */
	public ArrayList<Restaurant> parsePOIList(Document dom)
			throws XMLParseException {
		NodeList nodeList = dom.getElementsByTagName("result");
		int len = nodeList.getLength();
		if (len == 0)
			return null; // no result

		ArrayList<Restaurant> rstLst = new ArrayList<Restaurant>(len);
		for (int i = 0; i < len; i++) {
			rstLst.add(parseOneRestaurant((Element) nodeList.item(i)));
		}
		return rstLst;

	}

	/**
	 * From the dom, fill in detail of a restaurant
	 * 
	 * @param tar
	 *            is the target restaurant
	 * @param dom
	 *            is the dom got from Google
	 * @throws XMLParseException
	 *             is tag is not there
	 */
	public void fillRestaurantDetail(Restaurant tar, Document dom)
			throws XMLParseException {
		try {
			Element element = (Element) dom.getElementsByTagName("result")
					.item(0);
			String formattedPhone = getTextByTag(element,
					"formatted_phone_number");
			tar.setPhone(formattedPhone);
			String website = getOptionalTextByTag(element, "website");
			tar.setWebsite(website);

			// add all the reviews
			ArrayList<Review> lst = tar.getReviews();
			NodeList nodeList = element.getElementsByTagName("review");
			int len = nodeList.getLength();

			for (int i = 0; i < len; i++) {
				lst.add(parseOneReview((Element) nodeList.item(i)));
			}
		} catch (NullPointerException e) {
			throw new XMLParseException(
					"Tag does not exists, parsing exception", e);
		}
	}

	private Review parseOneReview(Element element) throws XMLParseException {
		Review rst = null;
		try {

			String text = getTextByTag(element, "text");
			String author = getTextByTag(element, "author_name");
			String authorUrl = getOptionalTextByTag(element, "author_url");
			rst = new Review(text, author, authorUrl);
		} catch (NullPointerException e) {
			throw new XMLParseException(
					"Tag does not exists, parsing exception", e);
		}
		return rst;
	}

	/**
	 * Paser one restaurant from an element
	 * 
	 * @param element
	 *            is the element in the node list
	 * @return a restaurant instance
	 * @throws XMLParseException
	 *             if we cannot parse this element
	 */
	private Restaurant parseOneRestaurant(Element element)
			throws XMLParseException {
		Restaurant rst = null;
		try {
			double lat = Double.parseDouble(getTextByTag(element, "lat"));
			double lng = Double.parseDouble(getTextByTag(element, "lng"));
			String ratingTxt = getOptionalTextByTag(element, "rating");
			double rating = ratingTxt == null ? 0 : Double
					.parseDouble(ratingTxt);
			GeoPoint gp = Utility.geoPointFromLatLng(lat, lng);
			String ref = getTextByTag(element, "reference");
			String name = getTextByTag(element, "name");
			String addr = getTextByTag(element, "vicinity");
			Photo thumbNail = parseOnePhoto((Element) getNodeByTag(element,
					"photo"));
			rst = new Restaurant(ref, name, addr, thumbNail, gp, rating);
		} catch (NullPointerException e) {
			throw new XMLParseException(
					"Tag does not exists, parsing exception\n", e);
		}
		return rst;
	}

	/**
	 * Parse the photo instance
	 * 
	 * @param element
	 *            is the element containing the
	 * @return The photo instance if the photo exists, and null otherwise
	 * @throws XMLParseException
	 */
	private Photo parseOnePhoto(Element element) throws XMLParseException {

		Photo rst = null;
		if (element != null) {
			try {
				int width = Integer.parseInt(getTextByTag(element, "width"));
				int height = Integer.parseInt(getTextByTag(element, "height"));
				String reference = getTextByTag(element, "photo_reference");
				rst = new Photo(reference, width, height);
			} catch (NullPointerException e) {
				throw new XMLParseException(
						"Tag does not exists, parsing exception", e);
			}
		}
		return rst;
	}

	/**
	 * Helper method to extract the text value from a dom provide its tag name
	 * 
	 * @param element
	 *            parent element
	 * @param str
	 *            is the tag name to be found
	 * @return Text of the specific node
	 * @throws XMLParseException
	 *             is the exception thrown from getValue
	 */
	private String getTextByTag(Element element, String str)
			throws XMLParseException {
		return getValue(getNodeByTag(element, str));
	}

	/**
	 * Similar to getTextByTag, this method allows optional text
	 * 
	 * @param element
	 * @param str
	 * @return text if the tag exists, empty string otherwise
	 * @throws XMLParseException
	 */
	private String getOptionalTextByTag(Element element, String str)
			throws XMLParseException {
		Node ret = getNodeByTag(element, str);
		if (ret == null)
			return null;
		else
			return getValue(ret);

	}

	/**
	 * Get a node list by the tag name and return the first Node
	 * 
	 * @param element
	 *            is the parent element
	 * @param str
	 *            is the tag name to be found
	 * @return the required node
	 */
	private Node getNodeByTag(Element element, String str) {
		Node ret = element.getElementsByTagName(str).item(0);
		return ret;
	}

	/**
	 * Get the text value of a given node
	 * 
	 * @param node
	 *            the given node
	 * @return the text value of the node
	 * @throws XMLParseException
	 *             if the node is not a text node
	 */
	private String getValue(Node node) throws XMLParseException {
		Node child = node.getFirstChild();

		if (child.getNodeType() != Node.TEXT_NODE)
			throw new XMLParseException(node.getNodeName()
					+ " should have a child text node");
		StringBuilder sb = new StringBuilder();
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			sb.append(children.item(i).getNodeValue());
		}

		return sb.toString();
	}
}
