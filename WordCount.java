import java.util.*;
import java.util.regex.*;
import java.io.*;

public class WordCount
{
	public static void main(String[ ] args){
		
		//Map<String, Integer> wordCounts = new TreeMap<String, Integer>();
		Map<String, Integer> wordCounts = new TreeMap<String, Integer>();

		// if(args.length != 2){
		// 	System.out.println("Provide the input and output files when runing the program: java WordCount input output");
		// 	return;
		// }

		String inputFileName = args[0];

		CountWord c = new CountWord(inputFileName, wordCounts, 0);
		c.start();
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

   	private static Map<String, Integer> sortByComparator(Map<String, Integer> unsortMap) {
 
		// Convert Map to List
		List<Map.Entry<String, Integer>> list = 
			new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());
 
		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1,
                                           Map.Entry<String, Integer> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});
 
		// Convert sorted map back to a Map
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
			e.printStackTrace();
			return;
		}

	}

	public void run(){
		int count;
      	String word;

      	Pattern pattern = Pattern.compile("[\\s]+");
      	// Pattern pattern = Pattern.compile("[^\\w|;.']+");


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

		WriteToFile();
	}
}