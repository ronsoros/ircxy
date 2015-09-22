/**
 *	Tes - Java Threaded Echo Server.
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

/*
	For each active user, there exists a Pinger.
	Every 180 seconds, pinger pings the user.
	If the user fails to reply to 2 pings in a row,
	their connection to the server is terminated (ping timeout).
*/

class Pinger implements Runnable {

  int unrepliedPings = 0;
  Socket sock;
  Thread thisThread;
  String serverName;
  int interval = 180; //number of seconds between pings

  boolean xmlFormat = false; //whether to send pings wrapped in xml.
  String strPing = "";
  String strXMLPing = "";
  
	//boolean oldJavaVersion = false;
	EchoServer es;
	
  public Pinger(String sn, int iv, EchoServer e) {
	thisThread = new Thread(this);
	serverName = sn;
	interval = iv;
	//oldJavaVersion = oj;
	es = e;
	strPing = "PING :" + serverName + "\r\n";
	strXMLPing = "<message value=\"PING :" + serverName + "\"/>" + '\u0000';
  }

  


  public void resetPings() { unrepliedPings = 0; }

  public boolean timedout() { return (unrepliedPings >= 3); }


  public void run() {
	while (true) {

		try {
			Thread.sleep(interval*1000);
		} catch (InterruptedException e) {}



		try {
			
			SocketUtil s = new SocketUtil(sock);
			PrintStream out = s.getPrintStream();
			if (xmlFormat) {	out.print( strXMLPing ); }
			else		   {	out.print( strPing    ); }

			out.flush();

		} catch (IOException e) {
			//System.out.println("Pinger.java ERR: " + e);
		}

		unrepliedPings++;

		if (unrepliedPings >= 3) {
			try {
				SocketUtil su = new SocketUtil(sock);
				PrintStream sout = su.getPrintStream();
				sout.print( "ERROR :Closing Link [" + sock.getInetAddress().getHostName() + "] (Ping timeout)\r\n" );
			} catch (IOException e) { }
			

			try {
				//System.out.println("Pinger.java: Attempting to shutdown socket");
				
				es.sInpOut( sock );
				
				sock.close();
			} catch (IOException e) {
				System.out.println("Pinger.java sds ERR: " + e);
			}
		}


	}


  }

 public void setInterval( int iv ) {
	 //thisThread.stop();
 	interval = iv;
	 //thisThread.start();
 }
 public void setXML( boolean onoff ) {
 	xmlFormat = onoff;
 }

 public void startPinger(Socket sock1, int iv) {
 	sock = sock1;
 	interval = iv;
 	thisThread.start();
 }

 public void startPinger(Socket sock1) {
	startPinger(sock1, interval);
 }
 public void stopPinger() {
	thisThread.stop();
 }


}