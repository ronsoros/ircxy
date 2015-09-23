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
 *	David Forrest (david@tesx.org)
 *
 */
 
import java.util.Vector;
import java.io.*;

public class hub {

	public static Vector servers = new Vector();
	public static int scounter = 0;
	public static commandProcessor cp;
	public static int servercount;
	public static String version = "1.317"; //from 1.312 hub-versions will match the corresponding tes version.
	
	public static File conffile = new File("hub.conf");
	
	public static String hubName = "hub";
	public static String hubDescription = "hub Description";
	public static String hubPassword = "0";
	public static boolean webReport = true;
	public static String adminContact = "Undefined";
	public static String webLink = "";
	public static String webServers = "";
	
	private static int maxWhoWas = 0;
	
	private static boolean quiet = true;
	
	
	

	public static void main( String[] args ) {
	
		String arg = "";
		
		for (int i=0; i<args.length; i++) {
			arg = args[i];


			if (arg.equals("-d")) {
				quiet = false;
			}
			
			
			else if (arg.equals("-v")) {
				System.out.println("TES hub\tv" + version + " (www.tesx.org)");
				System.out.println("Part of the TES project by David Forrest.");
				System.exit(0);
			}
			
			else if (arg.equals("-h")) {
				System.out.println("Usage: java hub <option> <option> ...");
				System.out.println("\r\nValid options are:");
				System.out.println("\t\t" +		"-d \t Run in Debug Mode");
				System.out.println("\t\t" +		"-v \t Display Version Information");
				System.out.println("");
				System.exit(0);
			}
			else {
				System.out.println("Invalid option: " + arg);
				System.out.println("Use -h for help");
				System.exit(0);
			}
			
		}

		System.out.print("TES hub\tv" + version);
		if (!quiet) { System.out.print( "\t- Running in Debug Mode." ); }
		System.out.println("\r\n");

		
		cp = new commandProcessor(quiet); //initialise the command processor

		/* read the conf file. */
		if (conffile.exists()) {
			
			try {
				FileReader s0 = new FileReader(conffile);
				BufferedReader s1 = new BufferedReader(s0);
				
				String tmpLine;
				String strOption = "";
				int strValue = 0;
				int tmpIndex = 0;
			
				
			    try {


					while (true) {
					  String line = s1.readLine();
					  if (line == null)
						break;
			
					  /* deal with the line */
					  line = line.trim();
			
					  if ( (line.length() >0) && (line.charAt(0) != '#') ) {
					    /* System.out.println("conf; " + line); */
			
			
						tmpIndex = line.indexOf(" ");
						int tI = line.indexOf("\t");
						
			
						if ((tI != -1) &&    ((tmpIndex == -1) || (tmpIndex > tI)))
								tmpIndex = tI;
						else if ( (tmpIndex == -1) && (tI == -1) ) {
								System.out.println("\r\nParse error in config line: " + line);
								System.exit(0);
						}
			
			
			
						strOption = line.substring(0, tmpIndex);
						tmpLine = line.substring(tmpIndex+1).trim();
						/* System.out.println("::::" + strOption + "::::" + tmpLine + "::::"); */
			
						if (strOption.equalsIgnoreCase("hub-password")) {
							hubPassword = tmpLine;
						
						} else if (strOption.equalsIgnoreCase("hub-name")) {
							hubName = tmpLine;
						} else if (strOption.equalsIgnoreCase("hub-description")) {
							hubDescription = tmpLine;						
						
						
						} else if (strOption.equalsIgnoreCase("web-report")) {
							if (tmpLine.equalsIgnoreCase("yes")) {
								webReport = true;
							} else {
								webReport = false;
							}
						} else if (strOption.equalsIgnoreCase("web-admin-contact")) {
							adminContact = tmpLine;
						} else if (strOption.equalsIgnoreCase("web-website")) {
							webLink = tmpLine;
							
						} else if (strOption.equalsIgnoreCase("max-whowas")) {
							try {
								maxWhoWas = Integer.parseInt(tmpLine);
							} catch (NumberFormatException e) { maxWhoWas = 0; }
						
						
						
						} else if (strOption.equalsIgnoreCase("server")) {
							/* serverip:port:password */
							String tmp = tmpLine;
							int port = 6667;
							String ip = "";
							String pass = "";
							if (tmp.indexOf(":") != -1) {
								ip = tmp.substring(0, tmp.indexOf(":"));
								tmp = tmp.substring(tmp.indexOf(":")+1);
							}
							if (tmp.indexOf(":") != -1) {
								port = Integer.parseInt(tmp.substring(0,tmp.indexOf(":")));
								tmp = tmp.substring(tmp.indexOf(":")+1);
							}
							pass = tmp;
								
							webServers = webServers + "_" + ip + "*" + port;
							servers.addElement( new serverConnection( ip, port, pass, cp, scounter, quiet ) );
							scounter++;
						}
						
						
						
						



					  }
			
			
					}
			
					s1.close();
			
				    } catch(IOException e) {
					System.out.println("Eeek:: " + e);
				    }
				  } catch (FileNotFoundException e) {
					System.out.println("Error: " + e);
				  }
			
			
			
			
			
			
			
			
					/* Now start the hub connecting to servers.. */
					System.out.println("\r\n");
					System.out.println("Network tree: (" + scounter + " servers)\r\n");
					
					System.out.println("HUB ----\\");
					serverConnection tmpsc = null;
					
					for (int i=0; i<servers.size(); i++) {
						tmpsc = (serverConnection)servers.elementAt(i);
						System.out.println("	|- " + tmpsc.getServerName() + " (" + tmpsc.getServerPort() + ")" );
					}
				
					System.out.println("\r\n");
					
					if (quiet) { System.out.println("Hub launched successfully."); }
					
					
					/* give cp the server information. */
					cp.setMaxWhoWas( maxWhoWas );
					cp.setServers( servers );
					cp.setHubName( hubName );
					cp.setHubDescription( hubDescription );
					cp.setConfFile( conffile );
					
					if (webReport) {
						webContact wc = new webContact(hubName, version, adminContact, hubDescription, webLink, webServers, cp );
					}						
					
					
					/* start connecting to the servers. */
					serverConnection tmp = null;
					for (int i=0; i<servers.size(); i++) {
						tmp = (serverConnection)servers.elementAt( i );
						tmp.startSC();
					}
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
		} else {
			System.out.println("No configuration file found: " + conffile);
			System.out.println(" --> You NEED a conf file !!! <--");
			System.out.println("");
			System.out.println("Go make one..");
			System.exit(0);
		}












	
	}
	/* end class hub */
}