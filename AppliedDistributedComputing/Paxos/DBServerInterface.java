import java.rmi.*;

public interface DBServerInterface extends Remote
{ 
  public String Get(String key) throws RemoteException;
}
