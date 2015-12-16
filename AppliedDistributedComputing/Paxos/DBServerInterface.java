import java.rmi.*;

// allows clients to Get values from DBServer without going through Proposer
public interface DBServerInterface extends Remote
{ 
  public String Get(String key) throws RemoteException;
}
