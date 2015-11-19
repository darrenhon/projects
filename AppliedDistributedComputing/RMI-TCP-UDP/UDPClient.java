import java.net.*; 
import java.io.*;
import java.nio.charset.*;

public class UDPClient 
{
  // use the Logger helper class for logging
  private static Logger s_logger = new Logger("udpclient.log");
  // define a constant 10s timeout for waiting responses from server
  private static final int CONNECTION_TIMEOUT = 10000;

  public static void main (String args[]) 
  {
    DatagramSocket socket = null;
    try
    {
      int serverPort;
      try
      {
        // try to get the server port from command line
        // if it is unable to get the port or if the 
        // port is not an integer, and exception will be thrown
        // and a usage description will be shown
        serverPort = Integer.parseInt(args[1]);
      }
      catch (Exception e)
      {
        s_logger.LogWithScreen("Invalid argument(s).");
        System.out.println("Usage: UDPClient server-host-address server-port");
        System.out.println("");
        return;
      }

      // create a DatagramSocket with a timeout for receive operations
      socket = new DatagramSocket();
      socket.setSoTimeout(CONNECTION_TIMEOUT);

      // here are a list of commands to be sent to the server sequentially
      String[] commands = {

        // pre-populate the store
        "put(A,100)",
        "put(B,200)",
        "put(C,300)",
        "put(D,400)",
        "put(E,500)",
        "put(F,600)",
        "put(G,700)",

        // perform 5 gets
        "get(A)",
        "get(B)",
        "get(C)",
        "get(D)",
        "get(E)",

        // perform 5 puts
        "put(A,1000)",
        "put(B,2000)",
        "put(C,3000)",
        "put(D,4000)",
        "put(E,5000)",

        // get back the 5 puts and make sure they are the new values
        "get(A)",
        "get(B)",
        "get(C)",
        "get(D)",
        "get(E)",

        // perform 5 deletes
        "delete(A)",
        "delete(B)",
        "delete(C)",
        "delete(D)",
        "delete(E)",

        // get back the 5 deletes and make sure they're gone
        "get(A)",
        "get(B)",
        "get(C)",
        "get(D)",
        "get(E)",

        // try get a non-existing key
        "get(Z)",

        // try delete a non-existing key
        "delete(Z)",

        // try malformed commands
        "put(A)",
        "get(A,100)",
        "delete(A,100)",
        "gibberish %^@$%^$#%&$^&#%^"
      };

      // write commands to server
      for (String command : commands)
      {
        ExecuteCommand(command, socket, args[0], serverPort);
      }

      // show command instructions
      BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
      System.out.println("");
      System.out.println("Command usage:");
      System.out.println("Put: PUT(key,value)");
      System.out.println("Get: GET(key)");
      System.out.println("Delete: DELETE(key)");
      System.out.println("Commands are case insensitive.");
      System.out.println("Key and value are case and space sensitive.");
      System.out.println("No parenthesis in key and value.");
      System.out.println("Enter exit or Ctrl-C to exit");
      System.out.println("");

      while (true)
      {
        System.out.print("Please enter command:");
        String command = stdin.readLine();
        if (command.equalsIgnoreCase("exit"))
        {
          return;
        }
        ExecuteCommand(command, socket, args[0], serverPort);
      }

      // exit the program after all commands are sent
    }
    catch (IOException e)
    {
      // A SocketException will be thrown from DatagramSocket constructor if
      // the socket could not be opened
      // An IOException will be thrown from DatagramSock.receive if there is
      // an I/O error
      // log the error and exit
      s_logger.LogWithScreen("IOException:"+e.getMessage());
    } 
    catch (Exception e)
    {
      // log any other exception and exit
      s_logger.LogWithScreen("Exception:"+e.getMessage());
    } 
    finally 
    {
      // if the socket is opened, close it before exiting the program
      if (socket!=null) 
      {
        socket.close();
      } 
    }
  } 

  private static void ExecuteCommand(String command, DatagramSocket socket, String server, int serverPort)
    throws UnknownHostException, IOException, UnsupportedEncodingException
  {
    InetAddress serverAdd = InetAddress.getByName(server); 
    String serverHost = server + ":" + serverPort;
    
    // write each command to server
    s_logger.LogWithScreen("Sending command to " + serverHost + " :" + command);
    byte[] stream = command.getBytes(Charset.forName("UTF-8"));
    DatagramPacket request = new DatagramPacket(stream, stream.length, serverAdd, serverPort); 
    socket.send(request);

    // get the response from the socket with a 1000 byte buffer
    byte[] buffer = new byte[1000];
    DatagramPacket reply = new DatagramPacket(buffer, buffer.length); 
    try
    {
      socket.receive(reply);

      // convert the byte array into string and log it
      String data = new String(reply.getData(), "UTF-8");
      s_logger.LogWithScreen("Received: "+ data);
    }
    catch (SocketTimeoutException e)
    {
      // A SocketTimeoutException will be thrown from DatagramSocket.receive
      // if it takes longer than the specified timeout
      // log the exception and continue with the next command
      s_logger.LogWithScreen("SocketTimeoutException:"+e.getMessage());
    }
  }
}

