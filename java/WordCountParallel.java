import java.util.StringTokenizer;
import java.util.Map;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileReader;

/** This is the parallel version of WordCount.java by way of
 * WordCountParallelBad.java.  The key change is that we use a
 * concurrent hashmap to allow parallel accesses, rather than a
 * synchronized HashMap.  Notice the updateCount() function needs be
 * concerned with maintaining the count correctly.
 */

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WordCountParallel implements Runnable {
    private final String buffer;
    private final ConcurrentMap<String,Integer> counts;

    public WordCountParallel(String buffer, 
                             ConcurrentMap<String,Integer> counts) {
        this.counts = counts;
        this.buffer = buffer;
    }

    private final static String DELIMS = " :;,.{}()\t\n";
    private final static boolean printAll = false;

    /**
     * Looks for the last delimiter in the string, and returns its
     * index.
     */
    private static int findDelim(String buf) {
        for (int i = buf.length() - 1; i>=0; i--) {
            for (int j = 0; j < DELIMS.length(); j++) {
                char d = DELIMS.charAt(j);
                if (d == buf.charAt(i)) return i;
            }
        }
        return 0;
    }

    /** 
     * Reads in a chunk of the file into a string.  
     */
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

    /**
     * Updates the count for each number of words.  Uses optimistic
     * techniques to make sure count is updated properly.
     */
    private void updateCount(String q) {
        Integer oldVal, newVal;
        Integer cnt = counts.get(q);
        // first case: there was nothing in the table yet
        if (cnt == null) {
            // attempt to put 1 in the table.  If the old
            // value was null, then we are OK.  If not, then
            // some other thread put a value into the table
            // instead, so we fall through
            oldVal = counts.put(q, 1);
            if (oldVal == null) return;
        }
        // general case: there was something in the table
        // already, so we have increment that old value
        // and attempt to put the result in the table.
        // To make sure that we do this atomically,
        // we use concurrenthashmap's replace() method
        // that takes both the old and new value, and will
        // only replace the value if the old one currently
        // there is the same as the one passed in.
        // Cf. http://www.javamex.com/tutorials/synchronization_concurrency_8_hashmap2.shtml 
        do {
            oldVal = counts.get(q);
            newVal = (oldVal == null) ? 1 : (oldVal + 1);
        } while (!counts.replace(q, oldVal, newVal));
    } 

    /**
     * Main task : tokenizes the given buffer and counts words. 
     */
    public void run() {
        StringTokenizer st = new StringTokenizer(buffer,DELIMS);
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            //System.out.println("updating count for "+token);
            updateCount(token);
        }
    } 

    public static void main(String args[]) throws java.io.IOException {
        long startTime = System.currentTimeMillis();
        if (args.length < 1) {
            System.out.println("Usage: <file to count> [#threads] [chunksize]");
            System.exit(1);
        }
        int numThreads = 4;
        int chunksize = 1000;
        if (args.length >= 2)
            numThreads = Integer.valueOf(args[1]);
        if (args.length >= 3)
            chunksize = Integer.valueOf(args[2]);
        ExecutorService pool = Executors.newFixedThreadPool(numThreads);
        BufferedReader reader = new BufferedReader(new FileReader(args[0]));
        ConcurrentMap<String,Integer> m = 
            new ConcurrentHashMap<String,Integer>();
        String leftover = ""; // in case a string broken in half
        while (true) {
            String res = readFileAsString(reader,chunksize);
            if (res.equals("")) {
                if (!leftover.equals("")) 
                    new WordCountParallel(leftover,m).run();
                break;
            }
            int idx = findDelim(res);
            String taskstr = leftover + res.substring(0,idx);
            leftover = res.substring(idx,res.length());
            pool.submit(new WordCountParallel(taskstr,m));
        }
        pool.shutdown();
        try {
            pool.awaitTermination(1,TimeUnit.DAYS);
        } catch (InterruptedException e) {
            System.out.println("Pool interrupted!");
            System.exit(1);
        }
        long endTime = System.currentTimeMillis();
        long elapsed = endTime - startTime;
        int total = 0;
        for (Map.Entry<String,Integer> entry : m.entrySet()) {
            int count = entry.getValue();
            if (printAll)
                System.out.format("%-30s %d\n",entry.getKey(),count);
            total += count;
        }
        System.out.println("Total words = "+total);
        System.out.println("Total time = "+elapsed+" ms");
    }
}
