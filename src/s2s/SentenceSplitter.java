package s2s;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class responsible for splitting the sentences. 
 * @author rzvoncek
 *
 */

public class SentenceSplitter {

	private TrackTreeRoot trackTree;
	private SpotifyAdapter spotify;
	
	public SentenceSplitter() {
		trackTree = new TrackTreeRoot();
		spotify = new SpotifyAdapter();
	}

	
	/**
	 * Splits sentence into chunks according to known song titles. Known tracks have
	 * Spotify URI attached. Unmatched chunks are represented by tracks by blank URI.
	 * @param sentence
	 * @return List of tracks. 
	 */
	public List<Track> splitToTracks(String sentence) {

		List<Track> result = new ArrayList<Track>();

		String[] words = SentenceSplitter.splitToWords(sentence);

		// pull the song titles from Spotify
		trackTree.importTracks(
				spotify.fetchTracks(words));
		
		SentencePrefix prefix;

		do { 
			
			prefix = trackTree.findLongestPrefix(words);
		
			if ( prefix == null || ( prefix.getLastTrack() == null && prefix.getLastKnownTrack() == null  )  ) {
				// found no song matching the input word
				// or no known track for the given input.
				// cut the first word try again
				result.add(new Track(words[0],""));
				words = Arrays.copyOfRange(words, 1, words.length); 

			} else if ( prefix.getLastTrack() == null && prefix.getLastKnownTrack() != null  ) {
				// found some prefix in our tree, but the last matching word
				// has no track associated with it.
				// backtrack to last known track
				result.add(prefix.getLastKnownTrack());
				words =  Arrays.copyOfRange(words, prefix.getLastKnownLength(), words.length);
				
			} else if ( prefix.getLastTrack() != null ) {
				// found a song exactly where a prefix path ended
				// cut the prefix and continue with the rest of the sentence
				result.add(prefix.getLastTrack());
				words = Arrays.copyOfRange(words, prefix.getPrefixLen(), words.length);
			}
			
		} while (words.length != 0);
		
		return result;
	}

	
	/**
	 * Splits a sentence to words, or set of words if a word is too short.
	 * Does the split by the ' ' character. Later on this can be replaced by
	 * some more elaborate logic (e.g. based on English semantics).
	 * @param sentence to split
	 * @return array of words
	 */
	public static String[] splitToWords(String sentence) {
		
		String s = InputReader.sanitize(sentence);
		
		return s.split(" ");
		
	}
	
	public void shutDown() {
		spotify.shutDown();
	}
	
}
