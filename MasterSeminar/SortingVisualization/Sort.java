import java.lang.management.*;
import java.io.*;
import java.util.*;
import java.lang.*;

public class Sort
{
  private static MemoryMXBean s_bean = null;
  private static int s_iterations = 0;
  private static long s_maxMemUsage = 0;

  public static void main(String[] args)
  {
    // check inputs
    if (args.length < 2)
    {
      System.out.println("Usage: Sort Algorithm Input-file");
      System.out.println("Algorithm can be insertion, selection, bubble, merge and quick.");
      System.out.println("");
      return;
    }

    // read the input file into int array
    int[] input = null;
    int count = 0;
    try
    {
      BufferedReader br = new BufferedReader(new FileReader(args[1]));
      Vector<Integer> numbers = new Vector<Integer>();
      String line = br.readLine();
      while (line != null)
      {
        if (!line.isEmpty())
          numbers.add(Integer.parseInt(line));
        line = br.readLine();
      }
      System.out.println("Data size is " + numbers.size());
      input = new int[numbers.size()];
      for (int i = 0; i < numbers.size(); i++)
      {
        input[i] = numbers.get(i);
      }
      br.close();
    }
    catch (Exception e)
    {
      System.out.println("Error reading file line " + count + " :" + e.getMessage());
      return;
    }

    // initialize s_bean for memory usage measure
    s_bean = ManagementFactory.getMemoryMXBean();

    // calculate sortedness
    System.out.println("Sortedness is " + GetSortedness(input));

    s_maxMemUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
    System.out.println("Memory used before sorting " + s_maxMemUsage / 1000 + " KB");

    Chart.CreateChartWindow();
    Chart.SetChartValues(input);

    // start sorting
    int[] output = null;
    if (args[0].equalsIgnoreCase("insertion"))
    {
      Chart.SetStepDelay(10);
      output = InsertionSort(input);
    }
    else if (args[0].equalsIgnoreCase("selection"))
    {
      output = SelectionSort(input);
    }
    else if (args[0].equalsIgnoreCase("bubble"))
    {
      Chart.SetStepDelay(10);
      output = BubbleSort(input);
    }
    else if (args[0].equalsIgnoreCase("merge"))
    {
      output = MergeSort(input, 0, input.length);
    }
    else if (args[0].equalsIgnoreCase("quick"))
    {
      Chart.SetStepDelay(100);
      output = QuickSort(input, 0, input.length - 1);
    }
    else
    {
      System.out.println("Invalid algorithm");
      return;
    }
    Chart.Done();
  }

  static int[] InsertionSort(int[] input)
  {
    Chart.SetChartTitle("Insertion Sort");
    // pick each number and compare to the left of it
    for (int i = 1; i < input.length; i++)
    {
      int tmp = input[i];
      int j;
      // move the sorted array to the right 
      // until input[i] is just greater than the left one
      for (j = i - 1; j >=0 && tmp < input[j]; j--)
      {
        input[j + 1] = input[j];
        Chart.SetHighlight(j, j + 1);
        Chart.Repaint();
      }

      // put the number in place
      input[j + 1] = tmp;
      Chart.SetHighlight(i, j + 1);
      Chart.Repaint();
    }
    s_maxMemUsage = Math.max(s_maxMemUsage, ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed());
    return input;
  }

  static int[] SelectionSort(int[] input)
  {
    Chart.SetChartTitle("Selection Sort");
    // iterate from begin to end of input
    for (int i = 0; i < input.length - 1; i++)
    {
      int minIndex = i;
      // find the minimum of the rest of the array
      for (int j = i + 1; j < input.length; j++)
          if (input[j] < input[minIndex]) 
          {
              Chart.SetHighlight(j, minIndex);
              Chart.Repaint();
              minIndex = j;
          }

      // swap the current position with the minimum
      if (minIndex != i)
      {
        int min = input[minIndex];  
        input[minIndex] = input[i];
        input[i] = min;
        Chart.SetHighlight(i, minIndex);
        Chart.Repaint();
      }
    }
    s_maxMemUsage = Math.max(s_maxMemUsage, ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed());
    return input;
  }

  static int[] BubbleSort(int[] input)
  {
    Chart.SetChartTitle("Bubble Sort");
    boolean done = false;
    int temp;

    // continuously look for unsorted pair
    while (!done)
    {
      done = true;
      // check every pair from begin to end
      for(int i=0; i < input.length - 1; i++)
      {
        // if the next one is bigger, swap with it and mark not done
        if (input[i] > input[i+1])
        {
          temp = input[i];
          input[i] = input[i+1];
          input[i+1] = temp;
          Chart.SetHighlight(i, i + 1);
          Chart.Repaint();
          done = false;
        }
      }
    }
    s_maxMemUsage = Math.max(s_maxMemUsage, ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed());
    return input;
  }

  static int[] MergeSort(int[] input, int begin, int size)
  {
    Chart.SetChartTitle("Merge Sort");
    // if the input size is less then 2, it is already done
    if (size < 2)
      return input;
    
    // split the input into 2 halves
    int leftSize = size / 2;
    int rightSize = size - leftSize;
    int rightBegin = begin + leftSize;

    MergeSort(input, begin, leftSize);
    MergeSort(input, rightBegin, rightSize);

    // Sort each half recursively and then merge them
    Merge(input, begin, leftSize, rightSize);
    s_maxMemUsage = Math.max(s_maxMemUsage, ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed());
    return input;
  }

  static int[] QuickSort(int[] input, int beginIndex, int endIndex)
  {
    Chart.SetChartTitle("Quick Sort");
    int i = beginIndex;
    int j = endIndex;
    // use the middle value as a pivot
    int pivot = input[beginIndex + (endIndex - beginIndex) / 2];
    // each iteration i and j approach each other until i > j
    while (i <= j) {
      // skip the numbers from the left if less than pivot
      while (input[i] < pivot) {
        i++;
      }
      // skip the numbers from the right if greater than pivot
      while (input[j] > pivot) {
        j--;
      }
      // i stops at a number greater than or equal to pivot
      // j stops at a number smaller than or equal to pivot
      if (i <= j) {
        // exchange number and advance i and j until they cross each other
        Chart.SetHighlight(i, j);
        int tmp = input[i];
        input[i++] = input[j];
        input[j--] = tmp;
        Chart.Repaint();
      }
    }
    // pivot division is done
    // QuickSort the part that is smaller than pivot
    if (j > beginIndex)
        QuickSort(input, beginIndex, j);
    // QuickSort the part that is greater than pivot
    if (i < endIndex)
        QuickSort(input, i, endIndex);

    s_maxMemUsage = Math.max(s_maxMemUsage, ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed());
    return input;
  }

  static void Merge(int[] input, int begin, int leftSize, int rightSize) {
    int[] left = new int[leftSize];
    int[] right = new int[rightSize];
    System.arraycopy(input, begin, left, 0, leftSize);
    System.arraycopy(input, begin + leftSize, right, 0, rightSize);

    int leftCount = 0, rightCount = 0, outCount = begin;
    // for each number in the arrays,
    // compare them and put the smaller one to input
    while (leftCount < left.length && rightCount < right.length) {
      if (left[leftCount] <= right[rightCount]) {
        Chart.SetHighlight(outCount, -1);
        input[outCount++] = left[leftCount++];
        Chart.Repaint();
      } else {
        Chart.SetHighlight(outCount, -1);
        input[outCount++] = right[rightCount++];
        Chart.Repaint();
      }
    }
    // append the leftovers
    while (leftCount < left.length) {
      input[outCount++] = left[leftCount++];
      Chart.SetHighlight(outCount, -1);
      Chart.Repaint();
    }
    while (rightCount < right.length) {
      Chart.SetHighlight(outCount, -1);
      input[outCount++] = right[rightCount++];
      Chart.Repaint();
    }
  }

  // Sortedness = 1 - inversion score
  // Count inversions using merge sort
  static double GetSortedness(int[] input) {
    int[] copy = Arrays.copyOf(input, input.length);
    int n = copy.length;
    int[] current, workspace, tmp;
    workspace = Arrays.copyOf(input, n);
    current = Arrays.copyOf(input, n);
    double inversions = 0;
    // Step through blocks of array of exponentially increasing size.
    for (int blockSize = 1; blockSize < n; blockSize *= 2) {
      for (int left = 0; left < n; left += blockSize * 2) {
        int piv = left + blockSize;
        if (piv < n) {
          int right = piv + blockSize;
          if (right > n) right = n;
          inversions += mergeAndCountInversions(current, left, piv, right, workspace);
        }
      }
      // Workspace is now the partially sorted list, so swap the two.
      tmp = workspace;
      workspace = current;
      current = tmp;
    }
    for (int i = 0; i < n; ++i)
      copy[i] = current[i];

    // maximum number of inversion is n * (n-1) / 2
    double inversionScore = inversions / ((n - 1) * (n / 2.0));
    return 1 - inversionScore;
  }
   
  // Merge two blocks in the input array, place sorted result in output
  // array and return the inversion count.
  static int mergeAndCountInversions(int[] ar, int left, int piv, int right, int[] output)
  {
    int inversions = 0;
    int i = left, l = left, r = piv;
    for (; i < right; i++) {
      // Exhausted right, just fill with left.
      if (r == right) {
        output[i] = ar[l++];
      // Exhausted left, just fill with right.
      } else if (l == piv){
        output[i] = ar[r++];
      } else {
        if (ar[l] <= ar[r]){
          // Left less or equal.
          output[i] = ar[l++];
        } else {
          // Right less.
          // Add an inversion for each of the left elements remaining.
          inversions += piv - l;
          output[i] = ar[r++];
        }
      }
    }
    return inversions;
  }
}
