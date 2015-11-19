import java.rmi.*;
import java.util.*;

public interface ServerClientInterface extends Remote 
{ 
  public String Put(String key, String value) throws RemoteException;
  public String Get(String key) throws RemoteException;
  public String Delete(String key) throws RemoteException;
  public void Clear() throws RemoteException;
}
