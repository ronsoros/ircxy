/**
 *	Tes hub - Java Threaded Echo Server.
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
 
import java.util.*;
import java.io.*;


class commandProcessor {

	Vector servers = null;
	Vector opers = null;
	serverConnection tmpsc = null;
	serverConnection sc;

	String cmd;
	String hubName = "hub";
	String hubDescription = "hub Description";
	int i = 0;
	int servercount = 0;
	String data;
	
	int highestUserCount = 0;
	int userCount = 0;
	
	boolean halt = false;
	boolean quiet = true;
	
	long hut = System.currentTimeMillis(); //Hub UpTime
	
	
	//Input messages - From servers to hub
	private String I_CHANLIST = 	"&1:";
	private String I_USERLIST = 	"&2:";
	private String I_CHANSETTING =  "&3:";
	private String I_SERVERDESC =   "&4:";
	private String I_NEWSERVER =	"&5:";
	private String I_LISTSERVERS =  "&6:";
	private String I_REMOVESERVER = "&7:";
	private String I_RECONSERVER =  "&8:";
	private String I_REQUEST_HUB_UPTIME = "&9:";
	private String I_REQUESTLINKS = "&10:";
	private String I_PASSINFO =     "&11:";
	private String I_OPER =		 "&12:";
	private String I_UNOPER =	   "&13:";
	private String I_CLEARACCESS =  "&14:";
	private String I_ADDACCESS =    "&15:";
	private String I_REMACCESS =    "&16:";
	private String I_GETWHOISFOR =  "&17:";
	private String I_USERCONNECTING = "&18:";
	private String I_CHANMODE =	 "&19:";
	private String I_RUMOC =		"&20:";
	private String I_SBOC =		 "&21:";
	private String I_USBOC =		"&22:";
	private String I_SUMOC =		"&23:";
	private String I_IDLER_QUIT =   "&24:";
	private String I_USERQUIT =	 "&25:";
	private String I_REQUEST_NICKCHANGE = "&26:";
	private String I_NICKCHANGE =   "&27:";
	private String I_CHANJOIN =	 "&28:";
	private String I_CHANPART =	 "&29:";
	private String I_NCE =		  "&30:";
	private String I_NC =		   "&31:";
	private String I_PMCE =		 "&32:";
	private String I_PMC =		  "&33:";
	private String I_PMU =		  "&34:";
	private String I_FINDTOPIC =    "&35:";
	private String I_TOPICCHANGE =  "&36:";
	private String I_CHANPROP = 	"&37:";
	private String I_KICK =		 "&38:";
	private String I_REQUEST_CHANMODES = "&39:";
	private String I_REQUEST_NAMES = "&40:";
	private String I_REQUEST_CHANJOIN = "&41:";

			
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
	
	
	
	
	
	
	private void halt() { halt = true; } //stops the current command from displaying any more output to _screen_

	
	
	public commandProcessor(boolean q) {
		if (q) {
			quiet = true;
			halt = true;
		}
		
		servers = new Vector();
		opers = new Vector();
	}



	private void addServer( serverConnection s ) {
		servers.addElement( s );
		servercount = servers.size();
		s.startSC();
	}
	private void removeServer( int index ) {
		if (index < servers.size()) {
			serverConnection rms = (serverConnection)servers.elementAt( index );
			rms.removeServer();
			//servercount = servers.size();
		}
	}


	public void setServers( Vector s ) {
		 servers = s;
		 servercount = servers.size();
	}
	public void setOpers( Vector o   ) { opers = o; }
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
		data = str.substring( str.indexOf(" ") );
		data = data.trim();

		sc = (serverConnection)servers.elementAt( i );
		
						//process all the data here.
						StringTokenizer st = new StringTokenizer( data );
						
						cmd = st.nextToken();
						
						if (cmd.equals(I_USERLIST)) {
							while (st.hasMoreTokens()) {
								sc.addUser( st.nextToken() );
								userCount++;
								if (userCount > highestUserCount) { highestUserCount = userCount; }
							}
							
							
							//find out if we have any nick collisions
							int uc = sc.userCount();
							String tmpU = "";
							serverConnection s = null;
							for (int k=0; k<uc; k++) {
								tmpU = sc.getUser(k);
								
								//dont kill ourself!
								if (!tmpU.equalsIgnoreCase("nethub")) {								
									int j=0;
									boolean found = false;
									while ((j<servercount) && (!found)) {
										if (j != i) {
											s = (serverConnection)servers.elementAt(j);
											if (s.isConnected() && s.hasUser( tmpU )) {
												found = true;
											}
										}
										j++;
									}
								
									if (found) {
										sc.writeData(O_NICKCOLLISION + " " + tmpU);
										userCount--;
										if (!quiet) { System.out.println( "Nick collision: " + sc.getServerName() + " - " + s.getServerName() ); }
									}
								}
							}
							
							
							//System.out.println( "P: userlist: " + sc.getUsers().toString() );
							
							//Request channel lists from the server
							sc.writeData( O_SENDCHANLISTS );
							
							for (int n=0; n<servercount; n++) {
								tmpsc = (serverConnection)servers.elementAt(n);
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
							if (v.size() > 0) {
								sc.addChan( v );
							}
				
							
							
							for (int k=0; k<servercount; k++) {
								if (i != k) {
									tmpsc = (serverConnection)servers.elementAt(k);
									if (tmpsc.isConnected() && (tmpsc.hasChan( chan ) != -1)) {
										sc.removeChan( chan );
										sc.writeData(O_CHANCOLLISION + " " + chan);
										if (!quiet) { System.out.println( "Channel collision: (" + chan + ") :: " + sc.getServerFakeName() + "<-->" + tmpsc.getServerFakeName() ); }
										k = servercount; //no need to keep checking, we've already removed the channel
									}
								}								
							}
							
							
							
							
							/*
							//check for any channel collisions
							int cc = sc.chanCount();
							serverConnection s2 = null;
							String tmpC = "";
							
							for (int k=0; k<cc; k++) {
								if (k < sc.getChanList().size()) {
									tmpC = sc.getChanName(k);
								
									int l=0;
									boolean found = false;
									while ((l<servercount) && (!found)) {
										if (l != i) {
											s2 = (serverConnection)servers.elementAt(l);
											if (s2.isConnected() && (s2.hasChan( tmpC ) != -1)) {
												found = true;
											}
										}
										l++;
									}
								
									if (found) {
										
										//sc.removeChan(tmpC.toLowerCase());
										sc.removeChan(tmpC);
										
										sc.writeData("CHAN_COLLISION " + tmpC);
										if (!quiet) { System.out.println( "Channel collision: (" + tmpC + ") :: " + sc.getServerName() + "<-->" + s2.getServerName() ); }
									}
								} else {
									k = cc;
								}
							}
							*/
							
							
							
							
							
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
						
						
						else if (cmd.equals("PING")) {
							sc.writeData("PING X");
							sc.writeData("PONG");
							//if (!quiet) { System.out.print("PING?"); }
							halt();
						}
						
						else if (cmd.equals("PONG")) {
							//if (!quiet) { System.out.println("PONG!"); }
							halt();
						}

						
						else if (cmd.equals(I_SERVERDESC)) {
							if (st.countTokens() >= 2) {
								String sn = st.nextToken();
								String serverDesc = data.substring(cmd.length() + sn.length() +2);
								sc.setServerFakeName( sn );
								//System.out.println("server fakename set to " + sn );
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
									else					 { c = "Disconnected"; }
									if (tmpsc.isRemoved())   { c = "Removed";      }


									sc.writeData( O_PASS_INFO + " " + uid + " NOTICE " + nick + " :Server " + i + " :: " + tmpsc.getServerFakeName() + " (Status: " + c + ")" );
								}
									
								sc.writeData( O_PASS_INFO + " " + uid + " NOTICE " + nick + " :End of Servers List" );
							}							
						}

						else if (cmd.equals(I_REMOVESERVER)) {
							if (st.countTokens() == 1) {
								try {
									int index = Integer.parseInt( st.nextToken() );
									removeServer( index );
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
								String nick = st.nextToken().toLowerCase();
								String level = st.nextToken();
								
								opers.addElement( nick + " " + level);
								
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
							if (st.countTokens() == 2) {
								String levels = st.nextToken();
								String chan = st.nextToken();
								
								for (int n=0; n<servercount; n++) {
									if (n != i) {
										tmpsc = (serverConnection)servers.elementAt(n);
										if (tmpsc.isConnected() && (tmpsc.hasChan(chan.toLowerCase()) != -1)) {
											tmpsc.writeData(O_CLEAR_ACCESS + " " + levels + " " + chan);
										}
									}
								}								

							}
						}
						
						else if (cmd.equals(I_ADDACCESS)) {
							if (st.countTokens() == 3) {
								String level = st.nextToken();
								String chan = st.nextToken();
								String mask = st.nextToken();
								
								for (int n=0; n<servercount; n++) {
									if (n != i) {
										tmpsc = (serverConnection)servers.elementAt(n);
										if (tmpsc.isConnected() && (tmpsc.hasChan(chan.toLowerCase()) != -1)) {
											tmpsc.writeData(O_ADD_ACCESS + " " + level + " " + chan + " " + mask);
										}
									}
								}
								
							}
						}
						
						else if (cmd.equals(I_REMACCESS)) {
							if (st.countTokens() == 3) {
								String level = st.nextToken();
								String chan = st.nextToken();
								String mask = st.nextToken();
								
								for (int n=0; n<servercount; n++) {
									if (n != i) {
										tmpsc = (serverConnection)servers.elementAt(n);
										if (tmpsc.isConnected() && (tmpsc.hasChan(chan.toLowerCase()) != -1)) {
											tmpsc.writeData(O_REM_ACCESS + " " + level + " " + chan + " " + mask);
										}
									}
								}
								
							}
						}
						
						else if (cmd.equals(I_GETWHOISFOR)) {
							if (st.countTokens() == 3) {
								String requester = st.nextToken();
								String whoisNick = st.nextToken();
								String ruid = st.nextToken();
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
									tmpsc.writeData(O_WHOIS_REQUEST + " " + i + " " + ruid + " " + requester + " " + whoisNick);
									
									
								}
								
								
							}							
						}
						
						
						
						else if (cmd.equals(I_USERCONNECTING)) {
							
							if (st.countTokens() == 1) {
								String tmpUser = st.nextToken();
								sc.addUser( tmpUser );
								userCount++;
								if (userCount > highestUserCount) { highestUserCount = userCount; }
								
								//notify the other servers of this user connecting.
								serverConnection s3 = null;
								
								for (int n=0; n<servercount; n++) {
									if (n != i) {
										s3 = (serverConnection)servers.elementAt(n);
										if (s3.isConnected())
											s3.writeData(O_USER_CONNECTED + " " + tmpUser + " " + sc.getServerFakeName() );
									}
								}
								
								for (int n=0; n<servercount; n++) {
									tmpsc = (serverConnection)servers.elementAt(n);
									if (tmpsc.isConnected()) { tmpsc.writeData( O_NEW_USERCOUNT + " " + userCount + " " + highestUserCount ); }
								}
							}
						}
						
						
						
						else if (cmd.equals(I_CHANMODE)) {
							//CHANMODE: userMask chan modes
							if (st.countTokens() >= 3) {
								String um = st.nextToken();
								String chan = st.nextToken();
								String modes = data.substring(cmd.length() + um.length() + chan.length() + 3);
								
								for (int n=0; n<servercount; n++) {
									tmpsc = (serverConnection)servers.elementAt(n);
									if ((n != i) && (tmpsc.isConnected()) && (tmpsc.hasChan(chan.toLowerCase()) != -1)) {
										tmpsc.writeData( O_CHAN_MODE + " " + um + " " + chan + " " + modes );
									}
									
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
												}


												n = servercount;

											}


										} else if (mode.equals("+v")) {
											if (uStat.equals("@") && !wStat.equals("n")) {
												
												//go through all the servers and inform them of the mode change
												//including server(i)
												serverConnection tmp = null;
												for (int s=0; s<servercount; s++) {
													tmp = (serverConnection)servers.elementAt(s);
													
													tmp.writeData( O_SUMOC + " " + um + " " + chan + " +v " + whoTo );
													if (wStat.equals("r")) { tmp.SUMOC(chan, whoTo, "+"); }
												}


												n = servercount;
											}


										} else if (mode.equals("-v")) {
											if (uStat.equals("@") && !wStat.equals("n")) {
												
												//go through all the servers and inform them of the mode change
												//including server(i)
												serverConnection tmp = null;
												for (int s=0; s<servercount; s++) {
													tmp = (serverConnection)servers.elementAt(s);
													
													tmp.writeData( O_SUMOC + " " + um + " " + chan + " -v " + whoTo );
													if (wStat.equals("r")) { tmp.SUMOC(chan, whoTo, ""); }
												}


												n = servercount;
											}


										}



											



									}
									
								}
								
								
								
							}
						}
						
						
						
						else if (cmd.equals(I_SBOC) || cmd.equals(I_USBOC)) {
							//SBOC: userMask chan banMask
							if (st.countTokens() == 3) {
								String um = st.nextToken();
								String chan = st.nextToken();
								String ban = st.nextToken();
								
								for (int n=0; n<servercount; n++) {
									if (n!=i) {
										tmpsc = (serverConnection)servers.elementAt(n);
																		
										if (tmpsc.isConnected() && (tmpsc.hasChan(chan.toLowerCase()) != -1)) {
											if (cmd.equals(I_SBOC)) {
												tmpsc.writeData(O_SBOC + " " + um + " " + chan + " " + ban);
											} else {
												tmpsc.writeData(O_USBOC + " " + um + " " + chan + " " + ban);
											}
										}
									}
									
								}
							}
						}
						

						
						
						else if (cmd.equals(I_SUMOC)) {
							//SUMOC: userMask chan +q whoTo
							if (st.countTokens() == 4) {
								String um = st.nextToken();
								String ustat = um.substring(0,1);
								um = um.substring(1);
								
								String chan = st.nextToken();
								String mode = st.nextToken();
								
								String whoTo = st.nextToken();
								String unick = um.substring(1, um.indexOf("!"));
								
								for (int n=0; n<servercount; n++) {
									tmpsc = (serverConnection)servers.elementAt(n);
									
									//if (n != i) {
									
									
									if (tmpsc.isConnected() && (tmpsc.hasChan( chan.toLowerCase() ) != -1)) {
										
										if      (mode.equals("+q") ) {
											if (n != i) {
												tmpsc.writeData( O_SUMOC + " " + um + " " + chan + " " + mode + " " + whoTo );
											}
											tmpsc.SUMOC(chan, whoTo, ".");
											//sc.SUMOC(chan,whoTo, ".");
										}
										else if (mode.equals("-q") ) {
											if (n != i) {
												tmpsc.writeData( O_SUMOC + " " + um + " " + chan + " " + mode + " " + whoTo );
											}
											tmpsc.SUMOC(chan, whoTo, "");
											//sc.SUMOC(chan,whoTo, ".");
										}
										
										
										

										else if (mode.equals("+o") ) {
											if (n != i) {
												tmpsc.writeData( O_SUMOC + " " + um + " " + chan + " " + mode + " " + whoTo );
											}
											tmpsc.SUMOC(chan, whoTo, "@");
											//sc.SUMOC(chan, whoTo, "@");
										}

										else if (mode.equals("-o") ) {
											if (n != i) {
												tmpsc.writeData( O_SUMOC + " " + um + " " + chan + " " + mode + " " + whoTo );
											}
											tmpsc.SUMOC(chan, whoTo, "");
											//sc.SUMOC(chan, whoTo, "");
										}
										
										else if (mode.equals("+v") ) {
											if (n != i) {
												tmpsc.writeData( O_SUMOC + " " + um + " " + chan + " " + mode + " " + whoTo );
											}
											
											if (tmpsc.userStatusOnChan(chan, whoTo).equals("r")) {
												tmpsc.SUMOC(chan, whoTo, "+");
												//sc.SUMOC(chan, whoTo, "+");
											}
										}

										else if (mode.equals("-v") ) {
											if (n != i) {
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
								for (int n=0; n<servercount; n++) {
									tmpsc = (serverConnection)servers.elementAt(n);
									if (tmpsc.isConnected()) { tmpsc.writeData( O_NEW_USERCOUNT + " " + userCount + " " + highestUserCount ); }
								}
							}
						}
						
						
						else if (cmd.equals(I_USERQUIT)) {
							if (st.countTokens() >= 3) {
								//USER_QUIT: <userMask> <#chan1>,<#chan2>,...   quitmessage
								String tmpUser = st.nextToken();
								String tmpChans = st.nextToken();
								String tmpMsg = data.substring( cmd.length() + tmpUser.length() + tmpChans.length() + 3 );
								tmpMsg = tmpMsg.trim();
								
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
								
								//notify the other servers of this user quitting.
								//serverConnection tmpsc = null;
								String tmpc = "";
								
								for (int n=0; n<servercount; n++) {
									//if (n != i) {
										//find out if the user was on any channels on that server,
										//and send a quit message to each user on that channel.
										tmpsc = (serverConnection)servers.elementAt(n);
										if (tmpsc.isConnected()) {
											for (int p=0; p<c.size(); p++) {
												tmpc = (String)c.elementAt(p);
												if (tmpsc.hasChan( tmpc.toLowerCase() ) != -1) {
													if (n != i) {
														tmpsc.writeData(O_USER_QUIT + " " + tmpUser + " " + tmpc + " " + tmpMsg );
													}
													//remove the user from that channel
													tmpsc.removeUserFromChan( tmpc, tmpUser.substring(1, tmpUser.indexOf("!")) );
													   //sc.removeUserFromChan( tmpc, tmpUser.substring(1, tmpUser.indexOf("!")) );
													//If the channel is now empty, remove it from our chanlist for that server.
													tmpsc.removeChanIfEmpty( tmpc );
													//sc.removeChanIfEmpty( tmpc );
												
												}											
											}
										}
										


									//}
								}
								for (int n=0; n<servercount; n++) {
									tmpsc = (serverConnection)servers.elementAt(n);
									if (tmpsc.isConnected()) { tmpsc.writeData( O_NEW_USERCOUNT + " " + userCount + " " + highestUserCount ); }
								}

							}
						}
						
						

						
						
						
						else if (cmd.equals(I_REQUEST_NICKCHANGE)) {
							
							if (st.countTokens() == 4) {
								String tmpNick = st.nextToken().toLowerCase();
								String tmpUID = st.nextToken();
								String tmp = st.nextToken();
								String tmpNewNick = st.nextToken();
								
								serverConnection s4 = null;
								boolean found = false;
								for (int n=0; n<servercount; n++) {
									if (n != i) {
										s4 = (serverConnection)servers.elementAt(n);
										if (!quiet) { System.out.println(s4.getUsers().toString()); }
										if (s4.isConnected() && s4.hasUser( tmpNewNick )) {
											found = true;
										} //else {
										//	System.out.println("(" + s4.getServerName() + " dhu " + tmpNick + ")");
										//}
										
										
									}
								}
								
								if (found) {
									//System.out.println("nickchange deined - " + "NICKCHANGE_DENIED " + tmpUID + " " + tmpNewNick);
									sc.writeData(O_NICKCHANGE_DENIED + " " + tmpUID + " " + tmpNewNick );
								} else {
									//allow the nickchange
									//System.out.println("nickchange allowed");
									sc.writeData(O_NICKCHANGE_ALLOWED + " " + tmpUID + " " + tmpNewNick );
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
							if (st.countTokens() == 2) {
								
								//CHAN_JOIN: userMask #channel
								String um = st.nextToken();
								String c = st.nextToken();
								
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
										if (tmpsc.isConnected() && (tmpsc.hasChan( c ) != -1)) {
											tmpsc.writeData(O_CHAN_JOIN + " " + um + " " + c );
										}
									}
								}

							}						
						}
							
						
						else if (cmd.equals(I_CHANPART)) {
							if (st.countTokens() == 2) {
								//CHAN_PART: userMask #channel
								String um = st.nextToken();
								String c = st.nextToken();
								String nick = um.substring(1, um.indexOf("!"));
								//if (c.charAt(0) == ':')
								//	c = c.substring(1);
								boolean done = false;
								
								for (int n=0; n<servercount; n++) {
									if (n != i) {
										//find out if the user was on that channel on that server,
										//and send a part message to each user on that channel.
										tmpsc = (serverConnection)servers.elementAt(n);
										
											if (tmpsc.isConnected() && (tmpsc.hasChan( c.toLowerCase() ) != -1)) {
												tmpsc.writeData(O_CHAN_PART + " " + um + " " + c );
												//remove the user from that channel
												done = tmpsc.removeUserFromChan( c.toLowerCase(), nick.toLowerCase() );
												if (!done && !quiet)
													System.out.println("Unable to remove user: " + nick + " from chan: " + c);
												
												//If the channel is now empty, remove it from our chanlist for that server.
												tmpsc.removeChanIfEmpty( c );
												
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
								//fm = fm.trim();
								
								for (int n=0; n<servercount; n++) {
									if (n != i) {
										tmpsc = (serverConnection)servers.elementAt(n);
									
										if (tmpsc.isConnected() && (tmpsc.hasChan( chan.toLowerCase() ) != -1 )) {
											tmpsc.writeData("PM " + um + " " + chan + " " + fm);
										}
									}
								}
							halt();
							}
						}						
						
						
						else if (cmd.equals(I_PMC)) {
							//PM: userMask #channel   full message
							if (st.countTokens() >= 3) {
								String um = st.nextToken();
								String chan = st.nextToken();
								String fm = data.substring(cmd.length() + um.length() + chan.length() + 3);
								//fm = fm.trim();
								
								boolean chanExists = false;
								for (int n=0; n<servercount; n++) {
									if (n != i) {
										tmpsc = (serverConnection)servers.elementAt(n);
									
										if (tmpsc.isConnected() && (tmpsc.hasChan( chan.toLowerCase() ) != -1 )) {
											tmpsc.writeData("PM " + um + " " + chan + " " + fm);
											chanExists = true;
										}
									}
								}
								
								if (!chanExists) {
									sc.writeData( O_NO_SUCH_CHANNEL + " " + um.substring(1, um.indexOf("!")) + " " + chan );
								}
								
							halt();
							}
						}
						
						
						else if (cmd.equals(I_PMU)) {
							if (st.countTokens() >= 3) {
								String um = st.nextToken();
								String whoTo = st.nextToken();
								String fm = data.substring(cmd.length() + um.length() + whoTo.length() + 3);
								
								boolean userExists = false;
								for (int n=0; n<servercount; n++) {
									if (n != i) {
										tmpsc = (serverConnection)servers.elementAt(n);
										
										if (tmpsc.isConnected() && (tmpsc.hasUser( whoTo.toLowerCase() ))) {
											tmpsc.writeData("PMU " + um + " " + whoTo + " " + fm );
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
										
										
										sc.writeData( O_PASS_INFO + " " + " 332 " + nick + " " + chan + " :" + topic );
										sc.writeData( O_PASS_INFO + " " + " 333 " + nick + " " + chan + " " + whoSetTopic + " " + timestamp);
									} else {
										sc.writeData( O_PASS_INFO + " " + " 331 " + nick + " " + chan + " :No topic is set.");
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
										tmpsc.setChanOption( chan, "REM", "TOPIC", tmpsc.getChanOption( chan, "TOPIC" ) ); //remove the old..
										tmpsc.setChanOption( chan, "ADD", "TOPIC", nick + " " + timestamp + " " + topic.substring(1) );   //add the new.
										if (n != i) {
											tmpsc.writeData( O_TOPIC_CHANGE + " " + um + " " + chan + " " + timestamp + " " + topic );
										}
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
							//KICK: [S]status userMask chan kickWho :reason for kick
							if (st.countTokens() >= 5) {
								
								String uStat = st.nextToken();
								String um = st.nextToken();
								String chan = st.nextToken();
								String kickWho = st.nextToken();
								String nick = st.nextToken();
								String reason = data.substring(cmd.length() + uStat.length() + um.length() + chan.length() + kickWho.length() + 5);
								
								boolean chanExists = false;
								
								sc.removeUserFromChan( chan, nick );
								int userStat = 0;
								if (uStat.startsWith("S"))
									userStat = Integer.parseInt(uStat.substring(1));
								else
									userStat = Integer.parseInt(uStat);
								
								
								boolean allowKick = false;
								
								for (int n=0; n<servercount; n++) {
									tmpsc = (serverConnection)servers.elementAt(n);
									
									if ( (n != i) && tmpsc.isConnected() && (tmpsc.hasChan(chan.toLowerCase()) != -1) ) {
										
										if (!uStat.startsWith("S")) {
											//normal users
											//find the server which has the kickWho user.
											if ((userStat == 3) || ((userStat == 2) && (!tmpsc.userStatusOnChan(chan, kickWho).equals("q")))) {
												allowKick = true;
											}
										} else {
											//user doing the kicking is a sysop
										}
										
									}									
								}
								
								if (allowKick) {
									for (int n=0; n<servercount; n++) {
										tmpsc = (serverConnection)servers.elementAt(n);
										if ( tmpsc.isConnected() && (tmpsc.hasChan(chan.toLowerCase()) != -1) ) {
											tmpsc.removeUserFromChan( chan, kickWho );
											tmpsc.removeChanIfEmpty( chan );
											//if (n!=i) {
												System.out.println("Kick: n=" + n + " :: " + um + " " + chan + " " + kickWho + " " + reason );
												tmpsc.writeData( O_KICK_FROM_CHAN + " " + um + " " + chan + " " + kickWho + " " + reason );
											//}
											//System.out.println("telling server " + "KICK_FROM_CHAN " + um + " " + chan + " " + kickWho + " " + reason );
										}
									}
								} else {
									sc.writeData( O_NOT_CHAN_OP + " " + nick + " " + chan );
								}
									
								
								
								if (!chanExists) {
									sc.writeData(O_NO_SUCH_CHANNEL + " " + nick + " " + chan );
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
								
								for (int n=0; n<servercount; n++) {
									tmpsc = (serverConnection)servers.elementAt(n);
									
									if (tmpsc.isConnected() && (tmpsc.hasChan(chan.toLowerCase()) != -1)) {
										tmpsc.writeData(O_REQUEST_CHANMODES + " " + nick + " " + uid + " " + i + " " + chan );
										n = servercount;
										chanExists = true;
									}
								}
								
								if (!chanExists) {
									sc.writeData(O_NO_SUCH_CHANNEL + " " + nick + " " + chan );
								}

							}
						}
						
						
						
						else if (cmd.equals(I_REQUEST_NAMES)) {
							//REQUEST_NAMES: userID nick  #channel
							if (st.countTokens() == 3) {
								String uid = st.nextToken();
								String nick = st.nextToken();
								String chan = st.nextToken();
								
								String names = "";
								int hc = -1;
								
								for (int n=0; n<servercount; n++) {
									tmpsc = (serverConnection)servers.elementAt(n);
									
									if (tmpsc.isConnected()) {
										hc = tmpsc.hasChan( chan.toLowerCase() );
										if (hc != -1)
											names = names + " " + tmpsc.namesList(hc);
									}
								}
								
								//System.out.println("hub: nameslist " + chan + " :" + names);
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
							//System.out.println(data);
							
							if (st.countTokens() > 1) {
							
								String allC = st.nextToken();
								String uID = st.nextToken();
								String nick = st.nextToken();
								String cmmd = data.substring(cmd.length() + allC.length() + uID.length() + nick.length() + 4);
								cmmd = cmmd.trim();
								
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
											
											
														if (s6.isConnected() && (s6.hasChan( c.toLowerCase() ) != -1)) {
															//System.out.println("chan " + c + " exists on " + s6.getServerName() );
															if (found.size() > 0)
																tmp2 = (String)found.lastElement();
																
																
															if (tmp2.startsWith( c + " :" ) ) {
																tmp2 = (String)found.elementAt(found.size()-1);
																tmp2 = tmp2 + " " + s6.namesList(s6.hasChan(c.toLowerCase()));
																
																found.setElementAt( tmp2, found.size()-1 );
															} else {
																found.addElement(c + " :" + s6.namesList(s6.hasChan(c.toLowerCase())));
																allowedChans.removeElement( c );
															}
														}
													}
												}
											}
									//}

								
								if (found.size() > 0) {
								
									//we must construct a names list and send it to the server,
									//to pass to the user
																		
									//clone-channels uid (cmmd asdf) #chan :names list
									for (int f =0; f<found.size(); f++) {
										System.out.println("clone channel -> " + uID + " (" + cmmd + ") " + (String)found.elementAt(f));
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
										
										
										for (int n=0; n<servercount; n++) {
											if (n != i) {
												tmpsc = (serverConnection)servers.elementAt(n);
												
												if (tmpsc.isConnected() && (tmpsc.hasChan( chan.toLowerCase() ) != -1)) {
													topic = tmpsc.getChanOption( chan, "TOPIC" );
													onjoin = tmpsc.getChanOption( chan, "ONJOIN" );
													onpart = tmpsc.getChanOption( chan, "ONPART" );
													ownerkey = tmpsc.getChanOption( chan, "OWNERKEY" );
													hostkey = tmpsc.getChanOption( chan, "HOSTKEY" );
													
													
													n = servercount; //terminate the for-loop
												}
											}										
											
										}



										sc.addChan( v );
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
										
										//String topic = sc.getChanOption( (String)found.elementAt(f), "TOPIC" );
										

											
										
										String infoString = cmmd.length() + " " + cmmd + " " +
															topic.length() + " " + topic + " " +
															onjoin.length() + " " + onjoin  + " " +
															onpart.length() + " " + onpart  + " " +
															ownerkey  + " " +
															hostkey  + " ";
										
										sc.writeData(O_CLONE_CHANNEL + " " + uID + " " + infoString + (String)found.elementAt(f) );
									}
									

									

								}
								
								
								if (allowedChans.size() > 0) {
									String tmp = "";
									for (int f=0; f<allowedChans.size(); f++)
										tmp = tmp + "," + (String)allowedChans.elementAt(f);
									
									//System.out.println("chanjoin allowed -> " + tmp.substring(1) );
									sc.writeData( O_CHANJOIN_ALLOWED + " " + tmp.substring(1) + " " + uID + " " + cmmd );
								}
							
								
								
							}
						}
						
						
						else if (cmd.equals(":" + sc.getServerFakeName())) {
							//raws are being sent to us ?
							if (st.countTokens() > 1) {
								//halt(); //display relevant info instead
								
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
											if (v.startsWith("1.2") || v.equals("1.300") || v.equals("1.301")) {
												System.out.println("WARNING! Tes v." + v + " detected. I need version 1.302 or greater ! Auto-removing server..");
												sc.removeServer();
												halt();
												
											} else {
											   data = "I'm running TES v. " + v;
											}
								} else if (raw.equals("432")) {
									sc.removeServer();
									halt();
									if (!quiet) { System.out.println("Hub Password not accepted on :" + sc.getServerName() + ". Auto-removing server from list"); }
								} else {
									halt();
								}
								
								
							}							
						}
						else if (data.endsWith( " 432 * NetHub :Erroneus Nickname" )) {
							//refused connection to the server
							sc.removeServer();
							if (!quiet) { System.out.println("Hub Password not accepted on :" + sc.getServerName() + ". Auto-removing server from list"); }
						}
						
						
						
						
						
							//display every statement passed to the server?
							if (halt) { halt = false; }
							else if (!quiet) { System.out.println("<--(" + sc.getServerName() + ") " + data ); }

						
						
						
		
		
			
	}//end parse()
	
	
	
	
//end class commandProcessor
}