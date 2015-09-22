class KLine {

	public String mask;
	public boolean global;
	public String setter;
	public String reason;
	
	public KLine() {
		mask = "";
		global = false;
		setter = "";
		reason = "";
	}
	public KLine( String m, boolean g, String s, String r ) {
		mask = m;
		global = g;
		setter = s;
		reason = r;
	}
}