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
 

import java.net.Socket;
import java.util.Vector;

class User {

 String nick = "";
 String ident = "";
 String hostname = "";
 String realname = "";
 boolean registered = false;
 public boolean ircx = false;
 public boolean xml = false;
 String sntPass = "";
 
 String awayMsg = "";
 boolean away = false;

 int sysop = 0;

 String signOnTime = "";
 int numChansJoined = 0;  //dont let a user join more than a certain number of chans.

 long lastNickChangeTime = (long)0;
 long lastMsg = (long)0;
 long idleSince = System.currentTimeMillis();

 int floodCount = 0; //the number of msgs that have been sent too quickly

 String modes = ""; //user modes

 Vector silences = new Vector();

 Vector songs = new Vector();
 int lineSpeed = 0;

 int ID = -1;		//-1 = invalid ID (indicating free /usable array index)
			// * = socket index

 Socket userSocket = null;


	public static void main(String[] args) {
	}


	void cleanUser() {
		ID = -1;
		nick = "";
		ident = "";
		hostname = "";
		realname = "";
		registered = false;
		away = false;
		ircx = false;
		xml = false;
		awayMsg = "";
		sysop = 0;
		numChansJoined = 0;
		signOnTime = "";
		lastNickChangeTime = (long)0;
		lastMsg = (long)0;
		idleSince = System.currentTimeMillis();
		floodCount = 0;
		modes = "";
		silences = new Vector();
		songs = new Vector();
		lineSpeed = 0;
		userSocket = null;
	}

	void register() {
		signOnTime = "" + (long)(System.currentTimeMillis() / 1000) + "";
		setLastMsg(System.currentTimeMillis());
		registered = true;
	}
	boolean isRegistered() { return registered; }
	
	String sentPass() { return sntPass; }
	void setPass(String pss) { sntPass = pss; }

	String getSignOnTime() { return signOnTime; }

	int getNumChansJoined()  { return numChansJoined; }
	void incNumChansJoined() { numChansJoined++;      }
	void decNumChansJoined() { numChansJoined--;      }




	boolean isAway() { return away;	}
	String awayMessage() { return awayMsg; }

	void setAway(String am) {
		away = true;
		awayMsg = am;
	}
	void setBack() {
		away = false;
		awayMsg = "";
	}







	String getModes()             { return modes; }
	void addMode(String modeChar) {	modes = modes + modeChar; }
	void addMode(char modeChar)   {	modes = modes + modeChar; }

	boolean remMode(String modeChar) {
	  if (modes.indexOf(modeChar) != -1) {
	    //find the mode and remove it
	    modes = modes.substring(0, modes.indexOf(modeChar)) + modes.substring(modes.indexOf(modeChar)+1, modes.length());
		return true;
	  } else {
		return false;
	  }
	}
	








	String addSilence( String mask ) {
		if ((mask.indexOf("!") == -1) && (mask.indexOf("@") == -1)) {
			
			if (mask.indexOf(".") == -1) {	mask = mask + "!*@*"; /*assume nickname*/ }
			else {							mask = "*!*@" + mask; /*assume hostname*/ }
		}
		else if ((mask.indexOf("!") == -1) && (mask.indexOf("@") != -1)) {	mask = "*!" + mask; }
		else if ((mask.indexOf("!") != -1) && (mask.indexOf("@") == -1)) {	mask = mask + "@*"; }

		
		if (hasSilence( mask ) == -1) {
			silences.add( mask );
		}
		return mask;
	}
	String remSilence( String mask ) {
		String remd = "";
		int pos = hasSilence(mask);
		if (pos != -1) {
			remd = (String)silences.elementAt(pos);
			silences.removeElementAt( pos );
		}
		return remd;
	}
	int hasSilence( String mask ) {
		int pos = -1;
		for (int i=0; i<silences.size(); i++) {
			if (mask.equalsIgnoreCase( (String)silences.elementAt(i) )) {
				pos = i;
				i = silences.size();
			}
		}
		return pos;
	}
	boolean hasSilenceMatch( String mask ) {
		boolean match = false;
		for (int i=0; i<silences.size(); i++) {
			if (matches((String)silences.elementAt(i), mask)) {
				match = true;
				i = silences.size();
			}
		}
		return match;
	}
	Vector getSilences() {
		return silences;
	}





	void incFloodCount()   { floodCount++; }
	int floodCount()       { return floodCount; }
	void resetFloodCount() { floodCount = 0; }



	void setLastMsg(long tmp) { lastMsg = tmp;  }
	long getLastMsg()         { return lastMsg; }

	void setIdle(long tmp)	{ idleSince = tmp;  }
	long getIdle()			{ return idleSince; }


	void makeSysop(int level)  { sysop = level;  }
	void unSysop()    { sysop = 0; }
	int isSysop() { return sysop; }


	long lastNickChangeTime()            { return lastNickChangeTime; }
	void setLastNickChangeTime(long tmp) { lastNickChangeTime = tmp;  }


	void setNick( String nickname ) { nick = nickname; }
	String getNick()                { return nick; }

	void setIdent( String strIdent ) { ident = strIdent; }
	String getIdent()                { return ident;     }

	void setHostname( String strHost ) { hostname = strHost; }
	String getHostname()               { return hostname;    }

	void setRealname( String strRealname ) { realname = strRealname; }
	String getRealName()                   { return realname;        }


	void setID(int idvalue) { ID = idvalue; }
	int ID()                { return ID;    }

	void setSocket(Socket sck) {
		 userSocket = sck;
		 //userSocket.setSoLinger(true, 0);
	}
	Socket getSocket()         { return userSocket; }









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




 //end of class User
}