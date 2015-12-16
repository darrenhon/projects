import java.io.*;

// Asynchronous task
public class Task implements Serializable
{
  public TaskType Type;
  public OpCode OpCode;
  public int ClientId;
  public int ProposerId;
  public int ProposalId;
  public String Key;
  public String Value;
  public String toString()
  {
    return "Type:" + Type + ". OpCode:" + OpCode + ". ClientId:" + ClientId + ". ProposerId:" + ProposerId + ". ProposalId:" + ProposalId + ". Key:" + Key + ". Value:" + Value;
  }

  private void writeObject(java.io.ObjectOutputStream out) throws IOException
  {
    out.writeObject(Type);
    out.writeObject(OpCode);
    out.writeObject(ClientId);
    out.writeObject(ProposerId);
    out.writeObject(ProposalId);
    out.writeObject(Key);
    out.writeObject(Value);
  }

  private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
  {
    Type = (TaskType) in.readObject();
    OpCode = (OpCode) in.readObject();
    ClientId = (int) in.readObject();
    ProposerId = (int) in.readObject();
    ProposalId = (int) in.readObject();
    Key = (String) in.readObject();
    Value = (String) in.readObject();
  }

  private void readObjectNoData() throws ObjectStreamException
  {
    Key = null;
    Value = null;
    ClientId = 0;
    ProposalId = 0;
    ProposerId = 0;
  }

}
