import java.net.*; 
import java.io.*;

public class TCPServer 
{
  // Logger and HashMapParser are 2 helper classes for logging and 
  // maintaining the hash map.
  private static Logger s_logger = new Logger("tcpserver.log");
  private static HashMapParser s_parser = new HashMapParser();

  public static void main (String args[]) 
  {
    ServerSocket listenSocket = null;
    Socket clientSocket = null;
    try
    {
      int serverPort;
      try
      {
        // try to get the server port from command line argument
        // if there is no argument, or if it is not an integer, an exception
        // will be thrown and a usage description will be shown.
        serverPort = Integer.parseInt(args[0]);
      }
      catch (Exception e)
      {
        s_logger.LogWithScreen("Invalid argument(s).");
        System.out.println("Usage: TCPServer server-port");
        System.out.println("");
        return;
      }

      // create a server socket at the server port and listen to it
      listenSocket = new ServerSocket(serverPort); 
      s_logger.LogWithScreen("Listening to port " + serverPort);

      // only 1 client is supported
      // keep accepting new client if old client is disconnected
      while (true)
      {
        clientSocket = listenSocket.accept();
        String clientHost = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
        s_logger.LogWithScreen("Client " + clientHost + " connected");

        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
        DataInputStream in = new DataInputStream(clientSocket.getInputStream());

        // infinitely read data from the socket until the client closes the
        // connection or user press ctrl-c
        while (true)
        {
          String data;
          try
          {
            data = in.readUTF();
          }
          catch (EOFException e)
          {
            // EOFException will be thrown by DataInputStream.readUTF if the 
            // client connection is closed. Log it, close the socket, and
            // listen for another client.
            s_logger.LogWithScreen("Client " + clientHost + " disconnected");
            try
            {
              clientSocket.close();
            }
            catch (IOException ex)
            {
              // socket may be unable to close. Log the error and exit
              s_logger.LogWithScreen("Unable to close socket. IOException:"+ex.getMessage());
              clientSocket = null;
              return;
            }
            break;
          }

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
        // if a client is connect, close the socket before exiting the program
        if (clientSocket != null)
        {
          clientSocket.close();
        }
      }
      catch (IOException e)
      {
        // socket may be unable to close. Log the error and exit
        s_logger.LogWithScreen("Unable to close socket. IOException:"+e.getMessage());
      }
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
