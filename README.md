twitter-dataset-collector
=========================

<p>The project facilitates the distribution of Twitter datasets by downloading sets of tweets (if still available) using their ids as input. It is very similar to the popular <a href="https://github.com/myleott/twitter-corpus-tools">twitter-corpus-tools</a>. However, it is significantly more simple to use since it has only a single dependency (<a href="http://jsoup.org/">jsoup</a>) and simple text-based input/output file formats. We recommend to use the collector with moderation (i.e. do not use too many threads and do not leave it running for too long) and only for research purpuses (i.e. to distribute Twitter datasets and reproduce results).</p>

<h2><u>Instructions</u></h2>
For large sets of tweets (many tens of thousands), you probably want to use the multi-threaded implementation of the collector. In class `eu.socialsensor.twcollect.TweetCorpusDownloader` just run the method:

    void downloadIdsMultiThread(String idsFile, String responsesLogFile, boolean resume, int nrThreads)
    
providing as arguments the name of the file (`idsFile`) with the tweet IDs, one ID per line, the name of the file (`responsesLogFile`) where the collected tweets (plus some additional information) will be logged, a boolean flag (`resume`) indicating whether the downloading should resume or start from scratch, and the number of threads (`nrThreads`) that will be used for parallelizing the requests-responses to/from twitter.com. Note that in case that `resume` is set to true and a file `responsesLogFile` already exists (from a previous execution of the method), the collector will skip the requests for the already collected tweets. 

The same arguments (with the exception of `nrThreads`) can be used with the single-threaded implementation:

    void downloadIds(String idsFile, String responsesLogFile, final boolean resume)


<h2><u>Technical considerations</u></h2>

Running the multi-threading implementation using 10 threads, we processed the set of over 27K tweets contained in file `tweets_27K.txt` in a bit more than an hour, i.e. we achieved an average throughput of 6.7tweets/sec. With the single-threaded implementation, it took us around 1.25secs per tweet. The tweet ids were collected during the US Elections (November 6) 2012, and the test was performed 11 December 2013 (more than a year a later). As a result, only 78% of the 27,250 tweets were downloaded. There were 5,616 tweets that were not available (probably removed by their authors), 321 tweets from suspended accounts, and 45 tweets that failed to download for other reasons.

<h2><u>Contact</u></h2>

<p>For more information or support, contact: papadop@iti.gr or symeon.papadopoulos@gmail.com</p>
