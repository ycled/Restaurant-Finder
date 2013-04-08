package edu.cmu.android.restaurant.models;

/**
 * Class representing a single review
 * 
 * @author Shenghao Huang
 * 
 */
public class Review {
	private String text, author, authorUrl;

	public String getText() {
		return text;
	}

	public String getAuthor() {
		return author;
	}

	public String getAuthorUrl() {
		return authorUrl;
	}

	@Override
	public String toString() {
		return "Review [text=" + text + ", author=" + author + "]";
	}

	/**
	 * Constructor with arguments, note: authorUrl may be null!!!
	 * 
	 * @param text
	 * @param author
	 * @param authorUrl
	 */
	public Review(String text, String author, String authorUrl) {
		super();
		this.text = text;
		this.author = author;
		this.authorUrl = authorUrl;
	}

}
