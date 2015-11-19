import java.util.*;
import java.io.*;

public class Transaction implements Serializable
{
  public Opcode Opcode;
  public String Key;
  public String Value;
  public UUID Id;
  public Status Status;

  public Transaction(UUID id, Opcode opcode)
  {
    Id = id;
    Opcode = opcode;
    Status = Status.Preparing;
  }

  public Transaction(UUID id, Opcode opcode, String key)
  {
    this(id, opcode);
    Key = key;
  }

  public Transaction(UUID id, Opcode opcode, String key, String value)
  {
    this(id, opcode, key);
    Value = value;
  }

  private void writeObject(java.io.ObjectOutputStream out) throws IOException
  {
    out.writeObject(Opcode);
    out.writeObject(Key);
    out.writeObject(Value);
    out.writeObject(Id);
    out.writeObject(Status);
  }

  private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
  {
    Opcode = (Opcode) in.readObject();
    Key = (String) in.readObject();
    Value = (String) in.readObject();
    Id = (UUID) in.readObject();
    Status = (Status) in.readObject();
  }

  private void readObjectNoData() throws ObjectStreamException
  {
    Key = null;
    Value = null;
    Id = null;
    Status = Status.Unknown;
  }

  public String toString()
  {
    String msg = Id.toString() + ":" + Opcode.toString() + "(";
    if (Key != null)
    {
      msg += Key + (Value == null ? "" : "," + Value);
    }
    msg += ") Status:" + Status;
    return msg;
  }
}
