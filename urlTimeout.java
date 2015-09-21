import java.net.*;
import java.io.*;
import java.util.Vector;
import java.util.Properties;

/*
	Part of jabo - Java Bookmark Organizer
	Author : David Forrest
	Written: 2001/2002
*/

/**
 *	This class provides a feature lacking from Java connection classes.. a download timeout feature.
 *	
 *	
 *	This class allows a URL to be downloaded, while providing the ability to control the timeout delay when connecting to the URL.
 *	The timeout delay is controlled by the DownloadURL class (included with jabo).
 *	<br>
 *	Inputs: A string containing the URL to be downloaded.<br>
 *	Outputs: A vector containing the raw document content from the url.
 *
 */


public class urlTimeout implements Runnable {

	Thread thisThread;				//A new thread to run while attempting to download the url.
	Vector content = new Vector();	//Vector containing the downloaded document's content.
	String urlString = "";			//The url we are downloading from.
	boolean finished = false;		//Indicating whether the download is complete or not.

  /**
   * Starts a new Thread for this class
   */
  public urlTimeout() {
	thisThread = new Thread(this); //create a new thread for this download
  }


  public void setProxy( String proxyMachine, String proxyPort ) {
				//Set the system props for proxy use
				Properties sysSet = System.getProperties();
				sysSet.put("proxySet", "true");
				sysSet.put("proxyHost", proxyMachine);
				sysSet.put("proxyPort", proxyPort);
				System.setProperties(sysSet);
  }

  public void run() {
  	//This method gets executed when the thread is started.
  	
		URL u;	//new url variable
		
		String inputLine;	//temp variable.
		
		
		try {
			
			//for some reason java's url classes crash when they see an '@'
			//e.g. http://user@domain.com/
			if (urlString.indexOf("@") == -1) {

      
   
				u = new URL( urlString );       //initialise the url
				URLConnection uc = u.openConnection();
        
				BufferedReader in = new BufferedReader( new InputStreamReader(uc.getInputStream()));
                
				inputLine = "";
				//while there is more to download...
				while ((inputLine = in.readLine()) != null) {
					content.addElement( inputLine ); //retrieve the content, and put it in the n$                                }
				}
				finished = true; //finished downloading
				in.close(); //close the connection


			} else {
				//If the url does contain an '@', there is really nothing much we can do except give up!
				finished = true; //we're finished.
			}
			
		//catch any exceptions.
		} catch(MalformedURLException e) { finished = true; }
		  catch(IOException e) { finished = true; }


  }

 /**
  * Returns true if any content has been downloaded yet, flase otherwise.
  */
 public boolean hasContent() {
 	//Have we downloaded anything yet?
 	return (content.size() > 0);
 }
 
 /**
  * Returns true if downloading has completed, false otherwise.
  */
 public boolean finishedContent() {
	//Are we finished downloading yet?
	return finished;
 }
 
 /**
  * Returns the content downloaded so far.
  * Really, this method should not be called until downloading has completed.
  */
 public Vector getContent() {
 	return content;
 }

 /**
  * Starts downloading of the specified URL
  */
 public void startTimer(String strURL) {
	urlString = strURL;
	thisThread.start(); //start the new thread
 } 
 
 /**
  * Stops the thread for this class, effectively terminating it.
  */
 public void stopTimer() {
	//stop the thread.
	thisThread.stop();
 }


}