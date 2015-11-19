import java.rmi.*;
import java.rmi.registry.*;
import java.io.*;
import java.lang.*;

public class RMIServer
{
  private static Logger s_logger = new Logger("RMIServer.log");
  private static String s_server;
  private static Server s_thisServer;
  // maximum number of simulaneous servers
  private static final int MAX_SERVER = 10;

  public static void main(String[] args)
  {
    // remove itself from other servers' active server list during shutdown
    Runtime.getRuntime().addShutdownHook(
        new Thread() 
        {
          public void run() 
          {
            try
            {
              if (s_server == null || s_thisServer == null)
              {
                return;
              }

              Registry registry = LocateRegistry.getRegistry();
              registry.unbind(s_server);
              s_logger.LogWithScreen("Server " + s_server + " unbound");
              for (ServerServerInterface server : s_thisServer.GetServers())
              {
                server.RemoveServer(s_server);
              }
            }
            catch (Exception e)
            {
              s_logger.LogWithScreen("Unable to unbind " + s_server + " on exit. Exception:" + e.getMessage());
            }
          }
        });

    try
    {
      // locate the rmiregistry address from command line argument, defaul localhost
      String rmiRegAdd = "localhost";
      if (args.length == 1)
      {
        rmiRegAdd = args[0];
      }

      s_logger.LogWithScreen("Binding Server object in registry");
      s_thisServer = new Server();
      Registry registry = LocateRegistry.getRegistry(rmiRegAdd);
      // discover existing servers from the registry by name "Server#"
      for (int i = 1; i <= MAX_SERVER; i++)
      {
        try
        {
          // if it is bound, add it to the server list, then try the next number
          registry.lookup("Server" + i);
          s_thisServer.AddServer("Server" + i);
        }
        catch (NotBoundException e)
        {
          // take the available name
          if (s_server == null)
          {
            s_server = "Server" + i;
          }
        }
      }
      registry.rebind(s_server, s_thisServer);
      s_logger.LogWithScreen("Binding " + s_server + " completed");
      // add this server to each existing server
      for (ServerServerInterface server : s_thisServer.GetServers())
      {
        server.AddServer(s_server);
      }
      // after initialization, recover unfinished transactions
      s_thisServer.RecoverTransactions();
    }
    catch (RemoteException e)
    {
      s_logger.LogWithScreen("RemoteException:" + e.getMessage());
      System.exit(0);
    }
  }
}
