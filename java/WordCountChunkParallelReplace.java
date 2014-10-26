import java.util.*;
import java.io.*;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WordCountChunkParallelReplace implements Runnable {
    private final String buffer;
    private final ConcurrentMap<String,Integer> counts;

    public WordCountChunkParallelReplace(String buffer, 
                             ConcurrentMap<String,Integer> counts) {
        this.counts = counts;
        this.buffer = buffer;
    }

    private final static String DELIMS = " \n";

    private static int findDelim(String buf) {
        for (int i = buf.length() - 1; i>=0; i--) {
            for (int j = 0; j < DELIMS.length(); j++) {
                char d = DELIMS.charAt(j);
                if (d == buf.charAt(i)) return i;
            }
        }
        return 0;
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

    private void updateCount(String q) {
        q = q.replaceFirst("[^a-zA-Z0-9]*", "");
        q = q.replaceAll("[^a-zA-Z0-9]*$", "");
        if(q.length() == 0) return;
        Integer oldVal, newVal;
        Integer cnt = counts.get(q);

        if (cnt == null) {

            oldVal = counts.put(q, 1);
            if (oldVal == null) return;
        }

        do {
            oldVal = counts.get(q);
            newVal = (oldVal == null) ? 1 : (oldVal + 1);
        } while (!counts.replace(q, oldVal, newVal));
    } 

    public void run() {
        StringTokenizer st = new StringTokenizer(buffer,DELIMS);
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            updateCount(token);
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

    public static void main(String args[]) throws java.io.IOException {
        long startTime = System.currentTimeMillis();
        if (args.length < 1) {
            System.out.println("Usage: <file to count> [#threads] [chunksize]");
            System.exit(1);
        }
        int numThreads = 4;
        int chunksize = 1024;
        if (args.length >= 2)
            numThreads = Integer.valueOf(args[1]);
        // if (args.length >= 3)
        //     chunksize = Integer.valueOf(args[2]);

        ConcurrentMap<String,Integer> wordCounts = new ConcurrentHashMap<String,Integer>();

        //long numFiles = splitFile(new File(args[0]));
        long numFiles = 155;

        ConcurrentMap<String,Integer> m = new ConcurrentHashMap<String,Integer>();

        for(long i = 0; i < numFiles; i++){
            System.out.println("File " + i);
            ExecutorService pool = Executors.newFixedThreadPool(numThreads);
            BufferedReader reader = new BufferedReader(new FileReader("input/chuck-10Gb-"+i));
            String leftover = ""; // in case a string broken in half
            while (true) {
                String res = readFileAsString(reader,chunksize);
                if (res.equals("")) {
                    if (!leftover.equals("")) 
                        new WordCountChunkParallelReplace(leftover,m).run();
                    break;
                }
                int idx = findDelim(res);
                String taskstr = leftover + res.substring(0,idx);
                leftover = res.substring(idx,res.length());
                pool.submit(new WordCountChunkParallelReplace(taskstr,m));
            }
            pool.shutdown();
            try {
            pool.awaitTermination(1,TimeUnit.DAYS);
            } catch (InterruptedException e) {
                System.out.println("Pool interrupted!");
                System.exit(1);
            }
        }

        WriteToFile(m);
    }
}
