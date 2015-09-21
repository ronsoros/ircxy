import java.util.Vector;

class ChannelAccessList {

	Vector access;
	int accessLimit = 30;
	String name;
	
	public ChannelAccessList() {
		access = new Vector();
		name = "";
	}
	
	
	
	//------- CHANNEL ACCESS LIST CODE ---------
	
	Vector getAccessList() { return access; }
	int    numAccesses()   { return access.size(); }
	
	String getAccessAt( int id ) {
		if ((id >= access.size()) || (id < 0)) {
			return "";
		} else {
			//stored: type mask expire ownerset :reason
			//return: TYPE mask expire * :reason
			String tmpAccess = (String)access.elementAt( id );
			String tmpType = tmpAccess.substring(0, tmpAccess.indexOf(" ")).toUpperCase();
							 tmpAccess = tmpAccess.substring( tmpAccess.indexOf(" ")+1 );
			String tmpMask = tmpAccess.substring(0, tmpAccess.indexOf(" ")) + "$*";
							 tmpAccess = tmpAccess.substring( tmpAccess.indexOf(" ")+1 );
			String tmpExp =  tmpAccess.substring(0, tmpAccess.indexOf(" "));
							 tmpAccess = tmpAccess.substring( tmpAccess.indexOf(" ")+1 );
							 tmpAccess = tmpAccess.substring( tmpAccess.indexOf(" ")+1 ); //bypass the ownerset variable
							 
			return tmpType + " " + tmpMask + " " + tmpExp + " * " + tmpAccess;
		}
	}
	
	int addAccess( String type, String mask, int expire, boolean ownerset, String reason ) {
		//check that the vector doesn't already contain it.
		//return code: 0=ok, 1=duplicate_entry, 2=full
		
		String tmpAccess = "";
		boolean duplicate = false;
		
		if (access.size() >= accessLimit) {
			//full
			return 2;
		} else {
			for (int i=0; i<access.size(); i++) {
				tmpAccess = (String)access.elementAt( i );
				tmpAccess = tmpAccess.toLowerCase();
				if (tmpAccess.startsWith( type.toLowerCase() + " " + mask.toLowerCase() + " " )) {
					duplicate = true;
					i = access.size();
				}
			}
		}

		if (duplicate) {
			return 1;
		} else {
			String os = "0";
			if (ownerset) { os = "1"; }
			access.add( type + " " + mask + " " + expire + " " + os + " :" + reason );
			return 0;			
		}

		
		
	}
	
	boolean hasAccess( String type, String mask, boolean match ) {
		//check if the mask already exists in a particular access..
		//return code: true=yes, false=no
		
		String tmpAccess = "";
		boolean exists = false;
		
		for (int i=0; i<access.size(); i++) {
			tmpAccess = (String)access.elementAt( i );
			tmpAccess = tmpAccess.toLowerCase();
			if (tmpAccess.startsWith( type.toLowerCase() + " " )) {
				tmpAccess = tmpAccess.substring( tmpAccess.indexOf(" ")+1 );
				tmpAccess = tmpAccess.substring(0, tmpAccess.indexOf(" "));				
				
				if (mask.equalsIgnoreCase(tmpAccess) || (match && matches(tmpAccess, mask))) {
					exists = true;
					i = access.size();
				}
			}
		}
		return exists;
	}
	
	
	
	
	int removeAccess( String type, String mask, boolean owner ) {
		//check if the entry exists
		//return code: 0=ok, 1=not_allowed, 2=no_such_entry
		
		
		boolean exists = false;
		boolean allowed = false;
		
		String tmpAccess = "";
		String tmpType = "";
		String tmpMask = "";
		String tmpExpire = "";
		String tmpOS = "";
		
		for (int i=0; i<access.size(); i++) {
			tmpAccess = (String)access.elementAt( i );
			tmpAccess = tmpAccess.toLowerCase();
			
			tmpType = tmpAccess.substring(0, tmpAccess.indexOf(" "));
						tmpAccess = tmpAccess.substring( tmpAccess.indexOf(" ")+1 );
			tmpMask = tmpAccess.substring(0, tmpAccess.indexOf(" "));
						tmpAccess = tmpAccess.substring( tmpAccess.indexOf(" ")+1 );
			
			if (mask.length() == 0) { tmpMask = ""; }
			if (type.length() == 0) { tmpType = ""; }
				
			if (tmpType.equalsIgnoreCase(type) && tmpMask.equalsIgnoreCase(mask)) {
				exists = true;
				
				tmpExpire = tmpAccess.substring(0, tmpAccess.indexOf(" "));
							tmpAccess = tmpAccess.substring( tmpAccess.indexOf(" ")+1 );
				tmpOS = tmpAccess.substring(0, tmpAccess.indexOf(" "));
							tmpAccess = tmpAccess.substring( tmpAccess.indexOf(" ")+1 );

				if (tmpOS.equals("0") || (owner)) {
					allowed = true;
					
					access.removeElementAt( i );
					if (mask.length() == 0) { i--; }
					else { i = access.size(); }
				}
							
			}
			
		}
		
		if (!exists) { return 2; }
		else if (!allowed) { return 1; }
		else { return 0; }
			
		
		
		
	}
	
	void clearAccess( boolean owner, String type ) {
		if (owner && type.equals("")) { access.clear(); }
		else {
			removeAccess( type, "", owner );
		}
	}
	
	// end of channel access list code


	private boolean matches( String tmpMask, String tmpNick ) {
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

}