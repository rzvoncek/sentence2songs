package s2s;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;

/**
 * Class responsible for communication with the Spotify database.
 * Based on Apache's caching HTTP client. HTTP requests are executed
 * in parallel.
 * @author rzvoncek
 *
 */

public class SpotifyAdapter {

	private static final int CACHE_ENTRY_MAX_CNT = 500;
	private static final int CACHE_ENTRY_MAX_SIZE = 3145728; // 3 MB
	
	private CacheConfig cacheConfig;
	private HttpClient httpClient;
	private long tLastRequest;
	
	ExecutorService queryExecExecutor;
	CompletionService<List<Track>> queryExecExecutorService;
	private final int QUERY_EXEC_WORKER_CNT = 
			Runtime.getRuntime().availableProcessors()*2;
	
	public SpotifyAdapter() {
		cacheConfig = new CacheConfig();
		cacheConfig.setMaxCacheEntries(CACHE_ENTRY_MAX_CNT);
		cacheConfig.setMaxObjectSize(CACHE_ENTRY_MAX_SIZE);

		httpClient = new CachingHttpClient(
				new DefaultHttpClient(new PoolingClientConnectionManager()),
				cacheConfig);
		
		queryExecExecutor = Executors.newFixedThreadPool(QUERY_EXEC_WORKER_CNT);
		queryExecExecutorService = 
				new ExecutorCompletionService<List<Track>>(queryExecExecutor);
		
		tLastRequest = 0;
	}

	/**
	 * Runs queries for all given words. Short words are concatenated together
	 * and executed as one query.
	 * 
	 * Ensures there will be no queries for words as 'I','we', etc.
	 * 
	 * @param words
	 * @return
	 */
	public List<Track> fetchTracks (String[] words) {
		
		List<Track> result = new ArrayList<Track>();
		int jobsScheduled = 0;
		String prevWord = "";
		String query;
		
		
		for ( String w : words ) {

			// the word is too short, save it for the next iteration
			if ( w.length() < 3 ) {
				prevWord = w;
				continue;
			}

			else {
				// there is a word saved from previous iteration
				if ( prevWord.length() != 0 ) {
					query = prevWord.concat(" ").concat(w);
				} else { 
					query = w;
				}
			}
			
			// instantiate and submit a new query execution worker
			ParallelQueryExec worker = new ParallelQueryExec(this,httpClient,query);
			queryExecExecutorService.submit(worker);
			jobsScheduled++;
			prevWord = "";

		}
		
		//  gather the results
		for (int i=0;i<jobsScheduled;i++) {
			List<Track> partialResult;
			try {
				// each worker will return a List of tracks it pulled
				partialResult = queryExecExecutorService.take().get();
			} catch (InterruptedException e) {
				System.err.println("ERROR: " + e.getMessage());
				continue;
			} catch (ExecutionException e) {
				System.err.println("ERROR: " + e.getMessage());
				continue;
			}
			if ( partialResult != null ) {
				// merge the partial List with the global result 
				result.addAll(partialResult);
			}
		}
		
		return result;
	}

	/**
	 * Allows workers maintain the query rate restriction in a thread-safe way.
	 * @return
	 */
	synchronized public long getLastRequestTime() {
		return this.tLastRequest;
	}

	/**
	 * Allows workers maintain the query rate restriction in a thread-safe way.
	 * @return
	 */
	synchronized public void setLastRequestTime(long tNow) {
		this.tLastRequest = tNow;
	}

}
