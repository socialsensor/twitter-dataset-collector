package eu.socialsensor.twcollect;

/**
 * Data container class for several metadata fields of a tweet.
 * @author kleinmind
 *
 */
public class TweetFields {
	
	private final String id;
	private final String username;
	private final String text;
	private final String pubTime;
	private int numRetweets = 0;
	private int numFavorites = 0;

	
	// constructor using all fields
	public TweetFields(String id, String username, String text, String pubTime,
			int numRetweets, int numFavorites){
		this.id = id;
		this.username = username;
		this.text = text;
		this.pubTime = pubTime;
		this.numRetweets = numRetweets;
		this.numFavorites = numFavorites;
	}
	
	// constructor without number of retweets & favorites
	public TweetFields(String id, String username, String text, String pubTime){
		this(id, username, text, pubTime, 0, 0);
	}

	// Getters
	public String getId() {
		return id;
	}
	public String getUsername() {
		return username;
	}
	public String getText() {
		return text;
	}
	public String getPubTime() {
		return pubTime;
	}
	public int getNumRetweets() {
		return numRetweets;
	}
	public int getNumFavorites() {
		return numFavorites;
	}
	
	
	// Setters (only for retweets and favorites that are mutable)
	public void setNumRetweets(int numRetweets) {
		this.numRetweets = numRetweets;
	}
	public void setNumFavorites(int numFavorites) {
		this.numFavorites = numFavorites;
	}

	
	private static String SEPARATOR = "\t";
	private static String TAB_ASCII = "&#9;";
	
	@Override
	public String toString() {
		// serialize in a single-line (check if tab is contained in text)
		if (text != null && text.contains(SEPARATOR)){
			return id + SEPARATOR + username + SEPARATOR + 
					text.replaceAll(SEPARATOR, TAB_ASCII) +
					pubTime + SEPARATOR + numRetweets + SEPARATOR + numFavorites;
		} else {
			return id + SEPARATOR + username + SEPARATOR + 
					text + SEPARATOR + pubTime + SEPARATOR + 
					numRetweets + SEPARATOR + numFavorites;
		}
	}
	
	// de-serialize from an appropriately formatted String
	public static TweetFields fromString(String tweetFieldsInString){
		String[] parts = tweetFieldsInString.split(SEPARATOR);
		// id = parts[0], username = parts[1], text = parts[2], publicationTime = parts[3], 
		// numRetweets = parse(parts[4]), numFavorites = parse(parts[5])
		// make sure you change tab character
		String text = parts[2].replaceAll(TAB_ASCII, SEPARATOR);

		return new TweetFields(parts[0], parts[1], text, parts[3], 
				Integer.parseInt(parts[4]), Integer.parseInt(parts[5]));
		
	}
	
	
	// Override: hashCode and equals -> A TweetFields is identified by its id.
	@Override
	public int hashCode() {
		return id.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TweetFields){
			return id.equals(((TweetFields)obj).getId());
		} else {
			return false;
		}
	}
}
