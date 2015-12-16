import java.util.*;

public class Proposal
{
  public int ProposalId;
  public OpCode OpCode;
  public int ClientId;
  public String Key;
  public String Value;
  public int Promises;
  public String toString()
  {
    return "ProposalId:" + ProposalId + ". OpCode:" + OpCode + ". ClientId:" + ClientId + ". Key:" + Key + ". Value:" + Value;
  }
}
