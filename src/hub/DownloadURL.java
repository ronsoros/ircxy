import java.io.*;
import java.net.*;
import java.util.Vector;

/*
	Part of jabo - Java Bookmark Organizer
	Author : David Forrest
	Written: 2001/2002
*/

/**
 *	This class provides the methods for downloading the specified URL.
 *	It uses urlTimeout to retrieve the data, and also to provide a timeout feature.
 *	<br>
 *	Inputs: URL to be downloaded (as a string)<br>
 *			A timeout value (in seconds)<br>
 *	Outputs: A vector containing the url document's contents.
 *	
 */


public class DownloadURL {
	
	Vector urlContents = new Vector(); //vector containing the downloaded document contents.
	String err = "ERROR :DownloadURL.java -> ";
	boolean debug = false;
	
	/**
	 * Download the url document, with respect to the given timeout value.
	 */
	public DownloadURL( String strURL, int tout ) {
		
		//if the timeout value supplied is <= 0, set it to some default value.
		if (tout <= 0) {
			tout = 10;
		}		
		
		tout = tout * 1000; //timeout value (convert to milliseconds)
		
		urlTimeout ut = new urlTimeout(); //Create a new instance of urlTimeout class..
		ut.startTimer( strURL ); 		//.. and start it running.
		
		
		long timeout = System.currentTimeMillis(); //note the time we started downloading at.
		
		//Wait until the timeout has elapsed (or until we recieve some content)
		while (!ut.hasContent() && (System.currentTimeMillis() - timeout < tout)) {
			//just wait
		}
		
		//If a connection has been established within this time, great!
		//Wait until all the content has been downloaded.
		if (ut.hasContent()) {
			while (!ut.finishedContent()) {
				//just wait
			}
			urlContents = ut.getContent();  //save the full downloaded document in the new vector.
		} else {
			//If we haven't recieved any content within the specified timeout time,
			dprintln(strURL + " timed out."); //the url is unresponsive
		}
		

	}
	
	/**
	 * Returns the vector containing the downloaded url's contents.
	 */
	public Vector getVector() {
		return urlContents;
	}
	private void dprintln( String s ) {
		if (debug) { System.out.println( s ); }
	}
	
}//end class DownloadURL