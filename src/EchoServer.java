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
 *	David Forrest (david@tesx.org)
 *
 */


import java.net.*;
import java.io.*;
import java.util.StringTokenizer;
import java.util.Date;
import java.util.Vector;



/*
	Tes - Java Threaded Echo Server.
	A Java ircx server.

	Started: Aug 14, 2001.
	Author: David Forrest (david@tesx.org)


*/


public class EchoServer extends NetworkServer {

	protected static String version = "1.317";
	protected static boolean devRelease = false;


  

  /*
  	TES operates on the following principles:
  	The server holds an array of users (initially empty).
  	
  	When a client connects, it is allocated to a free slot in the array, and
  	thereafter referenced by it's index in that array.
  	
  	When a client disconnects, the array slot corresponding to that user is
  	freed, so that it may be re-used at a later time.
  	
  	A max_connections variable restrics the size of the array.

  */
  
  
  
  
	//Date and time of server startup / creation
	Date dt = new Date();
	String serverCreationDate = dt.toString(); 
	long sst = System.currentTimeMillis();
	long hubConnectedAt = System.currentTimeMillis();



	// --------- options
	//most of these are redefined from the ircx.conf file.
	File conffile = new File("ircxy.conf");


	protected String serverName = "irc.tesx.org"; //this must NOT contain spaces.
	protected String serverDescription = "TES - Java IRCX Server";

	boolean localhostONLY = false; //connections are only allowed from localhost ?
	
	int max_allowed_users = 100; //max number of users allowed on the server at one time.
	int max_connections = 50; //This is the initial array size, prior to any expansion.
	int users_expand_increment = 50; //Expand array by 50 each time ?
	
	
	User users[];
	
	webContact wc = null;
	
	Pinger pings[];
	int pingerInterval = 180;

	numerics raws;


	int max_channels = 100; //max number of open channels allowed at one time.
	Channel channels[];

	int userCount = 0;
	int operCount = 0;
	int chanCount = 0;


	Vector opers = new Vector(); //holds list of:  Oper_nick Oper_pass
	File operfile = conffile; //new File("olines.txt");


	int cloneLimit = 2;
	int txtFloodLimit = 15;

	String nickBadChars = "";
	String identBadChars = "";
	String chanBadChars = "";
	String invChars = ""; //string of chars from 128 - 159, built later


	File kfile = new File("klines.txt");
	Vector klines = new Vector(); //holds list of users banned from the server.
	
	
	String klinemail = "<undefined e-mail address>";


	static int port = 6667;
	String serverIP = "";

	int maxNickLen = 20;
	int maxIdentLen = 20;
	int maxRealnameLen = 20;
	int maxChanLen = 20;
	int maxTopicLen = 350;
	int maxUCJ = 10; //user can join 10 channels
	
	
	long cNickDelay = 15000; //ms between nickchanges.

	File motdfile = new File("motd.txt");
	String motd[];
	int motdLines = 0;

	String welcomeMsg = "";



	public Vector iLines = new Vector();


	boolean initPhase = true;


	boolean showIPs = false;
	boolean maskIPs = false;
	int maskType = 1;
	String ipMask = "masked";

	CS cs;	//chanserv
	boolean useChanserv = false;
	boolean csLocalhost = false;
	boolean csHiddenWhois = false;
	boolean csActive = false;
	
	String adminMe = "no info set";
	String adminLoc1 = "no location set";
	String adminLoc2 = "no location set";
	String adminEmail = "no contact info set";
	
	
	int highestUserCount = 0;
	
	String protectedNicks = "";
	String forceJoin = "";
	String forceOper = "";
	Vector operOnlyChans = new Vector();
	
	String serverFullNotice = "This server is full!";
	boolean rehash = false;
	
	int hubID = -1; //the userID of the hub (if connected).
	int hubPingInterval = 320; //ping interval for hub
	
	Vector rav = new Vector(); //request-approved-vector.
	int globalUserCount = 0;
	int globalHighestUserCount = 0;
	
	boolean webReport = true;
	String webDescription = "A friendly place to chat";
	String webLink = "";
	
	boolean oldJavaVersion = false; //Java versions prior to 1.3 didn't support socket.shutdownInput()/output
	
	boolean debug = false;
	
	Vector closedChans = new Vector(); //channels that' have been closed by opers
	
	int netServerCount = 1;
	
	ial whos = new ial();
	int maxWhoWas = 0;
	
	boolean enableSharing = true;
	int maxSongMatches = 100;
	
	boolean hubcompress = false; //compress privmsgs between hub and server.
	replacer compresser;
	
	//messages - From servers to hub
	private String O_CHANLIST = 	"&1";
	private String O_USERLIST = 	"&2";
	private String O_CHANSETTING =  "&3";
	private String O_SERVERDESC =   "&4";
	private String O_NEWSERVER =	"&5";
	private String O_LISTSERVERS =  "&6";
	private String O_REMOVESERVER = "&7";
	private String O_RECONSERVER =  "&8";
	private String O_REQUEST_HUB_UPTIME = "&9";
	private String O_REQUESTLINKS = "&10";
	private String O_PASSINFO =     "&11";
	private String O_OPER =		 "&12";
	private String O_UNOPER =	   "&13";
	private String O_CLEARACCESS =  "&14";
	private String O_ADDACCESS =    "&15";
	private String O_REMACCESS =    "&16";
	private String O_GETWHOISFOR =  "&17";
	private String O_USERCONNECTING = "&18";
	private String O_CHANMODE =	 "&19";
	private String O_RUMOC =		"&20";
	private String O_SBOC =		 "&21";
	private String O_USBOC =		"&22";
	private String O_SUMOC =		"&23";
	private String O_IDLER_QUIT =   "&24";
	private String O_USERQUIT =	 "&25";
	private String O_REQUEST_NICKCHANGE = "&26";
	private String O_NICKCHANGE =   "&27";
	private String O_CHANJOIN =	 "&28";
	private String O_CHANPART =	 "&29";
	private String O_NCE =		  "&30";
	private String O_NC =		   "&31";
	private String O_PMCE =		 "&32";
	private String O_PMC =		  "&33";
	private String O_PMU =		  "&34";
	private String O_FINDTOPIC =    "&35";
	private String O_TOPICCHANGE =  "&36";
	private String O_CHANPROP = 	"&37";
	private String O_KICK =		 "&38";
	private String O_REQUEST_CHANMODES = "&39";
	private String O_REQUEST_NAMES = "&40";
	private String O_REQUEST_CHANJOIN = "&41";
	private String O_SEND_LIST		= "&42";
	private String O_KILL			 = "&43";
	private String O_BROADCAST_REQUEST= "&44";
	private String O_FRAW_REQUEST=	  "&45";
	private String O_RFR			  = "&46";
	private String O_PASS_SERVICES	= "&47";
	private String O_GONOTICE_REQUEST = "&48";
	private String O_WHO_REQUEST	  = "&49";
	private String O_REQUEST_SILENCES = "&50";
	private String O_ISON_REQUEST	 = "&51";
	private String O_WHOWAS_REQUEST   = "&52";
	private String O_REQUEST_WHISPER  = "&53";
	private String O_CLOSE_CHAN	   = "&54";
	private String O_GLINES		   = "&55";
	private String O_UNGLINE		  = "&56";
	private String O_FILESHARE		= "&57";
	private String O_FILESHARE_REPLY  = "&58";
	private String O_NOTICE_REMOTE_OPERS="&59";
	private String O_CHGHOST		  = "&60";
	private String O_DEBUG_SWITCH	 = "&61";
	private String O_COMPRESS_DATA	= "&62";
	
	
	//Input messages - From hub to servers
	private String I_NICKCOLLISION =		"^1";
	private String I_SENDCHANLISTS =		"^2";
	private String I_CHANCOLLISION =		"^3";
	private String I_SERVER_SPLIT_NOTICE =  "^4";
	private String I_SERVER_SPLIT_CHAN =	"^5";
	private String I_PASS_INFO =			"^6";
	private String I_NEW_OPER =			 "^7";
	private String I_CLEAR_ACCESS =		 "^8";
	private String I_ADD_ACCESS =		   "^9";
	private String I_REM_ACCESS =		   "^10";
	private String I_WHOIS_REQUEST =		"^11";
	private String I_USER_CONNECTED =	   "^12";
	private String I_NEW_USERCOUNT =		"^13";
	private String I_CHAN_MODE =			"^14";
	private String I_SUMOC =				"^15";
	private String I_SBOC =				 "^16";
	private String I_USBOC =				"^17";
	private String I_USER_QUIT =			"^18";
	private String I_NICKCHANGE_ALLOWED =   "^19";
	private String I_NICKCHANGE_DENIED =	"^20";
	private String I_NICK_CHANGE =		  "^21";
	private String I_CHAN_JOIN =			"^22";
	private String I_CHAN_PART =			"^23";
	private String I_NO_SUCH_CHANNEL =	  "^24";
	private String I_NO_SUCH_NICK =		 "^25";
	private String I_TOPIC_CHANGE =		 "^26";
	private String I_CHAN_PROP =			"^27";
	private String I_KICK_FROM_CHAN =	   "^28";
	private String I_NOT_CHAN_OP =		  "^29";
	private String I_REQUEST_CHANMODES =	"^30";
	private String I_CLONE_CHANNEL =		"^31";
	private String I_CHANJOIN_ALLOWED =	 "^32";
	private String I_DISABLE_WEBREPORT =	"^33";
	private String I_NOTICE_ALL=			"^34";
	private String I_FRAW =				 "^35";
	private String I_KILL =				 "^36";
	private String I_NEW_SERVER=			"^37";
	private String I_PASS_SERVICES=		 "^38";
	private String I_NOTICE_OPER=		   "^39";
	private String I_NEW_SERVERCOUNT=	   "^40";
	private String I_SILENCES_REQ=		  "^41";
	private String I_WHISPER_USER=		  "^42";
	private String I_PASS_INFO_TESHASERV=		"^43";
	private String I_NEW_GLINE=			 "^44";
	//-------------------------





  public static void main(String[] args) {
    if (args.length > 0)
      port = Integer.parseInt(args[0]);
    EchoServer echoServer = new EchoServer(port, 0);
    echoServer.listen();

  }


  public String version() {
  	return version;
  }
  public boolean isDev() {
  	return devRelease;
  }
  
  

	//Shutdown the socket's input and output stream, setting them at EOF
	//NOTE: Java versions prior to 1.3 (or so) do not support this feature.
	public void sInpOut( Socket s ) {
	//if (!oldJavaVersion) {
	// 			try {
	// 				s.shutdownInput();
	//				 shutdownOutput();
	// 			} catch( IOException e ) {}
	//}
	}//end sInpOut








  public EchoServer(int port2, int maxConnections) {
    super(port2, maxConnections);
	port = port2;

	System.out.println("\r\n");
	
	System.out.println(" HACKED TES IRCXY  v."+version);
		
	System.out.println( "Java IRCX Server" );
	System.out.println( "Modified by Ronsor" );
	System.out.println("");


	//Initialise everything
    initConfFile();

	buildBadChars();
    initUserArray();
    initChannelArray();
    initMOTD();
    initKLines();
    initWebContact();

	raws = new numerics( serverName );

    initPhase = false;

	System.out.println( "\r\nSystem ready:\r\n" );


  }





  // --------------------- INITIALISATION METHODS ------------------

  public void initWebContact() {
  	if (webReport) {
		wc = new webContact(serverName, "" + port, version, adminEmail, webDescription, webLink,   this );
	  }
  }


  public void buildBadChars() {
  	char c;
  	for (int i=128; i<=159; i++) {
  		c = (char)i;
  		invChars = invChars + "" + c;
  	}
  }


  public void initConfFile() {
	//this'll parse the main server configuration file.
	//setting up all the options as it goes along.
	//Treat lines in conf file beginning with # as comments

	int iLinesSize = iLines.size();
	int oLinesSize = opers.size();
	int kLinesSize = klines.size();

	if (conffile.exists()) {

	  try {
		FileReader s0 = new FileReader(conffile);
		BufferedReader s1 = new BufferedReader(s0);

		String tmpLine;
		String strOption = "";
		int strValue = 0;
		int tmpIndex = 0;

		welcomeMsg = ""; //reset the welcomeMsg - Thanks to Cunka for pointing this out.

	    try {

		//read file into array
		while (true) {
		  String line = s1.readLine();
		  if (line == null)
			break;

		  //deal with the line
		  line = line.trim();

		  if ( (line.length() >0) && (line.charAt(0) != '#') ) {
		    //System.out.println("conf; " + line);


			tmpIndex = line.indexOf(" ");
			int tI = line.indexOf("\t");
			

			if ((tI != -1) &&    ((tmpIndex == -1) || (tmpIndex > tI)))
					tmpIndex = tI;
			else if ( (tmpIndex == -1) && (tI == -1) ) {
					System.out.println("\r\nError in config line: " + line);
					System.exit(0);
			}



			strOption = line.substring(0, tmpIndex);
			tmpLine = line.substring(tmpIndex+1).trim();
			//System.out.println("conf; " + strOption + " .... " + tmpLine);


					
		


			// IMPORTANT - The !rehash in this "if" statement indicates that this option CANNOT be rehashed!
			if (strOption.equalsIgnoreCase("max-connections") && !rehash) {
				strValue = Integer.parseInt(tmpLine);
				if (strValue == 0) {
					System.out.println("WARNING! YOU MUST SET A VALUE FOR MAX_CONNECTIONS GREATER THAN 0 !!");
					System.out.println("Exiting..");
					System.exit(0);
				} else {
					max_allowed_users = strValue;
				}

			}
			
			else if (strOption.equalsIgnoreCase("initial-uas") && !rehash) {
				strValue = Integer.parseInt(tmpLine);
				max_connections = strValue;
			}
			else if (strOption.equalsIgnoreCase("ua-expansion") && !rehash) {
				strValue = Integer.parseInt(tmpLine);
				users_expand_increment = strValue;
			}
			
			
			else if (strOption.equalsIgnoreCase("max-channels") && !rehash) {
				strValue = Integer.parseInt(tmpLine);
				max_channels = strValue;
			}
			
			else if (strOption.equalsIgnoreCase("max-whowas")) {
				strValue = Integer.parseInt(tmpLine);
				if (strValue < 0) { strValue = 0; }
				maxWhoWas = strValue;
				whos.max_dead_size = maxWhoWas;
			}
			
			else if (strOption.equalsIgnoreCase("clone-limit")) {
				strValue = Integer.parseInt(tmpLine);
				cloneLimit = strValue;
			}
			
			else if (strOption.equalsIgnoreCase("i-line")) {
				iLines.add( tmpLine );
			}
			
			else if (strOption.equalsIgnoreCase("o-line")) {
				opers.add( tmpLine );
			}
			
			else if (strOption.equalsIgnoreCase("txt-flood-limit")) {
				strValue = Integer.parseInt(tmpLine);
				txtFloodLimit = strValue;
			}
			
			else if (strOption.equalsIgnoreCase("ping-interval")) {
				strValue = Integer.parseInt(tmpLine);
				pingerInterval = strValue;
			}
			else if (strOption.equalsIgnoreCase("hub-ping-interval")) {
				strValue = Integer.parseInt(tmpLine);
				hubPingInterval = strValue;
			}

			else if (strOption.equalsIgnoreCase("server-port") && !rehash) {
				strValue = Integer.parseInt(tmpLine);
				port = strValue;
			}
			else if (strOption.equalsIgnoreCase("server-ip") && !rehash) {
				serverIP = tmpLine;
			}

			else if (strOption.equalsIgnoreCase("max-nick-length")) {
				maxNickLen = Integer.parseInt(tmpLine);
			}
			else if (strOption.equalsIgnoreCase("max-ident-length")) {
				maxIdentLen = Integer.parseInt(tmpLine);
			}
			else if (strOption.equalsIgnoreCase("max-realname-length")) {
				maxRealnameLen = Integer.parseInt(tmpLine);
			}
			else if (strOption.equalsIgnoreCase("max-chan-length")) {
				maxChanLen = Integer.parseInt(tmpLine);
			}
			else if (strOption.equalsIgnoreCase("max-topic-length")) {
				maxTopicLen = Integer.parseInt(tmpLine);
			}
			
			else if (strOption.equalsIgnoreCase("max-user-can-join")) {
				maxUCJ = Integer.parseInt(tmpLine);
			}

			else if (strOption.equalsIgnoreCase("server-name")) {
				serverName = tmpLine;
			}
			else if (strOption.equalsIgnoreCase("server-description")) {
				serverDescription = tmpLine;
			}


			else if (strOption.equalsIgnoreCase("admin-me")) {
				adminMe = tmpLine;
			}
			else if (strOption.equalsIgnoreCase("admin-location1")) {
				adminLoc1 = tmpLine;
			}
			else if (strOption.equalsIgnoreCase("admin-location2")) {
				adminLoc2 = tmpLine;
			}
			else if (strOption.equalsIgnoreCase("admin-email")) {
				adminEmail = tmpLine;
			}
			
			
			
			else if (strOption.equalsIgnoreCase("welcome-msg")) {
				welcomeMsg = welcomeMsg + tmpLine + "\r\n";
			}


			else if (strOption.equalsIgnoreCase("ip-addresses") && !rehash) {
				if (tmpLine.equals("1"))
					showIPs = true;
			}

			else if (strOption.equalsIgnoreCase("mask-ips")) {
				if (tmpLine.equals("1"))
					maskIPs = true;
			}
			
			else if (strOption.equalsIgnoreCase("mask-type")) {
				if (tmpLine.equals("1")) {
					maskType = 1;
				} else if (tmpLine.equals("2")) {
					maskType = 2;
				} else {
					maskType = 1; //default
				}
			}

			else if (strOption.equalsIgnoreCase("ip-mask")) {
					ipMask = tmpLine;
			}
			else if (strOption.equalsIgnoreCase("illegal-nick-chars")) {
				nickBadChars = tmpLine;
			}
			else if (strOption.equalsIgnoreCase("illegal-ident-chars")) {
				identBadChars = tmpLine;
			}
			else if (strOption.equalsIgnoreCase("illegal-chan-chars")) {
				chanBadChars = tmpLine;
			}
			
			else if (strOption.equalsIgnoreCase("illegal-chan")) {
				closedChans.add( tmpLine.toLowerCase() );
			}
			
			
			//else if (strOption.equalsIgnoreCase("k-line")) {
			//	klines.add( tmpLine );
			//}			
			else if (strOption.equalsIgnoreCase("kline-email")) {
				klinemail = tmpLine;
			}

			else if (strOption.equalsIgnoreCase("use-chanserv") && !rehash) {
				if (tmpLine.equals("1"))
					useChanserv = true;
			}

			else if (strOption.equalsIgnoreCase("service-localhost") && !rehash) {
				if (tmpLine.equals("1"))
					csLocalhost = true;
			}

			else if (strOption.equalsIgnoreCase("service-whois")) {
				if (tmpLine.equals("0"))
					csHiddenWhois = true;
			}

			else if (strOption.equalsIgnoreCase("protected-nick")) {
				protectedNicks = protectedNicks + tmpLine + ":";
			}
			
			else if (strOption.equalsIgnoreCase("force-join")) {
				forceJoin = tmpLine;
			}
			
			else if (strOption.equalsIgnoreCase("force-oper")) {
				forceOper = tmpLine;
			}
			
			else if (strOption.equalsIgnoreCase("oper-only-chan")) {
				if (!operOnlyChans.contains( tmpLine.toLowerCase() )) {
					operOnlyChans.add( tmpLine.toLowerCase() );
				}
			}
			
			else if (strOption.equalsIgnoreCase("server-full")) {
				serverFullNotice = tmpLine;
			}
			
			
			else if (strOption.equalsIgnoreCase("old-java")) {
				if (tmpLine.equals("1")) {
					oldJavaVersion = true;
				} else {
					oldJavaVersion = false;
				}
			}
			

			
			else if (strOption.equalsIgnoreCase("enable-file-sharing")) {
				if (tmpLine.equals("yes")) {
					enableSharing = true;
				} else {
					enableSharing = false;
				}
			}
			else if (strOption.equalsIgnoreCase("max-search-buffer")) {
				maxSongMatches = Integer.parseInt( tmpLine );
			}
			
			else if (strOption.equalsIgnoreCase("web-report")) {
				if (tmpLine.equals("yes") || tmpLine.equals("1")) {
					webReport = true;
					if (!rehash) { System.out.print("(Web-Report: Enabled)"); }
				} else {
					webReport = false;
					if (!rehash) { System.out.print("(Web-Report: Disabled)"); }
				}
			}
			
			else if (strOption.equalsIgnoreCase("web-description")) {
				webDescription = tmpLine;
			}	
			else if (strOption.equalsIgnoreCase("web-url")) {
				webLink = tmpLine;
			}			
			
			else if (strOption.equalsIgnoreCase("hub-compress") && !rehash) {
				if (tmpLine.equalsIgnoreCase("on")) {
					 compresser = new replacer();
					 hubcompress = true;
					 System.out.print("(Data-Compression: Enabled)");
				} else { hubcompress = false; }
			}
			
			
			/*
			else
				System.out.println( "conf; Unrecognised option->" + line );
			*/



		  }


		}

		s1.close();
		//System.out.println("conf; -End of configuration file.");

	    } catch(IOException e) {
		System.out.println("Eeek:: " + e);
	    }
	  } catch (FileNotFoundException e) {
		System.out.println("Error: " + e);
	  }

		if (!rehash) {
			channels = new Channel[ max_channels ];
			
			
			if (max_connections > max_allowed_users) {
				max_connections = max_allowed_users;
			}
			
			users = new User[max_connections];
			pings = new Pinger[max_connections];
			
			
			//System.out.print("(" + klines.size() + " k-lines)");
			System.out.print("(" + opers.size() + " o-lines)");
			
			
		}
		
		if (rehash) {
			for (int i=0; i<iLinesSize; i++) {	iLines.removeElementAt(0);	}
			for (int i=0; i<oLinesSize; i++) {	opers.removeElementAt(0);	}
			for (int i=0; i<kLinesSize; i++) {	klines.removeElementAt(0);	}
		}
		

	} else {
		if (!rehash) {
			System.out.println("No configuration file found: " + conffile);
			System.out.println(" --> You NEED a conf file !!! <--");
			System.out.println("");
			System.out.println("Go make one..");
			System.exit(0);
		}
	}

	rehash = true; //The next time this method is called, it will be with /REHASH

  }






  public void initUserArray() {
	//System.out.println("Init user array...size:" + max_connections + " " + users.length);
	for (int i=0; i < max_connections; i++) {
		users[i] = new User();
		pings[i] = new Pinger(serverName, pingerInterval, this);
	}
  }


	public boolean expandArray() {
		//if max_allowed_users is <= 100 the array is already at a max size, don't expant it.
		if ((max_allowed_users > 100) && (users.length < max_allowed_users)) {
			//find out which is smaller. the array_size+users_expand_increment, or max_allowed_users
			int expandSize = max_allowed_users;
			if ( (users.length+users_expand_increment) < max_allowed_users ) {
				expandSize = users.length+users_expand_increment;
			} else {
				expandSize = max_allowed_users;
			}

			//create, and initialise, new arrays.
			User u2[] = new User[expandSize];
			Pinger p2[] = new Pinger[expandSize];
			for (int i=users.length; i<expandSize; i++) {
				u2[i] = new User();
				p2[i] = new Pinger(serverName, pingerInterval, this);
			}
			//expand the arrays
			System.arraycopy( users, 0, u2, 0, users.length );
			System.arraycopy( pings, 0, p2, 0, pings.length );
			users = u2;
			pings = p2;
			max_connections = expandSize;
			u2 = null;
			return true;
		} else {
			return false;
		}
	}



  public void initChannelArray() {
	//System.out.println("Init channel array...size:" + max_channels);
	for (int i=0; i < channels.length; i++) {
		channels[i] = new Channel();
	}
  }



  public void initMOTD() {
	if (motdfile.exists()) {

	  try {
		FileReader s0 = new FileReader(motdfile);
		BufferedReader s1 = new BufferedReader(s0);

		String tmpLine;
		motdLines=0;

	    try {
		while ( (tmpLine = s1.readLine()) != null )
			motdLines++;

		s1.close();

		motd = new String[motdLines];


		s0 = new FileReader(motdfile);
		s1 = new BufferedReader(s0);


		int index = 0;
		while (true) {
		  String line = s1.readLine();
		  if (line == null)
			break;

		  //deal with the line
		  motd[index] = line;
		  index++;
		}

		s1.close();

		//if (initPhase)
		//	System.out.println("(motd is " + motdLines + " lines)");

	    } catch(IOException e) {
		System.out.println("Eeek:: " + e);
	    }
	  } catch (FileNotFoundException e) {
		System.out.println("Error: " + e);
	  }

	} else {
		if (initPhase)
			System.out.print("(MOTD file missing)");
	}


  }

  public void initKLines() {
    //String glines = "";
	if (kfile.exists()) {

	  try {
		FileReader s0 = new FileReader(kfile);
		BufferedReader s1 = new BufferedReader(s0);

		String tmpLine;

	    try {


		s0 = new FileReader(kfile);
		s1 = new BufferedReader(s0);

		klines.clear();
		
		while (true) {
		  String line = s1.readLine();
		  if (line == null)
			break;

		  boolean global = false;
		  if (line.trim().startsWith("g-line")) { global = true; }
		  line = line.substring(7);
		  String mask = "";
		  String setter = "";
		  String reason = "";
		  if (line.indexOf(" ") == -1) {
		  	//old format - no extra info, only mask.
		  	mask = line.trim();
		  } else {
		  	mask = line.substring(0, line.indexOf(" "));
		  		line = line.substring( line.indexOf(" ")+1 );
		  	setter = line.substring(0, line.indexOf(" "));
		  		line = line.substring( line.indexOf(" ")+1 );
		  	reason = line;
		  }
		  KLine nkline = new KLine( mask, global, setter, reason );
		  klines.add( nkline );
		}

		s1.close();


	    } catch(IOException e) {
		System.out.println("Eeek:: " + e);
	    }
	  } catch (FileNotFoundException e) {
		System.out.println("Error: " + e);
	  }

	} else {
		if (initPhase) {
			try {
				if (kfile.createNewFile()) {
					System.out.print("(" + kfile + " was created)");
				} else {
					System.out.print("(" + kfile + " could not be created)");
				}
			} catch(IOException e) {}
		}
	}
	//if (glines.length() > 0) { glines = glines.substring(1); }
	//return glines;

  }





  // ---------------------------------------------------------------
  // ---------------------------------------------------------------










  //given a nick, find the matchind userID..
  public int userIDfromNick(String strNick) {
	int index = -1;
	boolean foundUser = false;

	while (!foundUser && (index < (max_connections-1))) {
		index++;

		if (users[index].ID() != -1) {
			if (users[index].getNick().equalsIgnoreCase(strNick)) {
				foundUser = true;
			}
		}
	}

	if (foundUser) {
		return index;
	} else {
		return -1; //No such user found.
	}

  }



  //attempt to find an available userID for a new user..
  public int newUserID(Socket socket) throws IOException {
	//go through the array and find the first available (id=-1) index.
	int index = -1;
	boolean foundFree = false;

	while (!foundFree && (index < (max_connections-1))) {
		index++;

		if (users[index].ID() == -1) {
			//found a free one, reserve it, and break loop
			users[index].setID(-2);
			users[index].setSocket(socket);
			foundFree = true;
		}
	}

	if (foundFree) {
		userCount++;
		if (userCount > highestUserCount)
			highestUserCount = userCount;
			
			
		return index;
	} else {
		return -1; //maxed out ! no free connections.
	}
  }

  //When a user disconnects, free up their userID for later use..
  public void releaseUserID(int userid) throws IOException{

	boolean netsplit = false;
	
	pings[userid].stopPinger();

	if (users[userid].isSysop()>0) {
	 operCount--;
	 tellServices( "UNOPER: " + users[userid].getNick() );
	}
	//if it was the hub which disconnected, flag a netsplit.
	if (users[userid].isSysop() == 10) {
	 hubID = -1;
	 netsplit = true;
	}

	if (users[userid].ID() != -1)
	 userCount--;

	users[userid].cleanUser();


	//deal with netsplit now
	if (netsplit)
		cleanNetsplit();
		

  }







  /* NOTE !!! countTokens() returns how many UNSEEN tokens are left !!! */
  public void handleCommand( String cmd, int userID ) throws IOException {

		//convert xml commands to irc command format.
		//<message value="/command arg1 arg2"/>
		if ((cmd.charAt(0) == '<') && (cmd.indexOf("=\"") != -1) && (cmd.endsWith("\"/>"))) {
			cmd = cmd.substring( cmd.indexOf("=\"")+2 );
			cmd = cmd.substring( 0, cmd.length()-3 );
			users[userID].xml = true;
			pings[userID].setXML( true );
		}



	  StringTokenizer st = new StringTokenizer(cmd, " ");
	  String command = st.nextToken();
	  command = command.trim().toUpperCase();



	//since privmsg is the most used command, check for it first in the else-if..
	if (command.equals("PRIVMSG")) {
		if (users[userID].isRegistered()) {
		  if (st.countTokens() >= 2) {
			String whereTo = st.nextToken();

			/*Construct the full message to be sent. Can be calculated from string lengths.
			  Full command	- cmd.lenght()
			  PRIVMSG	- 8 letters (including spaces)
			  #channel	- whereTo.length() +1
			  :		- 1 letter
			*/

			int tmpLen = whereTo.length() + 10;
			String fullMessage = cmd.substring(tmpLen);

			users[userID].setIdle( System.currentTimeMillis() );

			//bundled privmsg - Dana
			if (whereTo.indexOf(",") != -1) {
				String tmp = whereTo + ",";
				while (tmp.indexOf(",") != -1) {
					handleCommand("PRIVMSG " + tmp.substring(0, tmp.indexOf(",")) + " :" + fullMessage, userID);
					tmp = tmp.substring(tmp.indexOf(",")+1);
				}

			} else {

			//2 possibilities.
			//1. It's a privmsg to a channel
			if (whereTo.charAt(0) == '#') {
				int chanID = is_channel(whereTo);
				if (chanID != -1) {
					if (isMember(userID, chanID)) {
					  boolean userCanTalk = true;

					  if (channels[chanID].ismode("m")) {
						//moderated, only allow ops to talk
						//int tmpID = channels[chanID].userArrayPos(userID);
						if (channels[chanID].getMemberStatusByID(userID) == 0) {
							tellUser(userID, ":" + serverName + " 404 " + users[userID].getNick() + " " + whereTo + " :Cannot send to channel");
							userCanTalk = false;
						}
					  } else if (channels[chanID].ismode("c")) {
					  	//if (user[userID].isSysop() == 0) {
						  	if ( (fullMessage.indexOf( ((char)2) ) != -1) || (fullMessage.indexOf( ((char)3) ) != -1) ) {
						  		userCanTalk = false;
						  		tellUser(userID, ":" + serverName + " PRIVMSG " + channels[chanID].getName() + " :Sorry, Colors are not allowed in " + channels[chanID].getName());
						  	}
					  	//}
					  }

					  if (userCanTalk) {
					  	if (channels[chanID].ismode("S")) {
					  		//strip colour chars
					  		String fm = "";
								for (int i = 0; i < fullMessage.length(); i ++) {
									if (fullMessage.charAt(i) == ((char)2)) {
										i++;
									} else  if (fullMessage.charAt(i) == ((char)3)) {
										i++;
										if ((i< fullMessage.length()) && Character.isDigit(fullMessage.charAt(i))) { i++; }
										if ((i< fullMessage.length()) && Character.isDigit(fullMessage.charAt(i))) { i++; }
									}
									if (i< fullMessage.length()) {
										fm+= fullMessage.charAt(i);
									}
								}
							 fullMessage = fm;
					  	}
					  	
					  	
					  	
						  tellUsersInChanButOne(chanID, userID, ":" + users[userID].getNick() + "!" + users[userID].getIdent() + "@" + users[userID].getHostname() + " PRIVMSG " + whereTo + " :" + fullMessage);
						  if ((hubID != -1) && (userID != hubID)) {
						  	if (hubcompress) { fullMessage = compress(fullMessage); }
						  	tellUser(hubID, O_PMCE + " " + userMask(userID) + " " + whereTo + " " + fullMessage);
						  }

					  //} else
					  //	  tellUser(userID, ":" + serverName + " 404 " + users[userID].getNick() + " " + whereTo + " :Cannot send to channel");
					  }
					} else {
					  //check if channel is +n ?? TBD !!!

					}



				} else {
					if ((hubID != -1) && (userID != hubID)) {
						if (hubcompress) { fullMessage = compress(fullMessage); }
						tellUser(hubID, O_PMC + " " + userMask(userID) + " " + whereTo + " " + fullMessage);
					}
					else	
						tellUser(userID, ":" + serverName + " 403 " + users[userID].getNick() + " " + whereTo + " :No such channel");
				}


			 } else {
			//2. It's a privmsg to a user
				int tmpUID = userIDfromNick(whereTo);
				if (tmpUID != -1) {
				 //user exists on server, allow sending.
				 //no need to tell hub about this
				 if (users[tmpUID].hasSilenceMatch( userMaskNC(userID) ) == false) {
				 	//if they aren't on tmpUID's silence list, let the message through
				 	tellUser(tmpUID, ":" + users[userID].getNick() + "!" + users[userID].getIdent() + "@" + users[userID].getHostname() + " PRIVMSG " + whereTo + " :" + fullMessage);
				 }


				} else {
					if ((hubID != -1) && (hubID != userID)) {
				  	if (hubcompress) { fullMessage = compress(fullMessage); }
						tellUser(hubID, O_PMU + " " + userMask(userID) + " " + whereTo + " " + fullMessage );
						//System.out.println("PMU sent: " + userMask(userID) + " " + whereTo + " " + fullMessage );
					}
					else
						tellUser(userID, ":" + serverName + " 401 " + users[userID].getNick() + " " + whereTo + " :No such nick/channel");
				}



			}


		}


		  }
		} else {
			tellUserMustRegister(userID);
		}










	} else if (command.equals("NICK")) {
		
		if ((userID != hubID) && (hubID != -1) && (!hasCallback(userID + " " + cmd)) && (st.countTokens() == 1)) {
			String n = st.nextToken();
			if ((n.charAt(0) == ':') && (n.length() >1)) { n = n.substring(1); }
			tellUser(hubID, O_REQUEST_NICKCHANGE + " " + n + " " + userID);

		} else if  ((userID == hubID) || (((hubID == -1) && (st.countTokens() == 1)) || ((hubID != -1) && removeCallback(userID + " " + cmd)))) {
			
			
			String tmpNick = st.nextToken();
			String tmp = users[userID].getNick();
			if (tmp.equals("")) {
				tmp = "*";
			}

			//some clients send NICK :nickname (note the colon), deal with this properly - BanZai pointed this out
			if ((tmpNick.charAt(0) == ':') && (tmpNick.length() > 1))
				tmpNick = tmpNick.substring(1);


			if (invalidNick(tmpNick, userID)) {
				tellUser(userID, raws.r432(tmp, tmpNick) );
			} else if (!nickInUse(tmpNick)) {
				//Must inform ALL relevant users of this nick change !!!
				boolean canChange = true;
				if (users[userID].isRegistered()) {


				  if ((System.currentTimeMillis() - cNickDelay) > users[userID].lastNickChangeTime()) {
					users[userID].setLastNickChangeTime(System.currentTimeMillis());
					sendUserNickChange(userID, userMask(userID), tmpNick );
					canChange = true;
				  } else {
				    //nickchange too fast.438 nick :Nick change too fast. Please wait sec seconds.
				    long secWait = ((long)(((cNickDelay - (System.currentTimeMillis() - users[userID].lastNickChangeTime()))/1000))+1);
				    tellUser(userID, ":" + serverName + " 438 " + users[userID].getNick() + " :Nick change too fast. Please wait " + secWait + " seconds.");
					canChange = false;
				  }

				}
				if (canChange) {
					users[userID].setNick(tmpNick);
				}

				if ((users[userID].getIdent() != "") && !users[userID].isRegistered()) {
					users[userID].register();

					if (users[userID].isSysop() >0) {
						tellServices("OPER: " + users[userID].getNick());
						if ((hubID != -1) && (userID != hubID)) {
							tellUser(hubID, O_OPER + " " + users[userID].getNick() + " " + users[userID].isSysop());
						}
						//add the oper modes here, since they won't have been added during /oper (reason: no validnick)
						handleCommand( "MODE " + users[userID].getNick() + " +" + defaultSysopModes( users[userID].isSysop() ), userID );
					}
				 	
					welcomeUser(userID);
				}

			} else {

				tellUser(userID, raws.r433( tmp, tmpNick ) );
			}
		} else {
			tellUser(userID, "Format is NICK <nickname>");
		}
		
		
	} else if (command.equals("USER")) {
		if (users[userID].isRegistered() == true) {
			tellUser(userID, raws.r462( users[userID].getNick() ));
		} else if (st.countTokens() >= 4) {
			String tmpIdent = st.nextToken();
			if (invalidIdent(tmpIdent)) {
				tmpIdent = "new";
			}

			 String tmpUnused1 = st.nextToken();
			 String tmpUnused2 = st.nextToken();

			 String tmpRealname = cmd.substring(8 + tmpIdent.length() + tmpUnused1.length() + tmpUnused2.length());

			 if ((tmpRealname.charAt(0) == ':') && (tmpRealname.length() >1))
				tmpRealname = tmpRealname.substring(1);


			 users[userID].setIdent(tmpIdent);

			 setHostMask(userID);


				 if (users[userID].sentPass() != "")
				 	handleCommand("OPER " + tmpIdent + " " + users[userID].sentPass(), userID );
				 	

			 users[userID].setRealname(tmpRealname);
			 if (users[userID].getNick() != "") {
				 users[userID].register();
				 welcomeUser(userID);
			 }


		} else {
			tellUser(userID, "Format is USER <ident> <field2> <field3> <realname>");
		}

	} else if (command.equals("PASS")) {
		if (!users[userID].isRegistered()) {
			if (st.countTokens() == 1)
				users[userID].setPass( st.nextToken() );
			
		}


	} else if (command.equals("AWAY")) {
	     if (users[userID].isRegistered()) {
		if (users[userID].isAway()) {
		   users[userID].setBack();
		   tellUser(userID, raws.r305(users[userID].getNick())); 
		} else if (!users[userID].isAway() && (st.countTokens() == 0)) {
		   tellUser(userID, raws.r305(users[userID].getNick()));
		} else if (!users[userID].isAway()) {
		   users[userID].setAway(cmd.substring(5));
		   tellUser(userID, raws.r306(users[userID].getNick()));
		}
	     }






	} else if (command.equals("TIME")) {
		if (users[userID].isRegistered()) {
			Date d = new Date();
			tellUser(userID, ":" + serverName + " 391 " + users[userID].getNick() + " :" + d.toString() );
			
		}
		







	} else if (command.equals("JOIN")) {
		
		if (users[userID].isRegistered()) {
		 boolean moreToJoin = true;
		 boolean joinedTooMany = false;
		 String chans = "";



		if (st.countTokens() >= 1) {
		    chans = st.nextToken();
		    String tmpChan = chans;

			String key = "";
			if (st.countTokens() >= 1) {
				key = st.nextToken();
			}


		 while (moreToJoin && !joinedTooMany) {
		  if ((users[userID].getNumChansJoined() < maxUCJ) || (users[userID].isSysop()>0)) {

				if (tmpChan.indexOf(",") != -1) {
				  tmpChan = tmpChan.substring(0, tmpChan.indexOf(","));
				  chans = chans.substring(chans.indexOf(",")+1);
				} else {
				  moreToJoin = false;
				}

				int chanID = is_channel(tmpChan);
				if (chanID != -1) {
				 tmpChan = channels[chanID].getName(); //ensures correct capitalisation in the name.

				 if (!isMember(userID, chanID)) {

						if (true) {
						
						boolean giveOwner = false;
						boolean giveHost = false;
						boolean giveVoice = false;
						boolean isBanned = false;
						String njr = "";
						
						
						int njn = 000; //Added to track numeric for KNOCK - Dana
						
						if (hasVoiceAccess(userID, chanID)) {
							giveVoice = true;
						}
						
						if (users[userID].isSysop()>0) {
							giveOwner = true;

						} else if (hasOwnerAccess(userID, chanID)) {
							giveOwner = true;
						} else if (hasHostAccess(userID, chanID)) {
							giveHost = true;


						} else if (!key.equals("")) {
						  if ((channels[chanID].ownerkey() != "") && (channels[chanID].ownerkey().equals(key)))
							giveOwner = true;
						  else if ((channels[chanID].hostkey() != "") && (channels[chanID].hostkey().equals(key)))
							giveHost = true;
						} else if (isBannedFromChan(userID, chanID)) {
							isBanned = true;
						} else if ((channels[chanID].ismode("O") || isClosedChan(channels[chanID].getName())) && (users[userID].isSysop() == 0)) {
							isBanned = true;
							njr = raws.r913(users[userID].getNick());
							njn = 913; //add numeric tracking for KNOCK event - Dana
						} else if (channels[chanID].ismode("i") && (users[userID].isSysop() == 0)) {
							if (channels[chanID].hasInvite(users[userID].getNick())) {
							    channels[chanID].unsetInvite(users[userID].getNick());
							} else {
							    isBanned = true;
							    njr = ":" + serverName + " 473 " + users[userID].getNick() + " " + tmpChan + " :Cannot join channel (+i)";
							    njn = 473; //knock tracking - Dana
							}
						} else if ((channels[chanID].getLimit() > 0) && (channels[chanID].membercount() >= channels[chanID].getLimit())) {
							isBanned = true;
							njr = raws.r471(users[userID].getNick(), tmpChan);
							njn = 471; //knock tracking - Dana
						}




					//assign user to existing channel
					int tmpID;
					if (isBanned) {
						tmpID = -1;
					} else {
						tmpID = channels[chanID].add_user(userID, 0); // <-add as regular
					}

					if (tmpID != -1) {
						//successfull join, inform user + channel members
						tellUser( userID, userMask(userID) + " JOIN :" + tmpChan); // 1st
						
						if ((hubID != -1) && (userID != hubID))
							tellUser(hubID, O_CHANJOIN + " " + userMask(userID) + " " + tmpChan);
							
						
						users[userID].incNumChansJoined();

						String strTopic = channels[chanID].getTopic();
						if (!strTopic.equals("")) {
							tellUser(userID, raws.r332(users[userID].getNick(), tmpChan, channels[chanID].getTopic()));
							tellUser(userID, raws.r333(users[userID].getNick(), tmpChan, channels[chanID].whoSetTopic(), channels[chanID].topicTimeStamp()));
						}

						String tmpOJ = channels[chanID].onjoin();
						if (tmpOJ != "")
							tellUser(userID, ":" + tmpChan + " PRIVMSG " + tmpChan + " :" + tmpOJ);


						if (giveOwner) {
							channels[chanID].setMemberStatusByID(userID, 3);
						} else if (giveHost) {
							channels[chanID].setMemberStatusByID(userID, 2);
						} else if (giveVoice) {
							channels[chanID].setMemberStatusByID(userID, 1);
						}
						sendNamesList(userID, chanID);


						//inform the other people
						if (!key.equals("")) { key = " " + key; }
						tellUsersInChanButOne(chanID, userID, userMask(userID) + " JOIN :" + tmpChan);
						tellServices( userMask(userID) + " JOIN :" + tmpChan + key);

						
						if (giveOwner) {
						 //make him +q himself
						 //change the users status in the channel
						 //channels[chanID].setMemberStatusByID(userID,3);
							 if ((hubID != -1) && (userID != hubID)) {
							 	tellUser(hubID, O_SUMOC + " ." + userMask(userID) + " " + tmpChan + " +q " + users[userID].getNick() + " 1");
							 } else {
								 tellUsersInChan(chanID, userMask(userID) + " MODE " + tmpChan + " +o " + users[userID].getNick(),
								 						userMask(userID) + " MODE " + tmpChan + " +q " + users[userID].getNick());
							 }
						 
						}
						else if (giveHost) {
						 //channels[chanID].setMemberStatusByID(userID,2);
							 if ((hubID != -1) && (userID != hubID)) {
							 	tellUser(hubID, O_SUMOC + " @" + userMask(userID) + " " + tmpChan + " +o " + users[userID].getNick() + " 1");
							 } else {
	 							 tellUsersInChan(chanID, userMask(userID) + " MODE " + tmpChan + " +o " + users[userID].getNick());
	 						}
						}
						else if (giveVoice) {
							 if ((hubID != -1) && (userID != hubID)) {
							 	tellUser(hubID, O_SUMOC + " @" + userMask(userID) + " " + tmpChan + " +v " + users[userID].getNick() + " 1");
							 } else {
	 							 tellUsersInChan(chanID, userMask(userID) + " MODE " + tmpChan + " +v " + users[userID].getNick());
	 						}
						}

						//sendNamesList(userID, chanID);

					} else {
						//can't join channel, inform user - TBD !!! Why cant join? !!!
						if (isBanned && !njr.equals("")) {
							tellUser(userID, njr);
						} else if (isBanned) {
							tellUser(userID, raws.r474( users[userID].getNick(), tmpChan ));
							njn = 474;
						}

						if (channels[chanID].ismode("u"))
						   tellOpsInChan(chanID, userMask(userID) + " KNOCK " + tmpChan + " " + njn); //repaired knock reply - Dana

					}
				  
				  } //hub request
				 } else {
						//tellUser(userID, "already on that channel");
				 }


				} else {
					
					//make sure they're not trying to join an oper-only chan
					if ( (users[userID].isSysop() == 0) && isOperOnlyChan( tmpChan ) ) {
						tellUser(userID, raws.r913(users[userID].getNick()));
					} else if ( (users[userID].isSysop() == 0) && isClosedChan(tmpChan) ) {
						tellUser(userID, raws.r403(users[userID].getNick(), tmpChan));
					} else if ( (hubID != -1) && (!hasCallback(userID + " JOIN " + tmpChan)) ) {
						makeCallback(userID + " JOIN " + tmpChan);
						tellUser(hubID, O_REQUEST_CHANJOIN + " " + tmpChan + " " + userID + " " + users[userID].ircx + " " + users[userID].getNick() + " JOIN " + tmpChan + " " + key);
					} else if ( (hubID == -1) || ( (hubID != -1) && removeCallback(userID + " JOIN " + tmpChan)) ) {
							
								//check if valid channel name, and create it
								if (isValidChanName(tmpChan)) {
									int chanid = newChannelID(tmpChan);
									//add the user to the chan
									channels[chanid].add_user(userID, 3); // <-- make owner !!
									tellUser(userID, userMask(userID) + " JOIN :" + tmpChan);
									users[userID].incNumChansJoined();
									
									
									//construct names list
									String strStart = ":" + serverName + " 353 " + users[userID].getNick() + " = " + channels[chanid].getName() + " :";
									String strLNames = "." + users[userID].getNick();
									if (!users[userID].ircx) { strLNames = "@" + users[userID].getNick(); }
									
									String strEnd = ":" + serverName + " 366 " + users[userID].getNick() + " " + channels[chanid].getName() + " :End of /NAMES list.";
									//send names list
									tellUser(userID, strStart + strLNames);
									tellUser(userID, strEnd);
									
									
									//init lists...
									channels[chanid].setCreation( (long)(System.currentTimeMillis() / 1000)  );
									
									if (!key.equals("")) { key = " " + key; }
			
									if ((hubID != -1) && (userID != hubID)) {
										tellUser(hubID, O_CHANJOIN + " ." + userMask(userID) + " " + tmpChan + key);
									}
									
									tellServices( userMask(userID) + " JOIN :" + tmpChan + key);			
			
								} else {
									tellUser(userID, raws.r403( users[userID].getNick(), tmpChan ));
								}
					}
				}



		  } else {
			joinedTooMany = true;
			tellUser(userID, raws.r405(users[userID].getNick(), chans));
		  }
		  tmpChan = chans;
		 }//end of while loop for multi-join

		} else {
		  tellUser(userID, "Format is JOIN #channel <key>");
		}



		} else {
			tellUserMustRegister(userID);
		}



	} else if (command.equals("PART")) {
		if (users[userID].isRegistered()) {

			if (st.countTokens() >= 1) {

				String tmpChan = st.nextToken();
				
				String partMessage = "";
				if (st.countTokens() >= 1) {
					partMessage = cmd.substring( command.length() + tmpChan.length() + 2 );
					if (!partMessage.startsWith(":")) { partMessage = ":" + partMessage; }
					partMessage = " " + partMessage;
				}
				
				//for partall - Dana
				if (tmpChan.indexOf(",") != -1) {
					String tmp = tmpChan + ",";
					int numRequests = 0; //set a high limit - David.
					while ((tmp.indexOf(",") != -1) && (numRequests < maxUCJ)) {
						handleCommand("PART " + tmp.substring(0, tmp.indexOf(",")) + partMessage, userID);
						tmp = tmp.substring(tmp.indexOf(",")+1);
					}
				}



				//Get the channel's ID.
				//IF the user is on the channel... remove the user
				//notify all users in the channel of his departure.

				int chanID = is_channel(tmpChan);
				if (chanID != -1) {
					if (isMember(userID, chanID)) {
						//System.out.println("Sent part msg to " + users[userID].getNick());
						users[userID].decNumChansJoined();

						//if the channel is now empty, destroy it!
						if (channels[chanID].membercount() == 1) {
							String tmpOJ = channels[chanID].onpart();
							
							channels[chanID].cleanChannel();
							chanCount--;
							//System.out.println("Channel closed(p) -> " + tmpChan);
	
							tellUser(userID, ":" + users[userID].getNick() + "!" + users[userID].getIdent() + "@" + users[userID].getHostname() + " PART " + tmpChan + partMessage);
							tellServices(":" + users[userID].getNick() + "!" + users[userID].getIdent() + "@" + users[userID].getHostname() + " PART " + tmpChan + partMessage);

							if (tmpOJ != "")
								tellUser(userID, ":" + tmpChan + " NOTICE " + tmpChan + " :" + tmpOJ);

						} else {

							tellUsersInChan(chanID, ":" + users[userID].getNick() + "!" + users[userID].getIdent() + "@" + users[userID].getHostname() + " PART " + tmpChan + partMessage);
							tellServices(":" + users[userID].getNick() + "!" + users[userID].getIdent() + "@" + users[userID].getHostname() + " PART " + tmpChan + partMessage);
							channels[chanID].remove_user(userID);
							String tmpOJ = channels[chanID].onpart();
							if (tmpOJ != "")
								tellUser(userID, ":" + tmpChan + " NOTICE " + tmpChan + " :" + tmpOJ);
						}


					//notify the hub
					if (hubID != -1)
						tellUser(hubID, O_CHANPART + " " + userMask(userID) + " " + tmpChan + partMessage);
					




					} else {
						tellUser(userID, raws.r442(users[userID].getNick(), tmpChan));
					}

				}



			} else {
				tellUser(userID, "Format is PART #channel");
			}

		} else {
			tellUserMustRegister(userID);
		}



	} else if (command.equals("NAMES")) {
		if (users[userID].isRegistered()) {
		  if (st.countTokens() == 1) {
			String tmpChan = st.nextToken();
			
			if ((hubID != -1) && (userID != hubID)) {
				tellUser(hubID, O_REQUEST_NAMES + " " + userID + " " + users[userID].getNick() + " " + tmpChan + " " + users[userID].ircx );
			} else {
				int chanID = is_channel(tmpChan);
				if (chanID != -1) {
					sendNamesList(userID, chanID);
				} else {
					tellUser(userID, raws.r403(users[userID].getNick(), tmpChan));
				}
			}
		  }
		}

	
	} else if (command.equals("STATS")) {
		if (users[userID].isRegistered()) {
			if (st.countTokens() == 1) {
				String option = st.nextToken();
			/*
			   list prohibited nicknames: /stats q
			   lists server bans and global server bans on this server: /stats k
			   lists Operator Definition entries: /stats o
			   lists the length of time the server has been running: /stats u
			   lists client definition entries: /stats i
			   lists available commands: /stats m
			   lists client and connection classes, and their ping times: /stats y
			   lists leaves and hub servers: /stats h
			   lists servers that are defined, but are not currently connected: /stats x
			   lists Connection statistics: /stats w				
			*/

				//else if (options.equals("q")) {
				//	tellUser(userID, ":" + serverName + " 222 " + users[userID].getNick() + " qnick :reason");
				//}
				//if (option.equals("u")) {
				//	tellUser(userID, ":" + serverName + " 242 " + users[userID].getNick() + " :Server Up num days, time");
				//	tellUser(userID, ":" + serverName + " 250 " + users[userID].getNick() + " :Highest connection count: total (num clients)");
				//} 

				
				//else {
					option = "*";
				//}
				
				tellUser(userID, ":" + serverName + " 219 " + users[userID].getNick() + " " + option + " :End of /STATS report");
				
			}
		}
	



	} else if (command.equals("UPTIME")) {
	    if (users[userID].isRegistered()) {
			long ut = (long)((System.currentTimeMillis() - sst)/1000); //time server has been up (sec).
	
			long days = (ut/86400); //gives the total number of days
			long hrs = (ut%86400); //give the remainder of hours
			long hours = (hrs/3600); // gives the total number of hours
			long min = (hrs%3600); // gives remainder of minutes and sec
			long mins = (min/60); // gives the minutes
			long sec = (min%60); // gives the remaining seconds
	
			noticeUser(userID, "Server has been up: " + days + " days " + hours + " hrs. " + mins + " mins. " + sec + " sec.");

			if (hubID != -1) {
				tellUser( hubID, O_REQUEST_HUB_UPTIME + " " + userID + " " + users[userID].getNick() );
				ut = (long)((System.currentTimeMillis() - hubConnectedAt)/1000); //time hub has been connected
				days = (ut/86400); //gives the total number of days
				hrs = (ut%86400); //give the remainder of hours
				hours = (hrs/3600); // gives the total number of hours
				min = (hrs%3600); // gives remainder of minutes and sec
				mins = (min/60); // gives the minutes
				sec = (min%60); // gives the remaining seconds
		
				noticeUser(userID, "Connected to hub for: " + days + " days " + hours + " hrs. " + mins + " mins. " + sec + " sec.");
			}






	    }












	} else if (command.equals("INFO")) {
	    if (users[userID].isRegistered()) {
		tellUserNumeric(userID, 371, "Tes - Java IRCX server - v" + version);
		tellUserNumeric(userID, 371, "Written by David Forrest (david@tesx.org - http://www.tesx.org)");
		tellUserNumeric(userID, 374, "End of /INFO list.");

	    }


	} else if (command.equals("ADMIN")) {
		if (users[userID].isRegistered()) {
			tellUser(userID, raws.r256(users[userID].getNick(), adminMe)); 
			tellUserNumeric(userID, 257, adminLoc1);
			tellUserNumeric(userID, 258, adminLoc2);
			tellUserNumeric(userID, 259, adminEmail);			
		}






	} else if (command.equals("WHISPER")) {
		//Syntax: WHISPER <channel> <nick-list> :<message>
		if (users[userID].isRegistered()) {
			if (users[userID].ircx) {
				if (st.countTokens() >= 3) {
					String chan = st.nextToken();
					String nickList = st.nextToken();
					String message = cmd.substring( command.length() + chan.length() + nickList.length() + 3 );
					if (message.charAt(0) == ':') { message = message.substring(1); }
					
					int cID = is_channel( chan );
					if (cID != -1) {
						int ustat = channels[cID].getMemberStatusByID( userID );
						
						if (ustat > -1) {
							if (!channels[cID].ismode("w") || (ustat >= 2)) {
								StringTokenizer ns = new StringTokenizer( nickList, "," );
								String cNick = "";
								int uid = -1;
								while (ns.hasMoreTokens()) {
									cNick = ns.nextToken();
									uid = userIDfromNick( cNick );
									if (uid != -1) {
										if (isMember(uid, cID)) {
											whisperUser( uid, cID, nickList, userMask(userID), message );
										} else {
											tellUser(userID, ":" + serverName + " 441 " + users[userID].getNick() + " " + users[uid].getNick() + " :They aren't on that channel");
										}
									} else {
										if ((hubID != -1) && (userID != hubID)) {
											tellUser(hubID, O_REQUEST_WHISPER + " " + userID + " " + users[userID].getNick() + " " + channels[cID].getName() + " " + cNick + " " + nickList + " " + message);
										} else {
											tellUser(userID, ":" + serverName + " 401 " + users[userID].getNick() + " " + cNick + " :No such nick/channel");
										}
									}							
								}
							} else {
								//can't use whisper in this channel
								tellUser(userID, ":" + serverName + " 923 " + users[userID].getNick() + " " + channels[cID].getName() + " :Does not permit whispers");
							}
						} else {
							tellUser(userID, ":" + serverName + " 442 " + users[userID].getNick() + " " + channels[cID].getName() + " :You're not on that channel");
						}
						
					} else {
						tellUser(userID, ":" + serverName + " 403 " + users[userID].getNick() + " " + chan + " :No such channel");
					}
					
					
				} else {
					tellUser(userID, ":" + serverName + " 461 " + users[userID].getNick() + " WHISPER :Not enough parameters");
				}
			} else {
				tellUser(userID, ":" + serverName + " 421 " + users[userID].getNick() + " " + command + " :Unknown command");
			}
		}
		
		



	} else if (command.equals("NOTICE")) {
		if (users[userID].isRegistered()) {
		  if (st.countTokens() >= 2) {
			String whereTo = st.nextToken();

			/*Construct the full message to be sent. Can be calculated from string lengths.
			  Full command	- cmd.lenght()
			  NOTICE	- 7 letters (including spaces)
			  #channel	- whereTo.length() +1
			  :		- 1 letter
			*/


			users[userID].setIdle( System.currentTimeMillis() );

			int tmpLen = whereTo.length() + 9;
			String fullMessage = cmd.substring(tmpLen);

			//bundled notice - Dana
			if (whereTo.indexOf(",") != -1) {
				String tmp = whereTo + ",";
				while (tmp.indexOf(",") != -1) {
					handleCommand("NOTICE " + tmp.substring(0, tmp.indexOf(",")) + " :" + fullMessage, userID);
					tmp = tmp.substring(tmp.indexOf(",")+1);
				}
			} else {


			//2 possibilities.


			//1. It's a privmsg to a channel
			if (whereTo.charAt(0) == '#') {
				int chanID = is_channel(whereTo);
				if (chanID != -1) {
					if (isMember(userID, chanID)) {
					  //allow user to send msg
					  tellUsersInChanButOne(chanID, userID, ":" + users[userID].getNick() + "!" + users[userID].getIdent() + "@" + users[userID].getHostname() + " NOTICE " + whereTo + " :" + fullMessage);
					  if ((hubID != -1) && (userID != hubID))
					  	tellUser(hubID, O_NCE + " " + userMask(userID) + " " + whereTo + " " + fullMessage);

					} else {
					  //check if channel is +n ?? TBD !!!

					}



				} else {
					if ((hubID != -1) && (userID != hubID)) {
						tellUser(hubID, O_NC + " " + userMask(userID) + " " + whereTo + " " + fullMessage);
					} else {
							tellUser(userID, ":" + serverName + " 403 " + users[userID].getNick() + " " + whereTo + " :No such channel");
					}
				}


			} else {
			//2. It's a privmsg to a user
				int tmpUID = userIDfromNick(whereTo);
				if (tmpUID != -1) {
				 //user exists on server, allow sending. - no need to tell hub
				 if (users[tmpUID].hasSilenceMatch( userMaskNC(userID) ) == false) {
					 tellUser(tmpUID, ":" + users[userID].getNick() + "!" + users[userID].getIdent() + "@" + users[userID].getHostname() + " NOTICE " + whereTo + " :" + fullMessage);
				 }

				} else {
					if ((hubID != -1) && (hubID != userID))
						tellUser(hubID, "NU: " + userMask(userID) + " " + whereTo + " " + fullMessage );
					else
						tellUser(userID, ":" + serverName + " 401 " + users[userID].getNick() + " " + whereTo + " :No such nick/channel");
				}



			}
	
			}
		  }
		  
		} else {
			tellUserMustRegister(userID);
		}




	} else if (command.equals("TOPIC")) {
	//:irc.splog.net 332 bob #splog :It's The End Of The World As We Know It!
	//:irc.splog.net 333 bob #splog UmBonGo 998096773

		int chanID;
		String strChan;
		String strTopic;

		if (users[userID].isRegistered()) {
			if (st.countTokens() == 1) {
			  strChan = st.nextToken();
			  chanID = is_channel(strChan);
			  if (chanID != -1) {
				strTopic = channels[chanID].getTopic();
				if (!strTopic.equals("")) {
					tellUser(userID, ":" + serverName + " 332 " + users[userID].getNick() + " " + strChan + " :" + channels[chanID].getTopic());
					tellUser(userID, ":" + serverName + " 333 " + users[userID].getNick() + " " + strChan + " " + channels[chanID].whoSetTopic() + " " + channels[chanID].topicTimeStamp());

				} else {
					tellUser(userID, ":" + serverName + " 331 " + users[userID].getNick() + " " + strChan + " :No topic is set.");
				}
			  } else {
			    if ((hubID != -1) && (userID != hubID))
			    	tellUser(hubID, O_FINDTOPIC + " " + userID + " " + users[userID].getNick() + " " + strChan);
			    else
					tellUser(userID, ":" + serverName + " 403 " + users[userID].getNick() + " " + strChan + " :No such channel");
			  }


			} else if (st.countTokens() >= 2) {
				//set the topic
				//check user is on channel
				//check user is priveledged
				strChan = st.nextToken();
				chanID = is_channel(strChan);
				int topicLen = 8 + strChan.length();
				strTopic = cmd.substring(topicLen);
				if (strTopic.length() > maxTopicLen) { strTopic = strTopic.substring(0, maxTopicLen); } //chop long topics.

				if ((chanID != -1) && (isMember(userID, chanID) || (users[userID].isSysop() == 5))) {
				  //check if +t (only ops set topic)
					String tmpStat1 = "";
					if (users[userID].isSysop() == 5) { tmpStat1 = "."; }
					else { tmpStat1 = channels[chanID].user_status(userID); }
					
					boolean userIsPriv = true;
										
				 	 if (channels[chanID].ismode("t")) {
						userIsPriv = (tmpStat1.equals(".") || tmpStat1.equals("@"));
					  }
					

					if (userIsPriv) {
						channels[chanID].setTopic(strTopic);
						channels[chanID].setwhoSetTopic(users[userID].getNick());
						channels[chanID].settopicTimeStamp( (long)(System.currentTimeMillis() / 1000)  );
						tellUsersInChan(chanID, userMask(userID) + " TOPIC " + strChan + " :" + strTopic);
						tellServices( userMask(userID) + " TOPIC " + strChan + " :" + strTopic);
												
						if ((hubID != -1) && (userID != hubID)) {
							tellUser(hubID, O_TOPICCHANGE + " " + userMask(userID) + " " + strChan + " " + channels[chanID].topicTimeStamp() + " :" + strTopic);
						}
					} else {
						//tell only ops set topics
						tellUser(userID, ":" + serverName + " 482 " + users[userID].getNick() + " " + strChan + " :You're not channel operator");
					}

				} else {
				  if ((users[userID].isSysop() == 5) && (hubID != -1)) {
					tellUser(hubID, O_TOPICCHANGE + " " + userMask(userID) + " " + strChan + " " + ((long)(System.currentTimeMillis() / 1000)) + " :" + strTopic);
				  } else {
					  //tell user theyre not on that channel
				  }
				}


			}





		}



	} else if (command.equals("KICK")) {

		if (st.countTokens() >= 2) {
		  //check both users are on the channel
		  String strChan = st.nextToken();
		  String tmpKickWho = st.nextToken();
		  String kickReason = users[userID].getNick();

		  if (st.countTokens() >= 1) {
			kickReason = cmd.substring(8 + strChan.length() + tmpKickWho.length());
		  }

		    //bundled kick - Dana
			if (tmpKickWho.indexOf(",") != -1) {
				String tmp = tmpKickWho + ",";
				while (tmp.indexOf(",") != -1) {
					handleCommand("KICK " + strChan + " " + tmp.substring(0, tmp.indexOf(",")) + " :" + kickReason, userID);
					tmp = tmp.substring(tmp.indexOf(",")+1);
				}
			} else {
		  



		  int chanID = is_channel(strChan);
		  if (chanID != -1) {
		  	strChan = channels[chanID].getName();
		  }
		  
		  int kID = userIDfromNick(tmpKickWho);

			//hub is kicking users due to channel collision
		  /*
		  if ((userID == hubID) &&  (chanID != -1) && (kID != -1) && isMember(kID, chanID)) {
			users[kID].decNumChansJoined();
			String kickString = userMask(userID) + " KICK " + channels[chanID].getName() + " " + users[kID].getNick() + " :Channel collsion";
			
			
				if (channels[chanID].membercount() == 1) {
					channels[chanID].cleanChannel();
					tellUser(kID, kickString);
					tellServices( kickString );
				} else {
					channels[chanID].remove_userAtPos(channels[chanID].userArrayPos(kID));
					tellUsersInChan(chanID, kickString);
					tellUser(kID, kickString);
					tellServices( kickString );
				}


		  } else 
		  */
		  if ( (chanID != -1) && (kID != -1) && isMember(userID, chanID) && isMember(kID, chanID) ) {
			//make sure the user has high enough op status to kick the other user
			int stat1 = channels[chanID].getMemberStatusByID(userID);
			int stat2 = channels[chanID].getMemberStatusByID(kID);

			if  ((users[userID].isSysop() >= users[kID].isSysop()) && (stat1 >= stat2) && (stat1 != 0)) {
				//allow the kick

				users[kID].decNumChansJoined();
				//case where user kicking themselves out of a channel that only they are in..
				String kickString = userMask(userID) + " KICK " + channels[chanID].getName() + " " + users[kID].getNick() + " :" + kickReason;

				if (channels[chanID].membercount() == 1) {
					channels[chanID].cleanChannel();
					if (hubID == -1) {
						tellUser(kID, kickString);
						tellServices( kickString );
					}
				} else {
					channels[chanID].remove_user(kID);
					if (hubID == -1) {
						tellUsersInChan(chanID, kickString);
						tellUser(kID, kickString);
						tellServices( kickString );
					}
				}
				
				int pos1 = channels[chanID].userArrayPos(userID); //the position may now have changed, since kID was removed!
				if ((hubID != -1) && (userID != hubID) && ((pos1 != -1) || (userID == kID))) {
			  		tellUser(hubID, O_KICK + " 0 " + stat1 + " " + userMask(userID) + " " + strChan + " " + tmpKickWho + " :" + kickReason );
			  		tellServices( kickString );
				}
				
			} else {
				tellUser(userID, ":" + serverName + " 482 " + users[userID].getNick() + " " + strChan + " :You're not channel operator");
			}
			
			
				
			
		  } else if ( (hubID != -1) && (userID != hubID) && (chanID != -1) && isMember(userID, chanID) ) {
		  	//kicking a user who is on the channel, but might be on another server.
		  	//int pos = channels[chanID].userArrayPos(userID);
		  	int uStatus = channels[chanID].getMemberStatusByID(userID);
		  	
		  	if ( (users[userID].isSysop() > 0) || (uStatus > 1) ) {
		  		tellUser(hubID, O_KICK + " 1 " + uStatus + " " + userMask(userID) + " " + channels[chanID].getName() + " " + tmpKickWho + " :" + kickReason );
		  	} else {
				tellUser(userID, ":" + serverName + " 482 " + users[userID].getNick() + " " + strChan + " :You're not channel operator");
		  	}
		  	
		  	
		  }
		  
		  
		  
		  
		  
		  }
		}


	//TBD - Modify invite for server linking
	} else if (command.equals("INVITE")) {
		if (st.countTokens() >= 2) {
		  //check inviter is on the channel
		  //check invitee is not on channel

		  //INVITE <nick> <#channel>
		  String strInvitee = st.nextToken();
		  String strChan = st.nextToken();

		  int chanID = is_channel(strChan);
		  int iID = userIDfromNick(strInvitee);

		  if ( (chanID != -1) && (iID != -1) && isMember(userID, chanID) && !isMember(iID, chanID) ) {
			if ((users[userID].isSysop() > 0) || (channels[chanID].getMemberStatusByID(userID) >= 2)) {
				//allow the invite
				tellUser(iID, userMask(userID) + " INVITE " + users[iID].getNick() + " :" + strChan);
				tellUser(userID, ":" + serverName + " 341 " + users[userID].getNick() + " " + users[iID].getNick() + " " + strChan); 
				channels[chanID].setInvite(users[iID].getNick());


			} else {
				//dont allow the invite
				tellUser(userID, ":" + serverName + " 482 " + users[userID].getNick() + " " + strChan + " :You're not channel operator");
			}
		  }


		}





	} else if (command.equals("MODE")) {
		//1. User mode (v,o,q)
		//2. Channel mode (n,t)
		int chanID;
		boolean notw = false;
		String strChan;
		  if (st.countTokens() == 3) {
			strChan = st.nextToken();
			chanID = is_channel(strChan);
			if (chanID != -1) {
			// to get irssi to work, a hack must be done ::(
			String tmode = st.nextToken();
			//if (tmode.length() > 1) {
			//	String tgt = tmode.substring(2, tmode.length() + 1);
			//	tmode = tmode.substring(0, 2);
			//} else {
				String tgt = st.nextToken();
			//}
			if (tmode.indexOf("w") > -1 && isMember(userID, chanID)) {
				boolean addm = (tmode.indexOf("+") > -1);
				boolean isowner = (channels[chanID].getMemberStatusByID(userID) == 3);
				boolean isop = (channels[chanID].getMemberStatusByID(userID) == 2);
				if (isowner == true || isop == true){
					String stat = tgt.substring(0,3);
					String usr = tgt.substring(3, tgt.length());
					if (stat.indexOf("o") > -1 && (isop == true || isowner == true)){
					 if(addm == true){
						int success = channels[chanID].addAccess( "HOST", usr, 0, false, "" );
					 } else {
						int success = channels[chanID].removeAccess( "HOST", usr, false );
					 }
					} else if(stat.indexOf("o") > -1 && isowner == true) {
					 if(addm == true){
						int success = channels[chanID].addAccess( "OWNER", usr, 0, false, "");
					 } else {
						int success = channels[chanID].removeAccess( "OWNER", usr, false);
					 }
					} else if(stat.indexOf("v") > -1 && (isowner == true || isop == true)) {
					 if(addm == true){
						int success = channels[chanID].addAccess( "VOICE", usr, 0, false, "");
					 } else {
						int success = channels[chanID].removeAccess ( "VOICE", usr, false);
					 }
					} 
				tellUsersInChan(chanID, userMask(userID) + " MODE " + channels[chanID].getName() + " " + tmode + " " + tgt);
			}
				} else {
				notw = true;
				}
			}
			
		  }
		
		  if (notw == false) {
		  if (st.countTokens() == 1) {
			strChan = st.nextToken();
			chanID = is_channel(strChan);
			
			if (strChan.equals("ISIRCX") && !users[userID].isRegistered()) {
				handleCommand( "IRCX", userID );
			} else if (chanID != -1) {
				tellUser(userID, ":" + serverName + " 324 " + users[userID].getNick() + " " + strChan + " " + channels[chanID].modeString());
			} else {
				if (strChan.equalsIgnoreCase(users[userID].getNick())) {
					tellUser(userID, ":" + serverName + " 221 " + strChan + " +" + users[userID].getModes());
				} else {
					//perhaps the channel exists on another server ? Ask the hub..
					if ((hubID != -1) && (userID != hubID)) {
						tellUser(hubID, O_REQUEST_CHANMODES + " " + users[userID].getNick() + " " + userID + " " + strChan );
					} else {					
						tellUser(userID, ":" + serverName + " 403 " + users[userID].getNick() + " " + strChan + " :No such channel");
					}
				}
			}

		  } else if (st.countTokens() == 2) {
			//mode #chan +n-t+m
			strChan = st.nextToken();
			String strMode = st.nextToken();
			chanID = is_channel(strChan);

			boolean services = (users[userID].isSysop() == 5);

			if (chanID != -1) {
			  if (isMember(userID, chanID) || services) {
			  
				String tmpStat1 = channels[chanID].user_status(userID);
				boolean userIsPriv = (tmpStat1.equals(".") || tmpStat1.equals("@") || services);
				
				if (userIsPriv) {

				  String plusModes = ""; //modes to be +'d to channel
				  String minusModes = ""; //modes to be -'d from channel
				  char nextMode = '+';
				  char nextChar;
				  String strModeCpy = strMode;
				   while (strModeCpy.length() >0) {

					nextChar = strModeCpy.charAt(0);
					if (  (nextChar == '+') || (nextChar == '-')  ) {
						nextMode = nextChar;
					} else if ( nextMode == '+' ) {
						plusModes = plusModes + nextChar;
					} else if ( nextMode == '-' ) {
						minusModes = minusModes + nextChar;
					}
					strModeCpy = strModeCpy.substring(1);

				   }


				   String correctModes = "";
				   String illegalModes = ""; //any unknown mode chars

				   if (plusModes.length() > 0)
					correctModes = "+";


				   while (plusModes.length() >0) {

					nextMode = plusModes.charAt(0);
					plusModes = plusModes.substring(1);

					if ((nextMode == 't') ||
						(nextMode == 'n') ||
						(nextMode == 'u') ||
						(nextMode == 'm') ||
						(nextMode == 'i') ||
						(nextMode == 's') ||
						(nextMode == 'c') ||
						(nextMode == 'S')
						
						) {
						if (!channels[chanID].ismode("" + nextMode + "")) {
							channels[chanID].setmode("" + nextMode + "");
							if (correctModes.indexOf("" + nextMode + "") == -1) {
								correctModes = correctModes + nextMode;
							}
						}
					
						
						
					} else if ( (nextMode == 'O') && (!channels[chanID].ismode("O")) && (users[userID].isSysop() > 0) ) {
						channels[chanID].setmode("O");
						correctModes = correctModes + "O";
					} else if ( (nextMode == 'b') ) {
						
						//display channel banlist...
						int counter = 0;
						String tmpAccess = channels[chanID].getAccessAt(0);
						String preBit = ":" + serverName + " 367 " + users[userID].getNick() + " " + channels[chanID].getName() + " ";
						while (tmpAccess.length() > 0) {
							if (tmpAccess.startsWith("DENY")) {
								tmpAccess = tmpAccess.substring(5);
								tmpAccess = tmpAccess.substring(0, tmpAccess.indexOf(" ")-2);
								tellUser(userID, preBit + tmpAccess);
							}							
							counter++;
							tmpAccess = channels[chanID].getAccessAt( counter );
						}
						
						/*
						int numBans = channels[chanID].numBans();

						for (int i=0; i< numBans; i++) {
							tellUser(userID, ":" + serverName + " 367 " + users[userID].getNick() + " " + channels[chanID].getName() + " " + channels[chanID].getBan(i));
						}
						*/
						
						tellUser(userID, ":" + serverName + " 368 " + users[userID].getNick() + " " + channels[chanID].getName() + " :End of Channel Ban List");

					} else {
						illegalModes = illegalModes + nextMode;
					}


				   }
				   if (correctModes.equals("+"))
				       correctModes = "";



				   if (minusModes.length() > 0)
					correctModes = correctModes + "-";


				   while (minusModes.length() >0) {

					nextMode = minusModes.charAt(0);
					minusModes = minusModes.substring(1);

					if ((nextMode == 't') ||
						(nextMode == 'n') ||
						(nextMode == 'u') ||
						(nextMode == 'm') ||
						(nextMode == 'i') ||
						(nextMode == 's') ||
						(nextMode == 'c') ||
						(nextMode == 'S')
						 ) {
							
						if (channels[chanID].ismode("" + nextMode + "")) {
							channels[chanID].unsetmode("" + nextMode + "");
							if (correctModes.indexOf("" + nextMode + "") == -1) {
								correctModes = correctModes + nextMode;
							}
						}
					} else if ( (nextMode == 'O') && (channels[chanID].ismode("O")) && (users[userID].isSysop() > 0) ) {
						channels[chanID].unsetmode("O");
						correctModes = correctModes + "O";

					} else if (nextMode == 'l') {
						correctModes = correctModes + nextMode;
						channels[chanID].setlimit(0);
					} else {
						illegalModes = illegalModes + nextMode;
					}


				   }

				   if (correctModes.endsWith("-"))
					correctModes = correctModes.substring(0, correctModes.length()-1);



				   if (correctModes.length() > 0) {
					tellUsersInChan(chanID, userMask(userID) + " MODE " + channels[chanID].getName() + " " + correctModes);
					tellServices( userMask(userID) + " MODE " + channels[chanID].getName() + " " + correctModes );
					
					if ((hubID != -1) && (userID != hubID)) {
						tellUser(hubID, O_CHANMODE + " " + userMask(userID) + " " + channels[chanID].getName()  + " " + channels[chanID].modeStringLX() + " " + channels[chanID].getLimit() + " " + correctModes);
					}
					
				   }
				   if (illegalModes.length() > 0) {
					tellUser(userID, ":" + serverName + " 472 " + users[userID].getNick() + " " + illegalModes + " :are unknown mode chars to me");
				   }







				} else {
					tellUser(userID, ":" + serverName + " 482 " + users[userID].getNick() + " " + strChan + " :You're not channel operator");
				}

			  } else {
				//your not on that channel
				tellUser(userID, ":" + serverName + " 482 " + users[userID].getNick() + " " + strChan + " :You're not channel operator");
			  }
			} else {

			  if (strChan.equalsIgnoreCase(users[userID].getNick())) {
				String tmpModes = users[userID].getModes();
				String tmpNewModes = "";
				String uNick = users[userID].getNick();
				boolean plusmode = true;
				boolean allowCurrMode = false;
				//if (strMode.charAt(0) == '+') { plusmode = true; }
				//else if (strMode.charAt(0) == '-') { plusmode = false; }

				char currMode = strMode.charAt(0);
				
			
				if ( (currMode == '+') || (currMode == '-') ) {
					while (strMode.length() >= 1) {
						currMode = strMode.charAt(0);
						
						if (currMode == '+') { plusmode = true; }
						else if (currMode == '-') { plusmode = false; }
						else {
							if (plusmode) {
								if (tmpModes.indexOf("" + currMode) == -1) {
								  
								  if ( (currMode == 'i') || (currMode == 'w') || (currMode == 's') ) {
									  allowCurrMode = true;
								  }
								  
								  if ( (users[userID].isSysop() > 0) &&
								  	( (currMode == 'F') || (currMode == 'W') || (currMode == 'H') ||
								  	  (currMode == 'c') || (currMode == 'f') || (currMode == 'h') ||
								  	  (currMode == 'k') || (currMode == 'K')
								  	   ) ) {
								  	    //See documentation.txt for the meaning of the above modes.
								  		allowCurrMode = true;
								  }
								  						  
								  if (allowCurrMode) {
									  if ((tmpNewModes.indexOf("+") == -1) || (tmpNewModes.lastIndexOf('+') < tmpNewModes.lastIndexOf('-'))) {
									  	tmpNewModes = tmpNewModes + "+";
									  }
									  tmpNewModes = tmpNewModes + currMode;
									  users[userID].addMode(currMode);
								  }
									
								}
							} else {
								if (users[userID].remMode("" + currMode)) {
								  if ((tmpNewModes.indexOf("-") == -1) || (tmpNewModes.lastIndexOf('-') < tmpNewModes.lastIndexOf('+'))) {
								  	tmpNewModes = tmpNewModes + "-";
								  }
								  tmpNewModes = tmpNewModes + currMode;								  
								}
							}
						}
						strMode = strMode.substring(1); //move on to the next mode
					}
					
					if (!tmpNewModes.equals("")) {
						tellUser(userID, ":" + uNick + " MODE " + uNick + " " + tmpNewModes);
						tellServices( ":" + uNick + " MODE " + uNick + " " + tmpNewModes );
					}
					
										
				} else {
					tellUser(userID, ":" + serverName + " 501 " + uNick + " :Unknown MODE flag");
				}
				
				
				
				/*
				if ((strMode.length() > 1) && (strMode.charAt(0) == '+')) {
					if (tmpModes.indexOf("" + strMode.charAt(1)) == -1) {
					  users[userID].addMode(strMode.charAt(1));
					  tellUser(userID, ":" + strChan + " MODE " + strChan + " " + strMode);
					}
				} else if ((strMode.length() > 1) && (strMode.charAt(0) == '-')) {
					if (users[userID].remMode("" + strMode.charAt(1) + ""))
						tellUser(userID, ":" + strChan + " MODE " + strChan + " -" + strMode.charAt(1));
				}
				*/
				

			  } else {
				tellUser(userID, ":" + serverName + " 403 " + users[userID].getNick() + " " + strChan + " :No such channel");
			  }
			}



		  } else if (st.countTokens() > 3) {
			//#chan +ooo <nick> <nick2> <nick3>
			strChan = st.nextToken();
			String strModes = st.nextToken();
			char strPM = strModes.charAt(0);
			strModes = strModes.substring(1);
			boolean illegalString = false;
			String whoTo = "";
			chanID = is_channel(strChan);
			int pos1 = -1;
			int pos2 = -1;
			int stat1 = -1;
			int stat2 = -1;
			int whoToID = -1;
			boolean servicebot = (users[userID].isSysop() == 5);
			if (strModes.length() > 35)
				illegalString = true;





		    if ( (chanID != -1) && (isMember(userID, chanID) || servicebot) ) {
			pos1 = channels[chanID].userArrayPos(userID);
			if (servicebot) {
				//chanserv is always allowed to op/deop people.
				stat1 = 3;
			} else if (pos1 != -1) {
				stat1 = channels[chanID].getMemberStatus(pos1);
			}
				

			if ((strPM != '+') && (strPM != '-'))
				illegalString = true;

			String sayString = "";
			String sayStringIRCD = "";
			
			while (!illegalString && (strModes.length() > 0)) {
			    if ((strModes.charAt(0) == '+') || (strModes.charAt(0) == '-')) {
				strPM = strModes.charAt(0);

			    } else if (strModes.charAt(0) == 'o') {
				if (st.countTokens() > 0) {
				  whoTo = st.nextToken();
				  if (strPM == '+') {
				    //deal with +o user
				    whoToID = userIDfromNick(whoTo);
				    if ((whoToID != -1) && isMember(whoToID, chanID) ) {
					//pos2 = channels[chanID].userArrayPos(whoToID);
					stat2 = channels[chanID].getMemberStatusByID(whoToID);
					
					
					if ( users[userID].isSysop() < users[whoToID].isSysop() )
						stat1 = 0; //effectively make them a regular when trying to change sysop's modes
					
					  if ( (stat1 > 1) && (stat1 >= stat2) ) {
						//change the users status in the channel
						channels[chanID].setMemberStatusByID(whoToID,2);

						if (stat2 == 3) {
						  sayString = sayString + userMask(userID) + " MODE " + channels[chanID].getName() + " -q " + whoTo + "\r\n";
						  if ((hubID != -1) && (userID != hubID)) {
						   tellUser(hubID, O_SUMOC + " ." + userMask(userID) + " " + channels[chanID].getName() + " -q " + whoTo );
						  }
						}
						
						sayString = sayString + userMask(userID) + " MODE " + channels[chanID].getName() + " +o " + whoTo + "\r\n";
						sayStringIRCD+= userMask(userID) + " MODE " + channels[chanID].getName() + " +o " + whoTo + "\r\n";
						if ((hubID != -1) && (userID != hubID)) {
						 tellUser(hubID, O_SUMOC + " ." + userMask(userID) + " " + channels[chanID].getName() + " +o " + whoTo );
						}

					  }
				    } else if ((whoToID == -1) && (hubID != -1) && (userID != hubID)) {
				    	//deal with off server mode
				    	String uStat = "";
				    	if (servicebot) { uStat = "."; }
				    	else { uStat = channels[chanID].getMemberStatusStr(pos1); }
				    	tellUser(hubID, O_RUMOC + " " + uStat + userMask(userID) + " " + channels[chanID].getName() + " +o " + whoTo );
				    }



				  } else {

				    //deal with -o user
				    whoToID = userIDfromNick(whoTo);
				    if ((whoToID != -1) && isMember(whoToID, chanID) ) {
					  //pos2 = channels[chanID].userArrayPos(whoToID);
					  stat2 = channels[chanID].getMemberStatusByID(whoToID);
					  
					if ( users[userID].isSysop() < users[whoToID].isSysop() )
						stat1 = 0; //effectively make them a regular when trying to change sysop's modes
					  
					  if ( (stat1 > 1) && (stat1 >= stat2) ) {
						//change the users status in the channel
						channels[chanID].setMemberStatusByID(whoToID,0);
						if (stat2 == 3) {
						  sayString = sayString + userMask(userID) + " MODE " + channels[chanID].getName() + " -q " + whoTo + "\r\n";
						  if ((hubID != -1) && (userID != hubID)) {
						   tellUser(hubID, O_SUMOC + " ." + userMask(userID) + " " + channels[chanID].getName() + " -q " + whoTo );
						  }
						}

						sayString = sayString + userMask(userID) + " MODE " + channels[chanID].getName() + " -o " + whoTo + "\r\n";
						sayStringIRCD+= userMask(userID) + " MODE " + channels[chanID].getName() + " -o " + whoTo + "\r\n";
						if ((hubID != -1) && (userID != hubID)) {
						 tellUser(hubID, O_SUMOC + " ." + userMask(userID) + " " + channels[chanID].getName() + " -o " + whoTo );
						}

					  }
				    } else if ((whoToID == -1) && (hubID != -1) && (userID != hubID)) {
				    	//deal with off server mode
				    	String uStat = "";
				    	if (servicebot) { uStat = "."; }
				    	else { uStat = channels[chanID].getMemberStatusStr(pos1); }
				    	tellUser(hubID, O_RUMOC + " " + uStat + userMask(userID) + " " + channels[chanID].getName() + " -o " + whoTo );
				    }


				  }
				}
			    } else if (strModes.charAt(0) == 'q') {
				if (st.countTokens() > 0) {
				  whoTo = st.nextToken();
				  if (strPM == '+') {
				    //deal with +q user

				    whoToID = userIDfromNick(whoTo);
				    if ((whoToID != -1) && isMember(whoToID, chanID) ) {
					//pos2 = channels[chanID].userArrayPos(whoToID);
					stat2 = channels[chanID].getMemberStatusByID(whoToID);
					
					if ( users[userID].isSysop() < users[whoToID].isSysop() )
						stat1 = 0; //effectively make them a regular when trying to change sysop's modes
						
						
					if ( (stat1 == 3) ) {
					 //change the users status in the channel
					 channels[chanID].setMemberStatusByID(whoToID,3);
					 if (stat2 == 2) {
					  sayString = sayString + userMask(userID) + " MODE " + channels[chanID].getName() + " -o " + whoTo + "\r\n";
					  if ((hubID != -1) && (userID != hubID)) {
						tellUser(hubID, O_SUMOC + " ." + userMask(userID) + " " + channels[chanID].getName() + " -o " + whoTo );
					  }
					 } else {
						  sayStringIRCD+= userMask(userID) + " MODE " + channels[chanID].getName() + " +o " + whoTo + "\r\n";					 	
					 }
					 
					  sayString = sayString + userMask(userID) + " MODE " + channels[chanID].getName() + " +q " + whoTo + "\r\n";
					  if ((hubID != -1) && (userID != hubID)) {
					    tellUser(hubID, O_SUMOC + " ." + userMask(userID) + " " + channels[chanID].getName() + " +q " + whoTo );
					  }


					}
				    } else if ((whoToID == -1) && (hubID != -1) && (userID != hubID)) {
				    	//deal with off server mode
				    	String uStat = "";
				    	if (servicebot) { uStat = "."; }
				    	else { uStat = channels[chanID].getMemberStatusStr(pos1); }
				    	tellUser(hubID, O_RUMOC + " " + uStat + userMask(userID) + " " + channels[chanID].getName() + " +q " + whoTo );
				    }



				  } else {
				    //deal with -q user

				    whoToID = userIDfromNick(whoTo);
				    if ((whoToID != -1) && isMember(whoToID, chanID) ) {
					//pos2 = channels[chanID].userArrayPos(whoToID);
					stat2 = channels[chanID].getMemberStatusByID(whoToID);
					
					if ( users[userID].isSysop() < users[whoToID].isSysop() )
						stat1 = 0; //effectively make them a regular when trying to change sysop's modes
					
					if ( (stat1 == 3) && (stat2 == 3) ) {
					 channels[chanID].setMemberStatusByID(whoToID,0);
					 sayString = sayString + userMask(userID) + " MODE " + channels[chanID].getName() + " -q " + whoTo + "\r\n";
					 sayStringIRCD+= userMask(userID) + " MODE " + channels[chanID].getName() + " -o " + whoTo + "\r\n";
				     if ((hubID != -1) && (userID != hubID)) {
					  tellUser(hubID, O_SUMOC + " ." + userMask(userID) + " " + channels[chanID].getName() + " -q " + whoTo );
					 }

				 	}

				    } else if ((whoToID == -1) && (hubID != -1) && (userID != hubID)) {
				    	//deal with off server mode
				    	String uStat = "";
				    	if (servicebot) { uStat = "."; }
				    	else { uStat = channels[chanID].getMemberStatusStr(pos1); }
				    	tellUser(hubID, O_RUMOC + " " + uStat + userMask(userID) + " " + channels[chanID].getName() + " -q " + whoTo );
				    }
				  }
				}



			    } else if (strModes.charAt(0) == 'v') {



				if (st.countTokens() > 0) {
				  whoTo = st.nextToken();
				  if (strPM == '+') {
				    //deal with +v user

				    whoToID = userIDfromNick(whoTo);
				    if ((whoToID != -1) && isMember(whoToID, chanID) ) {
					//pos2 = channels[chanID].userArrayPos(whoToID);
					stat2 = channels[chanID].getMemberStatusByID(whoToID);
					
					if ( users[userID].isSysop() < users[whoToID].isSysop() )
						stat1 = 0; //effectively make them a regular when trying to change sysop's modes
					
					  if ( (stat1 >= 2) && (stat2 == 0) ) {
						channels[chanID].setMemberStatusByID(whoToID,1);
						sayString = sayString + userMask(userID) + " MODE " + channels[chanID].getName() + " +v " + whoTo + "\r\n";
						sayStringIRCD+= userMask(userID) + " MODE " + channels[chanID].getName() + " +v " + whoTo + "\r\n";
					    if ((hubID != -1) && (userID != hubID)) {
					    	tellUser(hubID, O_SUMOC + " @" + userMask(userID) + " " + channels[chanID].getName() + " +v " + whoTo );
					    }

					  }
				    } else if ((whoToID == -1) && (hubID != -1) && (userID != hubID)) {
				    	//deal with off server mode
				    	String uStat = "";
				    	if (servicebot) { uStat = "."; }
				    	else { uStat = channels[chanID].getMemberStatusStr(pos1); }
				    	tellUser(hubID, O_RUMOC + " " + uStat + userMask(userID) + " " + channels[chanID].getName() + " +v " + whoTo );
				    }



				  } else {
				    //deal with -v user

				    whoToID = userIDfromNick(whoTo);
				    if ((whoToID != -1) && isMember(whoToID, chanID) ) {
					//pos2 = channels[chanID].userArrayPos(whoToID);
					stat2 = channels[chanID].getMemberStatusByID(whoToID);
					
					if ( users[userID].isSysop() < users[whoToID].isSysop() )
						stat1 = 0; //effectively make them a regular when trying to change sysop's modes
					
					  if( (stat1 >= 2) ) {
						channels[chanID].setMemberStatusByID(whoToID,0);
						sayString = sayString + userMask(userID) + " MODE " + channels[chanID].getName() + " -v " + whoTo + "\r\n";
						sayStringIRCD+= userMask(userID) + " MODE " + channels[chanID].getName() + " -v " + whoTo + "\r\n";
					    if ((hubID != -1) && (userID != hubID)) {
					    	tellUser(hubID, O_SUMOC + " @" + userMask(userID) + " " + channels[chanID].getName() + " -v " + whoTo );
					    }
					  }

				    } else if ((whoToID == -1) && (hubID != -1) && (userID != hubID)) {
				    	//deal with off server mode
				    	String uStat = "";
				    	if (servicebot) { uStat = "."; }
				    	else { uStat = channels[chanID].getMemberStatusStr(pos1); }
				    	tellUser(hubID, O_RUMOC + " " + uStat + userMask(userID) + " " + channels[chanID].getName() + " -v " + whoTo );
				    }

				  }
				}












			    } else
				illegalString = true;

			    strModes = strModes.substring(1);
			}

			if (!sayString.equals("")) {
				sayString = sayString.substring(0, sayString.length()-2); //remove the carriage-returns prior to sending.
				if (!sayStringIRCD.equals("")) {
					sayStringIRCD = sayStringIRCD.substring(0, sayStringIRCD.length()-2); //remove the carriage-returns prior to sending.
				}
				tellUsersInChan(chanID, sayStringIRCD, sayString);
				tellServices( sayString ); //remove the carriage-returns prior to sending.
			}



		    } else {
 				if ( servicebot && (hubID != -1) ) {
 				
					if ((strPM != '+') && (strPM != '-'))
						illegalString = true;
		
					while (!illegalString && (strModes.length() > 0)) {
					    if ((strModes.charAt(0) == '+') || (strModes.charAt(0) == '-')) {
							strPM = strModes.charAt(0);
		
					    } else if (st.countTokens() > 0) {
							  whoTo = st.nextToken();
							  if (!whoTo.equals(":")) {
							      tellUser(hubID, O_RUMOC + " ." + userMask(userID) + " " + strChan + " " + strPM + strModes.charAt(0) + " " + whoTo );
						      }
						} else {
							illegalString = true;
						}
		
					    strModes = strModes.substring(1);
					}				  	
				  	
				  	
 				} else {
 				
					tellUser(userID, ":" + serverName + " 403 " + users[userID].getNick() + " " + strChan + " :No such channel");
				 }
		    }





		  } else if (st.countTokens() == 3) {
			//#tes +o David
			strChan = st.nextToken();
			String strMode = st.nextToken();
			String strWhoTo = st.nextToken();
			chanID = is_channel(strChan);


			//correct multi-modes on sigle-user, e.g. /mode +oooooo nickname
			if (strMode.length() > 2) {
				strMode = strMode.substring(0, 2);
			}
			  
			  

			if (chanID != -1) {

			  //make sure both users are on the channel
			  int whoToID = userIDfromNick(strWhoTo);

			  if (strMode.equals("+l") && isMember(userID, chanID)) {
				String tmpStat1 = channels[chanID].user_status(userID);
				boolean userIsPriv = (tmpStat1.equals(".") || tmpStat1.equals("@"));
				if (userIsPriv) {
					int lval = 0;
				   try {
				     lval = Integer.parseInt(strWhoTo);
				   } catch (NumberFormatException e) {}

				   if (lval > 0) {
					tellUsersInChan(chanID, userMask(userID) + " MODE " + channels[chanID].getName() + " +l " + strWhoTo);
					tellServices( userMask(userID) + " MODE " + channels[chanID].getName() + " +l " + strWhoTo );
					
					if ((hubID != -1) && (userID != hubID)) { tellUser(hubID, O_CHANMODE + " " + userMask(userID) + " " + channels[chanID].getName() + " " + channels[chanID].modeStringLX() + " " + lval + " +l " + strWhoTo ); }
					
					channels[chanID].setlimit(lval);
				   }
				}

			  //+h allows users to op themselves with the key, without cycling the channel
			  } else if (strMode.equals("+h") && isMember(userID, chanID)) {
			  	
			  	if (channels[chanID].ownerkey().equals( strWhoTo )) {
			  		channels[chanID].setMemberStatusByID(userID, 3);
			  		handleCommand("MODE " + channels[chanID].getName() + " +q " + users[userID].getNick(), userID);
			  		
			  	} else if (channels[chanID].hostkey().equals( strWhoTo )) {
			  		channels[chanID].setMemberStatusByID(userID, 2);
			  		handleCommand("MODE " + channels[chanID].getName() + " +o " + users[userID].getNick(), userID);
			  	}


			  
			  } else if ((whoToID != -1) && isMember(userID, chanID) && isMember(whoToID, chanID)) {
				//check if the user is allowed to make the mode change
				  //int pos1 = channels[chanID].userArrayPos(userID);
				  //int pos2 = channels[chanID].userArrayPos(whoToID);
				  int stat1 = channels[chanID].getMemberStatusByID(userID);
				  int stat2 = channels[chanID].getMemberStatusByID(whoToID);

				if ( users[userID].isSysop() < users[whoToID].isSysop() ) {
					stat1 = 0; //effectively make them a regular when trying to change sysop's modes
				}


					

				if (strMode.equals("+o")) {


				  // to +o someone you must have greater or equal status to them
				  // you must have greater or equal status to an operator
				  if ( (stat1 > 1) && (stat1 >= stat2) ) {
					//change the users status in the channel
					channels[chanID].setMemberStatusByID(whoToID,2);
					if (stat2 == 3) {
					  tellUsersInChan(chanID, userMask(userID) + " MODE " + channels[chanID].getName() + " -o " + strWhoTo,
					  						userMask(userID) + " MODE " + channels[chanID].getName() + " -q " + strWhoTo);
					  tellServices( userMask(userID) + " MODE " + channels[chanID].getName() + " -q " + strWhoTo );
					  
						  if ((hubID != -1) && (userID != hubID)) {
						   tellUser(hubID, O_SUMOC + " ." + userMask(userID) + " " + channels[chanID].getName() + " -q " + strWhoTo );
						  }
					}
					tellUsersInChan(chanID, userMask(userID) + " MODE " + channels[chanID].getName() + " +o " + strWhoTo);
					tellServices( userMask(userID) + " MODE " + channels[chanID].getName() + " +o " + strWhoTo );
					
						  if ((hubID != -1) && (userID != hubID)) {
						   tellUser(hubID, O_SUMOC + " ." + userMask(userID) + " " + channels[chanID].getName() + " +o " + strWhoTo );
						  }
				  } else {
					tellUser(userID, ":" + serverName + " 482 " + users[userID].getNick() + " " + channels[chanID].getName() + " :You're not channel operator");
				  }



				} else if (strMode.equals("-o")) {


				  // to -o someone you must have greater or equal status to them
				  // you must have greater or equal status to an operator

				  if ( (stat1 > 1) && (stat1 >= stat2) ) {
					//change the users status in the channel
					channels[chanID].setMemberStatusByID(whoToID,0);

					if (stat2 == 3) {
					  tellUsersInChan(chanID, userMask(userID) + " MODE " + channels[chanID].getName() + " -o " + strWhoTo,
					  						userMask(userID) + " MODE " + channels[chanID].getName() + " -q " + strWhoTo);
					  tellServices( userMask(userID) + " MODE " + channels[chanID].getName() + " -q " + strWhoTo );
					  
						  if ((hubID != -1) && (userID != hubID)) {
						   tellUser(hubID, O_SUMOC + " ." + userMask(userID) + " " + channels[chanID].getName() + " -q " + strWhoTo );
						  }
					} else {
					  tellUsersInChan(chanID, userMask(userID) + " MODE " + channels[chanID].getName() + " -o " + strWhoTo);
					  tellServices( userMask(userID) + " MODE " + channels[chanID].getName() + " -o " + strWhoTo );
					  
						  if ((hubID != -1) && (userID != hubID)) {
						   tellUser(hubID, O_SUMOC + " ." + userMask(userID) + " " + channels[chanID].getName() + " -o " + strWhoTo );
						  }
					}
				  } else {
					tellUser(userID, ":" + serverName + " 482 " + users[userID].getNick() + " " + channels[chanID].getName() + " :You're not channel operator");
				  }






				} else if (strMode.equals("+q")) {
				  // to +q someone you must be an owner
				  // they must be less than an owner

				  if ( (stat1 == 3) ) {
					//change the users status in the channel
					channels[chanID].setMemberStatusByID(whoToID,3);
					if (stat2 == 2) {
					  tellUsersInChan(chanID, userMask(userID) + " MODE " + channels[chanID].getName() + " -o " + strWhoTo);
					  tellServices( userMask(userID) + " MODE " + channels[chanID].getName() + " -o " + strWhoTo );
					  
						  if ((hubID != -1) && (userID != hubID)) {
						   tellUser(hubID, O_SUMOC + " ." + userMask(userID) + " " + channels[chanID].getName() + " -o " + strWhoTo );
						  }
					}

					  tellUsersInChan(chanID, userMask(userID) + " MODE " + channels[chanID].getName() + " +o " + strWhoTo,
					  						userMask(userID) + " MODE " + channels[chanID].getName() + " +q " + strWhoTo);
					  tellServices( userMask(userID) + " MODE " + channels[chanID].getName() + " +q " + strWhoTo);
					  
						  if ((hubID != -1) && (userID != hubID)) {
						   tellUser(hubID, O_SUMOC + " ." + userMask(userID) + " " + channels[chanID].getName() + " +q " + strWhoTo );
						  }
				  } else {
					tellUser(userID, ":" + serverName + " 482 " + users[userID].getNick() + " " + channels[chanID].getName() + " :You're not channel operator");
				  }









				} else if (strMode.equals("-q")) {
				  // to -q someone you must be an owner
				  // they must also be an owner

				  if ( (stat1 == 3) && (stat2 == 3) ) {

					channels[chanID].setMemberStatusByID(whoToID,0);
					tellUsersInChan(chanID, userMask(userID) + " MODE " + channels[chanID].getName() + " -o " + strWhoTo,
									userMask(userID) + " MODE " + channels[chanID].getName() + " -q " + strWhoTo);
					tellServices( userMask(userID) + " MODE " + channels[chanID].getName() + " -q " + strWhoTo);
						  if ((hubID != -1) && (userID != hubID)) {
						   tellUser(hubID, O_SUMOC + " ." + userMask(userID) + " " + channels[chanID].getName() + " -q " + strWhoTo );
						  }
				  } else {
					tellUser(userID, ":" + serverName + " 482 " + users[userID].getNick() + " " + channels[chanID].getName() + " :You're not channel operator");
				  }




				} else if (strMode.equals("+v")) {
				  // to +v someone you must be an op or owner
				  // they must be a regular
				  if ( (stat1 >= 2) && (stat2 == 0) ) {
					tellUsersInChan(chanID, userMask(userID) + " MODE " + channels[chanID].getName() + " +v " + strWhoTo);
					tellServices(  userMask(userID) + " MODE " + channels[chanID].getName() + " +v " + strWhoTo);
					
						  if ((hubID != -1) && (userID != hubID)) {
						   tellUser(hubID, O_SUMOC + " ." + userMask(userID) + " " + channels[chanID].getName() + " +v " + strWhoTo );
						  }
					channels[chanID].setMemberStatusByID(whoToID,1);

				  }


				} else if (strMode.equals("-v")) {
				  // to -v someone you must be an op or owner
				  // they must be +v
				  if ( (stat1 >= 2) ) {
					channels[chanID].setMemberStatusByID(whoToID,0);
					tellUsersInChan(chanID, userMask(userID) + " MODE " + channels[chanID].getName() + " -v " + strWhoTo);
					tellServices( userMask(userID) + " MODE " + channels[chanID].getName() + " -v " + strWhoTo);
					
						  if ((hubID != -1) && (userID != hubID)) {
						   tellUser(hubID, O_SUMOC + " ." + userMask(userID) + " " + channels[chanID].getName() + " -v " + strWhoTo );
						  }
				  }




				}







			  } else {
				//the channel exists, so we have a user setting MODE #chan +* something

			   if (isMember(userID, chanID)) {
				  //int pos = channels[chanID].userArrayPos(userID);
				  int stat = channels[chanID].getMemberStatusByID(userID);


				  //to ban someone you must be an op or an owner
				if (strMode.equals("+b") && (stat >= 2)) {
					strWhoTo = formatBanMask( strWhoTo );
					//boolean canSetBan = channels[chanID].setBan( strWhoTo );
					int success = channels[chanID]. addAccess( "DENY", strWhoTo, 0, (stat == 3), "" );

					if (success == 0) {
						//:bob!~djf@62.31.114.XXX MODE #test +b *!*@*
						tellUsersInChan(chanID, userMask(userID) + " MODE " + channels[chanID].getName() + " +b " + strWhoTo);
						tellServices( userMask(userID) + " MODE " + channels[chanID].getName() + " +b " + strWhoTo);
						if ((hubID != -1) && (userID != -1)) { tellUser(hubID, O_SBOC + " " + userMask(userID) + " " + channels[chanID].getName() + " " + strWhoTo + " " + stat); }
					} else if (success == 2) {
						tellUser(userID, ":" + serverName + " 478 " + users[userID].getNick() + " " + channels[chanID].getName() + " " + strWhoTo + " :Channel ban list is full");
					}

				} else if (strMode.equals("-b") && (stat >=2)) {
					//boolean canUnset = channels[chanID].unsetBan( strWhoTo );
					int success = channels[chanID].removeAccess( "DENY", strWhoTo, (stat == 3) );

					if (success == 0) {
						tellUsersInChan(chanID, userMask(userID) + " MODE " + channels[chanID].getName() + " -b " + strWhoTo);
						tellServices( userMask(userID) + " MODE " + channels[chanID].getName() + " -b " + strWhoTo);
						
						if ((hubID != -1) && (userID != -1)) { tellUser(hubID, O_USBOC + " " + userMask(userID) + " " + channels[chanID].getName() + " " + strWhoTo); }
					} else {
						//tellUser(userID, "no such ban");
					}

				} else if (strMode.equals("+w") && (stat >=2)) {
					noticeUser(userID, "hi");
				} else {
					//off server mode +o/q/v whoTo  ?
					if ((hubID != -1) && (userID != -1)) {
					    String uStat = channels[chanID].getMemberStatusStr(channels[chanID].userArrayPos(userID));
					    tellUser(hubID, O_RUMOC + " " + uStat + userMask(userID) + " " + channels[chanID].getName() + " " + strMode + " " + strWhoTo );
				    }
					
				}

			   } else {
				tellUser(userID, ":" + serverName + " 482 " + users[userID].getNick() + " " + channels[chanID].getName() + " :You're not channel operator");
			   }

			  }







			}

		  }
		}



	} else if (command.equals("ACCESS")) {
		//ACCESS #channel ADD/DELETE/LIST   OWNER/HOST/DENY/VOICE    *!*@*
		if (users[userID].isRegistered()) {
		  String strChan = "";
		  String strADL = "";
		  String strLevel = "";
		  String strMask = "";
		  int cID = -1;
			int numLevs = 0;

		  if (st.countTokens() == 1) {
			//access #channel
			//if user is on channel, show them the access list

			strChan = st.nextToken();
			cID = is_channel(strChan);

			if ( (cID != -1) && (isMember(userID, cID)) ) {
				tellUser(userID, ":" + serverName + " 803 " + users[userID].getNick() + " " + strChan + " :Start of access entries");
				//:server 804 bob #test OWNER/HOST/DENY/VOICE *!*@*$* 0 * :
				String tmpAccess = channels[cID].getAccessAt( 0 );
				int acounter = 0;
				String strPrebit = ":" + serverName + " 804 " + users[userID].getNick() + " " + channels[cID].getName() + " ";
				while (tmpAccess.length() > 0) {
					tellUser(userID, strPrebit + tmpAccess);					
					acounter++;
					tmpAccess = channels[cID].getAccessAt( acounter );
				}

				tellUser(userID, ":" + serverName + " 805 " + users[userID].getNick() + " " + strChan + " :End of access entries");
			} else if (cID == -1) {
				tellUser(userID, ":" + serverName + " 924 " + users[userID].getNick() + " " + strChan + " :No such object found");
			} else {
				tellUser(userID, ":" + serverName + " 913 " + users[userID].getNick() + " " + strChan + " :No access");
			}

		  } else if ( (st.countTokens() == 2) || (st.countTokens() == 3) ) {
			strChan = st.nextToken();
			strADL =  st.nextToken();

			if (st.countTokens() == 1)
				strLevel = st.nextToken();


			if (strADL.equalsIgnoreCase("LIST")) {
				handleCommand( "ACCESS " + strChan, userID );

			} else if (strADL.equalsIgnoreCase("CLEAR")) {
			 	cID = is_channel(strChan);
			 	if ( (cID != -1) && (isMember(userID, cID)) && (channels[cID].getMemberStatusByID(userID) >= 2) ) {
					boolean isowner = (channels[cID].getMemberStatusByID(userID) == 3); //channels[cID].userArrayPos(userID));
					
					if (strLevel.equals("")) {
						channels[cID].clearAccess( isowner, "" );
						if (isowner) {
						    if ((hubID != -1) && (hubID != userID)) { tellUser(hubID, O_CLEARACCESS + " OBH " + strChan + " " + isowner); }
						    
						    tellUser(userID, ":" + serverName + " 820 " + users[userID].getNick() + " " + strChan + " * :Clear");
						} else {
							if ((hubID != -1) && (hubID != userID)) { tellUser(hubID, O_CLEARACCESS + " BH " + strChan); }
						    tellUser(userID, ":" + serverName + " 922 " + users[userID].getNick() + " " + strChan + " :Some entries not cleared due to security");
						}


					} else if (strLevel.equalsIgnoreCase("OWNER")) {
						if (isowner) {
							channels[cID].clearAccess( isowner, "OWNER" );
						    if ((hubID != -1) && (hubID != userID)) { tellUser(hubID, O_CLEARACCESS + " O " + strChan + " " + isowner); }
						    
							tellUser(userID, ":" + serverName + " 820 " + users[userID].getNick() + " " + strChan + " OWNER :Clear");
						}
					} else if (strLevel.equalsIgnoreCase("HOST")) {
							channels[cID].clearAccess( isowner, "HOST" );
						    if ((hubID != -1) && (hubID != userID)) { tellUser(hubID, O_CLEARACCESS + " H " + strChan + " " + isowner); }
						    
							tellUser(userID, ":" + serverName + " 820 " + users[userID].getNick() + " " + strChan + " HOST :Clear");
					} else if (strLevel.equalsIgnoreCase("DENY")) {
							channels[cID].clearAccess( isowner, "DENY" );
						    if ((hubID != -1) && (hubID != userID)) { tellUser(hubID, O_CLEARACCESS + " B " + strChan + " " + isowner); }
						    
							tellUser(userID, ":" + serverName + " 820 " + users[userID].getNick() + " " + strChan + " DENY :Clear");
					} else if (strLevel.equalsIgnoreCase("VOICE")) {
							channels[cID].clearAccess( isowner, "VOICE" );
						    if ((hubID != -1) && (hubID != userID)) { tellUser(hubID, O_CLEARACCESS + " V " + strChan + " " + isowner); }
						    
							tellUser(userID, ":" + serverName + " 820 " + users[userID].getNick() + " " + strChan + " VOICE :Clear");
					}
					
				} else if (cID == -1) {
					tellUser(userID, ":" + serverName + " 924 " + users[userID].getNick() + " " + strChan + " :No such object found");
				} else {
					tellUser(userID, ":" + serverName + " 913 " + users[userID].getNick() + " " + strChan + " :No access");
				}





			} else {
				tellUser(userID, ":" + serverName + " 903 " + users[userID].getNick() + " " + strChan + " :Bad level");
			}

		  } else if (st.countTokens() > 3) {
			//the hard part!
			strChan   = st.nextToken();
			strADL    = st.nextToken();
			strLevel  = st.nextToken();
			strMask   = st.nextToken();
			cID = is_channel(strChan);

			String tout = "";
			int timeout = 0;
			String reason = "";
			if (st.hasMoreTokens()) {
				tout = st.nextToken();
				try { timeout = Integer.parseInt(tout); } catch (NumberFormatException e) {}
				
				if (st.hasMoreTokens()) {
					reason = cmd.substring(command.length() + strChan.length() + strADL.length() + strLevel.length() + strMask.length() + tout.length() + 6);
					if ((reason.length() > 0) && (reason.charAt(0) == ':')) { reason = reason.substring(1); }
				}
			}


			//ACCESS #channel ADD/DELETE/LIST   OWNER/HOST/DENY/VOICE    *!*@*

			if ( (cID != -1) && (isMember(userID, cID)) ) {

				if (strADL.equalsIgnoreCase("LIST")) {
					//show list
					handleCommand( "ACCESS " + strChan, userID );
					
					
				} else if (strADL.equalsIgnoreCase("ADD")) {


					//check if the user is an op!!
					//int tmpID = channels[cID].userArrayPos(userID);
					int ms = channels[cID].getMemberStatusByID(userID);
					if (ms >= 2) {

					  if (strLevel.equalsIgnoreCase("OWNER")) {
						//user must be owner to add owner access..
						if (ms == 3) {

							//ADD THE ACCESS !!!
							//boolean canSetOwn = channels[cID].setOwn( strMask );
							int success = channels[cID].addAccess( strLevel, strMask, timeout, true, reason );
							
							if (success == 0) {
								
							 if ((hubID != -1) && (userID != hubID)) { tellUser(hubID, O_ADDACCESS + " O " + strChan + " " + strMask + " " + ms + " " + timeout + " " + reason); }
							 //modify the next line to reply correctly
							 tellUser(userID, ":" + serverName + " 801 " + users[userID].getNick() + " " + strChan + " OWNER " + strMask + "$* " + timeout + " * :" + reason);
							} else if (success == 1) {
								tellUser(userID, ":" + serverName + " 914 " + users[userID].getNick() + " :Duplicate access entry");
							} else {
								tellUser(userID, ":" + serverName + " 916 " + users[userID].getNick() + " :Too many access entries");
							}

						} else {
					    		tellUser(userID, ":" + serverName + " 913 " + users[userID].getNick() + " " + strChan + " :No access");
						}



					  } else if (strLevel.equalsIgnoreCase("HOST")) {
							//boolean canSetHost = channels[cID].setHost( strMask );
							int success = channels[cID].addAccess( strLevel, strMask, timeout, (ms == 3), reason );
							if (success == 0) {
							 if ((hubID != -1) && (userID != hubID)) { tellUser(hubID, O_ADDACCESS + " H " + strChan + " " + strMask + " " + ms + " " + timeout + " " + reason); }
							 tellUser(userID, ":" + serverName + " 801 " + users[userID].getNick() + " " + strChan + " HOST " + strMask + "$* " + timeout + " * :" + reason);

							} else if (success == 1) {
								tellUser(userID, ":" + serverName + " 914 " + users[userID].getNick() + " :Duplicate access entry");
							} else {
								tellUser(userID, ":" + serverName + " 916 " + users[userID].getNick() + " :Too many access entries");
							}


					  } else if (strLevel.equalsIgnoreCase("VOICE")) {
							int success = channels[cID].addAccess( strLevel, strMask, timeout, (ms == 3), reason );
							if (success == 0) {
							 if ((hubID != -1) && (userID != hubID)) { tellUser(hubID, O_ADDACCESS + " V " + strChan + " " + strMask + " " + ms + " " + timeout + " " + reason); }
							 tellUser(userID, ":" + serverName + " 801 " + users[userID].getNick() + " " + strChan + " VOICE " + strMask + "$* " + timeout + " * :" + reason);

							} else if (success == 1) {
								tellUser(userID, ":" + serverName + " 914 " + users[userID].getNick() + " :Duplicate access entry");
							} else {
								tellUser(userID, ":" + serverName + " 916 " + users[userID].getNick() + " :Too many access entries");
							}
					  } else if (strLevel.equalsIgnoreCase("DENY")) {
							//boolean canSetBan = channels[cID].setBan( strMask );
							int success = channels[cID].addAccess( strLevel, strMask, timeout, (ms == 3), reason );
							if (success == 0) {
							 if ((hubID != -1) && (userID != hubID)) { tellUser(hubID, O_ADDACCESS + " B " + strChan + " " + strMask + " " + ms + " " + timeout + " " + reason); }
							 tellUser(userID, ":" + serverName + " 801 " + users[userID].getNick() + " " + strChan + " DENY " + strMask + "$* " + timeout + " * :" + reason);

							} else if (success == 1) {
								tellUser(userID, ":" + serverName + " 914 " + users[userID].getNick() + " :Duplicate access entry");
							} else {
								tellUser(userID, ":" + serverName + " 916 " + users[userID].getNick() + " :Too many access entries");
							}
					  } else {
					    tellUser(userID, ":" + serverName + " 903 " + users[userID].getNick() + " " + strChan + " :Bad level");
					  }
					} else {
					    tellUser(userID, ":" + serverName + " 913 " + users[userID].getNick() + " " + strChan + " :No access");
					}



				} else if (strADL.equalsIgnoreCase("DELETE")) {
					//check if the user is an op!!
					//int tmpID = channels[cID].userArrayPos(userID);
					int ms = channels[cID].getMemberStatusByID(userID);
					if (ms >= 2) {

					  if (strLevel.equalsIgnoreCase("OWNER")) {
						//user must be owner to delete owner access..
						if (ms == 3) {

							//boolean canUnSetOwn = channels[cID].unsetOwn( strMask );
							int success = channels[cID].removeAccess( strLevel, strMask, true );
							
							if (success == 0) {
							 if ((hubID != -1) && (userID != hubID)) { tellUser(hubID, O_REMACCESS + " O " + strChan + " " + strMask + " " + ms); }
							 tellUser(userID, ":" + serverName + " 802 " + users[userID].getNick() + " " + strChan + " OWNER " + strMask + "$* 0 * :");
							} else if (success == 1) {
							    tellUser(userID, ":" + serverName + " 913 " + users[userID].getNick() + " " + strChan + " :No access");
							} else {
								tellUser(userID, ":" + serverName + " 915 " + users[userID].getNick() + " :Unknown access entry");
							}

						} else {
					    		tellUser(userID, ":" + serverName + " 913 " + users[userID].getNick() + " " + strChan + " :No access");
						}



					  } else if (strLevel.equalsIgnoreCase("HOST")) {
							//boolean canUnSetHost = channels[cID].unsetHost( strMask );
							int success = channels[cID].removeAccess( strLevel, strMask, (ms == 3) );
							if (success == 0) {
							 if ((hubID != -1) && (userID != hubID)) { tellUser(hubID, O_REMACCESS + " H " + strChan + " " + strMask + " " + ms); }
							 tellUser(userID, ":" + serverName + " 802 " + users[userID].getNick() + " " + strChan + " HOST " + strMask + "$* 0 * :");
							} else if (success == 1) {
							    tellUser(userID, ":" + serverName + " 913 " + users[userID].getNick() + " " + strChan + " :No access");
							} else {
								tellUser(userID, ":" + serverName + " 915 " + users[userID].getNick() + " :Unknown access entry");
							}


					  } else if (strLevel.equalsIgnoreCase("VOICE")) {
							int success = channels[cID].removeAccess( strLevel, strMask, (ms == 3) );
							if (success == 0) {
							 if ((hubID != -1) && (userID != hubID)) { tellUser(hubID, O_REMACCESS + " V " + strChan + " " + strMask + " " + ms); }
							 tellUser(userID, ":" + serverName + " 802 " + users[userID].getNick() + " " + strChan + " VOICE " + strMask + "$* 0 * :");
							} else if (success == 1) {
							    tellUser(userID, ":" + serverName + " 913 " + users[userID].getNick() + " " + strChan + " :No access");
							} else {
								tellUser(userID, ":" + serverName + " 915 " + users[userID].getNick() + " :Unknown access entry");
							}
					  } else if (strLevel.equalsIgnoreCase("DENY")) {
							int success = channels[cID].removeAccess( strLevel, strMask, (ms == 3) );
							if (success == 0) {
							 if ((hubID != -1) && (userID != hubID)) { tellUser(hubID, O_REMACCESS + " B " + strChan + " " + strMask + " " + ms); }
							 tellUser(userID, ":" + serverName + " 802 " + users[userID].getNick() + " " + strChan + " DENY " + strMask + "$* 0 * :");
							} else if (success == 1) {
							    tellUser(userID, ":" + serverName + " 913 " + users[userID].getNick() + " " + strChan + " :No access");
							} else {
								tellUser(userID, ":" + serverName + " 915 " + users[userID].getNick() + " :Unknown access entry");
							}
					  } else {
					    tellUser(userID, ":" + serverName + " 903 " + users[userID].getNick() + " " + strChan + " :Bad level");
					  }
					} else {
					    tellUser(userID, ":" + serverName + " 913 " + users[userID].getNick() + " " + strChan + " :No access");
					}
				} else {
					tellUser(userID, ":" + serverName + " 903 " + users[userID].getNick() + " " + strChan + " :Bad level");
				}


			} else if (cID == -1) {
				tellUser(userID, ":" + serverName + " 924 " + users[userID].getNick() + " " + strChan + " :No such object found");
			} else {
				tellUser(userID, ":" + serverName + " 913 " + users[userID].getNick() + " " + strChan + " :No access");
			}







		  } else {
			tellUser(userID, ":" + serverName + " 461 " + users[userID].getNick() + " ACCESS :Not enough parameters");
		  }







		}








	} else if (command.equals("PROP")) {
		//prop #channel
		String tmpChan;
		int tmpID;
		String tmpProp;

		if (users[userID].isRegistered()) {
			if (st.countTokens() == 1) {
				//:server 461 bob prop :Not enough parameters
				tellUser(userID, ":" + serverName + " 461 " + users[userID].getNick() + " PROP " + ":Not enough parameters");
			} else if (st.countTokens() == 2) {
			  //prop #channel [property]
			  tmpChan = st.nextToken();
			  tmpID = is_channel(tmpChan);
			  tmpProp = st.nextToken();



			  if (tmpID == -1) {
				tellUser(userID, ":" + serverName + " 924 " + users[userID].getNick() + " " + tmpChan + " :No such object found.");
			  } else {
				String onjoinR = "";
				if (channels[tmpID].onjoin() != "")
					onjoinR = ":" + serverName + " 818 " + users[userID].getNick() + " " + tmpChan + " OnJoin :" + channels[tmpID].onjoin();

				String onpartR = "";
				if (channels[tmpID].onpart() != "")
					onpartR = ":" + serverName + " 818 " + users[userID].getNick() + " " + tmpChan + " OnPart :" + channels[tmpID].onpart();


				String creationR = ":" + serverName + " 818 " + users[userID].getNick() + " " + tmpChan + " Creation :" + channels[tmpID].creation();

				String nameR = ":" + serverName + " 818 " + users[userID].getNick() + " " + tmpChan + " Name :" + channels[tmpID].getName();

				String oidR = ":" + serverName + " 818 " + users[userID].getNick() + " " + tmpChan + " OID :0";

				String endofProps = ":" + serverName + " 819 " + users[userID].getNick() + " " + tmpChan + " :End of properties";


				//ownerkey and hostkey are NOT queryable.

				if (tmpProp.equalsIgnoreCase("ONJOIN")) {
					if (onjoinR != "")
						tellUser(userID, onjoinR);
					tellUser(userID, endofProps);
				} else if (tmpProp.equalsIgnoreCase("ONPART")) {
					if (onpartR != "")
						tellUser(userID, onpartR);
					tellUser(userID, endofProps);

				} else if (tmpProp.equalsIgnoreCase("CREATION")) {
					tellUser(userID, creationR);
					tellUser(userID, endofProps);

				} else if (tmpProp.equalsIgnoreCase("NAME")) {
					tellUser(userID, nameR);
					tellUser(userID, endofProps);

				} else if (tmpProp.equalsIgnoreCase("OID")) {
					tellUser(userID, oidR);
					tellUser(userID, endofProps);


				} else if (tmpProp.equalsIgnoreCase("*")) {
					tellUser(userID, oidR);
					tellUser(userID, nameR);
					tellUser(userID, creationR);

					if (onjoinR != "") {
						tellUser(userID, onjoinR);
					}
					if (onpartR != "") {
						tellUser(userID, onpartR);
					}
					tellUser(userID, endofProps);


				}


			  }

			} else if (st.countTokens() > 2) {
				//user wants to set a prop

			  tmpChan = st.nextToken();
			  tmpID = is_channel(tmpChan);
			  tmpProp = st.nextToken();
			  String tmpValue = cmd.substring(7 + tmpChan.length() + tmpProp.length());
			  if (tmpValue.charAt(0) == ':')
				tmpValue = tmpValue.substring(1);

			  boolean servicebot = (users[userID].isSysop() == 5);
				
			  if (tmpID == -1) {
			    if ((servicebot) && (hubID != -1)) {
			    	//allow service bots to set up channels
					tmpProp = tmpProp.toUpperCase();
					if (tmpProp.equals("ONJOIN") || tmpProp.equals("ONPART") || tmpProp.equals("OWNERKEY") || tmpProp.equals("HOSTKEY")) {
						tellUser( hubID, O_CHANPROP + " " + userMask(userID) + " " + tmpProp + " " + tmpChan + " " + tmpValue );
					}
			    } else {
					tellUser(userID, ":" + serverName + " 924 " + users[userID].getNick() + " " + tmpChan + " :No such object found.");
				}
			  } else {

			    String nopermissions = ":" + serverName + " 908 " + users[userID].getNick() + " :No permissions to perform command";

			   if (isMember(userID, tmpID) || servicebot) {
			   	
			    int tmpStat = 0;
			    if (servicebot) { tmpStat = 3; }
			    else {			tmpStat = channels[tmpID].getMemberStatusByID(userID); }
			    
			    if (tmpStat >= 2) {
				if (tmpProp.equalsIgnoreCase("ONJOIN")) {
				  channels[tmpID].setOnjoin(tmpValue);
				  tellUsersInChan(tmpID, userMask(userID) + " PROP " + tmpChan + " ONJOIN :" + tmpValue);
				  
				  //notify the hub of the change
				  if ( (hubID != -1) && (userID != hubID) ) {
				  	tellUser( hubID, O_CHANPROP + " " + userMask(userID) + " ONJOIN " + tmpChan + " " + tmpValue );
				  }
				  
				} else if (tmpProp.equalsIgnoreCase("ONPART")) {
				  channels[tmpID].setOnpart(tmpValue);
				  tellUsersInChan(tmpID, userMask(userID) + " PROP " + tmpChan + " ONPART :" + tmpValue);

				  //notify the hub of the change
				  if ( (hubID != -1) && (userID != hubID) ) {
				  	tellUser( hubID, O_CHANPROP + " " + userMask(userID) + " ONPART " + tmpChan + " " + tmpValue );
				  }

				} else if (tmpProp.equalsIgnoreCase("OWNERKEY")) {
				  if (tmpStat == 3) {
					  channels[tmpID].setOwnerkey(tmpValue);
					  tellOwnersInChan(tmpID, userMask(userID) + " PROP " + tmpChan + " OWNERKEY :" + tmpValue);
					  
					  //notify the hub of the change
					  if ( (hubID != -1) && (userID != hubID) ) {
					  	tellUser( hubID, O_CHANPROP + " " + userMask(userID) + " OWNERKEY " + tmpChan + " " + tmpValue );
					  }
				  } else {
					  tellUser(userID, nopermissions);
				  }
				} else if (tmpProp.equalsIgnoreCase("HOSTKEY")) {
				  if (tmpStat == 3) {
					  channels[tmpID].setHostkey(tmpValue);
					  tellOpsInChan(tmpID, userMask(userID) + " PROP " + tmpChan + " HOSTKEY :" + tmpValue);
					  
					  //notify the hub of the change
					  if ( (hubID != -1) && (userID != hubID) ) {
					  	tellUser( hubID, O_CHANPROP + " " + userMask(userID) + " HOSTKEY " + tmpChan + " " + tmpValue );
					  }
				  } else {
					  tellUser(userID, nopermissions);
				  }


				//prop topic - Dana
				} else if (tmpProp.equalsIgnoreCase("TOPIC")) {
				  if (tmpStat == 3) {
					channels[tmpID].setTopic(tmpValue);
					channels[tmpID].setwhoSetTopic(users[userID].getNick());
					channels[tmpID].settopicTimeStamp( (long)(System.currentTimeMillis() / 1000)  );
					tellOpsInChan(tmpID, userMask(userID) + " PROP " + tmpChan + " TOPIC :" + tmpValue);
					
					  //notify the hub of the change
					  if ( (hubID != -1) && (userID != hubID) ) {
					  	tellUser( hubID, O_TOPICCHANGE + " " + userMask(userID) + " " + tmpChan + " " + channels[tmpID].topicTimeStamp() + " :" + tmpValue );
					  }
							
				  } else {
					tellUser(userID, nopermissions);
				  }






				} else {
				  tellUser(userID, ":" + serverName + " 905 " + users[userID].getNick() + " " + tmpChan + " :Bad property specified");
				}
			    } else {
					tellUser(userID, nopermissions);
			    }
			   } else {
				tellUser(userID, nopermissions);
			   }







			  }




			}

		}


	} else if (command.equals("SILENCE")) {
		//SILENCE +/-nick!ident@host OR SILENCE nick
		if (users[userID].isRegistered()) {
			String param = users[userID].getNick();
			if (st.countTokens() > 0) { param = st.nextToken(); }
			
				if (param.length() > 1) {
					if (param.charAt(0) == '-') {
						//removal of a mask from the silence list
						String remd = users[userID].remSilence( param.substring(1) );
						if (remd.length() > 0) {
							tellUser(userID, ":" + serverName + " SILENCE -" + remd);
						}
						
					} else if (param.charAt(0) == '+') {
						//addition of a mask to the silence list
						//:<usermask> SILENCE +fff!*@*
						String added = users[userID].addSilence( param.substring(1) );
						tellUser(userID, ":" + serverName + " SILENCE +" + added);
						
					} else {
						int tmpID = userIDfromNick( param );
						if (tmpID != -1) {
							//requesting someone elses silence list
							Vector s = users[tmpID].getSilences();
							for (int i=0; i<s.size(); i++) {
								tellUser(userID, ":" + serverName + " 271 " + users[userID].getNick() + " " + users[tmpID].getNick() + " " + s.elementAt(i));
							}
			
							tellUser(userID, ":" + serverName + " 272 " + users[userID].getNick() + " :End of Silence List");
							//:server 271 <nick> <whoto-nick> <mask>
							//:server 272 <nick> :End of Silence List
							
						} else {
							if ((hubID != -1) && (userID != hubID)) {
								//tell the hub...
								tellUser(hubID, O_REQUEST_SILENCES + " " + userID + " " + users[userID].getNick() + " " + param);
							} else {
								//add +param to silence list ?
								/* handleCommand( "SILENCE +" + param, userID ); */
								
								//:server 401 <nick> <param> :No such nick/channel
								tellUser(userID, ":" + serverName + " 401 " + users[userID].getNick() + " " + param + " :No such nick/channel");
							}
						}
					}
				}

		}







	//	Song / File sharing features
	
	} else if (command.equals("FILESHARE")) {
		if (users[userID].isRegistered()) {
			int tid = userIDfromNick("teshaserv");
			if ((tid != -1) && (users[tid].isSysop() == 5)) {
				if (cmd.length() > command.length()) {
					cmd = "TS " + users[userID].getNick() + " " + users[userID].getSocket().getInetAddress().getHostAddress() + " " + cmd.substring(10);
					tellUser(tid, cmd);
				}
			} else if ((hubID != -1) && (userID != hubID)) {
				//pass fileshare to hub
				if (cmd.length() > command.length()) {
					tellUser(hubID, O_FILESHARE + " " + users[userID].getNick() + " " + users[userID].getSocket().getInetAddress().getHostAddress() + " " + cmd.substring(10));
				}
			} else {
				tellUser(userID, "TESHA Filesharing has been disabled.");
			}
		}
	
	} else if (command.equals("FSR") && (users[userID].isSysop() >= 5)) {
		if (st.hasMoreTokens()) {
			String nick = st.nextToken();
			int uid = userIDfromNick( nick );
			if (uid != -1) {
				tellUser( uid, cmd.substring(5 + users[uid].getNick().length() ) );
			} else if ((hubID != -1) && (userID != hubID)) {
				//user is on another server?
				tellUser(hubID, O_FILESHARE_REPLY + " " + cmd.substring(command.length()+1));
			}
		}


















	// ---------------------	OPER COMMANDS		----------------------
	// ---------------------------------------------------------------------------
	} else if (command.equals("DEBUG")) {
		if (users[userID].isSysop() >= 3) {
			if (st.countTokens() >= 1) {
				String s = st.nextToken().toUpperCase();
				
				if (st.hasMoreTokens()) {
					//user is using a -s or -h flag.
					String onoff = st.nextToken().toUpperCase();
					if (s.equals("-H")) {
						if ((hubID != -1) && (userID != hubID)) {
							if (onoff.equals("ON") || onoff.equals("OFF")) {
								tellUser( hubID, O_DEBUG_SWITCH + " " + onoff );
								noticeUser( userID, "Hub debug -> " + onoff );
							}
						} else {
							noticeUser( userID, "Error: Not connected to hub." );
						}
					} else if (s.equals("-S")) {
						if (onoff.equals("ON")) { debug = true; }
						else				{ debug = false; }						
					}
				} else {				
					if (s.equals("ON")) { debug = true; }
					else				{ debug = false; }
				}
					
			} else {
				noticeUser(userID, "Usage /DEBUG [-h/-s] ON/OFF");
			}
				String deb = "OFF";
				if (debug) { deb="ON"; }
				noticeUser(userID, "Server DEBUG is : " + deb);

		}
	
	
	} else if (command.equals("OPER")) {
		 if (users[userID].isSysop() > 0) {
		 	noticeUser(userID, "--- You are already an oper!");
		 } else {
		  if (st.countTokens() == 2) {
			String tmpNick = st.nextToken();
			String tmpPass = st.nextToken();
			
			String isOper = "";
			int i=0;
			String tmpLevel = "";
			String tmpN, tmpP;
			StringTokenizer oToks;
			while (isOper.equals("") && (i<opers.size())) {
				//L:N:P
				oToks = new StringTokenizer( (String)opers.elementAt(i), ":" );
				if (oToks.countTokens() == 3) {
					tmpLevel = oToks.nextToken();
					tmpN = oToks.nextToken();
					tmpP = oToks.nextToken();
					if (tmpNick.equalsIgnoreCase(tmpN) && tmpPass.equals(tmpP)) {
						isOper = tmpLevel;
					}
				}
				i++;
			}
			
			

			if (!isOper.equals("")) {
				
				if (isOper.equals("O")) {
					users[userID].makeSysop(1);
					if (users[userID].isRegistered()) {
						if ( (hubID != -1) && (userID != hubID) ) {
							tellUser( hubID, O_OPER + " " + users[userID].getNick() + " 1" );
						}
							noticeAll("--- " + users[userID].getNick() + " is now an IRC Operator");
					}
				} else if (isOper.equals("G")) {
					users[userID].makeSysop(2);
					if (users[userID].isRegistered()) {
						if ( (hubID != -1) && (userID != hubID) ) {
							tellUser( hubID, O_OPER + " " + users[userID].getNick() + " 2" );
						}
						noticeAll("--- " + users[userID].getNick() + " is now a Global IRC Operator");
					}
					
				} else if (isOper.equals("N")) {
					users[userID].makeSysop(3);
					if (users[userID].isRegistered()) {
						if ( (hubID != -1) && (userID != hubID) ) {
							tellUser( hubID, O_OPER + " " + users[userID].getNick() + " 3" );
						}
						noticeAll("--- " + users[userID].getNick() + " is now a Network Administrator");
					}
				} else if (isOper.equals("S")) {
					users[userID].makeSysop(5);
					if (users[userID].isRegistered()) {
						if ( (hubID != -1) && (userID != hubID) ) {
							tellUser( hubID, O_OPER + " " + users[userID].getNick() + " 5" );
						}
						noticeOpers("--- " + users[userID].getNick() + " is now a Services Bot");
					}

				} else if (isOper.equals("H")) {
					if (hubID == -1) {
						noticeOpers("--- Connected to Network HUB");
						
						users[userID].makeSysop(10);
						hubID = userID;

					
						hubConnectedAt = System.currentTimeMillis();
					
					
						//change the ping interval of the hub
						pings[userID].setInterval( hubPingInterval );
					
					} else {
						noticeOpers("--- Duplicate attempt from HUB to connect. Refusing...");
					}						
				} else {
					users[userID].makeSysop(1); //default incase of unknown level

				}

				if (users[userID].isRegistered()) {
					tellServices( "OPER: " + users[userID].getNick() );
				}

				operCount++;
				users[userID].setHostname(serverName);

				if (!users[userID].getNick().equals("")) {
					handleCommand( "MODE " + users[userID].getNick() + " +" + defaultSysopModes( users[userID].isSysop() ), userID );
				}
				
				//forceOper - Dana
				if ((forceOper.length() > 0) && (userID != hubID) && (users[userID].isRegistered()) ) {
					int cid = is_channel( forceOper );
					if (!( (cid != -1) && isMember(userID, cid) )) {
						//already on chan, do nothing..
					} else {
						noticeUser(userID, "The server is forcing you to join: " + forceOper);
						handleCommand( "JOIN " + forceOper, userID );
					}
				}


				//noticeAll("--- " + users[userID].getNick() + " is now an IRC Operator");
			} else {
				if (users[userID].isRegistered())
					noticeUser(userID, "--- " + users[userID].getNick() + " :No o-lines for your host");
			}
		  }
		 }



	} else if (command.equals("UNOPER")) {
		if (users[userID].isSysop() > 0) {
			handleCommand( "MODE " + users[userID].getNick() + " -" + defaultSysopModes( users[userID].isSysop() ), userID );
			users[userID].unSysop();
			operCount--;

			setHostMask(userID);

			//forceOper - Dana
			if (forceOper.length() > 0) {
				int cid = is_channel( forceOper );
				if ( (cid != -1) && isMember(userID, cid) ) {
					noticeUser(userID, "The server is forcing you to Part: " + forceOper);
					handleCommand( "PART " + forceOper, userID );
				}
			}

			noticeUser(userID, "You are no longer a system operator");
			
			if ((hubID != -1) && (userID != hubID)) {
				tellUser(hubID, O_UNOPER + " " + users[userID].getNick());
			}
			tellServices( "UNOPER: " + users[userID].getNick() );
		}

	} else if (command.equals("WEBREPORT")) {
		if (users[userID].isSysop() == 3) {
			if (webReport) {
				wc.doUpdateNow();
				noticeUser(userID, "WebReport complete.");
			} else {
				noticeUser(userID, "WebReport has been disabled in the conf!");
			}			
		} else if (users[userID].isSysop() > 0) {
			noticeUser(userID, "Only Network Admin may use /WEBREPORT");
		}



/*
	} else if (command.equals("CS")) {

		
		if ((users[userID].isSysop() == 3) && useChanserv && (st.countTokens() == 1)) {
		 String nt = st.nextToken();
		 if (nt.equalsIgnoreCase("START") && !csActive) {
		   if (csLocalhost)
			   cs = new CS("127.0.0.1", port);
		   else
			   cs = new CS(serverIP, port);


		   cs.startCS();
		   csActive = true;
		 } else if (nt.equalsIgnoreCase("STOP") && csActive) {
		 	killUser( userIDfromNick("ChanServ"), "Services Halted" );
		 	csActive = false;
		 	cs.stopCS();
		 } else if (nt.equalsIgnoreCase("RESTART") && csActive) {
		 	killUser( userIDfromNick("ChanServ"), "Services Restarting" );
			   if (csLocalhost)
				   cs = new CS("127.0.0.1", port);
			   else
				   cs = new CS(serverIP, port);
			 cs.startCS();
		 }
		}
*/

	} else if (command.equals("CHANSERV")) {
	     if (st.countTokens() >= 1) {
			String msg = cmd.substring(9);
			if (msg.charAt(0) != ':') { msg = ":" + msg; }
			handleCommand( "PRIVMSG ChanServ " + msg, userID );
	     }




	} else if (command.equals("R")) {
		if (users[userID].getNick().equalsIgnoreCase("chanserv") && (users[userID].isSysop() == 5)) {
		  if (st.countTokens() == 4) {
			if (st.nextToken().equalsIgnoreCase("RFR")) {
			  //RequestForRegister #channel nickname pass
			  String tmpChan = st.nextToken();
			  String tmpMask = st.nextToken();
			  String tmpNick = nickFromMask(tmpMask);
			  String tmpPass = st.nextToken();
			  int cID = is_channel(tmpChan);
			  if (cID != -1) {
				int uid = userIDfromNick(tmpNick);
				if (uid != -1) {
				   int apos = channels[cID].userArrayPos(uid);
				   if (apos != -1) {
					if (channels[cID].getMemberStatusByID(uid) == 3) {
						tellUser(userID, serverName + " !R RFR " + tmpChan + " " + starMask(tmpMask) + " " + tmpPass);
					}
				   }
				}
			  } else if (hubID != -1) {
			  	//perhaps the channel exists on another server..
			  	tellUser(hubID, O_RFR + " " + userID + " " + tmpChan + " " + starMask(tmpMask) + " " + tmpPass);
			  }
			}
		  }
		}



	/* Use FRAW instead */
	/*
	} else if (command.equals("MIMIC")) {
		//MIMIC nick #chan :text to say...
		if (users[userID].isSysop()>0) {
		  if (st.countTokens() > 2) {
			String tmpNick = st.nextToken();
			String tmpChan = st.nextToken();
			String sayWhat = cmd.substring(8 + tmpNick.length() + tmpChan.length());
			int tmpID = userIDfromNick(tmpNick);
			int chanID = is_channel(tmpChan);
			if ((tmpID != -1) && (chanID != -1)) {
				if (isMember(tmpID, chanID)) {
					tellUsersInChan(chanID, userMask(tmpID) + " PRIVMSG " + tmpChan + " :" + sayWhat);
					noticeOpers(users[userID].getNick() + " used MIMIC on " + tmpNick + " in " + tmpChan);
				}
			}
		  }
		}
	*/

	} else if (command.equals("GLINE")) {
		if (users[userID].isSysop()>=2) {
			if (st.countTokens() > 0) {
				String mask = st.nextToken();
		    	String reason = "";
		    	if (st.hasMoreTokens()) {
		    		reason = cmd.substring(command.length() + mask.length() + 2);
		    	}
		    
				if (hubID != -1) {
					if (validKLine(mask)) {
						KLine k = new KLine( mask, true, users[userID].getNick(), reason );
						if (!isKLined(mask)) {
							writeKLine(k);						
						}
						if (userID != hubID) {
							tellUser(hubID, O_GLINES + " " + mask + " " + k.setter + " " + reason);
							noticeUser(userID, "GLine added for: " + mask);
						}
					} else {
						noticeUser(userID, "Invalid GLine: " + mask);
					}					
				} else {
					noticeUser(userID, "Not connected to hub! Try /KLINE instead");
				}
			} else {
			noticeUser(userID, "The format is /GLINE <hostname> [reason for gline]");
			if (showIPs)
				noticeUser(userID, "e.g. /GLINE 123.12.23.* Server abuse.");
			else
				noticeUser(userID, "e.g. /GLINE *.subdomain.someisp.com Server abuse");
			}			
		} else {
			noticeUser(userID, "You require Global-Operator Status to use this command.");
		}

	} else if (command.equals("UNGLINE")) {
		if (users[userID].isSysop()>=2) {
			if (st.countTokens() > 0) {
				String mask = st.nextToken();
				if (hubID != -1) {
					if (isKLined(mask)) {
						unwriteKLine(mask, true);						
					}
					if (userID != hubID) {
						tellUser(hubID, O_UNGLINE + " " + mask);
						noticeUser(userID, "GLine removed for: " + mask);
					}
				} else {
					noticeUser(userID, "Not connected to hub! Try /UNKLINE instead");
				}
			}			
		} else {
			noticeUser(userID, "You require Global-Operator Status to use this command.");
		}
		



	} else if (command.equals("KLINE")) {
		if (users[userID].isSysop()>0) {
		  if (st.countTokens() > 0) {
		    String mask = st.nextToken();
		    String reason = "";
		    if (st.hasMoreTokens()) {
		    	reason = cmd.substring(command.length() + mask.length() + 2);
		    }
			if (isKLined(mask)) {
				noticeUser(userID, "KLine already exists for mask " + mask);
			} else {
				if (validKLine(mask)) {
					KLine k = new KLine( mask, false, users[userID].getNick(), reason );
					writeKLine(k);
					noticeUser(userID, "KLine added for mask " + mask);
				} else {
					noticeUser(userID, "Invalid KLine mask " + mask);
				}
			}
		  } else {
			noticeUser(userID, "The format is /KLINE <hostname> [reason for kline]");
			if (showIPs)
				noticeUser(userID, "e.g. /KLINE 123.12.23.* Server abuse.");
			else
				noticeUser(userID, "e.g. /KLINE *.subdomain.someisp.com Server abuse");
		  }
		}
		


	} else if (command.equals("UNKLINE")) {
		if (users[userID].isSysop()>0) {
		  if (st.countTokens() == 1) {
			String mask = st.nextToken();
			if (!isKLined(mask)) {
				noticeUser(userID, "There is no active KLine for mask " + mask);
			} else {
				unwriteKLine(mask);
				noticeUser(userID, "KLine removed for mask " + mask);
			}

		  } else {
			noticeUser(userID, "The format is /UNKLINE <hostname>");
			noticeUser(userID, "e.g. /UNKLINE *.subdomain.someisp.com");
		  }
		}



	} else if (command.equals("KLINES")) {
		if (users[userID].isSysop()>0) {
			listKLines(userID);
		}
	} else if (command.equals("GLINES")) {
		if (users[userID].isSysop()>0) {
			listGLines(userID);
		}






	} else if (command.equals("REHASH")) {
		if ((st.countTokens() == 1) && (users[userID].isSysop() >= 3)) {
			String nt = st.nextToken();
			if (nt.equalsIgnoreCase("-all")) {
				initMOTD();
				initConfFile();
				//initOperArray();
				initKLines();
				noticeUser(userID, "Rehashed all server files");
			} else if (nt.equalsIgnoreCase("-conf")) {
				initConfFile();
				noticeUser(userID, "Rehashed conf file");
			//} else if (nt.equalsIgnoreCase("-olines")) {
				//initOperArray();
				//noticeUser(userID, "Rehashed server file: olines");
			} else if (nt.equalsIgnoreCase("-motd")) {
				initMOTD();
				noticeUser(userID, "Rehashed server file: motd");
			} else if (nt.equalsIgnoreCase("-klines")) {
				initKLines();
				noticeUser(userID, "Rehashed server file: " + kfile);
			} else {
				noticeUser(userID, "Format is /REHASH <option>");
				noticeUser(userID, "Options are:");
				noticeUser(userID, "-all  Rehash ALL files.");
				noticeUser(userID, "-conf Rehash ircxy.conf file");
				//noticeUser(userID, "-olines  Rehash olines file.");
				noticeUser(userID, "-motd  Rehash MOTD file.");
				noticeUser(userID, "-klines  Rehash k-lines & g-lines.");
			}

		} else if (users[userID].isSysop() == 1) {
			noticeUser(userID, "Only Network Admins may use REHASH");
		}



	} else if (command.equals("CONF")) {
try {
	 if (users[userID].isSysop() >= 2) {
	 String comarr[] = cmd.split(" ");
	 noticeUser(userID, comarr[0] + " " + comarr[1]);
	 String affarr = ".*";
		File log= new File("ircxy.conf");
	try{
	    	FileReader fr = new FileReader(log);
    		String s;
    		String totalStr = "";
    		try (BufferedReader br = new BufferedReader(fr)) {
			
		        while ((s = br.readLine()) != null) {
			 if(comarr[1] == "set" || comarr[0] == "SET"){
				if(comarr[2] == "o-line"){
					affarr = comarr[3];
				} else {
					affarr = ".*";
				}
				s = s.replaceAll(comarr[2] + "\\t+" + affarr, comarr[2] + "\t\t" + comarr[3]);
			 } else if(comarr[1] == "uncomment" || comarr[1] == "UNCOMMENT") {
				s = s.replaceAll("#" + comarr[2] + "\\t+" + comarr[3], comarr[2] + "\t\t");
			} else if(cmd.indexOf("VIEW") > -1) {
				noticeUser(userID, "CONF/VIEW: " + s.replaceAll("\\t"," "));
			}
            			totalStr += s + "\r\n";
        		}
       		 FileWriter fw = new FileWriter(log);
    		fw.write(totalStr);
    		fw.close();
   	 }
	}catch(Exception e){
	}
	}
} catch(Exception e){
	noticeUser(userID, "ERROR CONF/" + "*");
}
	} else if (command.equals("CHGHOST") || command.equals("VHOST")) {
		if (users[userID].isSysop() >0) {
			String tmpNick;
			int tmpID;
			if (st.countTokens() > 0) {
				tmpNick = st.nextToken();
				tmpID = userIDfromNick(tmpNick);
				
				if (st.countTokens() == 1) {
				  //chghost nickname :newhost
				  String newhost = cmd.substring(command.length() + 2 + tmpNick.length());
				  if (newhost.charAt(0) == ':') { newhost = newhost.substring(1); }
					  
				  if (tmpID != -1) {
					if ((newhost.length() > 0) && (newhost.length() <= 25)) {
						//change the host
						users[tmpID].setHostname(newhost);
						noticeUser(userID, tmpNick + " -> " + userMask(tmpID));
					} else {
						noticeUser(userID, "Invalid host length (" + newhost.length() + " chars.) Must be between 0 and 25");
					}
				  } else {
				  	//no such user on this server, are we linked ?
				  	//user must be at least global-op to do this.
				  	if (users[userID].isSysop() >= 2) {
				  		if ((hubID != -1) && (userID != hubID)) {
				  			tellUser(hubID, O_CHGHOST + " " + users[userID].getNick() + " " + userID + " " + tmpNick + " " + newhost);
				  		} else {
				  			noticeUser(userID, "User not found: " + tmpNick);
				  		}
				  	} else {
				  		noticeUser(userID, "You require Global-OP status to use /" + command + " globally.");
				  	}
				  }
				} else if (st.countTokens() == 0) {
					if (tmpID != -1) {
						setHostMask(tmpID);
						noticeUser(userID, tmpNick + " -> " + userMask(tmpID));
					} else {
				  	//no such user on this server, are we linked ?
				  	//user must be at least global-op to do this.
				  	if (users[userID].isSysop() >= 2) {
				  		if ((hubID != -1) && (userID != hubID)) {
				  			tellUser(hubID, O_CHGHOST + " " + tmpNick);
				  		} else {
				  			noticeUser(userID, "User not found: " + tmpNick);
				  		}
				  	} else {
				  		noticeUser(userID, "You require Global-OP status to use /" + command + " globally.");
				  	}
				   }
				}
			} else {
				noticeUser(userID, "Format is /CHGHOST <nickname> <newhost>");
			}
		} else {
			tellUser(userID, ":" + serverName + " 481 " + users[userID].getNick() + " :Permission Denied- You're not an IRC operator");
		}



	} else if (command.equals("CHGNICK")) {
		if (st.countTokens() == 2) {
			handleCommand( "FRAW " + st.nextToken() + " NICK " + st.nextToken(), userID );
		}
		
		









	} else if (command.equals("FRAW")) {
		if (users[userID].isSysop() > 0) {


			String tmpNick;
			int tmpID;
			if (st.countTokens() >= 2) {
			  tmpNick = st.nextToken();
			  tmpID = userIDfromNick(tmpNick);
			  String fcmd = cmd.substring(6 + tmpNick.length());
			  
			  if (fcmd.charAt(0) == ':') { fcmd = fcmd.substring(1); }
			  
			  if (tmpID != -1) {
				if (users[tmpID].isSysop() <= users[userID].isSysop()) {
					//stuff the command into handleCommand
					handleCommand(fcmd, tmpID);

					noticeOpers(users[userID].getNick() + " used FRAW on " + tmpNick + " -> " + fcmd);
					if ((hubID != -1) && (userID != hubID)) { tellUser(hubID, O_NOTICE_REMOTE_OPERS + " " + users[userID].getNick() + " used FRAW on " + tmpNick + " -> " + fcmd); }
				} else {
					noticeUser(userID, "Can't FRAW " + users[tmpID].getNick() + ", insuficcient Oper status.");
				}
			  } else {
			  	//fraw'ing a user on another server?
			  	if ( (hubID != -1) && (userID != hubID) ) {
			  		//the must be at least global-oper
			  		if (users[userID].isSysop() >= 2) {
			  			tellUser( hubID, O_FRAW_REQUEST + " " + users[userID].getNick() + " " + tmpNick + " " + fcmd );
						  noticeOpers(users[userID].getNick() + " used FRAW on " + tmpNick + " -> " + fcmd);
			  		} else {
			  			noticeUser(userID, "You must be at least GlobalOper to use FRAW on a non-local user.");
			  		}
			  	}
			  }
			} else {
				noticeUser(userID, "Format is /FRAW <nickname> <raw command to be forced from user>");
			}

		} else {
			tellUser(userID, ":" + serverName + " 481 " + users[userID].getNick() + " :Permission Denied- You're not an IRC operator");
		}
			  





	} else if (command.equals("DIE")) {
		if (users[userID].isSysop() >= 3) {
			int count = 5;
			if (st.countTokens() > 0) {
				try { count = Integer.parseInt( st.nextToken() ); } catch (NumberFormatException e) {}
			}
			noticeAll("The server has been ordered to DIE by " + users[userID].getNick() + ", shutting down in " + count + " secs");
			try { Thread.sleep(count * 1000); } catch (InterruptedException e) { }
			System.out.println("\r\nTES: DIE command issued by " + userMask(userID) + " - Shutting down.\r\n");
			System.exit(2);
		}

		


	
	} else if (command.equals("CHANOPEN")) {
		if (users[userID].isSysop() > 0) {
			if (st.countTokens() == 1) {
				String chan = st.nextToken();
				boolean exists = closedChans.removeElement( chan.toLowerCase() );
				if (exists) {
					noticeUser(userID, "Channel: " + chan + " is no longer closed");
				} else {
					noticeUser(userID, "Channel: " + chan + " was not closed!");
				}
			}
		}



	} else if (command.equals("CHANCLOSE") && (users[userID].isSysop() > 0)) {
		//close a channel by making it an oper-only-chan
		//remove the channel
		//then kick everyone out.
		if (st.countTokens() >= 1) {
			String chan = st.nextToken();
			String reason = " :Channel closed";
			if (st.hasMoreTokens()) {
				reason+= " (" + cmd.substring(command.length() + chan.length() + 2) + ")";
			}

			int cID = is_channel( chan );
			if (cID != -1) {
				//take a note of all kick msgs to send
				Vector msgs = new Vector();
				chan = channels[cID].getName();
				int tmpID = -1;
				for (int i=0; i<channels[cID].membercount(); i++) {
					tmpID = channels[cID].getMemberID(i);
					if (tmpID != -1) {
						msgs.addElement( "" + channels[cID].getMemberID(i) );
					}
				}
				
				//add channel to the oper-only-chans list
				if (!closedChans.contains( chan.toLowerCase() )) {
					closedChans.add( chan.toLowerCase() );
				}
				//close the channel
				//channels[cID].cleanChannel();
				//chanCount--;
				

				
				//kick the users
				tmpID = -1;
				for (int i=0; i<msgs.size(); i++) {
					tmpID = Integer.parseInt( (String)msgs.elementAt(i) );
					if (tmpID != userID) { handleCommand( "PART " + chan, tmpID ); }
					noticeUser( tmpID, chan + " has been closed by " + users[userID].getNick() + " (" + reason + ")" );
				}
				
				//tell the hub to kick the other users.
				//if ((hubID != -1) && (userID != hubID)) {
				//	tellUser( hubID, O_CLOSE_CHAN + " " + chan + " " + reason );
				//}
				
			} else {
				//close a non-existant channel, i.e. simply make that channel +O
				//it may exist on another server!
				if (!closedChans.contains( chan.toLowerCase() )) {
					closedChans.add( chan.toLowerCase() );
				}
			}
		}
	



	//kill a user on the server.
	} else if (command.equals("KILL")) {
		if (users[userID].isSysop() > 0) {
		  if (st.countTokens() >= 1) {
			String whoToKill = st.nextToken();
			int kID = userIDfromNick(whoToKill);
			if (kID != -1) {
			   if (users[userID].isSysop() >= users[kID].isSysop()) {
				String reason = users[userID].getNick();
				if (st.countTokens() >=1) {
 					reason = cmd.substring(6 + whoToKill.length());

					if ((reason.charAt(0) == ':') && (reason.length() > 1))
						reason = reason.substring(1);

				}
				Socket tmp = users[kID].getSocket();
				noticeUser(kID, "You have been KILLed by " + users[userID].getNick() + " (" + reason + ")");
				noticeAllWithFlag( "Received KILL for user: " + whoToKill + " from: " + users[userID].getNick() + " (" + reason + ")", "k" );
				sendUserQuit(kID, "KILL by " + users[userID].getNick() + " (" + reason + ")");
				tmp.close();
				releaseUserID(kID);
			   } else {
				noticeUser(userID, "You have insufficient IRCop status to /KILL this user");
				noticeUser(kID,    users[userID].getNick() + " tried to /KILL you.");
			   }
			} else if (whoToKill.charAt(0) == '#') {
				//kill on a channel ?
				String r = users[userID].getNick();
				if (st.countTokens() >=1) { r = cmd.substring( 6 + whoToKill.length()); }
				handleCommand( "CHANCLOSE " + whoToKill + " " + r, userID );
			} else {
				//perhaps the user is trying to /kill a user on another server on the network
				if ((hubID != -1) && (userID != hubID)) {
					if (users[userID].isSysop() > 1) {
						String reason = users[userID].getNick();
						if (st.countTokens() >=1) {
		 					reason = cmd.substring(6 + whoToKill.length());
		
							if (reason.charAt(0) != ':') { reason = ":" + reason; }
		
						}
						tellUser(hubID, O_KILL + " " + userID + " " + users[userID].getNick() + " " + whoToKill + " " + reason);
					} else {
						noticeUser(userID, "You have insufficient IRCop status to /KILL this user");
					}
				}
				
				
			}
		  }
		} else {
			tellUser(userID, ":" + serverName + " 481 " + users[userID].getNick() + " :Permission Denied- You're not an IRC operator");
		}


	} else if (command.equals("BROADCAST") || command.equals("SNOTICE")) {
		if (users[userID].isSysop() >0) {
		  if (st.countTokens() >= 1) {
			String strMsg = "<" + users[userID].getNick() + "> " + cmd.substring(cmd.indexOf(" "));
			
			noticeAll("BROADCAST: " + strMsg);
			
			if ( (users[userID].isSysop() >= 2) && (hubID != -1) && (userID != hubID) ) {
				tellUser( hubID, O_BROADCAST_REQUEST + " " + strMsg );
			}
		  }
		} else {
			tellUser(userID, ":" + serverName + " 481 " + users[userID].getNick() + " :Permission Denied- You're not an IRC operator");
		}


	} else if (command.equals("LONOTICE")) {
		if (users[userID].isSysop() >0) {
		  if (st.countTokens() >= 1) {
			String strMsg = "<" + users[userID].getNick() + "> " + cmd.substring(cmd.indexOf(" "));
			
			noticeOpers("Local-ONOTICE: " + strMsg);
			
		  }
		} else {
			tellUser(userID, ":" + serverName + " 481 " + users[userID].getNick() + " :Permission Denied- You're not an IRC operator");
		}
	} else if (command.equals("GONOTICE")) {
		if (users[userID].isSysop() >0) {
		  if (st.countTokens() >= 1) {
			String strMsg = "<" + users[userID].getNick() + "> " + cmd.substring(cmd.indexOf(" "));
			
			if ( (users[userID].isSysop() >= 2) && (hubID != -1) && (userID != hubID) ) {
				noticeOpers("Global-ONOTICE: " + strMsg);
				tellUser( hubID, O_GONOTICE_REQUEST + " " + strMsg );
			} else if (users[userID].isSysop() < 2) {
				noticeUser(userID, "You have insufficient IRCop status to use /GONOTICE");
			}
			
		  }
		} else {
			tellUser(userID, ":" + serverName + " 481 " + users[userID].getNick() + " :Permission Denied- You're not an IRC operator");
		}		
		

	/*
	//Removed - Use /broadcast or /snotice
	} else if (command.equals("SMSG")) {
		// by Dana.
		if (users[userID].isSysop() >0) {
		  if (st.countTokens() >= 1) {
			String strMsg = cmd.substring(5);
			noticeAll(strMsg);
		  }
		} else {
			tellUser(userID, ":" + serverName + " 481 " + users[userID].getNick() + " :Permission Denied- You're not an IRC operator");
		}
	*/


	} else if (command.equals("ADDSERVER")) {
		if (users[userID].isSysop() == 3) {
			if (st.countTokens() == 3) {
				if (hubID != -1) {
					String nsIP = st.nextToken();
					String nsPort = st.nextToken();
					String nsPass = st.nextToken();
					tellUser( hubID, O_NEWSERVER + " " + nsIP + " " + nsPort + " " + nsPass );
					noticeUser( userID, "Notifying hub of new server: " + nsIP + ":" + nsPort );					
				} else {
					noticeUser( userID, "Cannot add server to hub unless we are connected to hub!" );
				}				
			} else {
				noticeUser( userID, "Format is ADDSERVER <ip> <port> <hub-password>" );
			}		
		}		
	} else if (command.equals("REMSERVER")) {
		if (users[userID].isSysop() == 3) {
			if (st.countTokens() == 1) {
				if (hubID != -1) {
					tellUser( hubID, O_REMOVESERVER + " " + st.nextToken() );
					noticeUser( userID, "Requesting server removal from hub" );					
				} else {
					noticeUser( userID, "Cannot remove server to hub unless we are connected to hub!" );
				}				
			} else {
				noticeUser( userID, "Format is REMSERVER <index> (See /SERVERLIST)" );
			}		
		}
	} else if (command.equals("UNREMSERVER")) {
		if (users[userID].isSysop() == 3) {
			if (st.countTokens() == 1) {
				if (hubID != -1) {
					tellUser( hubID, O_RECONSERVER + " " + st.nextToken() );
					noticeUser( userID, "Requesting server reconnection to hub" );
				} else {
					noticeUser( userID, "Cannot remove server to hub unless we are connected to hub!" );
				}				
			} else {
				noticeUser( userID, "Format is REMSERVER <index> (See /SERVERLIST)" );
			}		
		}
		
		
		
	} else if (command.equals("SERVERLIST")) {
		if (users[userID].isSysop() > 0) {
				if (hubID != -1) {
					tellUser( hubID, O_LISTSERVERS + " " + userID + " " + users[userID].getNick() );
				} else {
					noticeUser( userID, "Cannot retreive server list unless we are connected to hub!" );
				}				
		}




	// ---------------------------------------------------------------------------
	// ---------------------------------------------------------------------------




	} else if (command.equals("WHO")) {
		if (users[userID].isRegistered()) {
		  if (st.countTokens() == 1) {
			String tmpChan = st.nextToken();


			//I added the following for bundled who - Dana
			if (tmpChan.indexOf(",") != -1) {
				String tmp = tmpChan + ",";
				int numRequests = 0; //added this to limit the number of requests a user can make in one call. To prevent mis-use. - David
				
				while ((tmp.indexOf(",") != -1) && (numRequests < max_channels)) {
					numRequests++;
					handleCommand("WHO " + tmp.substring(0, tmp.indexOf(",")), userID);
					tmp = tmp.substring(tmp.indexOf(",")+1);
				}
			} else {




			//:servername 352 bob #test ~djf 62.31.114.78 ntsecurity.nu bob H@ :0 test
			//:servername 315 bob #test :End of /WHO list
			int chanID = is_channel(tmpChan);
			int uid = userIDfromNick(tmpChan);
			
			if ((hubID != -1) && (userID != hubID)) {
				//let the hub deal with the request
				tellUser(hubID, O_WHO_REQUEST + " " + userID + " " + users[userID].getNick() + " " + tmpChan);
			
			} else if (chanID != -1) {

			  //go through the channel, and return a string for each member in the chan
			  String tmpString = ":" + serverName + " 352 " + users[userID].getNick() + " " + channels[chanID].getName() + " ";
			  
			  int mID;
			  for (int i=0; i<channels[chanID].membercount(); i++) {
				mID = channels[chanID].getMemberID(i);
				if (mID != -1) {
					tellUser(userID, tmpString + users[mID].getIdent() + " " + users[mID].getHostname() + " " + serverName + " " + users[mID].getNick() + " H" + channels[chanID].getMemberStatusStr(i) + " :0 " + users[mID].getRealName() );
				}
			  }
				tellUser(userID, ":" + serverName + " 315 " + users[userID].getNick() + " " + channels[chanID].getName() +  " :End of /WHO list");


			} else if (uid != -1) {
				tellUser(userID, ":" + serverName + " 352 " + users[userID].getNick() + " " + users[uid].getNick() + " " +
						users[uid].getIdent() + " " + users[uid].getHostname() + " " + serverName + " " + users[uid].getNick() + " H@ :0 " + users[uid].getRealName() );

				tellUser(userID, ":" + serverName + " 315 " + users[userID].getNick() + " " + users[uid].getNick() +  " :End of /WHO list");

			} else if (tmpChan.indexOf("*") != -1) {
			   String tmpString1 = ":" + serverName + " 352 " + users[userID].getNick() + " " + tmpChan + " ";
			   for (int index=0; index < (max_connections-1); index++) {

				if ( (users[index].ID() != -1)  && ((index != hubID) || (users[userID].isSysop() > 0)) ) {
				  if (matches(tmpChan, users[index].getNick()))
					tellUser(userID, tmpString1 + users[index].getIdent() + " " + users[index].getHostname() + " " + serverName + " " + users[index].getNick() + " H :0 " + users[index].getRealName() );
				}
			   }
				tellUser(userID, ":" + serverName + " 315 " + users[userID].getNick() + " " + tmpChan + " :End of /WHO list");


			} else {
				tellUser(userID, ":" + serverName + " 315 " + users[userID].getNick() + " " + tmpChan +  " :End of /WHO list.");
			}

			}

		  }
		}



	} else if (command.equals("LIST")) {
		if (users[userID].isRegistered()) {
			sendChanList(userID);
		}
	} else if (command.equals("LISTX")) {
		if (users[userID].isRegistered()) {
			sendChanListX(userID);
		}
			



	} else if (command.equals("ISON")) {
		if (users[userID].isRegistered()) {
		  if (st.countTokens() >= 1) {
		  	
			if (hubID == -1) {	  	
				String tmpNick = "";
				int tmpID = -1;
				String foundNicks = "";
	
				while (st.countTokens() > 0) {
					tmpNick = st.nextToken();
					tmpID = userIDfromNick(tmpNick);
					if (tmpID != -1) {
					  foundNicks = foundNicks + " " + users[tmpID].getNick();
					}
				}
				if (foundNicks.length() > 0) { foundNicks = foundNicks.substring(1); }
				tellUser(userID, ":" + serverName + " 303 " + users[userID].getNick() + " :" + foundNicks);
				
			} else if (userID != hubID) {
				tellUser(hubID, O_ISON_REQUEST + " " + userID + " " + users[userID].getNick() + cmd.substring(command.length()));
			}

		  } else {
			tellUserNumeric(userID, 461, "Format is ISON <nick> <nick> ...");
		  }
		}


	} else if (command.equals("WHOWAS")) {
		if (users[userID].isRegistered() && (st.countTokens() >= 1)) {
			String query = st.nextToken();
			int count = 0; //number of results to display
			
			if (st.countTokens() > 0) {
				try { count = Integer.parseInt(st.nextToken()); }
				catch (NumberFormatException e) {}
			}

			if ((hubID != -1) && (userID != hubID)) {
				tellUser(hubID, O_WHOWAS_REQUEST + " " + userID + " " + users[userID].getNick() + " " + query + " " + count);				
			} else {
				//local whowas implementation
				//search the dead ial list for matches.
				Vector matches = null;
				matches = whos.getWasMatches( query, 0 );
			
				
				if ((matches != null) && (matches.size() > 0)) {
					Who tmpWho;
					if ((count <= 0) || (count > matches.size())) { count = matches.size(); }
					
					for (int k=0; k<count; k++) {
						tmpWho = (Who)matches.elementAt( k );
						tellUser(userID, ":" + serverName + " 314 " + users[userID].getNick() + " " + tmpWho.get314());
						tellUser(userID, ":" + serverName + " 312 " + users[userID].getNick() + " " + tmpWho.get312());										
					}									

				} else {
					tellUser(userID, ":" + serverName + " 406 " + users[userID].getNick() + " " + query + " :There was no such nickname" );
				}
				tellUser(userID, ":" + serverName + " 369 " + users[userID].getNick() + " " + query + " :End of WHOWAS" );
				
				
			}
			
			
		}
		
		




	} else if (command.equals("WHOIS")) {
		if (users[userID].isRegistered()) {
		  if (st.countTokens() == 1) {
			//find out if whois'd user is on the server
			String tmpNick = st.nextToken();
			String tmpServ = ":" + serverName;
			String uNick = users[userID].getNick();
			String tmpHostName, tmpChans;
			int tmpID = -1;
			long idleTime;
			
			tmpID = userIDfromNick(tmpNick);
			if ((tmpID != -1) && ( (users[tmpID].isSysop() != 10) || (users[userID].isSysop() > 0)  )) {
				tmpNick = users[tmpID].getNick(); //makes sure the Capitalisation is correct in the nick
				tmpHostName = users[tmpID].getHostname();


				tellUser(userID, tmpServ + " 311 " + uNick + " " + tmpNick + " " + users[tmpID].getIdent() + " " + tmpHostName + " * :" + users[tmpID].getRealName() );

				if ((users[userID].isSysop()>0) || (userID == tmpID))
				   tellUser(userID, tmpServ + " 320 " + uNick + " " + tmpNick + " :is connecting from " + getRealHostname(tmpID));

				tmpChans = whoisChanList(tmpID, users[userID].ircx);
				if (!tmpChans.equals("")) {
				  if ((users[tmpID].isSysop() != 5) || (((users[tmpID].isSysop() == 5) && (!csHiddenWhois)) || (users[userID].isSysop() > 0)))
					  tellUser(userID, tmpServ + " 319 " + uNick + " " + tmpNick + " :" + tmpChans);
				}


				tellUser(userID, tmpServ + " 312 " + uNick + " " + tmpNick + " " + serverName + " :" + serverDescription);

				if (users[tmpID].getModes().indexOf("H") == -1) {
					String level = getOperLevelStr( users[tmpID].isSysop() );
					if (!level.equals("")) {
						tellUser(userID, tmpServ + " 313 " + uNick + " " + tmpNick + level);
						if (users[tmpID].getModes().indexOf("h") != -1) {
							tellUser(userID, tmpServ + " 310 " + uNick + " " + tmpNick + " :is available for help.");
						}
					}
				}


				if ((users[tmpID].isSysop() >0) && (tmpID != userID) && (users[tmpID].getModes().indexOf("W") != -1)) {
				  noticeUser(tmpID, "--- " + users[userID].getNick() + " is doing a /whois on you.");
				}

				if (users[tmpID].isAway()) {
				  tellUser(userID, tmpServ + " 301 " + uNick + " " + tmpNick + " " + users[tmpID].awayMessage());
				}

				idleTime = (long)((System.currentTimeMillis() - users[tmpID].getIdle())/1000);
				tellUser(userID, tmpServ + " 317 " + uNick + " " + tmpNick + " " + idleTime + " " + users[tmpID].getSignOnTime() + " :seconds idle, signon time");



				tellUser(userID, tmpServ + " 318 " + uNick + " " + tmpNick + " :End of /WHOIS list.");
			} else {
				
				
				if ((hubID != -1) && (tmpID != hubID) && (userID != hubID)) {
					tellUser(hubID, O_GETWHOISFOR + " " + users[userID].getNick() + " " + tmpNick + " " + userID + " " + users[userID].ircx);
				} else {
					tellUser(userID, ":" + serverName + " 401 " + users[userID].getNick() + " " + tmpNick + " :No such nick/channel");
					tellUser(userID, ":" + serverName + " 318 " + users[userID].getNick() + " " + tmpNick + " :End of /WHOIS list.");
				}
			}


		  }
		}




	} else if (command.equals("IRCX")) {
		//if (users[userID].isRegistered()) {
			String nick = "*";
			if (users[userID].isRegistered()) { nick = users[userID].getNick(); }
			users[userID].ircx = true;
			tellUser(userID, ":" + serverName + " 800 " + nick + " 1 0 ANON 512 *"); 
		//}

	} else if (command.equals("LUSERS")) {
		if (users[userID].isRegistered()) {
			sendLUsers(userID);
		}

	} else if (command.equals("MOTD")) {
		if (users[userID].isRegistered()) {
			sendMOTD(userID);
		}

	} else if (command.equals("VERSION")) {
		if (users[userID].isRegistered()) {
			tellUser(userID, ":" + serverName + " 351 " + users[userID].getNick() + " " + version + " TES :" + serverDescription); 
		}
		


	} else if (command.equals("PING")) {
		if (users[userID].isRegistered()) {
		  if (st.countTokens() >= 1) {
			String tok1 = st.nextToken();
			String tok2 = users[userID].getNick();
			
			//tmpOrigin = users[userID].getNick();
			if (st.countTokens() == 1)
				tok2 = tok1;

			if (tok2.charAt(0) == ':')
				tok2 = tok2.substring(1);


			if (userID == hubID) {
				tellUser( userID, "PONG" );
			} else {
				tellUser(userID, ":" + serverName + " PONG " + serverName + " :" + tok2);
			}
			pings[userID].resetPings();

			//if (userID == hubID) { System.out.println("Recieved PING from hub"); }

		  } else {
			tellUserNumeric(userID, 409, "No origin specified");
		  }
		}


	
	} else if (command.equals("PONG")) {
		if (users[userID].isRegistered()) {
			pings[userID].resetPings();
		}




	} else if (command.equals("USERHOST")) {
		if (users[userID].isRegistered()) {
		  if (st.countTokens() == 1) {
			String tmpNick = st.nextToken();
			int tmpID = userIDfromNick(tmpNick);
			if (tmpID != -1) {
				tellUserNumeric(userID, 302, users[tmpID].getNick() + "=+" + users[tmpID].getIdent() + "@" + users[tmpID].getHostname());
			}
		  }
		}





	} else if (command.equals("QUIT")) {
		if (users[userID].isRegistered()) {
		  String quitMessage = users[userID].getNick();

		  //theres a quit message
		  if (st.countTokens() > 0) {
			quitMessage = cmd.substring(5);
			if (quitMessage.charAt(0) == ':')
				quitMessage = quitMessage.substring(1);
		  }

		  users[userID].getSocket().close(); // Do we need this ?
		  sendUserQuit(userID, quitMessage);
		  releaseUserID(userID);
		}


	} else if (command.equals("LINKS")) {
		if (users[userID].isRegistered()) {
			if ((hubID != -1) && (userID != hubID)) {
				tellUser(hubID, O_REQUESTLINKS + " " + userID + " " + users[userID].getNick() );
			} else {
				tellUser(userID, ":" + serverName + " 364 " + users[userID].getNick() + " " + serverName + " " + serverName + " :0 " + serverDescription );
				tellUser(userID, ":" + serverName + " 365 " + users[userID].getNick() + " :End of /LINKS list.");
			}		
		}
		






	// ------------ HUB COMMANDS --------------
	
	
	} else if (command.equals(I_PASS_INFO)) {
		if (userID == hubID) {
			//pass information directly to a user
			//PASS_INFO userID info...
			if (st.countTokens() > 1) {
				String suid = st.nextToken();
				int uid = Integer.parseInt( suid );
				String info = cmd.substring( command.length() + suid.length() + 2 );
				info = info.trim();
				
				tellUser(uid, ":" + serverName + " " + info );			
				
				
			}
			
		}
	
	} else if (command.equals(I_NEW_GLINE)) {
		if (userID == hubID) {
			//I_NEW_GLINE mask setter reason
			if (st.countTokens() >= 2) {
				String mask = st.nextToken();
				String setter = st.nextToken();
				String reason = "";
				if (st.hasMoreTokens()) {
					reason = cmd.substring(command.length() + mask.length() + setter.length() + 3);
				}
				if (!isKLined(mask)) {
					KLine k = new KLine(mask, true, setter, reason);
					writeKLine(k);						
				}
			}
		}
	

	/*
	} else if (command.equals(I_PASS_INFO_TESHASERV)) {
		if (userID == hubID) {
			//pass information directly to a user
			//PASS_INFO nick info...
			if (st.countTokens() > 1) {
				int uid = userIDfromNick( "teshaserv" );
				if ((uid != -1) && (users[uid].isSysop() ==5)) {
					String info = cmd.substring( command.length() + 1 );
					info = info.trim();
					tellUser(uid, "TS " + info );
				}				
			}
			
		}
	*/


	} else if (command.equals(I_WHISPER_USER) && (userID == hubID)) {
		//O_WHISPER_USER fromNick toChan toNick nickList message
		if (st.countTokens() >= 5) {
			String umFrom = st.nextToken();
			String toChan = st.nextToken();
			String toNick = st.nextToken();
			String nickList = st.nextToken();
			String message = cmd.substring( command.length() + umFrom.length() + toChan.length() + toNick.length() + nickList.length() + 5 );
			
			int cID = is_channel( toChan );
			if (cID != -1) {
				int uid = userIDfromNick( toNick );
				if ((uid != -1) && isMember(uid, cID)) {
					whisperUser( uid, cID, nickList, umFrom, message );
				}			
			}
		}
		
		

	} else if (command.equals(I_PASS_SERVICES)) {
		if (userID == hubID) {
			tellLocalServices( cmd.substring( command.length() + 1 ) );
		}
		

	} else if (command.equals(I_NOTICE_ALL)) {
		if (userID == hubID) {
			//I_BROADCAST message
			if (st.countTokens() > 1) {
				String message = cmd.substring( command.length() + 1 );
				noticeAll( message );
			}			
		}
		
	} else if (command.equals(I_NOTICE_OPER)) {
		if (userID == hubID) {
			//I_NOTICE_OPER message
			if (st.countTokens() > 1) {
				String message = cmd.substring( command.length() + 1 );
				noticeOpers( message );
			}			
		}
	
	} else if (command.equals(I_FRAW)) {
		if ( (userID == hubID) && (st.countTokens() >= 3) ) {
			//I_FRAW requester whoTo command...
			String requester = st.nextToken();
			String whoTo = st.nextToken();
			String fcmd = cmd.substring( command.length() + requester.length() + whoTo.length() + 3 );
			
			
			int fID = userIDfromNick( whoTo );
			if (fID != -1) {
				handleCommand(fcmd, fID);
				noticeOpers(requester + " used FRAW on " + whoTo + " -> " + fcmd);
			}
			
		}
		

	} else if (command.equals(I_KILL)) {
		if ( (userID == hubID) && (st.countTokens() >= 3) ) {
			//I_FRAW requester whoTo command...
			String requester = st.nextToken();
			String whoTo = st.nextToken();
			String fcmd = cmd.substring( command.length() + requester.length() + whoTo.length() + 3 );
			fcmd = "QUIT :KILL by " + requester + " (" + fcmd + ")";
			
			int fID = userIDfromNick( whoTo );
			if (fID != -1) {
				handleCommand(fcmd, fID);
			}
			
		}


	} else if (command.equals(I_NEW_SERVER)) {
		if ((userID == hubID) && (st.countTokens() == 2)) {
			//I_NEW_SERVER usercount servername
			String uc = st.nextToken();
			String servername = st.nextToken();
			noticeAll( "Now linked with: " + servername + " (Gained " + uc + " users)" );
		}


	} else if (command.equals(I_NICKCOLLISION)) {
		if ((userID == hubID) && (st.countTokens() == 1)) {
			
				String whoToKill = st.nextToken();
				int kID = userIDfromNick(whoToKill);
				if (kID != -1) {
					String reason = "Nickname collision";

					Socket tmp = users[kID].getSocket();
					
						String chans = "";
						
						if (users[kID].ID() != -1) {
				
							for (int i=0; i < max_channels; i++) {
				
								if (channels[i].getName() != "") {
									if (isMember(kID, i)) {
										tellUsersInChanButOne(i, kID, userMask(kID) + " QUIT :Nickname Collision");
										channels[i].remove_user(kID);
										chans = chans + "," + channels[i].getName();
										//if the channel is now empty, destroy it!
										if (channels[i].membercount() == 0) {
											//System.out.println("Channel closed(suq) -> " + channels[i].getName());
											channels[i].cleanChannel();
											chanCount--;
										}
									}
								}
				
							}

						}

					noticeUser(kID, "Connection closed: (Nickname Collision)");
					tmp.close();
					releaseUserID(kID);

				}

		}






	
	} else if (command.equals(I_SERVER_SPLIT_NOTICE)) {
		if ((userID == hubID) && (st.countTokens() == 2)) {
			//SERVER_SPLIT serverName numUsersLost
			String sn = st.nextToken();
			String numLost = st.nextToken();
			
			noticeAll("Server QUIT ! :" + sn + " (Lost " + numLost + " users)");
		}

	} else if (command.equals(I_SERVER_SPLIT_CHAN)) {
		//System.out.println("Server_split_chan");
		if ((userID == hubID) && (st.countTokens() >= 3)) {
			String sn = st.nextToken();
			String chan = st.nextToken();
			String tmpNames = cmd.substring(command.length() + sn.length() + chan.length() +3);
			//String tmpMask = "";
			
			int cID = is_channel(chan);
			if (cID != -1) {
				StringTokenizer ns = new StringTokenizer(tmpNames, ".@+ ");
				
				while (ns.hasMoreTokens()) {
					tellUsersInChan(cID, ":" + ns.nextToken() + "!user@unknown-location QUIT :Netsplit (" + sn + " from hub)");
				}
				
			}
		}
		
		
	
	
	
	
	} else if (command.equals(I_SBOC)) {
		//SBOC userMask  chan  banMask
		if ((userID == hubID) && (st.countTokens() >= 5)) {
			String um = st.nextToken();
			String chan = st.nextToken();
			String ban = st.nextToken();
			boolean os = st.nextToken().equals("3"); //3 == channel owner status.
			String reason = cmd.substring(command.length() + um.length() + chan.length() + ban.length() + 6);
			int chanID = is_channel(chan);
			if (chanID != -1) {
				int success = channels[chanID].addAccess( "DENY", ban, 0, os, reason );

				if (success == 0) {
					//:bob!~djf@62.31.114.XXX MODE #test +b *!*@*
					tellUsersInChan(chanID, um + " MODE " + channels[chanID].getName() + " +b " + ban);
				}
			}
			
		}
	
	} else if (command.equals(I_USBOC)) {
		//USBOC userMask  chan  banMask
		if ((userID == hubID) && (st.countTokens() == 3)) {
			
			
			String um = st.nextToken();
			String chan = st.nextToken();
			String ban = st.nextToken();
			
			int chanID = is_channel(chan);
			if (chanID != -1) {
					int success = channels[chanID].removeAccess( "DENY", ban, true );

					if (success == 0) {
						tellUsersInChan(chanID, um + " MODE " + channels[chanID].getName() + " -b " + ban);
					}
			}
		}
	
	
	
	
		
	} else if (command.equals(I_CHAN_MODE)) {
		if ((userID == hubID) && (st.countTokens() >= 3)) {
			String um = st.nextToken();
			String chan = st.nextToken();
			String modes = cmd.substring(command.length() + um.length() + chan.length() +3 );
			
			int cID = is_channel(chan);
			if (cID != -1) {
				//alter the modes in the channel
				String plusModes = "";
				String minusModes = "";
				//Modes will already be ordered by the sending server...  +nt-im	or  +l 100
				if (modes.startsWith("+l ")) {
					int limit = Integer.parseInt(modes.substring(modes.indexOf(" ")+1));
					channels[cID].setlimit(limit);
				} else if (modes.charAt(0) == '+') {
					if (modes.indexOf("-") != -1) {
						plusModes = modes.substring(1, modes.indexOf("-"));
						minusModes = modes.substring(modes.indexOf("-")+1);
					} else {
						plusModes = modes.substring(1);
					}
				
				} else if (modes.charAt(0) == '-') {
					minusModes = modes.substring(1);
				}
				for (int i=0; i<plusModes.length(); i++)  { channels[cID].setmode( ""+ plusModes.charAt(i) );     }
				for (int i=0; i<minusModes.length(); i++) { channels[cID].unsetmode( "" + minusModes.charAt(i) ); }
				
				
				
				
				tellUsersInChan(cID, um + " MODE " + chan + " " + modes);
				tellServices( um + " MODE " + chan + " " + modes);
				
			}
		}
	
	
		
	} else if (command.equals(I_NEW_USERCOUNT)) {
		if ((userID == hubID) && (st.countTokens() == 2)) {
			globalUserCount = Integer.parseInt(st.nextToken());
			globalHighestUserCount = Integer.parseInt(st.nextToken());
		}
		
	} else if (command.equals(I_NEW_SERVERCOUNT)) {
		if ((userID == hubID) && (st.countTokens() == 1)) {
			netServerCount = Integer.parseInt( st.nextToken() );
		}
		
		
	} else if (command.equals(I_CLEAR_ACCESS)) {
		if ((userID == hubID) && (st.countTokens() == 3)) {
			String levels = st.nextToken();
			String chan = st.nextToken();
			boolean owner = st.nextToken().equals("true");
			
			int cID = is_channel(chan);
			if (cID != -1) {
				if (levels.indexOf("O") != -1) { channels[cID].clearAccess(owner, "OWNER"); }
				if (levels.indexOf("H") != -1) { channels[cID].clearAccess(owner, "HOST"); }
				if (levels.indexOf("B") != -1) { channels[cID].clearAccess(owner, "DENY"); }
				if (levels.indexOf("V") != -1) { channels[cID].clearAccess(owner, "VOICE"); }
			}
			
		}
	
	
	} else if (command.equals(I_ADD_ACCESS)) {
		if ((userID == hubID) && (st.countTokens() >= 6)) {
			String level = st.nextToken();
			String chan = st.nextToken();
			String mask = st.nextToken();
			boolean os = st.nextToken().equals("3");
			String tout = st.nextToken();
			int timeout = 0;
			try { timeout = Integer.parseInt(tout); } catch (NumberFormatException e) {}
			
			String reason = cmd.substring( command.length() + level.length() + chan.length() + mask.length() + tout.length() + 7 );
			
			int cID = is_channel(chan);
			if (cID != -1) {
				if (level.equals("O")) { channels[cID].addAccess("OWNER", mask, timeout, os, reason); }
				else if (level.equals("H")) { channels[cID].addAccess("HOST", mask, timeout, os, reason); }
				else if (level.equals("V")) { channels[cID].addAccess("VOICE", mask, timeout, os, reason); }
				else if (level.equals("B")) { channels[cID].addAccess("DENY", mask, timeout, os, reason); }
			}
			
		}
	} else if (command.equals(I_REM_ACCESS)) {
		if ((userID == hubID) && (st.countTokens() == 4)) {
			String level = st.nextToken();
			String chan = st.nextToken();
			String mask = st.nextToken();
			boolean os = st.nextToken().equals("3");
			
			int cID = is_channel(chan);
			if (cID != -1) {
				if (level.equals("O")) { channels[cID].removeAccess( "OWNER", mask, os ); }
				else if (level.equals("H")) { channels[cID].removeAccess( "HOST", mask, os ); }
				else if (level.equals("V")) { channels[cID].removeAccess( "VOICE", mask, os ); }
				else if (level.equals("B")) { channels[cID].removeAccess( "DENY", mask, os ); }
			}
			
		}
		
		
		
		
	
	
		
	} else if (command.equals(I_REQUEST_CHANMODES)) {
		if (userID == hubID) {
			//REQUEST_CHANMODES nick uid sid chan
			if (st.countTokens() == 4) {
				String nick = st.nextToken();
				String uid = st.nextToken();
				String sid = st.nextToken();
				String chan = st.nextToken();
				
				
				int cID = is_channel(chan);
				if (cID != -1) {
					tellUser(hubID, O_PASSINFO + " " + sid + " " + uid + " :" + serverName + " 324 " + nick + " " + chan + " " + channels[cID].modeString() );
				} else {
					tellUser(hubID, O_PASSINFO + " " + sid + " " + uid + " :" + serverName + " 403 " + nick + " " + chan + " :No such channel");
				}
			}		
		}
		
		
		
	} else if (command.equals(I_KICK_FROM_CHAN)) {
		if (userID == hubID) {
		  if (st.countTokens() >= 4) {

		  	String um = st.nextToken();
		  	String chan = st.nextToken();
		  	String kickWho = st.nextToken();
		  	String reason = cmd.substring(command.length() + um.length() + chan.length() + kickWho.length() + 4);
		  	String nick = um.substring(1, um.indexOf("!"));
		  	reason = reason.trim();
		  	
			  int kID = userIDfromNick(kickWho);
		  	int cID = is_channel( chan );
			  String kickString = um + " KICK " + chan + " " + kickWho + " " + reason;
							  	
		  	if (cID != -1) {

					if (!reason.startsWith(":")) {
						reason = ":" + reason;
					}
								  		
					if (kID != -1) {
						if  (isMember(kID, cID)) {
							int pos = channels[cID].userArrayPos(kID);
							users[kID].decNumChansJoined();
	
							if (channels[cID].membercount() ==1) {
								channels[cID].cleanChannel();
								tellUser(kID, kickString);
							} else {
								channels[cID].remove_userAtPos(pos);
								tellUsersInChan(cID, kickString);
								tellUser(kID, kickString);
							}
						} else {
							tellUsersInChan(cID, kickString);
							tellUser(kID, kickString);
						}
					} else {
						tellUsersInChan(cID, kickString);
						tellLocalServices( kickString );
					}
			  } else if (nick.equalsIgnoreCase(kickWho)) {
			  	//a user has just kicked themselves from a chan.
			  	//the chan will no-longer exist, so just tell this user.
			  	if (kID != -1) {
			  		tellUser(kID, kickString);
			  	}
			  }
			
		  }			
		}
		
		
		


	} else if (command.equals("FIND_TOPIC")) {
		if (userID == hubID) {
			if (st.countTokens() == 3) {
				//FIND_TOPIC uid sid nick chan
				String uid = st.nextToken();
				String sid = st.nextToken();
				String nick = st.nextToken();
				String chan = st.nextToken();
				
				
				int chanID = is_channel(chan);
				if (chanID != -1) {
					String strTopic = channels[chanID].getTopic();
					String pi = O_PASSINFO + " " + sid + " " + uid + " ";
					if (!strTopic.equals("")) {
						tellUser(hubID, pi + ":" + serverName + " 332 " + nick + " " + chan + " :" + channels[chanID].getTopic());
						tellUser(hubID, pi + ":" + serverName + " 333 " + nick + " " + chan + " " + channels[chanID].whoSetTopic() + " " + channels[chanID].topicTimeStamp());

					} else {
						tellUser(hubID, pi + ":" + serverName + " 331 " + nick + " " + chan + " :No topic is set.");
					}
				
				}
				
			}
		}


	} else if (command.equals(I_TOPIC_CHANGE)) {
		if (userID == hubID) {
			if (st.countTokens() >= 4) {
				//TOPIC_CHANGE userMask chan timestamp :topic
				String um = st.nextToken();
				String chan = st.nextToken();
				String timestamp = st.nextToken();
				String topic = cmd.substring(command.length() + um.length() + chan.length() + timestamp.length() + 4);
				String nick = um.substring(1, um.indexOf("!"));
				
				int cID = is_channel( chan );
				if (cID != -1) {
					channels[cID].setTopic(topic.substring(1));
					channels[cID].setwhoSetTopic(nick);
					channels[cID].settopicTimeStamp( timestamp  );
					tellUsersInChan( cID, um + " TOPIC " + chan + " " + topic);
					tellServices( um + " TOPIC " + chan + " " + topic);
				}
				
				
			}
		}


	} else if (command.equals(I_CHAN_PROP)) {
		if (userID == hubID) {
			if (st.countTokens() >= 4) {
				//CHAN_PROP um prop chan :value
				String um = st.nextToken();
				String prop = st.nextToken();
				String chan = st.nextToken();
				String value = cmd.substring(command.length() + um.length() + chan.length() + prop.length() + 4);
				String nick = um.substring(1, um.indexOf("!"));
				
				int cID = is_channel(chan);
				if (cID != -1) {

					if (prop.equals("ONJOIN")) {
						channels[cID].setOnjoin( value );
						tellUsersInChan(cID, um + " PROP " + chan + " " + prop + " :" + value);
						
					} else if (prop.equals("ONPART")) {
						channels[cID].setOnpart( value );
						tellUsersInChan(cID, um + " PROP " + chan + " " + prop + " :" + value);
						
						
					} else if (prop.equals("OWNERKEY")) {
						channels[cID].setOwnerkey( value );
						tellOwnersInChan(cID, um + " PROP " + chan + " " + prop + " :" + value);
					} else if (prop.equals("HOSTKEY")) {
						channels[cID].setHostkey( value );
						tellOpsInChan(cID, um + " PROP " + chan + " " + prop + " :" + value);
					}
					
					
					
					//TBD : other props ! Remember to check who to tell, i.e. tellUsersInChan OR tellOwners etc !
				}
			}
		}





	} else if (command.equals(I_NEW_OPER)) {
		if (userID == hubID) {
			if (st.countTokens() == 3) {

				String nick = st.nextToken();
				int level = Integer.parseInt( st.nextToken() );
				String sn = st.nextToken();
				String operLevel = "an IRC Operator";
					
				
				if (level == 1)
				  operLevel = "an IRC Operator";
				else if (level == 2)
				  operLevel = "a Global IRC Operator";				  
				else if (level == 3)
				  operLevel = "a Network Administrator";
				else if (level == 5)
				  operLevel = "a Network Services Bot";
				else if (level == 10)
				  operLevel = "the Network HUB";
				  
				  
				  
				
				noticeAll( "--- " + nick + " is now " + operLevel + " (on server " + sn + ")");
				
				
			}
		}
		
		
		
	} else if (command.equals(I_SUMOC)) {
		if (userID == hubID) {
			if (st.countTokens() == 4) {
				//SUMOC usermask chan mode whoTo
				String um = st.nextToken();
				String chan = st.nextToken();
				String mode = st.nextToken();
				String whoTo = st.nextToken();
				
				int cID = is_channel( chan );
				
				if (cID != -1) {
					//if the user is on the channel, set their mode
					int uid = userIDfromNick(whoTo);
					if (uid != -1) {
						int pos = channels[cID].userArrayPos(uid);

						if (pos != -1) {
							String uStat = channels[cID].getMemberStatusStr(pos);
							if 	 (mode.equals("+q")) { channels[cID].setMemberStatusByID(uid, 3); }
							else if (mode.equals("-q")) { channels[cID].setMemberStatusByID(uid, 0); }
							
							else if (mode.equals("+o")) { channels[cID].setMemberStatusByID(uid, 2); }
							else if (mode.equals("-o")) { channels[cID].setMemberStatusByID(uid, 0); }
							
							else if (mode.equals("+v") && uStat.equals("")) { channels[cID].setMemberStatusByID(uid, 1); }
							else if (mode.equals("-v") && uStat.equals("")) { channels[cID].setMemberStatusByID(uid, 0); }
						}
						
					}
					
					
					//inform the users on that channel
					if (mode.equals("+q")) {
						tellUsersInChan( cID, um + " MODE " + chan + " +o " + whoTo,
											  um + " MODE " + chan + " " + mode + " " + whoTo
										 );
					} else if (mode.equals("-q")) {
						tellUsersInChan( cID, um + " MODE " + chan + " -o " + whoTo,
											  um + " MODE " + chan + " " + mode + " " + whoTo
										 );
					} else {
						tellUsersInChan( cID, um + " MODE " + chan + " " + mode + " " + whoTo );
					}
				
										
				}
				
				
			}
		}
	
	
	} else if (command.equals(I_NOT_CHAN_OP)) {
		if (userID == hubID) {
			if (st.countTokens() == 2) {
				String nick = st.nextToken();
				String chan = st.nextToken();
				
				int uid = userIDfromNick( nick );
				if (uid != -1) {
					tellUser( uid, ":" + serverName + " 482 " + nick + " " + chan + " :You're not channel operator");
				}
				
				
			}
		}
		
	
	} else if (command.equals(I_NO_SUCH_CHANNEL)) {
		if (userID == hubID) {
			if (st.countTokens() == 2) {
				String nick = st.nextToken();
				String chan = st.nextToken();
				
				int uid = userIDfromNick( nick );
				if (uid != -1) {
					tellUser( uid, ":" + serverName + " 403 " + nick + " " + chan + " :No such channel");
				}
				
				
			}
		}
	
	} else if (command.equals(I_NO_SUCH_NICK)) {
		if (userID == hubID) {
			if (st.countTokens() == 2) {
				String nick = st.nextToken();
				String whoTo = st.nextToken();
				
				int uid = userIDfromNick( nick );
				if (uid != -1) {
					tellUser( uid, ":" + serverName + " 401 " + nick + " " + whoTo + " :No such nick/channel");
				}
				
			}
			
		}
	
	
	
	
	
	
	} else if (command.equals("N")) {
		if (userID == hubID) {
			//N userMask chan  full-message
			if (st.countTokens() >= 3) {
				
				String um = st.nextToken();
				String chan = st.nextToken();
				
				String fm = cmd.substring(command.length() + um.length() + chan.length() +3 );
				//fm = fm.trim();
				
				
				int cID = is_channel( chan );
				if (cID != -1) {
					tellUsersInChan( cID, um + " NOTICE " + chan + " :" + fm );
				}
			}
		}
		
	} else if (command.equals("NU")) {
		if (userID == hubID) {
			//NU userMask chan  full-message
			if (st.countTokens() >= 3) {
				
				String um = st.nextToken();
				String whoTo = st.nextToken();
				
				String fm = cmd.substring(command.length() + um.length() + whoTo.length() +3 );
			
				int uID = userIDfromNick( whoTo );
				if (uID != -1) {
					if (users[uID].hasSilenceMatch(um.substring(1)) == false) {
						tellUsersInChan( uID, um + " NOTICE " + whoTo + " :" + fm );
					}
				}
			}
		}
	

	
	
	} else if (command.equals("PM")) {
		if (userID == hubID) {
			//PM userMask chan  full-message
			if (st.countTokens() >= 3) {
				
				String um = st.nextToken();
				String chan = st.nextToken();

				int cID = is_channel( chan );
				if (cID != -1) {
					String fm = cmd.substring(command.length() + um.length() + chan.length() +3 );
					if (hubcompress) { fm = decompress(fm); }
					tellUsersInChan( cID, um + " PRIVMSG " + chan + " :" + fm );					
				}
			}
		}
		
	} else if (command.equals("PMU")) {
		if (userID == hubID) {
			//PM userMask chan  full-message
			if (st.countTokens() >= 3) {
				
				String um = st.nextToken();
				String whoTo = st.nextToken();
				
				int uID = userIDfromNick( whoTo );
				if (uID != -1) {
					String fm = cmd.substring(command.length() + um.length() + whoTo.length() +3 );
					if (users[uID].hasSilenceMatch(um.substring(1)) == false) {
						if (hubcompress) { fm = decompress(fm); }
						tellUser( uID, um + " PRIVMSG " + whoTo + " :" + fm );
					}						
				}
			}
		}
	
	
	} else if (command.equals(I_SILENCES_REQ)) {
		if (userID == hubID) {
			//I_SILENCES_REQ server-id	requester-user-id	requester-nick	param
			if (st.countTokens() == 4) {
				String sid = st.nextToken();
				String ruid = st.nextToken();
				String requester = st.nextToken();
				String param = st.nextToken();
				
				int tmpID = userIDfromNick( param );
				String pis = O_PASSINFO + " " + sid + " " + ruid;
				
				if (tmpID != -1) {
					Vector s = users[tmpID].getSilences();
					for (int i=0; i<s.size(); i++) {
						tellUser(hubID, pis + " 271 " + requester + " " + param + " " + s.elementAt(i));
					}
			
					tellUser(hubID, pis + " 272 " + requester + " :End of Silence List");

				} else {
					tellUser(hubID, pis + " 401 " + requester + " " + param + " :No such nick/channel");
				}
				
			}			
		}
	
	
	
	} else if (command.equals(I_WHOIS_REQUEST)) {
		if (userID == hubID) {
			//retrieve whois information for a particular user, and pass it back to the hub
			//WHOIS_REQUEST server-id    requested-user-id   requester  whoisNick
			if (st.countTokens() >= 4) {
				
				String sid = st.nextToken(); //id of the requesting server
				String ruid = st.nextToken(); //userID of the requesting user
				String requester = st.nextToken(); //nickname of the requesting user
				String whoisNick = st.nextToken(); //nickname of the user being whois'd
				
				int operLevel = -1;
				
				if (st.countTokens() > 0) {
					try {
						operLevel = Integer.parseInt( st.nextToken() );
					} catch(NumberFormatException e) {}
				}
				boolean ircx = true;
				if ((st.countTokens() > 0) && st.nextToken().equals("false")) { ircx = false; }
				
				String pis = O_PASSINFO + " " + sid + " " + ruid; //pass info start line




			int tmpID = userIDfromNick(whoisNick);
			if ((tmpID != -1) && ( (users[tmpID].isSysop() != 10) || (users[userID].isSysop() > 0)  )) {
				whoisNick = users[tmpID].getNick(); //makes sure the Capitalisation is correct in the nick
				String tmpServ = ""; //":" + serverName;
				//unick = requester
				String tmpHostName = users[tmpID].getHostname();


				tellUser(hubID, pis + tmpServ + " 311 " + requester + " " + whoisNick + " " + users[tmpID].getIdent() + " " + tmpHostName + " * :" + users[tmpID].getRealName() );

				if (operLevel > 0) {
				   tellUser(hubID, pis + tmpServ + " 320 " + requester + " " + whoisNick + " :is connecting from " + getRealHostname(tmpID));
				}
				
				String tmpChans = whoisChanList(tmpID, ircx);
				if (!tmpChans.equals("")) {
				  if ((users[tmpID].isSysop() != 5) || (((users[tmpID].isSysop() == 5) && (!csHiddenWhois)) || (operLevel > 0)))
					  tellUser(hubID, pis + tmpServ + " 319 " + requester + " " + whoisNick + " :" + tmpChans);
				}


				tellUser(hubID, pis + tmpServ + " 312 " + requester + " " + whoisNick + " " + serverName + " :" + serverDescription);


				if (users[tmpID].getModes().indexOf("H") == -1) {
					String level = getOperLevelStr( users[tmpID].isSysop() );
					if (!level.equals("")) {
						tellUser(hubID, pis + tmpServ + " 313 " + requester + " " + whoisNick + level);
						if (users[tmpID].getModes().indexOf("h") != -1) {
							tellUser(hubID, pis + tmpServ + " 310 " + requester + " " + whoisNick + " :is available for help.");
						}
					}
				}


				if ((users[tmpID].isSysop() >0) && (users[tmpID].getModes().indexOf("W") != -1)) {
				  noticeUser(tmpID, "--- " + requester + " is doing a /whois on you.");
				}

				if (users[tmpID].isAway()) {
				  tellUser(hubID, pis + tmpServ + " 301 " + requester + " " + whoisNick + " " + users[tmpID].awayMessage());
				}

				long idleTime = (long)((System.currentTimeMillis() - users[tmpID].getLastMsg())/1000);
				tellUser(hubID, pis + tmpServ + " 317 " + requester + " " + whoisNick + " " + idleTime + " " + users[tmpID].getSignOnTime() + " :seconds idle, signon time");



				tellUser(hubID, pis + tmpServ + " 318 " + requester + " " + whoisNick + " :End of /WHOIS list.");
			} else {
					tellUser(hubID, pis + " 401 " + requester + " " + whoisNick + " :No such nick/channel");
					tellUser(hubID, pis + " 318 " + requester + " " + whoisNick + " :End of /WHOIS list.");
			}


				
			}	
			
			
		}
		
		
	
	
	} else if (command.equals(I_SENDCHANLISTS)) {
		if (userID == hubID) {
			sendHubChanLists();
		}
		
		
	} else if (command.equals(I_CHANCOLLISION)) {
		if (userID == hubID) {
			if (st.countTokens() == 1) {
				String chan = st.nextToken();
				
				//Get every user to part this channel.
				int cID = is_channel(chan);
				int tmpID = -1;
				
				
				if (cID != -1) {

					Vector tmpUsers = channels[cID].getMembers();
					chan = channels[cID].getName(); //correct capitilisation.
					
					channels[cID].cleanChannel();
					chanCount--;
					String uid = "";
					
					for (int i=0; i<tmpUsers.size(); i++) {
						uid = (String)tmpUsers.elementAt( i );
						uid = uid.substring( 0, uid.indexOf(" ") );
						tmpID = Integer.parseInt( uid );
						users[tmpID].decNumChansJoined();
						tellUser(tmpID, userMask(hubID) + " KICK " + chan + " " + users[tmpID].getNick() + " :Channel collision" );
					}


				}
				
				
			}
			
		}


	
	
	} else if (command.equals(I_USER_CONNECTED)) {
		if (userID == hubID) {
			if (st.countTokens() == 2) {
				String nUser = st.nextToken();
				String nServer = st.nextToken();
				noticeAllWithFlag( nUser + " is connecting on server " + nServer, "F" );
			}
			
		}
	
	
	} else if (command.equals(I_NICKCHANGE_DENIED)) {
		if (userID == hubID) {
			if (st.countTokens() == 2) {
				int tmpID = Integer.parseInt(st.nextToken());
				String newNick = st.nextToken();
				//String callBack = tmpID + " NICK " + newNick;
				//removeCallback(callBack);
				tellUser(tmpID, raws.r433(users[tmpID].getNick(), newNick));			
				
			}
		}
	
	} else if (command.equals(I_NICKCHANGE_ALLOWED)) {
		if (userID == hubID) {
			if (st.countTokens() == 2) {
				int tmpID = Integer.parseInt(st.nextToken());
				String newNick = st.nextToken();
				makeCallback( tmpID + " NICK " + newNick );
				handleCommand( "NICK " + newNick, tmpID );
			}
		}
	
	
	
	} else if (command.equals(I_NICK_CHANGE)) {
		if (userID == hubID) {
			if (st.countTokens() > 2) {
				//NICK_CHANGE userMask newNick #chan1 #chan2 #chan3
				String um = st.nextToken();
				String on = um.substring(0, um.indexOf("!"));
				String nn = st.nextToken();
				
				String c = "";
				int cID = -1;
				
				
				int tmpUID;
				int ids[] = new int[max_connections];
				for (int a=0; a<ids.length; a++)
					ids[a] = 0;
			
				
				while (st.hasMoreTokens()) {
					c = st.nextToken();
					cID = is_channel( c );
					if (cID != -1) {
						//tellUsersInChan( cID, um + " NICK " + nn );
						
						for (int j=0; j<channels[cID].membercount(); j++) {
							tmpUID = channels[cID].getMemberID(j);
							if (tmpUID != -1) {
								ids[tmpUID] = 1;
							}
						}
						
					}
					
				}
				
				//relevant indexes are now set
				
				for (int k=0; k <ids.length; k++) {
					if (ids[k] == 1)
						tellUser(k, um + " NICK " + nn);
				}
	
			}
		}
		
		
		
	} else if (command.equals(I_CHANJOIN_ALLOWED)) {
		if (userID == hubID) {

			if (st.countTokens() >= 3) {
				String chans = st.nextToken();
				String suid = st.nextToken();
				int uID = Integer.parseInt( suid );
				String origCmd = cmd.substring( command.length() + 3 + chans.length() + suid.length() );
				
				//remove the original callback
				removeCallback( uID + " " + origCmd );
				//construct a new callback
				makeCallback( uID + " JOIN " + chans );
				//make the user join the channels
				handleCommand(origCmd, uID);
				
				
			}
		}
			

	} else if (command.equals(I_CLONE_CHANNEL)) {
		if (userID == hubID) {
			//clone-channel uid cmmd.length cmmd topic.length topic onjoin.length onjoin ... (onpart) (ownerkey) (hostkey) (cmodes) (climit) #chan :names list
			
			String fullLine = cmd.substring(command.length()+1);
			
			String suid = fullLine.substring(0, fullLine.indexOf(" "));
			fullLine = fullLine.substring(fullLine.indexOf(" ")+1);
			
			int uid = Integer.parseInt(suid);
			
			int len = 0;
			
			//find the length of cmmd
			len = Integer.parseInt( fullLine.substring(0, fullLine.indexOf(" ")) );
			fullLine = fullLine.substring(fullLine.indexOf(" ")+1);
			
			String cmmd = fullLine.substring(0, len);
			fullLine = fullLine.substring(len+1);
			
			
			String key = cmmd.substring(5); // 5 = "JOIN "
			if (key.indexOf(" ") == -1) { key = ""; }
			else {
				key = key.substring( key.indexOf(" ")+1 );
				cmmd = cmmd.substring(0, cmmd.length() - key.length() - 1);
			}
			
			
			
			//find out the length of topic
			len = Integer.parseInt( fullLine.substring(0, fullLine.indexOf(" ")) );
			fullLine = fullLine.substring(fullLine.indexOf(" ")+1);
			
			String topic = fullLine.substring(0, len);
			fullLine = fullLine.substring(len+1);
			
			
			//find out the length of onjoin
			len = Integer.parseInt( fullLine.substring(0, fullLine.indexOf(" ")) );
			fullLine = fullLine.substring(fullLine.indexOf(" ")+1);
			
			String onjoin = fullLine.substring(0, len);
			fullLine = fullLine.substring(len+1);

			
			//onpart..
			len = Integer.parseInt( fullLine.substring(0, fullLine.indexOf(" ")) );
			fullLine = fullLine.substring(fullLine.indexOf(" ")+1);
			
			String onpart = fullLine.substring(0, len);
			fullLine = fullLine.substring(len+1);
			
			String ownerkey = fullLine.substring(0, fullLine.indexOf(" "));
			fullLine = fullLine.substring(fullLine.indexOf(" ")+1);
			
			String hostkey = fullLine.substring(0, fullLine.indexOf(" "));
			fullLine = fullLine.substring(fullLine.indexOf(" ")+1);

			String cmodes = fullLine.substring(0, fullLine.indexOf(" "));
			fullLine = fullLine.substring(fullLine.indexOf(" ")+1);

			String climit = fullLine.substring(0, fullLine.indexOf(" "));
			fullLine = fullLine.substring(fullLine.indexOf(" ")+1);


			String tmpChan = fullLine.substring(0, fullLine.indexOf(" "));
			fullLine = fullLine.substring(fullLine.indexOf(" ")+2);
			
			String nL = fullLine.trim();
			
			
			
			//remove the original callback
			removeCallback( uid + " " + cmmd );
			
			//Open a new channel, called 'chan'.
			//make the user join it.
			//send that user the names list.
			
			
			
					if (isValidChanName(tmpChan)) {
						//System.out.println("Create channel: " + tmpChan);
						int chanid = newChannelID(tmpChan);
						//add the user to the chan
						
						String strLNames = "";
						if (users[uid].isSysop() > 0) {
							channels[chanid].add_user(uid, 3);
							if (users[uid].ircx) { strLNames = "." + users[uid].getNick() + " " + nL; }
							else { strLNames = "@" + users[uid].getNick() + " " + nL; }
						} else {
							channels[chanid].add_user(uid, 0);
							strLNames = users[uid].getNick() + " " + nL;
						}
						
						tellUser(uid, userMask(uid) + " JOIN :" + tmpChan);

						users[uid].incNumChansJoined();


						//names list
						String strStart = ":" + serverName + " 353 " + users[uid].getNick() + " = " + tmpChan + " :";
						String strEnd = ":" + serverName + " 366 " + users[uid].getNick() + " " + tmpChan + " :End of /NAMES list.";
						tellUser(uid, strStart + strLNames);
						tellUser(uid, strEnd);


						
						channels[chanid].setCreation( (long)(System.currentTimeMillis() / 1000)  );
						//channels[chanid].initBanList();
						//channels[chanid].initOwnList();
						//channels[chanid].initHostList();
						channels[chanid].clone = true;
						
						//set up the topic
						if (!topic.equals("")) {
							//topic comes in 3 parts	who_set_topic timestamp :topic
							String whoSetTopic = topic.substring(0, topic.indexOf(" "));
							topic = topic.substring(topic.indexOf(" ")+1);
							String timestamp = topic.substring(0, topic.indexOf(" "));
							topic = topic.substring(topic.indexOf(" ")+2); //get rid of :
							
							channels[chanid].setwhoSetTopic( whoSetTopic );
							channels[chanid].settopicTimeStamp( timestamp );
							channels[chanid].setTopic( topic );
							
							
							tellUser(uid, raws.r332(users[uid].getNick(), tmpChan, topic));
							tellUser(uid, raws.r333(users[uid].getNick(), tmpChan, whoSetTopic, timestamp));
						}

						//set up the onjoin / onpart
						if (!onjoin.equals("")) {
							channels[chanid].setOnjoin( onjoin );
							tellUser(uid, ":" + tmpChan + " PRIVMSG " + tmpChan + " :" + onjoin);
						}
						if (!onpart.equals(""))   { channels[chanid].setOnpart( onpart );     }
						if (!ownerkey.equals("")) { channels[chanid].setOwnerkey( ownerkey ); }
						if (!hostkey.equals(""))  { channels[chanid].setHostkey( hostkey );   }
						
						if (cmodes.length() > 1) { channels[chanid].modes = cmodes.substring(1); }
						int cLim = 0;
						try { cLim = Integer.parseInt( climit ); } catch (NumberFormatException e) {}
						channels[chanid].setlimit( cLim );
						
												
						
						tellUser(hubID, O_CHANJOIN + " " + userMask(uid) + " " + tmpChan );
						tellServices( userMask(uid) + " JOIN :" + tmpChan);
						
						
						if ( (hubID != -1) && (users[uid].isSysop() >0) ) {
							tellUser(hubID, O_SUMOC + " ." + userMask(uid) + " " + tmpChan + " +q " + users[uid].getNick() );

						} else if (ownerkey.equals(key) && (!key.equals(""))) {
							int uap = channels[chanid].userArrayPos(uid);
							if (uap != -1) {
								channels[chanid].setMemberStatusByID(uid, 3);
								tellUsersInChan(chanid, userMask(uid) + " MODE " + tmpChan + " +q " + users[uid].getNick() );
								tellUser(hubID, O_SUMOC + " ." + userMask(uid) + " " + tmpChan + " +q " + users[uid].getNick() );
							}							
						} else if (hostkey.equals(key) && (!key.equals(""))) {
							int uap = channels[chanid].userArrayPos(uid);
							if (uap != -1) {
								channels[chanid].setMemberStatusByID(uid, 2);
								tellUsersInChan(chanid, userMask(uid) + " MODE " + tmpChan + " +o " + users[uid].getNick() );
								tellUser(hubID, O_SUMOC + " ." + userMask(uid) + " " + tmpChan + " +o " + users[uid].getNick() );
							}							
						}

					} else {
						tellUser(userID, raws.r403( users[userID].getNick(), tmpChan ));
					}
					
					
					
			
		}
		
		
		

			
	} else if (command.equals(I_CHAN_JOIN)) {
		if (userID == hubID) {
			//a user on another server has joined a channel here..
			//inform the users in that channel
			
			//CHAN_JOIN userMask #channel reason
			if (st.countTokens() >= 2) {
				
				String um = st.nextToken();
				String n = um.substring(0, um.indexOf("!"));
				String chan = st.nextToken();
				String key = "";
				if (st.hasMoreTokens()) { key = " " + st.nextToken(); }
				
				int cID = is_channel( chan );
				if (cID != -1) {
					tellUsersInChan( cID, um + " JOIN :" + chan);
					channels[cID].addXuser( um );				
				}
				
			}		
			
		}	
	
	
	} else if (command.equals(I_CHAN_PART)) {
		if (userID == hubID) {
			//CHAN_PART userMask #channel reason
			if (st.countTokens() >= 2) {
				String um = st.nextToken();
				String chan = st.nextToken();
				String reason = "";
				if (st.hasMoreTokens()) {
					reason = cmd.substring(command.length() + um.length() + chan.length() + 3);
					if (!st.nextToken().startsWith(":")) { reason = ":" + reason; }
					reason = " " + reason;
				}
				
				int cID = is_channel(chan);
				if (cID != -1) {
					tellUsersInChan( cID, um + " PART " + chan + reason);
					channels[cID].removeXuser( um );
				}			
			}			
		}
	
	
	
	} else if (command.equals(I_USER_QUIT)) {
		if (userID == hubID) {
			//USER_QUIT  userMask  chans  quit-message
			if (st.countTokens() >= 3) {
				String um = st.nextToken();
				String chanStr = st.nextToken();
				String quitMsg = cmd.substring(command.length() + um.length() + chanStr.length() + 3 );
				quitMsg = quitMsg.trim();
				if (!quitMsg.startsWith(":")) { quitMsg = ":" + quitMsg; }
				Vector notifys = new Vector();
				Vector chanMembers = new Vector();
				int cID = -1;
				
				StringTokenizer chans = new StringTokenizer( chanStr, "," );
				
				while (chans.hasMoreTokens()) {
					//take note of all users in chan
					cID = is_channel( chans.nextToken() );
					if (cID != -1) {
						
						//put the id of every user in this chanel into the notifys
						chanMembers = channels[cID].getMembers();
						for (int mc=0; mc<chanMembers.size(); mc++) {
							if (!notifys.contains( (String)chanMembers.elementAt(mc) )) {
								 notifys.add( (String)chanMembers.elementAt(mc) );
							}
						}
						
						
						//tellUsersInChan( cID, um + " QUIT " + quitMsg );
					}
				}

				//inform the appropriate people.
				
				tellServices( um + " QUIT " + quitMsg );
				
				int tmpID = -1;
				String sID = "";
				for (int i=0; i<notifys.size(); i++) {
					sID = (String)notifys.elementAt(i);
					sID = sID.substring(0, sID.indexOf(" "));
					tmpID = Integer.parseInt( sID );
					if (tmpID != userID) {
						tellUser(tmpID, um + " QUIT " + quitMsg);
					}
				}
				
			}	
			
			
				


			
			
			
			
			
		}
	
	
	} else if (command.equals(I_DISABLE_WEBREPORT)) {
		if (userID == hubID) {
				if (wc != null) {
					wc.stopThread();
					wc = null;
				}
		}
	
	
	
	// ----------------------------------------




















	} else {
		//defauls case - unknown command
		if (users[userID].isRegistered()) {
			tellUser(userID, ":" + serverName + " 421 " + users[userID].getNick() + " :" + command + " Unknown Command");
		}
	}




  }












  public String realUserMask( int UserID ) {
	return ":" + users[UserID].getNick() + "!" + users[UserID].getIdent() + "@" + getRealHostname(UserID);
  }

  public String userMask( int UserID ) {
	return ":" + userMaskNC( UserID );
  }

  public String userMaskNC( int UserID ) {
	return users[UserID].getNick() + "!" + users[UserID].getIdent() + "@" + users[UserID].getHostname();
  }
  


  public void tellUserNumeric( int UserID, int num, String strMsg ) throws IOException {
	tellUser(UserID, ":" + serverName + " " + num + " " + users[UserID].getNick() + " :" + strMsg);
  }


  public void welcomeUser( int userID ) throws IOException {
	String userNick = users[userID].getNick();

	if (userID == hubID) { tellUser(hubID, O_SERVERDESC + " " + serverName + " " + serverDescription ); }
	
	tellUser( userID, ":" + serverName + " 001 " + userNick + " :Welcome to " + serverName + " " + userMask(userID) );
	tellUser( userID, ":" + serverName + " 002 " + userNick + " :Your host is " + serverName + " running version " + version + "");
	tellUser( userID, ":" + serverName + " 003 " + userNick + " :This server was created on " + serverCreationDate );

	//mirc reads the value from the 004 numeric. if >=5.5 then it thinks it's an ircx server.
	tellUser( userID, ":" + serverName + " 004 " + userNick + " IRCXY-" + version + " iox biklmnopqrstvw" );

	tellUser( userID, ":" + serverName + " 005 " + userNick + " IRCv4 PROTOCOL=IRCv4 CHANMODES=bw,k,lmnopqrstvi MAXCHANNELS=" + max_channels
															 + " NICKLEN=" + maxNickLen
															 + " TOPICLEN=" + maxTopicLen
															 + " CHANTYPES=# PREFIX=(qov).@+ CASEMAPPING=IRCv4 COMPLIANT=IRCv4 :are available on this server");



	if (userID != hubID) {
		sendLUsers(userID);
		sendMOTD(userID);

		//send the welcome lines from ircx.conf
		String tmp = welcomeMsg;
		while (tmp.indexOf("\r\n") != -1) {
			noticeUser(userID, tmp.substring(0, tmp.indexOf("\r\n")));
			tmp = tmp.substring(tmp.indexOf("\r\n")+2);
		}


		if (users[userID].isSysop() != 5) {
			//force the user to join a channel here.
			if (forceJoin.length() > 0) {
				noticeUser(userID, "The server is forcing you to join: " + forceJoin);
				handleCommand( "JOIN " + forceJoin, userID );
			}
			tellServices( "USERCONNECT :" + users[userID].getNick() + "!" + users[userID].getIdent() + "@" + getRealHostname(userID) + " " + serverName);
		} else {
			//get a list of opers, and pass it to the services.
			String opers = "";
			for (int i=0; i < (max_connections); i++) {
				if (users[i].isSysop() > 0) { opers+= " " + users[i].getNick(); }
			}
			tellUser(userID, "OPERS:" + opers);
		}
		noticeAllWithFlag(users[userID].getNick() + " is connecting from " + getRealHostname(userID), "c");


		
		if (hubID != -1) {
			tellUser(hubID, O_USERCONNECTING + " " + getWho(userID) );   //users[userID].getNick());
		}
		
	} else {

			welcomeHub();
	
	}



  }

  public void tellUserMustRegister( int userID ) throws IOException {
	tellUser( userID, ":" + serverName + "  451 " + users[userID].getNick() +  " :You have not registered!" );
  }





  public void tellAll( String str ) throws IOException {
	for (int index=0; index < (max_connections); index++) {
		if (users[index].ID() != -1) {
			tellUser(index, str);
		}
	}
  }

  public void tellLocalServices( String str ) throws IOException {
	for (int index=0; index<(max_connections); index++) {
		if ( (users[index].ID() != -1) && (users[index].isSysop() == 5) )
			tellUser(index, "SD " + str);
	}  	
  }
  public void tellServices( String str ) throws IOException {
	if (hubID != -1) {
		tellUser(hubID, O_PASS_SERVICES + " " + str);
	} else {
		for (int index=0; index<(max_connections); index++) {
			if ( (users[index].ID() != -1) && (users[index].isSysop() == 5) )
				tellUser(index, "SD " + str);
		}
	}
  }

  public void noticeOpers( String str ) throws IOException {
	for (int index=0; index<max_connections; index++) {
		if ( (users[index].ID() != -1) && (users[index].isSysop()>0) )
			noticeUser(index, str);
	}
  }

  public void noticeAllWithFlag( String str, String flag ) throws IOException {
	for (int index=0; index<(max_connections); index++) {
		if ( (users[index].ID() != -1) && (users[index].getModes().indexOf(flag) != -1) )
			noticeUser(index, str);
	}
  }


  public void noticeAll( String str ) throws IOException {
	for (int index=0; index < (max_connections); index++) {

		if (users[index].ID() != -1) {
			noticeUser(index, str);
		}
	}
  }






  public void tellUsersInChan( int chanID, String str ) throws IOException {
	int tmpID;
	for (int i=0; i< channels[chanID].membercount(); i++) {
		tmpID = channels[chanID].getMemberID(i);
		if (tmpID != -1) {
			tellUser(tmpID, str);
		}
	}
  }

  //tell ircx users one thing, and ircd users another...
  public void tellUsersInChan( int chanID, String strD, String strX ) throws IOException {
	int tmpID;
	for (int i=0; i< channels[chanID].membercount(); i++) {
		tmpID = channels[chanID].getMemberID(i);
		if (tmpID != -1) {
			if (users[tmpID].ircx) { tellUser(tmpID, strX); }
			else				   { tellUser(tmpID, strD); }
		}
	}  	
  }



  public void tellUsersInChanNCR( int chanID, String str ) throws IOException {
	int tmpID;
	for (int i=0; i< channels[chanID].membercount(); i++) {
		tmpID = channels[chanID].getMemberID(i);
		if (tmpID != -1) {
			tellUserNCR(tmpID, str);
		}
	}
  }



  public void tellOwnersInChan( int chanID, String str) throws IOException {
	int tmpID;
	for (int i=0; i< channels[chanID].membercount(); i++) {
		tmpID = channels[chanID].getMemberID(i);
		if ((tmpID != -1) && (channels[chanID].getMemberStatus(i) == 3)) {
			tellUser(tmpID, str);
		}
	}
  }

  public void tellOpsInChan( int chanID, String str) {
	int tmpID;
	for (int i=0; i< channels[chanID].membercount(); i++) {
		tmpID = channels[chanID].getMemberID(i);
		if ((tmpID != -1) && (channels[chanID].getMemberStatus(i) >= 2)) {
			tellUser(tmpID, str);
		}
	}
  }





  public void tellUsersInChanButOne( int chanID, int userID, String str ) {
	int tmpID;
	for (int i=0; i< channels[chanID].membercount(); i++) {
		tmpID = channels[chanID].getMemberID(i);
		//System.out.println("memberID:" + tmpID);
		if ((tmpID != -1) && (tmpID != userID)) {
			tellUser(tmpID, str);
		}
	}
  }



  public String wrapXML( String str ) {
  	return "<message value=\"" + str + "\"/>" + '\u0000';
  }

  public void tellUser( int userID, String str ) {
	try {
		SocketUtil s = new SocketUtil(users[userID].getSocket());

		//PrintStream out = s.getPrintStream();
		Writer out = s.getWriterStream();

		if (users[userID].xml) { str = wrapXML( str ); }
		else					{ str = str + "\r\n"; }
			
		//out.print( str );
		out.write( str );
		out.flush();


	} catch (Exception e) {}
  }


   public void tellUserNCR( int userID, String str ) {
	try {
		SocketUtil s = new SocketUtil(users[userID].getSocket());

		PrintStream out = s.getPrintStream();
		if (users[userID].xml) { str = wrapXML( str ); }
		out.print( str );
		out.flush();

		//if (pings[userID].timedout())
		//	killUser(userID);

	} catch (Exception e) {}
  }


  public void noticeUser( int userID, String str ) throws IOException {
	try {
		SocketUtil s = new SocketUtil(users[userID].getSocket());

		PrintStream out = s.getPrintStream();
		str = ":" + serverName + " NOTICE " + users[userID].getNick() + " :" + str;
		if (users[userID].xml) { str = wrapXML( str ); }
		else					{ str = str + "\r\n"; }
		out.print( str );
		out.flush();
	} catch (Exception e) {}
  }




  /*
  public void whisperAll( String str ) throws IOException {
	for (int index=0; index < (max_connections); index++) {
		if (users[index].ID() != -1) {
			whisperUser(index, str);
		}
	}
  }
  */
  public void whisperUser( int uidTo, int cidTo, String recips, String umFrom, String str ) throws IOException {
    try {
		SocketUtil s = new SocketUtil(users[uidTo].getSocket());
		PrintStream out = s.getPrintStream();
		
		if (users[uidTo].ircx) {
			str = umFrom + " WHISPER " + channels[cidTo].getName() + " " + recips + " :" + str;
		} else {
			str = umFrom + " PRIVMSG " + users[uidTo].getNick() + " :" + str;			
		}

		if (users[uidTo].xml) { str = wrapXML( str ); }
		else					{ str = str + "\r\n"; }
		out.print( str );
		out.flush();
	} catch (Exception e) {}
  }
  
  // --
  






  
  public void handleConnection(Socket server) throws IOException {


	if ((userCount >= max_connections) && (!expandArray())) {
		//don't allow any more connections to the server
		SocketUtil s1 = new SocketUtil(server);
		PrintStream out1 = s1.getPrintStream();
		out1.print( ":" + serverName + " NOTICE * :" + serverFullNotice + " (" + max_connections + " users)\r\n" );
		out1.flush();


	try {
		Thread.sleep(5000);
	} catch (InterruptedException e) { }
	server.close();
	
		

   }else if (overCloned(server)) {
   //check if this user has exceeded the clone limit
	//inform the user without actually allowing them to connect to the server
	SocketUtil s1 = new SocketUtil(server);
	PrintStream out1 = s1.getPrintStream();
	out1.print( ":" + serverName + " NOTICE * :Exceeded maximum amount of connections from one host.\r\n" );
	out1.flush();

	noticeAllWithFlag("Clone flood from -> " + server.getInetAddress().getHostName(), "f");

	try {
		Thread.sleep(5000);
	} catch (InterruptedException e) { }
	server.close();	

   } else if (isKLined(server).length() > 0) {

	//inform the user without actually allowing them to connect to the server
	SocketUtil s2 = new SocketUtil(server);
	PrintStream out2 = s2.getPrintStream();
	out2.print( ":" + serverName + " NOTICE * :You are k-lined [" + isKLined(server) + "] Contact: " + klinemail + "\r\n" );
	out2.flush();

	noticeAllWithFlag("K-lined connection from -> " + server.getInetAddress().getHostName(), "K");

	try {
		Thread.sleep(5000);
	} catch (InterruptedException e) { }
	server.close();	

   } else {

	//get this user an ID..
	int userID = newUserID(server);
	pings[userID] = new Pinger(serverName, pingerInterval, this);
	pings[userID].startPinger(server);


	//change pinger interval here ?
	//pings[userID].setInterval( 320 );


	Socket s = users[userID].getSocket();
	BufferedReader in = new BufferedReader(new InputStreamReader( s.getInputStream(), "ISO-8859-1" ));


	String message = "";
	boolean goodUser = true;
	String killMessage = "Lost Connection";
	char cchar;
	
	try {
	    while( goodUser ) {
			message = in.readLine();
			
			//cchar = (char)in.read();
			//if ( (cchar == '\r') || (cchar == '\n') || (cchar == '\u0000') ) {
				if (message.length() > 0) {
					//process command
					if (debug) { System.out.println("id#" + userID + ":: " + message); }
					
					//Check if the user is flooding, before processing their command.
					if ((users[userID].isSysop() < 5) && flooding(userID)) {
						goodUser = false;
						tellUser(userID, "ERROR :Closing link " + users[userID].getNick() + " (Flooding)");
						killMessage = "Flooding";
		
					} else if (message.trim().equals("")) {
						//System.out.println("Blank message");
					} else if ((message != null) && (!message.equals(""))) {
							handleCommand( message, userID );
						
						
					} else {
						goodUser = false;
					}
					
					message = "";
				}
			//} else if (cchar.equals(null)) {
			//	goodUser = false;
			//} else {
			//	message+= cchar;
			//}

		}//end while
		
		
		//Terminate the socket
		

		sInpOut( server );
		
		server.close();
		//Send a quit message & free up that userID
		sendUserQuit(userID, killMessage);
		releaseUserID(userID);
		
		
	}
	catch (IOException e) {
	    //System.out.println("Eeek:: " + e);
	    	
				sInpOut( server );
			
				server.close(); // Do we need this ?

				sendUserQuit(userID, "Broken Pipe");
				releaseUserID(userID);
	}
	catch (NullPointerException e) {
	    //System.out.println("Eeek:: " + e);
	    	

				sInpOut( server );
			
				server.close();
				sendUserQuit(userID, "Lost Connection");
				releaseUserID(userID);
	}

   }



  }
//end handleConnection




	//Determine whether the user is flooding the server with commands
	public boolean flooding( int userID ) {
		boolean fld = false;
		//do not allow servicebots or higher to flood themselves off.
		//if (users[userID].isSysop() < 5) {
			if ((System.currentTimeMillis() - users[userID].getLastMsg()) <= 3000) {
				users[userID].incFloodCount();
				
				if (users[userID].floodCount() > txtFloodLimit) {
					fld = true;
				} else {
					users[userID].setLastMsg(System.currentTimeMillis());
				}			
				
			} else {
				users[userID].resetFloodCount();
				users[userID].setLastMsg(System.currentTimeMillis());
			}
		//}
		return fld;
	}



	/* ------ Nickname methods -------- */
	public boolean nickInUse( String strNick ) {
		//loop through all the nicks, if nick is already in use return true

		int index = -1;
		boolean foundNick = false;
		while (!foundNick && (index < (max_connections-1))) {
			index++;
			if ((users[index].ID() != -1) && users[index].getNick().equalsIgnoreCase(strNick)) {
				foundNick = true;
			}
		}
		return foundNick;
	}



	public boolean overCloned(Socket sock) {
		int clones = 0;
		String tmpIP = sock.getInetAddress().getHostAddress();
		String tmpAddress = sock.getInetAddress().getHostName();
		String tmpAddress2 = "";

		//find out how many clones this user has on the server currently....
		for (int i=0; i<max_connections; i++) {
			if (users[i].ID() != -1) {
				tmpAddress2 = users[i].getSocket().getInetAddress().getHostName();
				if (tmpAddress.equals(tmpAddress2))
					clones++;
			}
		}			

		//determine the appropriate i-line
		int is = iLines.size();
		String limit = "-";
		int number_limit = 1;
		String mask = "";
		
		for (int i=0; i<is; i++) {
			//limit mask
			mask = (String)iLines.elementAt( i );
			limit = mask.substring(0, mask.indexOf(":"));
			mask = mask.substring(mask.indexOf(":")+1);
			
			if (matches(mask, tmpIP)) {
				i = is;
			}
		}

		//check if this is over the relevant i-line
		if (limit.equals("*")) {
			//infinate connections from this host, i.e. can't be overcloned
			return false;
		} else {
			try {
				number_limit = Integer.parseInt( limit );
			} catch (NumberFormatException e) { number_limit = 1; }

			if (clones >= number_limit)
				return true;
			else
				return false;
		}
		
	}






	public boolean invalidNick( String strNick, int userID ) {

		//Check the nickname for invalid characters
	   if (strNick.length() > maxNickLen) {
		return true;
	   } else {
		String badChars = nickBadChars + invChars + '\u0008';
		boolean tmpBool = false;

		for (int i=0; i< badChars.length(); i++) {
			if (strNick.indexOf(badChars.substring(i,i+1)) != -1) {
				tmpBool = true;
			}
		}

		String tmp = strNick.toLowerCase();

		//if the nicknames contains those weird chr(160)'s, make sure it starts with a '


		String tmpPN = protectedNicks;
		String tmpN = "";
		if (users[userID].isSysop() == 0) {
			while ((tmpPN.length() >0) && (!tmpBool)) {
				tmpN = tmpPN.substring(0, tmpPN.indexOf(":"));
				tmpPN = tmpPN.substring(tmpPN.indexOf(":")+1);
				if (tmp.indexOf( tmpN ) != -1)
					tmpBool = true;
			}
		}

		return tmpBool;
	   }
	}

	public String nickFromMask( String mask ) {
		//UmBonGo!djf@tesx.org
		return mask.substring(0, mask.indexOf("!"));
	}


	public boolean invalidIdent( String strIdent ) {
	   if (strIdent.length() > maxIdentLen) {
		return true;
	   } else {
		String badChars = identBadChars + invChars;
		boolean tmpBool = false;

		for (int i=0; i< badChars.length(); i++) {
			if (strIdent.indexOf(badChars.substring(i,i+1)) != -1) {
				tmpBool = true;
			}
		}
		return tmpBool;
	   }
	}




	/* ------ Channel methods -------- */

	public boolean isOperOnlyChan( String chan ) {
		if (operOnlyChans.contains( chan.toLowerCase() )) {
			return true;
		} else {
			return false;
		}
	}
	public boolean isClosedChan( String chan ) {
		if (closedChans.contains( chan.toLowerCase() )) { return true; }
		else											{ return false; }
	}

	public boolean isValidChanName( String str ) {
		//check if it contains any incorrect chars.
		//check starts with a #
		//check if it's a closed channel
	   if (str.length() == 0) {
	   	return false;
	   } else if (str.length() > maxChanLen) {
			return false;
	   } else {
		if (str.charAt(0) == '#') {
		  String badChars = chanBadChars + invChars;
		  boolean tmpBool = true;

		  //illegal chars ?
		  for (int i=0; i< badChars.length(); i++) {
			if (str.indexOf(badChars.substring(i,i+1)) != -1) {
				tmpBool = false;
			}
		  }
		  
		  //closed channel?
		  //if (closedChans.contains( str.toLowerCase() )) {
		  //	tmpBool = false;
		  //}
		  
		  
		  return tmpBool;

		} else {
			return false;
		}
	   }
 	}



	public int is_channel(String strChan) {
		//check if the given channelName is that of an existing channel, return channel index
		int index = -1;
		boolean foundChan = false;
		while (!foundChan && (index < (max_channels-1))) {
			index++;
			//System.out.println("index:" + index);
			if (channels[index].getName().equalsIgnoreCase(strChan)) {
				foundChan = true;
			}
		}

		if (foundChan)
		   return index;
		else
		   return -1;

	}//end of is_channel()




	public int newChannelID(String strChan) {
		//go through the array and find the first available (name = "") index.
		int index = -1;
		boolean foundFree = false;

		while (!foundFree && (index < (max_channels-1))) {
			index++;

			if (channels[index].getName().equals("")) {
				//found a free one, reserve it, and break loop
				channels[index].setName(strChan);
				channels[index].init_members();
				chanCount++;
				foundFree = true;
			}
		}

		if (foundFree) {
			return index;
		} else {
			return -1; //maxed out ! no free channels.
		}
 	}//end newChannelID()



	public void releaseChannel(String strChan) {
		//go through the array and find the channel.
		int index = -1;
		boolean foundChan = false;

		while (!foundChan && (index < (max_channels+1))) {
			index++;

			if (channels[index].getName().equalsIgnoreCase(strChan)) {
				channels[index].cleanChannel();
				foundChan = true;
				chanCount--;
			}
		}

	}//end releaseChannel








	public void sendNamesList(int userID, int chanID) throws IOException {

		if ((hubID != -1) && (userID != hubID)) {
			tellUser(hubID, O_REQUEST_NAMES + " " + userID + " " + users[userID].getNick() + " " + channels[chanID].getName() + " " + users[userID].ircx);
		} else {
			String strStart = ":" + serverName + " 353 " + users[userID].getNick() + " = " + channels[chanID].getName() + " :";
			String strLNames = "";
			int tmpMID;
			for (int i=0; i<channels[chanID].membercount(); i++) {
				tmpMID = channels[chanID].getMemberID(i);
				if (tmpMID != -1) {
					strLNames = channels[chanID].getMemberStatusStr(i,users[userID].ircx) + users[tmpMID].getNick() + " " + strLNames;
				}
			}
	
			strLNames = strLNames.trim();

			String strEnd = ":" + serverName + " 366 " + users[userID].getNick() + " " + channels[chanID].getName() + " :End of /NAMES list.";

			tellUser(userID, strStart + strLNames);
			tellUser(userID, strEnd);
		}

	}


	public boolean isMember(int userID, int chanID) {

		//loop through the channel list and find out if this user is in it.
		int index = -1;
		boolean foundUser = false;


		for (int i=0; i<channels[chanID].membercount(); i++) {
			if (channels[chanID].getMemberID(i) == userID) {
				foundUser = true;
				i = channels[chanID].membercount();
			}
		}
		return foundUser;

	}




	public void sendUserQuit(int userID, String quitMsg) throws IOException {
		//First check if this is a valid user, if not break out method.
		//Must go through each channel, if user is on channel -> inform everyone of his quit.
		String chans = "";
		Vector notifys = new Vector(); //userID's of the users that require to be notified of the quit.
		Vector chanMembers = new Vector();

		if (users[userID].ID() != -1) {

			String um = userMask(userID);
			
			
			//Check if it was a ping timeout
			if (pings[userID].timedout()) {
				quitMsg = "Ping Timeout";
			}
			
						
			for (int i=0; i < max_channels; i++) {

				if (channels[i].getName() != "") {
					if (isMember(userID, i)) {
						//put the id of every user in this chanel into the notifys
						chanMembers = channels[i].getMembers();
						for (int mc=0; mc<chanMembers.size(); mc++) {
							if (!notifys.contains( (String)chanMembers.elementAt(mc) )) {
								 notifys.add( (String)chanMembers.elementAt(mc) );
							}
						}

						channels[i].remove_user(userID);
						chans = chans + "," + channels[i].getName();
						//if the channel is now empty, destroy it!
						if (channels[i].membercount() == 0) {
							channels[i].cleanChannel();
							chanCount--;
						}

					}
				}

			}
			
			//inform the appropriate people.
			int tmpID = -1;
			String sID = "";
			for (int i=0; i<notifys.size(); i++) {
				sID = (String)notifys.elementAt(i);
				sID = sID.substring(0, sID.indexOf(" "));
				tmpID = Integer.parseInt( sID );
				if ((tmpID != userID) && (tmpID != hubID)) {
					tellUser(tmpID, um + " QUIT :" + quitMsg);
				}
			}
			
			
			tellServices( um + " QUIT :" + quitMsg );
			
			if (userID == hubID) {
				//we lost the hub !
				//System.out.println("HUB QUIT! Reason: " + quitMsg);
				noticeAll( "Connection to the HUB has been lost." );
			
			} else if (hubID != -1) {
				if (chans.length() == 0) {
					//check if user has a nick.  i.e. they might just have send USER ident ... information, then QUIT.
					if (users[userID].getNick().length() > 0) {
						tellUser( hubID, O_IDLER_QUIT + " " + users[userID].getNick() );
					}
				} else {
					if (quitMsg.equals("")) { quitMsg = ":"; }
					tellUser( hubID, O_USERQUIT + " " + um + " " + chans.substring(1) + " " + quitMsg );
				}
			}
			
			//put user in whowas listing
			if (userID != hubID) {
				makeWhoWas( userID );
			}
			

		}

	}



	public void sendUserNickChange(int userID, String usrMask, String newNick) throws IOException {
		//we must create a list of all the userID's that need informed of this user's nick change
		/*
		create a tmp array[max_connections], init all entries to 0. Go through the channels the user
		is on and put a 1 in the array index corresponding to a user that needs to be informed.
		Lastly, go through the array, informing each of the 1's (users) in the array.
		*/
		int tmpUID;
		int ids[] = new int[max_connections];
		for (int a=0; a<ids.length; a++)
			ids[a] = 0;

		ids[userID] = 1; //must inform the user that is actually changing nick.
		String chans = "";
		

		for (int i=0; i< max_channels; i++) {
			if (channels[i].getName() != "") {
				if (isMember(userID, i)) {
					chans = chans + " " + channels[i].getName();
					for (int j=0; j<channels[i].membercount(); j++) {
						tmpUID = channels[i].getMemberID(j);
						if (tmpUID != -1)
							ids[tmpUID] = 1;
					}
				}
			}
		}
		//relevant indexes are now set
		
		for (int k=0; k <ids.length; k++) {
			if (ids[k] == 1)
				tellUser(k, usrMask + " NICK " + newNick);
		}

		if ((hubID != userID) && (hubID != -1))
			tellUser(hubID, O_NICKCHANGE + " " + usrMask + " " + newNick + chans );


		tellServices( usrMask + " NICK " + newNick );

	}
	
	public String whoisChanList(int userID) { return whoisChanList(userID, true); }
	public String whoisChanList(int userID, boolean ircx) {
		String tmpChans = "";
		String userNick = users[userID].getNick();
		for (int i=0; i< max_channels; i++) {
			if (channels[i].getName() != "") {
				if (isMember(userID, i)) {
					tmpChans = tmpChans + " " + channels[i].getMemberStatusStr(channels[i].userArrayPos(userID), ircx) + channels[i].getName();
				}
			}
		}
		return tmpChans.trim();



	}






	public void sendChanList(int userID) throws IOException {

		if ( (userID != hubID) && (hubID != -1) ) {
			tellUser(hubID, O_SEND_LIST + " " + userID + " " + users[userID].getNick() + " 0"); // <-- 0 indicates non-listx
		} else {
			String tmpString = users[userID].getNick();
	
			tellUser(userID, ":" + serverName + " 321 " + tmpString + " Channel :Users Name");
			tmpString = ":" + serverName + " 322 " + tmpString + " ";
	
			for (int i=0; i<max_channels; i++) {
				if (channels[i].getName() != "") {
				  //if the channel is +secret, only show it to sysops.
				  if (!channels[i].ismode("s") || (users[userID].isSysop()>0)) {
					tellUser(userID, tmpString + channels[i].getName() + " " + channels[i].membercount() + " :[" + channels[i].modeString() + "] " + channels[i].getTopic());
				  }
				}
			}
	
			tellUserNumeric(userID, 323, "End of /LIST");
		}
	}


	public void sendChanListX(int userID) throws IOException {


		if ( (userID != hubID) && (hubID != -1) ) {
			tellUser(hubID, O_SEND_LIST + " " + userID + " " + users[userID].getNick() + " 1"); // <-- 1 indicates listx, not list.
		} else {
			String tmpString = users[userID].getNick();
	
			tellUser(userID, ":" + serverName + " 811 " + tmpString + " :Start of ListX");
			tmpString = ":" + serverName + " 812 " + tmpString + " ";
	
			for (int i=0; i<max_channels; i++) {
				if (channels[i].getName() != "") {
				  //if the channel is +secret, only show it to sysops.
				  if (!channels[i].ismode("s") || (users[userID].isSysop()>0)) {
					tellUser(userID, tmpString + channels[i].getName() + " " + channels[i].modeStringLX() + " " + channels[i].membercount() + " " + channels[i].getLimit() + " :" + channels[i].getTopic());
				  }
				}
			}
	
			tellUserNumeric(userID, 817, "End of ListX");
		}

	}







	public boolean isBannedFromChan(int userID, int chanID) {
		return channels[chanID].hasAccess( "DENY", users[userID].getNick() + "!" + users[userID].getIdent() + "@" + users[userID].getHostname(), true );
	}

	public boolean hasOwnerAccess(int userID, int chanID) {
		return channels[chanID].hasAccess( "OWNER", users[userID].getNick() + "!" + users[userID].getIdent() + "@" + users[userID].getHostname(), true );
	}

	public boolean hasHostAccess(int userID, int chanID) {
		return channels[chanID].hasAccess( "HOST", users[userID].getNick() + "!" + users[userID].getIdent() + "@" + users[userID].getHostname(), true );
	}

	public boolean hasVoiceAccess(int userID, int chanID) {
		return channels[chanID].hasAccess( "VOICE", users[userID].getNick() + "!" + users[userID].getIdent() + "@" + users[userID].getHostname(), true );
	}



	public void sendLUsers( int userID ) throws IOException {

		tellUserNumeric(userID, 251, "There are " + trueUserCount(1) + " user(s) and 0 invisible on " + netServerCount + " server(s)");
		tellUser(userID, ":" + serverName + " 252 " + users[userID].getNick() + " " + operCount + " :operator(s) online");
		tellUser(userID, ":" + serverName + " 254 " + users[userID].getNick() + " " + chanCount + " :channels formed");

		int connectedServers = 0;
			if (hubID != -1) { connectedServers = 1; }
		tellUserNumeric(userID, 255, "I have " + userCount + " client(s) and " + connectedServers + " server(s)");
		
		tellUserNumeric(userID, 265, "Current local users: "  + trueUserCount(0) + " Max: " + trueUserCount(2));
		tellUserNumeric(userID, 266, "Current global users: " + trueUserCount(1) + " Max: " + trueUserCount(3));


	}

	public int trueUserCount( int w ) {
							int T_LOCAL = 0;
							int T_GLOBAL = 1;
							int T_HIGHEST_LOCAL = 2;
							int T_HIGHEST_GLOBAL = 3;
		int uc = 0;
		
		if (w == T_LOCAL) {
			uc = userCount;
			if (hubID != -1) { uc--; }
		} else if (w == T_GLOBAL) {
			uc = userCount;
			if (hubID != -1) { uc = globalUserCount; }
		} else if (w == T_HIGHEST_LOCAL) {
			uc = highestUserCount;
			if (hubID != -1) { uc--; }
		} else if (w == T_HIGHEST_GLOBAL) {
			uc = highestUserCount;
			if (hubID != -1) { uc = globalHighestUserCount; }
		}
		
		return uc;
	}


	public void sendMOTD( int userID ) throws IOException {
		tellUserNumeric(userID, 375, "- " + serverName + " Message of the Day -");

		for (int i=0; i<motdLines; i++)
			tellUserNumeric(userID, 372, "- " + motd[i]);

		tellUserNumeric(userID, 376, "End of /MOTD command");
	}




	public boolean matches( String tmpMask, String tmpNick ) {
		boolean match = true;
		String nick = tmpNick.toLowerCase();
		String mask = tmpMask.toLowerCase();
		int mlen = mask.length();
		int npos = -1;


		for (int i = 0; i < mlen; i++) {
			if (mask.charAt(i) != '*') {
				npos = nick.indexOf(mask.charAt(i));
				if ( npos == -1 ) {
					match = false;
					i = mlen;
				}
				else
					nick = nick.substring(npos);
			}
		}
		return match;
	}


	public String isKLined( Socket server ) {
	  if (klines.size() > 0) {
		String userhost = server.getInetAddress().getHostName();
		String userip = server.getInetAddress().getHostAddress();

		//go through the kline array and find out if they are klined
		String isKd = "";
		KLine k;
		
		for (int i=0; i < klines.size(); i++) {
			k = (KLine)klines.elementAt(i);
			if (matches( k.mask, userhost ) || matches( k.mask, userip )) {
				if (k.reason.length() > 0) { isKd = k.reason; }
				else { isKd = "*"; }
				i = klines.size();
			}
		}
		return isKd;

	  } else {
		return "";
	  }
	}


	public boolean isKLined( String mask ) {
		KLine k;
		boolean match = false;
		for (int i=0; i<klines.size(); i++) {
			k = (KLine)klines.elementAt(i);
			if (mask.equals(k.mask)) {
				match = true;
				i = klines.size();
			}
		}
		return match;
		//return (klines.contains( mask ));
	}

	//public int klineIndex( String mask ) {
	//  return (klines.indexOf( mask ));
	//}


	public void listKLines( int userID ) throws IOException {
	  KLine k;
	  boolean none = true;
	  if (klines.size() > 0) {
		for (int i=0; i < klines.size(); i++) {
			k = (KLine)klines.elementAt(i);
			if (!k.global) {
				none = false;
				noticeUser(userID, "KLine: " + k.mask + " Set by: " + k.setter + " [" + k.reason + "]");
			}		
			
		}

	  }
	  if (none) {
		noticeUser(userID, "There are no active K-Lines. See also /GLINES");
	  }
	}

	public void listGLines( int userID ) throws IOException {
	  KLine k;
	  boolean none = true;
	  if (klines.size() > 0) {
		for (int i=0; i < klines.size(); i++) {
			k = (KLine)klines.elementAt(i);
			if (k.global) {
				none = false;
				noticeUser(userID, "GLine: " + k.mask + " Set by: " + k.setter + " [" + k.reason + "]");
			}
		}

	  }
	  if (none) {
		noticeUser(userID, "There are no active G-Lines. See also /KLINES");
	  }
	}	



	public void writeKLine( KLine k ) {

	 if (kfile.exists()) {
	    try {
			PrintWriter pout = new PrintWriter(new FileWriter(kfile.getName(), true)); //append

			if (k.mask != "") {
				if (k.global) { pout.print( "g" ); }
				else		  { pout.print( "k" ); }
				pout.println("-line " + k.mask + " " + k.setter + " " + k.reason);
			}
			pout.close();

			//re-initialise the klines array to make the new one active
			initKLines();
			klines.add( k );

	    } catch(IOException e) {}

	 } else {
		//System.out.println("No k-line file found: " + kfile);
	 }


	}


	public void unwriteKLine( String mask ) {
				unwriteKLine( mask, false );
	}
	public void unwriteKLine( String mask, boolean global ) {
		//go through the kline file, reading it into a vector.
		//Write it out, ommitting this k-line
		if (kfile.exists()) {
			String prebit = "k-line";
			if (global) { prebit = "g-line"; }
			
			Vector v = new Vector();

			try {
				FileReader s0 = new FileReader(kfile);
				BufferedReader s1 = new BufferedReader(s0);


				while (true) {
				  String line = s1.readLine();
				  if (line == null)
					break;
		
				  //deal with the line
				  line = line.trim();
				  boolean match = false;
				  
				  if (line.startsWith(prebit)) {
				  	String cpyLine = line;
				  	cpyLine = cpyLine.substring(prebit.length()+1);
				  	if (cpyLine.substring(0, cpyLine.indexOf(" ")).equalsIgnoreCase(mask)) {
				  		match = true;
				  	}
				  	
				  	// && line.endsWith(mask))) {
				  	//v.add( line );
				  }
				  if (!match) {
				  	v.add( line );
				  }
				  
				  // else if (v.lastElement().toString().startsWith("#")) {
				  	//a comment about the k-line ?
				  //}
				}
				
				s1.close();
				
				
				
				PrintWriter pout = new PrintWriter(new FileWriter(kfile)); //overwrite
		
				for (int i=0; i<v.size(); i++) {
					pout.println( (String)v.elementAt(i) );
				}
				pout.close();
				//klines = v;
				initKLines();

			} catch (Exception e) {}



			
		}
		
	}


	public String getWho( int userID ) {
		return users[userID].getIdent() + " " + users[userID].getHostname() + " " + serverName + " " + users[userID].getNick() + " H@ :0 " + users[userID].getRealName();
	}
	public void makeWhoWas( int userID ) {
		if (maxWhoWas > 0) {
			whos.addIAL( users[userID].getNick(), users[userID].getIdent(), users[userID].getHostname(), serverName, users[userID].getRealName(), "H@", ":0");
			whos.removeIAL( users[userID].getNick() );
		}		
	}



	public boolean validKLine( String mask ) {
		if (mask.indexOf("**") != -1) {
			return false;
		} else {
			return true;
		}
	}


	public void killUser(int userID, String msg) {
		try {
		Socket tmp = users[userID].getSocket();
		sendUserQuit(userID, msg);
		tmp.close();
		releaseUserID(userID);
		} catch (IOException e) {}
	}



	public void killUser(int userID) {
		try {
		Socket tmp = users[userID].getSocket();
		sendUserQuit(userID, "Error: Ping Timeout");
		tmp.close();
		releaseUserID(userID);
		} catch (IOException e) {}
	}


	public String formatBanMask( String mask ) {
		if ((mask.indexOf("!") == -1) && (mask.indexOf("@") == -1)) {
			
			if (mask.indexOf(".") == -1) {	mask = mask + "!*@*"; /*assume nickname*/ }
			else {							mask = "*!*@" + mask; /*assume hostname*/ }
		}
		else if ((mask.indexOf("!") == -1) && (mask.indexOf("@") != -1)) {
			if (mask.startsWith("@")) { mask = "*" + mask; }
			mask = "*!" + mask;
		}
		else if ((mask.indexOf("!") != -1) && (mask.indexOf("@") == -1)) {
			if (mask.endsWith("!")) { mask = mask + "*"; }
			mask = mask + "@*";
		}

		return mask;	
	}


	public void setHostMask(int tmpID) {
		String tmpIP;
		if (showIPs) {
		 tmpIP = users[tmpID].getSocket().getInetAddress().getHostAddress();
		 if (maskIPs) {


		  StringTokenizer st = new StringTokenizer(tmpIP, ".");
		  tmpIP = st.nextToken() + "." + st.nextToken() + "." + st.nextToken() + ".";

		  String maskBit = st.nextToken();
		  
		  
		  if (maskType == 1) {
		  	// X's

			  if (maskBit.length() == 1)
				maskBit = "X";
			  else if (maskBit.length() == 2)
				maskBit = "XX";
			  else
				maskBit = "XXX";
	
			  tmpIP = tmpIP + maskBit;
			  
		  } else if (maskType == 2) {
		  	tmpIP += "1";
		  }

		 }
		 users[tmpID].setHostname(tmpIP);


		}
		else {
			tmpIP = users[tmpID].getSocket().getInetAddress().getHostName();
			if (maskIPs) {
			  if (tmpIP.indexOf(".") != -1) {
				  tmpIP = ipMask + tmpIP.substring(tmpIP.indexOf("."));
			  } else {
			  	tmpIP = ipMask + tmpIP;
			  }
			}

		 users[tmpID].setHostname(tmpIP);
		}

	}


	public String starMask( String mask ) {
		String tmpIP = mask.substring(mask.indexOf("@")+1);
		if (showIPs) {
			StringTokenizer st = new StringTokenizer(tmpIP, ".");
			tmpIP = st.nextToken() + "." + st.nextToken() + ".*";
		} else {
			tmpIP = "*" + tmpIP.substring(tmpIP.indexOf("."));
		}
		return mask.substring(0, mask.indexOf("@")+1) + tmpIP;

	}






	public String getRealHostname( int tmpID ) {
		if (showIPs) {
		 return  users[tmpID].getSocket().getInetAddress().getHostAddress();
		}
		else {
		 return users[tmpID].getSocket().getInetAddress().getHostName();
		}



	}
	
	
	
	// ------------------ HUB methods --------------------
	public boolean hasCallback(String str) {
		return ( rav.contains( str.toLowerCase() ) );
	}
	public void makeCallback(String str) {
		if (!hasCallback(str)) {
			rav.addElement( str.toLowerCase() );
		}
	}
	public boolean removeCallback(String str) {
		boolean test = rav.removeElement( str.toLowerCase() );
		return test;
	}
	public void removeAllCallbacks() {
		rav.removeAllElements();
	}
	
	
	public void welcomeHub() {
		String ul = "";
		String tmpWho = "";
		for (int i=0; i< max_connections; i++) {
			if (users[i].ID() != -1) {
				tmpWho = users[i].getIdent() + " " + users[i].getHostname() + " " + serverName + " " + users[i].getNick() + " H@ :0 " + users[i].getRealName();      //djf 62.31.114.78 ntsecurity.nu bob H@ :0 test
				ul = ul + " " + tmpWho.length() + " " + tmpWho;
			}			
		}
		tellUser( hubID, O_USERLIST + ul );
		
		if (hubcompress) { tellUser(hubID, O_COMPRESS_DATA); }

		//tell the hub about g-lines
		KLine k;
		for (int i=0; i<klines.size(); i++) {
			k = (KLine)klines.elementAt(i);
			if (k.global) {
				tellUser(hubID, O_GLINES + " " + k.mask + " " + k.setter + " " + k.reason);
			}
		}
		
	}
	
    public void sendHubChanLists() {
    	
		String cl = "";
		for (int index=0; index < (max_channels-1); index++) {
			cl = "";
			if (!channels[index].getName().equals("")) {
				cl = channels[index].getName();
			
				int tmpID;
				for (int i=0; i<channels[index].membercount(); i++) {
					tmpID = channels[index].getMemberID(i);
					if (tmpID != -1) {
						cl = cl + " " + channels[index].getMemberStatusStr(i) + users[tmpID].getNick();
					}
				}
				tellUser( hubID, O_CHANLIST + " " + cl );
		
				//send chan settings to hub
				if (!channels[index].getTopic().equals("")) {
					tellUser( hubID, O_CHANSETTING + " ADD TOPIC " + channels[index].getName() + " " + channels[index].whoSetTopic() + " " + channels[index].topicTimeStamp() + " :" + channels[index].getTopic() );
				}
				if (!channels[index].onjoin().equals("")) {
					tellUser( hubID, O_CHANSETTING + " ADD ONJOIN " + channels[index].getName() + " " + channels[index].onjoin() );
				}
				if (!channels[index].onpart().equals("")) {
					tellUser( hubID, O_CHANSETTING + " ADD ONPART " + channels[index].getName() + " " + channels[index].onpart() );
				}				
				if (!channels[index].ownerkey().equals("")) {
					tellUser( hubID, O_CHANSETTING + " ADD OWNERKEY " + channels[index].getName() + " " + channels[index].ownerkey() );
				}			
				if (!channels[index].hostkey().equals("")) {
					tellUser( hubID, O_CHANSETTING + " ADD HOSTKEY " + channels[index].getName() + " " + channels[index].hostkey() );
				}
				if (!channels[index].modeStringLX().equals("")) {
					tellUser( hubID, O_CHANSETTING + " ADD CHANMODES " + channels[index].getName() + " " + channels[index].modeStringLX() );
				}
				if (channels[index].getLimit() > 0) {
					tellUser( hubID, O_CHANSETTING + " ADD LIMIT " + channels[index].getName() + " " + channels[index].getLimit() );
				}
				
			}		
		}
		

		//now send the list of sysops
		for (int index=0; index < (max_connections-1); index++) {

			if ((users[index].ID() != -1) && (users[index].isSysop() > 0) && (users[index].isSysop() != 10)) {
				tellUser(hubID, O_OPER + " " + users[index].getNick() + " " + users[index].isSysop());
			}
		}
		
    	
    }	
	
	
	
	public void cleanNetsplit() throws IOException {
		//remove all the callbacks, ready for the hub reconnecting
		removeAllCallbacks();
		globalUserCount = 0;
		globalHighestUserCount = 0;
		netServerCount = 1; //only us left
		
		//go through every channel, and remove xUsers
		String tmpMask = "";
		for (int index=0; index < (max_channels-1); index++) {

			if (!channels[index].getName().equals("")) {
				
				
				//re-send the /names information ... 
				for (int k=0; k<channels[index].membercount(); k++) {
					sendNamesList( channels[index].getMemberID( k ), index );
				}
					
				
				//while (channels[index].xUserCount() > 0) {
				//	tmpMask = channels[index].nextXuser();
				//	tellUsersInChan( index, tmpMask + " QUIT :Netsplit from hub" );
				//}

		
			}		
		}
		
	}
	
	
	public String getServerUptime() {
			long ut = (long)((System.currentTimeMillis() - sst)/1000); //time server has been up (sec).
	
			long days = (ut/86400); //gives the total number of days
			long hrs = (ut%86400); //give the remainder of hours
			long hours = (hrs/3600); // gives the total number of hours
			long min = (hrs%3600); // gives remainder of minutes and sec
			long mins = (min/60); // gives the minutes
			long sec = (min%60); // gives the remaining seconds

			return days + " days " + hours + " hrs.";
	}
	
	
	
	
	
	public String defaultSysopModes( int l ) {
		String defModes = "FWcfhkK";
			 if (l == 1) { defModes+= "o"; } //local operator
		else if (l == 2) { defModes+= "O"; } //global operator
		else if (l == 3) { defModes+= "N"; } //network admin
		else if (l == 5) { defModes+= "S"; } //services
		
		return defModes;
	}
	
	
	public String getOperLevelStr( int l ) {
		String level = "";
		
		if (l == 1) { level = " :is an IRC Operator"; }
		else if (l == 2) { level = " :is a Global Operator"; }
		else if (l == 3) { level = " :is a Network Administrator"; }
		else if (l == 5) { level = " :is a Services Bot"; }
		else if (l == 10) { level = " :is the Network Hub"; }
		
		return level;
		
	}
	
	private String compress( String str ) {
		return compresser.encode( str );
	}
	private String decompress( String str ) {
		return compresser.decode( str );
	}


}
