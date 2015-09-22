import java.io.*;
import java.util.*;
import java.net.*;

class vChecker {

	String filePath = "http://www.tesx.org/downloads/";
	String subDir = "";
	
	Vector fileList = new Vector(); //list of all files to be downloaded
	
	int STABLE_LIST = 1;
	int DEV_LIST = 2;
	
	int NO_DOWNLOAD = 0;
	int BUNDLE = 1;
	int UPGRADE = 2;
	
	Vector lv;


	public static void main( String args[] ) {
		new vChecker( "0", false );
	}


	public vChecker( String version, boolean dev ) {
		String d = "";
		if (dev) { d = " (dev release)"; }
		System.out.println( "You are currently using TES version: " + version + d );


		System.out.println( "Checking www.splog.net for a later release.." );
		
		DownloadURL du = new DownloadURL( "http://www.splog.net/?page=versioncheck", 10 );
		lv = du.getVector();
		

		String r = "";
		for (int i = 0; i < version.length(); i ++) {
			if (version.charAt(i) != '.') r += version.charAt(i);
		}
		

    	String cv = r;
		String lvS = (String)lv.elementAt(0);
			   lvS = lvS.substring(0, lvS.indexOf(" "));
		String lvD = (String)lv.elementAt(1);
			   lvD = lvD.substring(0, lvD.indexOf(" "));
		
		int cvi = Integer.parseInt( cv );
		int lvSi = Integer.parseInt( lvS );
		int lvDi = Integer.parseInt( lvD );		
		
		//if we're currently using a stable version, check if there is a more up to date stable (or dev) version.
		boolean downloadStable = false;
		boolean downloadDev = false;
		
		System.out.println("");
		//System.out.println("Updates available:");
				
			if (lvSi > cvi) {
				String released = (String)lv.elementAt(0);
					   released = released.substring(released.indexOf(" ")+1);
					   released = released.substring(released.indexOf(" ")+1);
				System.out.println("\t --> Stable Version: \t" + lvS + "\t(" + released + ")");
				downloadStable = true;
				if ((lvDi > cvi) && (lvDi > lvSi)) {
					   released = (String)lv.elementAt(1);
					   released = released.substring(released.indexOf(" ")+1);
					   released = released.substring(released.indexOf(" ")+1);
					System.out.println("\t --> Dev. version: \t" + lvD + "\t(" + released + ")");
					downloadDev = true;
				}
			} else if (lvDi > cvi) {
				//if (!dev) { System.out.println("You have the latest stable version, but.."); }
				System.out.println("\t --> Dev. version: \t" + lvD);
				downloadDev = true;
				
			} else if (cvi > lvDi) {
				System.out.println("The version you are running is newer than the latest development release.");
				System.out.println("You should only update to correct a corrupted version.");
			} else {
				System.out.println("You have the latest version");
				System.out.println("You should only update to correct a corrupted version.");
			}
			System.out.println("\r\n");

			int didStableDownload = NO_DOWNLOAD;
			int didDevDownload = NO_DOWNLOAD;
				
				
				System.out.print("Download latest stable version now? (y/n) ");				

				if (getResponse().startsWith("y")) {

					System.out.println();
					
	
					optionPrompt();
					
					String response = getResponse();
					if (response.equals("1")) {
						subDir = "";
						download("tes" + lvS + ".tar.gz");
						didStableDownload = BUNDLE;
					} else if (response.equals("2")) {
						downloadStable();
						didStableDownload = UPGRADE;
					}
				}
				


				System.out.print("\r\nDownload latest development version now? (y/n) ");				

				if (getResponse().startsWith("y")) {

					System.out.println();
					
	
					optionPrompt();
					
					String response = getResponse();
					if (response.equals("1")) {
						subDir = "";
						download("tes" + lvD + "-dev.tar.gz");
						didDevDownload = BUNDLE;
					} else if (response.equals("2")) {
						downloadDev();
						didDevDownload = UPGRADE;
					}
				}

				System.out.println();






			if (didStableDownload == BUNDLE) {
				System.out.println( "The latest stable bundle has been downloaded to the current directory." );
			} else if (didStableDownload == UPGRADE) {
				System.out.println("The latest stable upgrade has been installed.");
				System.out.println("Note: The latest versions of ircx.conf and hub.conf have been saved as new_ircx.conf, and new_hub.conf");
			}
			
			System.out.println();
							
			if (didDevDownload == BUNDLE) {
				System.out.println( "The latest dev. bundle has been downloaded to the current directory." );
			} else if (didDevDownload == UPGRADE) {
				System.out.println("The latest dev. upgrade has been installed.");
				System.out.println("Note: The latest versions of ircx.conf and hub.conf have been saved as new_ircx.conf, and new_hub.conf");
			}
		
			System.out.println("\r\n");		
				
	}
	
	
	
	
	
private String getResponse() {
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

private void optionPrompt() {
	System.out.println("Please choose an option:");
	System.out.println("1\t- Download the bundle (as a .tar.gz file)");
	System.out.println("2\t- Upgrade my existing version (old files will be overwritten)");
	System.out.println("3\t- Cancel. (Don't download! I changed my mind)");
	System.out.print("1, 2 or 3? :");
}
	
	private void buildFileList(int listType) {
		
		String subSearch;
		if (listType == STABLE_LIST) {
			subDir = "classfiles/stable/";
			subSearch = "/stable/";
		} else {
			subDir = "classfiles/dev/";
			subSearch = "/dev/";
		}
		
		System.out.print("Retreiving File List...");
		DownloadURL du = new DownloadURL( filePath + subDir + "fileList.txt" , 10 );
		Vector l = du.getVector();
		fileList = new Vector();
		
		String nextFile = "";
		int numFiles = 0;

		for (int i=0; i<l.size(); i++) {
			nextFile = (String)l.elementAt(i);
			if (nextFile.length() > 0) {
				nextFile = nextFile.substring( nextFile.indexOf(subSearch) + subSearch.length() );
				if (!nextFile.equals("fileList.txt") && !nextFile.equals("hub") && (nextFile.length()>0) ) {			
					fileList.addElement( nextFile );
				}
				numFiles++;
			}
		}
		if (numFiles > 0) {
			System.out.println("Complete (" + numFiles + " files)\r\n");
		} else {
			System.out.println("File List was empty!");
		}



	}
	
	
	public void downloadStable() {
		buildFileList( STABLE_LIST );
		subDir = "classfiles/stable/";
		for (int i=0; i<fileList.size(); i++) {
			download( (String)fileList.elementAt(i) );
		}		
	}
	public void downloadDev() {
		buildFileList( DEV_LIST );
		subDir = "classfiles/dev/";
		for (int i=0; i<fileList.size(); i++) {
			download( (String)fileList.elementAt(i) );
		}		
	}
	
	
	public void download(String filename) {
			try {
				URL ud = new URL(filePath + subDir + filename);
				
				URLConnection uc = ud.openConnection();
				InputStream stream = uc.getInputStream();
				BufferedInputStream in = new BufferedInputStream(stream);
				FileOutputStream file = new FileOutputStream(filename);
				BufferedOutputStream pout = new BufferedOutputStream(file);
			
				
				
				//find out the file size
				String fs = (String)lv.elementAt(1);
					   fs = fs.substring(fs.indexOf(" ")+1);
					   fs = fs.substring(0,fs.indexOf(" "));
				int dfs = Integer.parseInt(fs);
				
				while (filename.length() < 25) { filename+= " "; }
				
				System.out.print("Downloading: " + filename + "\t\t:  "); // (" + (dfs/1000) + " KB)
				int percent = (int)(dfs/20);
				//System.out.println("Percent: " + percent);

				int tmp;
				int counter = 0;
				//System.out.println("\t\t0%\t\t100%");
				//System.out.print  ("Progress:\t");
				while ((tmp = in.read()) != -1) {
					counter++;
					pout.write( tmp );
					if (counter == percent) {
						counter = 0;
						//System.out.print("#");
					}
				}
				//System.out.println("\r\n");
				System.out.println("Complete");
				//pout.print( uc.getContent() );
				pout.flush();
				pout.close();
			
			
			} catch (Exception e) { System.out.println("Error: " + e); }
	}


}