import java.io.*;
import java.util.*;
import java.text.*;

public class Logger
{
  private String m_filename;

  // initialize the logger with a filename
  public Logger(String filename)
  {
    m_filename = filename;
  }

  // write the log to file and print to screen at the same time
  public void LogWithScreen(String data)
  {
    System.out.println(data);
    Log(data);
  }

  // write the log to file
  public synchronized void Log(String data)
  {
    try
    {
      // open the file, write the log with a timestamp, and close the file
      PrintWriter out = new PrintWriter(new FileWriter(m_filename, true));
      DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
      Date date = new Date();
      out.println(dateFormat.format(date) + " - " + data);
      out.close();
    }
    catch (IOException e)
    {
      // if it fails to open or write the log, an IOException is thrown
      // display the exception on screen
      System.out.println("Unable to write log to " + m_filename + ". IOException:" + e.getMessage());
    }
  }
}
