public class numerics {
	String serverName;
	

	public String r256( String n, String a )				{ return ":" + serverName + " 256 " + n + " " + serverName + " :" + a; }

	public String r305( String n )						 { return ":" + serverName + " 305 " + n + " :You are no longer marked as being away"; }
	public String r306( String n )						 { return ":" + serverName + " 306 " + n + " :You have been marked as being away"; }

	public String r332( String n, String c, String t )	 { return ":" + serverName + " 332 " + n + " " + c + " :" + t; }
	public String r333( String n, String c, String t, String ts) { return ":" + serverName + " 333 " + n + " " + c + " " + t + " " + ts; }


	public String r403( String n, String c)				{ return ":" + serverName + " 403 " + n + " " + c + " :No such channel"; }
	public String r405( String n, String c)				{ return ":" + serverName + " 405 " + n + " " + c + " :You have joined too many channels"; }

	public String r432( String on, String nn ) 			{ return ":" + serverName + " 432 " + on + " " + nn + " :Erroneus Nickname"; }
	public String r433( String on, String nn ) 			{ return ":" + serverName + " 433 " + on + " " + nn + " :Nickname is already in use"; }
	public String r438( String m, String n, long d )		{ return ":" + serverName + " 438 " + m + " " + n + " :Nick change too fast. Please wait " + d + " seconds."; }


	public String r442( String n, String c)				{ return ":" + serverName + " 442 " + n + " " + c + " :You're not on that channel"; }

	public String r462( String n )						{ return ":" + serverName + " 462 " + n + " :You may not register"; }

	public String r471( String n, String c)				{ return ":" + serverName + " 471 " + n + " " + c + " :Cannot join channel (+l)"; }
	public String r473( String n, String c)				{ return ":" + serverName + " 473 " + n + " " + c + " :Cannot join channel (+i)"; }
	public String r474( String n, String c)				{ return ":" + serverName + " 474 " + n + " " + c + " :Cannot join channel (+b)"; }

	public String r913( String n )						 { return ":" + serverName + " 913 " + n + " :Permission Denied- Only IRC Opers may join this channel."; }








	public numerics( String sn ) {
		serverName = sn;
	}
 //end class raws
}