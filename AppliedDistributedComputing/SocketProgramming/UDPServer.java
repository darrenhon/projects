import java.net.*; 
import java.io.*;
import java.nio.charset.*;

public class UDPServer 
{
  // Logger and HashMapParser are 2 helper classes for logging and 
  // maintaining the hash map.
  private static Logger s_logger = new Logger("udpserver.log");
  private static HashMapParser s_parser = new HashMapParser();

  public static void main (String args[]) 
  {
    DatagramSocket socket = null;
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
        System.out.println("Usage: UDPServer server-port");
        System.out.println("");
        return;
      }

      // create a datagram socket at the server port
      socket = new DatagramSocket(serverPort); 
      s_logger.LogWithScreen("Created UDP socket on port " + serverPort);
      byte[] buffer = new byte[1000]; 

      // infinitely read data from the socket until the user press ctrl-c
      while(true)
      {
        // use a 1000 byte buffer to store the incoming datagram
        DatagramPacket request = new DatagramPacket(buffer, buffer.length); 
        socket.receive(request);

        // convert the byte array into string
        String data = new String(request.getData(), 0, request.getLength(),  "UTF-8");
        String clientHost = request.getAddress().getHostAddress() + ":" + request.getPort();
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
          // log malformed data with the source address and port
          output = "Data malformed";
          s_logger.LogWithScreen("Received malformed data of length " + data.length() + " from " + clientHost);
        }

        // send the response to client
        byte[] stream = output.getBytes(Charset.forName("UTF-8"));
        DatagramPacket reply = new DatagramPacket(stream, stream.length, request.getAddress(), request.getPort()); 
        socket.send(reply);
      }
    } 
    catch (IOException e) 
    {
      // SocketException will be thrown if the socket could not be opened
      // IOException will be thrown if there is I/O error when reading from or
      // writing to the socket. Log the exception and exit program.
      s_logger.LogWithScreen("IOException:" + e.getMessage());
    } 
    catch (Exception e)
    {
      // log any other exceptions
      s_logger.LogWithScreen("Exception:" + e.getMessage());
    }
    finally 
    { 
      // if the socket is opened, close it before exiting the program
      if (socket != null)
      {
        socket.close();
      }
    }
  }
}
