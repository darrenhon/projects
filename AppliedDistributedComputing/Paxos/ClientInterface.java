import java.rmi.*;
// Client interface for Learner to call to announce results
public interface ClientInterface extends Remote
{
  public void Announce(Task task) throws RemoteException;
}
