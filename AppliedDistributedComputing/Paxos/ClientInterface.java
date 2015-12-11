import java.rmi.*;
public interface ClientInterface extends Remote
{
  public void Announce(Task task) throws RemoteException;
}
