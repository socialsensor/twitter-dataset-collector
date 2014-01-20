package eu.socialsensor.twcollect;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


/**
 * Utility class for downloading a set of tweets (using their IDs) and keeping 
 * track of some statistics
 * @author kleinmind
 *
 */
public class TweetCorpusDownloader {

	// very simple example of multi-threading downloading
	public static void main(String[] args) {
		String idFile = "tweets_200.txt";
		String responseFile = "responses.txt";

		try {
			downloadIdsMultiThread(idFile, responseFile, true, 10);
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	// assumes that files ending with "txt" contain ids
	protected static String[] getIdFiles(String idFileDirectory) {
		File idFileDir = new File(idFileDirectory);
		if (idFileDir.isDirectory()){
			String[] files = idFileDir.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					if (name.endsWith("txt")){
						return true;
					}
					return false;
				}
			});
			return files;
		}
		return new String[0];
	}
	
	/**
	 * 
	 * @param nrThreads This matters if one wants to use the multi-threading method
	 */
	public TweetCorpusDownloader(int nrThreads){
		// initialize
		downloadExecutor = Executors.newFixedThreadPool(nrThreads);
		pool = new ExecutorCompletionService<TweetFieldsResponse>(downloadExecutor);
		numPendingTasks = 0;
		maxNumPendingTasks = nrThreads * 10;
	}
	
	protected static final String UTF8 = "UTF-8";
	
	
	// idsFile: file with tweet IDs, one tweet ID per line
	// responsesLogFile: file where responses will be logged - if it already exists, 
	//						it will be used for resuming
	// resume: if true, the responsesLogFile will be used as a starting point (no redownloading)
	//			if false, all IDs will be redownloaded and the responsesLogFile will be overwritten
	public static void downloadIds(String idsFile, String responsesLogFile, 
			final boolean resume){
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(idsFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		String tempOutput = responsesLogFile + ".tmp";
				
		Set<String> existingIds = new HashSet<String>();
		if (resume){
			if ((new File(responsesLogFile)).exists()){
				existingIds = TweetFieldsResponse.readIds(responsesLogFile);
				// copy "existingIds" lines to temporary output file
				copyExistingIds(responsesLogFile, tempOutput, existingIds);
			}
		}
		
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(tempOutput, resume),UTF8));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		TweetFieldsFetcher fetcher = new TweetFieldsFetcher();
		String tweetId = null;
		int countLine = 0;
		try {
			while ((tweetId = reader.readLine()) != null){
				countLine++;
				if (existingIds.contains(tweetId)){
					continue;
				}
				System.out.println(countLine);
				TweetFieldsResponse response = fetcher.fetchTweetFields(tweetId);
				writer.write(response.toString());
				writer.newLine();
			}
			writer.close();
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// copy temporary file to original response file
		try {
			Files.move(Paths.get(tempOutput), Paths.get(responsesLogFile), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// same as above
	// nrThreads defines the number of threads used to fetch twitter URLs
	// use with caution!
	public static void downloadIdsMultiThread(String idsFile, 
			String responsesLogFile, boolean resume, int nrThreads) throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader(idsFile));
		// first count the number of tweets to download
		int nrTweets = 0;
		while (reader.readLine() != null){
			nrTweets++;
		}
		reader.close();
		// reopen
		reader = new BufferedReader(new FileReader(idsFile));
		
		String tempOutput = responsesLogFile + ".tmp";
		
		Set<String> existingIds = new HashSet<String>();
		if (resume){
			if ((new File(responsesLogFile)).exists()){
				existingIds = TweetFieldsResponse.readIds(responsesLogFile);
				System.out.println("Loaded " + existingIds.size() + " ids");
				nrTweets -= existingIds.size(); // we are not going to count those as download tasks
				// copy "existingIds" lines to temporary output file
				copyExistingIds(responsesLogFile, tempOutput, existingIds);
			}
		}
				
		TweetCorpusDownloader downloader = new TweetCorpusDownloader(nrThreads);
		
		// Not yet sure whether a ParallelWriter is really necessary.
		File responseFile = new File(tempOutput);
		ParallelWriter pwriter = new ParallelWriter(responseFile, resume);
		new Thread(pwriter).start();
		
		int submittedCounter = 0;
        int completedCounter = 0;
        int failedCounter = 0;
        
        long start = System.currentTimeMillis();
        while (true) {

                // if there are more tasks to submit and the downloader can accept more tasks then submit
                while (submittedCounter < nrTweets && downloader.canAcceptMoreTasks()) {
                        String tweetId = reader.readLine();
                        if (existingIds.contains(tweetId)) { continue; }
                        submittedCounter++;
                        downloader.submitTweetFetchTask(tweetId);
                }
                // if there are submitted tasks that are pending completion, try to consume
                if (completedCounter + failedCounter < submittedCounter) {
                	try {
                		TweetFieldsResponse response = downloader.getTweetFieldsResponseWait();
                		pwriter.append(response.toString());
                        completedCounter++;
                        System.out.println(completedCounter + " downloads completed!");
                     } catch (Exception e) {
                    	failedCounter++;
                        System.out.println(failedCounter + " downloads failed!");
                        System.out.println(e.getMessage());
                     }
                }
                // if all tasks have been consumed then break;
                if (completedCounter + failedCounter == nrTweets) {
                	downloader.shutDown();
                    reader.close();
                    break;
                }
        }
        
        long end = System.currentTimeMillis();
        System.out.println("Total time: " + (end - start) + " ms");
        System.out.println("Downloaded tweets: " + completedCounter);
        System.out.println("Failed tweets: " + failedCounter);
		
		pwriter.end();
		
		// copy temporary file to original response file
		Files.move(Paths.get(tempOutput), Paths.get(responsesLogFile), StandardCopyOption.REPLACE_EXISTING);
	}
	
	
	// fields used by the multi-threading fetching method
	private ExecutorService downloadExecutor;
    private CompletionService<TweetFieldsResponse> pool;
	private int numPendingTasks;
	private final int maxNumPendingTasks;
	
	
	// helper methods used by the multi-threading fetching method
	
	

	protected static void copyExistingIds(String responseFile, String tempFile, Set<String> existingIds){
		try {
			BufferedReader outReader = new BufferedReader(new InputStreamReader(new FileInputStream(responseFile), UTF8));
			BufferedWriter outWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), UTF8));
			
			String line = null;
			while ( (line = outReader.readLine()) != null) {
				TweetFieldsResponse response = TweetFieldsResponse.fromString(line);
				if (existingIds.contains(response.getTweet().getId())){
					// if the id has been "properly" downloaded then copy it
					outWriter.write(line);
					outWriter.newLine();
				}
			}
			
			outReader.close();
			outWriter.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	
	protected void submitTweetFetchTask(String tweetId) {
        Callable<TweetFieldsResponse> call = new TweetFetch(tweetId);
        pool.submit(call);
        numPendingTasks++;
    }
	
	protected TweetFieldsResponse getTweetFieldsResponse() throws Exception {
        Future<TweetFieldsResponse> future = pool.poll();
        if (future == null) { // no completed tasks in the pool
        	return null;
        } else {
            try {
            	TweetFieldsResponse response = future.get();
                return response;
            } catch (Exception e) {
                throw e;
            } finally {
                // in any case (Exception or not) the numPendingTask should be reduced
                numPendingTasks--;
            }
        }
    }
	
	
	protected TweetFieldsResponse getTweetFieldsResponseWait() throws Exception {
        try {
        	TweetFieldsResponse response = pool.take().get();
            return response;
        } catch (Exception e) {
            throw e;
        } finally {
        	//in any case (Exception or not) the numPendingTask should be reduced
            numPendingTasks--;
        }
	}
	
	
	protected boolean canAcceptMoreTasks() {
        if (numPendingTasks < maxNumPendingTasks) {
            return true;
        } else {
        	return false;
        }
	}
	
	protected void shutDown() throws InterruptedException {
        downloadExecutor.shutdown();
        downloadExecutor.awaitTermination(10, TimeUnit.SECONDS);
	}
}
