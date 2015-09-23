class WhoWas extends Who {

	private String signOff = "";
	
	public String getSignOff() {
		return signOff;
	}
	public void setSignOff( String s ) {
		signOff = s;
	}

	public String get314() {
		return nick + " " + ident + " " + ip + " * :" + realname;
	}
	public String get312() {
		return nick + " " + server + " :" + signOff;
	}

}