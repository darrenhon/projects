import java.rmi.*;
public interface ServerInterface extends Remote
{
  public void AddTask(Task task) throws RemoteException;
}
