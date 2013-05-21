package s2s;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Class responsible for reading the standard input, calling the
 * splitting algorithm and printing the results.
 * 
 * @author rzvoncek
 *
 */

public class InputReader extends Thread {

	BufferedReader in;
	SentenceSplitter splitter;
	
	public InputReader(SentenceSplitter splitter) {
		in = new BufferedReader(new InputStreamReader(System.in));
		this.splitter = splitter;
	}
	
	public void run() {
		
		String sentence = "";

		try { 
			
  			while ( (sentence = in.readLine()) != null ) {
  				try { 
  					sentence = InputReader.sanitize(sentence);
  					System.out.println(splitToTracks(sentence));
  				} catch (Exception e) {
  					// ignored...
  					return;
  				}
			}

		} catch (IOException ioe) {
			return;
		}
		
		// shut down worker threads from the splitter
		finally {
			// ignored...
			splitter.shutDown();
		}
		
		return;
			
	}

	/**
	 * Removes unwanted characters from the given sentence.
	 * @param sentence Input sentence
	 * @return sentence striped from unwanted characters
	 */
	public static String  sanitize(String sentence) {
		return sentence
				.toLowerCase()
				.replaceAll(",","")
				.replaceAll("'", "'")
				.replaceAll("`", "'")
				.replaceAll(":", "");
	}

	/**
	 * Actually calls the splitting method. Receives list of (matched) tracks.
	 * Formats the list of tracks to printable string.
	 * @param sentence
	 * @return
	 */
	private String splitToTracks(String sentence) {

		StringBuffer result = new StringBuffer();

		List<Track> tracks = splitter.splitToTracks(sentence);

		for ( Track t : tracks ) {
			result.append( String.format("\t%36s ",t.getUrl()) );
			result.append(t.getName() + "\n");
		}
		
		return result.toString();
	}
	
}
