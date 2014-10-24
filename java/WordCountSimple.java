import java.util.*;
import java.util.regex.*;
import java.io.*;

public class WordCountSimple
{
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

	private static void WriteToFile(Map<String, Integer> wordCounts){
		try {
 
			File file = new File("output/wordcount-java-simple.txt");
 
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

	public static void main(String[ ] args){
		
		//String inputFileName = args[0];

		int numThreads = args.length;

		ArrayList<CountWord> threads = new ArrayList<CountWord>();

		Map<String, Integer> wordCounts = new TreeMap<String, Integer>();

		for(int i = 0; i < numThreads; i++){
			CountWord cw = new CountWord(args[i], wordCounts, i);
			threads.add(cw);
			cw.start();
		}

		for ( CountWord t: threads ) {
			try{
				t.join();
				wordCounts.putAll( t.getWordCountMap() );
			} catch (Exception ex){}
		}

		WriteToFile(wordCounts);
		System.out.println("allright");
	}
}

class CountWord extends Thread
{
	private String inputFileName;
	private Map<String, Integer> wordCounts;
	private int id;

	public CountWord(String inputFileName, Map<String, Integer> wordCounts, int id){
		this.inputFileName = inputFileName;
		this.wordCounts = wordCounts;
		this.id = id;
   	}

   	public Map<String, Integer> getWordCountMap(){
   		return wordCounts;
   	}

	public void run(){
		int count;
      	String word;

      	Pattern pattern = Pattern.compile("[\\s]+");

		try
		{
			Scanner in = new Scanner(new File(inputFileName));

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
			System.out.println(inputFileName + " not found!");
			e.printStackTrace();
			return;
		}
	}
}