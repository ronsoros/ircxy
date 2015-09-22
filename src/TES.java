/**
 *	TES - Java IRCX Server
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
 This is just a Thread with a field to store a
 Socket object. Used as a thread-safe means to pass
 the Socket from handleConnection to run.
*/

class Connection extends Thread {
  protected Socket serverSocket;

  public Connection(Runnable serverObject,Socket serverSocket) {
    super(serverObject);
    this.serverSocket = serverSocket;
  }
}




public class TES extends EchoServerimplements Runnable {


  public static void main(String[] args) {
  	
  	
	int port = 6667;
	String arg = "";	
	
	
	for (int i=0; i<args.length; i++) {
		arg = args[i];
		if (arg.startsWith("-p=")) {
		  try {
			port = Integer.parseInt(arg.substring(3));
		  } catch( NumberFormatException e ) {
		  	System.out.println("Usage: -p=<port>\t\tWhere <port> is an integer value");
		  	System.exit(0);		  	
		  }
		}
		
		
		else if (arg.equals("-c")) {
			//check for the latest version of TES
			//TES es = new TES();
			vChecker vc = new vChecker(version, devRelease);
			System.exit(0);
		}
		
		
		
		
		else if (arg.equals("-h")) {
			System.out.println("Usage: java TES <option> <option> ...");
			System.out.println("\r\nValid options are:");
			System.out.println("\t\t" +		"-p=<port> \t Bind TES to the specified port");
			System.out.println("\t\t" +		"-c \t Check for new versions of TES");
			System.out.println("");
			System.exit(0);
		}
		else {
			System.out.println("Invalid option: " + arg);
			System.out.println("Use -h for help");
			System.exit(0);
		}
		
		
	}

	TES echoServer =new TES(port, 0);

    echoServer.listen();
  }


  public TES(int port, int connections) {
    super(port, connections);
  }

                                  
  public void handleConnection(Socket server) {
    Connection connectionThread= new Connection(this, server);
    connectionThread.start();
  }
    
  public void run() {
    Connection currentThread= (Connection)Thread.currentThread();
    try {
      super.handleConnection(currentThread.serverSocket);
    } catch(IOException ioe) {
      System.out.println("IOException: " + ioe);
      ioe.printStackTrace();
    }
  }
}
