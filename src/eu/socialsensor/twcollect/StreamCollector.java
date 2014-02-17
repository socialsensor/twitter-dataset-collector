package eu.socialsensor.twcollect;


import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import eu.socialsensor.twcollect.util.FileUtil;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.json.DataObjectFactory;

public class StreamCollector {
	
	
	public static void main(String[] args) throws TwitterException, IOException{
	    
		StreamCollector collector = new StreamCollector();
		collector.setMaxJsonFileSize(100*1024); // 100MB batches
		collector.open("tweets.json");
		
		long[] seeds = FileUtil.convertStringToLongs(
				FileUtil.readTokensFromFile("seeds.txt"));
		String[] keywords = FileUtil.readTokensFromFile("keywords.txt");
		
		FilterQuery filter = new FilterQuery(seeds);
		filter.track(keywords);
		collector.startFilter(filter);
	}
	
	
	protected BufferedWriter writer = null;
	protected StatusListener listener = null;
	protected long maxJsonFileSize = 0; // (in KB) if >0, then the collector tries to create output json files 
								       // of approximately that size (i.e. by creating multiple files)
	protected long currentFileSize = 0; // counts the size of the currently opened file
	protected int fileCounter = 0; // counts the number of files written so far
	
	// set maximum JSON file size (in KB)
	public void setMaxJsonFileSize(long fileSize){
		this.maxJsonFileSize = fileSize;
	}

	
	protected void open(final String tweetDump) {
		openWriter(tweetDump + ".0");
		
		listener = new StatusListener(){
			public void onStatus(Status status) {
				try {
					String line = DataObjectFactory.getRawJSON(status);
					writer.append(line);
					writer.newLine();
					currentFileSize += line.length();
					if (maxJsonFileSize > 0){
						if (currentFileSize >= maxJsonFileSize*1024){
							closeWriter();
							fileCounter++;
							currentFileSize = 0;
							openWriter(tweetDump + "." + fileCounter);
						}
					}
				} catch (IOException e){
					e.printStackTrace();
					closeWriter();
				}
			}
			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}
			public void onException(Exception ex) {
				ex.printStackTrace();
			}
			@Override
			public void onScrubGeo(long arg0, long arg1) {
			}
			@Override
			public void onStallWarning(StallWarning arg0) {
				//System.out.println(arg0.toString());
			}
		};
		Runtime.getRuntime().addShutdownHook(new Shutdown(this));
	}
	
	protected void startFilter(FilterQuery filter){
		TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
		twitterStream.addListener(listener);
		twitterStream.filter(filter);
	}
	
	protected void close(){
		closeWriter();
	}
	
	
	protected void openWriter(String file){
		try {
			writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(file), FileUtil.UTF8));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	protected void closeWriter(){
		if (writer != null){
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Class in case system is shutdown: Responsible to close all services 
	 * that are running at the time being.
	 */
	protected class Shutdown extends Thread {
		StreamCollector process = null;

		public Shutdown(StreamCollector process) {
			this.process = process;
		}

		public void run() {
			System.out.println("Shutting down collector...");
			if (process != null) {
				process.close();
			}
			System.out.println("Done...");
		}
	}
	
	
	
}