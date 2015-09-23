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
 
import java.util.*;
import java.io.*;


class commandProcessor {

	float compatibleTES = (float)1.317; //minimum tes version required.
	Vector servers = null;
	Vector opers = null;
	serverConnection tmpsc = null;
	serverConnection sc;
	File conffile = null;
	
	String cmd;
	String hubName = "hub";
	String hubDescription = "hub Description";
	int i = 0;
	int servercount = 0;
	String data;
	
	int highestUserCount = 0;
	public int userCount = 0;
	
	boolean halt = false;
	boolean quiet = true;
	
	ial whos = new ial(); //internal address list containing who info for all users.
	
	
	long hut = System.currentTimeMillis(); //Hub UpTime
	
	replacer compresser = new replacer();

	
	
	//Input messages - From servers to hub
	private String I_CHANLIST = 	"&1";
	private String I_USERLIST = 	"&2";
	private String I_CHANSETTING =  "&3";
	private String I_SERVERDESC =   "&4";
	private String I_NEWSERVER =	"&5";
	private String I_LISTSERVERS =  "&6";
	private String I_REMOVESERVER = "&7";
	private String I_RECONSERVER =  "&8";
	private String I_REQUEST_HUB_UPTIME = "&9";
	private String I_REQUESTLINKS = "&10";
	private String I_PASSINFO =     "&11";
	private String I_OPER =		 "&12";
	private String I_UNOPER =	   "&13";
	private String I_CLEARACCESS =  "&14";
	private String I_ADDACCESS =    "&15";
	private String I_REMACCESS =    "&16";
	private String I_GETWHOISFOR =  "&17";
	private String I_USERCONNECTING = "&18";
	private String I_CHANMODE =	 "&19";
	private String I_RUMOC =		"&20";
	private String I_SBOC =		 "&21";
	private String I_USBOC =		"&22";
	private String I_SUMOC =		"&23";
	private String I_IDLER_QUIT =   "&24";
	private String I_USERQUIT =	 "&25";
	private String I_REQUEST_NICKCHANGE = "&26";
	private String I_NICKCHANGE =   "&27";
	private String I_CHANJOIN =	 "&28";
	private String I_CHANPART =	 "&29";
	private String I_NCE =		  "&30";
	private String I_NC =		   "&31";
	private String I_PMCE =		 "&32";
	private String I_PMC =		  "&33";
	private String I_PMU =		  "&34";
	private String I_FINDTOPIC =    "&35";
	private String I_TOPICCHANGE =  "&36";
	private String I_CHANPROP = 	"&37";
	private String I_KICK =		 "&38";
	private String I_REQUEST_CHANMODES = "&39";
	private String I_REQUEST_NAMES = "&40";
	private String I_REQUEST_CHANJOIN = "&41";
	private String I_SEND_LIST		= "&42";
	private String I_KILL			 = "&43";
	private String I_BROADCAST_REQUEST= "&44";
	private String I_FRAW_REQUEST	 = "&45";
	private String I_RFR			  = "&46";
	private String I_PASS_SERVICES	= "&47";
	private String I_GONOTICE_REQUEST = "&48";
	private String I_WHO_REQUEST	  = "&49";
	private String I_REQUEST_SILENCES = "&50";
	private String I_ISON_REQUEST	 = "&51";
	private String I_WHOWAS_REQUEST   = "&52";
	private String I_REQUEST_WHISPER  = "&53";
	private String I_CLOSE_CHAN	   = "&54";
	private String I_GLINES		   = "&55";
	private String I_UNGLINE		  = "&56";
	private String I_FILESHARE		= "&57";
	private String I_FILESHARE_REPLY  = "&58";
	private String I_NOTICE_REMOTE_OPERS="&59";
	private String I_CHGHOST		  = "&60";
	private String I_DEBUG_SWITCH	 = "&61";
	private String I_COMPRESS_DATA	= "&62";
	
	//Output messages - From hub to servers
	private String O_NICKCOLLISION =		"^1";
	private String O_SENDCHANLISTS =		"^2";
	private String O_CHANCOLLISION =		"^3";
	private String O_SERVER_SPLIT_NOTICE =  "^4";
	private String O_SERVER_SPLIT_CHAN =	"^5";
	private String O_PASS_INFO =			"^6";
	private String O_NEW_OPER =			 "^7";
	private String O_CLEAR_ACCESS =		 "^8";
	private String O_ADD_ACCESS =		   "^9";
	private String O_REM_ACCESS =		   "^10";
	private String O_WHOIS_REQUEST =		"^11";
	private String O_USER_CONNECTED =	   "^12";
	private String O_NEW_USERCOUNT =		"^13";
	private String O_CHAN_MODE =			"^14";
	private String O_SUMOC =				"^15";
	private String O_SBOC =				 "^16";
	private String O_USBOC =				"^17";
	private String O_USER_QUIT =			"^18";
	private String O_NICKCHANGE_ALLOWED =   "^19";
	private String O_NICKCHANGE_DENIED =	"^20";
	private String O_NICK_CHANGE =		  "^21";
	private String O_CHAN_JOIN =			"^22";
	private String O_CHAN_PART =			"^23";
	private String O_NO_SUCH_CHANNEL =	  "^24";
	private String O_NO_SUCH_NICK =		 "^25";
	private String O_TOPIC_CHANGE =		 "^26";
	private String O_CHAN_PROP =			"^27";
	private String O_KICK_FROM_CHAN =	   "^28";
	private String O_NOT_CHAN_OP =		  "^29";
	private String O_REQUEST_CHANMODES =	"^30";
	private String O_CLONE_CHANNEL =		"^31";
	private String O_CHANJOIN_ALLOWED =	 "^32";
	private String O_DISABLE_WEBREPORT =	"^33";
	private String O_NOTICE_ALL=			"^34";
	private String O_FRAW =				 "^35"; 
	private String O_KILL =				 "^36";
	private String O_NEW_SERVER=			"^37";
	private String O_PASS_SERVICES=		 "^38";
	private String O_NOTICE_OPER=		   "^39";
	private String O_NEW_SERVERCOUNT=	   "^40";
	private String O_SILENCES_REQ=		  "^41";
	private String O_WHISPER_USER=		  "^42";
	private String O_PASS_INFO_TESHASERV=   "^43";
	private String O_NEW_GLINE=			 "^44";
	
	private void halt() { halt = true; } //stops the current command from displaying any more output to _screen_

	
	
	public commandProcessor(boolean q) {
			quiet = q;
			halt = q;

		servers = new Vector();
		opers = new Vector();
		opers.add( "nethub 10" );
	}


	public void setMaxWhoWas( int m ) {
		whos.max_dead_size = m;
	}
	public void setConfFile( File f ) {
		conffile = f;
	}

	private void addServer( serverConnection s ) {
		servers.addElement( s );
		servercount = servers.size();
		s.startSC();
		addConfLine( "server\t\t\t" + s.serverName + ":" + s.serverPort + ":" + s.hubPassword );
	}
	private void removeServer( int index, int r ) {
		if (index < servers.size()) {
			serverConnection rms = (serverConnection)servers.elementAt( index );
			rms.removeServer(r);
		}
	}


	public void setServers( Vector s ) {
		 servers = s;
		 servercount = servers.size();
	}
	//public void setOpers( Vector o   ) { opers = o; }
	public void setHubName( String h ) { hubName = h; }
	public void setHubDescription( String d ) { hubDescription = d; }


	public void serverQuit(int sid) {
		//a server has quit, handle this..
		serverConnection ds = (serverConnection)servers.elementAt(sid);
		Vector du = ds.getUserlist();
		
		userCount = userCount - du.size();
		
		//Save all the user info from this server, then clean it.
		Vector tmpUserList = ds.getUserlist();
		Vector tmpChanList = ds.getChanlist();
		String sn = ds.getServerFakeName();
		
		ds.clean();

		//remove all who's for that server.
		Who tmpWho = null;
		for (int i=0; i<whos.size(); i++) {
			tmpWho = whos.getIALWhoAt( i );
			if (!tmpWho.nick.equalsIgnoreCase("nethub") && tmpWho.server.equals( sn )) {
				whos.removeIAL( tmpWho.nick );
				i--;
			}
		}

		Vector tmpChan = null;
		Vector tmpSettings = null;
		String info = "";
		
		//Go through the other servers, nofitying them of the squit
		for (int i=0; i<servercount; i++) {
			if (i != sid) {
				tmpsc = (serverConnection)servers.elementAt(i);
				tmpsc.writeData(O_SERVER_SPLIT_NOTICE + " " + sn + " " + tmpUserList.size());
				
				//go through all the channels, and 
				for (int j=0; j<tmpChanList.size(); j++) {
					tmpChan = (Vector)tmpChanList.elementAt(j);
					tmpSettings = (Vector)tmpChan.elementAt(0);
					info = (String)tmpSettings.elementAt(0) + " "; //channel name
					
					for (int k=1; k<tmpChan.size(); k++) {
						info+= (String)tmpChan.elementAt(k) + " ";
					}
					tmpsc.writeData(O_SERVER_SPLIT_CHAN + " " + sn + " " + info.trim());
					
				}
				
				
			}
		}

		
	}


	

	private void removeOper( String n ) {
		String tmp = "";
		n = n.toLowerCase();
		for (int i=0; i<opers.size(); i++) {
			tmp = (String)opers.elementAt(i);
			if (n.equals( tmp.substring(0, tmp.indexOf(" ")) )) {
				opers.removeElementAt(i);
				i = opers.size();
			}
		}
	}
	private void changeOperNick( String on, String nn ) {
		String tmp = "";
		on = on.toLowerCase();
		nn = nn.toLowerCase();
		for (int i=0; i<opers.size(); i++) {
			tmp = (String)opers.elementAt(i);
			if (on.equals( tmp.substring(0, tmp.indexOf(" ")) )) {
				tmp = (String)opers.elementAt(i);
				opers.removeElementAt(i);
				opers.addElement( nn.toLowerCase() + " " + tmp.substring(tmp.indexOf(" ")+1) );				
				i = opers.size();
			}
		}
	}
	
	private int findOperLevel( String n ) {
		String tmp = "";
		n = n.toLowerCase();
		int ol = 0;

		for (int i=0; i<opers.size(); i++) {
			tmp = (String)opers.elementAt(i);

			if (n.equals( tmp.substring(0, tmp.indexOf(" ")) )) {
				ol = Integer.parseInt( tmp.substring(tmp.indexOf(" ")+1) );
				i = opers.size();
			}
		}
		return ol;
	}






	public synchronized void parse( String str ) {
		i = Integer.parseInt( str.substring(0, str.indexOf(" ")) );
		data = str.substring( str.indexOf(" ") +1);
		//data = data.trim();

		sc = (serverConnection)servers.elementAt( i );
		
						//System.out.println("data: " + data);
						//process all the data here.
						StringTokenizer st = new StringTokenizer( data );
						cmd = st.nextToken();
						
						if (cmd.equals(I_USERLIST)) {
							
							ial tmpIal = new ial();
							String ul = "";
							if (data.length() > cmd.length()) { ul = data.substring(cmd.length()+1); }
							
							int tmpLen = 0;
							String tmpWho = "";
							while (ul.length() > 0) {
								//extract length
								if (ul.indexOf(" ") != -1) {
									try {
										tmpLen = Integer.parseInt( ul.substring(0, ul.indexOf(" ")) );
										ul = ul.substring( ul.indexOf(" ")+1 );
										tmpWho = ul.substring(0, tmpLen);
										ul = ul.substring( tmpLen + 1 );
										
										//djf 62.31.114.78 ntsecurity.nu bob H@ :0 test
										String id = tmpWho.substring(0, tmpWho.indexOf(" "));
													tmpWho = tmpWho.substring( tmpWho.indexOf(" ")+1 );
										String i  = tmpWho.substring(0, tmpWho.indexOf(" "));
													tmpWho = tmpWho.substring( tmpWho.indexOf(" ")+1 );
										String s  = tmpWho.substring(0, tmpWho.indexOf(" "));
													tmpWho = tmpWho.substring( tmpWho.indexOf(" ")+1 );
										String n  = tmpWho.substring(0, tmpWho.indexOf(" "));
													tmpWho = tmpWho.substring( tmpWho.indexOf(" ")+1 );
										String m  = tmpWho.substring(0, tmpWho.indexOf(" "));
													tmpWho = tmpWho.substring( tmpWho.indexOf(" ")+1 );
										String h  = tmpWho.substring(0, tmpWho.indexOf(" "));
										String r  = tmpWho.substring( tmpWho.indexOf(" ")+1 );

										
										tmpIal.addIAL( n, id, i, s, r, m, h );

									} catch (Exception e) { ul = ""; }
								} else { ul = ""; }								
							}


							
							
							//find out if we have any nick collisions
							int uc = tmpIal.size(); //sc.userCount();
							String tmpU = "";
							//serverConnection s = null;
							for (int k=0; k<tmpIal.size(); k++) {
								tmpU = tmpIal.getNickAt( k );
								
								//dont kill ourself!
								if (!tmpU.equalsIgnoreCase("nethub")) {								
									int j=0;
									boolean found = false;
									while ((j<servercount) && (!found)) {
										if (j != i) {
											tmpsc = (serverConnection)servers.elementAt(j);
											if (tmpsc.isConnected() && tmpsc.hasUser( tmpU )) {
												found = true;
												//u.removeElementAt(k);
												tmpIal.removeIAL( tmpU );
												k--;
											}
										}
										j++;
									}
								
									if (found) {
										sc.writeData(O_NICKCOLLISION + " " + tmpU);
										//userCount--;
										if (!quiet) { System.out.println( "Nick collision: " + sc.getServerName() + " - " + tmpsc.getServerName() ); }
									}
								}
							}
							
							for (int l=0; l<tmpIal.size(); l++) {
								sc.addUser( tmpIal.getNickAt(l) );
								whos.addIAL( tmpIal.getIALWhoAt(l) );
								userCount++;
								if (userCount > highestUserCount) { highestUserCount = userCount; }
							}
							
							
							
							//Request channel lists from the server
							sc.writeData( O_SENDCHANLISTS );
							
							for (int n=0; n<servercount; n++) {
								tmpsc = (serverConnection)servers.elementAt(n);
								if (n != i) {
									tmpsc.writeData( O_NEW_SERVER + " " + tmpIal.size() + " " + sc.getServerName() );
								}
								if (tmpsc.isConnected()) { tmpsc.writeData( O_NEW_USERCOUNT + " " + userCount + " " + highestUserCount ); }
							}
							
						}
						
						else if (cmd.equals(I_CHANLIST)) {
							Vector v = new Vector();
							String chan = "";
							//String nick = "";
							
							if (st.countTokens() > 0) {
								chan = st.nextToken();
								v.addElement( chan );
							}
							
							while (st.hasMoreTokens()) {
								//nick = st.nextToken();
								//v.addElement( nick.toLowerCase() );
								v.addElement( st.nextToken() );
							}
							//if (v.size() > 0) {
							//	sc.addChan( v );
							//}
				

							boolean collision = false;
							
							for (int k=0; k<servercount; k++) {
								if (i != k) {
									tmpsc = (serverConnection)servers.elementAt(k);
									if (tmpsc.isConnected() && (tmpsc.hasChan( chan ) != -1)) {
										//sc.removeChan( chan );
										collision = true;
										//sc.writeData(O_CHANCOLLISION + " " + chan);
										//if (!quiet) { System.out.println( "Channel collision: (" + chan + ") :: " + sc.getServerFakeName() + "<-->" + tmpsc.getServerFakeName() ); }
										k = servercount; //no need to keep checking, we've already removed the channel
									}
								}								
							}
				
							if (collision) {
								sc.writeData(O_CHANCOLLISION + " " + chan);
								if (!quiet) { System.out.println( "Channel collision: (" + chan + ") :: " + sc.getServerFakeName() + "<-->" + tmpsc.getServerFakeName() ); }								
							} else if (v.size() > 0) {
								sc.addChan( v );
							}
							
							
						}
						
						
						
						else if (cmd.equals(I_CHANSETTING)) {
							//CHAN_SETTING: add/replace/remove option  #chan  setting
							if (st.countTokens() >= 3) {
								String arm = st.nextToken();
								String option = st.nextToken();
								String chan = st.nextToken();
								String setting = data.substring(cmd.length() + arm.length() + option.length() + chan.length() + 4);
								
								for (int n=0; n<servercount; n++) {
									tmpsc = (serverConnection)servers.elementAt(n);
									
									if ( (tmpsc.isConnected() && (tmpsc.hasChan(chan.toLowerCase()) != -1) ) ) {
											tmpsc.setChanOption(chan.toLowerCase(), arm, option, setting);
									}
									
								}
								
								
							}
						}
						
						else if (cmd.equals(I_COMPRESS_DATA)) {
							sc.enableCompression();
						}
						
						
						else if (cmd.equals("PING")) {
							sc.writeData("PING X");
							sc.writeData("PONG");
							halt();
						}
						
						else if (cmd.equals("PONG")) {
							halt();
						}

						
						else if (cmd.equals(I_SERVERDESC)) {
							if (st.countTokens() >= 2) {
								String sn = st.nextToken();
								String serverDesc = data.substring(cmd.length() + sn.length() +2);
								sc.setServerFakeName( sn );
								sc.setServerDescription( serverDesc );
								halt();							
							}
						}
						
						
						
						else if (cmd.equals(I_NEWSERVER)) {
							if (st.countTokens() == 3) {
								//ip, port, pass
								String new_ip = st.nextToken();
								String new_port = st.nextToken();
								String new_pass = st.nextToken();
								
								try {
									int nep = Integer.parseInt( new_port );
									addServer( new serverConnection( new_ip, nep, new_pass, this, servers.size(), quiet ) );
								} catch (Exception e) {
									
								}
							}
						}

						else if (cmd.equals(I_LISTSERVERS)) {
							if (st.countTokens() == 2) {
								String uid = st.nextToken();
								String nick = st.nextToken();
								
								String c = "";
								for (int i=0; i<servers.size(); i++) {
									tmpsc = (serverConnection)servers.elementAt(i);
									if (tmpsc.isConnected()) { c = "Connected";    }
									else					 { c = "Trying..."; }
									if (tmpsc.isRemoved())   { c = "Removed [" + tmpsc.removedReasonStr() + "]";      }


									sc.writeData( O_PASS_INFO + " " + uid + " NOTICE " + nick + " :Server " + i + " :: " + tmpsc.getServerFakeName() + " (" + tmpsc.serverName + ":" + tmpsc.serverPort + ") (" + c + ")" );
								}
									
								sc.writeData( O_PASS_INFO + " " + uid + " NOTICE " + nick + " :End of Servers List" );
							}							
						}

						else if (cmd.equals(I_REMOVESERVER)) {
							if (st.countTokens() == 1) {
								try {
									int index = Integer.parseInt( st.nextToken() );
									removeServer( index, 2 );
								} catch (Exception e) {}
							}
						}
						else if (cmd.equals(I_RECONSERVER)) {
							if (st.countTokens() == 1) {
								try {
									int index = Integer.parseInt( st.nextToken() );
									if (index < servers.size()) {
										tmpsc = (serverConnection)servers.elementAt(index);
										if (tmpsc.isRemoved()) {
											String sip = tmpsc.getServerName();
											int sport = tmpsc.getServerPort();
											String hp = tmpsc.getHubPass();
											tmpsc.clean();
											serverConnection ns = new serverConnection( sip, sport, hp, this, index, quiet );
											servers.setElementAt( ns, index );
											ns.startSC();																						
										}
										
									}
									

								} catch (Exception e) {}
							}							
						}
						
						
						
						else if (cmd.equals(I_FILESHARE)) {
							//I_FILESHARE command string...
							if (st.countTokens() > 0) {
								String nick = st.nextToken();
								boolean found = false;
								int counter = 0;
								while (!found && (counter < servercount)) {
									if (counter != i) {
										tmpsc = (serverConnection)servers.elementAt(counter);
										if (tmpsc.hasUser("teshaserv") && (findOperLevel("teshaserv") == 5)) {
											tmpsc.writeData(O_PASS_INFO_TESHASERV + data.substring(cmd.length()));
											found = true;
										}
									}
									counter++;
								}
								if (!found) {
									sc.writeData("FSR " + nick + " TESHA Filesharing has been disabled.");
								}
							}							
						}
						else if (cmd.equals(I_FILESHARE_REPLY)) {
							//I_FILESHARE_REPLY nick string . . .
							if (st.countTokens() > 1) {
								String nick = st.nextToken();
								boolean found = false;
								int counter = 0;
								while (!found && (counter < servercount)) {
									if (counter != i) {
										tmpsc = (serverConnection)servers.elementAt(counter);
										if (tmpsc.hasUser(nick)) {
											tmpsc.writeData("FSR " + data.substring(cmd.length() + 1));
											found = true;
										}
									}
									counter++;
								}
							}
						}
						
						
						
						
						
						else if (cmd.equals(I_GLINES)) {
							//I_GLINES mask setter reason
							if (st.countTokens() >=1) {
								
								for (int j=0; j<servercount; j++) {
									if (i != j) {
										tmpsc = (serverConnection)servers.elementAt(j);
										tmpsc.writeData(O_NEW_GLINE + data.substring(cmd.length()));
									}
								}
							}
						}

						else if (cmd.equals(I_UNGLINE)) {
							if (st.countTokens() >=1) {
								for (int j=0; j<servercount; j++) {
									if (i != j) {
										tmpsc = (serverConnection)servers.elementAt(j);
										tmpsc.writeData("UNGLINE" + data.substring(cmd.length()));
									}
								}																
							}							
						}
						
						
						else if (cmd.equals(I_BROADCAST_REQUEST)) {
							//I_BROADCAST_REQUEST message
							if (st.countTokens() >= 1) {
								String message = data.substring(cmd.length() + 1);
								
								for (int j=0; j<servercount; j++) {
									if (i != j) {
										tmpsc = (serverConnection)servers.elementAt( j );
										tmpsc.writeData( O_NOTICE_ALL + " BROADCAST: " + message );
									}
								}
							}
							
						}
						
						
						
						
						else if (cmd.equals(I_REQUEST_WHISPER)) {
							//O_REQUEST_WHISPER	userID fromNick toChan toNick nickList message
							if (st.countTokens() >= 6) {
								String uid = st.nextToken();
								String fromNick = st.nextToken();
								String toChan = st.nextToken();
								String toNick = st.nextToken();
								String nickList = st.nextToken();
								String message = data.substring(cmd.length() + uid.length() + fromNick.length() + toChan.length() + toNick.length() + nickList.length() + 6);

								//try to find the user toNick
								boolean userExists = false;
								for (int n=0; n<servercount; n++) {
									if (n != i) {
										tmpsc = (serverConnection)servers.elementAt( n );
										if (tmpsc.hasUser( toNick )) {
											
											if (!tmpsc.userStatusOnChan( toChan, toNick ).equals("n")) {
												tmpsc.writeData( O_WHISPER_USER + " " + whos.userMask(fromNick) + " " + toChan + " " + toNick + " " + nickList + " " + message );
											} else {
												tmpsc.writeData( O_PASS_INFO + " " + uid + " 441 " + fromNick + " " + toNick + " :They aren't on that channel");
											}
											
											userExists = true;
											n = servercount;
										}
									}
								}
								
								if (!userExists) {
									sc.writeData( O_PASS_INFO + " " + uid + " 401 " + fromNick + " " + toNick + " :No such nick/channel");
								}
								
							}							
						}
						
						
						
						
						else if (cmd.equals(I_GONOTICE_REQUEST)) {
							//I_GONOTICE_REQUEST message
							if (st.countTokens() >= 1) {
								String message = data.substring(cmd.length() + 1);
								
								for (int j=0; j<servercount; j++) {
									if (i != j) {
										tmpsc = (serverConnection)servers.elementAt( j );
										tmpsc.writeData( O_NOTICE_OPER + " Global-ONOTICE: " + message );
									}
								}
							}
							
						}
						
						else if (cmd.equals(I_FRAW_REQUEST)) {
							//I_FRAW_REQUEST requester whoTo command...
							if (st.countTokens() >= 3) {
								String requester = st.nextToken();
								String whoTo = st.nextToken();
								String command = data.substring(cmd.length() + requester.length() + whoTo.length() + 3);
								
								int requesterOper = findOperLevel( requester );
								int whoToOper = findOperLevel( whoTo );
								boolean found = false;
								
								if ( (requesterOper > 0) && (requesterOper >= whoToOper) ) {
									int j = 0;
									while (!found && (j<servercount)) {
										if (i != j) {
											tmpsc = (serverConnection)servers.elementAt( j );
											if (tmpsc.hasUser( whoTo )) {
												tmpsc.writeData( O_FRAW + " " + requester + " " + whoTo + " " + command );
												found = true;
											}										
										}
										j++;
									}
									
									if (found) {
										for (int k=0; k<servercount; k++) {
											if ((i != k) && (k != (j-1))) {
												tmpsc = (serverConnection)servers.elementAt( k );
												tmpsc.writeData( O_NOTICE_OPER + " " + requester + " used FRAW on " + whoTo + " -> " + command);
											}
										}
									}
									
								}
								
							}							
						}
						
						else if (cmd.equals(I_CHGHOST)) {
							//I_CHGHOST requester nick newhost
							if (st.countTokens() >= 3) {
								String requester = st.nextToken();
								String reqID = st.nextToken();
								String toNick = st.nextToken();
								String newhost = "";
								if (st.hasMoreTokens()) { newhost = data.substring(cmd.length() + requester.length() + reqID.length() + toNick.length() + 3); }
								
								int ro = findOperLevel( requester );
								int to = findOperLevel( toNick );
								boolean found = false;
								
								if ((ro > 0) && (ro >= to)) {
									int j = 0;
									while (!found && (j<servercount)) {
										if (i != j) {
											tmpsc = (serverConnection)servers.elementAt( j );
											if (tmpsc.hasUser( toNick )) {
												tmpsc.writeData( "CHGHOST " + toNick + newhost );
												found = true;
											}										
										}
										j++;
									}
									if (found) {
										//success
										sc.writeData( O_PASS_INFO + " " + reqID + " NOTICE " + requester + " :CHGHOST: Successfull, " + toNick + " -> " + newhost );
									} else {
										//no such user
										sc.writeData( O_PASS_INFO + " " + reqID + " NOTICE " + requester + " :CHGHOST: No such user: " + toNick );
									}
								} else {
									//insufficient oper status
									sc.writeData( O_PASS_INFO + " " + reqID + " NOTICE " + requester + " :CHGHOST: You have Insufficient oper status to CHGHOST: " + toNick );
								}
						
								
							}
						}
						
						
						else if (cmd.equals(I_KILL)) {
							//I_KILL userID killer victim reason...
							if (st.countTokens() >= 4) {
								String uid = st.nextToken();
								String killer = st.nextToken();
								String victim = st.nextToken();
								String reason = data.substring( cmd.length() + uid.length() + killer.length() + victim.length() + 4 );
								
								int killerOper = findOperLevel( killer );
								int victimOper = findOperLevel( victim );
								
								if ( (killerOper > 0) && (killerOper >= victimOper) ) {
									
									for (int j=0; j<servercount; j++) {
										if (i != j) {
											tmpsc = (serverConnection)servers.elementAt( j );
											if (tmpsc.hasUser( victim )) {
												tmpsc.writeData( O_KILL + " " + killer + " " + victim + " " + reason );
											}											
										}										
									}
									
								}							
								
								
							}							
						}
						
						
						else if (cmd.equals(I_PASS_SERVICES)) {
							if (st.countTokens() >= 1) {
								String strSD = data.substring( cmd.length() + 1 );
								for (int n=0; n<servercount; n++) {
									//if (n != i) {
										tmpsc = (serverConnection)servers.elementAt( n );
										if (tmpsc.isConnected()) {
											tmpsc.writeData( O_PASS_SERVICES + " " + strSD );
										}
									//}
								}
							}
						}
						
						
						else if (cmd.equals(I_SEND_LIST)) {
							//I_SEND_LIST userID nick listx?
							if (st.countTokens() == 3) {
								String uid = st.nextToken();
								String tmpNick = st.nextToken();
								boolean isoper = (findOperLevel( tmpNick ) > 0);
								String lx = st.nextToken();
								boolean listx = (lx.equals("1"));
								Vector chaninfo = new Vector();
								String tmpInfo = "";
								String cModes = "";
								String cLimit = "";
								int    cMembers = 0;
								String cTopic = "";
								Vector chans = new Vector();
								//build a list of all channels
								
								//LISTX -->  chan-name modes membercount limit :topic
								//LIST  -->  chan-name membercount :[modes] topic
								
								if (listx) {
									chaninfo.add( "811 " + tmpNick + " :Start of ListX" );
								} else {
									chaninfo.add( "321 " + tmpNick + " Channel :Users Name");
								}
								
								serverConnection tmpsc2 = null;
								int hc = -1;
								
								for (int i=0; i<servercount; i++) {
									tmpsc = (serverConnection)servers.elementAt(i);
									for (int j=0; j<tmpsc.chanCount(); j++) {
										String tmpChan = tmpsc.getChanName(j);
										if (!chans.contains( tmpChan.toLowerCase() )) {
											chans.addElement( tmpChan.toLowerCase() );
											//add the info to chaninfo vector
											cModes = tmpsc.getChanOption( tmpChan, "CHANMODES" );
											if ((cModes.indexOf("s") == -1) || isoper) {
												if (listx) { cLimit = tmpsc.getChanOption( tmpChan, "LIMIT" ); }
												cTopic = tmpsc.getChanOption( tmpChan, "TOPIC" );
												
												if (cModes.equals("ERROR")) { cModes = "+nt"; }
												if (listx && cLimit.equals("ERROR")) { cLimit = "0"; }
	
												if (cTopic.equals("ERROR")) {
													cTopic = "";
												} else {
														cTopic = cTopic.substring( cTopic.indexOf(" ")+1 ); //remove whoSetTopic
														cTopic = cTopic.substring( cTopic.indexOf(" ")+2 ); //remove timestamp, and colon
												}
												
												cMembers = tmpsc.chanMemberCount( j );
												for (int z=0; z<servercount; z++) {
													if (z != i) {
														tmpsc2 = (serverConnection)servers.elementAt(z);
														hc = tmpsc2.hasChan( tmpChan );
														if (hc != -1) {
															cMembers += tmpsc2.chanMemberCount( hc );
														}
													}
												}
												
												
												
												if (listx) {
															tmpInfo=	"812 " + tmpNick + " " +
																		tmpChan + " " +
																		cModes  + " " +
																		cMembers    + " " +
																		cLimit  + " " +
																		":" + cTopic;
												} else {
															tmpInfo=	"322 " + tmpNick + " " +
																		tmpChan + " " +
																		cMembers    + " " +
																		":[" + cModes + "] " + cTopic;
												}
																		
												
												chaninfo.add( tmpInfo );
											} //end of if mode 's'
										}
										
									}
									
								}
								
								if (listx) {
									chaninfo.add( "817 " + tmpNick + " :End of ListX" );
								} else {
									chaninfo.add( "323 " + tmpNick + " :End of /LIST");
								}
								
								
								for (int i=0; i<chaninfo.size(); i++) {
									sc.writeData( O_PASS_INFO + " " + uid + " " + chaninfo.elementAt( i ) );
								}
								
								
							
								
							}
						}
							
							
						
						
						
						
						else if (cmd.equals(I_REQUEST_HUB_UPTIME)) {
							if (st.countTokens() == 2) {
								String uid = st.nextToken();
								String nick = st.nextToken();
								
								long ut = (long)((System.currentTimeMillis() - hut)/1000); //time server has been up (sec).
								long days = (ut/86400); //gives the total number of days
								long hrs = (ut%86400); //give the remainder of hours
								long hours = (hrs/3600); // gives the total number of hours
								long min = (hrs%3600); // gives remainder of minutes and sec
								long mins = (min/60); // gives the minutes
								long sec = (min%60); // gives the remaining seconds

								sc.writeData( O_PASS_INFO + " " + uid + " NOTICE " + nick + " :Hub has been up: " + days + " days " + hours + " hrs. " + mins + " mins. " + sec + " sec." );
							}							
						}
						
						
						
						
						
						else if (cmd.equals(I_REQUESTLINKS)) {
							if (st.countTokens() == 2) {
								String uid = st.nextToken();
								String nick = st.nextToken();
								
								for (int n=0; n<servercount; n++) {
									tmpsc = (serverConnection)servers.elementAt( n );
									
									if (tmpsc.isConnected()) {
										sc.writeData(O_PASS_INFO+ " " + uid + " 364 " + nick + " " + tmpsc.getServerFakeName() + " " + hubName + " :0 " + tmpsc.getServerDescription() );
									}
								}
								
								sc.writeData(O_PASS_INFO + " "  + uid + " 364 " + nick + " " + hubName + " " + sc.getServerFakeName() + " :0 " + hubDescription );
								sc.writeData(O_PASS_INFO + " "  + uid + " 365 " + nick + " :End of /LINKS list.");
							}
						}
						
						
						
						
						else if (cmd.equals(I_PASSINFO)) {
							if (st.countTokens() > 2) {
								//pass info from a server to another server
								//PASS_INFO: server-id  user-id  info
								String ssid = st.nextToken();
								int sid = Integer.parseInt( ssid );
								if (sid < servers.size()) {
									tmpsc = (serverConnection)servers.elementAt( sid );
									
									if (tmpsc.isConnected()) {
										String uid = st.nextToken();
										String info = data.substring( cmd.length() + ssid.length() + uid.length() + 3);
										info = info.trim();
										tmpsc.writeData( O_PASS_INFO + " "  + uid + " " + info );
									}
									
								}
								
							}
						}
						
						
						else if (cmd.equals(I_OPER)) {
							if (st.countTokens() == 2) {
								//OPER: nick  operlevel
								String nick = st.nextToken();
								String level = st.nextToken();
								
								opers.addElement( nick.toLowerCase() + " " + level);
								
								for (int n=0; n<servercount; n++) {
									if (n != i) {
										tmpsc = (serverConnection)servers.elementAt(n);
										if (tmpsc.isConnected())
											tmpsc.writeData( O_NEW_OPER + " " + nick + " " + level + " " + sc.getServerFakeName());
									}
									
								}
								
								
							}							
						}
						
						else if (cmd.equals(I_UNOPER)) {
							if (st.countTokens() == 1) {
								String nick = st.nextToken().toLowerCase();
								removeOper( nick );
							}
						}
						
						
						
						else if (cmd.equals(I_CLEARACCESS)) {
							if (st.countTokens() == 3) {
								String levels = st.nextToken();
								String chan = st.nextToken();
								String owner = st.nextToken();
								
								for (int n=0; n<servercount; n++) {
									if (n != i) {
										tmpsc = (serverConnection)servers.elementAt(n);
										if (tmpsc.isConnected() && (tmpsc.hasChan(chan.toLowerCase()) != -1)) {
											tmpsc.writeData(O_CLEAR_ACCESS + " " + levels + " " + chan + " " + owner);
										}
									}
								}								

							}
						}
						
						else if (cmd.equals(I_ADDACCESS)) {
							if (st.countTokens() >= 5) {
								String level = st.nextToken();
								String chan = st.nextToken();
								//String mask = st.nextToken();
								
								for (int n=0; n<servercount; n++) {
									if (n != i) {
										tmpsc = (serverConnection)servers.elementAt(n);
										if (tmpsc.isConnected() && (tmpsc.hasChan(chan.toLowerCase()) != -1)) {
											tmpsc.writeData(O_ADD_ACCESS + data.substring(cmd.length()));
										}
									}
								}
								
							}
						}
						
						else if (cmd.equals(I_REMACCESS)) {
							if (st.countTokens() == 4) {
								String level = st.nextToken();
								String chan = st.nextToken();
								String mask = st.nextToken();
								String owner = st.nextToken();
								
								for (int n=0; n<servercount; n++) {
									if (n != i) {
										tmpsc = (serverConnection)servers.elementAt(n);
										if (tmpsc.isConnected() && (tmpsc.hasChan(chan.toLowerCase()) != -1)) {
											tmpsc.writeData(O_REM_ACCESS + " " + level + " " + chan + " " + mask + " " + owner);
										}
									}
								}
								
							}
						}
						
						/*
						else if (cmd.equals(I_GETWHOFOR)) {
							if (st.countTokens() == 3) {
								String requester = st.nextToken();
								String whoNick = st.nextToken();
								String ruid = st.nextToken();
								//find the server that this user is on, and request whois info for them
								

								boolean found = false;
								
								for (int n=0; n<servercount; n++) {
									if (n != i) {
										tmpsc = (serverConnection)servers.elementAt(n);
										if (tmpsc.isConnected() && tmpsc.hasUser(whoNick.toLowerCase())) {
											found = true;
											tmpsc.writeData(O_WHO_REQUEST + " " + i + " " + ruid + " " + requester + " " + whoNick);
										}
									}
									
								}
								
								if (!found) {
									//the user does not exist.
									//sc.writeData(O_PASS_INFO + " "  + ruid + " 401 " + requester + " " + whoisNick + " :No such nick/channel");
									//sc.writeData(O_PASS_INFO + " "  + ruid + " 318 " + requester + " " + whoisNick + " :End of /WHOIS list.");
								}
								
							}	
							
						}
						*/
						
						
						else if (cmd.equals(I_GETWHOISFOR)) {
							if (st.countTokens() == 4) {
								String requester = st.nextToken();
								String whoisNick = st.nextToken();
								String ruid = st.nextToken();
								boolean ircx = true;
								if (st.nextToken().equals("false")) { ircx = false; }
								//find the server that this user is on, and request whois info for them
								

								int onserver = -1;
								
								for (int n=0; n<servercount; n++) {
									if (n != i) {
										tmpsc = (serverConnection)servers.elementAt(n);
										if (tmpsc.isConnected() && tmpsc.hasUser(whoisNick.toLowerCase())) {
											onserver = n;
											n = servercount;
										}
									}
									
								}
								
								if (onserver == -1) {
									//the user does not exist.
									sc.writeData(O_PASS_INFO + " "  + ruid + " 401 " + requester + " " + whoisNick + " :No such nick/channel");
									sc.writeData(O_PASS_INFO + " "  + ruid + " 318 " + requester + " " + whoisNick + " :End of /WHOIS list.");
								} else {
									//they exist on server  'onserver'
									//ask that server for whois info
									tmpsc = (serverConnection)servers.elementAt(onserver);
									tmpsc.writeData(O_WHOIS_REQUEST + " " + i + " " + ruid + " " + requester + " " + whoisNick + " " + findOperLevel(requester) + " " + ircx);
									
									
								}
								
								
							}							
						}
						
						else if (cmd.equals(I_WHOWAS_REQUEST)) {
							//i_whowas_request uid unick query count
							if (st.countTokens() == 4) {
								String uid = st.nextToken();
								String uNick = st.nextToken();
								String query = st.nextToken();
								int count = 0;
								try { count = Integer.parseInt(st.nextToken()); }
								catch (NumberFormatException e) {}
								
								
								//search the dead ial list for matches.
								Vector matches = whos.getWasMatches( query, 0 );
								String passStr = O_PASS_INFO + " " + uid;
								
								if ((matches != null) && (matches.size() > 0)) {
									Who tmpWho;
									if ((count <= 0) || (count > matches.size())) { count = matches.size(); }
									
									for (int k=0; k<count; k++) {
										tmpWho = (Who)matches.elementAt( k );

										sc.writeData(passStr + " 314 " + uNick + " " + tmpWho.get314());
										sc.writeData(passStr + " 312 " + uNick + " " + tmpWho.get312());										
									}									

								} else {								
									sc.writeData( passStr + " 406 " + uNick + " " + query + " :There was no such nickname" );
								}
								sc.writeData( passStr + " 369 " + uNick + " " + query + " :End of WHOWAS" );
		
							}
						}
						
						
						
						else if (cmd.equals(I_ISON_REQUEST)) {
							//i_ison_request uid unick nick1 nick2 nick3
							if (st.countTokens() >= 3) {
								String uid = st.nextToken();
								String uNick = st.nextToken();
								String foundNicks = "";
								String tmpNick = "";
								
								while (st.countTokens() > 0) {
									tmpNick = st.nextToken();
									
									for (int n=0; n<servercount; n++) {
										tmpsc = (serverConnection)servers.elementAt( n );
										
										if (tmpsc.hasUser( tmpNick )) {
											foundNicks+= " " + tmpsc.correctNick(tmpNick);
											n = servercount;
										}
									}
								}
								if (foundNicks.length() > 0) { foundNicks = foundNicks.substring(1); }
								sc.writeData( O_PASS_INFO + " " + uid + " 303 " + uNick + " :" + foundNicks );
								
							}							
						}
						
						
						
						else if (cmd.equals(I_WHO_REQUEST)) {
							//i_who_request uid nick query
							if (st.countTokens() == 3) {
								String ruid = st.nextToken();
								String nick = st.nextToken();
								String query = st.nextToken();
								
								//find out if query is a channel.
								//if it is, find all the nicks on that channel
								int cid = -1;
								Vector nicks = new Vector();
								Vector tmpNicks = new Vector();
								String tmpNick = "";
								for (int n=0; n<servercount; n++) {
									tmpsc = (serverConnection)servers.elementAt(n);
									if (tmpsc.isConnected()) {
										cid = tmpsc.hasChan( query );
										//System.out.println(cid);
										
										if (cid != -1) {
											tmpNicks = tmpsc.chanNicks( cid );
											for (int k=1; k<tmpNicks.size(); k++) {
												tmpNick = (String)tmpNicks.elementAt(k);
												if (tmpNick.length() > 0) {
													if ( (tmpNick.charAt(0) == '.') || (tmpNick.charAt(0) == '@') || (tmpNick.charAt(0) == '+') ) {
														tmpNick = tmpNick.substring(1);
													}
													nicks.add( tmpNick );
												}
											}
										}
									}
								}

								String tmpString = " 352 " + nick + " " + query + " ";
								if (nicks.size() > 0) {
									String tmpWho = "";
									for (int x=0; x<nicks.size(); x++) {
										tmpWho = whos.getIAL((String)nicks.elementAt(x));
										if (tmpWho.length() > 0) {
											sc.writeData(O_PASS_INFO + " "  + ruid + tmpString + tmpWho);
										}
									}
									sc.writeData(O_PASS_INFO + " " + ruid + " 315 " + nick + " " + query +  " :End of /WHO list");
								} else {
									//find out if query is the nickname of a user
									boolean isNickname = false;
									for (int n=0; n<servercount; n++) {
										tmpsc = (serverConnection)servers.elementAt(n);
										if (tmpsc.hasUser(query)) {
											sc.writeData(O_PASS_INFO + " " + ruid + tmpString + whos.getIAL((String)query));
											isNickname = true;
											n = servercount;
										}										
									}
									if (!isNickname) {
										//if it's a pattern, try and match it...
										if (query.indexOf("*") != -1) {
											Vector matches = whos.getMatches( query, 0 ); // 0 = match on nickname
											for (int n=0; n<matches.size(); n++) {
												sc.writeData(O_PASS_INFO + " " + ruid + tmpString + (String)matches.elementAt(n));
											}											
										}
									}
									sc.writeData(O_PASS_INFO + " " + ruid + " 315 " + nick + " " + query +  " :End of /WHO list");

									
								
								}
								
								
							}
							
						}	
							
						
						
						else if (cmd.equals(I_REQUEST_SILENCES)) {
							//I_REQUEST_SILENCES uid unick param
							if (st.countTokens() == 3) {
								String uid = st.nextToken();
								String unick = st.nextToken();
								String param = st.nextToken();
								boolean uexists = false;
								
								for (int n=0; n<servercount; n++) {
									if (n != i) {
										tmpsc = (serverConnection)servers.elementAt( n );
										if (tmpsc.hasUser( param )) {
											tmpsc.writeData( O_SILENCES_REQ + " " + i + " " + uid + " " + unick + " " + param );
											uexists = true;
											n = servercount;
										}
									}									
								}
								
								if (!uexists) {
									sc.writeData(O_PASS_INFO + " " + uid + " 401 " + unick + " " + param + " :No such nick/channel");
								}
							}
						}
							
						
						
						else if (cmd.equals(I_USERCONNECTING)) {
							
							//djf 62.31.114.78 ntsecurity.nu bob H@ :0 test
							if (st.countTokens() >= 6) {
								String ident = st.nextToken();
								String ip    = st.nextToken();
								String svr   = st.nextToken();
								String nick  = st.nextToken();
								String modes = st.nextToken();
								String hops  = st.nextToken();
								int len = cmd.length() + ident.length() + ip.length() + svr.length() + nick.length() + modes.length() + hops.length() + 7;
								String realname = "";
								if (data.length() > len) {
									realname = data.substring( len );
								}									
								
								
															
								sc.addUser( nick );
								whos.addIAL( nick, ident, ip, svr, realname, modes, hops );
								userCount++;
								if (userCount > highestUserCount) { highestUserCount = userCount; }
								
								//notify the other servers of this user connecting.
								serverConnection s3 = null;
								
								for (int n=0; n<servercount; n++) {
									if (n != i) {
										s3 = (serverConnection)servers.elementAt(n);
										if (s3.isConnected())
											s3.writeData(O_USER_CONNECTED + " " + nick + "!" + ident + "@" + ip + " " + sc.getServerFakeName() );
									}
								}
								
								for (int n=0; n<servercount; n++) {
									tmpsc = (serverConnection)servers.elementAt(n);
									if (tmpsc.isConnected()) { tmpsc.writeData( O_NEW_USERCOUNT + " " + userCount + " " + highestUserCount ); }
								}
							}
						}
						
						
						
						else if (cmd.equals(I_CHANMODE)) {
							//CHANMODE: userMask chan fullModeString limit modes
							if (st.countTokens() >= 5) {
								String um = st.nextToken();
								String chan = st.nextToken();
								String allModes = st.nextToken(); //full modestring for listx
								String limit = st.nextToken();
								String modes = data.substring(cmd.length() + um.length() + chan.length() + allModes.length() + limit.length() + 5);
								
								sc.setChanOption(chan, "REPLACE", "CHANMODES", allModes);
								sc.setChanOption(chan, "REPLACE", "LIMIT", limit);
								for (int n=0; n<servercount; n++) {
									tmpsc = (serverConnection)servers.elementAt(n);
									if ((n != i) && (tmpsc.isConnected()) && (tmpsc.hasChan(chan.toLowerCase()) != -1)) {
										tmpsc.writeData( O_CHAN_MODE + " " + um + " " + chan + " " + modes );
										tmpsc.setChanOption(chan, "REPLACE", "CHANMODES", allModes);
										tmpsc.setChanOption(chan, "REPLACE", "LIMIT", limit);
									}
									
								}
								
							}
						}
						
						
						else if (cmd.equals(I_RFR)) {
							//RFR chanserv-userid #chan starmask pass
							if (st.countTokens() == 4) {
								String uid = st.nextToken();
								String chan = st.nextToken();
								String starMask = st.nextToken();
								String pass = st.nextToken();
								String nick = ":";
								if (starMask.indexOf("!") != -1) { nick = starMask.substring(0, starMask.indexOf("!")); }
								
								boolean isowner = false;

								for (int n=0; n<servercount; n++) {
									tmpsc = (serverConnection)servers.elementAt(n);
									if ( (n != i) && (tmpsc.isConnected()) && (tmpsc.hasChan(chan.toLowerCase()) != -1) ) {
										//System.out.println("found server with chan: " + chan );
										//System.out.println("user status (" + nick + ") on chan=" + tmpsc.userStatusOnChan(chan.toLowerCase(), nick.toLowerCase()));
										if (tmpsc.userStatusOnChan(chan.toLowerCase(), nick.toLowerCase()).equals(".")) {
											isowner = true;
										}
										//n = servercount; //stop searching										
									}
								}
								if (isowner) {
									//System.out.println("hub is allowing rfr");
									sc.writeData(O_PASS_INFO + " " + uid + " !R RFR " + chan + " " + starMask + " " + pass);
								}
							}							
						}
						
						
						
						else if (cmd.equals(I_RUMOC)) {
							//RUMOC: [status]userMask  chan  mode  whoTo
							if (st.countTokens() == 4) {
								String um = st.nextToken();
								String chan = st.nextToken();
								String mode = st.nextToken();
								String whoTo = st.nextToken();
								boolean allowMode = false;
								
								String uStat = "";
								if (um.charAt(0) != ':') {
									uStat = "" + um.charAt(0);
									um = um.substring(1);
								}
								String nick = um.substring(1, um.indexOf("!"));
								
								//Check for sysop status
								boolean sysop = false;
								int nl = findOperLevel( nick );
								int wl = findOperLevel( whoTo );
								//System.out.println("nl " + nick + " = " + nl);
								//System.out.println("wl " + whoTo + " = " + wl);
								if ((nl > 0) && (nl >= wl)) {
									sysop = true;
									uStat = ".";
								} else if ((wl > 0) && (wl > nl)) {
									uStat = "";
								}
								
								
								//correct the capitilisation of the nick
								for (int n=0; n<servercount; n++) {
									tmpsc = (serverConnection)servers.elementAt(n);
									if (tmpsc.hasUser(whoTo)) {
										whoTo = tmpsc.correctNick( whoTo );
										n = servercount;
									}
								}


								String wStat = "";
								
								for (int n=0; n<servercount; n++) {
									tmpsc = (serverConnection)servers.elementAt(n);
									if ( (n != i) && (tmpsc.isConnected()) && (tmpsc.hasChan(chan.toLowerCase()) != -1) ) {
										wStat = tmpsc.userStatusOnChan(chan.toLowerCase(), whoTo.toLowerCase());
										//if (sysop && !wStat.equals("n")) { wStat = "r"; }
										
										//System.out.println("s" + n + " wStat:" + wStat + " mode:" + mode);
										
										if (mode.equals("+q")) {
											if (uStat.equals(".") && !wStat.equals("n")) {

												//go through all the servers and inform them of the mode change
												//including server(i)
												serverConnection tmp = null;
												for (int s=0; s<servercount; s++) {
													tmp = (serverConnection)servers.elementAt(s);
													
													if (wStat.equals("@")) { tmp.writeData(O_SUMOC + " " + um + " " + chan + " -o " + whoTo ); }
													tmp.writeData( O_SUMOC + " " + um + " " + chan + " +q " + whoTo );
													tmp.SUMOC(chan, whoTo, ".");
													allowMode = true;
												}


												n = servercount;
											}											

										} else if (mode.equals("-q")) {
											if (uStat.equals(".") && !wStat.equals("n")) {
												//go through all the servers and inform them of the mode change
												//including server(i)
												serverConnection tmp = null;
												for (int s=0; s<servercount; s++) {
													tmp = (serverConnection)servers.elementAt(s);

													tmp.writeData( O_SUMOC + " " + um + " " + chan + " -q " + whoTo );
													tmp.SUMOC(chan, whoTo, "");
													allowMode = true;
												}


												n = servercount;

											}
											
											
										} else if (mode.equals("+o")) {
											if (!wStat.equals("n") &&  ( uStat.equals(".") || (  uStat.equals("@") && !wStat.equals(".")  ) ) ) {
												
												//go through all the servers and inform them of the mode change
												//including server(i)
												serverConnection tmp = null;
												for (int s=0; s<servercount; s++) {
													tmp = (serverConnection)servers.elementAt(s);
													
													if (wStat.equals(".")) { tmp.writeData(O_SUMOC + " " + um + " " + chan + " -q " + whoTo ); }
													tmp.writeData( O_SUMOC + " " + um + " " + chan + " +o " + whoTo );
													tmp.SUMOC(chan, whoTo, "@");
													allowMode = true;
												}


												n = servercount;
											}
											
										} else if (mode.equals("-o")) {
											//System.out.println("RUMOC -o: uStat=" + uStat + " wStat=" + wStat);
											if (!wStat.equals("n") &&  ( uStat.equals(".") || (  uStat.equals("@") && !wStat.equals(".")  ) ) ) {
												
												//go through all the servers and inform them of the mode change
												//including server(i)
												serverConnection tmp = null;
												for (int s=0; s<servercount; s++) {
													tmp = (serverConnection)servers.elementAt(s);
													
													if (wStat.equals(".")) { tmp.writeData( O_SUMOC + " " + um + " " + chan + " -q " + whoTo ); }

													tmp.writeData( O_SUMOC + " " + um + " " + chan + " -o " + whoTo );
													tmp.SUMOC(chan, whoTo, "");
													allowMode = true;
												}


												n = servercount;

											}


										} else if (mode.equals("+v")) {
											if ((uStat.equals("@") || uStat.equals(".")) && !wStat.equals("n")) {
												
												//go through all the servers and inform them of the mode change
												//including server(i)
												serverConnection tmp = null;
												for (int s=0; s<servercount; s++) {
													tmp = (serverConnection)servers.elementAt(s);
													
													tmp.writeData( O_SUMOC + " " + um + " " + chan + " +v " + whoTo );
													if (wStat.equals("r")) { tmp.SUMOC(chan, whoTo, "+"); }
													allowMode = true;
												}


												n = servercount;
											}


										} else if (mode.equals("-v")) {
											if ((uStat.equals("@") || uStat.equals(".")) && !wStat.equals("n")) {
												
												//go through all the servers and inform them of the mode change
												//including server(i)
												serverConnection tmp = null;
												for (int s=0; s<servercount; s++) {
													tmp = (serverConnection)servers.elementAt(s);
													
													tmp.writeData( O_SUMOC + " " + um + " " + chan + " -v " + whoTo );
													if (wStat.equals("r")) { tmp.SUMOC(chan, whoTo, ""); }
													allowMode = true;
												}


												n = servercount;
											}


										}


									}
									
								}
								if (allowMode) {
									for (int n=0; n<servercount; n++) {
										tmpsc = (serverConnection)servers.elementAt(n);
										if (tmpsc.isConnected()) {
											tmpsc.writeData(O_PASS_SERVICES + " " + um + " MODE " + chan + " " + mode + " " + whoTo);
										}
									}
								}

								
								
								
							}
						}
						
						
						
						else if (cmd.equals(I_SBOC) || cmd.equals(I_USBOC)) {
							//SBOC: userMask chan banMask
							if (st.countTokens() >= 5) {
								String um = st.nextToken();
								String chan = st.nextToken();
								//String ban = st.nextToken();
								
								for (int n=0; n<servercount; n++) {
									if (n!=i) {
										tmpsc = (serverConnection)servers.elementAt(n);
																		
										if (tmpsc.isConnected() && (tmpsc.hasChan(chan.toLowerCase()) != -1)) {
											if (cmd.equals(I_SBOC)) {
												tmpsc.writeData(O_SBOC + data.substring(cmd.length()));
											} else {
												tmpsc.writeData(O_USBOC + data.substring(cmd.length()));
											}
										}
									}
									
								}
							}
						}
						

						
						
						else if (cmd.equals(I_SUMOC)) {
							//SUMOC: userMask chan +q whoTo
							if (st.countTokens() >= 4) {
								String um = st.nextToken();
								String ustat = um.substring(0,1);
								um = um.substring(1);
								
								String chan = st.nextToken();
								String mode = st.nextToken();
								
								String whoTo = st.nextToken();
								
								boolean echoBack = false;
								if (st.hasMoreTokens()) {
									echoBack = (st.nextToken().equals("1"));
								}
								
								for (int n=0; n<servercount; n++) {
									tmpsc = (serverConnection)servers.elementAt(n);
									if (tmpsc.hasUser(whoTo)) {
										whoTo = tmpsc.correctNick( whoTo ); //correct the capitilisation of the nick
										n = servercount;
									}
								}
								
								
								String unick = um.substring(1, um.indexOf("!"));
								
								for (int n=0; n<servercount; n++) {
									tmpsc = (serverConnection)servers.elementAt(n);
									
									//if (n != i) {
									
									
									if (tmpsc.isConnected() && (tmpsc.hasChan( chan.toLowerCase() ) != -1)) {
										
										if      (mode.equals("+q") ) {
											if ((n != i) || (echoBack)) {
												tmpsc.writeData( O_SUMOC + " " + um + " " + chan + " " + mode + " " + whoTo );
											}
											tmpsc.SUMOC(chan, whoTo, ".");
											//sc.SUMOC(chan,whoTo, ".");
										}
										else if (mode.equals("-q") ) {
											if ((n != i) || (echoBack)) {
												tmpsc.writeData( O_SUMOC + " " + um + " " + chan + " " + mode + " " + whoTo );
											}
											tmpsc.SUMOC(chan, whoTo, "");
											//sc.SUMOC(chan,whoTo, ".");
										}
										
										
										

										else if (mode.equals("+o") ) {
											if ((n != i) || (echoBack)) {
												tmpsc.writeData( O_SUMOC + " " + um + " " + chan + " " + mode + " " + whoTo );
											}
											tmpsc.SUMOC(chan, whoTo, "@");
											//sc.SUMOC(chan, whoTo, "@");
										}

										else if (mode.equals("-o") ) {
											if ((n != i) || (echoBack)) {
												tmpsc.writeData( O_SUMOC + " " + um + " " + chan + " " + mode + " " + whoTo );
											}
											tmpsc.SUMOC(chan, whoTo, "");
											//sc.SUMOC(chan, whoTo, "");
										}
										
										else if (mode.equals("+v") ) {
											if ((n != i) || (echoBack)) {
												tmpsc.writeData( O_SUMOC + " " + um + " " + chan + " " + mode + " " + whoTo );
											}
											
											if (tmpsc.userStatusOnChan(chan, whoTo).equals("r")) {
												tmpsc.SUMOC(chan, whoTo, "+");
												//sc.SUMOC(chan, whoTo, "+");
											}
										}

										else if (mode.equals("-v") ) {
											if ((n != i) || (echoBack)) {
												tmpsc.writeData( O_SUMOC + " " + um + " " + chan + " " + mode + " " + whoTo );
											}

											if (tmpsc.userStatusOnChan(chan, whoTo).equals("r")) {
												tmpsc.SUMOC(chan, whoTo, "");
												//sc.SUMOC(chan, whoTo, "");
											}

										}



									}	
									
									//}								
								}
								
								
							}
						}
						
						else if (cmd.equals(I_IDLER_QUIT)) {
							if (st.countTokens() == 1) {
								String nick = st.nextToken();
								userCount--;
								sc.removeUser( nick );
								whos.removeIAL( nick );
								
								for (int n=0; n<servercount; n++) {
									tmpsc = (serverConnection)servers.elementAt(n);
									if (tmpsc.isConnected()) { tmpsc.writeData( O_NEW_USERCOUNT + " " + userCount + " " + highestUserCount ); }
								}
							}
						}
						
						/*
						else if (cmd.equals(I_KILL)) {
							//I_KILL userID userNick whoToKill :reason
							if (st.countTokens() >= 4) {
								String killerID = st.nextToken();
								String killerNick = st.nextToken();
								String whoToKill = st.nextToken();
								String reason = data.substring( cmd.length() + killerID.length() + killerNick.length() + whoToKill.length() + 4 );
								
								boolean userExists = false;
								
								//try to find the user
								for (int i=0; i<servercount; i++) {
									tmpsc = (serverConnection)servers.elementAt(i);
									if (tmpsc.hasUser(whoToKill)) {
										
										
									}									
								}
								
								
								
							}							
						}
						*/
						
						
						else if (cmd.equals(I_USERQUIT)) {
							if (st.countTokens() >= 3) {
								//USER_QUIT: <userMask> <#chan1>,<#chan2>,...   quitmessage
								String tmpUser = st.nextToken();
								String tmpChans = st.nextToken();
								String tmpMsg = data.substring( cmd.length() + tmpUser.length() + tmpChans.length() + 3 );
								//tmpMsg = tmpMsg.trim();
								if (!tmpMsg.startsWith(":")) { tmpMsg = ":" + tmpMsg; }
								
								Vector c = new Vector();
								
								if (tmpChans.indexOf(",") != -1) {
									StringTokenizer cs = new StringTokenizer(tmpChans, ",");
									while (cs.hasMoreTokens())
										c.addElement( cs.nextToken() );
								} else {
										c.addElement( tmpChans );
								}
									
									
								userCount--;
								sc.removeUser( tmpUser.substring(1, tmpUser.indexOf("!")) );
								whos.removeIAL( tmpUser.substring(1, tmpUser.indexOf("!")) );

								
								//notify the other servers of this user quitting.
								//serverConnection tmpsc = null;
								String tmpc = "";
								boolean toldThisServer = false;
								
								for (int n=0; n<servercount; n++) {
									//toldThisServer = false;
									//if (n != i) {
										//find out if the user was on any channels on that server,
										//and send a quit message to each user on that channel.
										tmpsc = (serverConnection)servers.elementAt(n);
										if (tmpsc.isConnected()) {
											for (int p=0; p<c.size(); p++) {
												tmpc = (String)c.elementAt(p);
												if (tmpsc.hasChan( tmpc.toLowerCase() ) != -1) {
													//if ((n != i) && (!toldThisServer)) {
													//	tmpsc.writeData(O_USER_QUIT + " " + tmpUser + " " + tmpc + " " + tmpMsg );
													//	toldThisServer = true;
													//}
													//remove the user from that channel
													tmpsc.removeUserFromChan( tmpc, tmpUser.substring(1, tmpUser.indexOf("!")) );
													   //sc.removeUserFromChan( tmpc, tmpUser.substring(1, tmpUser.indexOf("!")) );
													//If the channel is now empty, remove it from our chanlist for that server.
													tmpsc.removeChanIfEmpty( tmpc );
													//sc.removeChanIfEmpty( tmpc );
												
												} //else if (tmpsc.isConnected()) {
													//tmpsc.writeData(O_USER_QUIT + " " + tmpUser + " " + tmpc + " " + tmpMsg );
												//}
											}
										}
										
									//}
								}
								for (int n=0; n<servercount; n++) {
									tmpsc = (serverConnection)servers.elementAt(n);
									if (n != i) {
										tmpsc.writeData(O_USER_QUIT + " " + tmpUser + " " + tmpChans + " " + tmpMsg);
									}
									tmpsc.writeData( O_NEW_USERCOUNT + " " + userCount + " " + highestUserCount );									
									
								}

								/*for (int n=0; n<servercount; n++) {
									tmpsc = (serverConnection)servers.elementAt(n);
									if (tmpsc.isConnected()) { tmpsc.writeData( O_NEW_USERCOUNT + " " + userCount + " " + highestUserCount ); }
								}*/

							}
						}
						
						

						
						
						
						else if (cmd.equals(I_REQUEST_NICKCHANGE)) {
							
							if (st.countTokens() == 2) {
								String tmpNick = st.nextToken();
								String tmpUID = st.nextToken();

								boolean found = false;
								for (int n=0; n<servercount; n++) {
									if (n != i) {
										tmpsc = (serverConnection)servers.elementAt(n);
										if (!quiet) { System.out.println(tmpsc.getUsers().toString()); }
										if (tmpsc.isConnected() && tmpsc.hasUser( tmpNick.toLowerCase() )) {
											found = true;
										}
									}
								}
								
								if (found) {
									sc.writeData(O_NICKCHANGE_DENIED + " " + tmpUID + " " + tmpNick );
								} else {
									//allow the nickchange
									sc.writeData(O_NICKCHANGE_ALLOWED + " " + tmpUID + " " + tmpNick );
								}
								
								
							}
							
						}
						
						
						else if (cmd.equals(I_NICKCHANGE)) {
							if (st.countTokens() >1 ) {
								
								//NICK_CHANGE: userMask newNick #chan1 #chan2 ...
								String um = st.nextToken();
								String nn = st.nextToken();
								String on = um.substring(1, um.indexOf("!"));
								
								String chans = data.substring( cmd.length() + um.length() + 2 );
								chans = chans.trim();
									
								
								serverConnection s5 = null;
								
								whos.nickChange( on, nn );
								
								for (int n=0; n<servercount; n++) {
									if (n != i) {
										s5 = (serverConnection)servers.elementAt(n);
										if (s5.isConnected())
											s5.writeData( O_NICK_CHANGE + " " + um + " " + nn + " " + chans );
									}
								}
								
								
								
								//We must change the users's nick in each of the channels on that server
								sc.removeUser( on );
								sc.addUser( nn );
								
								sc.nickChange( on, nn );
								
								changeOperNick( on, nn );

								
								
							}
							
						}
						
						
						

							
						
						
						else if (cmd.equals(I_CHANJOIN)) {
							if (st.countTokens() >= 2) {
								
								//CHAN_JOIN: userMask #channel key
								String um = st.nextToken();
								String c = st.nextToken();
								String key = "";
								if (st.hasMoreTokens()) { key = " " + st.nextToken(); }
								
								String stat = "";
								if (um.charAt(0) != ':') {
									stat = um.charAt(0) + "";
									um = um.substring(1);
								}
								String nick = um.substring(1, um.indexOf("!"));
								//nick = nick.toLowerCase();
								
								//Add the channel to the servers channel list?
								if (sc.hasChan( c.toLowerCase() ) == -1) {
									Vector v = new Vector();
									//v.addElement( c.toLowerCase() );
									v.addElement(c);
									v.addElement( stat + nick );
									sc.addChan( v );
								} else {
									sc.addUserToChan( c.toLowerCase(), stat + nick );
								}
								
								
								
								//notify any servers that have this channel								
								for (int n=0; n<servercount; n++) {
									if (n != i) {
										tmpsc = (serverConnection)servers.elementAt(n);
										if (tmpsc.isConnected() ) { //&& (tmpsc.hasChan( c ) != -1)) {
											tmpsc.writeData(O_CHAN_JOIN + " " + um + " " + c + key );
										}
									}
								}

							}						
						}
							
						
						else if (cmd.equals(I_CHANPART)) {
							if (st.countTokens() >= 2) {
								//CHAN_PART: userMask #channel reason
								String um = st.nextToken();
								String c = st.nextToken();
								String nick = um.substring(1, um.indexOf("!"));
								String reason = "";
								if (st.hasMoreTokens()) {
									reason = data.substring(cmd.length() + um.length() + c.length() + 3);
									if (!reason.startsWith(":")) { reason = ":" + reason; }
									reason = " " + reason;
								}
								
								//if (c.charAt(0) == ':')
								//	c = c.substring(1);
								boolean done = false;
								
								for (int n=0; n<servercount; n++) {
									if (n != i) {
										//find out if the user was on that channel on that server,
										//and send a part message to each user on that channel.
										tmpsc = (serverConnection)servers.elementAt(n);
										
											if (tmpsc.isConnected() && (tmpsc.hasChan( c.toLowerCase() ) != -1)) {
												tmpsc.writeData(O_CHAN_PART + " " + um + " " + c + reason );
												//remove the user from that channel
												done = tmpsc.removeUserFromChan( c.toLowerCase(), nick.toLowerCase() );
												if (!done && !quiet)
													System.out.println("Unable to remove user: " + nick + " from chan: " + c);
												
												//If the channel is now empty, remove it from our chanlist for that server.
												tmpsc.removeChanIfEmpty( c );
												
											} else if (tmpsc.isConnected()) {
												//for the benefit of services
												//tmpsc.writeData(O_CHAN_PART + " " + um + " " + c );
											}
										
										


									}
								}
								
								sc.removeUserFromChan( c, nick );
								sc.removeChanIfEmpty( c) ;
								
							}
						}
						
						
						
						// -----------------------------
						// --------- NOTICE  -----------
						// -----------------------------
						else if (cmd.equals(I_NCE)) {
							//NCE: userMask #channel   full message
							if (st.countTokens() >= 3) {
								String um = st.nextToken();
								String chan = st.nextToken();
								String fm = data.substring(cmd.length() + um.length() + chan.length() + 3);
								fm = fm.trim();
								
								for (int n=0; n<servercount; n++) {
									if (n != i) {
										tmpsc = (serverConnection)servers.elementAt(n);
									
										if (tmpsc.isConnected() && (tmpsc.hasChan( chan.toLowerCase() ) != -1 )) {
											tmpsc.writeData("N " + um + " " + chan + " " + fm);
										}
									}
								}
							}
						}						
						
						
						else if (cmd.equals(I_NC)) {
							//NC: userMask #channel   full message
							if (st.countTokens() >= 3) {
								String um = st.nextToken();
								String chan = st.nextToken();
								String fm = data.substring(cmd.length() + um.length() + chan.length() + 3);
								fm = fm.trim();
								
								boolean chanExists = false;
								for (int n=0; n<servercount; n++) {
									if (n != i) {
										tmpsc = (serverConnection)servers.elementAt(n);
									
										if (tmpsc.isConnected() && (tmpsc.hasChan( chan.toLowerCase() ) != -1 )) {
											tmpsc.writeData("N " + um + " " + chan + " " + fm);
											chanExists = true;
										}
									}
								}
								
								if (!chanExists) {
									sc.writeData( O_NO_SUCH_CHANNEL + " " + um.substring(1, um.indexOf("!")) + " " + chan );
								}
								
								
							}
						}
						
						
						else if (cmd.equals("NU:")) {
							if (st.countTokens() >= 3) {
								String um = st.nextToken();
								String whoTo = st.nextToken();
								String fm = data.substring(cmd.length() + um.length() + whoTo.length() + 3);
								
								boolean userExists = false;
								for (int n=0; n<servercount; n++) {
									if (n != i) {
										tmpsc = (serverConnection)servers.elementAt(n);
										
										if (tmpsc.isConnected() && (tmpsc.hasUser( whoTo.toLowerCase() ))) {
											tmpsc.writeData("NU " + um + " " + whoTo + " " + fm );
											n = servercount;
											userExists = true;
										}
									}
								}
								
								if (!userExists) {
									sc.writeData(O_NO_SUCH_NICK + " " + um.substring(1, um.indexOf("!")) + " " + whoTo );
								}
								
							
							}
						}
						
						
						
						
						
						
						// -----------------------------
						// --------- PRIVMSG -----------
						// -----------------------------
						else if (cmd.equals(I_PMCE)) {
							//PM: userMask #channel   full message
							if (st.countTokens() >= 3) {
								String um = st.nextToken();
								String chan = st.nextToken();
								String fm = data.substring(cmd.length() + um.length() + chan.length() + 3);
								String fmu = "";
								if (!sc.compress) {
									fmu = fm;
									fm = "";
								}
								
								for (int n=0; n<servercount; n++) {
									if (n != i) {
										tmpsc = (serverConnection)servers.elementAt(n);
									
										if (tmpsc.isConnected() && (tmpsc.hasChan( chan.toLowerCase() ) != -1 )) {
											if (tmpsc.compress && fm.equals("")) { fm = compress(fmu); }
											else if (!tmpsc.compress && fmu.equals("")) { fmu = decompress(fm); }
											
											if (tmpsc.compress) { tmpsc.writeData("PM " + um + " " + chan + " " + fm); }
											else { tmpsc.writeData("PM " + um + " " + chan + " " + fmu); }
										}
									}
								}
							//halt();
							}
						}						
						
						
						else if (cmd.equals(I_PMC)) {
							//PM: userMask #channel   full message
							if (st.countTokens() >= 3) {
								String um = st.nextToken();
								String chan = st.nextToken();
								String fm = data.substring(cmd.length() + um.length() + chan.length() + 3);
								String fmu = "";
								if (!sc.compress) {
									fmu = fm;
									fm = "";
								}
								
								boolean chanExists = false;
								for (int n=0; n<servercount; n++) {
									if (n != i) {
										tmpsc = (serverConnection)servers.elementAt(n);
									
										if (tmpsc.isConnected() && (tmpsc.hasChan( chan.toLowerCase() ) != -1 )) {
											if (tmpsc.compress && fm.equals("")) { fm = compress(fmu); }
											else if (!tmpsc.compress && fmu.equals("")) { fmu = decompress(fm); }
											if (tmpsc.compress) { tmpsc.writeData("PM " + um + " " + chan + " " + fm); }
											else { tmpsc.writeData("PM " + um + " " + chan + " " + fmu); }
											chanExists = true;
										}
									}
								}
								
								if (!chanExists) {
									sc.writeData( O_NO_SUCH_CHANNEL + " " + um.substring(1, um.indexOf("!")) + " " + chan );
								}
								
							//halt();
							}
						}
						
						
						else if (cmd.equals(I_PMU)) {
							if (st.countTokens() >= 3) {
								String um = st.nextToken();
								String whoTo = st.nextToken();
								String fm = data.substring(cmd.length() + um.length() + whoTo.length() + 3);
								String fmu = "";
								if (!sc.compress) {
									fmu = fm;
									fm = "";
								}
								
								boolean userExists = false;
								for (int n=0; n<servercount; n++) {
									if (n != i) {
										tmpsc = (serverConnection)servers.elementAt(n);
										
										if (tmpsc.isConnected() && (tmpsc.hasUser( whoTo.toLowerCase() ))) {
											if (tmpsc.compress && fm.equals("")) { fm = compress(fmu); }
											else if (!tmpsc.compress && fmu.equals("")) { fmu = decompress(fm); }
											
											if (tmpsc.compress) { tmpsc.writeData("PMU " + um + " " + whoTo + " " + fm ); }
											else { tmpsc.writeData("PMU " + um + " " + whoTo + " " + fmu ); }
											n = servercount;
											userExists = true;
										}
									}
								}
								
								if (!userExists) {
									sc.writeData(O_NO_SUCH_NICK + " " + um.substring(1, um.indexOf("!")) + " " + whoTo );
								}
								
							halt();
							}
						}
						
						
						
						
						else if (cmd.equals(I_FINDTOPIC)) {
							//FIND_TOPIC: userID nick chan
							if (st.countTokens() == 2) {
								String uid = st.nextToken();
								String nick = st.nextToken();
								String chan = st.nextToken();
								
								boolean foundchan = false;
								String topic = "";
																
								for (int n=0; n<servercount; n++) {
									tmpsc = (serverConnection)servers.elementAt(n);
									
									if ((n != i) && tmpsc.isConnected() && (tmpsc.hasChan( chan.toLowerCase() ) != -1)) {
										//tmpsc.writeData( "FIND_TOPIC " + uid + " " + i + " " + nick + " " + chan );`
										topic = tmpsc.getChanOption(chan, "TOPIC");
										
										n = servercount;
										foundchan = true;
									}
								}
								
								if (!foundchan) {
									sc.writeData( O_PASS_INFO + " "  + uid + " 403 " + nick + " " + chan + " :No such channel" );
								} else {
									if (!topic.equals("")) {
										String whoSetTopic = topic.substring(0, topic.indexOf(" "));
										topic = topic.substring(topic.indexOf(" ")+1);
										String timestamp = topic.substring(0, topic.indexOf(" "));
										topic = topic.substring(topic.indexOf(" ")+1);
										
										
										sc.writeData( O_PASS_INFO + " " + uid + " 332 " + nick + " " + chan + " :" + topic );
										sc.writeData( O_PASS_INFO + " " + uid + " 333 " + nick + " " + chan + " " + whoSetTopic + " " + timestamp);
									} else {
										sc.writeData( O_PASS_INFO + " " + uid + " 331 " + nick + " " + chan + " :No topic is set.");
									}

									
								}
								
							}
						}
						
						else if (cmd.equals(I_TOPICCHANGE)) {
							//TOPIC_CHANGE: userMask chan timestamp :newtopic
							if (st.countTokens() >= 4) {
								String um = st.nextToken();
								String nick = um.substring(1, um.indexOf("!"));
								String chan = st.nextToken();
								String timestamp = st.nextToken();
								String topic = data.substring(cmd.length() + um.length() + chan.length() + timestamp.length() + 4);
								
								for (int n=0; n<servercount; n++) {
									tmpsc = (serverConnection)servers.elementAt(n);
									
									if (tmpsc.isConnected() && (tmpsc.hasChan( chan.toLowerCase() ) != -1)) {
										//tmpsc.setChanOption( chan, "REM", "TOPIC", tmpsc.getChanOption( chan, "TOPIC" ) ); //remove the old..
										//tmpsc.setChanOption( chan, "ADD", "TOPIC", nick + " " + timestamp + " " + topic.substring(1) );   //add the new.
										tmpsc.setChanOption( chan, "REPLACE", "TOPIC", nick + " " + timestamp + " " + topic );
										if (n != i) {
											tmpsc.writeData( O_TOPIC_CHANGE + " " + um + " " + chan + " " + timestamp + " " + topic );
										}
									} else if (tmpsc.isConnected()) {
											tmpsc.writeData( O_TOPIC_CHANGE + " " + um + " " + chan + " " + timestamp + " " + topic );										
									}
								}
								
								
							}
						}
						
						
				  	  //tellUser( hubID, "CHAN_PROP: " + userMask(userID) + " " + tmpProp + " " + tmpChan + " " + tmpValue );
						else if (cmd.equals(I_CHANPROP)) {
							if (st.countTokens() >= 4) {
								String um = st.nextToken();
								//String nick = um.substring(1, um.indexOf("!"));
								String prop = st.nextToken();
								String chan = st.nextToken();
								String value = data.substring(cmd.length() + um.length() + prop.length() + chan.length() + 4);
								
								for (int n=0; n<servercount; n++) {
									tmpsc = (serverConnection)servers.elementAt(n);
									
									if (tmpsc.isConnected() && (tmpsc.hasChan( chan.toLowerCase() ) != -1)) {
										tmpsc.setChanOption( chan, "REM", prop, tmpsc.getChanOption( chan, prop ) );
										tmpsc.setChanOption( chan, "ADD", prop, value );
										if (n != i) {
											tmpsc.writeData( O_CHAN_PROP + " " + um + " " + prop + " " + chan + " " + value );
										}
										
									}
								}
							}
							
						}
						
						
						
						
						else if (cmd.equals(I_KICK)) {
							//KICK: reporthome? [S]status userMask chan kickWho :reason for kick
							if (st.countTokens() >= 6) {
								
								boolean reportHome = (st.nextToken().equals("1")); //whether to echo the kick to the originating server
								String uStat = st.nextToken();
								String um = st.nextToken();
								String chan = st.nextToken();
								String kickWho = st.nextToken();
								String nick = um.substring(1,um.indexOf("!"));
								String reason = data.substring(cmd.length() + uStat.length() + um.length() + chan.length() + kickWho.length() + 7);
								
								boolean chanExists = false;
								boolean userExists = false;
								int kickedServer = -1; //which server is the user being kick, on ?
								
								//sc.removeUserFromChan( chan, kickWho );
								int userStat = 0;
								if (uStat.startsWith("S"))
									userStat = Integer.parseInt(uStat.substring(1));
								else
									userStat = Integer.parseInt(uStat);
								
								
								boolean allowKick = false;
								int kickerOper = findOperLevel( nick );
								int kickedOper = findOperLevel( kickWho );
								//System.out.println("kickerOper=" + kickerOper + " ,kickedOper=" + kickedOper);
								
								//this section needs tidying up! get rid of "S" - not needed, since
								//we keep a record of opers on the hub..
								for (int n=0; n<servercount; n++) {
									tmpsc = (serverConnection)servers.elementAt(n);
									
									if (!chanExists && (tmpsc.hasChan(chan.toLowerCase()) != -1)) { chanExists = true; }
									
									//(n != i) && 
									if ( tmpsc.isConnected() && (tmpsc.hasChan(chan.toLowerCase()) != -1) ) {
										
										if (!tmpsc.userStatusOnChan(chan, kickWho).equals("n")) { userExists = true; }
										
										if (kickerOper != 0) {
											//user doing the kicking is an oper
											if (!tmpsc.userStatusOnChan(chan, kickWho).equals("n") && (kickedOper <= kickerOper)) {
													//System.out.println("allowing the kick");
													allowKick = true;
													kickedServer = n;
											}
											
										} else {
											if (kickedOper == 0) {
												if ((userStat == 3) || ((userStat == 2) && (!tmpsc.userStatusOnChan(chan, kickWho).equals(".")))) {
													if (!tmpsc.userStatusOnChan(chan, kickWho).equals("n")) {
														allowKick = true;
														kickedServer = n;
													}
												} else if (nick.equals(kickWho) && (!tmpsc.userStatusOnChan(chan, kickWho).equals("n"))) {
													//user kicked themselves!
													//System.out.println("hub: self kick; " + nick + ", " + kickWho);
													allowKick = true;
													kickedServer = n;
												}
											}
											
										}
										
										
										

										
										
									}									
								}
								
								sc.removeUserFromChan( chan, kickWho );
								
								//System.out.println("allowKick=" + allowKick);
								if (allowKick) {
									for (int n=0; n<servercount; n++) {
										tmpsc = (serverConnection)servers.elementAt(n);
										if ( tmpsc.isConnected() && (tmpsc.hasChan(chan.toLowerCase()) != -1) ) {
											tmpsc.removeUserFromChan( chan, kickWho );
											tmpsc.removeChanIfEmpty( chan );
											
											//if (n != i) {
												tmpsc.writeData( O_KICK_FROM_CHAN + " " + um + " " + chan + " " + kickWho + " " + reason );
											//}
											
										}
										if (tmpsc.isConnected()) {
											//for services
											//tmpsc.writeData( O_KICK_FROM_CHAN + " " + um + " " + chan + " " + kickWho + " " + reason );
											tmpsc.writeData( O_PASS_SERVICES + " " + um + " KICK " + chan + " " + kickWho + " " + reason );
										}
									}
								} else {

									if (!chanExists) {
										sc.writeData(O_NO_SUCH_CHANNEL + " " + nick + " " + chan );
									} else if (!userExists) {
										sc.writeData(O_NO_SUCH_NICK + " " + nick + " " + kickWho );
									} else {
										sc.writeData( O_NOT_CHAN_OP + " " + nick + " " + chan );
									}
								}
								
								
								
							}							
						}
						
						
						else if (cmd.equals(I_REQUEST_CHANMODES)) {
							//REQUEST_CHANMODES: nick uid #channel
							if (st.countTokens() == 3) {
								String nick = st.nextToken();
								String uid = st.nextToken();
								String chan = st.nextToken();
								
								boolean chanExists = false;
								String cmodes = "";
								String climit = "";
								
								for (int n=0; n<servercount; n++) {
									tmpsc = (serverConnection)servers.elementAt(n);
									
									if (tmpsc.isConnected() && (tmpsc.hasChan(chan.toLowerCase()) != -1)) {
										//tmpsc.writeData(O_REQUEST_CHANMODES + " " + nick + " " + uid + " " + i + " " + chan );
										cmodes = tmpsc.getChanOption( chan, "CHANMODES" );
										climit = tmpsc.getChanOption( chan, "LIMIT" );

										n = servercount;
										chanExists = true;
									}
								}
								
								if (chanExists) {
									if (cmodes.equals("ERROR")) { cmodes = "+nt"; }
									if (!climit.equals("ERROR")) { cmodes+= " " + climit; }
									sc.writeData(O_PASS_INFO + " " + uid + " 324 " + nick + " " + chan + " " + cmodes );
								} else {
									sc.writeData(O_NO_SUCH_CHANNEL + " " + nick + " " + chan );
								}

							}
						}
						
						
						else if (cmd.equals(I_DEBUG_SWITCH)) {
							//DEBUG_SWITCH ON/OFF
							if (st.countTokens() >= 1) {
								String onoff = st.nextToken();
								if (onoff.equals("ON")) { quiet = false; }
								else					{ quiet = true;  }
							}
						}
						
						
						else if (cmd.equals(I_REQUEST_NAMES)) {
							//REQUEST_NAMES: userID nick  #channel ircx
							if (st.countTokens() == 4) {
								String uid = st.nextToken();
								String nick = st.nextToken();
								String chan = st.nextToken();
								boolean ircx = true;
								if (st.nextToken().equals("false")) { ircx = false; }
								String names = "";
								int hc = -1;
								
								for (int n=0; n<servercount; n++) {
									tmpsc = (serverConnection)servers.elementAt(n);
									
									if (tmpsc.isConnected()) {
										hc = tmpsc.hasChan( chan.toLowerCase() );
										if (hc != -1)
											names = names + " " + tmpsc.namesList(hc, ircx);
									}
								}
								
								names = names.trim();
								
								if (names.length() == 0) {
									//channel doesn't exist
									sc.writeData( O_PASS_INFO + " "  + uid + " 403 " + nick + " " + chan + " :No such channel" );
								} else {
									sc.writeData( O_PASS_INFO + " "  + uid + " 353 " + nick + " = " + chan + " :" + names );
									sc.writeData( O_PASS_INFO + " "  + uid + " 366 " + nick + " " + chan + " :End of /NAMES list." );
								}
								
							}
						}
						
						
						
						
						
						else if (cmd.equals(I_REQUEST_CHANJOIN)) {
							//REQUEST_CHANJOIN: #c1,#c2,#c3   userID    commmand
							
							if (st.countTokens() > 1) {
							
								String allC = st.nextToken();
								String uID = st.nextToken();
								boolean ircx = true;
								String bircx = st.nextToken();
								if (bircx.equals("false")) { ircx = false; }
								String nick = st.nextToken();
								String cmmd = data.substring(cmd.length() + allC.length() + uID.length() + bircx.length() + nick.length() + 5);
								//cmmd = cmmd.trim();


								
								Vector chans = new Vector();
								
								if (allC.indexOf(",") != -1) {
									StringTokenizer scc = new StringTokenizer( allC, "," );
									while (scc.hasMoreTokens())
										chans.addElement( scc.nextToken() );
								} else {
									chans.addElement( allC );
								}
									
								
								Vector allowedChans = new Vector();
								allowedChans = chans;
								
								
								Vector found = new Vector(); //the channels that already exist.
								serverConnection s6 = null;
								String c = "";
								String tmp2 = "";
									

											
								for (int f=0; f<chans.size(); f++) {
									c = (String)chans.elementAt(f);
									
									for (int n=0; n<servercount; n++) {
										if (n != i) {
											s6 = (serverConnection)servers.elementAt(n);
								
											if (sc.hasChan( c.toLowerCase() ) != -1) {
												/*
													This happens when more than 1 join at almost the same time after chan-collision.
													To avoid problems for user2, we just respond with a chanjoin_allowed, since the
													chan will already exist locally for them when the first user joined.
												*/
											} else if (s6.isConnected() && (s6.hasChan( c.toLowerCase() ) != -1)) {
												if (found.size() > 0)
													tmp2 = (String)found.lastElement();
													
													
												if (tmp2.startsWith( c + " :" ) ) {
													tmp2 = (String)found.elementAt(found.size()-1);
													tmp2 = tmp2 + " " + s6.namesList(s6.hasChan(c.toLowerCase()), ircx);
													
													found.setElementAt( tmp2, found.size()-1 );
												} else {
													found.addElement(c + " :" + s6.namesList(s6.hasChan(c.toLowerCase()), ircx));
													allowedChans.removeElement( c );
												}
											}
										}
									}
								}

								
								if (found.size() > 0) {
								
									//we must construct a names list and send it to the server,
									//to pass to the user
																		
									//clone-channels uid (cmmd asdf) #chan :names list
									for (int f =0; f<found.size(); f++) {
										if (!quiet) {
											System.out.println("clone channel -> " + uID + " (" + cmmd + ") " + (String)found.elementAt(f));
										}
										Vector v = new Vector();
										String chan = (String)found.elementAt(f);
										chan = chan.substring(0, chan.indexOf(" "));
										
										
										//v.addElement( chan.toLowerCase() );
										v.addElement( chan );
										
										//v.addElement( nick.toLowerCase() );
										v.addElement( nick );

										String topic = "";
										String onjoin = "";
										String onpart = "";
										String ownerkey = "";
										String hostkey = "";
										String cmodes = "";
										String climit = "";
										sc.addChan( v );
																				
										for (int n=0; n<servercount; n++) {
											if (n != i) {
												tmpsc = (serverConnection)servers.elementAt(n);
												
												if (tmpsc.isConnected() && (tmpsc.hasChan( chan.toLowerCase() ) != -1)) {
													topic = tmpsc.getChanOption( chan, "TOPIC" );
													onjoin = tmpsc.getChanOption( chan, "ONJOIN" );
													onpart = tmpsc.getChanOption( chan, "ONPART" );
													ownerkey = tmpsc.getChanOption( chan, "OWNERKEY" );
													hostkey = tmpsc.getChanOption( chan, "HOSTKEY" );
													cmodes = tmpsc.getChanOption( chan, "CHANMODES" );
													climit = tmpsc.getChanOption( chan, "LIMIT" );
													
													n = servercount; //terminate the for-loop
												}
											}										
											
										}



										//sc.addChan( v );
										if (topic.equals("ERROR")) { topic = ""; }
										else { sc.setChanOption( chan, "ADD", "TOPIC", topic ); }
										
										if (onjoin.equals("ERROR")) { onjoin = ""; }
										else { sc.setChanOption( chan, "ADD", "ONJOIN", onjoin ); }
										
										if (onpart.equals("ERROR")) { onpart = ""; }
										else { sc.setChanOption( chan, "ADD", "ONPART", onpart ); }

										if (ownerkey.equals("ERROR")) { ownerkey = ""; }
										else { sc.setChanOption( chan, "ADD", "OWNERKEY", ownerkey ); }
										
										if (hostkey.equals("ERROR")) { hostkey = ""; }
										else { sc.setChanOption( chan, "ADD", "HOSTKEY", hostkey ); }										
										
										if (cmodes.equals("ERROR")) { cmodes = "+nt"; }
										sc.setChanOption( chan, "ADD", "CHANMODES", cmodes );
																				
										if (climit.equals("ERROR")) { climit = "0"; }
										sc.setChanOption( chan, "ADD", "LIMIT", climit );
										
										//String topic = sc.getChanOption( (String)found.elementAt(f), "TOPIC" );
										

											
										
										String infoString = cmmd.length() + " " + cmmd + " " +
															topic.length() + " " + topic + " " +
															onjoin.length() + " " + onjoin  + " " +
															onpart.length() + " " + onpart  + " " +
															ownerkey  + " " +
															hostkey  + " " +
															cmodes + " " +
															climit + " ";
										
										sc.writeData(O_CLONE_CHANNEL + " " + uID + " " + infoString + (String)found.elementAt(f) );
									}
									

									

								}
								
								
								if (allowedChans.size() > 0) {
									String tmp = "";
									for (int f=0; f<allowedChans.size(); f++)
										tmp = tmp + "," + (String)allowedChans.elementAt(f);
									
									sc.writeData( O_CHANJOIN_ALLOWED + " " + tmp.substring(1) + " " + uID + " " + cmmd );
								}
							
								
								
							}
						}
						
						
						else if (cmd.equals(":" + sc.getServerFakeName())) {
							//raws are being sent to us ?
							if (st.countTokens() > 1) {
								//display relevant info instead
								
								String raw = st.nextToken();
								if (raw.equals("001") && (st.countTokens() == 5)) {
									String mn = st.nextToken();
										   mn = st.nextToken();
										   mn = st.nextToken();
										   mn = st.nextToken();
										   mn = st.nextToken();
									data = "Nice to meet you " + mn;
								} else if (raw.equals("002") && (st.countTokens() == 10)) {
									String v = st.nextToken();
										   v = st.nextToken();
										   v = st.nextToken();
										   v = st.nextToken();
										   v = st.nextToken();
										   v = st.nextToken();
										   v = st.nextToken();
										   v = st.nextToken();
										   v = st.nextToken();
											
											float tver = 0;
											try {
												tver = Float.parseFloat( v );
											} catch( NumberFormatException e) { tver = -1; }
											
											if (tver < compatibleTES) {
												if (!quiet) {
													if (tver == -1) {
														System.out.print("Could not determine TES version from server. ");
													} else {
														System.out.print("Old TES version (" + v + ") detected. ");
													}
													System.out.println("Auto-removing server: " + i);
													halt();
												}
												removeServer( i, 1 );
											} else {
											   data = "I'm running TES v. " + v;
											}
											
											
											
								} else if (raw.equals("432")) {
									removeServer( i, 3 );
									halt();
									if (!quiet) { System.out.println("Hub Password not accepted on :" + sc.getServerName() + ". Auto-removing server from list"); }
								} else {
									halt();
								}
								
								
							}							
						}
						//else if (data.endsWith( " 432 * NetHub :Erroneus Nickname" )) {
							//refused connection to the server
						//	sc.removeServer();
						//	if (!quiet) { System.out.println("Hub Password not accepted on :" + sc.getServerName() + ". Auto-removing server from list"); }
						//}
						
						
						
						
						
							//display every statement passed to the server?
							if (halt) { halt = false; }
							else if (!quiet) { System.out.println("<--(" + sc.getServerName() + ") " + data ); }

						
						
						
		
		
			
	}//end parse()
	
	private void addConfLine( String line ) {
		if (conffile.exists()) {
		  try {
		   long fileLength = conffile.length();
		   RandomAccessFile raf = new RandomAccessFile(conffile, "rw");
		   //try {
			raf.seek(fileLength);
			raf.writeBytes(line + "\r\n");
			raf.close();
		  //} catch (IOException e) {}
		  //} catch (FileNotFoundException e) { }
		  } catch (Exception e) {}
		}
	}
	
	private String compress( String str ) {
		return compresser.encode(str);
	}
	private String decompress( String str ) {
		return compresser.decode(str);
	}
	
//end class commandProcessor
}