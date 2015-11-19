import java.io.*;
import java.util.*;
import java.util.regex.*;

public class HashMapParser
{
  // use a hash map to store key-value pairs
  private HashMap<String, String> m_map = new HashMap<String, String>();

  // Parse input from TCP/UDP client
  // check if the input is well-formed
  // execute the get/put/delete command
  // the command should be in format of "OPERATOR(KEY[,VALUE])"
  // OPERATOR can be get/put/delete, case insensitive
  // get and delete has only 1 parameter that is the KEY
  // put has 2 parameters, both KEY and VALUE
  // KEY and VALUE are case and space sensitive
  public synchronized String Parse(String input) throws Exception
  {
    boolean malformed = false;
    try
    {
      // extract OPERATOR, KEY, and VALUE from the input
      // if the regular expression fails to run, an exception
      // is thrown that is caught below and return with an error
      String[] results = input.split("[(,)]");

      // if it is a well-formed get command
      if (results.length == 2 && results[0].equalsIgnoreCase("get"))
      {
        if (m_map.containsKey(results[1]))
        {
          // if the key exists, return the value
          return "Got:" + m_map.get(results[1]);
        }
        else
        {
          // if the key doesn't exists, return error
          return "Error: key " + results[1] + " does not exist";
        }
      }
      // if it is a well-formed delete command
      else if (results.length == 2 && results[0].equalsIgnoreCase("delete"))
      {
        if (m_map.containsKey(results[1]))
        {
          // if the key exists, remove it and return a response
          m_map.remove(results[1]);
          return "Deleted key:" + results[1];
        }
        else
        {
          // if it doesn't exist, return error
          return "Error: key " + results[1] + " does not exist";
        }
      }
      // if it is a well-formed put command
      else if (results.length == 3 && results[0].equalsIgnoreCase("put"))
      {
        if (!m_map.containsKey(results[1]))
        {
          // if the key does not exists, store the key value pair
          m_map.put(results[1], results[2]);
          return "Put key:" + results[1];
        }
        else
        {
          // if the key exists, replace the current value
          m_map.put(results[1], results[2]);
          return "Put key:" + results[1] + ". Replaced original value.";
        }
      }
      // all other cases are malformed data. throw exception below
    }
    catch (PatternSyntaxException e)
    {
    }

    throw new Exception("Malformed input");
  }
}
