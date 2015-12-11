import java.rmi.*;
public interface ProposerInterface extends Remote
{
  public int GetNewProposalId() throws RemoteException;
}
