import java.util.*;
public class Acceptor extends ServerBase
{
  // stores the highest accepted proposal for each key. This is suggested by Paxos Made Simple
  private HashMap<String, Proposal> accepted = new HashMap<String, Proposal>();

  // stores the highest promised proposalId for each key. This is suggested by Paxos Made Simple
  private HashMap<String, Integer> prepared = new HashMap<String, Integer>();

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
    if (prepared.containsKey(task.Key) && prepared.get(task.Key) > task.ProposalId)
    {
      // already promised a newer request. ignore this one
      LogWithScreen("Already promised a newer request. Ignore this one.");
      return;
    }

    Proposal proposal = accepted.get(task.Key);
    if (proposal != null)
    {
      // promise the highest number proposal ever accepted
      task.ProposalId = proposal.ProposalId;
      task.OpCode = proposal.OpCode;
      task.Key = proposal.Key;
      task.Value = proposal.Value;
    }

    task.Type = TaskType.Promise;
    ServerInterface proposer = GetRoleMember("Proposer", task.ProposerId);
    if (proposer != null)
    {
      proposer.AddTask(task);
    }
  }

  private void Accept(Task task) throws Exception
  {
    if (prepared.containsKey(task.Key) && prepared.get(task.Key) > task.ProposalId)
    {
      // already promised a newer request. ignore this one
      LogWithScreen("Already promised a newer request. Ignore this one.");
      return;
    }

    Proposal proposal = accepted.get(task.Key);
    if (proposal == null || proposal.ProposalId <= task.ProposalId)
    {
      // it's a newer proposal. save to highest accepted proposal
      proposal = new Proposal();
      proposal.ProposalId = task.ProposalId;
      proposal.ClientId = task.ClientId;
      proposal.OpCode = task.OpCode;
      proposal.Key = task.Key;
      proposal.Value = task.Value;
      accepted.put(task.Key, proposal);
    }

    // send Accepted to proposer and announce to one learner
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
  }
}
