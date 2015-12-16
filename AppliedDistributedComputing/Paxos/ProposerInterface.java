import java.rmi.*;
// Propose leader distribute proposal id in a centralized way
public interface ProposerInterface extends Remote
{
  public int GetNewProposalId() throws RemoteException;
}
