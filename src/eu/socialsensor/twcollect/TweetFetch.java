package eu.socialsensor.twcollect;

import java.util.concurrent.Callable;

/**
 * Models the task-process of fetching a tweet and extracting TweetFields from it 
 * @author papadop
 *
 */
public class TweetFetch implements Callable<TweetFieldsResponse> {

	
	private final TweetFieldsFetcher fetcher = new TweetFieldsFetcher();
	private final String tweetId;
	
	public TweetFetch(String tweetId){
		this.tweetId = tweetId;
	}
	
	@Override
	public TweetFieldsResponse call() throws Exception {
		return fetcher.fetchTweetFields(tweetId);
	}

}
