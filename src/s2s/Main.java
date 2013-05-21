package s2s;

/**
 * sentence2songs - build given sentence out of song titles
 * 
 * This utility reads lines from standard input and attempts 
 * to compose them out of song titles pulled from Spotify database
 * (http://http://developer.spotify.com/technologies/web-api/).
 * 
 * Main features:
 * 	- parallel queries and response processing to Spotify database
 *  - query response caching from Apache's HTTP client
 *  - tree-based internal representation of known tracks titles
 *    - allows fast lookup of repeated queries
 * 
 * @author rzvoncek
 *
 */

public class Main {

	public static void main(String[] args) {

		SentenceSplitter sentenceSplitter = new SentenceSplitter();
		
		new InputReader(sentenceSplitter).start();
		
	}

}

