import java.net.*;
import java.io.*;

public class NetworkServer {
  protected int port, maxConnections;

  //----------------------------------------------------
  /** Build a server on specified port. It will continue
   *  to accept connections (passing each to
   *  handleConnection) until an explicit exit
   *  command is sent (e.g. System.exit) or the
   *  maximum number of connections is reached. Specify
   *  0 for maxConnections if you want the server
   *  to run indefinitely.
   */
  
  public NetworkServer(int port, int maxConnections) {
    this.port = port;
    this.maxConnections = maxConnections;
  }

  //----------------------------------------------------
  /** Monitor a port for connections. Each time one
   *  is established, pass resulting Socket to
   *  handleConnection.
   */
  
  public void listen() {
    int i=0;
    try {
      ServerSocket listener = new ServerSocket(port);
      Socket server;
      while((i++ < maxConnections) ||
            (maxConnections == 0)) {
        server = listener.accept();
        handleConnection(server);
      }
    } catch (Exception e) {
      System.out.println("Error: " + e);
    }
  }

  //----------------------------------------------------
  /** This is the method that provides the behavior
   *  to the server, since it determines what is
   *  done with the resulting socket. <B>Override this
   *  method in servers you write.</B>
   *  <P>
   *  This generic version simply reports the host
   *  that made the connection, shows the first line
   *  the client sent, and sends a single line
   *  in response.
   */

  protected void handleConnection(Socket server)throws IOException{
    SocketUtil s = new SocketUtil(server);
    DataInputStream in = s.getDataStream();
    PrintStream out = s.getPrintStream();
    out.println("Generic Network Server");
    server.close();
  }
}
