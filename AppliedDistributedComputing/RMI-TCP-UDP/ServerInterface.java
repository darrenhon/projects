import java.rmi.*;

public interface ServerInterface extends Remote 
{ 
  public String Put(String key, String value) throws RemoteException;
  public String Get(String key) throws RemoteException;
  public String Delete(String key) throws RemoteException;
}
