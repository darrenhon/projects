import java.rmi.*;
import java.rmi.server.*;
import java.io.*;
import java.util.*;
import java.rmi.*;
import java.rmi.registry.*;

// Server is a remote object to be invoked by clients and other servers
// The interface for server-server communication is ServerServerInterface
// The interface for server-client communication is ServerClientInterface
// Each RMIServer instance has a Server object registered in the rmiregistry
public class DBServer extends ServerBase implements DBServerInterface
{
  private HashMap<String, String> map = new HashMap<String, String>();

  @SuppressWarnings("unchecked")
  public DBServer(String rmi, Logger logger) throws Exception
  {
    super(rmi, logger);
    // retrieve DB
    try
    {
      ObjectInputStream ois = new ObjectInputStream(new FileInputStream("ServerData.obj"));   
      map = (HashMap<String, String>)ois.readObject();
      ois.close();
      LogWithScreen("Restored db from persistent storage");
    }
    catch(Exception e)
    {
      LogWithScreen("Unable to read db from persistent storage.");
    }
  }

  protected void Process(Task task)
  {
    switch (task.Type)
    {
      case Do:
        switch (task.OpCode)
        {
          case Put:
            Put(task.Key, task.Value);
            break;
          case Delete:
            Delete(task.Key);
            break;
        }
        break;
      default:
        LogWithScreen("Unknown task.");
        break;
    }
  }

  // ServerClientInterface implementation

  public String Get(String key) throws RemoteException
  {
    synchronized(map)
    {
      try
      {
        LogWithScreen("Received GET(" + key + ") from " + getClientHost());
      }
      catch (ServerNotActiveException e)
      {
        LogWithScreen("Unable to get client host:" + e.getMessage());
      }
      String result = map.get(key);
      return result;
    }
  }

  // private functions

  // called when a delete command is committing
  private String Delete(String key)
  {
    synchronized(map)
    {
      String result =  map.remove(key);
      PersistDB();
      return result;
    }
  }

  // called when a put command is committing
  private String Put(String key, String value)
  {
    synchronized(map)
    {
      String result = map.put(key, value);
      PersistDB();
      return result;
    }
  }

  // save the in-memory hashmap to disk
  private void PersistDB()
  {
    try
    {
      FileOutputStream fout = new FileOutputStream("ServerData.obj");
      ObjectOutputStream oos = new ObjectOutputStream(fout);   
      oos.writeObject(map);
      oos.close();
      LogWithScreen("Change written to persistent storage");
    }
    catch(Exception e)
    {
      LogWithScreen("Unable to write change to persistent storage. Exception:" + e.getMessage());
    }
  }
}

