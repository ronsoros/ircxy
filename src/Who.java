import java.util.Date;

class Who {

	//e.g. ~djf 62.31.114.78 ntsecurity.nu bob H@ :0 test
	
	public String nick = "";
	public String ident = "";
	public String ip = "";
	public String server = "";
	public String realname = "";
	public String modes = "";
	public String hops = "";
	public String signOff = "";

	public void setWho( String n, String id, String i, String s, String r, String m, String h ) {
		nick = n;
		ident = id;
		ip = i;
		server = s;
		realname = r;
		modes = m;
		hops = h;
	}
	public String getWho() {
		return ident + " " + ip + " " + server + " " + nick + " " + modes + " " + hops + " " + realname;
	}

	public void setSignOff() {
		Date dt = new Date();
		signOff = dt.toString();		
	}


	public String get314() {
		return nick + " " + ident + " " + ip + " * :" + realname;
	}
	public String get312() {
		return nick + " " + server + " :" + signOff;
	}



}