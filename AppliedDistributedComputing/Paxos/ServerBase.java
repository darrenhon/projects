import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public abstract class ServerBase extends UnicastRemoteObject implements ServerInterface
{
  private class Down extends TimerTask
  {
    @Override
    public void run() 
    {
      if (exit) return;
      int ran = (int)(Math.random() * 10.0);
      LogWithScreen("Server down. Will be up in " + ran + "s");
      UnregisterRMI();
      timer.schedule(new Up(), ran * 1000);
    }
  }
  private class Up extends TimerTask
  {
    @Override
    public void run() 
    {
      if (exit) return;
      try
      {
        RegisterRMI();
      } catch (Exception e){}
      int ran = (int)(Math.random() * 60.0);
      timer.schedule(new Down(), ran * 1000);
    }
  }

  public static final int MAX_SERVER = 10;
  protected Logger logger;
  private String rmiAddress;
  private ConcurrentLinkedQueue<Task> tasks = new ConcurrentLinkedQueue<Task>();
  private Timer timer = new Timer();
  protected int Id = 0;
  private boolean exit = false;
  private Thread worker = new Thread()
  {
    public void run()
    {
      while (!exit)
      {
        while (!tasks.isEmpty() && ! exit)
        {
          try
          {
            Process(tasks.poll());
          }
          catch (Exception e)
          {
            LogWithScreen("Process task exception:" + e + "," + e.getMessage());
          }
        }
        try
        {
          wait();
        }
        catch (Exception e)
        {
        }
      }
    }
  };

  abstract void Process(Task task) throws Exception;

  public ServerBase(String rmi, Logger logger) throws Exception
  {
    this.logger = logger;
    rmiAddress = rmi;
    worker.start();
    Runtime.getRuntime().addShutdownHook(
        new Thread() 
        {
          public void run() 
          {
            try
            {
              synchronized(worker)
              {
                exit = true;
                worker.notify();
                worker.join();
              }
              UnregisterRMI();
            }
            catch (Exception e)
            {
              LogWithScreen("Unable to end worker thread on exit. Exception:" + e + "," + e.getMessage());
            }
          }
        });
    RegisterRMI();
    timer.schedule(new Down(), (long)(Math.random() * 60 * 1000));
  }

  public void AddTask(Task task)
  {
    LogWithScreen("Received task. " + task.toString());
    tasks.add(task);
    synchronized(worker)
    {
      worker.notify();
    }
  }

  protected void LogWithScreen(String msg)
  {
    logger.LogWithScreen(GetServerName() + ":" + msg);
  }

  protected String GetServerName()
  {
    return GetRole() + Id;
  }

  protected ArrayList<Integer> FindRoleMembers(String role)
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
    for (int i = 1; i <= MAX_SERVER; i++)
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

  protected ClientInterface GetClient(int id)
  {
    try
    {
      Registry registry = LocateRegistry.getRegistry(rmiAddress);
      return (ClientInterface)registry.lookup("Client" + id);
    }
    catch (Exception e)
    {
      return null;
    }
  }

  protected ServerInterface GetRoleMember(String role, int id)
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

  private String GetRole()
  {
    return getClass().getName();
  }

  private void RegisterRMI() throws Exception
  {
    Registry registry = LocateRegistry.getRegistry(rmiAddress);
    for (int i = 1; i <= MAX_SERVER; i++)
    {
      try
      {
        // if it is bound, then try the next number
        registry.lookup(GetRole() + i);
      }
      catch (NotBoundException e)
      {
        // take the available id
        Id = i;
        registry.rebind(GetServerName(), this);
        LogWithScreen("Registered " + GetServerName());
        return;
      }
    }
    throw new Exception("RMI registry " + GetRole() + " fulled");
  }

  private void UnregisterRMI()
  {
    try
    {
      Registry registry = LocateRegistry.getRegistry(rmiAddress);
      registry.unbind(GetServerName());
    }
    catch (Exception e)
    {
      LogWithScreen("Unable to unbind " + GetServerName() + ". Exception:" + e.getMessage());
    }
  }
}

