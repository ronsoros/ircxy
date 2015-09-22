import java.io.*;


public class LZW {

	static String dict[];
	static int point;
	
	public LZW() {
	}

	public static String compress( String in ) {
		dictInit( in.length() );
		String out = "";
		String w = "";
		char k;
		while (in.length() > 0) {
			k = in.charAt(0);
			in = in.substring(1);
			if (dictHas(w + k)) { w+=k; }
			else {
				out+= dictCode(w);
				dictAdd(w + k);
				w = k + "";
			}
		}
		return out + dictCode(w);
	}

	public static String decompress( String in ) {
		dictInit( in.length()+1 );
		String out = "";
		String w = "";
		int k = (int)in.charAt(0);
		w = dictChar( k );
		in = in.substring(1);
		out += w;

		while (in.length() > 0) {
			k = (int)in.charAt(0);
			in = in.substring(1);
			out += dictChar(k);
			dictAdd(w + dictChar(k).charAt(0));
			w = dictChar(k);
		}
		return out;
	}
	private static void dictInit( int size ) {
		dict = new String[size];
		point = 0;
	}


	private static boolean dictHas( String s ) {
		return (dictCode(s).length() > 0);
	}
	private static String dictCode( String s ) {
		String code = "";
		char c;
		int i=0;
		if (s.length() == 1) { //it's a character, just return the ascii code
			return "" + s.charAt(0);
		} else {
			while ((code.length() == 0) && (i < dict.length)) {
				if ((dict[i] != null) && dict[i].equals(s)) {
					c = (char)(i+256);
					code = "" + c;
				}
				i++;
			}
			return code;
		}
	}
	private static String dictChar( int code ) {
		if (code < 256) { return "" + (char)code; }
		else if ((code-256) < dict.length) { return dict[code-256]; }
		else { return ""; }
	}
	private static void dictAdd( String s ) {
		dict[point] = s;
		point++;
	}
		

private static String getResponse() {
		InputStreamReader isr = new InputStreamReader ( System.in );
		BufferedReader br = new BufferedReader ( isr );
		String r = "n";
		try {
			r = br.readLine();
		} catch ( IOException ioe ) {
			// won't happen too often from the keyboard
		}
		return r;
}




	public static void main( String args[] ) {
		
		System.out.print("Input: ");
		String str1 = getResponse();


		long tt = System.currentTimeMillis();
		String c = compress( str1 );
		tt = System.currentTimeMillis() - tt;
		System.out.println("Compressed: (" + str1.length() + " -> " + c.length() + " chars) in " + tt + " ms.");
		if (args.length > 0) {
			System.out.println("---COMPRESSED DATA FOLLOWS\r\n" + c + "\r\nEND OF DATA---");
		}

		System.out.println("A saving of: " + (str1.length() - c.length()) + " chars.");

		tt = System.currentTimeMillis();
		String dec = decompress(c);
		tt = System.currentTimeMillis() - tt;
		System.out.println( "Decompressed (in " + tt + " ms.) to:\r\n" + dec );
		
		
	}



}
