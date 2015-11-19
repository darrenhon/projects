import java.rmi.*;
import java.rmi.server.*;
import java.io.*;
import java.util.*;

public class Server extends UnicastRemoteObject implements ServerInterface
{
  private Logger logger = new Logger("Server.log");
  private HashMap<String, String> map = new HashMap<String, String>();

  public Server () throws RemoteException
  {
  }

  // synchronize all methods for map access to avoid thread conflicts
  public synchronized String Get(String key) throws RemoteException
  {
    try
    {
      logger.LogWithScreen("GET(" + key + ") from " + getClientHost());
    }
    catch (ServerNotActiveException e)
    {
      logger.LogWithScreen("Unable to get client host:" + e.getMessage());
    }
    return map.get(key);
  }

  // synchronize all methods for map access to avoid thread conflicts
  public synchronized String Delete(String key) throws RemoteException
  {
    try
    {
      logger.LogWithScreen("DELETE(" + key + ") from " + getClientHost());
    }
    catch (ServerNotActiveException e)
    {
      logger.LogWithScreen("Unable to get client host:" + e.getMessage());
    }
    return map.remove(key);
  }

  // synchronize all methods for map access to avoid thread conflicts
  public synchronized String Put(String key, String value) throws RemoteException
  {
    try
    {
      logger.LogWithScreen("PUT(" + key + "," + value + ") from " + getClientHost());
    }
    catch (ServerNotActiveException e)
    {
      logger.LogWithScreen("Unable to get client host:" + e.getMessage());
    }
    return map.put(key, value);
  }
}
