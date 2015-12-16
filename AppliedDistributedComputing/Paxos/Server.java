import java.util.*;

public class Server
{
  private static Logger s_logger = new Logger("Server.log");
  private static ArrayList<ServerBase> servers = new ArrayList<ServerBase>();

  public static void PrintUsage()
  {
    System.out.println("Usage: Server [rmi-registry-address] Role [Role] [Role] ...");
    System.out.println("Role: dbserver proposer learner acceptor");
    System.out.println("Example: Server localhost dbserver proposer proposer learner learner acceptor acceptor");
  }

  public static void main(String[] argv)
  {
    if (argv.length < 1)
    {
      PrintUsage();
      return;
    }

    boolean foundRole = false;
    String rmi = "localhost";
    for (String arg : argv)
    {
      if (arg.equalsIgnoreCase("dbserver") ||
          arg.equalsIgnoreCase("proposer") ||
          arg.equalsIgnoreCase("learner") ||
          arg.equalsIgnoreCase("acceptor"))
      {
        foundRole = true;
      }
      else
      {
        rmi = arg;
      }
    }
    if (!foundRole)
    {
      PrintUsage();
      return;
    }

    for (String arg : argv)
    {
      try
      {
        if (arg.equalsIgnoreCase("dbserver"))
        {
          servers.add(new DBServer(rmi, s_logger));
        }
        else if (arg.equalsIgnoreCase("proposer"))
        {
          servers.add(new Proposer(rmi, s_logger));
        }
        else if (arg.equalsIgnoreCase("learner"))
        {
          servers.add(new Learner(rmi, s_logger));
        }
        else if (arg.equalsIgnoreCase("acceptor"))
        {
          servers.add(new Acceptor(rmi, s_logger));
        }
      }
      catch (Exception e)
      {
        s_logger.LogWithScreen("Failed to create " + arg + ". Exception: " + e.getMessage());
        System.exit(0);
      }
    }
  }
}
