package s2s;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 
 * Root of the tree storing the known song tiles.
 * 
 * First words from all known song titles are stored in the trackTree map. Values
 * of the trackTree map are pointers to TrackTreeNodes containing next word of a
 * song title. Traversing the tree by looking up track title words eventually 
 * leads to a track of the given title, provided the track is known.
 * 
 * @author rzvoncek
 *
 */

public class TrackTreeRoot {

	
	Map<String,TrackTreeNode> trackTree;
	SpotifyAdapter spotify;

	public TrackTreeRoot() {
		trackTree = new HashMap<String,TrackTreeNode>();
		spotify = new SpotifyAdapter();
		
	}
	
	/**
	 * Traverses the tree searching for the longest known song title prefix.
	 * 
	 * Remembers the last encountered matching prefix and can therefore backtrack
	 * in case the input sentence runs out of words before another track is found
	 * in the tree.
	 * 
	 * @param words
	 * @return
	 */
	public SentencePrefix findLongestPrefix(String[] words) {
	
		List<String> nextWords = new ArrayList<String>(Arrays.asList(words));
		TrackTreeNode ttn,ttnChild;
		
		StringBuffer prefix = new StringBuffer();
		int prefixLen = 0;
		String nextWord;
		
		String lastPrefix = "";
		int lastCnt = 0;
		Track lastTrack = null;
		
		if ( words.length == 0 )
			return null;
		
		ttn = trackTree.get(nextWords.get(0));
		
		if ( ttn == null ) {
			return null;
		}
		
		nextWords.remove(0);

		// word was found, remember it and check the children
		prefix.append(ttn.getWord() + " ");
		prefixLen++;
		
		if ( nextWords.size() == 0 ) 
			return new SentencePrefix(prefix.toString(),prefixLen,ttn.getTrack());
		
		do {

			// save track for possible backtracking
			if ( ttn.getTrack() != null ) {
				lastPrefix = prefix.toString();
				lastCnt = prefixLen;
				lastTrack = ttn.getTrack();
			}
			
			nextWord = nextWords.get(0);
			
			// is there next matching word ?
			ttnChild = ttn.getChild(nextWord);
			if ( ttnChild != null ) {
				prefix.append(nextWord + " ");
				nextWords.remove(0);
				prefixLen++;
				ttn = ttnChild;
				
			} else {
				// can't continue the prefix
				break;
			}
		} while ( nextWords.size() != 0 && words.length > prefixLen);
		
		return new SentencePrefix(prefix.toString(),prefixLen,ttn.getTrack(),lastPrefix,lastCnt,lastTrack);
		
	}
	
	
	/**
	 * Imports tracks fetched from Spotify DB into the trackTree.
	 * 
	 * Parallelizing this method led to minor speedup and was 
	 * therefore dropped.
	 * @param newTracks
	 */
	public void importTracks(List<Track> newTracks) {
		

		for ( Track t : newTracks ) {
		
			String[] words = SentenceSplitter.splitToWords(t.getName());
			
			if ( words.length == 0 ) 
				continue;
			
			TrackTreeNode ttn = trackTree.get(words[0]);
			
			if ( ttn == null ) {
				// there are no songs starting with given word 
				insertNewTrack(t);
			}
			else {
				// there are some songs starting with the given word
				insertExistingTrack(t,ttn,words);
			}
		}
		
	}	
	
	/**
	 * Inserts a track whose first was not present in trackTree yet.
	 * @param t
	 */
	private void insertNewTrack(Track t) {
		
		TrackTreeNode ttn;
		
		String[] words = SentenceSplitter.splitToWords(t.getName());
		
		if ( words.length == 0 )
			return;
		
		// put an entry to the root node
		ttn = new TrackTreeNode(words[0],null,0);
		trackTree.put(words[0], ttn);
		
		// track name is 1 word -> no need to add any children
		if ( words.length == 1 ) {
			ttn.addTrack(t);
			return;
		}
		
		insertNewTrack(ttn, Arrays.copyOfRange(words, 1, words.length), t);

	}
	
	/**
	 * Inserts a track that already has a portion of its title prefix present in the
	 * trackTree.
	 * @param t
	 * @param rootNode
	 * @param words
	 */
	private void insertExistingTrack(Track t, TrackTreeNode rootNode, String[] words) {
		
		// find matching prefix
		TrackTreeNode ttn = lookupNode(rootNode,words);
		
		// depth == 0 still requires cutting one word
		int cutFrom = 0;
		if ( ttn.getDepth() == 0 ) {
			cutFrom = 1;
		} else {
			cutFrom = ttn.getDepth();
		}
		
		String[] wordsReminder = Arrays.copyOfRange(words, cutFrom, words.length);
		
		// if wordsReminder is empty, put track to ttn! 
		if ( wordsReminder.length == 0) {
			ttn.addTrack(t);
		} 

		// insert the rest of the song title anew
		else {
			insertNewTrack(ttn,wordsReminder,t);
		}
		
	}

	/**
	 * Searches for the longest known matching prefix for a given node.
	 * @param rootNode from where start traversing
	 * @param words name of the looked-up node
	 * @return
	 */
	private TrackTreeNode lookupNode(TrackTreeNode rootNode, String[] words) {
		
		TrackTreeNode ttn = rootNode;
		TrackTreeNode ttnChild;
		
		for ( int i=0;i<words.length;i++ ) {
			
			ttnChild = rootNode.getChild(words[i]);

			if ( ttnChild == null )
				return ttn;
			else {
				ttn = ttnChild;
			}
		}
		return ttn;
	}

	/**
	 * Insert a track under an existing non-root node.
	 * @param ttnStart
	 * @param words
	 * @param t
	 */
	private void insertNewTrack(TrackTreeNode ttnStart, String[] words, Track t) {
		
		TrackTreeNode ttn = ttnStart;
		TrackTreeNode ttnChild;
		
		for ( int i=0;i<words.length-1;i++) {
			
			ttnChild = ttn.getChild(words[i]);
			if ( ttnChild == null ) {
				ttnChild = new TrackTreeNode(words[i],null,ttn.getDepth()+1);
			}

			ttn.addChild(words[i], ttnChild);
			ttn = ttnChild;
		}
		
		ttnChild = new TrackTreeNode(words[words.length-1],t,ttn.getDepth()+1);
		ttn.addChild(words[words.length-1], ttnChild);
	}	
	
}
