import java.rmi.*;
import java.rmi.registry.*;
import java.io.*;
import java.lang.*;

public class RMIClient
{
  private static Logger s_logger = new Logger("RMIClient.log");

  public static void main(String[] args)
  {
    // check arguments and show usage in case of error
    //if (args.length != 1)
    //{
    //  s_logger.LogWithScreen("Invalid argument(s).");
    //  System.out.println("Usage: RMIClient server-address");
    //  System.out.println("");
    //  return;
    //}

    try
    {
      String rmiRegAdd = "localhost";
      if (args.length >= 1)
      {
        rmiRegAdd = args[0];
      }

      String serverName = "Server1";
      if (args.length >= 2)
      {
        serverName = args[1];
      }


      Registry registry = LocateRegistry.getRegistry();
      ServerClientInterface server = (ServerClientInterface) registry.lookup(serverName); 

      // initialize the server hash map
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

        // pre-populate the store again
        "put(A,100)",
        "put(B,200)",
        "put(C,300)",
        "put(D,400)",
        "put(E,500)",
        "put(F,600)",
        "put(G,700)",

      };

      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

      // write pre-defined commands to server
      System.out.println("Do you want to pre-populate the store? Y(es) or other keys(no):");
      if (in.readLine().equalsIgnoreCase("y"))
      {
        s_logger.LogWithScreen("Running pre-defined commands");
        for (String command : commands)
        {
          ParseAndRun(command, server);
        }
        s_logger.LogWithScreen("Finished running pre-defined commands");
      }

      // show command instructions
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

      // repeatedly get and run commands until user breaks
      while (true)
      {
        System.out.print("Please enter command:");
        String command = in.readLine();
        if (command.equalsIgnoreCase("exit"))
        {
          return;
        }
        ParseAndRun(command, server);
      }
    }
    catch (RemoteException e)
    {
      s_logger.LogWithScreen("RemoteException:" + e.getMessage());
    }
    catch (NotBoundException e)
    {
      s_logger.LogWithScreen("Server not found. NotBoundException:" + e.getMessage());
    }
    catch (Exception e)
    {
      s_logger.LogWithScreen("Exception:" + e.getMessage());
    }
  }

  public static void ParseAndRun(String command, ServerClientInterface server)
  {
    s_logger.Log("Entered command:" + command);
    try
    {
      // extract OPERATOR, KEY, and VALUE from the input
      String[] results = command.split("[(,)]");

      // if it is a well-formed get command
      if (results.length == 2 && results[0].equalsIgnoreCase("get"))
      {
        String value = server.Get(results[1]);
        s_logger.LogWithScreen("Value:" + value);
      }
      // if it is a well-formed delete command
      else if (results.length == 2 && results[0].equalsIgnoreCase("delete"))
      {
        String value = server.Delete(results[1]);
        s_logger.LogWithScreen("Result:" + value);
      }
      // if it is a well-formed put command
      else if (results.length == 3 && results[0].equalsIgnoreCase("put"))
      {
        String value = server.Put(results[1], results[2]);
        s_logger.LogWithScreen("Put finished " + (value == null ? "" : "replacing the original value"));
      }
      else if (results[0].equalsIgnoreCase("clear"))
      {
        server.Clear();
        s_logger.LogWithScreen("Clear finished ");
      }
      else
      {
        // all other cases are malformed data
        s_logger.LogWithScreen("Command error:" + command);
      }
    }
    catch (Exception e)
    {
      s_logger.LogWithScreen("Command error:" + command + ". Exception:" + e.getMessage());
    }
  }
}
