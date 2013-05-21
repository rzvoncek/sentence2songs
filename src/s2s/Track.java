package s2s;

/**
 * Represents a track pulled from Spotify database.
 * @author rzvoncek
 *
 */

public class Track {

	private String url;
	private String name;
	
	public Track(String name, String url) {
		this.url = url;
		this.name = name;
	}

	public String getUrl() {
		return url == null ? "" : url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getName() {
		return name == null ? "" : name ;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
	
}
