/**
 * Copyright (C) 2001-2002 David Forrest
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 **/

import java.net.*;
import java.io.*;
import java.util.*;

/*
	ChanServ for:
	TES.
	Author: David Forrest (david@splog.net)


	CS acts as a connection to the server (think of CS as a sysop user)
	It can request certain info from the server to help it make decisions.
	It will sit in channels, giving chanop/owner status to certain users, etc.
*/

class CS implements Runnable {

	static boolean debug = false;
	String version = "1.2";


	char BOLD = 2; //mirc bold char

	static boolean spawn = true; //running as a spawn of TES ?
	static String server = "127.0.0.1";
	int port = 6667;
	Socket socket = null;

	//File conffile = new File("ircx.conf"); //you should make this the same as your ircx.conf file.
						//chanserv just uses it and extracts the relevant info.
	File cfile = new File("CS.conf");	//chanserv's main file. it reads/writes chan info to this file.
	File conffile = cfile;

	PrintWriter bw;
	String password = "";
	String preBit = "ChanServ -> ";
	String rehashPass = "" + System.currentTimeMillis() + "";

	//we're going to hold   [Channel-name][owner!mask@*.isp.co.uk]
	// no we are not ! String channels[][];
	int numChannels = 0;
	int chanCounter = -1;

	String lineToBeSent;
	BufferedReader br;
	int ERROR = 1;

	Vector chans = new Vector();
	Vector opers = new Vector();

	static boolean opDelay = true;
	static boolean killed = false;

 Thread thisThread;


  public CS(String serverIP, int Sport) {
    killed = false;
	server = serverIP;
	port = Sport;

	thisThread = new Thread(this);
	//thisThread.start();
  }

  public CS(String serverIP, int Sport, boolean start) {
  	killed = false;
  	server = serverIP;
  	port =   Sport;
  	thisThread = new Thread(this);
  	if (start) { thisThread.start(); }
  }


 public static void main (String args[]) {
	spawn = false;
	String a = server;
	int p = 6667;
	
	String arg = "";
	for (int i=0; i<args.length; i++) {
		arg = args[i];
		if (arg.startsWith("-p=")) {
		  try {
			p = Integer.parseInt(arg.substring(3));
		  } catch( NumberFormatException e ) {
		  	System.out.println("Usage: -p=<port>\t\tWhere <port> is an integer value");
		  	System.out.println("If -p is not specified, the default port of 6667 will be used.");
		  	System.exit(0);		  	
		  }
		}
		

		
		else if (arg.startsWith("-a=")) {
			a = arg.substring(3);
		}
		
		else if (arg.equals("-d")) {
			debug = true;
		}
		else if (arg.equals("-nod")) {
			opDelay = false;
		}
		
		
		else if (arg.equals("-h")) {
			System.out.println("Usage: java CS <option> <option> ...");
			System.out.println("\r\nValid options are:");
			System.out.println("\t\t" +		"-a=<address> \t The IP-Address of the TES server.");
			System.out.println("\t\t" +		"-p=<port> \t The port of the TES server");
			System.out.println("\t\t" +		"-nod\t\t No delay while opping users on join.");
			System.out.println("\t\t" +		"-d\t\t Enable Debug mode.");
			System.out.println("");
			System.exit(0);
		}
		else {
			System.out.println("Invalid option: " + arg);
			System.out.println("Use -h for help");
			System.exit(0);
		}
	

	}
		if (a.equals("")) {
			System.out.println("No Address specified");
			System.out.println("See: java CS -h\t\t for correct usage.\r\n");
			System.exit(0);
		} else {
			new CS(a, p, true);
		}
  }



	private void dprintln( String l ) {
		if (debug) { System.out.println( l ); }
	}
	private void dprint( String l ) {
		if (debug) { System.out.print( l ); }
	}



  public void run() {
  	System.out.println("Loading ChanServ: (" + server + "/" + port + ")");
  	while (!killed) {
  		//keep chanserv alive
  		if (connectService()) {
  			dprintln("Successfull connection");
  			processData(); //keep processing data until we get disconnected.
  		} else {
  			dprintln("Couldn't connect, retrying.");
  			try { Thread.sleep( 10000 ); } catch(Exception e) {}
  		}  		
  	} 	
  }



  public boolean connectService() {
  	boolean success = true;
  	try {
  		socket = new Socket(server, port);
  	} catch (Exception e) { success = false; }
  	
  	return success;
  }
  public void disconnectService() {
  	try {
  		socket.close();
  	} catch (Exception e) { }  	
  }


  public void processData() {


	try {
	    bw = new PrintWriter(socket.getOutputStream(),true);
	    BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream())); 

		//find out how many channels we are dealing with.
		countChannels();
		//noticeOpers("ChanServ is currently monitoring " + numChannels + " channels");

		//init the conf file here.
		initConfFile(socket);
		
		bw.print( "USER chanserv f f :/msg ChanServ HELP\r\n" );
		bw.flush();

		bw.print( "NICK ChanServ\r\n" );
		bw.flush();
		
		



		//join ALL the channels we are assigned to..
		//joinChans(socket, "#");
		readChans();


	  	StringTokenizer st;

		String message = "";
		String command = "";
		String mask = "";

		while(true) {
			message = input.readLine();
			//dprintln("incoming: " + message);

			if (message == null) {
				dprintln("Disconnected.");
				break;
			} else if ( (message.equals("")) || message.trim().equals("")  ) {
				//blank message
				
			} else {
				st = new StringTokenizer(message, " ");
	 			mask = st.nextToken();

				if (mask.equals("PING")) {
					bw.print("PONG\r\n");
					bw.flush();
				} else if (mask.equals("OPERS:")) {
					//dprint("opers(): ");
					while (st.hasMoreTokens()) {
						opers.add( st.nextToken().toLowerCase() );
						//dprint( (String)opers.elementAt( opers.size()-1 ) + ", " );
					}
					//dprintln("");
					
				}else if (mask.equals("SD")) {
					dprintln(message);
					//Service Data
					String t1 = "";
					if (st.countTokens()>0) { t1 = st.nextToken(); }
					String t2 = "";
					if (st.countTokens()>0) { t2 = st.nextToken(); }
					String t3 = "";
					if (st.countTokens()>0) { t3 = st.nextToken(); }
					String t4 = "";
					if (st.countTokens()>0) { t4 = st.nextToken(); }
					String t5 = "";
					if (st.countTokens()>0) { t5 = st.nextToken(); }
										
					if (t2.equals("JOIN") && !t3.equals("")) {
						t3 = t3.substring(1);
						//dprintln("JOIN " + t3);
						//user joined a channel
						if (monitoringChan(t3)) {
							int mc = getMC(t3);

							if (mc == 0) {
								//new channel
								//check if the user gets q/o/v
								t1 = t1.substring(1); //get rid of the : from usermask
								if (!nickFromMask(t1).equalsIgnoreCase("chanserv")) {

								   if ((userOwnsChan(t1, t3) || userIsAutoQ(t1, t3))
								   	|| (!t4.equals("") && getChanOption(t3, "password").equals(t4))) {
								   		
								    if (opDelay) {		
										try {
											Thread.sleep(1000);
										} catch (InterruptedException e) { }
									}
									
									bw.print("MODE " + t3 + " +qq " + nickFromMask(t1) + " :\r\n");
									bw.flush();
								   } else if (userIsAutoOp(t1, t3)) {
								   	
								    if (opDelay) {
										try {
											Thread.sleep(1000);
										} catch (InterruptedException e) { }
									}

									bw.print("MODE " + t3 + " +oo " + nickFromMask(t1) + " :\r\n");
									bw.flush();


								   } else {
								   	 if (opDelay) {
											try {
												Thread.sleep(1000);
											} catch (InterruptedException e) { }
										}
											
								   	bw.print("NOTICE " + t3 + " :" + t3 + " is a registered channel.\r\n");
								   	
								   	if (!opers.contains(nickFromMask(t1).toLowerCase())) {
								   		//don't deop sysops
								   		bw.print("MODE " + t3 + " -oo " + nickFromMask(t1) + " :\r\n");
								   	}
								   	bw.flush();
								   }
								}
					
								//set up the channel
								setupChan(t3);
								
								
							} else {
								//check if they get ops/q/etc
								t1 = t1.substring(1); //get rid of the : from usermask
								if (!nickFromMask(t1).equalsIgnoreCase("chanserv")) {
								   if (userOwnsChan(t1, t3) || userIsAutoQ(t1, t3)) {
								    if (opDelay) {
										try {
											Thread.sleep(1000);
										} catch (InterruptedException e) { }
									}
									
									bw.print("MODE " + t3 + " +qq " + nickFromMask(t1) + " :\r\n");
									bw.flush();
								   } else if (userIsAutoOp(t1, t3)) {
								    if (opDelay) {
										try {
											Thread.sleep(1000);
										} catch (InterruptedException e) { }
									}

									bw.print("MODE " + t3 + " +oo " + nickFromMask(t1) + " :\r\n");
									bw.flush();


								   }
								}
							}						
							updateMC(t3, mc+1); //increase the membercount
							addUserToChan( t3, nickFromMask(t1) );
						}
					} else if (t2.equals("PART") && !t3.equals("")) {
						//dprintln("PART " + t3);
						//user parted a channel
						
						if (monitoringChan(t3)) {
							int mc = getMC(t3);
							updateMC(t3, mc-1); //decrease the membercount
							remUserFromChan( t3, nickFromMask( t1 ) );
						}
					
					} else if (t2.equals("KICK") && !t3.equals("") && !t4.equals("") && !t5.equals("")) {
						if (monitoringChan(t3)) {
							int mc = getMC(t3);
							updateMC(t3, mc-1);
							remUserFromChan( t3, nickFromMask( t1 ) );
						}
					
					} else if (t2.equals("QUIT")) {
						changeNickOnChans( nickFromMask(t1), "" );					
					
					} else if (t2.equals("NICK") && !t3.equals("")) {
						changeNickOnChans( nickFromMask(t1), t3 );
						if (opers.removeElement( nickFromMask(t1).toLowerCase() )) {
							opers.add( t3.toLowerCase() );
						}						
					
					} else if (t1.equals("OPER:") && !t2.equals("")) {
						opers.add( t2.toLowerCase() );
						//dprintln("oper(): " + (String)opers.elementAt( opers.size()-1 ));
					
					} else if (t1.equals("UNOPER:") && !t2.equals("")) {
						if (opers.removeElement( t2.toLowerCase() )) {
							dprintln("successfull unoper(): " + t2);
						} else {
							dprintln("failed unoper(): " + t2);
						}
						
					} else if (t2.equals("TOPIC") && !t3.equals("") && !t4.equals("")) {
						if (monitoringChan(t3)) {
							String topic = message.substring(mask.length() + t1.length() + t2.length() + t3.length() + 4);
							if (topic.startsWith(":")) { topic = topic.substring(1); }
							
							if (getChanOption(t3, "static-topic").equalsIgnoreCase("ON")) {
								String oldTopic = getChanOption(t3, "topic");
								bw.print("TOPIC " + t3 + " :" + oldTopic);
								bw.flush();
							} else if (getChanOption(t3, "remember-topic").equalsIgnoreCase("ON")) {
								//save the topic
								if (getChanOption(t3, "topic").equals("")) {
									insertLine(t3, "\ttopic\t\t" + topic);
								} else {
									replaceOption(t3, "topic", "\ttopic\t\t" + topic);
								}
							}
							
						}
						
					
					}

				}

				mask = mask.substring(1); //get rid of the :

				if (st.countTokens() >= 1)
					command = st.nextToken();

	  			command = command.trim().toUpperCase();

				
				if (command.equals("353")) {
					//we're receiving names info
					if (st.countTokens() >= 4) {
						//nick = #chan :n1 n2 n3
						String nick = st.nextToken();	//get rid of nick
						String tmp = st.nextToken();			//get rid of =
						String chan = st.nextToken().toLowerCase();
						int membercount = st.countTokens(); //remaining tokens == users on channel
						updateMC( chan, membercount );
						String nl = "";
						while (st.hasMoreTokens()) { nl+= " " + st.nextToken(); }							
						replaceNamesList( chan, nl.substring(1) );
						
						//now we need to deop users on this channel
						deopOnChan( chan );
						setupChan(  chan );
					}
					


				} else if (command.equals("PRIVMSG")) {


				  if (st.countTokens() == 2) {
					if (st.nextToken().equalsIgnoreCase("chanserv")) {
					  String nb = st.nextToken();
					  if (nb.equalsIgnoreCase(":help")) {
					    bw.print("NOTICE " + nickFromMask(mask) + " :ChanServ allows you to register and control various\r\n");
					    bw.print("NOTICE " + nickFromMask(mask) + " :aspects of channels.  ChanServ can often prevent\r\n");
					    bw.print("NOTICE " + nickFromMask(mask) + " :malicious users from \"taking over\" channels by limiting\r\n");
					    bw.print("NOTICE " + nickFromMask(mask) + " :who is allowed channel operator priviliges.\r\n");
					    bw.print("NOTICE " + nickFromMask(mask) + " :To use chanserv commands type /msg Chanserv <command>\r\n");
					    bw.print("NOTICE " + nickFromMask(mask) + " :For help on a command type /msg Chanserv HELP <command>\r\n");
					    bw.print("NOTICE " + nickFromMask(mask) + " :Available <command>'s are:\r\n");
					    bw.print("NOTICE " + nickFromMask(mask) + " :   " + BOLD + "REGISTER" + BOLD + " -- Register a channel\r\n");
					    bw.print("NOTICE " + nickFromMask(mask) + " :   " + BOLD + "ACCESS" + BOLD + " -- Modify the list of priviledged users\r\n");
					    bw.print("NOTICE " + nickFromMask(mask) + " :   " + BOLD + "SET" + BOLD + " -- Modify channel specific options\r\n");
					    bw.print("NOTICE " + nickFromMask(mask) + " :   " + BOLD + "LIST" + BOLD + " -- View channel options/access list\r\n");
					    bw.print("NOTICE " + nickFromMask(mask) + " :   " + BOLD + "DROP" + BOLD + " -- Drop a registered channel\r\n");
					    bw.print("NOTICE " + nickFromMask(mask) + " :   " + BOLD + "INFO" + BOLD + " -- Channel/Chanserv information\r\n");
					    bw.flush();

					  } else if (nb.equalsIgnoreCase(":info")) {

						bw.print("NOTICE " + nickFromMask(mask) + " :I am ChanServ (version " + version + ")\r\n");
						bw.print("NOTICE " + nickFromMask(mask) + " :I'm currently monitoring" + BOLD + " " + numChannels + " " + BOLD + "channels\r\n");
						bw.print("NOTICE " + nickFromMask(mask) + " :For info on a specific channel try /chanserv INFO #channel\r\n");
						bw.flush();

					  }
					}

				  } else if (st.countTokens() == 3) {
					if (st.nextToken().equalsIgnoreCase("chanserv")) {
					  String nb2 = st.nextToken();
					  if (nb2.equalsIgnoreCase(":help")) {
						String hw = st.nextToken();
					     if (hw.equalsIgnoreCase("REGISTER")) {
						bw.print("NOTICE " + nickFromMask(mask) + " :Syntax: " + BOLD + "REGISTER #channel <password>" + BOLD + "\r\n");
						bw.print("NOTICE " + nickFromMask(mask) + " :e.g. /msg chanserv REGISTER #myChannel AberaKedabera\r\n");
						bw.print("NOTICE " + nickFromMask(mask) + " :" + BOLD + "Important" + BOLD + "; Your password will also serve as the default ownerkey for your channel. So keep it safe!\r\n");
						bw.flush();
					     } else if (hw.equalsIgnoreCase("ACCESS")) {
						bw.print("NOTICE " + nickFromMask(mask) + " :Syntax: " + BOLD + "ACCESS #channel <level> <mask> <password>" + BOLD + "\r\n");
						bw.print("NOTICE " + nickFromMask(mask) + " :e.g. /msg chanserv ACCESS #myChannel OP *!*@fbi.gov AberaKedabera\r\n");
						bw.print("NOTICE " + nickFromMask(mask) + " :Where <password> is the password used to REGISTER the channel\r\n");
						bw.print("NOTICE " + nickFromMask(mask) + " :-\r\n");
						bw.print("NOTICE " + nickFromMask(mask) + " :<level> can be:\r\n");
						bw.print("NOTICE " + nickFromMask(mask) + " :" + BOLD + "OP" + BOLD + " -- User gets auto-op when joining the channel.\r\n");
						bw.print("NOTICE " + nickFromMask(mask) + " :" + BOLD + "Q" + BOLD + " -- User gets auto-q when joining the channel.\r\n");

						bw.print("NOTICE " + nickFromMask(mask) + " :-\r\n");
						bw.print("NOTICE " + nickFromMask(mask) + " :" + BOLD + "ROP" + BOLD + " -- REMOVE an existing auto-op.\r\n");
						bw.print("NOTICE " + nickFromMask(mask) + " :" + BOLD + "RQ" + BOLD + " -- REMOVE an existing auto-q\r\n");


						bw.flush();
					     } else if (hw.equalsIgnoreCase("SET")) {
						bw.print("NOTICE " + nickFromMask(mask) + " :Syntax: " + BOLD + "SET #channel <option> <value> <password>" + BOLD + "\r\n");
						bw.print("NOTICE " + nickFromMask(mask) + " :Description: Sets up default channel options\r\n");
						bw.print("NOTICE " + nickFromMask(mask) + " :/msg ChanServ HELP LIST for information on viewing settings.\r\n");
						bw.print("NOTICE " + nickFromMask(mask) + " :-\r\n");
						bw.print("NOTICE " + nickFromMask(mask) + " :<option> can be:\r\n");
						bw.print("NOTICE " + nickFromMask(mask) + " :" + BOLD + "PASSWORD" + BOLD + " -- Change the channel password / default ownerkey.\r\n");
						bw.print("NOTICE " + nickFromMask(mask) + " :" + BOLD + "RTOPIC" + BOLD + " -- Remember Channel topics. <value> can be either ON/OFF for this option.\r\n");
												
						bw.print("NOTICE " + nickFromMask(mask) + " :-\r\n");						
						bw.print("NOTICE " + nickFromMask(mask) + " :<value>: <option> will be set to this value.\r\n");
						bw.print("NOTICE " + nickFromMask(mask) + " :<password>: The password used to REGISTER the channel initially\r\n");
						
						bw.flush();
						
					     }else if (hw.equalsIgnoreCase("LIST")) {
						bw.print("NOTICE " + nickFromMask(mask) + " :Syntax: " + BOLD + "LIST #channel <option> <password>" + BOLD + "\r\n");
						bw.print("NOTICE " + nickFromMask(mask) + " :Description: View channel settings/access list.\r\n");
						bw.print("NOTICE " + nickFromMask(mask) + " :-\r\n");
						bw.print("NOTICE " + nickFromMask(mask) + " :<option> can be:\r\n");
						bw.print("NOTICE " + nickFromMask(mask) + " :" + BOLD + "ACCESS" + BOLD + " -- List the users with channel access.\r\n");
						bw.print("NOTICE " + nickFromMask(mask) + " :" + BOLD + "SETTINGS" + BOLD + " -- List the channel's default settings.\r\n");
						bw.print("NOTICE " + nickFromMask(mask) + " :-\r\n");						
						bw.print("NOTICE " + nickFromMask(mask) + " :<password>: The password used to REGISTER the channel initially\r\n");
						bw.flush();
					     }

					  } else if (nb2.equalsIgnoreCase(":info")) {
						infoFor(st.nextToken(), mask, socket);

					  }
					}



				  } else if (st.countTokens() >= 3) {
					if (st.nextToken().equalsIgnoreCase("chanserv")) {
					  String nt = st.nextToken();
					  //dprintln(nt);
					  if (nt.equalsIgnoreCase(":rehash")) {
						  if (st.nextToken().equals(rehashPass)) {
							//numChannels = 0;
							//countChannels();
							//joinChans(socket, "#");
							//initConfFile(socket);
						  }

					
					  } else if (nt.equalsIgnoreCase(":SET")) {
						String chan = st.nextToken();

						if (st.countTokens() == 3) {
							String option = st.nextToken().toUpperCase();
							String value = st.nextToken();
							String pass = st.nextToken();
							if (passMatch(chan, pass)) {
								if (option.equals("PASSWORD")) {
									if (replaceOption(chan, option, "\t" + option.toLowerCase() + "\t\t" + value)) {
										bw.print("NOTICE " + nickFromMask(mask) + " :" + option + " changed sucessfully.\r\n");							
									} else {
										bw.print("NOTICE " + nickFromMask(mask) + " :Unknown error while setting: " + option + "\r\n");
									}									
									bw.flush();
								} else if (option.equals("RTOPIC")) {
									String crtopic = getChanOption( chan, "remember-topic" );
									if (crtopic.equals("")) { crtopic = "OFF"; }
									
									if (value.equalsIgnoreCase("ON")) {
										if (crtopic.equals("OFF")) { insertLine( chan, "\tremember-topic\t\tON" ); }
										bw.print("NOTICE " + nickFromMask(mask) + " :RTOPIC is now ON\r\n");
									} else if (value.equalsIgnoreCase("OFF")) {
										if (crtopic.equals("ON")) { replaceOption(chan, "remember-topic", "\tremember-topic\t\tOFF"); }
										bw.print("NOTICE " + nickFromMask(mask) + " :RTOPIC is now ON\r\n");
									} else {
										bw.print("NOTICE " + nickFromMask(mask) + " :Usage is SET RTOPIC ON/OFF\r\n");
									}									
									bw.flush();
				
								} else {
									bw.print("NOTICE " + nickFromMask(mask) + " :Unrecognised option: " + option + "\r\n");
									bw.flush();
								}
								
							} else {
								bw.print("NOTICE " + nickFromMask(mask) + " :Incorrect channel password.\r\n");
								bw.flush();
							}							
						} else {
							//incorrect syntax
						}


					  } else if (nt.equalsIgnoreCase(":LIST")) {
					  	String chan = st.nextToken();
					  	
						if (st.countTokens() == 2) {
							String option = st.nextToken().toUpperCase();
							String pass = st.nextToken();
							if (passMatch(chan, pass)) {
								if (option.equals("ACCESS")) {
									showUserAccessList( nickFromMask(mask), chan );
								} else if (option.equals("SETTINGS")) {
									showUserSettingsList( nickFromMask(mask), chan );
								} else {
									bw.print("NOTICE " + nickFromMask(mask) + " :Unrecognised option: " + option + "\r\n");
									bw.flush();
								}
								
							} else {
								bw.print("NOTICE " + nickFromMask(mask) + " :Incorrect channel password.\r\n");
								bw.flush();
							}							
						} else {
							//incorrect syntax
						}



					  } else if (nt.equalsIgnoreCase(":ACCESS")) {
						String chan = st.nextToken();
						if (st.countTokens() == 3) {
						  String ac = st.nextToken();
						  if (ac.equalsIgnoreCase("OP")) {
							String tmpMask = st.nextToken();
							String tmpPass = st.nextToken();
							if (passMatch(chan, tmpPass)) {
								insertLine(chan, "		auto-op		" + tmpMask);
								bw.print("NOTICE " + nickFromMask(mask) + " :Added auto-op" + BOLD + " " + tmpMask + " " + BOLD + "\r\n");
								bw.flush();
							} else {
								bw.print("NOTICE " + nickFromMask(mask) + " :You must be channel registrar/owner to do this\r\n");
								bw.flush();
							}
						  } else if (ac.equalsIgnoreCase("Q")) {
							String tmpMask = st.nextToken();
							String tmpPass = st.nextToken();
							if (passMatch(chan, tmpPass)) {
								insertLine(chan, "		auto-q		" + tmpMask);
								bw.print("NOTICE " + nickFromMask(mask) + " :Added auto-q" + BOLD + " " + tmpMask + " " + BOLD + "\r\n");
								bw.flush();
							} else {
								bw.print("NOTICE " + nickFromMask(mask) + " :You must be channel registrar/owner to do this\r\n");
								bw.flush();
							}


						  } else if (ac.equalsIgnoreCase("ROP")) {
							String tmpMask = st.nextToken();
							String tmpPass = st.nextToken();
							if (passMatch(chan, tmpPass)) {
								removeLine(chan, "		auto-op		" + tmpMask);
								bw.print("NOTICE " + nickFromMask(mask) + " :Removed auto-op" + BOLD + " " + tmpMask + " " + BOLD + "\r\n");
								bw.flush();
							} else {
								bw.print("NOTICE " + nickFromMask(mask) + " :You must be channel registrar/owner to do this\r\n");
								bw.flush();
							}

						  } else if (ac.equalsIgnoreCase("RQ")) {
							String tmpMask = st.nextToken();
							String tmpPass = st.nextToken();
							if (passMatch(chan, tmpPass)) {
								removeLine(chan, "		auto-q		" + tmpMask);
								bw.print("NOTICE " + nickFromMask(mask) + " :Removed auto-q" + BOLD + " " + tmpMask + " " + BOLD + "\r\n");
								bw.flush();
							} else {
								bw.print("NOTICE " + nickFromMask(mask) + " :You must be channel registrar/owner to do this\r\n");
								bw.flush();
							}

						  }

						}



					  } else if (nt.equalsIgnoreCase(":DROP")) {
						//user wishes to drop a channel.
						if (st.countTokens() == 2) {
						  String tmpChan = st.nextToken();
						  String tmpPass = st.nextToken();

						  //make sure this channel is a registered one..
						  if (chanIsRegd(tmpChan)) {

						     if (passMatch(tmpChan, tmpPass)) {
							removeChan(tmpChan);
							bw.print("NOTICE " + nickFromMask(mask) + " :Channel" + BOLD + " " + tmpChan + " " + BOLD + "has been DROPPED.\r\n");
							bw.print("NOTICE " + tmpChan + " :ChanServ is no longer monitoring this channel.\r\n");
							bw.print("PART " + tmpChan + "\r\n");
							bw.flush();


						     } else {
							bw.print("NOTICE " + nickFromMask(mask) + " :You must be channel registrar/owner to do this\r\n");
							bw.flush();
						     }



						  } else {
						    bw.print("NOTICE " + nickFromMask(mask) + " :" + BOLD + " " + tmpChan + " " + BOLD + "is not a registered channel!\r\n");
						    bw.flush();
						  }

						} else {
						  bw.print("NOTICE " + nickFromMask(mask) + " :DROP <#channel> <password>\r\n");
						  bw.flush();
						}





					  } else if (nt.equalsIgnoreCase(":REGISTER")) {
						//a user wants to reg a channel.
						//find out what channel & what user.
						dprintln("Received register request..");
						if (st.countTokens() == 2) {
						  String tmpChan = st.nextToken();
						  String tmpPass = st.nextToken();
						dprintln("chan=" + tmpChan + " pass=" + tmpPass);
						  //find out if 'anyone' owns this channel already..
						  if (!chanIsRegd(tmpChan)) {

							dprintln("Requesting RFR..");
						   //make a request to the server to see if this user is +q in the channel.
						   bw.print("R RFR " + tmpChan + " " + mask + " " + tmpPass + "\r\n");
						   bw.flush();

						  } else {
						    bw.print("NOTICE " + nickFromMask(mask) + " :This channel has already been registered\r\n");
						    bw.flush();
						  }

						} else {
						  bw.print("NOTICE " + nickFromMask(mask) + " :REGISTER <#channel> <password>\r\n");
						  bw.flush();
						}


					  }



					}
				  }






				} else if (command.equals("!R")) {
				  //server is acknowledging a Request we made previously.
				  if (st.countTokens() == 4) {
					if (st.nextToken().equals("RFR")) {
					   String tmpChan = st.nextToken();
					   String tmpMask = st.nextToken();
					   String tmpPass = st.nextToken();
					   String tmpNick = nickFromMask(tmpMask);


					   writeChanToFile(tmpChan, tmpMask, tmpPass);
						
						chans.add( tmpChan.toLowerCase() + " 0 :" );
						bw.print("NAMES " + tmpChan + "\r\n");
						bw.flush();
						
					   //join the channel
					   //bw.print("JOIN " + tmpChan + "\r\n");
					   bw.print("NOTICE " + tmpNick + " :" + BOLD + " " + tmpChan + " " + BOLD + "is now registered to you! (" + tmpMask + ")\r\n");
					   bw.print("NOTICE " + tmpChan + " :" + tmpChan + " :is now registered to " + tmpNick + "\r\n");
					   bw.print("NOTICE " + tmpNick + " :" + BOLD + " Please cycle in " + tmpChan + " to regain your status.\r\n");
					   bw.flush();

					}
				  }









				/*
				} else if (command.equals("JOIN")) {
				  if (st.countTokens() == 1) {
					//find out what channel the user joined.
					//if we are monitoring that channel, and the mask is that of the chanowner, +q them.
					String tmpChan = st.nextToken();
					tmpChan = tmpChan.substring(1); //get rid of the :#channel -> #channel
					if (!nickFromMask(mask).equalsIgnoreCase("chanserv")) {
					   if (userOwnsChan(mask, tmpChan) || userIsAutoQ(mask, tmpChan)) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) { }

						bw.print("MODE " + tmpChan + " +q " + nickFromMask(mask) + "\r\n");
						bw.flush();
					   } else if (userIsAutoOp(mask, tmpChan)) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) { }

						bw.print("MODE " + tmpChan + " +o " + nickFromMask(mask) + "\r\n");
						bw.flush();
					   }
					}

				  }
				 */ 
				  
				}
				




			}


		    }





	}
	catch (IOException e) {
	    dprintln(preBit + e);
	}

	try {
	    socket.close();
	}
	catch (IOException e) {
	    dprintln(preBit + e);
	}





  }


  public void countChannels() {
	if (cfile.exists()) {

	  try {
		FileReader s0 = new FileReader(cfile);
		BufferedReader s1 = new BufferedReader(s0);

		try {


			while (true) {
			  String line = s1.readLine();
			  if (line == null)
				break;

				  line = line.trim();
				  if ( line.length() >0 ) {
					if (line.charAt(0) == '#')
						numChannels++;
				  }


			}

			s1.close();

		    } catch(IOException e) {
			dprintln("Eeek:: " + e);
		    }
	  } catch (FileNotFoundException e) {
		dprintln("Error: " + e);
	  }
	}
  }

  public void leaveChan(Socket socket, String chan) {
	//try {
	    //tbw = new PrintWriter(socket.getOutputStream(),true);
	    bw.print("PART " + chan + "\r\n");
	    bw.flush();
	//}
	//catch (IOException e) {
	//    dprintln(preBit + e);
	//}  	
  }


	public boolean monitoringChan( String chan ) {
		boolean m = false;
		String tmpChan = "";
		chan = chan.toLowerCase();
		for (int i=0; i<chans.size(); i++) {
			tmpChan = (String)chans.elementAt(i);
			if (tmpChan.toLowerCase().startsWith( chan + " " )) {
				m = true;
				i = chans.size();
			}
		}
		return m;	
	}


	public void deopOnChan( String chan ) {
		//deop everyone on that channel
		String names = "";
		String deopnicks = "";
		String modes = "-";
		for (int i=0; i<chans.size(); i++) {
			names = (String)chans.elementAt(i);
			if (names.startsWith( chan + " " )) {
				names = names.substring( names.indexOf(":")+1 );
				StringTokenizer n = new StringTokenizer( names );
				String nick = "";
				while (n.hasMoreTokens()) {
					nick = n.nextToken().toLowerCase();
					if (nick.startsWith(".") || nick.startsWith("@")) {
						nick = nick.substring(1); //remove the status bit
						
						//don't de-op sysops
						if (!opers.contains(nick)) {
							modes+= "q"; // -q
							deopnicks+= " " + nick;
						}
					}
				}

				i = chans.size();
			}
		}
		if (deopnicks.length() > 0) {
			if (modes.length() == 2) {
				//this is to fix a bug with TES
				modes+= "q";
				deopnicks+= " :";
			}
			dprintln("deopOnChan(): " + "MODE " + chan + " " + modes + deopnicks );
			bw.print( "MODE " + chan + " " + modes + deopnicks + "\r\n");
			bw.flush();
		}
		
	}



	public void replaceNamesList( String chan, String nl ) {
		//dprintln("replaceNamesList(): " + chan + " " + nl);
		String tmpChan = "";
		chan = chan.toLowerCase();
		for (int i=0; i<chans.size(); i++) {
			tmpChan = (String)chans.elementAt(i);
			if (tmpChan.startsWith( chan + " " )) {
				dprintln( "replaceNamesList() {\r\n" + tmpChan + "\r\n" );
				String strStart = tmpChan.substring(0, tmpChan.indexOf(":"));
				chans.setElementAt( strStart + nl, i );
				dprintln( "" + chans.elementAt( i ) );
			}
		}		
	}
	
	public void changeNickOnChans( String oldNick, String newNick ) {
		String tmpChan = "";
		boolean isDebug = debug;
		dprintln( "changeNickOnChans(" + oldNick + "," + newNick + ") {" );

		for (int i=0; i<chans.size(); i++) {
			tmpChan = (String)chans.elementAt( i );
			tmpChan = tmpChan.substring(0, tmpChan.indexOf(" ")); //get the #chan name

			debug = false; //temporarily switch off debug
			if (remUserFromChan( tmpChan, oldNick )) {
				if (!newNick.equals("")) {
					addUserToChan(   tmpChan, newNick );
					debug = isDebug;
					dprintln("Nick changed on: " + tmpChan );
				} else {
					debug = isDebug;
					dprintln("Nick removed from: " + tmpChan);
				}
			}
		}
		debug = isDebug;
		dprintln( "}");
	}
	
	
	
	public void addUserToChan( String chan, String nick ) {
		dprintln("addUserToChan(): " + chan + " " + nick);
		String tmpChan = "";
		chan = chan.toLowerCase();
		for (int i=0; i<chans.size(); i++) {
			tmpChan = (String)chans.elementAt(i);
			if (tmpChan.startsWith( chan + " " )) {
				dprintln( "addUserToChan(" + chan + "," + nick + ") {\r\n" + tmpChan);
				if (tmpChan.endsWith(":")) {
					chans.setElementAt( tmpChan + nick, i );
				} else {
					chans.setElementAt( tmpChan + " " + nick, i );
				}
				dprintln( chans.elementAt( i ) + "\r\n}");
			}
		}		
	}
	public boolean remUserFromChan( String chan, String nick ) {
		//dprintln("remUserFromChan(): " + chan + " " + nick);
		String tmpChan = "";
		chan = chan.toLowerCase();
		boolean success = false;
		
		for (int i=0; i<chans.size(); i++) {
			tmpChan = (String)chans.elementAt(i);
			if (tmpChan.startsWith( chan + " " )) {
				dprintln( "remUserFromChan(" + chan + "," + nick + ") {\r\n" + tmpChan);
				//#chan <mc> :<nick1> <nick2> . . .
				StringTokenizer st = new StringTokenizer( tmpChan );
				String strStart = st.nextToken() + " " + st.nextToken(); //#chan <mc>
				String names = "";
				String tmpName = "";
				while (st.hasMoreTokens()) {
					tmpName = st.nextToken();
					if (tmpName.startsWith(":")) {
						tmpName = tmpName.substring(1);
					}
					if (tmpName.startsWith(".") || tmpName.startsWith("@") || tmpName.startsWith("+")) {
						nick = tmpName.charAt(0) + nick;
					}
					if (!tmpName.equalsIgnoreCase(nick)) {
						names = names + " " + tmpName;
					} else {
						success = true;
					}
				}
				
				if (names.length() > 0) { names = names.substring(1); }
				chans.setElementAt( strStart + " :" + names, i );
				dprintln( chans.elementAt( i ) + "\r\n}");
			}
		}
		return success;	
	}	


	public void updateMC( String chan, int mc ) {
		String tmpChan = "";
		chan = chan.toLowerCase();
		for (int i=0; i<chans.size(); i++) {
			tmpChan = (String)chans.elementAt(i);
			if (tmpChan.startsWith( chan + " " )) {
				dprintln( "updateMC():\r\n" + tmpChan + "\r\n" );
				String strStart = tmpChan.substring(0, tmpChan.indexOf(" "));
				String strEnd = tmpChan.substring(tmpChan.indexOf(" ")+1);
					   strEnd = strEnd.substring(strEnd.indexOf(" ")+1);
				chans.setElementAt( strStart + " " + mc + " " + strEnd, i );
				dprintln( "-> " + chans.elementAt( i ) );
			}
		}
	}
	public int getMC( String chan ) {
		String tmpChan = "";
		int mc = 0;
		chan = chan.toLowerCase();
		for (int i=0; i<chans.size(); i++) {
			tmpChan = (String)chans.elementAt(i);
			if (tmpChan.toLowerCase().startsWith( chan + " " )) {
				tmpChan = tmpChan.substring(tmpChan.indexOf(" ")+1);
				tmpChan = tmpChan.substring(0, tmpChan.indexOf(" "));
				try {
					mc = Integer.parseInt(tmpChan);
				} catch( NumberFormatException e ) { dprintln("Error: " + e); }
			}
		}
		return mc;
	}


	public void readChans() {
		if (cfile.exists()) {
	
		  try {
			FileReader s0 = new FileReader(cfile);
			BufferedReader s1 = new BufferedReader(s0);
			String curChan = "";
			String pushLine = "";
	
			try {
	
	
				while (true) {
				  String line = s1.readLine();
				  if (line == null)
					break;
	
					  //line = line.trim();
					  //strip tabs without trimming the line, prevents topic from being corrupted - Dana.
					  while (line.startsWith("\t"))
						line = line.substring(1);
	
					  if ( line.length() >0 ) {
						if ((line.toLowerCase().startsWith("#")) && (line.endsWith(" {"))) {
							curChan = line.substring(0, line.indexOf(" "));
							chans.addElement( curChan.toLowerCase() + " 0 :"); // #chan <membercount>
							//try {
							    //tbw = new PrintWriter(socket.getOutputStream(),true);
							    dprintln("Monitoring channel: " + curChan);
							    try { Thread.sleep(100); } catch (Exception e) {}
							    bw.print("NAMES " + curChan + "\r\n");
							    bw.flush();
							//} catch (IOException e) { dprintln(e); }
					    }
					  }
	
				}
	
				s1.close();
	
			    } catch(IOException e) {
				dprintln("Eeek:: " + e);
			    }
		  } catch (FileNotFoundException e) {
			dprintln("Error: " + e);
		  }
		}	
		
	}


  public void setupChan(String chan) {
	if (cfile.exists()) {

	  try {
		FileReader s0 = new FileReader(cfile);
		BufferedReader s1 = new BufferedReader(s0);
		String curChan = "";
		String pushLine = "";
		boolean eoc = false; //end of chan, i.e. when we reach "}"

		try {


			while (!eoc) {
			  String line = s1.readLine();
			  if (line == null)
				break;

				  //line = line.trim();
				  //strip tabs without trimming the line, prevents topic from being corrupted - Dana.
				  while (line.startsWith("\t") || line.startsWith("\b"))
					line = line.substring(1);

				  if ( line.length() >0 ) {
					if ((line.toLowerCase().startsWith(chan.toLowerCase() + " ")) && (line.endsWith(" {"))) {
						curChan = line.substring(0, line.indexOf(" "));
						dprintln("setupchan(): found chan in conf: " + curChan);
					} else if ((curChan != "") && line.trim().equals("}")) {
						eoc = true;
						dprintln("setupchan(): found eoc in conf: " + curChan);
						
					} else if (curChan != "") {
					    pushLine = "";
					    if (line.startsWith("topic")) {
					    	String tmp = line.substring(5);
					    	while (tmp.startsWith("\t"))
								tmp = tmp.substring(1);
							pushLine = "TOPIC " + curChan + " :" + tmp;
					    } else if (line.startsWith("password")) {							
							pushLine = "PROP " + curChan + " OWNERKEY :" + line.substring(8).trim();
					    } else if (line.startsWith("hostkey")) {
							pushLine = "PROP " + curChan + " HOSTKEY :" + line.substring(7).trim();
					    } else if (line.startsWith("modes")) {
							pushLine = "MODE " + curChan + " " + line.substring(5).trim();
					    }



						if (pushLine != "") {
						    dprintln("setupchan(): " + curChan + " Pushing: " + pushLine);
						    bw.print(pushLine + "\r\n");
						    bw.flush();
						}




					}
				  }


			}

			s1.close();

		    } catch(IOException e) {
			dprintln("Eeek:: " + e);
		    }
	  } catch (FileNotFoundException e) {
		dprintln("Error: " + e);
	  }
	}
  }

  public void initConfFile(Socket socket) {

	chanCounter = -1;

	if (conffile.exists()) {

	  try {
		FileReader s0 = new FileReader(conffile);
		BufferedReader s1 = new BufferedReader(s0);

		String tmpLine;
		String strOption = "";
		int strValue = 0;
		int tmpIndex = 0;

	    try {

		//read o-lines file into array
		String tmpChan = ""; //channel we're currently setting up.

		while (true) {
		  String line = s1.readLine();
		  if (line == null)
			break;

		  //deal with the line
		  line = line.trim();

		  if ( (line.length() >0) && (line.charAt(0) != '#') ) {

			tmpIndex = line.indexOf(" ");
			int tI = line.indexOf("\t");
			

			if ((tI != -1) &&    ((tmpIndex == -1) || (tmpIndex > tI))) {
					tmpIndex = tI;

					strOption = line.substring(0, tmpIndex);
					tmpLine = line.substring(tmpIndex+1).trim();
		
					if (strOption.equalsIgnoreCase("o-line-password")) {
							    bw.print( "PASS " + tmpLine + "\r\n");
							    bw.flush();
					}
		
					else if (strOption.equalsIgnoreCase("chan-serv-rehashpass")) {
						rehashPass = tmpLine;
					}


			}

		  }


		}

		s1.close();

	    } catch(IOException e) {
		dprintln("Eeek:: " + e);
	    }
	  } catch (FileNotFoundException e) {
		dprintln("Error: " + e);
	  }

	} else {
		System.out.println("No configuration file found: " + conffile);
		System.out.println(" --> You NEED a conf file !!! <--");
		System.out.println("");
		System.out.println("Go make one..");
		if (!spawn) {
			System.exit(0);
		}
	}


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




	public String getChanOption( String chan, String option ) {
		//go through the file and find the 'owner' line.

		String result = "";
		boolean atChan = false; //we have reached the specified channel in the CS.conf file.
					//now start looking for the 'owner' line.


		if (cfile.exists()) {

		  try {
			FileReader s0 = new FileReader(cfile);
			BufferedReader s1 = new BufferedReader(s0);

			try {
				while (result.equals("")) {
				  String line = s1.readLine();
				  if (line == null)
					break;

					  line = line.trim();
					  if ( line.length() >0 ) {
						if (line.equalsIgnoreCase(chan + " {"))
							atChan = true;
						else if (line.equals("}"))
							atChan = false; //reached the end of current #chan definition.
						else if (atChan && line.startsWith(option)) {
							line = line.substring(option.length()).trim();
							result = line;
						}

					  }


				}

				s1.close();

		 	   } catch(IOException e) {
				dprintln("Eeek:: " + e);
		 	   }
	  	} catch (FileNotFoundException e) {
			dprintln("Error: " + e);
		  }
		}


		dprintln("getChanOption(" + chan + "," + option + ") = " + result);
		return result;

	}




	public boolean userOwnsChan( String mask, String chan ) {
		//go through the file and find the 'owner' line.

		boolean ownschan = false;
		boolean atChan = false; //we have reached the specified channel in the CS.conf file.
					//now start looking for the 'owner' line.


		if (cfile.exists()) {

		  try {
			FileReader s0 = new FileReader(cfile);
			BufferedReader s1 = new BufferedReader(s0);

			try {
				while (!ownschan) {
				  String line = s1.readLine();
				  if (line == null)
					break;

					  line = line.trim();
					  if ( line.length() >0 ) {
						if (line.equalsIgnoreCase(chan + " {"))
							atChan = true;
						else if (line.equals("}"))
							atChan = false; //reached the end of current #chan definition.
						else if (atChan && line.startsWith("registrar")) {
							line = line.substring(9).trim();
							ownschan = matches(line, mask);

							//dprintln(preBit + "ownschan(): " + line + " " + mask + " -> " + ownschan );
						}

					  }


				}

				s1.close();

		 	   } catch(IOException e) {
				dprintln("Eeek:: " + e);
		 	   }
	  	} catch (FileNotFoundException e) {
			dprintln("Error: " + e);
		  }
		}




		return ownschan;

	}



	public boolean chanIsRegd( String chan ) {
		boolean atChan = false; //we have reached the specified channel in the CS.conf file.

		if (cfile.exists()) {

		  try {
			FileReader s0 = new FileReader(cfile);
			BufferedReader s1 = new BufferedReader(s0);

			try {
				while (!atChan) {
				  String line = s1.readLine();
				  if (line == null)
					break;

					  line = line.trim();
					  if (  (line.length() >0 ) && line.equalsIgnoreCase(chan + " {")  )
							atChan = true;

				}

				s1.close();

		 	   } catch(IOException e) {
				dprintln("Eeek:: " + e);
		 	   }
	  	} catch (FileNotFoundException e) {
			dprintln("Error: " + e);
		  }
		}




		return atChan;

	}





	public boolean userIsAutoOp( String mask, String chan ) {
		//go through the file and find the 'owner' line.

		boolean autoop = false;
		boolean atChan = false; //we have reached the specified channel in the CS.conf file.
					//now start looking for the 'auto-op' line.


		if (cfile.exists()) {

		  try {
			FileReader s0 = new FileReader(cfile);
			BufferedReader s1 = new BufferedReader(s0);

			try {
				while (!autoop) {
				  String line = s1.readLine();
				  if (line == null)
					break;

					  line = line.trim();
					  if ( line.length() >0 ) {
						if (line.equalsIgnoreCase(chan + " {"))
							atChan = true;
						else if (line.equals("}"))
							atChan = false; //reached the end of current #chan definition.
						else if (atChan && line.startsWith("auto-op")) {
							line = line.substring(7).trim();
							autoop = matches(line, mask);
						}

					  }


				}

				s1.close();

		 	   } catch(IOException e) {
				dprintln("Eeek:: " + e);
		 	   }
	  	} catch (FileNotFoundException e) {
			dprintln("Error: " + e);
		  }
		}




		return autoop;

	}


	public void showUserAccessList( String nick, String chan ) {
		boolean atChan = false; //we have reached the specified channel in the CS.conf file.
					//now start looking for the 'auto-op' line.
		boolean done = false;

		if (cfile.exists()) {

		  try {
			FileReader s0 = new FileReader(cfile);
			BufferedReader s1 = new BufferedReader(s0);

			try {
				while (!done) {
				  String line = s1.readLine();
				  if (line == null)
					break;

					  line = line.trim();
					  if ( line.length() >0 ) {
						if (line.equalsIgnoreCase(chan + " {")) {
							atChan = true;
							bw.print("NOTICE " + nick + " :Start of Access List for " + chan + "\r\n");
						} else if (atChan && line.equals("}")) {
							atChan = false; //reached the end of current #chan definition.
							done = true;
							bw.print("NOTICE " + nick + " :End of Access List for " + chan + "\r\n");
							bw.flush();							
						} else if (atChan) {
							if (line.startsWith("auto-q")) {
								bw.print("NOTICE " + nick + " :" + BOLD + "Auto +q: " + BOLD + line.substring(6).trim() + "\r\n");
							} else if (line.startsWith("auto-op")) {
								bw.print("NOTICE " + nick + " :" + BOLD + "Auto +o: " + BOLD + line.substring(7).trim() + "\r\n");
							}
						}

					  }


				}

				s1.close();

		 	   } catch(IOException e) {
				dprintln("Eeek:: " + e);
		 	   }
	  	} catch (FileNotFoundException e) {
			dprintln("Error: " + e);
		  }
		}

	}


	public void showUserSettingsList( String nick, String chan ) {
		boolean atChan = false; //we have reached the specified channel in the CS.conf file.
					//now start looking for the 'auto-op' line.
		boolean done = false;

		if (cfile.exists()) {

		  try {
			FileReader s0 = new FileReader(cfile);
			BufferedReader s1 = new BufferedReader(s0);

			try {
				while (!done) {
				  String line = s1.readLine();
				  if (line == null)
					break;

					  line = line.trim();
					  if ( line.length() >0 ) {
						if (line.equalsIgnoreCase(chan + " {")) {
							atChan = true;
							bw.print("NOTICE " + nick + " :Start of Settings List for " + chan + "\r\n");
						} else if (atChan && line.equals("}")) {
							atChan = false; //reached the end of current #chan definition.
							done = true;
							bw.print("NOTICE " + nick + " :End of Settings List for " + chan + "\r\n");
							bw.flush();							
						} else if (atChan) {
							int index = line.indexOf("\b");
							if (index == -1) { index = line.indexOf("\t"); }
							String option = "";
							if (index != -1) {
								option = line.substring(0, index).toLowerCase();
								String value = line.substring(index).trim();
								
								if (!option.equals("auto-q") && !option.equals("auto-op") && !option.equals("password") ) {
									bw.print("NOTICE " + nick + " :" +BOLD+ option +BOLD+ ": " + value + "\r\n");
								}
							}
						}

					  }


				}

				s1.close();

		 	   } catch(IOException e) {
				dprintln("Eeek:: " + e);
		 	   }
	  	} catch (FileNotFoundException e) {
			dprintln("Error: " + e);
		  }
		}

	}


	public boolean userIsAutoQ( String mask, String chan ) {
		//go through the file and find the 'owner' line.

		boolean autoop = false;
		boolean atChan = false; //we have reached the specified channel in the CS.conf file.
					//now start looking for the 'auto-op' line.


		if (cfile.exists()) {

		  try {
			FileReader s0 = new FileReader(cfile);
			BufferedReader s1 = new BufferedReader(s0);

			try {
				while (!autoop) {
				  String line = s1.readLine();
				  if (line == null)
					break;

					  line = line.trim();
					  if ( line.length() >0 ) {
						if (line.equalsIgnoreCase(chan + " {"))
							atChan = true;
						else if (line.equals("}"))
							atChan = false; //reached the end of current #chan definition.
						else if (atChan && line.startsWith("auto-q")) {
							line = line.substring(6).trim();
							autoop = matches(line, mask);
						}

					  }


				}

				s1.close();

		 	   } catch(IOException e) {
				dprintln("Eeek:: " + e);
		 	   }
	  	} catch (FileNotFoundException e) {
			dprintln("Error: " + e);
		  }
		}




		return autoop;

	}



	public void infoFor( String chan, String mask, Socket socket ) {
		boolean atChan = false;
		String infoString = "";
		String nb = "NOTICE " + nickFromMask(mask) + " :";

		if (cfile.exists()) {

		  try {
			FileReader s0 = new FileReader(cfile);
			BufferedReader s1 = new BufferedReader(s0);

			try {
				while (true) {
				  String line = s1.readLine();
				  if (line == null)
					break;

					  line = line.trim();
					  if ( line.length() >0 ) {
						if (line.equalsIgnoreCase(chan + " {"))
							atChan = true;
						else if (line.equals("}"))
							atChan = false; //reached the end of current #chan definition.

						else if (atChan) {
						    if (line.startsWith("registrar")) {
							line = line.substring(9).trim();
							line = line.substring(0, line.indexOf("!"));
							infoString = infoString + nb + "Channel Info for: " + chan + "\r\n";
							infoString = infoString + nb + "Registrar: " + line + "\r\n";
						    } else if (line.startsWith("modes")) {
							line = line.substring(5).trim();
							infoString = infoString + nb + "Forced Modes: " + line + "\r\n";
						    } else if (line.startsWith("regon")) {
							line = line.substring(5).trim();
							infoString = infoString + nb + "Registered on: " + line + "\r\n";
						    }


						}

					  }


				}

				s1.close();

		 	   } catch(IOException e) {
				dprintln("Eeek:: " + e);
		 	   }
	  	} catch (FileNotFoundException e) {
			dprintln("Error: " + e);
		  }
		}


		if (infoString.equals(""))
		  infoString = "NOTICE " + nickFromMask(mask) + " :Channel is not registerd: " + chan + "\r\n";

		//try {
		    //bw = new PrintWriter(socket.getOutputStream(),true);
		    bw.print(infoString);
		    bw.flush();
		//}
		//catch (IOException e) { }



	}



	public boolean passMatch( String chan, String pass ) {
		//go through the file and find the 'owner' line.

		boolean autoop = false;
		boolean atChan = false; //we have reached the specified channel in the CS.conf file.
					//now start looking for the 'auto-op' line.


		if (cfile.exists()) {

		  try {
			FileReader s0 = new FileReader(cfile);
			BufferedReader s1 = new BufferedReader(s0);

			try {
				while (!autoop) {
				  String line = s1.readLine();
				  if (line == null)
					break;

					  line = line.trim();
					  if ( line.length() >0 ) {
						if (line.equalsIgnoreCase(chan + " {"))
							atChan = true;
						else if (line.equals("}"))
							atChan = false; //reached the end of current #chan definition.
						else if (atChan && line.startsWith("password")) {
							line = line.substring(8).trim();
							autoop = line.equals(pass);
						}

					  }


				}

				s1.close();

		 	   } catch(IOException e) {
				dprintln("Eeek:: " + e);
		 	   }
	  	} catch (FileNotFoundException e) {
			dprintln("Error: " + e);
		  }
		}




		return autoop;

	}



	public String nickFromMask( String mask ) {
		//UmBonGo!djf@tes.splog.net
		if (mask.charAt(0) == ':') { mask = mask.substring(1); }
		return mask.substring(0, mask.indexOf("!"));
	}


	public void writeChanToFile( String chan, String ownerMask, String pass ) {
		if (cfile.exists()) {
		  try {
		   long fileLength = cfile.length();
		   RandomAccessFile raf = new RandomAccessFile(cfile, "rw");
		   try {
		   raf.seek(fileLength);

			Date dt = new Date();
 			String regon = dt.toGMTString();

			
			//11 Aug 2002 17:25:56 GMT
			/*
			Calendar calendar = new GregorianCalendar();
			Date date = calendar.getTime();
			DateFormat localFormat = DateFormat.getDateInstance();
			System.out.println( "Localized  " + localFormat.format( date ) );
            */            
                           

		   String chanLine = "\r\n";
		   		chanLine = chanLine + chan + " {\r\n";
				chanLine = chanLine + 	"	registrar		" + ownerMask + "\r\n";
				chanLine = chanLine +   "	password		" + pass      + "\r\n";
				chanLine = chanLine +   "	regon			" + regon     + "\r\n";
				chanLine = chanLine + "}\r\n";


		   raf.writeBytes(chanLine);
		   raf.close();
		  } catch (IOException e) {}
		  } catch (FileNotFoundException e) { }

		}

	}


	public void insertLine( String chan, String lti ) {
	   if (cfile.exists()) {
		String lines[];
		int lc = 0;

		try {
			FileReader s0 = new FileReader(cfile);
			BufferedReader s1 = new BufferedReader(s0);

			try {
				String tmpLine;
				while ( (tmpLine = s1.readLine()) != null )
					lc++;

				s1.close();



				lines = new String[lc];

				s0 = new FileReader(cfile);
				s1 = new BufferedReader(s0);

				int index = 0;
				while (true) {
				  String line = s1.readLine();
				  if (line == null)
					break;

				  //deal with the line
				  lines[index] = line;
				  index++;
				}
				s1.close();


				PrintWriter pout = new PrintWriter(new FileWriter(cfile));

				int i=0;
				boolean atPlace = false;
				while (i < lc) {
				  pout.println(lines[i]);
				  if (lines[i].equalsIgnoreCase(chan + " {"))
					pout.println(lti);

				  i++;
				}


				pout.close();




		 	   } catch(IOException e) {
				dprintln("Eeek:: " + e);
		 	   }
	  	} catch (FileNotFoundException e) {
			dprintln("Error: " + e);
		}




	   }
	}


	public boolean replaceOption( String chan, String ltr, String lta ) {
	   boolean success = false;
	   
	   if (cfile.exists()) {
		String lines[];
		int lc = 0;

		try {
			FileReader s0 = new FileReader(cfile);
			BufferedReader s1 = new BufferedReader(s0);

			try {
				String tmpLine;
				while ( (tmpLine = s1.readLine()) != null )
					lc++;

				s1.close();



				lines = new String[lc];

				s0 = new FileReader(cfile);
				s1 = new BufferedReader(s0);

				int index = 0;
				while (true) {
				  String line = s1.readLine();
				  if (line == null)
					break;

				  //deal with the line
				  lines[index] = line;
				  index++;
				}
				s1.close();


				PrintWriter pout = new PrintWriter(new FileWriter(cfile));

				int i=0;
				boolean atChan = false;
				while (i < lc) {
				  if (lines[i].equalsIgnoreCase(chan + " {"))
					atChan = true;
				  else if (lines[i].equals("}"))
					atChan = false;


				  if (atChan && lines[i].trim().toLowerCase().startsWith(ltr.toLowerCase())) {
					pout.println(lta);
					success = true;
				  } else {
					pout.println(lines[i]);
				  }

				  i++;
				}


				pout.close();




		 	   } catch(IOException e) {
				dprintln("Eeek:: " + e);
		 	   }
	  	} catch (FileNotFoundException e) {
			dprintln("Error: " + e);
		}




	   }
	   return success;
	}


	public void removeLine( String chan, String ltr ) {
	   if (cfile.exists()) {
		String lines[];
		int lc = 0;

		try {
			FileReader s0 = new FileReader(cfile);
			BufferedReader s1 = new BufferedReader(s0);

			try {
				String tmpLine;
				while ( (tmpLine = s1.readLine()) != null )
					lc++;

				s1.close();



				lines = new String[lc];

				s0 = new FileReader(cfile);
				s1 = new BufferedReader(s0);

				int index = 0;
				while (true) {
				  String line = s1.readLine();
				  if (line == null)
					break;

				  //deal with the line
				  lines[index] = line;
				  index++;
				}
				s1.close();


				PrintWriter pout = new PrintWriter(new FileWriter(cfile));

				int i=0;
				boolean atChan = false;
				while (i < lc) {
				  if (lines[i].equalsIgnoreCase(chan + " {"))
					atChan = true;
				  else if (lines[i].equals("}"))
					atChan = false;


				  if (atChan && lines[i].equalsIgnoreCase(ltr)) {
					//miss out the line
				  } else {
					pout.println(lines[i]);
				  }

				  i++;
				}


				pout.close();




		 	   } catch(IOException e) {
				dprintln("Eeek:: " + e);
		 	   }
	  	} catch (FileNotFoundException e) {
			dprintln("Error: " + e);
		}




	   }
	}



	public void removeChan( String chan ) {
		String tmpChan = "";
		for (int i=0; i<chans.size(); i++) {
			tmpChan = (String)chans.elementAt(i);
			if (tmpChan.toLowerCase().startsWith( chan.toLowerCase() + " " )) {
				chans.removeElementAt(i);
				i = chans.size();
			}
		}


	   if (cfile.exists()) {
		String lines[];
		int lc = 0;

		try {
			FileReader s0 = new FileReader(cfile);
			BufferedReader s1 = new BufferedReader(s0);

			try {
				String tmpLine;
				while ( (tmpLine = s1.readLine()) != null )
					lc++;

				s1.close();



				lines = new String[lc];

				s0 = new FileReader(cfile);
				s1 = new BufferedReader(s0);

				int index = 0;
				while (true) {
				  String line = s1.readLine();
				  if (line == null)
					break;

				  //deal with the line
				  lines[index] = line;
				  index++;
				}
				s1.close();


				PrintWriter pout = new PrintWriter(new FileWriter(cfile));

				int i=0;
				boolean silent = false;
				while (i < lc) {
				  if (lines[i].equalsIgnoreCase(chan + " {"))
					silent = true;

				  if (!silent) {
					pout.println(lines[i]);
				  }

				  //putting this check *after* the pout makes sure we don't write an extra "}"
				  if (lines[i].equals("}"))
					silent = false;


				  i++;
				}


				pout.close();




		 	   } catch(IOException e) {
				dprintln("Eeek:: " + e);
		 	   }
	  	} catch (FileNotFoundException e) {
			dprintln("Error: " + e);
		}




	   }
	}



 //allow chanserv to be started
 public void startCS() {
	thisThread.start();
 }

 public void stopCS() {
	//thisThread.stop();
	killed = true;
	disconnectService();
 }



}