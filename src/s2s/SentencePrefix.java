package s2s;

/**
 * Class representing a part of the input sentence that was successfully
 * built from known song titles (or their parts).  
 * @author rzvoncek
 *
 */

public class SentencePrefix {

	private StringBuffer prefix;
	private int prefixLen;
	private Track lastTrack;
	
	private String lastKnownPrefix;
	private int lastKnownLength;
	private Track lastKnownTrack;
	
	public SentencePrefix() {
		prefix = new StringBuffer();
	}
	
	public SentencePrefix(String prefix, int prefixLen, Track prefixTrack) {
		this.prefix = new StringBuffer(prefix);
		this.prefixLen = prefixLen;
		this.lastTrack = prefixTrack;
	}

	public SentencePrefix(String prefix, int prefixLen, Track prefixTrack,
			String lastPrefix, int lastCnt, Track lastTrack) {
		
		this.prefix = new StringBuffer(prefix);
		this.prefixLen = prefixLen;
		this.lastTrack = prefixTrack;
		
		this.lastKnownPrefix = lastPrefix;
		this.setLastKnownLength(lastCnt);
		this.setLastKnownTrack(lastTrack);
	}

	public String getPrefix() {
		return prefix.toString();
	}

	public void setPrefix(String prefix) {
		this.prefix = new StringBuffer(prefix);
	}

	public int getPrefixLen() {
		return prefixLen;
	}

	public void setPrefixLen(int prefixLen) {
		this.prefixLen = prefixLen;
	}

	public Track getLastTrack() {
		return lastTrack;
	}

	public void setLastTrack(Track lastTrack) {
		this.lastTrack = lastTrack;
	}

	public void append(String chunk) {
		prefix.append(chunk);
	}


	public void incPrefixLen() {
		prefixLen++;
	}
	
	public void setLastTrackName (String name) {
		this.prefix = new StringBuffer(name);
	}

	public String getLastKnownPrefix() {
		return lastKnownPrefix;
	}

	public void setLastKnownPrefix(String lastKnownPrefix) {
		this.lastKnownPrefix = lastKnownPrefix;
	}

	public int getLastKnownLength() {
		return lastKnownLength;
	}

	public void setLastKnownLength(int lastKnownLength) {
		this.lastKnownLength = lastKnownLength;
	}

	public Track getLastKnownTrack() {
		return lastKnownTrack;
	}

	public void setLastKnownTrack(Track lastKnownTrack) {
		this.lastKnownTrack = lastKnownTrack;
	}

}
