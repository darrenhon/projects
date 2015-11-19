import java.rmi.*;
import java.rmi.registry.*;
import java.io.*;

public class RMIServer
{
  private static Logger s_logger = new Logger("RMIServer.log");
  public static void main(String[] args)
  {
    try
    {
      s_logger.LogWithScreen("Binding Server object in registry");
      ServerInterface server = new Server();
      Registry registry = LocateRegistry.getRegistry();
      registry.rebind("Server", server);
      s_logger.LogWithScreen("Binding completed");
    }
    catch (RemoteException e)
    {
      s_logger.LogWithScreen(e.getMessage());
    }
  }
}
