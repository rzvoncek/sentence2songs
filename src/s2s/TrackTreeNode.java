package s2s;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Represents a node of the internal tree keeping the known song titles.
 * 
 * Node is named by a word, contains list of tracks which tile ends with 
 * node's word. Also contains pointers to nodes which share title prefix
 * with this node.   
 * 
 * @author rzvoncek
 *
 */

public class TrackTreeNode {

	private String word;
	private List<Track> tracks;
	private Map<String,TrackTreeNode> children;
	private final int depth;
	private Random randomGenerator = new Random();
	
	public TrackTreeNode(String w, Track t, int d) {
		word = w;
		tracks = new ArrayList<Track>();
		if ( t != null ) 
			tracks.add(t);
		children = new HashMap<String,TrackTreeNode>();
		this.depth = d;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public void addChild(String w, TrackTreeNode child) {
		children.put(w, child);
	}
	
	public void addTrack(Track t) {
		this.tracks.add(t);
	}
	
	public TrackTreeNode getChild(String word) {
		return children.get(word);
	}
	
	public int getDepth() {
		return this.depth;
	}
	
	public Track getTrack() {
		
		if ( tracks.size() == 0 )
			return null;
		
		// return random track
		return this.tracks.get(randomGenerator.nextInt(tracks.size()));
	}
}
