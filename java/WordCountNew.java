import java.io.*;
import java.util.*; 

/* I am not doing strong error checking. Sorry... */

public class WordCountNew {
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

      ArrayList<ThreadCount> threads = new ArrayList<ThreadCount>();

      int n=0;
      while(numFiles > 0){
        System.out.println("File " + numFiles + "...");
        for(int i = 0; i < numThreads; i++){
          ThreadCount cw = new ThreadCount(i, n);
          threads.add(cw);
          cw.start();
          n++;
          numFiles--;
        }

        for ( ThreadCount t: threads ) {
          try{
            t.join();
            //wordCounts = t.getWordCountMap();
          } catch (Exception ex){}
        }
      }

      //WriteToFile(wordCounts);
      System.out.println("allright");
  }
}

class ThreadCount extends Thread
{
    private int id;
    private long numFile;
    private Map<String, Integer> _wordCount;

    public ThreadCount(int id, long numFile)
    {
        this.id = id;
        this.numFile = numFile;
        this._wordCount = new HashMap<String, Integer>();
    }
    
    public Map<String, Integer> getWordCount()
    {
        return this._wordCount;
    }

    @Override
    public void run()
    {
         try{
            BufferedReader br = new BufferedReader(new FileReader("input/chuck-10Gb-"+numFile));
            String line;
            while ((line = br.readLine()) != null) {
                for (String word: line.split("\\s+")){
                    // trim prefixing nonalphameric
                    word = word.replaceFirst("[^a-zA-Z0-9\\s]*", "");
                    word = new StringBuffer(word).reverse().toString();
                    word = word.replaceFirst("[^a-zA-Z0-9\\s]*", "");
                    word = new StringBuffer(word).reverse().toString();
                    //System.out.println(word);

                    Integer count = _wordCount.get(word);
                    _wordCount.put(word, count == null ? 1 : count + 1);
                }
            }
            br.close();

            } catch(IOException e) {
                e.printStackTrace();
            }

    }
}

