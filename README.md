twitter-dataset-collector
=========================

<p>The project facilitates the distribution of Twitter datasets by downloading sets of tweets (if still available) using their ids as input and by providing simple wrapping around the popular Twitter4j client. It is very similar to the popular <a href="https://github.com/myleott/twitter-corpus-tools">twitter-corpus-tools</a>. However, it is significantly more simple to use since it has very few dependencies (<a href="http://jsoup.org/">jsoup</a> and <a href="http://twitter4j.org/en/index.html">twitter4j</a>) and simple text-based input/output file formats. We recommend to use the collector with moderation (i.e. do not use too many threads and do not leave it running for too long) and only for research purpuses (i.e. to distribute Twitter datasets and reproduce results).</p>

<h2><u>Instructions</u></h2>

<h3>Twitter fetching</h3>
For large sets of tweets (many tens of thousands), you probably want to use the multi-threaded implementation of the collector. In class `eu.socialsensor.twcollect.TweetCorpusDownloader` just run the method:

    void downloadIdsMultiThread(String idsFile, String responsesLogFile, boolean resume, int nrThreads)
    
providing as arguments the name of the file (`idsFile`) with the tweet IDs, one ID per line, the name of the file (`responsesLogFile`) where the collected tweets (plus some additional information) will be logged, a boolean flag (`resume`) indicating whether the downloading should resume or start from scratch, and the number of threads (`nrThreads`) that will be used for parallelizing the requests-responses to/from twitter.com. Note that in case that `resume` is set to true and a file `responsesLogFile` already exists (from a previous execution of the method), the collector will skip the requests for the already collected tweets. 

The same arguments (with the exception of `nrThreads`) can be used with the single-threaded implementation:

    void downloadIds(String idsFile, String responsesLogFile, final boolean resume)

<h3>Twitter4j wrapper</h3>
To make use of the Twitter Streaming API, you would need to use the Twitter4j wrapper class `eu.socialsensor.twcollect.StreamCollector`. To do so, you first need to fill in your API credentials in the `twitter4j.properties` file and then run the `main` method of the class. By default the class makes use of a predefined list of Twitter user ids (from the file `seeds.txt` as well as a set of keywords (from the file `keywords.txt`) as filters to the Streaming API. Note that the shutdown hook of the class may not work properly (and therefore fail to finalize resources), e.g. when the `main` method is invoked and terminated from within the eclipse IDE.  

<h2><u>Technical considerations</u></h2>

Running the multi-threading implementation using 10 threads, we processed the set of over 27K tweets contained in file `tweets_27K.txt` in a bit more than an hour, i.e. we achieved an average throughput of 6.7tweets/sec. With the single-threaded implementation, it took us around 1.25secs per tweet. The tweet ids were collected during the US Elections (November 6) 2012, and the test was performed 11 December 2013 (more than a year a later). As a result, only 78% of the 27,250 tweets were downloaded. There were 5,616 tweets that were not available (probably removed by their authors), 321 tweets from suspended accounts, and 45 tweets that failed to download for other reasons.

<h2><u>Contact</u></h2>

<p>For more information or support, contact: papadop@iti.gr or symeon.papadopoulos@gmail.com</p>
