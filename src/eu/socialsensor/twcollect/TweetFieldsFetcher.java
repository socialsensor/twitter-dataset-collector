package eu.socialsensor.twcollect;

import java.io.IOException;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * This is a wrapper class that downloads the HTML page for a single tweet
 * and extracts the fields of interest (with the help of jsoup).
 * @author kleinmind
 *
 */
public class TweetFieldsFetcher {

	// constants
	private static final String TWITTER = "http://twitter.com/";
	private static final String DEFAULT_TWITTER_STATUS_ROOT = "username/status/";
	
	public TweetFieldsResponse fetchTweetFields(String tweetId){
		// generate twitter URL (this one creates a redirect!)
		String twitterURL = TWITTER + DEFAULT_TWITTER_STATUS_ROOT + tweetId;
		
		// keep track of time
		long time0 = System.currentTimeMillis(); 
		
		Connection.Response response = null;
		try {
			// this is where the download takes place
			response = Jsoup.connect(twitterURL).execute();
		} catch (HttpStatusException e) {
			System.err.println(twitterURL);
			return createResponse(null, e.getStatusCode(), time0, false);
		} catch (IOException e) {
			e.printStackTrace();
			return createOtherErrorResponse(time0);
		}
		
		// parsing the returned HTML code
		Document doc = null;
		try {
			doc = response.parse();
		} catch (IOException e) {
			e.printStackTrace();
			return createParseErrorResponse(time0);
		}
		
		// extract username
		String username = doc.select(".user-actions").attr("data-screen-name");
		
		// extract text
		Elements textEl = doc.select(".js-tweet-text");
		if (textEl == null || textEl.first() == null){
			// is the user suspension the only reason for getting a null text?
			System.err.println(twitterURL + " " + username);
			return createResponse(null, response.statusCode(), time0, true);
		}
		String text = textEl.first().text();
		
		// should always be there
		String publicationTime = doc.select(".tweet-timestamp").attr("title");
		
		// retweets (if available)
		Elements numRetweetsEl 	= doc.select(".stats .js-stat-retweets a strong");
		int numRetweets = 0;
		if (numRetweetsEl.text().length()>0) {
			numRetweets = Integer.parseInt(numRetweetsEl.text().replaceAll(",",""));
		}
		
		// favorites (if available)
		Elements numFavoritesEl	= doc.select(".stats .js-stat-favorites a strong");
		int numFavorites = 0;
		if (numFavoritesEl.text().length()>0){
			numFavorites = Integer.parseInt(numFavoritesEl.text().replaceAll(",",""));
		}	
		
		TweetFields tweetFields = new TweetFields(tweetId, username, text, 
				publicationTime, numRetweets, numFavorites);
		
		return createResponse(tweetFields, response.statusCode(), time0, false);
	}
	
	private TweetFieldsResponse createResponse(TweetFields tweetFields, int status, long time0, boolean suspended){
		return new TweetFieldsResponse(tweetFields, status, (int)(System.currentTimeMillis() - time0), suspended, false, false);
	}
	private TweetFieldsResponse createParseErrorResponse(long time0){
		return new TweetFieldsResponse(null, -1, (int)(System.currentTimeMillis() - time0), false, true, false);
	}
	private TweetFieldsResponse createOtherErrorResponse(long time0){
		return new TweetFieldsResponse(null, -1, (int)(System.currentTimeMillis() - time0), false, false, true);
	}
}
