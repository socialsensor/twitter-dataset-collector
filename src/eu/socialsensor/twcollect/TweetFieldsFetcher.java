package eu.socialsensor.twcollect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
			return createResponse(new TweetFields(tweetId, null, null, null), e.getStatusCode(), time0, false);
		} catch (IOException e) {
			e.printStackTrace();
			return createOtherErrorResponse(tweetId, time0);
		}
		
		// parsing the returned HTML code
		Document doc = null;
		try {
			doc = response.parse();
		} catch (IOException e) {
			e.printStackTrace();
			return createParseErrorResponse(tweetId, time0);
		}
		
		// extract parsed id
		String resolvedUrl = response.url().getPath();
		int lastSlashIdx = resolvedUrl.lastIndexOf('/');
		String originalId = resolvedUrl.substring(lastSlashIdx+1);
		
		// extract username
		String username = doc.select(".user-actions").attr("data-screen-name");
		
		// extract text
		Elements textEl = doc.select(".js-tweet-text"); // this is not the most appropriate way to check
		if (textEl == null || textEl.first() == null){
			// is the user suspension the only reason for getting a null text?
			System.err.println(twitterURL + " (suspeneded)");
			return createResponse(new TweetFields(tweetId, null, null, null), response.statusCode(), time0, true);
		}
		String text = textEl.first().text();
		
		// should always be there, but not correct for response tweets
		String publicationTime = doc.select(".tweet-timestamp").attr("title");		
		
		Elements mainEl = doc.select(".permalink-tweet");
		if (mainEl != null && mainEl.first() != null){
			Elements inEl = mainEl.first().select(".js-tweet-text");
			if (inEl != null && inEl.first() != null){
				text = inEl.first().text();	
			}
			// get correct publication time
			publicationTime = mainEl.select(".metadata").first().text();
		}
		
		// get tweets to which this tweet replies (if available)
		Elements repEl = doc.select(".permalink-in-reply-tos");
		String[] responseTos = null;
		if (repEl != null && repEl.first() != null){
			List<String> respIds = new ArrayList<String>();
			Elements inEl = repEl.first().select(".simple-tweet");
			if (inEl != null){
				for (int i = 0; i < inEl.size(); i++){
					respIds.add(inEl.get(i).attr("data-tweet-id"));
				}
			}
			responseTos = new String[respIds.size()];
			for (int i = 0; i < respIds.size(); i++){
				responseTos[i] = respIds.get(i);
			}
		}

		
		// retweets (if available)
		Elements numRetweetsEl 	= doc.select(".stats .js-stat-retweets a strong");
		int numRetweets = 0;
		if (numRetweetsEl.text().length()>0) {
			numRetweets = Integer.parseInt(numRetweetsEl.text().replaceAll(String.valueOf((char)160),"").replaceAll(",",""));
		}
		
		// favorites (if available)
		Elements numFavoritesEl	= doc.select(".stats .js-stat-favorites a strong");
		int numFavorites = 0;
		if (numFavoritesEl.text().length()>0){
			numFavorites = Integer.parseInt(numFavoritesEl.text().replaceAll(String.valueOf((char)160),"").replaceAll(",",""));
		}	
		
		TweetFields tweetFields = null;
		if (tweetId.equals(originalId)) {
			// original tweet
			tweetFields = new TweetFields(tweetId, username, 
					text, publicationTime, numRetweets, numFavorites, null, responseTos); 
		} else {
			tweetFields = new TweetFields(tweetId, username,
					text, publicationTime, numRetweets, numFavorites, originalId, null);
		}
				
		
		return createResponse(tweetFields, response.statusCode(), time0, false);
	}
	
	private TweetFieldsResponse createResponse(TweetFields tweetFields, int status, long time0, boolean suspended){
		return new TweetFieldsResponse(tweetFields, status, (int)(System.currentTimeMillis() - time0), suspended, false, false);
	}
	private TweetFieldsResponse createParseErrorResponse(String tweetId, long time0){
		return new TweetFieldsResponse(new TweetFields(tweetId, null, null, null), -1, (int)(System.currentTimeMillis() - time0), false, true, false);
	}
	private TweetFieldsResponse createOtherErrorResponse(String tweetId, long time0){
		return new TweetFieldsResponse(new TweetFields(tweetId, null, null, null), -1, (int)(System.currentTimeMillis() - time0), false, false, true);
	}
}
