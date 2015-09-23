import java.io.*;
import java.net.*;
import java.util.*;


public class webContact implements Runnable {

	Thread thisThread;
	String server;
	String port;
	String version;
	String adminEmail;
	String webDescription;
	String webLink;
	String serverlist;
	boolean waited = false;
	boolean alive = true;
	
	int interval = 60; //number of minutes between updates
	int fastinterval = 5; //if an error occurs, update this often.
	String servercode = null;
	commandProcessor cp = null;
	
	//for networks
	public webContact( String s, String v, String a, String d, String l, String w, commandProcessor c ) {
		thisThread = new Thread(this);
		server = s;
		port = "0";
		version = v;
		adminEmail = a;
		webDescription = d;
		webLink = l;
		serverlist = w;
		cp = c;
		thisThread.start();
	}
	

	public void stopThread() {
		alive = false;
	}

  public void run() {
	while (alive) {

		//initial waiting period, to make sure server isn't only up for a short time
		/*
		if (!waited) {
			try {
				Thread.sleep(1200000);
			} catch (InterruptedException e) {}
			waited = true;
		}
		*/

		doUpdateNow();

		//sleep between updates
		try {
			if (servercode == null) { Thread.sleep(fastinterval*60000); }
			else					{ Thread.sleep(    interval*60000); }
		} catch (InterruptedException e) {}


	}//end while
  }




	public void doUpdateNow() {
				
		//Date tmpd = new Date();
		//String d = tmpd.toString();
		//		d = URLEncoder.encode( d );
		
		
		//Variables that get sent _every_ time...
		String sndString = "&server=" + URLEncoder.encode(server)
										+ "&ut=0"
										+ "&uc=" + cp.userCount
										+ "&network=1";




		//Variables that get sent ONCE ONLY...
		if (servercode == null) {
			sndString = sndString +  "&prt=" + URLEncoder.encode(port)
								+  "&ver=" + URLEncoder.encode(version)
								+  "&wd=" + URLEncoder.encode( webDescription )
								+  "&wl=" + URLEncoder.encode( webLink )
								+  "&sl=" + URLEncoder.encode( serverlist )
								+  "&ae=" + URLEncoder.encode( adminEmail )
								+  "&os=" + URLEncoder.encode( System.getProperty("os.name") + " on an " + System.getProperty("os.arch") )
								+  "&jv=" + URLEncoder.encode( System.getProperty("java.version") );
		} else {
			sndString = sndString + "&scode=" + servercode;
		}
			
									
		//System.out.println("sndString: " + sndString);
		DownloadURL du = new DownloadURL( "http://www.tesx.org/?page=addserver" + sndString, 10 );
		Vector v = du.getVector();
  	  
		if (v.size() > 0) {
  	  		//save the scode
  	  		String s = (String)v.elementAt(0);
  	  		if (s.equals("DNY invalid-scode")) {
  	  			//problem with the scode - did it get lost ?
  	  			servercode = null;
  	  		} else {  	  		
	  	  		servercode = URLEncoder.encode( (String)v.elementAt(0) );
	  	  	}
		} else {
			//problem contacting website?
			servercode = null;
		}
		
	}

}