import java.util.*;
import java.io.*;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WordCountLastSplit implements Runnable {
    private final String buffer;
    private final Map<String,Integer> counts;

    public WordCountLastSplit(String buffer, Map<String,Integer> counts) {
        this.counts = counts;
        this.buffer = buffer;
    }

    private static String readFileAsString(BufferedReader reader, int size)
        throws java.io.IOException 
    {
        StringBuffer fileData = new StringBuffer(size);
        int numRead=0;

        while(size > 0) {
            int bufsz = 1024 > size ? size : 1024;
            char[] buf = new char[bufsz];
            numRead = reader.read(buf,0,bufsz);
            if (numRead == -1)
                break;
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            size -= numRead;
        }
        return fileData.toString();
    }

    public void run() {
        int count;
        String[] words = buffer.split("[^a-zA-Z0-9]*\\s+[^a-zA-Z0-9]*");
        for(String word : words){
          if(word.length() == 0) continue;
          if(counts.containsKey(word)) count = counts.get(word) + 1;
          else count = 1;
          counts.put(word, count);
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
            File newFile=new File("input/chunk-"+partCounter);
            newFile.createNewFile();
            out = new FileOutputStream(newFile);
            out.write(buffer,0,tmp);
            out.flush();
            out.close();
            partCounter++;
            System.out.println("input/chunk-"+partCounter);
        }

        return partCounter;
    }

    private static Map<String, Integer> sortByComparator(Map<String, Integer> unsortMap) {

        List<Map.Entry<String, Integer>> list = 
        new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());

        System.out.println("Works 1");

        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
          public int compare(Map.Entry<String, Integer> o1,
           Map.Entry<String, Integer> o2) {
            return (o2.getValue()).compareTo(o1.getValue());
        }
    });
        System.out.println("Works 2");

        Map.Entry<String, Integer> entry;
        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Iterator<Map.Entry<String, Integer>> it = list.iterator(); it.hasNext();) {
            entry = it.next();
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        System.out.println("Works 3");
        return sortedMap;
    }

    public static void WriteToFile(Map<String, Integer> wordCounts){
        try {
            File file = new File("output/wordcount-java.txt");

            if (!file.exists()) file.createNewFile();

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            String line;
            Map<String, Integer> sortedwordCounts;

            //sortedwordCounts = sortByComparator(wordCounts);

            for(String word : wordCounts.keySet()){
                line = word + ": " + wordCounts.get(word) + "\n";
                bw.write(line);
            }
            bw.close();

            System.out.println("Done");

        } catch (IOException e) {
          e.printStackTrace();
          return;
      }
    }

    public static void main(String args[]) throws java.io.IOException {
        int numThreads = 4;
        int chunksize = 1024;
        if (args.length >= 2) numThreads = Integer.valueOf(args[1]);

        int yes = 0;
        
        if (args.length >= 3) yes = Integer.valueOf(args[1]);

        long numFiles = 155;
        if(yes == 1) numFiles = splitFile(new File(args[0]));
        else numFiles = 155;

        //long startTime = System.currentTimeMillis();

        Map<String,Integer> m = new ConcurrentHashMap<String,Integer>();

        for(long i = 0; i < numFiles; i++){
            System.out.println("File " + i);
            ExecutorService pool = Executors.newFixedThreadPool(numThreads);
            BufferedReader reader = new BufferedReader(new FileReader("input/chunk-"+i));
            String leftover = ""; // in case a string broken in half
            while (true) {
                String res = readFileAsString(reader,chunksize);
                if (res.equals("")) break;
                pool.submit(new WordCountLastSplit(res,m));
            }
            pool.shutdown();
            try {
            pool.awaitTermination(1,TimeUnit.DAYS);
            } catch (InterruptedException e) {
                System.out.println("Pool interrupted!");
                System.exit(1);
            }
        }

        //long stopTime = System.currentTimeMillis();
        //System.out.println("Time = " + (stopTime - startTime));

        WriteToFile(m);
    }
}
