package eu.socialsensor.twcollect.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.json.DataObjectFactory;

public class SerializedStreamReader {

	
	public static void main(String[] args) throws IOException, TwitterException {
		
		File jsonDir = new File("./");
		
		File[] files = jsonDir.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				if (name.contains(".json.")){
					return true;
				}
				return false;
			}
		});
		List<String> jsonFileList = new ArrayList<String>();
		for (int i = 0; i < files.length; i++) {
			jsonFileList.add(files[i].getCanonicalPath());
		}
		
		final long minTime = 1392660000000L;
		final long maxTime = 1392746400000L;
		
		//printJsonFileSummary(jsonFileList);
		extractSubsetOfTweetFields(jsonFileList, "rehearsal_meta_filtered.txt", new StatusTransformer() {
			
			@Override
			public String extractLine(Status status) {
				String id = String.valueOf(status.getId());
				String timestamp = String.valueOf(status.getCreatedAt().getTime());
				String username = status.getUser().getScreenName();
				return id + "\t" + timestamp + "\t" + username;
			}
			
			@Override
			public Map<String, String> extractKeyValues(Status status) {
				// we don't care about this
				return new HashMap<String, String>();
			}
		}, new StatusFilterer() {
			
			@Override
			public boolean acceptStatus(Status status) {
				if (status.getCreatedAt().getTime() >= minTime &&
						status.getCreatedAt().getTime() <= maxTime){
					return true;
				}
				return false;
			}
		});
		
		
	}
	
	public static void extractSubsetOfTweetFields(List<String> jsonFiles, String outputFile,
			 StatusTransformer transformer, StatusFilterer filterer) throws IOException, TwitterException {
		
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outputFile), FileUtil.UTF8));
		
		for (int i = 0; i < jsonFiles.size(); i++) {
			System.out.println(jsonFiles.get(i));
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(jsonFiles.get(i)), FileUtil.UTF8));
			String line = null;
			while ((line = reader.readLine())!= null){
				Status status = DataObjectFactory.createStatus(line);
				if (filterer.acceptStatus(status)){
					writer.append(transformer.extractLine(status));
					writer.newLine();
				}
			}
			reader.close();
			writer.flush();
		}
		writer.close();
	}
	
	public static void printJsonFileSummary(List<String> jsonFiles) throws IOException, TwitterException{
		
		// aggregators
		int count =  0;
		int countGeo = 0;
		int countRetweet = 0;
		int countResponses = 0;
		long minTime = Long.MAX_VALUE, maxTime = 0;
		Date minDate = null, maxDate = null;
		Set<Long> users = new HashSet<Long>();
		
		for (int i = 0; i < jsonFiles.size(); i++) {
			System.out.println(jsonFiles.get(i));
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(jsonFiles.get(i)), FileUtil.UTF8));
			String line = null;
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
		}
		
		System.out.println("Period: [" + new Timestamp(minTime) + "," + new Timestamp(maxTime) + "]");
		System.out.println("Period: [" + minDate.toString() + "," + maxDate.toString() + "]");
		
		System.out.println("#tweets: " + count);
		System.out.println("#geo: " + countGeo);
		System.out.println("#retweets: " + countRetweet);
		System.out.println("#replies: " + countResponses);
		System.out.println("#users: " + users.size());
		
	}
	
	
}