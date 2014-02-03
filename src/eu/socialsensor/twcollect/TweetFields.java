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

	// if this is non-null the tweet is a retweet
	// and the rest of the fields (username, pubTime) refer to the original
	private final String originalId;
	
	// if this is non-null the tweet is a response to one or more tweets
	private final String[] responseTos;
	
	// constructor using all fields for an original tweet
	public TweetFields(String id, String username, String text, String pubTime,
			int numRetweets, int numFavorites){
		this(id, username, text, pubTime, numRetweets, numFavorites, null, null);
	}
	
	// constructor using all fields
	public TweetFields(String id, String username, String text, String pubTime,
			int numRetweets, int numFavorites, String originalId, String[] responseTos){
		this.id = id;
		this.originalId = originalId;
		
		// these fields refer to the original tweet
		this.username = username;
		this.text = text;
		this.pubTime = pubTime;
		this.numRetweets = numRetweets;
		this.numFavorites = numFavorites;
		
		this.responseTos = responseTos;
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
	
	public boolean isRetweeet() {
		if (originalId != null){
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isReply(){
		if (responseTos != null){
			return true;
		} else {
			return false;
		}
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
		String tweetText = text;
		if (text != null && text.contains(SEPARATOR)){
			tweetText = text.replaceAll(SEPARATOR, TAB_ASCII);
		}
		if (isReply()){
			StringBuffer buf = new StringBuffer();
			buf.append(responseTos[0]);
			for (int i = 1; i < responseTos.length; i++){
				buf.append("," + responseTos[i]);
			}
			return isTweetCode() + SEPARATOR + id + SEPARATOR + username + 
					SEPARATOR + tweetText + SEPARATOR + pubTime + SEPARATOR + numRetweets + 
					SEPARATOR + numFavorites + SEPARATOR + buf.toString();
		} else {
			return isTweetCode() + SEPARATOR + id + SEPARATOR + username + 
					SEPARATOR + tweetText + SEPARATOR + pubTime + SEPARATOR + numRetweets + 
					SEPARATOR + numFavorites + SEPARATOR + originalId;
		}
	}
	
	// possible values
	// O: original
	// R: retweet
	// Rp: reply
	// N: not available
	private String isTweetCode(){
		if (text == null){
			return "N";
		} else if (isRetweeet()){
			return "R";
		} else if (isReply()) {
			return "Rp";
		} else {
			return "O";
		}
	}
	
	// de-serialize from an appropriately formatted String
	public static TweetFields fromString(String tweetFieldsInString){
		String[] parts = tweetFieldsInString.split(SEPARATOR);
		// id = parts[1], username = parts[2], text = parts[3], publicationTime = parts[4], 
		// numRetweets = parse(parts[5]), numFavorites = parse(parts[6])
		// make sure you change tab character
		String text = parts[3].replaceAll(TAB_ASCII, SEPARATOR);
		String originalId = parts[7];
		if (parts[7].equals(NULL_STRING)){
			originalId = null;
		}
		if (text.equals(NULL_STRING)){
			text = null;
		}
		if (parts[0].equals("Rp")){
			// parse response ids
			return new TweetFields(parts[1], parts[2], text, parts[4], 
					Integer.parseInt(parts[5]), Integer.parseInt(parts[6]), null, parts[7].split(","));
		} else {
			return new TweetFields(parts[1], parts[2], text, parts[4], 
				Integer.parseInt(parts[5]), Integer.parseInt(parts[6]), originalId, null);
		}
	}
	protected static final String NULL_STRING = "null";
	
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
