import java.rmi.*;
import java.util.*;

public class Proposer extends ServerBase implements ProposerInterface
{
  // the number of acceptors that are supposed to be online
  private int totalAcceptors = 0;
  private int leaderId;
  private int proposalId = 0;
  private HashMap<String, Proposal> proposals = new HashMap<String, Proposal>();
  public Proposer(String rmi, Logger logger) throws Exception
  {
    super(rmi, logger);
    // the first proposer is the leader
    leaderId = Collections.min(FindRoleMembers("Proposer"));
  }

  public int GetNewProposalId() throws RemoteException
  {
    return ++proposalId;
  }

  protected void Process(Task task) throws Exception
  {
    switch (task.Type)
    {
      case Request:
        Request(task);
        break;
      case Promise:
        Promise(task);
        break;
      case Accepted:
        Accepted(task);
        break;
      default:
        LogWithScreen("Unknown task.");
        break;
    }
  }

  // Called by clients for requesting write operations
  private void Request(Task task) throws Exception
  {
    // Find the leader
    ProposerInterface leader = (ProposerInterface)GetRoleMember("Proposer", leaderId);
    if (leader == null)
    {
      // leader down, forget it
      return;
    }

    // get proposal id from leader
    try
    {
      proposalId = leader.GetNewProposalId();
    }
    catch (RemoteException e)
    {
      logger.LogWithScreen("Unable to get proposal id");
      return;
    }
    // create and store the proposal
    Proposal proposal = new Proposal();
    proposal.ProposalId = proposalId;
    proposal.ClientId = task.ClientId;
    proposal.OpCode = task.OpCode;
    proposal.Key = task.Key;
    proposal.Value = task.Value;
    proposals.put(task.Key, proposal);

    Task prepare = new Task();
    prepare.Type = TaskType.Prepare;
    prepare.ProposalId = proposalId;
    prepare.ProposerId = Id;
    prepare.ClientId = task.ClientId;
    prepare.OpCode = task.OpCode;
    prepare.Key = task.Key;
    prepare.Value = task.Value;

    // store the number of acceptors
    ArrayList<Integer> acceptors = FindRoleMembers("Acceptor");
    totalAcceptors = acceptors.size();
    // send Prepare to all acceptors
    for (int id : acceptors)
    {
      ServerInterface acceptor = GetRoleMember("Acceptor", id);
      if (acceptor != null)
      {
        acceptor.AddTask(prepare);
      }
    }
  }

  private void Accepted(Task task) throws Exception
  {
    Proposal proposal = proposals.get(task.Key);
    if (proposal == null)
    {
      LogWithScreen("Proposal key not found in accepted");
      // I didn't submit proposal for this key!
      return;
    }
    // job done. remove it
    proposals.remove(task.Key);
  }

  private void Promise(Task task) throws Exception
  {
    Proposal proposal = proposals.get(task.Key);
    if (proposal == null)
    {
      LogWithScreen("Proposal key not found in promise");
      // I didn't submit proposal for this key!
      return;
    }

    if (task.ProposalId > proposal.ProposalId)
    {
      LogWithScreen("Newer ProposalId is promised, replaceing the old one");
      // someone has submitted a newer proposal, accept the newer one
      proposal.ProposalId = task.ProposalId;
      proposal.ClientId = task.ClientId;
      proposal.OpCode = task.OpCode;
      proposal.Value = task.Value;
    }

    if (++proposal.Promises > totalAcceptors / 2)
    {
      LogWithScreen("Majority promised");
      // majority is achieved. Accept it
      Task accept = new Task();
      accept.Type = TaskType.Accept;
      accept.ProposalId = proposal.ProposalId;
      accept.ProposerId = Id;
      accept.ClientId = proposal.ClientId;
      accept.OpCode = proposal.OpCode;
      accept.Key = proposal.Key;
      accept.Value = proposal.Value;
      // send Accept to all acceptors
      for (int id : FindRoleMembers("Acceptor"))
      {
        ServerInterface acceptor = GetRoleMember("Acceptor", id);
        if (acceptor != null)
        {
          acceptor.AddTask(accept);
        }
      }
      // after accepted, remove from the list to avoid duplicate accept
      proposals.remove(task.Key);
    }
  }
}
