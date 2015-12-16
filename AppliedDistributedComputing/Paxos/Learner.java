import java.util.*;

public class Learner extends ServerBase
{
  // remember the proposals that were learned
  private ArrayList<Integer> learnedProposals = new ArrayList<Integer>();
  public Learner(String rmi, Logger logger) throws Exception
  {
    super(rmi, logger);
  }

  protected void Process(Task task) throws Exception
  {
    switch (task.Type)
    {
      case Announce:
        Announce(task);
        break;
      default:
        LogWithScreen("Unknown task.");
        break;
    }
  }

  private void Announce(Task task) throws Exception
  {
    if (learnedProposals.contains(task.ProposalId))
    {
      // already learned, avoid duplicate announce
      return;
    }
    learnedProposals.add(task.ProposalId);

    Task doit = new Task();
    doit.Type = TaskType.Do;
    doit.OpCode = task.OpCode;
    doit.Key = task.Key;
    doit.Value = task.Value;

    for (int id : FindRoleMembers("DBServer"))
    {
      ServerInterface server = GetRoleMember("DBServer", id);
      if (server != null)
      {
        server.AddTask(doit);
      }
    }
    ClientInterface client = GetClient(task.ClientId);
    if (client != null)
    {
      client.Announce(task);
    }
  }
}
