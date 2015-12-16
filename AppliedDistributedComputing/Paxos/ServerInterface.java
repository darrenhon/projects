import java.rmi.*;

// common interface for all server roles
public interface ServerInterface extends Remote
{
  // An asynchronous way to perform tasks on server
  public void AddTask(Task task) throws RemoteException;
}
