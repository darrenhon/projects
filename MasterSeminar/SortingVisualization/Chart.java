import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JComponent;
import java.lang.Thread;

public class Chart
{
  private static ChartPanel s_panel;
  private static JFrame s_frame;
  private static int s_delay = 20;

  public static void Done()
  {
    SetHighlight(-1, -1);
    s_panel.fill = Color.blue;
    s_panel.repaint();
  }

  public static void SetStepDelay(int delay)
  {
    s_delay = delay;
  }

  public static void SetChartTitle(String title)
  {
    s_panel.title = title;
  }

  public static void SetChartValues(int[] values)
  {
    s_panel.values = values;
    Repaint();
  }

  public static void SetHighlight(int hl1, int hl2)
  {
    s_panel.hl1 = hl1;
    s_panel.hl2 = hl2;
  }

  public static void Repaint()
  {
    try
    {
      Thread.sleep(s_delay);
    }
    catch (InterruptedException e)
    {
    }
    s_panel.repaint();
  }

  public static void CreateChartWindow() 
  {
    s_frame = new JFrame();
    s_frame.setSize(1200, 800);

    s_panel = new ChartPanel();
    s_frame.getContentPane().add(s_panel);

    WindowListener wndCloser = new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    };
    s_frame.addWindowListener(wndCloser);
    s_frame.setVisible(true);
  }
}
