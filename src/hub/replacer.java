/* 
	Compression by common word replacement.
	
	Important: Both the hub and the server must be using the SAME repwords array!
	
	replacer.java was written by David Forrest, in Jan/2003 and is part of the TES project.
*/

public class replacer {

	String[] repwords = {
		"american", "anyone", "answer", "and ", "are ",
		"channel",
		"david",
		"english",
		"have", "haha", "here",
		"kicked",
		"lol", "list",
		"my ",
		"name", "nick",
		"quit",
		"packet", "people", "program",
		"rofl", "ROFL",
		"something", "stop", "still", "script", "send", "screenshot", "silence", "scottish",
		"to ", "that", "there", "The", "the", "this", "topic", "test",
		"was ", "what", "when", "where",
		"you're", "your", "you"
	
	 };

	public replacer() {}

	public String encode( String str ) {
		String after = "";
		while (str.length() > 0) {
			if (str.indexOf("/") != -1) {
				after+= str.substring(0, str.indexOf("/")+1) + "/";
				str = str.substring( str.indexOf("/")+1 );
			} else {
				after += str;
				str = "";
			}
		}
		String code = "";
		for (int acounter=0; acounter<repwords.length; acounter++) {
			while (after.indexOf( repwords[acounter] ) != -1) {
				code+= after.substring(0, after.indexOf( repwords[acounter] )) + "/" + acounter;
				after = after.substring( after.indexOf( repwords[acounter] ) + repwords[acounter].length() );
			}
			after = code + after;
			code = "";
		}
		return after;
	}



	public String decode( String str ) {
		String code = "";
		int ssi = 0;
		for (int acounter=repwords.length-1; acounter>=0; acounter--) {
			while ((ssi = str.indexOf( "/" + acounter )) != -1) {
				if ((ssi == 0) || (str.indexOf( "//" + acounter ) != ssi-1)) {
					code+= str.substring(0, ssi) + repwords[acounter];
				} else {
					code+= str.substring(0, ssi + 1 + Integer.toString(acounter).length() );
				}
				str = str.substring( ssi + 1 + Integer.toString(acounter).length() );
			}
			str = code + str;
			code = "";
		}
		String after = "";
		while (str.length() > 0) {
			if (str.indexOf("//") != -1) {
				after+= str.substring(0, str.indexOf("//")+1);
				str = str.substring( str.indexOf("//")+2 );
			} else {
				after += str;
				str = "";
			}
		}
		return after;
	}




}