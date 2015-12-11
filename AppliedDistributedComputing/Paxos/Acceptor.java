import java.util.*;
public class Acceptor extends ServerBase
{
  private HashMap<String, Proposal> proposals = new HashMap<String, Proposal>();
  public Acceptor(String rmi, Logger logger) throws Exception
  {
    super(rmi, logger);
  }

  protected void Process(Task task) throws Exception
  {
    switch (task.Type)
    {
      case Prepare:
        Prepare(task);
        break;
      case Accept:
        Accept(task);
        break;
      default:
        LogWithScreen("Unknown task.");
        break;
    }
  }

  private void Prepare(Task task) throws Exception
  {
    Proposal proposal = proposals.get(task.Key);
    if (proposal == null || task.ProposalId > proposal.ProposalId)
    {
      // new proposal. promise it
      proposal = new Proposal();
      proposal.ProposalId = task.ProposalId;
      proposal.ClientId = task.ClientId;
      proposal.OpCode = task.OpCode;
      proposal.Key = task.Key;
      proposal.Value = task.Value;
      proposals.put(task.Key, proposal);

      task.Type = TaskType.Promise;
      ServerInterface proposer = GetRoleMember("Proposer", task.ProposerId);
      if (proposer != null)
      {
        proposer.AddTask(task);
      }
    }
    // ignore old proposals
  }

  private void Accept(Task task) throws Exception
  {
    Proposal proposal = proposals.get(task.Key);
    if (proposal == null)
    {
      // I did not promise this proposal
      LogWithScreen("Accept proposal not found" + proposal.toString());
      return;
    }
    if (task.ProposalId >= proposal.ProposalId)
    {
      // current or newer proposal, and majority has promised. ok I'll accept it
      task.Type = TaskType.Accepted;
      ServerInterface proposer = GetRoleMember("Proposer", task.ProposerId);
      if (proposer != null)
      {
        proposer.AddTask(task);
      }
      for (int id : FindRoleMembers("Learner"))
      {
        task.Type = TaskType.Announce;
        ServerInterface learner = GetRoleMember("Learner", id);
        if (learner != null)
        {
          // one learner announce is enough
          learner.AddTask(task);
          break;
        }
      }
      proposals.remove(task.Key);
    }
    // ignore old proposals
  }
}
