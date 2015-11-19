import java.rmi.*;
import java.util.*;

public interface ServerServerInterface extends Remote 
{ 
  // methods for maintaining list of active servers
  public void AddServer(String server) throws RemoteException;
  public void RemoveServer(String server) throws RemoteException;

  // methods for 2-phase commit
  public boolean PrepareToCommit(Transaction tran) throws RemoteException; 
  public String DoCommit(Transaction tran) throws RemoteException;  
  public void DoAbort(Transaction tran) throws RemoteException;
  public Status GetStatus(Transaction tran) throws RemoteException;
}
