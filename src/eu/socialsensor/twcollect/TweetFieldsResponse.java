package eu.socialsensor.twcollect;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


/**
 * Container class to encapsulate the TweetFields and information about
 * the response of twitter.com
 * @author kleinmind
 *
 */
public class TweetFieldsResponse {

	public static void main(String[] args) {
		String responseFile = "D:/socialsensor/code/twitter-dataset-collector/snow14_devset_tweets_v2.txt";//"responses.txt";//"D:/socialsensor/data/uselections_tweets/aggregate.txt";//"add a file created from TweetCorpusDownloader";
		TweetFieldsResponse.reportResults(responseFile);
	}
	
	private final TweetFields tweet;
	private final int status;
	private final int msecSpent;
	private final boolean parsingError;
	private final boolean otherError;
	private final boolean suspended;		// the twitter account is suspended
	
	// constructor to call when some HTTP response is available
	public TweetFieldsResponse(TweetFields tweet, int status, int msecSpent){
		this(tweet, status, msecSpent, false, false, false);
	}
	
	// constructor to call if something went wrong and no HTTP response is available
	public TweetFieldsResponse(TweetFields tweet, int status, 
			int msecSpent, boolean suspended, boolean parsingError, boolean otherError){
		this.tweet 		= tweet;
		this.status 	= status;
		this.msecSpent 	= msecSpent;
		this.suspended = suspended;
		this.parsingError = parsingError;
		this.otherError = otherError;
	}

	// Getters
	public TweetFields getTweet() {
		return tweet;
	}
	public int getStatus() {
		return status;
	}
	public int getMsecSpent() {
		return msecSpent;
	}
	public boolean isSuspended(){
		return suspended;
	}
	public boolean isParseError(){
		return parsingError;
	}
	public boolean isOtherError(){
		return otherError;
	}

	private static String SEPARATOR = "\t";
		
	@Override
	public String toString() {
		// serialize in a single line
		return status + SEPARATOR + suspended + SEPARATOR + parsingError + 
				SEPARATOR + otherError + SEPARATOR + msecSpent + SEPARATOR + tweet;
	}

	// de-serialize from an appropriately formatted String
	public static TweetFieldsResponse fromString(String tweetFieldsResponseInLine){
		
		String[] parts = tweetFieldsResponseInLine.split(SEPARATOR);
		
		if (parts[7].equals("null")){
			// tweet is not available
			return new TweetFieldsResponse(new TweetFields(parts[6], null, null, null), Integer.parseInt(parts[0]), 
					Integer.parseInt(parts[4]), Boolean.parseBoolean(parts[1]),
					Boolean.parseBoolean(parts[2]), Boolean.parseBoolean(parts[3]));
		} else {
			// recreate TweetFields string and parse it with the utility method of TweetFields
			TweetFields tweetFields = TweetFields.fromString(
					parts[5] + SEPARATOR + parts[6] + 
					SEPARATOR + parts[7] + SEPARATOR + parts[8] + SEPARATOR + 
					parts[9] + SEPARATOR + parts[10] + SEPARATOR + parts[11] + SEPARATOR + parts[12]);
			return new TweetFieldsResponse(tweetFields, Integer.parseInt(parts[0]), 
					Integer.parseInt(parts[4]), Boolean.parseBoolean(parts[1]), 
					Boolean.parseBoolean(parts[2]), Boolean.parseBoolean(parts[3]));
		}
	}
	
	public String responseSummary(){
		if (tweet == null){
			return status + " " + tweet.getId() + " " + msecSpent + "msecs";
		} else {
			return status + " " + "Fail" + " " + msecSpent + "msecs";
		}
	}
	
	
	
	// utility methods
	
	// reads only tweets that have been successfully downloaded
	// as well as tweets that were removed or suspended (since there is no chance
	// of them being downloaded)
	public static Set<String> readIds(String responseLogFile){
		return readIds(responseLogFile, 1000);
	}
	
	// return the ids of tweets that satisfy the conditions of code:
	// - code = 200 -> response.getStatus() == 200
	// - code = 404 -> response.getStatus() == 404
	// - code = 0 -> user is suspended
	// - code = -1 -> is parse error
	// - code = -2 -> is other error
	// - code = 1000 -> tweets whose ids have been checked, meaning either code 200 or 404 or user suspended
	public static Set<String> readIds(String responseLogFile, final int code){
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(responseLogFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Set<String> ids = new HashSet<String>();
		String line = null;
		try {
			while ((line = reader.readLine()) != null){
				TweetFieldsResponse response = TweetFieldsResponse.fromString(line);
				switch (code){
				case -2:
					if (response.isOtherError()){
						ids.add(response.getTweet().getId());
					}
					break;
				case -1:
					if (response.isParseError()){
						ids.add(response.getTweet().getId());
					}
					break;
				case 0:
					if (response.isSuspended()){
						ids.add(response.getTweet().getId());
					}
					break;
				case 200:
					if (response.getStatus() == 200){
						ids.add(response.getTweet().getId());
					}
					break;
				case 404:
					if (response.getStatus() == 404){
						ids.add(response.getTweet().getId());
					}
					break;
				case 1000:
					if ((response.getStatus() == 200) || 
							(response.getStatus() == 404) || 
							response.isSuspended()){
						ids.add(response.getTweet().getId());
					}
					break;
				default:
					// add all ids
					ids.add(response.getTweet().getId());
					break;
				}
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ids;
	}
	
	public static void reportResults(String responseLogFile){
		
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(responseLogFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		int countSuccess = 0;
		int countOriginal = 0;
		int countRetweets = 0;
		int countResponses = 0;
		int countSuspended = 0;
		int countParseErrors = 0;
		int countOtherErrors = 0;
		double totalMsec = 0.0;
		Map<Integer,Integer> statusMap = new HashMap<Integer,Integer>();
		int nrResponses = 0;
		String line = null;
		
		try {
			while ((line = reader.readLine()) != null){
				TweetFieldsResponse response = TweetFieldsResponse.fromString(line);
				nrResponses++;
				
				if ((!response.isSuspended()) && (!response.isParseError()) && 
						(!response.isOtherError())){ countSuccess++; }
				if (response.isSuspended()){ countSuspended++; }
				if (response.isParseError()){ countParseErrors++; }
				if (response.isOtherError()){ countOtherErrors++; }
				
				if (response.getTweet().isRetweeet()) {
					countRetweets++;
				}
				if (response.getTweet().isReply()) {
					countResponses++;
				}
				if (response.getStatus() == 200 && (!response.getTweet().isRetweeet()
						&& response.getTweet().getText()!=null)){
					countOriginal++;
				}
				
				Integer statusInMap = statusMap.get(response.getStatus());
				if (statusInMap == null){
					statusMap.put(response.getStatus(), 1);
				} else {
					statusMap.put(response.getStatus(), statusInMap+1);
				}
				totalMsec += response.getMsecSpent();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Success(%): " + (100.0*countSuccess)/nrResponses);
		System.out.println("Avg. response time: " + totalMsec/nrResponses + "msecs");
		System.out.println("Total: " + nrResponses);
		System.out.println("Original: " + countOriginal);
		System.out.println("Retweets: " + countRetweets);
		System.out.println("Responses: " + countResponses);
		System.out.println("Suspended: " + countSuspended);
		System.out.println("Parse errors: " + countParseErrors);
		System.out.println("Other errors: " + countOtherErrors);
		for (Entry<Integer, Integer> entry : statusMap.entrySet()){
			System.out.println("Status " + entry.getKey() + ": " + entry.getValue());
		}
	}
	
	

	
}
