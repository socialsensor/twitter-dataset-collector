package eu.socialsensor.twcollect.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.json.DataObjectFactory;

public class SerializedStreamReader {

	
	public static void main(String[] args) throws IOException, TwitterException {
		String tweetDump = "tweets.json";
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(tweetDump), FileUtil.UTF8));
		String line = null;
		int count =  0;
		int countGeo = 0;
		int countRetweet = 0;
		int countResponses = 0;
		long minTime = Long.MAX_VALUE, maxTime = 0;
		Date minDate = null, maxDate = null;
		Set<Long> users = new HashSet<Long>();
		while ((line = reader.readLine())!= null){
			Status status = DataObjectFactory.createStatus(line);
			count++;
			users.add(status.getUser().getId());
			if (status.isRetweet()){
				countRetweet++;
			}
			if (status.getInReplyToStatusId() > 0){
				countResponses++;
			}
			if (status.getGeoLocation() != null){
				countGeo++;
			}
			long tstamp = status.getCreatedAt().getTime();
			
			if (tstamp < minTime){
				minTime = tstamp;
				minDate = status.getCreatedAt();
			}
			if (tstamp > maxTime){
				maxTime = tstamp;
				maxDate = status.getCreatedAt();
			}
		}
		reader.close();
		
		
		System.out.println("Period: [" + new Timestamp(minTime) + "," + new Timestamp(maxTime) + "]");
		System.out.println("Period: [" + minDate.toString() + "," + maxDate.toString() + "]");
		
		System.out.println("#tweets: " + count);
		System.out.println("#geo: " + countGeo);
		System.out.println("#retweets: " + countRetweet);
		System.out.println("#replies: " + countResponses);
		System.out.println("#users: " + users.size());
	}
	
}