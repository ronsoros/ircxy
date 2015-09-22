/**
 * Copyright (C) 2001-2002 David Forrest
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 **/

import java.io.*;
import java.util.Vector;


/*
	fixOldJava for:
	Tes - Java Threaded Echo Server.
	Author: David Forrest (david@splog.net)

	Ensures the backwards compatibility of a java file
	by removing sInpOut() - the shutdownInput() / Output() socket procedures
	which were only introduced in Java 1.3.

*/


class fixOldJava {

	static File fn = null;
	static Vector contents = new Vector();
	static boolean skipMode = false;
		

	

	public static void doFix() {
	
		if (fn.exists()) {
			
		  try {
			FileReader s0 = new FileReader( fn );
			BufferedReader s1 = new BufferedReader(s0);
	
			String tmpLine;
	
	
			try {
				System.out.println("File: " + fn);
				System.out.print("Looking for sInpOut() lines.. ");
				while (true) {
				  String line = s1.readLine();
				  if (line == null)
					break;
	
				  //deal with the line
				  //line = line.trim();
				
				  if (!skipMode) {
					contents.add( line );
					
					line = line.trim();
					if (line.equals( "public void sInpOut( Socket s ) {" )) {
						skipMode = true;
					}					
				  } else {
				    String tmp = line;
					line = line.trim();
					if (line.equals( "}//end sInpOut" )) {
						skipMode = false;
						contents.add( "\t" + line );
					} else {
					    contents.add( "\t//Line removed by fixOldJava.java - " + tmp );
					}
				  }
				  
				
				
				
				}
								
				s1.close();
				
				System.out.println( "..done" );
				
				
				
				
				//write the vector back out to file
				System.out.print("Saving the new file.. ");
				
				PrintWriter pout = new PrintWriter(new FileWriter( fn ));
		
				for (int i=0; i<contents.size(); i++) {
					pout.println( (String)contents.elementAt(i) );
				}
		
				pout.close();

				System.out.println( "..done" );
				
				System.out.println();
				System.out.println( "Process is complete, you MUST now recompile TES\r\n" );
				
				
				
				
				
				
				
				

			} catch(IOException e) {
				System.out.println("Error: " + e);
	    	}
		  } catch (FileNotFoundException e) {
			  System.out.println("Error: " + e);
		  }
			
			
			
		} else {
			System.out.println("Error: The file (" + fn + ") does not exist!");
		}
	
	}


	public fixOldJava() {}
	

	public static void main( String args[] ) {
		if (args.length <= 1) {
			if (args.length == 1) {
				fn = new File( args[0] );
			} else {
				fn = new File( "EchoServer.java" );
			}
			
			doFix();			
			
		} else {
			System.out.println();
			System.out.println("Removes socket sInpOut() procedure from java source files");
			System.out.println("to ensure compatibility with java < 1.3");
			System.out.println("\r\nUsage is: java fixOldJava <filename>");
			System.out.println("If <filename> is not supplied, it will default to: EchoServer.java");
		}
	}


}//end fixOldJava