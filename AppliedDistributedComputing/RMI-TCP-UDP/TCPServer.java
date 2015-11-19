import java.net.*; 
import java.io.*;

public class TCPServer 
{
  // Logger and HashMapParser are 2 helper classes for logging and 
  // maintaining the hash map.
  private static Logger s_logger = new Logger("tcpserver.log");
  private static HashMapParser s_parser = new HashMapParser();
  private static volatile int s_clients = 0;
  private static int s_maxClients = 5;

  static class Connection extends Thread 
  {

    DataInputStream in;
    DataOutputStream out;
    Socket clientSocket;
    String clientHost;

    public Connection (Socket socket) 
    {
      try
      {
        clientSocket = socket;
        clientHost  = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
        s_logger.LogWithScreen("Client " + clientHost + " connected");
        in = new DataInputStream(clientSocket.getInputStream());
        out = new DataOutputStream(clientSocket.getOutputStream()); 
        start();
      }
      catch (IOException e)
      {
        s_logger.LogWithScreen("Failed to create input and output stream for " + clientHost);
      }
    }

    public void run()
    {
      ++s_clients;
      try
      { 
        if (s_clients > s_maxClients)
        {
          String msg = "Maximum number of clients reached. Closing connection from " + clientHost + ".";
          s_logger.LogWithScreen(msg);
          out.writeUTF(msg);
          return;
        }

        // infinitely read data from the socket until the client closes connection
        while (true)
        {
          String data;
          data = in.readUTF();
          s_logger.LogWithScreen("Received data from " + clientHost + " :" + (data.length() > 100 ? data.substring(0, 100) + "..." : data));

          String output;
          try
          {
            // Process the data with HashMapParser, a helper class to store a 
            // hashmap and parse and execute input from client
            // The Parse method will throw exception if the data is malformed
            output = s_parser.Parse(data);
            s_logger.LogWithScreen(output);
          }
          catch (Exception e)
          {
            // log malformed data
            output = "Data malformed";
            s_logger.LogWithScreen("Received malformed data of length " + data.length());
          }

          // send the response to client
          out.writeUTF(output);
        }
      } 
      catch (EOFException e) 
      {
        // EOFException will be thrown by DataInputStream.readUTF if the 
        // client connection is closed. Log it, close the socket, and exit thread
        s_logger.LogWithScreen("Client " + clientHost + " disconnected");
      } 
      catch(IOException e) 
      {
        System.out.println("IO:"+e.getMessage());
      } 
      finally 
      { 
        if (clientSocket != null)
        {
          try 
          {
            clientSocket.close();
          }
          catch (IOException e)
          {
            // socket may be unable to close. Log the error and end thread
            s_logger.LogWithScreen("Unable to close client socket " + clientHost + ". IOException:" + e.getMessage());
          }
        }
        --s_clients;
      }
    } 
  }

  public static void main (String args[]) 
  {
    ServerSocket listenSocket = null;
    try
    {
      int serverPort;
      try
      {
        // try to get the server port from command line argument
        // if there is no argument, or if it is not an integer, an exception
        // will be thrown and a usage description will be shown.
        serverPort = Integer.parseInt(args[0]);
        if (args.length == 2)
        {
          s_maxClients = Integer.parseInt(args[1]);
        }
      }
      catch (Exception e)
      {
        s_logger.LogWithScreen("Invalid argument(s).");
        System.out.println("Usage: TCPServer server-port [max-client]");
        System.out.println("");
        return;
      }

      // create a server socket at the server port and listen to it
      listenSocket = new ServerSocket(serverPort); 
      s_logger.LogWithScreen("Listening to port " + serverPort);

      // keep accepting new client
      while (true)
      {
        // create a new thread for each client
        // the number of clients is limited by s_maxClients
        Connection connection = new Connection(listenSocket.accept());
      }
    } 
    catch (IOException e) 
    {
      // IOException will be thrown if there is I/O error when opening
      // the socket. Log the exception and exit program.
      s_logger.LogWithScreen("IOException:" + e.getMessage());
    } 
    catch (Exception e)
    {
      // log any other exceptions
      s_logger.LogWithScreen("Exception:" + e.getMessage());
    }
    finally 
    { 
      try 
      {
        // if the server socket is opened, close it before exiting the program
        if (listenSocket != null)
        {
          listenSocket.close();
        }
      }
      catch (IOException e)
      {
        // socket may be unable to close. Log the error and exit
        s_logger.LogWithScreen("Unable to close socket. IOException:"+e.getMessage());
      }
    }
  }
}
