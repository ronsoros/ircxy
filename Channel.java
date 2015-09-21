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
 
 

/*

	Class defining a channel on the irc server

	- Must have a way of identifying the channel, id+ channel name ?
	1. Must maintain a list of all users on the channel + their status.
	2. Must have methods for adding users/removing users from channel
	3. Must hold channel settings, modes, topic, etc.

*/
import java.util.*;


class Channel {

	public boolean clone = false; //channel is cloned from another server
	Vector xUserList = new Vector();	//list of users on channel (but on another server).

	String name = "";
	String topic = "";
	int usercount = 0;
	String whotopic = "";
	String topictime = "";

	//props
	String strCreation = "";
	String strOwnerkey = "";
	String strHostkey = "";
	String strOnjoin = "";
	String strOnpart = "";




	String modes = "nt";
	int limit = 0;

	String invites = " ";


	String modeString() {
		if (limit >0)
			return "+" + modes + "l " + limit;
		else
			return "+" + modes;
	}

	//for listx
	String modeStringLX() {
		if (limit > 0)
			return "+" + modes + "l";
		else
			return "+" + modes;
	}


	ChannelAccessList cal = new ChannelAccessList();

	//------- CHANNEL ACCESS LIST CODE ---------
	
	Vector getAccessList() { return cal.getAccessList(); }
	int    numAccesses()   { return cal.numAccesses(); }
	
	String getAccessAt( int id ) {
		return cal.getAccessAt(id);
	}
	
	int addAccess( String type, String mask, int expire, boolean ownerset, String reason ) {
		return cal.addAccess(type, mask, expire, ownerset, reason);
	}
	
	boolean hasAccess( String type, String mask, boolean match ) {
		return cal.hasAccess( type, mask, match );
	}
	
	
	
	
	int removeAccess( String type, String mask, boolean owner ) {
		return cal.removeAccess( type, mask, owner );
	}
	
	void clearAccess( boolean owner, String type ) {
		cal.clearAccess( owner, type );
	}
	
	// end of channel access list code




	String creation() { return strCreation; }

	void setCreation(long crtime) { strCreation = "" + crtime + ""; }
	
	String ownerkey() { return strOwnerkey;	}
	void setOwnerkey(String strkey) { strOwnerkey = strkey;	}

	String hostkey() { return strHostkey; }
	void setHostkey(String strkey) { strHostkey = strkey; }

	String onjoin() { return strOnjoin; }
	void setOnjoin(String oj) { strOnjoin = oj; }

	String onpart() { return strOnpart; }
	void setOnpart(String op) { strOnpart = op; }





	String whoSetTopic() { return whotopic; }
	String topicTimeStamp() { return topictime; }
	void setwhoSetTopic(String strStr) { whotopic = strStr; }
	void settopicTimeStamp(long tm) { topictime = "" + tm + ""; }
	void settopicTimeStamp(String tm) { topictime = tm; }



	boolean hasInvite( String strNick ) {
	  if (invites.indexOf(" " + strNick + " ") != -1)
		return true;
	  else
		return false;
	}
	void setInvite( String strNick ) { invites = invites + strNick + " "; }
	void unsetInvite( String strNick ) {
	  if (hasInvite( strNick ))
		invites = invites.substring(0, invites.indexOf(" " + strNick + " ")) + invites.substring(invites.indexOf(" " + strNick + " ") + strNick.length() + 1, invites.length());
	}




	boolean ismode(String modeChar) {
		return (modes.indexOf(modeChar) != -1);
	}
	
	void setmode(String modeChar)   { modes = modes + modeChar;   }
	void unsetmode(String modeChar) {
	  if (modes.indexOf(modeChar) != -1) {
	    modes = modes.substring(0, modes.indexOf(modeChar)) + modes.substring(modes.indexOf(modeChar)+1, modes.length());
	    if (modeChar == "i")
		invites = " "; //invites should not carry [  pool terminology ;)  ]
	  }
	}


	void setlimit(int lim) { limit = lim;  }
	int  getLimit()        { return limit; }



	void setName(String strName) { name = strName; }
	String getName()             { return name;    }


	void setTopic(String strTopic) { topic = strTopic; }
	String getTopic()              { return topic;     }



	void cleanChannel() {
		name = "";
		clone = false;
		xUserList = new Vector();
		membs = new Vector();
		//access = new Vector();
		cal = new ChannelAccessList();
		topic = "";
		usercount = 0;
		init_members();
		modes = "nt";
		whotopic = "";
		topictime = "";
		strCreation = "";
		strOwnerkey = "";
		strHostkey = "";
		strOnjoin = "";
		strOnpart = "";
	}



	private Vector membs = new Vector();

	public Vector getMembers() { return membs; }

				//[max_channel members][2]  <- 2D array holding members and their chan status
				//[*][1]=0 <- regular
				//[*][1]=1 <- +v
				//[*][1]=2 <- +o
				//[*][1]=3 <- +q


	void init_members() {
		membs = new Vector();
	}


	int getMemberID( int arrIndex )                  {
		String id = (String)membs.elementAt(arrIndex);
		return Integer.parseInt( id.substring(0,id.indexOf(" ")) );
	}



	int getMemberStatus( int arrIndex )              {
		String s = (String)membs.elementAt(arrIndex);
		return Integer.parseInt(s.substring(s.indexOf(" ")+1));
	}
	int getMemberStatusByID( int userID ) {
		int id = userArrayPos( userID );
		String s;
		if (id != -1) {
			s = (String)membs.elementAt(id);
			return Integer.parseInt(s.substring(s.indexOf(" ")+1));
		} else {
			return -1;
		}
	}
	

	void setMemberStatusByID( int userID, int status) {
		int id = userArrayPos( userID );
		if (id != -1) {
			membs.setElementAt(userID + " " + status, id);
		}		
	}
	
	void setMemberStatus( int arrIndex, int status ) {
		int id = getMemberID(arrIndex);
		membs.setElementAt(id + " " + status, arrIndex);
	}



	String getMemberStatusStr( int arrIndex ) {
		return getMemberStatusStr( arrIndex, true );
	}
	String getMemberStatusStr( int arrIndex, boolean ircx ) {
		int tmpStat = getMemberStatus(arrIndex);
		if (tmpStat == 0)
			return "";
		else if (tmpStat == 1)
			return "+";
		else if (tmpStat == 2)
			return "@";
		else if ((tmpStat == 3) && ircx)
			return ".";
		else if ((tmpStat == 3) && !ircx)
			return "@";
		else
			return "";
	}



	int membercount() { return membs.size(); }





	int userArrayPos(int userID) {
		int index = -1;
		String tmp = "";

		for (int i=0; i<membs.size(); i++) {
			tmp = (String)membs.elementAt(i);
			if (tmp.startsWith(userID + " ")) {
				index = i;
				i = membs.size();
			}
		}

		return index;
	}



	String user_status(int userID) {
		int ap = userArrayPos( userID );
		if (ap != -1) {
			return getMemberStatusStr( ap );
		} else {
			return "";
		}
	}




	void addXuser( String nick ) {
		xUserList.addElement( nick );
	}
	void removeXuser( String nick ) {
		xUserList.removeElement( nick );
	}
	int xUserCount() {
		return xUserList.size();
	}
	String getXuser( int pos ) {
		return (String)xUserList.elementAt(pos);
	}
	String nextXuser() {
		String tmpU = (String)xUserList.elementAt(0);
		xUserList.removeElementAt(0);
		return tmpU;
	}



	int add_user(int userID, int status) {
		membs.add( userID + " " + status );
		return membs.size()-1;
	} //end add_user



	boolean remove_user(int userID) {
		//find the user in the array and 'remove' them
		//return true if they were successfully removed
		int ap = userArrayPos( userID );
		if (ap != -1) {
			membs.removeElementAt( ap );
			return true;
		} else {
			return false;
		}
	} //end remove_user


	void remove_userAtPos(int arrayPos) {
		membs.removeElementAt( arrayPos );
	}
	


}