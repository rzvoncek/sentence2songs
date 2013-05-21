package s2s;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.cache.CacheResponseStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.cache.CachingHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

/**
 * 
 * Implementation of worker thread handling an HTTP request.
 * The worker also takes care of parsing the received XML file. 
 * 
 * @author rzvoncek
 *
 */

public class ParallelQueryExec implements Callable<List<Track>> {

	private static final String baseURL = "http://ws.spotify.com/search/1/track?q=";
	
	private String query;
	private HttpClient client;
	private SpotifyAdapter spotify;
	
	private static XMLInputFactory factory = XMLInputFactory.newInstance();
	
	public ParallelQueryExec(SpotifyAdapter sa, HttpClient c, String q) {
		query = q;
		client = c;
		spotify = sa;
	}
	
	@Override
	public List<Track> call() throws Exception {

		HttpGet httpRequest;
		HttpResponse httpResponse;
		HttpEntity httpEntity = null;
		List<Track> result;
		HttpContext httpContext;
		
		throttleRequests();
		
		String url = baseURL;
		
		try {
			url += URLEncoder.encode(query, "UTF-8");
		} catch (UnsupportedEncodingException uee ) {
			System.err.println(uee.getMessage());
			return null;
		}
		
		httpRequest = new HttpGet(url);
		
		try {
			
			httpContext = new BasicHttpContext();
			httpResponse = client.execute(httpRequest, httpContext);

			// check if the request was served from local cache
			CacheResponseStatus responseStatus = (CacheResponseStatus) 
					 httpContext.getAttribute(CachingHttpClient.CACHE_RESPONSE_STATUS);
			
			// if not -> parse the response
 			if ( responseStatus == CacheResponseStatus.CACHE_MISS ) {
 				httpEntity = httpResponse.getEntity();
 				result = this.parse(httpEntity.getContent());
 			}
 			
 			// otherwise return no new tracks
 			else {
 				result = new ArrayList<Track>();
 			}

			EntityUtils.consume(httpEntity);
			
			return result;
 			
		}
			
		catch (ClientProtocolException e) {
			System.err.println(e.getMessage());
			return null;
		} catch (IOException e) {
			System.err.println(e.getMessage());
			return null;
		}
		
	}

	/**
	 * Ensures maintaining the Spotify limit of 10 requests per second.
	 * 
	 * With 10 queries per second allowed, executing one every 100 ms 
	 * can't lead to limit exhaustion.
	 */
	private void throttleRequests() {
		long tNow = new Date().getTime();
		long tSinceLastReq = tNow - spotify.getLastRequestTime();
		if ( tSinceLastReq < 100 ) {
			try { 
				Thread.sleep(100-tSinceLastReq); 
			} catch (Exception e) {}
		} else {
			spotify.setLastRequestTime(tNow);
		}
	}
	
	
	/**
	 * Parse the contents of received HTML document.
	 * @param inStream
	 * @return
	 */
	public List<Track> parse(InputStream inStream) {
		
		List<Track> tracks = new ArrayList<Track>();
		
		try { 

			XMLStreamReader streamReader = factory.createXMLStreamReader(inStream);
	
			while (streamReader.hasNext()) {

				streamReader.next();

				// found a start element
				if (streamReader.getEventType() == XMLStreamReader.START_ELEMENT) {

					String elementName = streamReader.getLocalName();

					// the started element is a track!
					if ("track".equals(elementName)) {
						String trackUrl = streamReader.getAttributeValue(0);
						
						// parse the rest of the track
						Track t = parseTrack(streamReader,trackUrl);
						if ( t != null ) {
							tracks.add(t);
						}
					}
				}
				
			}
			
        } catch (Exception e) {
        	System.err.println(e.getMessage());
        }
		
		return tracks;
		
	}

	/**
	 * Parses the details of a track element. Currently searches only for the track
	 * name.
	 * @param streamReader
	 * @param trackUrl
	 * @return
	 */
	private Track parseTrack(XMLStreamReader streamReader, String trackUrl) {

		try { 
			
			while (streamReader.hasNext()) {

				streamReader.next();
	
				// the track element ended before there was a name element
		        if (streamReader.getEventType() == XMLStreamReader.END_ELEMENT) {
		            String elementName = streamReader.getLocalName();
		            if ("track".equals(elementName)) {
		              return null;
		            }
	
		        // some other element within the track element
		        } else if (streamReader.getEventType() == XMLStreamReader.START_ELEMENT) {

		        	String elementName = streamReader.getLocalName();

		        	// yeah, it's the track name
		        	if ("name".equals(elementName)) {
		        		
		        		// parse away possible ' - feat smt'
		        		String title = streamReader.getElementText().toLowerCase();

		        		int cut = title.indexOf(" -");
		        		if ( cut != -1 )
		        			title = title.substring(0, cut);
		        		
		        		// parse away possible '(something...'
		        		cut = title.indexOf("(");
		        		if ( cut != -1 )
		        			title = title.substring(0, cut);
		        		
		        		return new Track(title,trackUrl);
		        	}
		        }
	
		    }
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return null;
		}
		
		return null;
	}
	
}
