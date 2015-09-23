 __ __|   ____|    ___|  README.TXT
    |     __|    \___ \  Last updated 07/12/2009 for TES version 1.317
    |     |            | 
   _|    _____|  _____/  
TESx : An Open Source Platform Independant IRCX Server
Written by David Forrest (david@squarish.net)



This file is intended to give a basic outline of how server-hub linking works in TESx.
It also outlines how to set up linking, to get you up and running to create your network.



Firstly, a note on how linking has been implemented. Unlike some irc networks, where
the hub also acts as a server (allowing user connections).
TES takes a different approach, as detailed:

* The hub connects to the servers, not vice-versa.

* The TES hub acts _only_ as a hub. This has the advantage of increased performance (in terms
of speed), reduced lag, increased hub security, and incread stability.

* All servers connected to a network will be connected directly to the hub, similar to the
following:
									HUB
					_________________|__________________...........................
					|			|					  |			:			:
				Server1		Server2				Server3					ServerX





1. Configuring the hub & servers for linking.

	_:: O-lines ::_
	
	Each server must have the following o-line in it's ircx.conf file:
					o-line		H:hub:<hub-password>
			e.g.	o-line		H:hub:s0mePaSSw0rD
			
			Note: The word "hub" (above) should be used no matter what your hub is called.
			The only part you change is the password.
			
			
			
	The hub must have an entry in hub.conf for each server:
					server	<server-ip>:<port>:<hub-password>
					
					<server-ip>		The IP address of a server to link to.
					<port>		 	The port that server accepts connections on.
					<hub-password>	 The password used to link. (see above).
					
			e.g.	server	irc2.splog.net:6667:s0mePaSSw0rD
			
			
	
2. The hub and servers are now ready to link.

	Start the servers:
		Refer to the TES server's README.TXT file for details on how to start the server.
		
	...and the hub:
		Type:	java hub
		Note: The optional argument -h displays the help screen.

	
	* When you first start up your network, try to start things in the following order:
	*	Start up each server.
	*	Start up the hub.
	* This is not a requirement, but just makes things run that little bit more smootly.
	
	
	
3. You should now have a network, Enjoy!



