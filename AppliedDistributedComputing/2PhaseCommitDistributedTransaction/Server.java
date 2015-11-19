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
public class Server extends UnicastRemoteObject implements ServerClientInterface, ServerServerInterface
{
  private Logger logger = new Logger("Server.log");
  private HashMap<String, String> map = new HashMap<String, String>();

  // each Server maintains a list of active servers
  private HashMap<String, ServerServerInterface> servers = new HashMap<String, ServerServerInterface>();

  // this is the transaction log
  private Vector<Transaction> transactions = new Vector<Transaction>();

  // this is a lock to synchronize multiple clients connections
  private Object s_clientLock = new Object();
  // this is a lock to synchronize multiple servers connections
  private Object s_serverLock = new Object();

  @SuppressWarnings("unchecked")
  public Server () throws RemoteException
  {
    // retrieve DB
    try
    {
      ObjectInputStream ois = new ObjectInputStream(new FileInputStream("ServerData.obj"));   
      map = (HashMap<String, String>)ois.readObject();
      ois.close();
      logger.LogWithScreen("Restored db from persistent storage");
    }
    catch(Exception e)
    {
      logger.LogWithScreen("Unable to read db from persistent storage.");
    }

    // retrieve transaction log
    try
    {
      ObjectInputStream ois = new ObjectInputStream(new FileInputStream("TransactionLog.obj"));   
      transactions = (Vector<Transaction>)ois.readObject();
      ois.close();
      logger.LogWithScreen("Restored " + transactions.size() + " transactions from transaction log");
    }
    catch(Exception e)
    {
      logger.LogWithScreen("Unable to read transaction log.");
    }
  }

  // called by the RMIServer after transaction log is retrieved from disk
  // if there is any unfinished transaction, ask other active servers for their statuses, and try to finish it
  public void RecoverTransactions()
  {
    synchronized(s_serverLock)
    {
      for (Transaction tran : transactions)
      {
        if (tran.Status == Status.Unknown || tran.Status == Status.Preparing)
        {
          Status status = tran.Status;
          // ask other servers what happened to this transaction
          for (ServerServerInterface server : GetServers())
          {
            Status tranStatus;
            try
            {
              tranStatus = server.GetStatus(tran);
            }
            catch (RemoteException e)
            {
              logger.LogWithScreen("Unable to retrieve transaction " + tran.Id + " status from server");
              continue;
            }

            // skip these 2 statuses. Not helpful
            if (tranStatus == Status.Unknown || tranStatus == Status.Preparing)
            {
              continue;
            }
            status = tranStatus;
            // if one of them is aborted, abort it
            if (tranStatus == Status.Aborted)
            {
              break;
            }
          }
          // conclusion is abort
          if (status == Status.Aborted)
          {
            RealAbort(tran);
          }
          // conclusion is commit
          else if (status == Status.Committed)
          {
            RealCommit(tran);
          }
          // else leave it as is
        }
      }
    }
  }

  // ServerServerInterface implementation

  // get an array of currently active servers
  public ServerServerInterface[] GetServers()
  {
    return servers.values().toArray(new ServerServerInterface[servers.size()]);
  }

  public void AddServer(String server) throws RemoteException
  {
    synchronized(s_serverLock)
    {
      try
      {
        Registry registry = LocateRegistry.getRegistry();
        servers.put(server, (ServerServerInterface)registry.lookup(server));
        logger.LogWithScreen("Added " + server + " to list");
      }
      catch (Exception e)
      {
        logger.LogWithScreen("Unable to add " + server + " to server list. Exception:" + e.getMessage());
      }
    }
  }

  public void RemoveServer(String server) throws RemoteException
  {
    synchronized(s_serverLock)
    {
      try
      {
        servers.remove(server);
        logger.LogWithScreen("Removed " + server + " from list");
      }
      catch (Exception e)
      {
        logger.LogWithScreen("Unable to add " + server + " to server list. Exception:" + e.getMessage());
      }
    }
  }

  // called by the transaction coordinator in the 1st phase of a 2-phase commit
  public boolean PrepareToCommit(Transaction tran) throws RemoteException
  {
    synchronized(s_serverLock)
    {
      logger.LogWithScreen("Preparing transaction " + tran);
      transactions.add(tran);
      PersistTransactionLog();

      if (tran.Opcode == Opcode.Delete)
      {
        return map.containsKey(tran.Key);
      }

      return true;
    }
  }

  // called by the transaction coordinator in the 2nd phase of a 2-phase commit
  public String DoCommit(Transaction tran) throws RemoteException
  {
    synchronized(s_serverLock)
    {
      return RealCommit(tran);
    }
  }

  // called by the transaction coordinator in the 2nd phase of a 2-phase commit
  public void DoAbort(Transaction tran) throws RemoteException
  {
    synchronized(s_serverLock)
    {
      RealAbort(tran);
    }
  }

  // called from other servers when they wake up and found unfinished transactions
  public Status GetStatus(Transaction tran) throws RemoteException
  {
    synchronized(s_serverLock)
    {
      for (Transaction myTran : transactions)
      {
        if (myTran.Id.equals(tran.Id))
        {
          return myTran.Status;
        }
      }
      return Status.Unknown;
    }
  }

  // ServerClientInterface implementation

  // clear the map. this command is not part of the project spec
  public void Clear() throws RemoteException
  {
    synchronized(s_clientLock)
    {
      try
      {
        logger.LogWithScreen("Received CLEAR from " + getClientHost());
      }
      catch (ServerNotActiveException e)
      {
        logger.LogWithScreen("Unable to get client host:" + e.getMessage());
      }

      // the server receiving the command will be the coordinator and initiate the 2-phase commit
      Transaction tran = new Transaction(UUID.randomUUID(), Opcode.Clear);
      DoTransaction(tran);
    }
  }

  public String Get(String key) throws RemoteException
  {
    synchronized(s_clientLock)
    {
      try
      {
        logger.LogWithScreen("Received GET(" + key + ") from " + getClientHost());
      }
      catch (ServerNotActiveException e)
      {
        logger.LogWithScreen("Unable to get client host:" + e.getMessage());
      }
      String result = map.get(key);
      return result;
    }
  }

  public String Delete(String key) throws RemoteException
  {
    synchronized(s_clientLock)
    {
      try
      {
        logger.LogWithScreen("Received DELETE(" + key + ") from " + getClientHost());
      }
      catch (ServerNotActiveException e)
      {
        logger.LogWithScreen("Unable to get client host:" + e.getMessage());
      }

      // the server receiving the command will be the coordinator and initiate the 2-phase commit
      Transaction tran = new Transaction(UUID.randomUUID(), Opcode.Delete, key);
      return DoTransaction(tran);
    }
  }

  public String Put(String key, String value) throws RemoteException
  {
    synchronized(s_clientLock)
    {
      try
      {
        logger.LogWithScreen("Received PUT(" + key + "," + value + ") from " + getClientHost());
      }
      catch (ServerNotActiveException e)
      {
        logger.LogWithScreen("Unable to get client host:" + e.getMessage());
      }

      // the server receiving the command will be the coordinator and initiate the 2-phase commit
      Transaction tran = new Transaction(UUID.randomUUID(), Opcode.Put, key, value);
      return DoTransaction(tran);
    }
  }

  // private functions

  // start the 2-phase commit
  private String DoTransaction(Transaction tran) throws RemoteException
  {
    // 1st phase
    boolean allAgree = true;
    // the coordinator prepare commit by writing into transaction log 
    allAgree = PrepareToCommit(tran);
    // inform the cohort to prepare commit
    for (ServerServerInterface server : GetServers())
    {
      if (!server.PrepareToCommit(tran))
      {
        allAgree = false;
      }
    }

    // 2nd phase
    // inform the cohort to commit/abort
    for (ServerServerInterface server : GetServers())
    {
      if (allAgree)
      {
        server.DoCommit(tran);
      }
      else 
      { 
        server.DoAbort(tran);
      }
    }
    // commit/abort from coordinator, mark transaction finished and update db
    if (allAgree)
    {
      return DoCommit(tran);
    }
    else
    {
      DoAbort(tran);
      return "Transaction aborted";
    }
  }

  // called when a delete command is committing
  private synchronized String RealDelete(String key)
  {
    String result =  map.remove(key);
    PersistDB();
    return result;
  }

  // called when a put command is committing
  private synchronized String RealPut(String key, String value)
  {
    String result = map.put(key, value);
    PersistDB();
    return result;
  }

  // called when a clear command is committing
  private synchronized void RealClear()
  {
    map.clear();
    PersistDB();
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
      logger.LogWithScreen("Change written to persistent storage");
    }
    catch(Exception e)
    {
      logger.LogWithScreen("Unable to write change to persistent storage. Exception:" + e.getMessage());
    }
  }

  // save the in-memory transaction log to disk
  private void PersistTransactionLog()
  {
    try
    {
      FileOutputStream fout = new FileOutputStream("TransactionLog.obj");
      ObjectOutputStream oos = new ObjectOutputStream(fout);   
      Vector<Transaction> transToSave = new Vector<Transaction>();
      for (Transaction tran : transactions)
      {
        // only save ongoing transactions. transactions which are committed or aborted do not need recovery
        if (tran.Status == Status.Unknown || tran.Status == Status.Preparing)
        {
          transToSave.add(tran);
        }
      }
      oos.writeObject(transToSave);
      oos.close();
      logger.LogWithScreen("Transaction log written to persistent storage");
    }
    catch(Exception e)
    {
      logger.LogWithScreen("Unable to write tranaction log to persistent storage. Exception:" + e.getMessage());
    }
  }

  // called in the 2nd phase of 2-phase commit, or in RecoverTransactions during server start up
  // must be called within a server lock because it will update transaction log
  private String RealCommit(Transaction tran)
  {
    logger.LogWithScreen("Committing transaction " + tran);
    String result = null;
    switch (tran.Opcode)
    {
      case Clear:
        RealClear();
        break;
      case Delete:
        result = RealDelete(tran.Key);
        break;
      case Put:
        result = RealPut(tran.Key, tran.Value);
        break;
    }

    for (Transaction myTran : transactions)
    {
      if (myTran.Id.equals(tran.Id))
      {
        myTran.Status = Status.Committed;
      }
    }
    PersistTransactionLog();

    return result;
  }

  // called in the 2nd phase of 2-phase commit, or in RecoverTransactions during server start up
  // must be called within a server lock because it will update transaction log
  private void RealAbort(Transaction tran)
  {
    logger.LogWithScreen("Aborting transaction " + tran);
    for (Transaction myTran : transactions)
    {
      if (myTran.Id.equals(tran.Id))
      {
        myTran.Status = Status.Aborted;
      }
    }
    PersistTransactionLog();
  }
}

