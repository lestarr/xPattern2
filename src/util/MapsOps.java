package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import model.WordSequences;
import modelparts.Word;

public class MapsOps {

	private static final String SPLITTER = ";";

	static public void printMap(Map<String, Double> ngramFreq, String info) {
		PrintStream savedStream = System.out;
		try {
			if (info != null)
				System.setOut(new PrintStream(new File(info)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String pattern : ngramFreq.keySet()) {
			System.out.println(pattern + "\t" + ngramFreq.get(pattern));
			// new DecimalFormat("#.#######################").format( hmap.get(pattern)));
			// System.out.println(pattern+"\t"+hmap.get(pattern));
		}
		System.setOut(savedStream);

	}
	
	static public <T, P> void printMapObject(Map<T, P> ngramFreq, String info) {
		PrintStream savedStream = System.out;
		try {
			if (info != null)
				System.setOut(new PrintStream(new File(info)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (T pattern : ngramFreq.keySet()) {
			System.out.println(pattern.toString() + "\t" + ngramFreq.get(pattern).toString());
			// new DecimalFormat("#.#######################").format( hmap.get(pattern)));
			// System.out.println(pattern+"\t"+hmap.get(pattern));
		}
		System.setOut(savedStream);

	}

	static public void printStringMap(Map<String, String> hmap, String info) {
		PrintStream savedStream = System.out;
		try {
			if (info != null)
				System.setOut(new PrintStream(new File(info)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		for (String pattern : hmap.keySet()) {
			System.out.println(pattern + "\t" + hmap.get(pattern));
		}
		System.setOut(savedStream);

	}

	static public Map<String, Double> printSortedMap(Map<String, Double> hmap, String info) {
		return printSortedMap(hmap, info, Integer.MAX_VALUE, true);
	}
	
	static public Map<String, Double> printSortedMap(Map<String, Double> hmap, String info, int schwelle) {
		return printSortedMap(hmap, info, Integer.MAX_VALUE, schwelle, true);
	}

	static public Map<String, Double> printSortedMap(Map<String, Double> hmap, String info, String sep) {
		return printSortedMap(hmap, info, Integer.MAX_VALUE, 0, true, sep);
	}
	
	static public SortedMap<String,Double> getSortedMap(Map<String, Double> hmap) {
		SortedMap<String,Double> sortedData = new TreeMap<String,Double>(new MapValueSort.ValueComparer(hmap));
		sortedData.putAll(hmap);
		return sortedData;
	}

	static public String getSortedMapAsString(Map<String, Double> hmap, String sep) {
		StringBuffer sbuf = new StringBuffer();
		SortedMap<String,Double> sortedData = new TreeMap<String,Double>(new MapValueSort.ValueComparer(hmap));
		sortedData.putAll(hmap);
		Iterator<String> it = sortedData.keySet().iterator();
		while(it.hasNext()) {
			String k = it.next();
			double v = sortedData.get(k);
			sbuf.append(k+"="+MyUtils.rdouble(v) + sep);
		}
		return sbuf.toString();
	}
	
	static public Map<String, Double> printSortedMap(Map<String, Double> hmap, String info, int howmany,
			boolean print) {
		return printSortedMap(hmap, info, howmany, 0, print);
	}

	static public Map<String, Double> printSortedMap(Map<String, Double> hmap, String info, int percentToSave,
			int schwelle, boolean print) {
		return printSortedMap(hmap, info, percentToSave, schwelle, print, "\n");
	}

	static public SortedMap getSortedMapObject(Map tsims) {
		if(tsims == null) return null;
		SortedMap sortedData = new TreeMap(new MapValueSort.ValueComparer(tsims));
		sortedData.putAll(tsims);
		return sortedData;
	}

	static public LinkedHashMap<Word,Double> printSortedMapWordDouble(Map<Word,Double> tsims, String info, int howManyWOrds, 
			double minfreq,
			boolean print,
			String sep) {
		if(howManyWOrds == -1) howManyWOrds = Integer.MAX_VALUE;
		if(tsims == null) return new LinkedHashMap();
		PrintStream stdout = System.out;
		try {
			if (info != null)
				System.setOut(new PrintStream(new File(info), "utf8"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		SortedMap<Word,Double> sortedData = new TreeMap(new MapValueSort.ValueComparer(tsims));
		sortedData.putAll(tsims);

		LinkedHashMap<Word, Double> toReturn = new LinkedHashMap();

		int i = 0;
		for (Iterator<Word> iter = sortedData.keySet().iterator(); iter.hasNext();) {
			i++;
			if (i > howManyWOrds)
				break;
			Word key = (Word) iter.next();
			double value = (double) sortedData.get(key);
			if(minfreq > 0 && (double)value < minfreq) continue; 
			if (print) {
				System.out.print(key + "\t" + sortedData.get(key));
				System.out.print(sep);
			}
			toReturn.put(key, value);
		}
		System.setOut(stdout);
		return toReturn;
	}
	
	static public LinkedHashMap<Object,Double> printSortedMapObjectDouble(Map<Object,Double> tsims, String info, int howManyWOrds, 
			double minfreq,
			boolean print,
			String sep) {
		if(howManyWOrds == -1) howManyWOrds = Integer.MAX_VALUE;
		if(tsims == null) return new LinkedHashMap();
		PrintStream stdout = System.out;
		try {
			if (info != null)
				System.setOut(new PrintStream(new File(info), "utf8"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		SortedMap<Object,Double> sortedData = new TreeMap(new MapValueSort.ValueComparer(tsims));
		sortedData.putAll(tsims);

		LinkedHashMap<Object, Double> toReturn = new LinkedHashMap();

		int i = 0;
		for (Iterator<Object> iter = sortedData.keySet().iterator(); iter.hasNext();) {
			i++;
			if (i > howManyWOrds)
				break;
			Object key = (Object) iter.next();
			double value = (double) sortedData.get(key);
			if(minfreq > 0 && (double)value < minfreq) continue; 
			if (print) {
				System.out.print(key + "\t" + sortedData.get(key));
				System.out.print(sep);
			}
			toReturn.put(key, value);
		}
		System.setOut(stdout);
		return toReturn;
	}


	static public LinkedHashMap<String, Double> printSortedMap(Map<String, Double> hmap, String info, int howManyWOrds,
			int schwelle, boolean print, String sep) {
		PrintStream stdout = System.out;
		try {
			if (info != null)
				System.setOut(new PrintStream(new File(info), "utf8"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SortedMap sortedData = new TreeMap(new MapValueSort.ValueComparer(hmap));
		sortedData.putAll(hmap);

		LinkedHashMap<String, Double> toReturn = new LinkedHashMap<String, Double>();

		int i = 0;
		int stop = howManyWOrds; // sortedData.size()*percentToSave/100;
		for (Iterator iter = sortedData.keySet().iterator(); iter.hasNext();) {
			i++;
			if (i > stop)
				break;
			String key = (String) iter.next();
			if (!((Double) sortedData.get(key) > schwelle))
				continue;
			if (print) {
				System.out.print(key + "\t" +
				// new DecimalFormat("#.#######################").format( sortedData.get(key)));
						new DecimalFormat("#.#####").format(sortedData.get(key)));
				System.out.print(sep);
			}
			toReturn.put(key, MyUtils.rdouble((double) sortedData.get(key)));
		}
		System.out.println();
		// for(String pattern: sortedData.keySet()){
		// System.out.println(pattern+"\t"+
		// new DecimalFormat("#.#######################").format(
		// sortedData.get(pattern)));
		// }
		System.setOut(stdout);
		return toReturn;
	}

	public static Map<String, Double> printSortedMap(HashMap<Word, Double> map, String info, int howManyWOrds,
			int schwelle, boolean print, String sep) {
		HashMap<String, Double> tmpmap = new HashMap<>();
		for (Word w : map.keySet()) {
			tmpmap.put(w.toString(), map.get(w));
		}
		return printSortedMap(tmpmap, info, howManyWOrds, schwelle, print, sep);
	}

	static public void printSortedSortedMap(Map<String, Double> hmap, int howmany) {
		int i = 0;
		for (String s : hmap.keySet()) {
			i++;
			if (i > howmany) {
				System.out.println();
				return;
			}
			System.out.print(s + " " + MyUtils.rdouble(hmap.get(s)) + ", ");
		}
		System.out.println();
	}

	public static void printParadigmMap(Map<String, Set<String>> paradigms, String info, boolean sort,
			boolean printreverse) {
		PrintStream stdout = System.out;
		if (info != null)
			try {
				System.setOut(new PrintStream(new File(info), "utf8"));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		Map<List<String>, Set<String>> reverseMap = new HashMap<List<String>, Set<String>>();
		Map<List<String>, Double> reverseMapFreq = new HashMap<List<String>, Double>();

		for (String key : paradigms.keySet()) {
			Set<String> wset = paradigms.get(key);
			if (sort) {
				ArrayList wlist = new ArrayList();
				wlist.addAll(wset);
				Collections.sort(wlist);
				if (printreverse) {
					if (!reverseMap.containsKey(wlist)) {
						reverseMap.put(wlist, new HashSet<String>());
						reverseMapFreq.put(wlist, 0.0);
					}
					reverseMap.get(wlist).add(key);
					reverseMapFreq.put(wlist, reverseMapFreq.get(wlist) + 1.0);
				} else
					System.out.println(key + "\t" + wlist.toString());
			} else {
				System.out.println(key + "\t" + wset.toString());
			}
		}
		System.setOut(stdout);
		SortedMap sortedData = new TreeMap(new MapValueSort.ValueComparer(reverseMapFreq));
		sortedData.putAll(reverseMapFreq);
		try {
			System.setOut(new PrintStream(new File("out/de-invert-paradigms.txt"), "utf8"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (Iterator iter = sortedData.keySet().iterator(); iter.hasNext();) {
			List<String> wset = (List<String>) iter.next();
			System.out.println(wset + ":\t" + reverseMap.get(wset));
		}
		System.setOut(stdout);
	}

	static public SortedMap<String, Double> returnSortedMap(Map<String, Double> ff_freq) {

		SortedMap<String, Double> sortedData = new TreeMap<String, Double>(new MapValueSort.ValueComparer(ff_freq));
		sortedData.putAll(ff_freq);

		return sortedData;
	}

	public static void addFreq(String w, Map<String, Double> seenWords) {
		addFreq(w, seenWords, 1.0);
	}

	public static void addFreq(int i, Map<Integer, Double> intFreq) {
		if (intFreq.containsKey(i))
			intFreq.put(i, intFreq.get(i) + 1.0);
		else
			intFreq.put(i, 1.0);
	}

	public static <T> void addFreqObject(T testPar, Map<T, Double> foundParadigms, double freq) {
		if (foundParadigms.containsKey(testPar))
			foundParadigms.put(testPar, foundParadigms.get(testPar) + freq);
		else
			foundParadigms.put(testPar, freq);
	}

	public static void addFreq(Word w, Map<Word, Double> seenWords) {
		if (seenWords.containsKey(w))
			seenWords.put(w, seenWords.get(w) + 1.0);
		else
			seenWords.put(w, 1.0);
	}

	public static void addFreq(String w, Map<String, Double> seenWords, double freq) {
		if (seenWords.containsKey(w))
			seenWords.put(w, seenWords.get(w) + freq);
		else
			seenWords.put(w, freq);
	}

	public static void addFreq(Word w, Map<Word, Double> seenWords, double value) {
		if (seenWords.containsKey(w))
			seenWords.put(w, seenWords.get(w) + value);
		else
			seenWords.put(w, value);		
	}

	public static void addFreq(Object o, Map<Object, Double> seenWords, double freq) {
		if (seenWords.containsKey(o))
			seenWords.put(o, seenWords.get(o) + freq);
		else
			seenWords.put(o, freq);
	}

	public static void addParadigm(String key, Map<String, String> seenWords, String newWord) {
		addParadigm(key, seenWords, newWord, true);
	}

	public static void addParadigm(String key, Map<String, String> seenWords, String newWord, boolean sortValue) {
		if (!seenWords.containsKey(key))
			seenWords.put(key, "");
		if (sortValue)
			seenWords.put(key, addStringSorted(seenWords.get(key), SPLITTER, newWord));
		else {
			String v = seenWords.get(key) + SPLITTER + newWord;
			seenWords.put(key, v);
		}

	}

	public static void addStringToValueSet(String key, Map<String, Set<String>> map, String value) {
		if (!map.containsKey(key))
			map.put(key, new HashSet());
		map.get(key).add(value);
	}
	
	public static void addStringToValueSetObject(String key, Map<String, Set<Object>> map, Object value) {
		if (!map.containsKey(key))
			map.put(key, new HashSet<Object>());
		map.get(key).add(value);
	}

	public static String getStringFormSortedSet(Set<String> set) {
		java.util.ArrayList<String> slist = new ArrayList<String>();
		slist.addAll(set);
		Collections.sort(slist);
		String toReturn = "";
		for (String s : slist) {
			toReturn = toReturn + SPLITTER + s;
		}
		return toReturn.replaceFirst("^" + SPLITTER, "");
	}

	private static String addStringSorted(String key, String splitter, String newWord) {
		String[] sarr = key.replaceFirst("^" + splitter, "").split(splitter);
		java.util.ArrayList<String> slist = new ArrayList<String>();
		slist.addAll(Arrays.asList(sarr));
		slist.add(newWord);
		Collections.sort(slist);
		String toReturn = "";
		for (String s : slist) {
			toReturn = toReturn + splitter + s;
		}
		return toReturn.replaceFirst("^" + splitter, "");
	}

	public static void addProb(String w, Double prob, Map<String, Double> seenWords) {
		if (seenWords.containsKey(w))
			seenWords.put(w, seenWords.get(w) + prob);
		else
			seenWords.put(w, prob);
	}

	public static HashMap<String, Double> transformListToMap(List<String> listToMap) {
		HashMap<String, Double> toReturn = new HashMap<String, Double>();
		for (String s : listToMap) {
			String[] sarr = s.split(SPLIT_PATTERN);
			if (sarr.length != 2)
				continue;
			toReturn.put(sarr[0], Double.parseDouble(sarr[1]));
		}
		return toReturn;
	}

	 public static Map<String, Double> readWordFreqs(String filePath) throws
	 NumberFormatException, IOException {
	 return readWordFreqs(filePath, 0, 0, 1, false, false);
	 }


	public static Map<String, Double> readWordFreqs(String filePath, double mainFreq, int indexOfWord, int indexFreq,
			boolean addPosNull, boolean tolower) throws NumberFormatException, IOException {
		String line;
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath)), "UTF-8"));

		Map<String, Double> wordFreqs = new HashMap<String, Double>();
		int c = 0;
		while ((line = br.readLine()) != null) {
			line = line.replaceAll(",", ".");
			// line = translateUkr(line);
			String[] sarr = line.split("\t");
			if (!sarr[indexOfWord].matches("[\\p{L}]+"))
				continue;
			if(indexFreq > sarr.length-1) System.out.println(sarr[0] +","+ sarr[1] + "," + sarr[sarr.length-1]);
			
			double freq = Double.parseDouble(sarr[indexFreq]);
			if (freq < mainFreq)
				continue;
			c++;

			String w = sarr[indexOfWord];
			if (tolower) {
				w = w.toLowerCase();
			}
			if (addPosNull)
				w = "_" + w + "_";
			MapsOps.addFreq(w, wordFreqs, freq);
		}
		br.close();
		System.out.println("count words:" + c);
		return wordFreqs;
	}

	public static Map<String, Double> readWordFreqs(WordSequences wsmodel, double mainFreq, boolean addPosNull,
			boolean tolower) throws NumberFormatException, IOException {

		Map<String, Double> wordFreqs = new HashMap<String, Double>();
		int c = 0;
		for (String w : wsmodel.idx().words.keySet()) {
			double freq = wsmodel.getWord(w).freq();
			if (freq < mainFreq)
				continue;
			c++;

			if (tolower) {
				w = w.toLowerCase();
			}
			if (addPosNull)
				w = "_" + w + "_";
			MapsOps.addFreq(w, wordFreqs, freq);
		}
		System.out.println("count words:" + c);
		return wordFreqs;
	}

	public static Map<String, Double> readWordList(String filePath, String splitter, int indexOfWord, boolean addPosNull)
			throws NumberFormatException, IOException {
		String line;
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath)), "UTF-8"));

		Map<String, Double> wordFreqs = new HashMap<String, Double>();
		int c = 0;
		while ((line = br.readLine()) != null) {
			// line = translateUkr(line);
			String[] sarr = line.toLowerCase().split("\t");
			if (!sarr[indexOfWord].matches("[\\p{L}]+"))
				continue;
			c++;

			String w = sarr[indexOfWord];
			if (addPosNull)
				w = "_" + w + "_";
			MapsOps.addFreq(w, wordFreqs);
		}
		br.close();
		System.out.println("count words:" + c);
		return wordFreqs;
	}

	private static final String SPLIT_PATTERN = "[,\t;]";

	static public Map<String, Double> getSortedMap(Map<String, Double> hmap, String info, boolean print) {
		return printSortedMap(hmap, info, Integer.MAX_VALUE, print);
	}

	static public SortedMap<String, Double> getSortedMapString(Map<String, Double> hmap) {
		SortedMap<String, Double> sortedData = new TreeMap(new MapValueSort.ValueComparer(hmap));
		sortedData.putAll(hmap);
		return sortedData;
	}

	static public SortedMap<String, Double> getSortedMapWord(Map<Word, Double> hmap) {
		HashMap<String, Double> tmpmap = new HashMap<>();
		for (Word w : hmap.keySet()) {
			tmpmap.put(w.toString(), hmap.get(w));
		}
		SortedMap<String, Double> sortedData = getSortedMapString(tmpmap);

		return sortedData;
	}

	public static SortedMap<Integer, Double> getSortedMapInt(HashMap<Integer, Double> semGroups) {
		SortedMap<Integer, Double> sortedData = new TreeMap(new MapValueSort.ValueComparer(semGroups));
		sortedData.putAll(semGroups);
		return sortedData;
	}
	
	public static Map<String, Double> getFirstEntriesString(Map<String, Double> freqMapR, int howmany) {
		SortedMap<String, Double> smap = getSortedMap(freqMapR);
		return getFirstEntries(smap, howmany, 0) ;
	}
	
	public static Map<String, Double> getFirstEntries(Map<Word, Double> freqMapR, int howmany) {
		SortedMap<String, Double> smap = getSortedMapWord(freqMapR);
		return getFirstEntries(smap, howmany, 0) ;
	}
	
	public static Map<String, Double> getFirstEntries(Map<Word, Double> freqMapR, int howmany, int schwelle) {
		SortedMap<String, Double> smap = getSortedMapWord(freqMapR);
		return getFirstEntries(smap, howmany, schwelle) ;
	}

	public static Map<String, Double> getFirstEntries(SortedMap<String, Double> smap, int howmany, int schwelle) {
		HashMap<String, Double> hmap = new HashMap<>();
		int i = 0;
		for (String w : smap.keySet()) {
			if (i >= howmany)
				break;
			if (smap.get(w) < schwelle)
				continue;
			hmap.put(w, smap.get(w));
			i++;
		}
		return hmap;
	}
	
	public static MyPair getFirst(Map thisParadigm) {
		SortedMap<Object, Double> smap = getSortedMapObject(thisParadigm);
		for (Object w : smap.keySet()) {
			return new MyPair(w.toString(), "", smap.get(w));
		}
		return new MyPair("", "", 0.0);
	}
	
	public static MyPair getLast(Map thisParadigm) {
		SortedMap<Object, Double> smap = getSortedMapObject(thisParadigm);
		Object last = null;
		for (Object w : smap.keySet()) {
			last = w;
		}
		if(last == null)
			return new MyPair("", "", 0.0);
		return new MyPair(last.toString(), "", smap.get(last));
	}
	
	public static List<MyPair> getFirstEntriesAsList(Map<String, Double> map1, int howmany) {
		SortedMap<String, Double> smap = getSortedMapString(map1);
		List<MyPair> toReturn = new ArrayList<>();
		int i = 0;
		for (String w : smap.keySet()) {
			if (i >= howmany)
				break;
			toReturn.add(new MyPair(w, "", smap.get(w)));
			i++;
		}
		return toReturn;
	}
	
	public static HashMap<String, Double> getFirstEntriesString(Map<String, Double> map1, int howmany,
			int schwelle) {
		SortedMap<String, Double> smap = getSortedMapString(map1);
		HashMap<String, Double> hmap = new HashMap<>();
		int i = 0;
		for (String w : smap.keySet()) {
			if (i >= howmany)
				break;
			if (smap.get(w) < schwelle)
				break;
			hmap.put(w, smap.get(w));
			i++;
		}
		return hmap;
	}
	
	public static HashMap<String, Double> getFirstEntriesObject(Map map1, int howmany, double signif) {
		SortedMap<Object, Double> smap = getSortedMapObject(map1);
		HashMap<String, Double> hmap = new HashMap<>();
		int i = 0;
		for (Object w : smap.keySet()) {
			if (i >= howmany)
				break;
			if (smap.get(w) < signif)
				break;
			hmap.put(w.toString(), smap.get(w));
			i++;
		}
		return hmap;
	}

	/**
	 * Adds 1 to the value of the keys, doesn't merges or looks at the values
	 * 
	 * @param map
	 * @param bigMap
	 */
	public static void mergeMaps(HashMap<String, Double> map, HashMap<String, Double> bigMap) {
		for (String s : map.keySet()) {
			MapsOps.addFreq(s, bigMap);
		}
	}

	/**
	 * persent of group2 which are in group1 in comparrison to the size of group1
	 * @param group1
	 * @param group2
	 * @param minFreq 
	 * @return
	 */
	public static double getIntersectionValue(HashMap<Word, Double> group1, HashMap<Word, Double> group2, double minFreq) {
		double sumSeen = 0;
		HashSet<Word> mainWords = new HashSet<>();
		double maxFreq = 50;
		for(Word w: group1.keySet()) {
			if(w.freq() > minFreq && w.freq() < maxFreq) mainWords.add(w);
		}
		
		HashSet<Word> testWords = new HashSet<>();
		for(Word w: group2.keySet()) {
			if(w.freq() > minFreq && w.freq() < maxFreq) testWords.add(w);
		}
		for(Word w: testWords) {
			if(mainWords.contains(w))
				sumSeen++;
		}
		return sumSeen/mainWords.size();
	}

	/**
	 * persent of group2 which are in group1 in comparrison to the size of group1
	 * @param group1
	 * @param group2
	 * @param minFreq 
	 * @return
	 */
	public static double getIntersectionValueForTestWord(HashMap<Word, Double> group1, HashMap<Word, Double> group2, double minFreq) {
		double sumSeen = 0;
		HashSet<Word> mainWords = new HashSet<>();
		double maxFreq = 100;
		for(Word w: group1.keySet()) {
			if(w.freq() > minFreq && w.freq() < maxFreq) mainWords.add(w);
		}
		
		HashSet<Word> testWords = new HashSet<>();
		for(Word w: group2.keySet()) {
			if(w.freq() > minFreq && w.freq() < maxFreq) testWords.add(w);
		}
		for(Word w: testWords) {
			if(mainWords.contains(w))
				sumSeen++;
		}
		return testWords.size() > 0 ? sumSeen/testWords.size() : 0;
	}
	
	public static void addSubParadigToDistribution(Set<Object> newSubParadigm , Map<Object,Double> distrMap) {
		for(Object elem: newSubParadigm) {
			if(!distrMap.containsKey(elem)) distrMap.put(elem, 0.0);
			distrMap.put(elem, distrMap.get(elem)+1);
			
		}
	}

	public static void addSubParadigToDistribution2(Set<String> newSubParadigm, Map<String, Double> freq_distribution) {
		for(String elem: newSubParadigm) {
			if(!freq_distribution.containsKey(elem)) freq_distribution.put(elem, 0.0);
			freq_distribution.put(elem, freq_distribution.get(elem)+1);
			
		}
	}

	public static MyPair getFirstButNOT(Map<String,Double> thisParadigm, Set<String> butnot) {
		SortedMap<String, Double> smap = getSortedMapString(thisParadigm);
		for (String w : smap.keySet()) {
			if(butnot == null || !butnot.contains(w))
				return new MyPair(w.toString(), "", smap.get(w));
		}
		return new MyPair("", "", 0.0);
	}

	public static String getSortedByKey(Map<String, Double> map) {
		StringBuffer sbuf = new StringBuffer();
		List<String> keysSorted = ListOps.of(map.keySet());
		Collections.sort(keysSorted);
		for(String key: keysSorted) {
			sbuf.append(key+"="+map.get(key)+", ");
		}
		return sbuf.toString();
	}




	
}
