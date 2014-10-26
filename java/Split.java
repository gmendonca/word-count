import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.*;
import java.util.regex.*;
import java.io.*;

public class Split {
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

    public static void main(String[] args) throws IOException {

        String inputFileName = args[0];
        int chunks = Integer.parseInt(args[1]);
        long[] offsets = new long[chunks];
        File file = new File(inputFileName);

        // determine line boundaries for number of chunks
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        for (int i = 1; i < chunks; i++) {
            raf.seek(i * file.length() / chunks);

            while (true) {
                int read = raf.read();
                if (read == '\n' || read == -1) {
                    break;
                }
            }

            offsets[i] = raf.getFilePointer();
        }
        raf.close();

        ArrayList<FileProcessor> threads = new ArrayList<FileProcessor>();

        Map<String, Integer> wordCounts = new TreeMap<String, Integer>();

        for(int i = 0; i < chunks; i++){
          long start = offsets[i];
          long end = i < chunks - 1 ? offsets[i + 1] : file.length();
          FileProcessor cw = new FileProcessor(wordCounts, i, file, start, end);
          threads.add(cw);
          cw.start();
        }

        for ( FileProcessor t: threads ) {
          try{
            t.join();
            wordCounts.putAll( t.getWordCountMap() );
          } catch (Exception ex){}
        }

        WriteToFile(wordCounts);
        System.out.println("allright");
    }
}

class FileProcessor extends Thread
{
  private Map<String, Integer> wordCounts;
  private int id;
  private final File file;
  private final long start;
  private final long end;

  public FileProcessor(Map<String, Integer> wordCounts, int id, File file, long start, long end){
    this.wordCounts = wordCounts;
    this.id = id;
    this.file = file;
    this.start = start;
    this.end = end;
  }

    public Map<String, Integer> getWordCountMap(){
      return wordCounts;
    }

  public void run(){
    int count;

    try {
      RandomAccessFile raf = new RandomAccessFile(file, "r");
      raf.seek(start);

      while (raf.getFilePointer() < end) {
        String line = raf.readLine();
        if (line == null) {
          continue;
        }
        //String[] words = line.split("\\s+");
        String[] words = line.split("[^a-zA-Z0-9]*\\s+[^a-zA-Z0-9]*");
        for(String word : words){
          //String word = w.replaceFirst("[^a-zA-Z0-9]*", "");
          //word = new StringBuilder(word).reverse().toString().replaceFirst("[^a-zA-Z0-9]*", "");
          //word = new StringBuilder(word).reverse().toString();
          if(word.length() == 0) continue;
          if(wordCounts.containsKey(word)) count = wordCounts.get(word) + 1;
          else count = 1;
          wordCounts.put(word, count);
        }
      }
      raf.close();
    } catch (IOException e) {
        e.printStackTrace();
    }
  }
}