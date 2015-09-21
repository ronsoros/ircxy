import java.net.*;
import java.io.*;

// This appears in Core Web Programming from
// Prentice Hall Publishers, and may be freely used
// or adapted. 1997 Marty Hall, hall@apl.jhu.edu.

/** A shorthand way to create PrintStreams and
 *  buffered/unbuffered DataInputStreams associated
 *  with a socket.
 */

public class SocketUtil {
  private Socket s;

  public SocketUtil(Socket s) {
    this.s = s;
  }
  
  public DataInputStream getDataStream()
      throws IOException {
    return(new DataInputStream(
                 new BufferedInputStream(
                       s.getInputStream())));
  }

  public DataInputStream getUnbufferedDataStream()
      throws IOException {
    return(new DataInputStream(s.getInputStream()));
  }

  public PrintStream getPrintStream()
      throws IOException {
    return(new PrintStream(s.getOutputStream()));
  }
  public Writer getWriterStream() throws IOException {
  	return (new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), "ISO-8859-1")));
  }
  
}
