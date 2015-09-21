import java.util.Vector;

class ial {
	
	Vector list = null; //currently active who listsing
	Vector deadList = null; //for implementation of WHOWAS
	public int max_dead_size = 0;
	
	public ial() {
		list = new Vector();
		deadList = new Vector();
	}
	public ial( int mds ) {
		list = new Vector();
		deadList = new Vector();
	}

	public void addIAL( String n, String id, String i, String s, String r, String m, String h ) {
		removeIAL( n );
		Who nu = new Who();
		nu.setWho( n, id, i, s, r, m, h );
		list.add( nu );
	}
	public void addIAL( Who w ) {
		removeIAL( w.nick );
		list.add( w );
	}
	
	
	public boolean removeIAL( String n ) {
		return removeIAL( n, true );
	}
	public boolean removeIAL( String n, boolean rem ) {
		boolean s = false;
		Who tmpWho = null;
		int i=0;
		
		while (!s && (i<list.size())) {
		//for (int i=0; i<list.size(); i++) {
			tmpWho = (Who)list.elementAt(i);
			if (tmpWho.nick.equalsIgnoreCase(n)) {
				if (rem) {
					if (max_dead_size > 0) {
						if (deadList.size() >= max_dead_size) {
							deadList.removeElementAt(0); //first in, first out
						}
						tmpWho.setSignOff();
						deadList.add( tmpWho );
					}
					try { list.removeElementAt(i); } catch(Exception e) {}
				}
				i = list.size();
				s = true;
			}
			i++;
		}
		return s;		
	}
	
	
	
	public boolean existsIAL( String n ) {
		return removeIAL( n, false );
	}

	public int size() {
		return list.size();
	}
	
	
	public void nickChange( String on, String nn ) {
		Who tmpWho = null;
		for (int i=0; i<list.size(); i++) {
			tmpWho = (Who)list.elementAt(i);
			if (tmpWho.nick.equalsIgnoreCase(on)) {
				tmpWho.nick = nn;
				list.setElementAt( tmpWho, i );
			}
		}
	}
	
	
	public Who getIALWho( String n ) {
		Who tmpWho = null;
		for (int i=0; i<list.size(); i++) {
			tmpWho = (Who)list.elementAt(i);
			if (tmpWho.nick.equalsIgnoreCase(n)) {
				i = list.size();
			}
		}
		return tmpWho;
	}
	public Who getIALWhoAt( int i ) {
		if (i < list.size()) {
			return (Who)list.elementAt(i);
		} else {
			return null;
		}
	}
	
	public String getIAL( String n ) {
		String s = "";
		Who tmpWho = null;
		for (int i=0; i<list.size(); i++) {
			tmpWho = (Who)list.elementAt(i);
			if (tmpWho.nick.equalsIgnoreCase(n)) {
				i = list.size();
				s = tmpWho.getWho();
			}
		}
		return s;
	}
	
	public String getIALat( int i ) {
		if (i < list.size()) {
			Who tmpWho = (Who)list.elementAt(i);
			return tmpWho.getWho();
		} else {
			return "";
		}
	}
	
	public String getNickAt( int i ) {
		if (i < list.size()) {
			Who tmpWho = (Who)list.elementAt(i);
			return tmpWho.nick;
		} else {
			return "";
		}
	}

	public Vector getMatches( String mask, int type ) {
		return getMatches( list, mask, type, false );
	}
	public Vector getWasMatches( String mask, int type ) {
		if (max_dead_size > 0) {
			return getMatches( deadList, mask, type, true );
		} else {
			return (new Vector());
		}
	}

	public Vector getMatches( Vector lst, String mask, int type, boolean was ) {
		//find all the nick matches to this query
		Vector tmp = new Vector();
		Who tmpWho = null;
		for (int i=0; i<lst.size(); i++) {
			tmpWho = (Who)lst.elementAt(i);

			String matchPart = tmpWho.nick; //default
			
			if      (type == 0) { matchPart = tmpWho.nick; }	//NICK match
			else if (type == 1) { matchPart = tmpWho.ip; }	  //IP match
			else if (type == 3) { matchPart = tmpWho.nick; }	//Either nick or ip match
			
			if (matches(mask, matchPart) || ((type == 3) && matches(mask, tmpWho.ip))) {
				if (was) {
					tmp.add( tmpWho );
				} else {
					tmp.add( tmpWho.getWho() );
				}
			}
		}
		return tmp;		
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

}