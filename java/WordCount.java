import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.util.concurrent.*;

public class WordCount
{

    public static ArrayList<ArrayList<String>> splitFile(File f, int numThreads) throws FileNotFoundException{

    	int it = 0;
    	ArrayList<String> lines = new ArrayList<String>();

    	Scanner in = new Scanner(f);

    	while (in.hasNext()){
    		lines.add(in.nextLine());
    	}

    	ArrayList<ArrayList<String>> files = new ArrayList<ArrayList<String>>();

    	int linePerFile = lines.size()/numThreads;

    	for(int i = 0; i < numThreads; i++){
    		files.add(new ArrayList<String>());
    	}

    	for(int i = 0; i < lines.size(); i++){
    		files.get(it).add(lines.get(i));
    		if(it <= numThreads && i != 0 &&i == (i*linePerFile)){
    			it++;
    		}
    	}

    	return files;
    }

	public static void main(String[ ] args){

		String inputFileName = args[0];

		int numThreads = Integer.parseInt(args[1]);

		try
		{

			File f = new File(inputFileName);

			ArrayList<ArrayList<String>> files;

			files = splitFile(f, numThreads);

			ArrayList<CountWord> maps = new ArrayList<CountWord>();

			ExecutorService es = Executors.newCachedThreadPool();
			for(int i = 0; i < numThreads; i++){
				Map<String, Integer> wordCounts = new TreeMap<String, Integer>();
				CountWord cw = new CountWord(files.get(i), wordCounts, i);
				es.execute(cw);
				maps.add(cw);
			}
			es.shutdown();
			boolean finshed = es.awaitTermination(60, TimeUnit.MINUTES);

			Thread m = new Thread(new Merge(maps));
			m.start();

		}catch(Exception e)
		{
			System.out.println(inputFileName + " not found!");
			e.printStackTrace();
			return;
		}
	}
}

class CountWord implements Runnable
{
	private ArrayList<String> inputFileName;
	private Map<String, Integer> wordCounts;
	private int id;

	public CountWord(ArrayList<String> inputFileName, Map<String, Integer> wordCounts, int id){
		this.inputFileName = inputFileName;
		this.wordCounts = wordCounts;
		this.id = id;
   	}

   	public Map<String, Integer> getWordCountMap(){
   		return wordCounts;
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

   	public void WriteToFile(){
		try {
 
			File file = new File("output/wordcount-java-" + id + ".txt");
 
			if (!file.exists()) file.createNewFile();
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			for(String word : sortByComparator(wordCounts).keySet()){
        		bw.write(new String(word + ": " + wordCounts.get(word) + "\n"));
     		}
			bw.close();
 
			System.out.println("Done");
 
		} catch (IOException e) {
			System.out.println("coco");
			e.printStackTrace();
			return;
		}

	}

	public void run(){
		int count;
      	String word;

		for(String s : inputFileName){
			String[] words = s.split("\\s+");
			for(String w : words){
				word = w.replaceFirst("[^a-zA-Z0-9]*", "");
				word = new StringBuilder(word).reverse().toString().replaceFirst("[^a-zA-Z0-9]*", "");
				word = new StringBuilder(word).reverse().toString();
				if(word.length() == 0) continue;
				if(wordCounts.containsKey(word)) count = wordCounts.get(word) + 1;
				else count = 1;
				wordCounts.put(word, count);
			}
		}

		WriteToFile();
	}
}

class Merge implements Runnable
{
	private ArrayList<CountWord> maps;
	private Map<String, Integer> wordCounts;

	public Merge(ArrayList<CountWord> maps){
		this.maps = maps;
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

	public void run(){

		wordCounts = new TreeMap<String, Integer>();
		for(CountWord cw : maps){
			wordCounts.putAll(cw.getWordCountMap());
		}

		try {
 
			File file = new File("output/wordcount-java-merged.txt");
 
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
}