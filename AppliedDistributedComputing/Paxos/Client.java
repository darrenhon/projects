import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.io.*;
import java.lang.*;
import java.util.*;

public class Client extends UnicastRemoteObject implements ClientInterface
{
  private Logger logger = new Logger("Client.log");
  private String rmiAddress = "localhost";
  private ArrayList<Integer> dbservers;
  private ArrayList<Integer> proposers;
  private final int MAX_CLIENT = 10;
  private int Id = 0;

  public Client() throws RemoteException
  {}

  public void Announce(Task task)
  {
    switch (task.OpCode)
    {
      case Delete:
        logger.LogWithScreen("Deleted key:" + task.Key);
        break;
      case Put:
        logger.LogWithScreen("Put done. Key:" + task.Key + ". Value:" + task.Value);
        break;
    }
  }

  private ArrayList<Integer> FindRoleMembers(String role)
  {
    ArrayList<Integer> results = new ArrayList<Integer>();
    Registry registry;
    try
    {
      registry = LocateRegistry.getRegistry(rmiAddress);
    }
    catch (Exception e)
    {
      return new ArrayList<Integer>();
    }
    for (int i = 1; i <= ServerBase.MAX_SERVER; i++)
    {
      try
      {
        // if it is bound, add to results
        registry.lookup(role + i);
        results.add(i);
      }
      catch (Exception e)
      {
      }
    }
    return results;
  }

  private ServerInterface GetRoleMember(String role, int id)
  {
    try
    {
      Registry registry = LocateRegistry.getRegistry(rmiAddress);
      return (ServerInterface)registry.lookup(role + id);
    }
    catch (Exception e)
    {
      return null;
    }
  }

  private ServerInterface GetFirstWorkingProposer()
  {
    for (int id : proposers)
    {
      ServerInterface proposer = GetRoleMember("Proposer", id);
      if (proposer != null)
      {
        return proposer;
      }
    }
    return null;
  }

  private void UnregisterRMI()
  {
    try
    {
      Registry registry = LocateRegistry.getRegistry(rmiAddress);
      registry.unbind("Client" + Id);
      logger.LogWithScreen("Unbound Client" + Id);
    }
    catch (Exception e)
    {
      logger.LogWithScreen("Unable to unbind client. Exception:" + e.getMessage());
      return;
    }
  }

  private void RegisterRMI()
  {
    try
    {
      Registry registry = LocateRegistry.getRegistry(rmiAddress);
      for (int i = 1; i <= MAX_CLIENT; i++)
      {
        try
        {
          // if it is bound, then try the next number
          registry.lookup("Client" + i);
        }
        catch (NotBoundException e)
        {
          // take the available id
          Id = i;
          registry.rebind("Client" + Id, this);
          logger.LogWithScreen("Registered Client" + Id);
          return;
        }
      }
      if (Id == 0)
      {
        logger.LogWithScreen("MAX_CLIENT(" + MAX_CLIENT + ") reached");
        return;
      }
    }
    catch (Exception e)
    {
      logger.LogWithScreen("Unable to register client. Exception:" + e.getMessage());
      return;
    }
  }

  private void Run(String[] argv)
  {
    if (argv.length >= 1)
    {
      rmiAddress = argv[0];
    }

    Runtime.getRuntime().addShutdownHook(
        new Thread() 
        {
          public void run() 
          {
            UnregisterRMI();
          }
        });
    RegisterRMI();
    dbservers = FindRoleMembers("DBServer");
    proposers = FindRoleMembers("Proposer");

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
    try
    {
      if (in.readLine().equalsIgnoreCase("y"))
      {
        logger.LogWithScreen("Running pre-defined commands");
        for (String command : commands)
        {
          ParseAndRun(command);
        }
        logger.LogWithScreen("Finished running pre-defined commands");
      }
    }
    catch (IOException e)
    {
      return;
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
      String command;
      try
      {
        command = in.readLine();
      }
      catch (Exception e)
      {
        return;
      }
      if (command.equalsIgnoreCase("exit"))
      {
        System.exit(0);
      }
      ParseAndRun(command);
      try { Thread.sleep(700); } catch (Exception e){}
    }
  }

  public static void main(String[] argv)
  {
    try
    {
      new Client().Run(argv);
    }
    catch (Exception e)
    {
    }
  }

  private void Get(String key)
  {
    HashMap<String, Integer> votes = new HashMap<String, Integer>();
    for (int id : dbservers)
    {
      DBServerInterface dbserver = (DBServerInterface)GetRoleMember("DBServer", id);
      if (dbserver != null)
      {
        String value;
        try
        {
          value = dbserver.Get(key);
        }
        catch (Exception e)
        {
          continue;
        }
        int vote = 0;
        if (votes.containsKey(value))
        {
          vote = votes.get(value);
        }
        if (++vote > dbservers.size() / 2)
        {
          // majority achieved
          logger.LogWithScreen("Key:" + key + ". Value:" + value);
          return;
        }
        votes.put(value, vote);
      }
    }
    logger.LogWithScreen("Unable to get majority value");
  }

  private void Delete(String key)
  {
    ServerInterface proposer = GetFirstWorkingProposer();
    if (proposer == null)
    {
      logger.LogWithScreen("No working proposer.");
      return;
    }
    Task request = new Task();
    request.Type = TaskType.Request;
    request.ClientId = Id;
    request.OpCode = OpCode.Delete;
    request.Key = key;
    try
    {
      proposer.AddTask(request);
    }
    catch (Exception e)
    {
      logger.LogWithScreen("Failed to request proposer");
    }
  }

  private void Put(String key, String value)
  {
    ServerInterface proposer = GetFirstWorkingProposer();
    if (proposer == null)
    {
      logger.LogWithScreen("No working proposer.");
      return;
    }
    Task request = new Task();
    request.Type = TaskType.Request;
    request.ClientId = Id;
    request.OpCode = OpCode.Put;
    request.Key = key;
    request.Value = value;
    try
    {
      proposer.AddTask(request);
    }
    catch (Exception e)
    {
      logger.LogWithScreen("Failed to request proposer");
    }
  }

  private void ParseAndRun(String command)
  {
    logger.Log("Entered command:" + command);
    try
    {
      // extract OPERATOR, KEY, and VALUE from the input
      String[] results = command.split("[(,)]");

      // if it is a well-formed get command
      if (results.length == 2 && results[0].equalsIgnoreCase("get"))
      {
        Get(results[1]);
      }
      // if it is a well-formed delete command
      else if (results.length == 2 && results[0].equalsIgnoreCase("delete"))
      {
        Delete(results[1]);
      }
      // if it is a well-formed put command
      else if (results.length == 3 && results[0].equalsIgnoreCase("put"))
      {
        Put(results[1], results[2]);
      }
      else
      {
        // all other cases are malformed data
        logger.LogWithScreen("Command error:" + command);
      }
    }
    catch (Exception e)
    {
      logger.LogWithScreen("Command error:" + command + ". Exception:" + e.getMessage());
    }
  }
}
