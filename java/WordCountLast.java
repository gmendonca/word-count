import java.util.*;
import java.io.*;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WordCountLast implements Runnable {
    private final String buffer;
    private final ConcurrentMap<String,Integer> counts;

    public WordCountLast(String buffer, ConcurrentMap<String,Integer> counts) {
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
          
          String line;

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

        ConcurrentMap<String,Integer> m = new ConcurrentHashMap<String,Integer>();

            ExecutorService pool = Executors.newFixedThreadPool(numThreads);
            BufferedReader reader = new BufferedReader(new FileReader(args[0]));
            String leftover = ""; // in case a string broken in half
            while (true) {
                String res = readFileAsString(reader,chunksize);
                if (res.equals("")) {
                    if (!leftover.equals("")) 
                        new WordCountLast(leftover,m).run();
                    break;
                }
                int idx = findDelim(res);
                String taskstr = leftover + res.substring(0,idx);
                leftover = res.substring(idx,res.length());
                pool.submit(new WordCountLast(taskstr,m));
            }
            pool.shutdown();
            try {
            pool.awaitTermination(1,TimeUnit.DAYS);
            } catch (InterruptedException e) {
                System.out.println("Pool interrupted!");
                System.exit(1);
            }

        WriteToFile(m);
    }
}
