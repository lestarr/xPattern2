//package modeltrain;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.StringTokenizer;
//
//import model.LetterTokModel;
//import modelparts.Flexion;
//import modelparts.MorphParadigm;
//import modelparts.WPartTitle;
//import modelparts.WordPart;
//import tokenizer.TestTokenizer;
//import util.CorpusUtils;
//import util.ListOps;
//import util.MapsOps;
//import util.MyPair;
//import util.MyUtils;
//
//public class MorphAnalyzerOld {
//
//	private static final String[] LETTERS = new String[] { "tsch", "sch", "ch", "ck", "qu" };
//	private static final String SPLITTER = "#";
//
//	public static LetterTokModel trainLetterTokModel(String lang, String fileIn, double mainFreq, int indexFreq) throws IOException {
////		if(lang.equals("de")) return trainLetterTokModel(lang, fileIn, mainFreq, 1, 3);
////		if(lang.equals("en")) return trainLetterTokModel(lang, fileIn, mainFreq, 1, 3);
////		if(lang.equals("ukr")) return trainLetterTokModel(lang, fileIn, mainFreq, 1, 2);
////		if(lang.equals("de")) return trainLetterTokModel(lang, fileIn, mainFreq, 1, 3);
//		return trainLetterTokModel(lang, fileIn, mainFreq, 1, indexFreq);
//	}
//	
//	
//	// take words uniq, make suffixes and prefixes of length < 10 and freq list of
//	// them
//	// compare every pref of len l with l+1, same for suf; l+1/l -> cond. Prob.
//	// /slice
//	// it shows how much variability is explained with this letter/combination
//	// compare slices after each other if there is a big step down - e.g. from 0.9
//	// to 0.2
//	// make cut or split; these places are for potential morphological components
//
//	// take all cut points - make statistic on them - suf, pref separated
//
//	private static LetterTokModel trainLetterTokModel(String lang, String fileIn, double mainFreq, int indexOfWord,
//			int indexFreq) throws IOException {
//		LetterTokModel ltmodel = new LetterTokModel(lang);
//
//		// read ngram model
//		String ngramFile = "out/ngram/" + lang + "-ngram-freq.model";
//		File f = new File(ngramFile);
//		Map<String, Double> ngramFreq;
//		boolean makeNgramFreq = false;
//		if (f.exists()) {
//			ngramFreq = MapsOps.readWordFreqs(ngramFile, 10, 0, 1, false, true);
//			ltmodel.ngrams = ngramFreq;
//		} else {
//			System.out.println("training new ngram model");
//			makeNgramFreq = true;
//			ngramFreq = new HashMap<String, Double>();
//		}
//
//		Map<String, Double> prefs = new HashMap<String, Double>();
//		Map<String, Double> sufs = new HashMap<String, Double>();
//
//		Map<String, Double> wordFreqs = MapsOps.readWordFreqs(fileIn, mainFreq, indexOfWord, indexFreq, true, true);
//		System.err.println(lang + ": " + wordFreqs.size());
//		int c = 0;
//		for (String w : wordFreqs.keySet()) {
//			c++;
//			if (c % 10000 == 0)
//				System.out.println(c);
//			if(wordFreqs.get(w) < 2) continue;
//			if (w.length() == 1)
//				continue;
//
//			List<String> subPrefs = getSubPrefs(w, lang);
//			List<String> subSufs = getSubSufs(w, lang);
//
//			for (String s : subPrefs) {
//				MapsOps.addFreq(s, prefs);
//			}
//			for (String s : subSufs) {
//				MapsOps.addFreq(s, sufs);
//			}
//			if (makeNgramFreq) {
//				List<String> ngrams = getSubStrings(w, lang);
//				for (String s : ngrams) {
//					MapsOps.addFreq(s, ngramFreq);
//				}
//			}
//		}
//		ltmodel.prefs = prefs;
//		ltmodel.sufs = sufs;
//		ltmodel.ngrams = ngramFreq;
//		if (makeNgramFreq) {
//			MapsOps.printMap(ngramFreq, ngramFile);
//		}
//		return ltmodel;
//	}
//
////	public static LetterTokModel trainLetterTokModel(WordSequences wsmodel, double mainFreq) throws IOException {
////		String lang = wsmodel.getLang();
////
////		LetterTokModel ltmodel = new LetterTokModel(lang);
////		// read ngram model
////		String ngramFile = "out/" + lang + "-new-ngram-freq.model";
////		File f = new File(ngramFile);
////		Map<String, Double> ngramFreq;
////		boolean makeNgramFreq = false;
////		if (f.exists()) {
////			ngramFreq = MapsOps.readWordFreqs(ngramFile, 2, 0, 1, false, true);
////			ltmodel.ngrams = ngramFreq;
////		} else {
////			makeNgramFreq = true;
////			ngramFreq = new HashMap<String, Double>();
////		}
////
////		Map<String, Double> prefs = new HashMap<String, Double>();
////		Map<String, Double> sufs = new HashMap<String, Double>();
////
////		Map<String, Double> wordFreqs = MapsOps.readWordFreqs(wsmodel, mainFreq, true, true);
////		System.err.println(lang + ": " + wordFreqs.size());
////		int c = 0;
////		for (String w : wordFreqs.keySet()) {
////			c++;
////			if (c % 10000 == 0)
////				System.out.println(c);
////
////			if (w.length() == 1)
////				continue;
////
////			List<String> subPrefs = getSubPrefs(w, lang);
////			List<String> subSufs = getSubSufs(w, lang);
////
////			for (String s : subPrefs) {
////				MapsOps.addFreq(s, prefs);
////			}
////			for (String s : subSufs) {
////				MapsOps.addFreq(s, sufs);
////			}
////			if (makeNgramFreq) {
////				List<String> ngrams = getSubStrings(w, lang);
////				for (String s : ngrams) {
////					MapsOps.addFreq(s, ngramFreq);
////				}
////			}
////		}
////		ltmodel.prefs = prefs;
////		ltmodel.sufs = sufs;
////		ltmodel.ngrams = ngramFreq;
////		if (makeNgramFreq) {
////			MapsOps.printSortedMap(ngramFreq, ngramFile);
////		}
////		return ltmodel;
////	}
//
//	// take words uniq, make suffixes and prefixes of length < 10 and freq list of
//	// them
//	// compare every pref of len l with l+1, same for suf; l+1/l -> cond. Prob.
//	// /slice
//	// it shows how much variability is explained with this letter/combination
//	// compare slices after each other if there is a big step down - e.g. from 0.9
//	// to 0.2
//	// make cut or split; these places are for potential morphological components
//
//	// take all cut points - make statistic on them - suf, pref separated
//
////	public static LetterTokModel trainLetterTokModel(String lang, WordSequences model, double mainFreq) throws IOException {
////		LetterTokModel ltmodel = new LetterTokModel(lang);
////
////		Map<String, Double> prefs = new HashMap<String, Double>();
////		Map<String, Double> sufs = new HashMap<String, Double>();
////
////		System.err.println(lang + ": " + model.idx().words.size());
////		int c = 0;
////		for (String w : model.idx().words.keySet()) {
////			c++;
////			if (c % 10000 == 0)
////				System.out.println(c);
////
////			if (w.length() == 1)
////				continue;
////			if (model.getWord(w).freq() < mainFreq)
////				continue;
////
////			// start process
////			if (!w.startsWith("_"))
////				w = "_" + w;
////			if (!w.endsWith("_"))
////				w = w + "_";
////
////			List<String> subPrefs = getSubPrefs(w, lang);
////			List<String> subSufs = getSubSufs(w, lang);
////
////			for (String s : subPrefs) {
////				MapsOps.addFreq(s, prefs);
////			}
////			for (String s : subSufs) {
////				MapsOps.addFreq(s, sufs);
////			}
////		}
////		ltmodel.prefs = prefs;
////		ltmodel.sufs = sufs;
////		return ltmodel;
////	}
//
//	static public List<String> getTokens(String w, String lang) {
//		List<String> tokens = new ArrayList<String>();
//		StringTokenizer st;
//		if (lang.equals("de"))
//			st = TestTokenizer.getTokenizer(w, LETTERS);
//		else
//			st = TestTokenizer.getTokenizer(w, new String[] {});
//		while (st.hasMoreTokens()) {
//			String letter = st.nextToken();
//			tokens.add(letter);
//		}
//		return tokens;
//	}
//
//	/**
//	 * gets all substrings from word, e.g.: part: _p,_pa,_par,_part, _part_,
//	 * p,pa,par,part, part_,a,ar,art,art_,r,rt,rt_,t,t_
//	 * 
//	 * @param w
//	 * @param lang
//	 * @return
//	 */
//	private static List<String> getSubStrings(String w, String lang) {
//		List<String> tokens = getTokens(w, lang);
//		List<String> substrings = new ArrayList<String>();
//		String prev = "";
//		for (String t : tokens) {
//			if (prev.equals("")) {
//				prev = t;
//				continue;
//			}
//			substrings.add(prev);
//			prev = prev + t;
//		}
//		// for (int i = 0; i < tokens.size(); i++) {
//		while (tokens.size() > 1) {
//			tokens.remove(0);
//			prev = "";
//			for (String t : tokens) {
//				if (prev.equals("")) {
//					prev = t;
//					substrings.add(prev);
//					continue;
//				}
//				prev = prev + t;
//				substrings.add(prev);
//			}
//		}
//
//		return substrings;
//	}
//
//	private static List<String> getSubPrefs(String w, String lang) {
//		List<String> tokens = getTokens(w, lang);
//		List<String> substrings = new ArrayList<String>();
//		String prev = "";
//		for (String t : tokens) {
//			if(t.length() == 0) continue;
//
//			prev = prev + t;
//			substrings.add(prev);
//		}
//		return substrings;
//	}
//
//	private static List<String> getSubSufs(String w, String lang) {
//		List<String> tokens = getTokens(w, lang);
//		List<String> substrings = new ArrayList<String>();
//		String prev = "";
//		for (int i = tokens.size() - 1; i >= 0; i--) {
//			String t = tokens.get(i);
//			if(t.length() == 0) continue;
//
//			prev = t + prev;
//			substrings.add(prev);
//		}
//		return substrings;
//	}
//
//	public static List<MyPair> getWordCondFreqAnalysisSubstrings(String lang, LetterTokModel ltmod, String word,
//			String sufOrPref) {
//		if(!word.startsWith("_")) word = "_" + word;
//		if(!word.endsWith("_")) word = word + "_";
//		// key = pair: f=prefix, s = sufix + slice(freq(pref+1)/freq(pref))
//		List<MyPair> slices = new ArrayList<MyPair>();
//		List<MyPair> substrPairs;
//		Map<String, Double> sufPrefFreq;
//
//		if (sufOrPref.startsWith("suf")) {
//			substrPairs = getSubstringPairs(getSubSufs(word, lang));
//			sufPrefFreq = ltmod.sufs;
//		} else {
//			substrPairs = getSubstringPairs(getSubPrefs(word, lang));
//			sufPrefFreq = ltmod.prefs;
//		}
//
//		for (MyPair p : substrPairs) {
////			if(p.second.length() == word.length()) continue;
//			double slice = computeSlice(p, sufPrefFreq);
//			if(word.length() < p.second.length()) {
//				System.out.println(word);
//				continue;
//			}
//			if(word.toString().equals("встановлювати"))
//				System.out.println();
//			if (sufOrPref.startsWith("suf"))
//				slices.add(new MyPair(word.substring(0, word.length() - p.second.length()), word.substring(word.length() - p.second.length()), slice));
//			else {
////				System.out.println(p.first);
////				System.out.println(p.second);
////				System.out.println(word);
//				slices.add(new MyPair(word.substring(0, p.second.length()), word.substring(p.second.length()), slice));
//			}
//		}
//		return slices;
//	}
//
//	// computes the cond prob from longer ngram if given shorter ngram: haus/hau
//	private static double computeSlice(MyPair twoNgrams, Map<String, Double> sufPrefFreq) {
//
////		if (!sufPrefFreq.containsKey(twoNgrams.first) || !sufPrefFreq.containsKey(twoNgrams.second)) {
////			return -1;
////		if (!sufPrefFreq.containsKey(twoNgrams.first) && !sufPrefFreq.containsKey(twoNgrams.second)) 
////			return -1;
////		else 
//			if (!sufPrefFreq.containsKey(twoNgrams.first) || !sufPrefFreq.containsKey(twoNgrams.second)) 
//			return 1;	
//		else {
//			double firstFreq = sufPrefFreq.get(twoNgrams.first);
//			double secondFreq = sufPrefFreq.get(twoNgrams.second);
//			double slice = secondFreq / firstFreq; // longer/shorter
//			return slice;
//		}
//	}
//
//	public static Map<String, ArrayList<Double>> getSufixSplits(String lang, LetterTokModel ltmod, String word)
//			throws IOException {
//		Map<String, ArrayList<Double>> wordSplits = new LinkedHashMap<String, ArrayList<Double>>();
//
//		// saves word with its indexes for splits
//		wordSplits.put(word, new ArrayList<Double>());
//
//		List<MyPair> sufPairs = getSubstringPairs(getSubSufs(word, lang));
//		for (MyPair p : sufPairs) {
//			double slice = computeSlice(p, ltmod.sufs);
//			wordSplits.get(word).add(slice);
//		}
//		return wordSplits;
//
//	}
//
//	private static List<MyPair> getSubstringPairs(List<String> substrings) {
//		List<MyPair> pairlist = new ArrayList<MyPair>();
//		String prev = "";
//		for (String s : substrings) {
//			if (prev.equals("")) {
//				prev = s;
//				continue;
//			} else {
//				MyPair p = new MyPair(prev, s);
//				pairlist.add(p);
//				prev = s;
//			}
//		}
//		return pairlist;
//	}
//
//	public static void printWordSplits(Map<String, ArrayList<Integer>> wordSplitters) {
//
//		for (String w1 : wordSplitters.keySet()) {
//			ArrayList<Integer> idxlist = wordSplitters.get(w1);
//			Collections.sort(idxlist);
//			if (idxlist.size() != 1)
//				continue;
//			if (idxlist.size() == 0) {
//				System.out.println(w1);
//			} else {
//				System.out.print(w1.substring(0, idxlist.get(0)));
//				for (int i = 0; i < idxlist.size() - 1; i++) {
//					System.out.print("|" + w1.substring(idxlist.get(i), idxlist.get(i + 1)));
//				}
//				System.out.print("|" + w1.substring(idxlist.get(idxlist.size() - 1)));
//				System.out.println();
//
//			}
//		}
//	}
//
//	public static MyPair getLastPeakSlice(List<MyPair> slices) {
//		MyPair lastPeak = null;
//		double prev = -1;
//		boolean up = false;
//
//		for (MyPair p : slices) {
//			double slice = p.freq;
//			if (prev == -1) {
//				prev = slice;
//				lastPeak = p;
//				up = false;
//			} else {
//				if (prev > slice && up) {
//					lastPeak = p;
//					up = false;
//				} else {
//					if (prev > slice) // goes down
//						up = false;
//					else
//						up = true; // goes up
//				}
//				prev = slice;
//			}
//		}
//		return lastPeak;
//	}
//
//	// takes last peak - where slice goes up and analyses the slices from then till
//	// the end - where ist the biggest step
//	// and changes the peak if there was bigger step afterwards
//	public static String getLastPeakSliceRounded(List<MyPair> slices) {
//		MyPair lastPeak = null;
//		MyPair lastBiggestStepPair = null;
//		double lastStep = -1.0;
//
//		double prev = -1;
//		boolean up = false;
//
//		for (MyPair p : slices) {
//			double slice = p.freq;
//			if (prev == -1) {
//				prev = slice;
//				up = false;
//			} else {
//				double step = prev - slice;
//				if (prev > slice && up) {
//					lastPeak = p;
//					lastBiggestStepPair = p;
//					up = false;
//					lastStep = step;
//				} else {
//					if (prev > slice) { // goes down
//						up = false;
//						if (step > lastStep) {// bigger step than the step after peak
//							lastBiggestStepPair = p;
//						}
//					} else {
//						up = true; // goes up
//					}
//				}
//				prev = slice;
//				lastStep = step;
//			}
//		}
//		String toReturn = "";
//		if (lastPeak != null) {
//			toReturn = lastPeak.first + SPLITTER + lastPeak.second;
//		}
//		if (lastBiggestStepPair != null) {
//			if (lastPeak != null && !lastBiggestStepPair.first.equals(lastPeak.first))
//				// toReturn = toReturn+" ->
//				// "+lastBiggestStepPair.first+"\t"+lastBiggestStepPair.second;
//				toReturn = lastBiggestStepPair.first + SPLITTER + lastBiggestStepPair.second;
//		}
//		return toReturn;
//	}
//
//	//mark steps (idx) which are > 0.5
//	public static List<Integer> getSteps(List<MyPair> slices, String sufOrPref) {
//		double prev = -1.0;
//		List<Integer> step_indexes = new ArrayList<Integer>();
//		for (MyPair p : slices) {
//			if (prev < 0) {
//				prev = p.freq;
//				continue;
//			} else {
//				double this_slice = p.freq;
//				double step = prev - this_slice;
//				if (step > 0.5) {
//					if (sufOrPref.equals("suf"))
//						step_indexes.add(0, p.first.length()); // add at the 1st position
//					else
//						step_indexes.add(p.first.length());
//				}
//				prev = this_slice;
//			}
//
//		}
//		return step_indexes;
//	}
//	
//	public static String getStepsAndSplit(List<MyPair> slices, List<Integer> step_indexes) {
//		StringBuffer word = new StringBuffer();
//		for (MyPair p : slices) {
//			word = word.append(p.first + p.second);
//			break;
//		}
//		for (int i = step_indexes.size() - 1; i >= 0; i--) {
//			int idx = step_indexes.get(i);
//			word = word.insert(idx, SPLITTER); // word.substring(0,idx)+"#"+word.substring(idx);
//		}
//		return word.toString();
//	}
//
//
//
//
//	private static List<Integer> getCutIdx(String wordWithCuts) {
//		List<Integer> idxList = new ArrayList<Integer>();
//		if (!wordWithCuts.contains(SPLITTER))
//			return idxList;
//		int lenSum = 0;
//		String[] sarr = wordWithCuts.split(SPLITTER);
//		for (int i = 0; i < sarr.length - 1; i++) { // we dont need last part, because it is the end of word
//			lenSum = lenSum + sarr[i].length();
//			idxList.add(lenSum);
//		}
//		return idxList;
//	}
//
//	public static List<WordPart> getWordParts(LetterTokModel ltmod, String w) {
//		if (!w.startsWith("_"))
//			w = "_" + w;
//		if (!w.endsWith("_"))
//			w = w + "_";
//		List<MyPair> slices = getWordCondFreqAnalysisSubstrings(ltmod.getLang(), ltmod, w, "pref");
//		String findFlexes = getLastPeakSliceRounded(slices);
//		String prefCuts = getStepsAndSplit(slices, getSteps(slices, "pref"));
//		slices = getWordCondFreqAnalysisSubstrings(ltmod.getLang(), ltmod, w, "suf");
//		String sufCuts = getStepsAndSplit(slices, getSteps(slices, "suf"));
//
//		// find part frequences
//		Map<Integer, Double> intFreq = new HashMap<Integer, Double>();
//		List<String> wordWithCutsList = new ArrayList<String>();
//		wordWithCutsList.add(findFlexes);
//		wordWithCutsList.add(sufCuts);
//		wordWithCutsList.add(prefCuts);
//		for (String s : wordWithCutsList) {
//			List<Integer> idxList = getCutIdx(s);
//			for (int idx : idxList) {
//				MapsOps.addFreq(idx, intFreq);
//			}
//		}
//
//		List<Integer> allCuts = new ArrayList<Integer>();
//		allCuts.addAll(intFreq.keySet());
//		Collections.sort(allCuts);
//
//		List<WordPart> wpList = new ArrayList<WordPart>();
//		int lastIdx = w.length();
//		for (int i = allCuts.size() - 1; i >= -1; i--) {
//			int idx;
//			String wpartString;
//			if (i == -1) { // for last part - start with 0
//				if (allCuts.size() < 2) // no need to add last part, it is the same as root
//					break;
//				idx = lastIdx;
//				wpartString = w.substring(0, lastIdx);
//			} else {
//				idx = allCuts.get(i);
//				wpartString = w.substring(idx, lastIdx);
//			}
//
//			double idxFreq = intFreq.get(idx);
//			lastIdx = idx;
//
//			WPartTitle title = new WPartTitle();
//			if (idxFreq > 1) {
//				title.setGood(true);
//			} else
//				title.setNew(true);
//			if (wpartString.endsWith("_")) {
//				title.setFlex(true);
//				// make root
//				String rootString = w.substring(0, idx);
//				WPartTitle rootTitle = new WPartTitle();
//				rootTitle.setRoot(true);
//				if (title.getGood())
//					rootTitle.setGood(true);
//				else
//					rootTitle.setNew(true);
//				WordPart root = new WordPart(rootTitle, rootString);
//				wpList.add(root);
//			} else
//				title.setPart(true);
//			WordPart wpart = new WordPart(title, wpartString);
//			wpList.add(wpart);
//		}
//		return wpList;
//	}
//
//	public static void getAllWordParts(LetterTokModel ltmod, Map<String, Double> seenWords) throws IOException {
//		Map<String, Double> wpatFreq = new HashMap<String, Double>();
//
//		for (String w : seenWords.keySet()) {
//			List<WordPart> wpList = getWordParts(ltmod, w);
//
//			// do sth with the wpList
//			String output = w;
//			for (WordPart wp : wpList) {
//				output = output + "\t" + wp.toString();
//				MapsOps.addFreq(wp.getString(), wpatFreq);
//			}
//			System.out.println(output);
//		}
//		// MapsOps.printSortedMap(wpatFreq, "out/"+lang+"-wordPartFreqs.txt");
//	}
//
//	public static void mainDe() throws IOException {
//		String filePath;
//		String lang = "de";
//		filePath = CorpusUtils.getLeipzigWordsPath(lang, "wiki");
//		LetterTokModel ltmod = trainLetterTokModel(lang, filePath, 30, 3); // no big difference between freq 1 and 2..
//		ltmod.serializeModel("out/model/de-LetterTokModel-3M.ser");
//		Map<String, Double> seenWords = MapsOps.readWordFreqs(filePath, 20, 1, 3, true, true);
//		getAllWordParts(ltmod, seenWords);
//	}
//
//	public static void mainEn() throws IOException {
//		String filePath;
//		String lang = "en";
//		filePath = CorpusUtils.getLeipzigWordsPath(lang, "wiki");
//		LetterTokModel ltmod = trainLetterTokModel(lang, filePath, 30, 3); // no big difference between freq 1 and 2..
//		Map<String, Double> seenWords = MapsOps.readWordFreqs(filePath, 20, 1, 3, true, true);
//		getAllWordParts(ltmod, seenWords);
//	}
//
//	public static void mainUkr() throws IOException {
//		String filePath;
//		String lang = "ukr";
//		filePath = CorpusUtils.getLeipzigWordsPath(lang, "news");
//		LetterTokModel ltmod = trainLetterTokModel(lang, filePath, 30, 2); // no big difference between freq 1 and 2..
//		Map<String, Double> seenWords = MapsOps.readWordFreqs(filePath, 20, 1, 2, true, true);
//		getAllWordParts(ltmod, seenWords);
//	}
//
//	public static String getCutsAndSlices(String lang, LetterTokModel ltmod, String w) {
//		if(!w.startsWith("_")) w = "_" + w;
//		if(!w.endsWith("_")) w = w + "_";
//		List<MyPair> slices = getWordCondFreqAnalysisSubstrings(lang, ltmod, w, "pref");
//		StringBuffer sb = new StringBuffer();
//		for (MyPair p : slices) {
//			sb.append(p.first+"\t"+p.second+"\t"+p.freq+"\n");
//		}
//		// cmpFlexes.add(getLastPeakSliceRounded(slices));
//		sb.append(getLastPeakSliceRounded(slices)+"\n"); //.replaceAll(SPLITTER, "\t")
//		String wordWithCuts = getStepsAndSplit(slices, getSteps(slices, "pref"));
//		sb.append(wordWithCuts+"\n");
//
//		// same with suffix
//		slices = getWordCondFreqAnalysisSubstrings(lang, ltmod, w, "suf");
//		for (MyPair p : slices) {
//			sb.append(p.first+"\t"+p.second+"\t"+p.freq+"\n");
//		}
//		wordWithCuts = getStepsAndSplit(slices, getSteps(slices, "suf"));
//		wordWithCuts = wordWithCuts; //.replaceAll(SPLITTER, "_._");
//		sb.append(wordWithCuts+"\n");
//		return sb.toString();
//	}
//
//	public static void exploreModel(String lang) throws IOException {
//		String filePath = null;
//		LetterTokModel ltmod = null;
//		if (lang.equalsIgnoreCase("de")) {
//			filePath = CorpusUtils.getLeipzigWordsPath(lang, "news");
//			ltmod = trainLetterTokModel(lang, filePath, 2, 1, 3); // no big difference between freq 1 and 2..
//		} else if (lang.equalsIgnoreCase("ukr")) {
//			filePath = CorpusUtils.getLeipzigWordsPath(lang, "news");
//			ltmod = trainLetterTokModel(lang, filePath, 2, 1, 2); // no big difference between freq 1 and 2..
//		}
//		while (true) {
//			System.out.println("your input ...");
//			String input = MyUtils.getSystemInput();
//			if (input.equals("e"))
//				break;
//
//			System.out.println(getWordParts(ltmod, input).toString());
//		}
//	}
//
//	public static String getRoot(LetterTokModel ltmod, String input) {
//		List<WordPart> wparts = getWordParts(ltmod, input);
//		if (WordPart.getRoot(wparts).length() > 0)
//			return WordPart.getRoot(wparts).replaceAll("_", "");
//		else
//			return null;
//	}
//	
//	public static String[] getRootFlex(LetterTokModel ltmod, String input) {
//		List<WordPart> wparts = getWordParts(ltmod, input);
//		String root = "", flex = "";
//		if (WordPart.getRoot(wparts).length() > 0) {
//			root =  WordPart.getRoot(wparts).replaceAll("_", "");
//			flex = WordPart.getFlexion(wparts).replaceAll("_", "");
//		}else
//			return new String[] {};
//			
//		return new String[] {root,flex};
//	}
//
//	public static int[] getAllPeaks(List<MyPair> slices) {
//		List<Integer> peaks = ListOps.of();
//		double highestProb = 0.3; // we start analysis here
//		int lastPeak = 0;
//		for (int i = 1; i < slices.size(); i++) {
//			MyPair act = slices.get(i);
//			MyPair prev = slices.get(i-1);
//			double step = prev.freq - act.freq;
//			if ((prev.freq >= highestProb ) &&  step > 0) {
//				highestProb = prev.freq;
//				lastPeak = i-1;
//			}
//		}
//		peaks.add(lastPeak);
//		//check one more Peak after highest Peak
//		highestProb = 0.5;
//		boolean sawNewPeak = false;
//		for (int i = lastPeak+2; i < slices.size(); i++) {
//			MyPair act = slices.get(i);
//			MyPair prev = slices.get(i-1);
//			double step = prev.freq - act.freq;
//			if ((prev.freq >= highestProb ) &&  step > 0.5) {
//				highestProb = prev.freq;
//				lastPeak = i-1;
//				sawNewPeak = true;
//			}
//		}
//		if(sawNewPeak) peaks.add(lastPeak);
//		
//		int[]array = new int[peaks.size()];
//		for (int i = 0; i < array.length; i++) {
//			array[i] = peaks.get(i);
//		}
//		return array;
//	}
//
//	public static void main(String[] args) throws IOException {
//	
//		mainUkr();
//		// mainDe();
//		// mainEn();
//		// exploreModel("de");
////		exploreModel("ukr");
//	}
//
//
//	public static MorphParadigm getBestParadigm(Set<Flexion> seenflexes, Map<String, MorphParadigm> mpars) {
//		//score = ratio of seenFlexes(seen in particular paradigm) to the number of Flexes of this MorphParadigm
//		double score = 0.0;
//		boolean paradigmHasAllFoundFlexes = false;
//		MorphParadigm toReturn = null;
//		Set<String> seenFlexesSet = new HashSet();
//		for(Flexion f: seenflexes) seenFlexesSet.add(f.toString());
//		
//		for(MorphParadigm mpar: mpars.values()) {
//			double sum = 0.0;
//			Set<Flexion> mparFlexes = mpar.getFlexes();
//			boolean hasAll = true;
//			for(Flexion seenFlex: seenflexes) {
//				if(!mparFlexes.contains(seenFlex)) {
//					hasAll = false;
//					break;
//				}
//				
//			}
//			
//			for(Flexion mpFlex: mparFlexes) {
//				if(seenFlexesSet.contains(mpFlex.toString())) sum++;
//			}
//			if(sum < 2) continue;
//			
//			double newRatio = (double)sum/mpar.getFlexes().size();
//			if(paradigmHasAllFoundFlexes) {
//				if(hasAll) { //firstAll not first time -> compare score
//					paradigmHasAllFoundFlexes = true;
//					if(newRatio > score) {
//						score = newRatio;
//						toReturn = mpar;
//					}
//				}
//			}
//			else {
//				if(hasAll) { //first time hasAll - take it
//					paradigmHasAllFoundFlexes = true;
//					score = newRatio;
//					toReturn = mpar;
//				}
//				else if(newRatio > score) { // no hasAll at all -> compare score
//					score = newRatio;
//					toReturn = mpar;
//				}
//			}
//			
//			
//		}
//		return toReturn;
//	}
//
//
//}
//
