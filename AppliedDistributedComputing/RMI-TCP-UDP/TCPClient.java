import java.net.*; 
import java.io.*;

public class TCPClient 
{
  // use the Logger helper class for logging
  private static Logger s_logger = new Logger("tcpclient.log");
  // define a constant 10s timeout for waiting responses from server
  private static final int CONNECTION_TIMEOUT = 10000;

  public static void main (String args[]) 
  {
    Socket socket = null;
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
        System.out.println("Usage: TCPClient server-host-address server-port");
        System.out.println("");
        return;
      }

      String serverHost = args[0] + ":" + serverPort;
      s_logger.LogWithScreen("Connecting to server " + serverHost);
      // connect to the server with the address supplied from command line
      // specify a timeout for read operations
      socket = new Socket(args[0], serverPort);
      socket.setSoTimeout(CONNECTION_TIMEOUT);
      s_logger.LogWithScreen("Connected to server " + serverHost);

      DataInputStream in = new DataInputStream( socket.getInputStream());
      DataOutputStream out = new DataOutputStream( socket.getOutputStream());

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
        ExecuteCommand(command, in, out);
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

      // keep reading command from user until exit or ctrl-c
      while (true)
      {
        System.out.print("Please enter command:");
        String command = stdin.readLine();
        if (command.equalsIgnoreCase("exit"))
        {
          return;
        }
        ExecuteCommand(command, in, out);
      }
    }
    catch (UnknownHostException e)
    { 
      // UnknownHostException will be thrown from Socket constructor 
      // if the server host address cannot be determined. 
      // log the error and exit 
      s_logger.LogWithScreen("UnknownHostException:"+e.getMessage());
    } 
    catch (EOFException e)
    {
      // EOFException will be thrown from DataInputStream.readUTF 
      // if the input stream unexpectedly terminates.
      // log the error and exit 
      s_logger.LogWithScreen("EOFException:"+e.getMessage());
    } 
    catch (IOException e)
    {
      // IOException will be thrown from DataInputStream.readUTF
      // if the connection is closed
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
      // if the socket is opened, close it before exit
      if (socket!=null) try 
      {
        socket.close();
      } 
      catch (IOException e)
      {
        // socket may be unable to close. log the exception and exit
        s_logger.LogWithScreen("Unable to close socket. IOException:"+e.getMessage());
      }
    }
  } 

  private static void ExecuteCommand(String command, DataInputStream in, DataOutputStream out) throws IOException
  {
    // write each command to server, get the response,
    // display to screen and write to log
    s_logger.LogWithScreen("Sending command:" + command);
    out.writeUTF(command);

    try
    {
      String data = in.readUTF();
      s_logger.LogWithScreen("Received: "+ data);
    }
    catch (SocketTimeoutException e)
    {
      // A SocketTimeoutException will be thrown from DataInputStream.readUTF
      // if it takes longer than the specified timeout
      // log the error and continue with the next command
      s_logger.LogWithScreen("SocketTimeoutException:"+e.getMessage());
    }
  }
}

