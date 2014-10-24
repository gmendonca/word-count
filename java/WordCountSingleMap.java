import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.*;
import java.util.regex.*;
import java.io.*;

public class WordCountSingleMap {
    private static Map<String, Integer> sortByComparator(Map<String, Integer> unsortMap) {

    List<Map.Entry<String, Integer>> list = 
      new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());

    Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
      public int compare(Map.Entry<String, Integer> o1,
                                           Map.Entry<String, Integer> o2) {
        return (o2.getValue()).compareTo(o1.getValue());
      }
    });

    Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
    for (Iterator<Map.Entry<String, Integer>> it = list.iterator(); it.hasNext();) {
      Map.Entry<String, Integer> entry = it.next();
      sortedMap.put(entry.getKey(), entry.getValue());
    }
    return sortedMap;
  }

  public static void WriteToFile(Map<String, Integer> wordCounts){
    try {
 
      File file = new File("output/wordcount-java.txt");
 
      if (!file.exists()) file.createNewFile();
 
      FileWriter fw = new FileWriter(file.getAbsoluteFile());
      BufferedWriter bw = new BufferedWriter(fw);
      for(String word : sortByComparator(wordCounts).keySet()){
            bw.write(new String(word + ": " + wordCounts.get(word) + "\n"));
        }
      bw.close();
 
      System.out.println("Done");
 
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }

  }

  public static long splitFile(File f) throws IOException {
        FileInputStream fi = new FileInputStream(f);
        FileOutputStream out;
        int partCounter = 0;
        int sizeOfFiles = 64 * 1024 * 1024;// 1MB
        long filesize = f.length();
        byte[] buffer = new byte[sizeOfFiles];
        int tmp = 0;
        while ((tmp = fi.read(buffer)) > 0) {
            File newFile=new File("input/chuck-10Gb-"+partCounter);
            newFile.createNewFile();
            out = new FileOutputStream(newFile);
            out.write(buffer,0,tmp);
            out.flush();
            out.close();
            partCounter++;
            System.out.println("input/chuck-10Gb-"+partCounter);
        }

        return partCounter;
    }

  public static void main(String[] args) throws IOException {

      String inputFileName = args[0];
      //int chunks = Runtime.getRuntime().availableProcessors();
      int numThreads = Integer.parseInt(args[1]);
      //long[] offsets = new long[threads];
      File file = new File(inputFileName);

      long numFiles = splitFile(file);

      ArrayList<FileProcessor> threads = new ArrayList<FileProcessor>();

      Map<String, Integer> wordCounts = new TreeMap<String, Integer>();
      int n=0;
      while(numFiles > 0){
        System.out.println("File " + numFiles + "...");
        for(int i = 0; i < numThreads; i++){
          FileProcessor cw = new FileProcessor(wordCounts, i, n);
          threads.add(cw);
          cw.start();
          n++;
          numFiles--;
        }

        for ( FileProcessor t: threads ) {
          try{
            t.join();
            //wordCounts = t.getWordCountMap();
          } catch (Exception ex){}
        }
      }

      WriteToFile(wordCounts);
      System.out.println("allright");
  }
}

class FileProcessor extends Thread
{
  private Map<String, Integer> wordCounts;
  private int id;
  private long numFile; 

  public FileProcessor(Map<String, Integer> wordCounts, int id, long numFile){
    this.wordCounts = wordCounts;
    this.id = id;
    this.numFile = numFile;
  }

    public Map<String, Integer> getWordCountMap(){
      return wordCounts;
    }

  public void run(){
    int count;
    System.out.println("Thread " + id + " running!");

    String word;

    Pattern pattern = Pattern.compile("[\\s]+");

    String path = "input/chuck-10Gb-"+numFile;

    try
    {
      Scanner in = new Scanner(new File(path));

      while (in.hasNext()){
        in.useDelimiter(pattern);
        word = in.next().replaceFirst("[^a-zA-Z0-9]*", "");
        word = new StringBuilder(word).reverse().toString().replaceFirst("[^a-zA-Z0-9]*", "");
        word = new StringBuilder(word).reverse().toString();
        if(word.length() == 0) continue;
        if(wordCounts.containsKey(word)) count = wordCounts.get(word) + 1;
        else count = 1;
        wordCounts.put(word, count);
      }
    }
    catch (FileNotFoundException e)
    {
      System.out.println(path + " not found!");
      e.printStackTrace();
      return;
    }
}
}