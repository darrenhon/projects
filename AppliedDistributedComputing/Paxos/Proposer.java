import java.rmi.*;
import java.util.*;

public class Proposer extends ServerBase implements ProposerInterface
{
  // a list of acceptors that are supposed to be online
  private ArrayList<Integer> acceptors;
  private int leaderId;
  private int proposalId = 0;
  private HashMap<String, Proposal> proposals = new HashMap<String, Proposal>();
  public Proposer(String rmi, Logger logger) throws Exception
  {
    super(rmi, logger);
    // find all online acceptors
    acceptors = FindRoleMembers("Acceptor");
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

  private void Request(Task task) throws Exception
  {
    // get proposal id from leader
    ProposerInterface leader = (ProposerInterface)GetRoleMember("Proposer", leaderId);
    if (leader == null)
    {
      // leader down, forget it
      return;
    }

    // replace existing proposal on this key
    try
    {
      proposalId = leader.GetNewProposalId();
    }
    catch (RemoteException e)
    {
      logger.LogWithScreen("Unable to get proposal id");
      return;
    }
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
    if (task.ProposalId < proposal.ProposalId)
    {
      LogWithScreen("Old ProposalId is accepted");
      // old proposal, trash it
      return;
    }
    if (task.ProposalId > proposal.ProposalId)
    {
      LogWithScreen("Newer ProposalId is accepted");
      // some has submitted a newer proposal, forget mine
      proposals.remove(task.Key);
      return;
    }
    LogWithScreen("My proposal is accepted");
    if (++proposal.Accepts > acceptors.size() / 2)
    {
      LogWithScreen("Majority accepted");
      // majority is achieved. job done. remove it
      proposals.remove(task.Key);
    }
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
    if (task.ProposalId < proposal.ProposalId)
    {
      LogWithScreen("Old ProposalId is promised");
      // old proposal, trash it
      return;
    }
    if (task.ProposalId > proposal.ProposalId)
    {
      LogWithScreen("Newer ProposalId is promised");
      // some has submitted a newer proposal, forget mine
      proposals.remove(task.Key);
      return;
    }
    LogWithScreen("My proposal is promised");
    if (++proposal.Promises > acceptors.size() / 2)
    {
      LogWithScreen("Majority promised");
      // majority is achieved. Accept it
      Task accept = new Task();
      accept.Type = TaskType.Accept;
      accept.ProposalId = task.ProposalId;
      accept.ProposerId = Id;
      accept.ClientId = task.ClientId;
      accept.OpCode = task.OpCode;
      accept.Key = task.Key;
      accept.Value = task.Value;
      for (int id : acceptors)
      {
        ServerInterface acceptor = GetRoleMember("Acceptor", id);
        if (acceptor != null)
        {
          acceptor.AddTask(accept);
        }
      }
    }
  }
}
