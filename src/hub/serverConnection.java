/**
 *	TES Hub - Java IRCX Server
 *	Author: David Forrest (david@splog.net)
 *
 *	Copyright (C) 2002 David Forrest
 *
 *	This program is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU General Public License
 *	as published by the Free Software Foundation; either version 2
 *	of the License, or (at your option) any later version.
 *
 *	This program is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with this program; if not, write to the Free Software
 *	Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 *	David Forrest (david@tes.splog.net)
 *
 */

import java.net.*;
import java.io.*;
import java.util.Vector;

/*
	Makes a connection to each server.
	Stores data recieved from the server.
	Can write data to the server.
	Holds information about the server (user-list etc).
	
	
	Data structures:
	
	1.	userList - Vector
			Each Element = a nickname of a user
	
	2.	chanList - Vector.
			FirstElement = a vector containing: chanName, chansettings
			NextElements = list of names on that channel
	
	
*/

class serverConnection implements Runnable {

  Socket sock;
  Thread thisThread;
  public String serverName;
  String serverFakeName;
  String serverDescription;
  public String hubPassword = "0";
  public int serverPort;
  boolean connected;
  String inData;
  boolean quiet = false;
  boolean removed = false;
  public int removedReason = 0;

	Vector userList;
	Vector chanList;
	commandProcessor cp = null;
	int scid = 0;
	boolean compress = false;

  public serverConnection(String sn, int sp, String hp, commandProcessor c, int id, boolean q) {
	thisThread = new Thread(this);
	serverName = sn;
	serverFakeName = sn;
	serverPort = sp;
	inData = "";
	connected = false;
	userList = new Vector();
	chanList = new Vector();
	hubPassword = hp;
	cp = c;
	scid = id;
	quiet = q;
	//thisThread.start();
  }


  public Vector getUserlist() { return userList; }
  public Vector getChanlist() { return chanList; }
  

  public void clean() {
  	inData = "";
  	connected = false;
  	userList = new Vector();
  	chanList = new Vector();
  	compress = false;
  }

	public void enableCompression() { compress = true; }



  public void startSC() {
  	thisThread.start();
  }


  public String correctNick( String u ) {
  	String tmpUser = "";
  	for (int i=0; i<userList.size(); i++) {
  		tmpUser = (String)userList.elementAt(i);
  		if (tmpUser.equalsIgnoreCase(u)) {
			  u = tmpUser;
  			i = userList.size(); //break loop
  		}  		
  	} 	
	return u;
  }

  public boolean hasUser( String u ) {
  	//go through the entire vector and look for this user.
  	boolean gotUser = false;
  	String tmpUser = "";
  	for (int i=0; i<userList.size(); i++) {
  		tmpUser = (String)userList.elementAt(i);
  		if (tmpUser.equalsIgnoreCase(u)) {
  			gotUser = true;
  			i = userList.size(); //break loop
  		}  		
  	}
  	
  	return gotUser;  	
  	//return (userList.contains( u.toLowerCase() ));
  }
  public boolean removeUser( String u ) {
  	//go through the entire vector and look for this user.
  	boolean gotUser = false;
  	String tmpUser = "";
  	for (int i=0; i<userList.size(); i++) {
  		tmpUser = (String)userList.elementAt(i);
  		if (tmpUser.equalsIgnoreCase(u)) {
  			gotUser = true;
  			i = userList.size(); //break loop
			  userList.removeElement(tmpUser);
  		}  		
  	}
  	
  	return gotUser;  	

  	//return (userList.remove( u.toLowerCase()) );
  }
  public void addUser( String u ) {
  	userList.addElement( u );
  }
  public int userCount() {
  	return userList.size();
  }
  public String getUser( int pos ) {
  	return (String)userList.elementAt(pos);
  }
  
	public Vector getUsers() {
		return userList;
	}



  public int chanCount() {
  	return chanList.size();
  }
  public String getChanName( int pos ) {
	Vector v = (Vector)chanList.elementAt(pos);
	
	v = (Vector)v.elementAt(0);
	
	return (String)v.elementAt(0);
  }



  public int hasChan( String c ) {
	  int i = 0;
	  boolean found = false;
	  Vector cv;
	  Vector tv; 
	  
  	while ((i < chanList.size()) && (!found)) {
		  cv = (Vector)chanList.elementAt(i);
		  
		  tv = (Vector)cv.elementAt(0);
		  if (c.equalsIgnoreCase((String)tv.elementAt(0) ) )
		  	found = true;

  		i++;
  	}
  	if (found)
	  	return i-1;
	  else
	  	return -1;
  }


  public boolean removeChanIfEmpty( String c ) {
	  int i = 0;
	  boolean found = false;
	  Vector cv;
	  Vector tv;
	  
  	while ((i < chanList.size()) && (!found)) {
		  cv = (Vector)chanList.elementAt(i);
		  
		  tv = (Vector)cv.elementAt(0);
		  
		  if ((cv.size() == 1) && c.equalsIgnoreCase((String)tv.elementAt(0) ) ) {
			  chanList.removeElementAt(i);
		  	found = true;
		  }

  		i++;
  	}
  	//System.out.println("removeChanIfEmpty( " + c + ") = " + found );
  	return found;  	
  }


  public boolean removeChan( String c ) {
	  int i = 0;
	  boolean found = false;
	  Vector cv;
	  Vector tv;
	  
  	while ((i < chanList.size()) && (!found)) {
		  cv = (Vector)chanList.elementAt(i);
		  tv = (Vector)cv.elementAt(0);
		  if (c.equalsIgnoreCase((String)tv.elementAt(0) ) ) {
			  chanList.removeElementAt(i);
		  	found = true;
		  }
  		i++;
  	}
  	return found;
  }
  
  //add a chan with no users on it - no longer used, use addChan(Vector) instead.
  /*public void addChan( String c ) {
  	Vector cv = new Vector();
  	Vector chanstuff = new Vector();
  	chanstuff.addElement( c );
  	
  	cv.addElement( chanstuff );
  	chanList.addElement( cv );
  	
  }*/
  
  //add a chan with users, first element in the vector should be chan name
  public void addChan( Vector c ) {
  	Vector chanstuff = new Vector();
  	chanstuff.addElement( (String)c.elementAt(0) );
  	c.setElementAt(chanstuff, 0);
  	chanList.addElement( c );
  }
  
  public String namesList( int cid ) { return namesList(cid, true); }
  public String namesList( int cid, boolean ircx ) {
  	Vector c = (Vector)chanList.elementAt( cid );
  	
  	String nl = "";
  	String tmpNick = "";
  	for (int i=1; i< c.size(); i++) {
  		if (ircx) { nl = nl + " " + (String)c.elementAt(i); }
  		else {
	  		tmpNick = (String)c.elementAt(i);
	  		if (tmpNick.length() > 0) {
		  		if (tmpNick.charAt(0) == '.') { tmpNick = "@" + tmpNick.substring(1); }
		  		nl = nl + " " + tmpNick;
		  	}
	  	}
  	}
	  if (nl.startsWith(" ")) { nl = nl.substring(1); }
  	return nl;
  }
  public Vector chanNicks( int cid ) {
  	Vector c = (Vector)chanList.elementAt( cid );
  	return c;
  }
  
  public int chanMemberCount( int cid ) {
  	Vector c = (Vector)chanList.elementAt( cid );
	  return (c.size()-1);
  }
  
  
  public void nickChange( String on, String nn ) {
  	Vector cv;
	  String tmpNick = "";
  	for (int i=0; i < chanList.size(); i++ ) {
  		cv = (Vector)chanList.elementAt(i);

			//start at 1 to avoid settings vector
			for (int j=1; j<cv.size(); j++) {
				tmpNick = (String)cv.elementAt(j);
				if (tmpNick.equalsIgnoreCase(on)) {
					cv.setElementAt(nn, j);
					j = cv.size(); //break loop
				} else if (tmpNick.equalsIgnoreCase("+" + on) || tmpNick.equalsIgnoreCase("@" + on) || tmpNick.equalsIgnoreCase("." + on)) {
					cv.setElementAt(tmpNick.charAt(0) + nn  ,j);
					j = cv.size(); //break loop
				}
			}

			//update the chanList vector
			//chanList.setElementAt( cv, i );

  	}
  }
  
  public void SUMOC( String c, String on, String nm ) {
  	Vector cv;
  	String chan = "";
  	//on = on.toLowerCase();
  	String tmpNick = "";
  	//System.out.println("SUMOC (");
  	
  	for (int i=0; i < chanList.size(); i++ ) {
  		cv = (Vector)chanList.elementAt(i);
  		
  		Vector tmp = (Vector)cv.elementAt(0);
		  chan = (String)tmp.elementAt(0);
		  	//System.out.println("chan= " + chan);
		  if (chan.equalsIgnoreCase(c )) {

			//System.out.println("\tfound chan: " + chan);

			for (int j=1; j<cv.size(); j++) {
				tmpNick = (String)cv.elementAt(j);
				//System.out.println("\t\tchecking nick: " + tmpNick);
				if (tmpNick.equalsIgnoreCase(on) || tmpNick.equalsIgnoreCase("+" + on) || tmpNick.equalsIgnoreCase("@" + on) || tmpNick.equalsIgnoreCase("." + on)) {
					if (tmpNick.startsWith("+") || tmpNick.startsWith("@") || tmpNick.startsWith(".")) {
						tmpNick = tmpNick.substring(1);
					}
					cv.setElementAt( nm + tmpNick  ,j);
					j = cv.size(); //break loop
					//System.out.println("\tmatch: " + tmpNick + " " + on);
				}
			}

			  
			//update the chanList vector
			chanList.setElementAt( cv, i );
			  
			  
			i = chanList.size();
		  }
		  
		  
  	}
  }
  
  public boolean removeUserFromChan( String c, String u ) {
	  int i = 0;
	  boolean found = false;
	  boolean foundu = false;
	  Vector cv;
	  String tmpNick = "";
	  
	  //System.out.println( "removeUserFromChan( " + c + ", " + u + ")" );
	  
	  if (chanList.size() > 0) {
	  	while ((i < chanList.size()) && (!found)) {
			  cv = (Vector)chanList.elementAt(i);
			  Vector tmp = (Vector)cv.elementAt(0);
			  if (c.equalsIgnoreCase( (String)tmp.elementAt(0) ) ) {
			  	
			  	
			  	
				for (int j=1; j<cv.size(); j++) {
					tmpNick = (String)cv.elementAt(j);
					//System.out.print(tmpNick + " - match? ");
					if (tmpNick.equalsIgnoreCase(u) || tmpNick.equalsIgnoreCase("+" + u) || tmpNick.equalsIgnoreCase("@" + u) || tmpNick.equalsIgnoreCase("." + u)) {
						//System.out.println("Yes");
						cv.removeElementAt( j);
						foundu = true;
						j = cv.size(); //break loop
					} else {
						//System.out.println("No");
					}
				}
			  	

				  //if (foundu) { chanList.setElementAt( cv, i ); }
				  		
				  /*if (!foundu) {
				  	System.out.println("User was not found on channel!");
				  }*/
				  	
			  	found = true;
			  }
	  		i++;
	  	}
  	}
  	return foundu;
  }


  public String getChanOption( String c, int index ) {
  	int i=0;
  	boolean found = false;
  	String option = "ERROR";
  	Vector cv;
  	
  	while ((i < chanList.size()) && (!found)) {
  		cv = (Vector)chanList.elementAt(i);
  		Vector settings = (Vector)cv.elementAt(0);
  		if (c.equalsIgnoreCase( (String)settings.elementAt(0) ) ) {
  			found = true;
  			int s = settings.size();
  			if (index <= s){
  				option = (String)settings.elementAt(index);
  			}
  		}
  		i++;  	
  	}
  	
  	return option;
  	
  	
  }
  
  
  public String getChanOption( String c, String o ) {
  	int i=0;
  	boolean found = false;
  	String option = "ERROR";
  	Vector cv;
  	
  	while ((i < chanList.size()) && (!found)) {
  		
  		cv = (Vector)chanList.elementAt(i);
  		Vector settings = (Vector)cv.elementAt(0);
  		if (c.equalsIgnoreCase( (String)settings.elementAt(0) ) ) {
  			found = true;
  			int s = settings.size();
  			
  			//find the option
  			//if (  o.equals("TOPIC") || o.equals("ONJOIN") || o.equals("ONPART") || o.equals("OWNERKEY") || o.equals("HOSTKEY")  ) {
  				String e = "";
  				for (int j=1; j<s; j++) {
  					e = (String)settings.elementAt(j);
  					//System.out.println("getChanOption(" + c + "," + o + ")  ? " + e );
  					if (e.startsWith(o + " ")) {
  						option = e.substring(o.length()+1);
  						j = s; //terminate the loop
  					}
  				}
  			//}
  			
  			
  			
  		}
  		
  		i++;
  	}
  	//System.out.println("Got chan option: " + c + " " + o + " :" + option );
  	return option;
  	
  }


  public void setChanOption( String c, String arm, String o, String s ) {
  	int i=0;
  	boolean found = false;
  	Vector cv;
  	
  	while ((i < chanList.size()) && (!found)) {

  		cv = (Vector)chanList.elementAt(i);
  		Vector settings = (Vector)cv.elementAt(0);
  		//System.out.println( c + " ? " + (String)settings.elementAt(0) );
  		
  		if (c.equalsIgnoreCase( (String)settings.elementAt(0) ) ) {
  			found = true;
  			
  			if (arm.equals("ADD")) {
						settings.addElement( o + " " + s ); //add the option+setting to this vector
						cv.setElementAt( settings, 0 ); //change the settings vector inside the chanvector
						chanList.setElementAt( cv, i ); //change the chanvector inside the chanList vector
						
			  } else if (arm.equals("REM")) {
			  	String e = "";
			  	for (int j=0; j<settings.size(); j++) {
			  		e = (String)settings.elementAt(j);
			  		if (e.equalsIgnoreCase(o + " " + s)) {
						settings.removeElementAt(j);
			  			
						cv.setElementAt( settings, 0 ); //change the settings vector inside the chanvector
						chanList.setElementAt( cv, i ); //change the chanvector inside the chanList vector
			  		}
			  	}
			  	
			  } else if (arm.equals("REPLACE")) {
			  	String e = "";
			  	boolean exists = false;
			  	for (int j=0; j<settings.size(); j++) {
			  		e = (String)settings.elementAt(j);
			  		if (e.startsWith(o + " ")) {
						settings.setElementAt(o + " " + s, j);
			  		  exists = true;
						cv.setElementAt( settings, 0 ); //change the settings vector inside the chanvector
						chanList.setElementAt( cv, i ); //change the chanvector inside the chanList vector
			  		}
			  	}
			  	if (!exists) {
			  		//add it
			  		settings.addElement( o + " " + s );
			  		cv.setElementAt( settings, 0 );
			  		chanList.setElementAt( cv, i );
			  	}
			  	
			  }
			  			
			  	

  			
  			
  		}
  		i++;
  	}
  	
  }


  public String userStatusOnChan( String c, String u ) {
  	//return the user's status on the channel (r, v, o, q)
  	//or n (not on channel)
	  int i = -1;
	  boolean found = false;
	  String foundu = "n";
	  Vector cv;
	  String tmpNick = "";
	  
  	while ((i < chanList.size()) && (!found)) {
  		i++;
		  cv = (Vector)chanList.elementAt(i);
		  Vector tmp = (Vector)cv.elementAt(0);
		  if (c.equalsIgnoreCase( (String)tmp.elementAt(0) ) ) {


			for (int j=1; j<cv.size(); j++) {
				tmpNick = (String)cv.elementAt(j);
				if (tmpNick.equalsIgnoreCase(u)) {
					foundu = "r";
					j = cv.size();
				} else if (tmpNick.equalsIgnoreCase("+" + u) || tmpNick.equalsIgnoreCase("@" + u) || tmpNick.equalsIgnoreCase("." + u)) {
					foundu = "" + tmpNick.charAt(0);
					j = cv.size(); //break loop
				}
			}



		  	found = true; //we found the channel

		  }
  	}
  	
  	return foundu;
  }



  public void addUserToChan( String c, String u ) {

	  int i = -1;
	  boolean found = false;
	  Vector cv;
	  
  	while ((i < chanList.size()) && (!found)) {
  		i++;
		  cv = (Vector)chanList.elementAt(i);
		  Vector tmp = (Vector)cv.elementAt(0);
		  if (c.equalsIgnoreCase( (String)tmp.elementAt(0) ) ) {
		  	
		  	
		  	if (userStatusOnChan(c, u).equals("n")) {
		  		cv.addElement( u );
		  		chanList.setElementAt( cv, i );		  		
		  	}

			
		  	found = true;
		  }
  	}

  }












  public boolean isConnected() {
  	return connected;
  }
  public boolean isRemoved() {
  	return removed;
  }

  public boolean isData() {
  	return (inData != "");
  }
  public String getData() {
  	String tmp;
  	tmp = inData;
  	inData = "";
  	return tmp;
  }
  

  public void writeData(String d) {
	   try {
			PrintWriter pw = new PrintWriter(sock.getOutputStream(),true);
			pw.print( d + "\r\n" );
			pw.flush();
	   } catch (Exception e) {
	   	//System.out.println("Write error: " + e);
	   }
  }


  public String getServerName() {
  	return serverName;
  }
  public int getServerPort() {
  	return serverPort;
  }
  public String getHubPass() {
  	return hubPassword;
  }
  public void setServerDescription(String s) { serverDescription = s; }
  public String getServerDescription() { return serverDescription; }


  public void setServerFakeName( String s) { serverFakeName = s; }
  public String getServerFakeName() { return serverFakeName; }
  
  
  public void removeServer(int r) {
  	/*
  		r=1	- Removed due to version incompatibility
  		r=2	- Removed by user.
  		r=3	- Password not accepted for hub.
  	*/
  	removed = true;
  	removedReason = r;
  	clean();
  	closeConnection();
  }
  public String removedReasonStr() {
  	if (removedReason == 1) { return "Incompatible"; }
  	else if (removedReason == 2) { return "Requested"; }
  	else if (removedReason == 3) { return "Bad Password"; }
  	else { return "Unknown"; }
  }
  
  public void closeConnection() {
  	try {
  		sock.close();
  	} catch (IOException e) {}
  }


  public void run() {

	
	while (!connected && !removed) {
		try {
			if (!quiet) { System.out.println("Attempting connection to: " + serverName + ":" + serverPort + " ..."); }
		    sock = new Socket(serverName, serverPort);
			if (!quiet) { System.out.println("Connected (" + sock.getInetAddress() + ")\r\n"); }
			connected = true;
			
					//We must identify to the server first.
					PrintWriter pw = new PrintWriter(sock.getOutputStream(),true);
					pw.print( "PASS " + hubPassword + "\r\nUSER hub f f :server hub\r\nNICK NetHub\r\n" );
					pw.flush();

					BufferedReader input = new BufferedReader(new InputStreamReader( sock.getInputStream(), "ISO-8859-1" ));
					    
					String line = "";

				    	while ( true ) {
					    	line = input.readLine();
					    	
					    	if (line == null)
					    		break;

							cp.parse( scid + " " + line );
				    	}

				
					connected = false;
					closeConnection();

			
		}
		catch (UnknownHostException e) {
			connected = false;
			if (!quiet) { System.out.println("-ERROR-(" + serverName + "): " + e); }
		}
		catch (IOException e) {
			connected = false;
		    if (!quiet) { System.out.println("-ERROR-(" + serverName + "): " + e); }
		}
		if (!connected) {
			//wait 30 seconds before reconnecting
			if (userList.size() > 0) { cp.serverQuit(scid); }
			
			try { Thread.sleep(30000); } catch (InterruptedException e) {}
		}

	}
	//thisThread.stop();
  }	







 //end class serverConnection
}