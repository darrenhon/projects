import java.util.*;

// Asynchronous task
public class Proposal
{
  public int ProposalId;
  public OpCode OpCode;
  public int ClientId;
  public String Key;
  public String Value;
  public int Promises;
  public int Accepts;
  public String toString()
  {
    return "ProposalId:" + ProposalId + ". OpCode:" + OpCode + ". ClientId:" + ClientId + ". Key:" + Key + ". Value:" + Value;
  }
}
