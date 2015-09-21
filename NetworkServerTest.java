public class NetworkServerTest {
  public static void main(String[] args) {
    int port = 5555;
    if (args.length > 0)
      port = Integer.parseInt(args[0]);
    NetworkServer nwServer = new NetworkServer(port, 0);
    nwServer.listen();
  }
}
