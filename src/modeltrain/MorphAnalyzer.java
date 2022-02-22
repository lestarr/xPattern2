package modeltrain;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import model.LetterTokModel;
import model.WordSequences;
import modelparts.Word;
import tokenizer.TestTokenizer;
import util.MapsOps;
import util.MyPair;

public class MorphAnalyzer {

	private static final String[] LETTERS = new String[] { "tsch", "sch", "ch", "ck", "qu" };
	private static final String SPLITTER = "#";

	public static LetterTokModel trainLetterTokModel(String lang, String fileIn, double mainFreq, int indexFreq) throws IOException {
		return trainLetterTokModel(lang, fileIn, mainFreq, 1, indexFreq);
	}
	
	
	// take words uniq, make suffixes and prefixes of length < 10 and freq list of
	// them
	// compare every pref of len l with l+1, same for suf; l+1/l -> cond. Prob.
	// /slice
	// it shows how much variability is explained with this letter/combination
	// compare slices after each other if there is a big step down - e.g. from 0.9
	// to 0.2
	// make cut or split; these places are for potential morphological components

	// take all cut points - make statistic on them - suf, pref separated

	private static LetterTokModel trainLetterTokModel(String lang, String fileIn, double mainFreq, int indexOfWord,
			int indexFreq) throws IOException {
		LetterTokModel ltmodel = new LetterTokModel(lang);

		// read ngram model
		String ngramFile = "model/ngram/" + lang + "-ngram-freq.model";
		File f = new File(ngramFile);
		Map<String, Double> ngramFreq;
		boolean makeNgramFreq = false;
		if (f.exists()) {
			ngramFreq = MapsOps.readWordFreqs(ngramFile, mainFreq, 0, 1, false, true);
			ltmodel.ngrams = ngramFreq;
		} else {
			System.out.println("training new ngram model");
			makeNgramFreq = true;
			ngramFreq = new HashMap<String, Double>();
		}

		Map<String, Double> prefs = new HashMap<String, Double>();
		Map<String, Double> sufs = new HashMap<String, Double>();

		Map<String, Double> wordFreqs = MapsOps.readWordFreqs(fileIn, mainFreq, indexOfWord, indexFreq, true, true);
		System.err.println(lang + ", ngram freqs: " + ngramFreq.size());
		System.err.println(lang + ", word freqs: " + wordFreqs.size());
		int c = 0;
		for (String w : wordFreqs.keySet()) {
			c++;
			if (c % 10000 == 0)
				System.out.println(c);
//			if(wordFreqs.get(w) < 2) continue; //here should be mainFreq!
			if (w.length() == 1)
				continue;

			List<String> subPrefs = getSubPrefs(w, lang);
			List<String> subSufs = getSubSufs(w, lang);

			for (String s : subPrefs) {
				MapsOps.addFreq(s, prefs);
			}
			for (String s : subSufs) {
				MapsOps.addFreq(s, sufs);
			}
			if (makeNgramFreq) {
				List<String> ngrams = getSubStrings(w, lang);
				for (String s : ngrams) {
					MapsOps.addFreq(s, ngramFreq);
				}
			}
		}
		ltmodel.prefs = prefs;
		ltmodel.sufs = sufs;
		ltmodel.ngrams = ngramFreq;
		if (makeNgramFreq) {
			MapsOps.printMap(ngramFreq, ngramFile);
		}
		return ltmodel;
	}

	private static List<String> getSubPrefs(String w, String lang) {
		List<String> tokens = getTokens(w, lang);
		List<String> substrings = new ArrayList<String>();
		String prev = "";
		for (String t : tokens) {
			if(t.length() == 0) continue;

			prev = prev + t;
			substrings.add(prev);
		}
		return substrings;
	}

	private static List<String> getSubSufs(String w, String lang) {
		List<String> tokens = getTokens(w, lang);
		List<String> substrings = new ArrayList<String>();
		String prev = "";
		for (int i = tokens.size() - 1; i >= 0; i--) {
			String t = tokens.get(i);
			if(t.length() == 0) continue;

			prev = t + prev;
			substrings.add(prev);
		}
		return substrings;
	}
	
	/**
	 * gets all substrings from word, e.g.: part: _p,_pa,_par,_part, _part_,
	 * p,pa,par,part, part_,a,ar,art,art_,r,rt,rt_,t,t_
	 * 
	 * @param w
	 * @param lang
	 * @return
	 */
	private static List<String> getSubStrings(String w, String lang) {
		List<String> tokens = getTokens(w, lang);
		List<String> substrings = new ArrayList<String>();
		String prev = "";
		for (String t : tokens) {
			if (prev.equals("")) {
				prev = t;
				continue;
			}
			substrings.add(prev);
			prev = prev + t;
		}
		// for (int i = 0; i < tokens.size(); i++) {
		while (tokens.size() > 1) {
			tokens.remove(0);
			prev = "";
			for (String t : tokens) {
				if (prev.equals("")) {
					prev = t;
					substrings.add(prev);
					continue;
				}
				prev = prev + t;
				substrings.add(prev);
			}
		}

		return substrings;
	}
	
	static public List<String> getTokens(String w, String lang) {
		List<String> tokens = new ArrayList<String>();
		StringTokenizer st;
		if (lang.equals("de"))
			st = TestTokenizer.getLetterTokenizer(w, LETTERS);
		else
			st = TestTokenizer.getLetterTokenizer(w, new String[] {});
		while (st.hasMoreTokens()) {
			String letter = st.nextToken();
			tokens.add(letter);
		}
		return tokens;
	}
	
	public static void computeRootFlexOneWord(Word inputword, WordSequences wsmodel, LetterTokModel ltmodel, boolean print) {
		computeRootFlexInitialOneWOrd(inputword, wsmodel, ltmodel);
		if(inputword.prefRootFlex == null || inputword.sufRootFlex == null) return;
		if(print) System.out.println(inputword.prefRootFlex + "\t" + inputword.sufRootFlex);
		
		//collectMoreRootsOneWord(inputword, wsmodel);
		//if(print) System.out.println(inputword.getRoot() + "\t" + inputword.getFlex());
		
		computeMoreRootsOneWord(inputword, wsmodel);
		if(print) System.out.println(inputword.getRoot() + "\t" + inputword.getFlex());
		
		computeNewRootFlexOneWord(inputword, wsmodel, true);
		if(print) System.out.println(inputword.getRoot() + "\t" + inputword.getFlex());
		
		computeNewRootFlexOneWord(inputword, wsmodel, false);
		if(print) System.out.println(inputword.getRoot() + "\t" + inputword.getFlex());
	}
	
	public static void computeRootFlexInitial(WordSequences wsmodel, LetterTokModel ltmodel, int freqTHH) {
		for(Word inputword: wsmodel.idx().getSortedWords()) {
			if(inputword.freq() < freqTHH) continue;
			computeRootFlexInitialOneWOrd(inputword, wsmodel, ltmodel);
			
		}
	}
	
	private static void computeRootFlexInitialOneWOrd(Word inputword, WordSequences wsmodel, LetterTokModel ltmodel) {
		List<MyPair> slicesPref = MorphAnalyzer.getWordCondFreqAnalysisSubstrings(wsmodel.getLang(), ltmodel,
				inputword.toString(), "pref");
//		boolean isCmp = isCompound(slicesPref);

		List<MyPair> prefCuts = getRootFlexFromPREFIXSlice(inputword.toString(), slicesPref);
		List<MyPair> slicesSuf = MorphAnalyzer.getWordCondFreqAnalysisSubstrings(wsmodel.getLang(), ltmodel,
				inputword.toString(), "suf");
		MyPair rootFlexPref = prefCuts.get(prefCuts.size() - 1);
		// boolean isCmpSuff = isCompoundSuf(slicesSuf);
		MyPair rootFlexSuf = getRootFlexFromSUFFIXSlice(inputword.toString(), slicesSuf);
		String suffFlex = rootFlexSuf.second.replaceAll("_", "");
		String suffRoot = rootFlexSuf.first.replaceAll("_", ""); // is not used here, redundand because of prefFlex == sufFlex, so the roots are also equal
		String prefFlex = rootFlexPref.second.replaceAll("_", "");
		String prefRoot = rootFlexPref.first.replaceAll("_", "");
		MapsOps.addStringToValueSet(prefRoot, wsmodel.idx().seenRootsNotChecked, prefFlex);
		MapsOps.addStringToValueSet(suffRoot, wsmodel.idx().seenRootsNotChecked, suffFlex);
		
		MapsOps.addFreq(prefFlex, wsmodel.idx().seenFlexesNotChecked);
		MapsOps.addFreq(suffFlex, wsmodel.idx().seenFlexesNotChecked);
		
		if (prefFlex.equals(""))
			prefFlex = "_";
		if (suffFlex.equals(""))
			suffFlex = "_";
		// if(!prefRoot.equals(inputword.toString()) && prefFlex.equals(suffFlex)) {
		
		inputword.prefRootFlex = new MyPair(prefRoot, prefFlex);
		inputword.sufRootFlex = new MyPair(suffRoot, suffFlex);
		
		if (prefFlex.equals(suffFlex)) {
			inputword.setRoot(prefRoot);
			inputword.setFlex(prefFlex);
			wsmodel.idx().seenRoots.add(prefRoot);
			MapsOps.addFreq(prefFlex, wsmodel.idx().seenFlexes);
//			System.out.println("SAME INIT flex\t" + prefRoot+ "\t" + prefFlex );
		}else
			setSuffix(inputword, prefFlex, suffFlex);
	}
	
	private static void setSuffix(Word inputword, String prefFlex, String suffFlex) {
		if(prefFlex.equals(suffFlex)) return;
		if(prefFlex.equals("_")) 
			inputword.setSuffix(suffFlex);
		if(suffFlex.equals("_"))
			inputword.setSuffix(prefFlex);
	}


	/**
	 * computes additional flexes and roots for cases were they were not the same but can be mapped:
	 * SAME SEEN_PREF flex	_гонщик а_ 1.0		_гонщ ика_ 0.2005
	 * SAME SEEN_SUFF flex	_котеджі в_ 1.0		_котедж ів_ 0.6141
	 * 
	 * writes found roots and flexes as seenRoots and seenFlexes
	 * 
		 * @param wsmodel
	 * @param ltmodel
	 */
	public static void collectMoreRoots(WordSequences wsmodel, int freqTHH) {
		for(Word inputword: wsmodel.idx().getSortedWords()) {
			if(inputword.freq() < freqTHH) continue;
			if(inputword.prefRootFlex == null || inputword.sufRootFlex == null) continue;
			collectMoreRootsOneWord(inputword, wsmodel);
		}
	}
	
	private static void collectMoreRootsOneWord(Word inputword, WordSequences wsmodel) {
		//look for other flexes, roots and for suffixes
		String suffFlex = inputword.sufRootFlex.second.replaceAll("_", "");
		String suffRoot = inputword.sufRootFlex.first.replaceAll("_", "");
		String prefFlex = inputword.prefRootFlex.second.replaceAll("_", "");
		String prefRoot = inputword.prefRootFlex.first.replaceAll("_", "");
		if(prefFlex.equals("")) prefFlex = "_";
		if(suffFlex.equals("")) suffFlex = "_";

		if(inputword.getFlex() != null && prefFlex.equals(suffFlex)) {
			return; // means: there are roots and flexes already written 
		}
		if(wsmodel.idx().seenRoots.contains(prefRoot) && wsmodel.idx().seenFlexes.containsKey(prefFlex) 
				//suffFlex not seen in SAME OR its freq is < as freq of prefFlex
				&& (!wsmodel.idx().seenFlexes.containsKey(suffFlex) || wsmodel.idx().seenFlexes.get(prefFlex) > wsmodel.idx().seenFlexes.get(suffFlex)) ) {
			wsmodel.idx().seenRoots.add(prefRoot);
			MapsOps.addFreq(prefFlex, wsmodel.idx().seenFlexes);
		}
		else if(wsmodel.idx().seenRoots.contains(suffRoot) && wsmodel.idx().seenFlexes.containsKey(suffFlex) 
				//suffFlex not seen in SAME OR its freq is < as freq of prefFlex
				&& (!wsmodel.idx().seenFlexes.containsKey(prefFlex) || wsmodel.idx().seenFlexes.get(suffFlex) > wsmodel.idx().seenFlexes.get(prefFlex)) ) {
			wsmodel.idx().seenRoots.add(suffRoot);
			MapsOps.addFreq(suffFlex, wsmodel.idx().seenFlexes);
		}
		else if(wsmodel.idx().seenRootsNotChecked.containsKey(prefRoot) && wsmodel.idx().seenRootsNotChecked.get(prefRoot).size() > 1
				&& wsmodel.idx().seenRootsNotChecked.containsKey(suffRoot) && wsmodel.idx().seenRootsNotChecked.get(suffRoot).size() == 1
				&& wsmodel.idx().seenFlexesNotChecked.containsKey(prefFlex) && wsmodel.idx().seenFlexesNotChecked.get(prefFlex) > 2) {  
				// do not take very seldom flexes!!!
				// >2 because of roots with Capitals

			wsmodel.idx().seenRoots.add(prefRoot);
			MapsOps.addFreq(prefFlex, wsmodel.idx().seenFlexes);
//			System.out.println("THIS WAS ADDED PREF\t" + inputword.prefRootFlex.toString()+ "\t" + inputword.sufRootFlex.toString());
		}
		else if(wsmodel.idx().seenRootsNotChecked.containsKey(suffRoot) && wsmodel.idx().seenRootsNotChecked.get(suffRoot).size() > 1
				&& wsmodel.idx().seenRootsNotChecked.containsKey(prefRoot) && wsmodel.idx().seenRootsNotChecked.get(prefRoot).size() == 1
				&& wsmodel.idx().seenFlexesNotChecked.containsKey(suffFlex) && wsmodel.idx().seenFlexesNotChecked.get(suffFlex) > 2) { // do not take very seldom flexes!!!

			wsmodel.idx().seenRoots.add(suffRoot);
			MapsOps.addFreq(suffFlex, wsmodel.idx().seenFlexes);
//			System.out.println("THIS WAS ADDED SUFF\t" + inputword.prefRootFlex.toString()+ "\t" + inputword.sufRootFlex.toString());
		}
	}
	
	public static void computeMoreRoots(WordSequences wsmodel, int freqTHH) {
		for(Word inputword: wsmodel.idx().getSortedWords()) {
			if(inputword.freq() < freqTHH) continue;
			if(inputword.prefRootFlex == null || inputword.sufRootFlex == null) continue;
			computeMoreRootsOneWord(inputword, wsmodel);
		}
	}
	
	private static void computeMoreRootsOneWord(Word inputword, WordSequences wsmodel) {
		//look for other flexes, roots and for suffixes
		
		String suffFlex = inputword.sufRootFlex.second.replaceAll("_", "");
		String suffRoot = inputword.sufRootFlex.first.replaceAll("_", "");
		String prefFlex = inputword.prefRootFlex.second.replaceAll("_", "");
		String prefRoot = inputword.prefRootFlex.first.replaceAll("_", "");
		if(prefFlex.equals("")) prefFlex = "_";
		if(suffFlex.equals("")) suffFlex = "_";

		if(inputword.getFlex() != null && prefFlex.equals(suffFlex)) {
			return; // means: there are roots and flexes already written 
		}
		if(wsmodel.idx().seenRoots.contains(prefRoot) && wsmodel.idx().seenFlexes.containsKey(prefFlex) 
				//suffFlex not seen in SAME OR its freq is < as freq of prefFlex
				&& (!wsmodel.idx().seenFlexes.containsKey(suffFlex) || wsmodel.idx().seenFlexes.get(prefFlex) > wsmodel.idx().seenFlexes.get(suffFlex)) ) {
			inputword.setRoot(prefRoot);
			inputword.setFlex(prefFlex);
//			System.out.println("SAME PREF post INIT\t" + inputword.prefRootFlex.toString()+ "\t" + inputword.sufRootFlex.toString()	);
		}
		else if(wsmodel.idx().seenRoots.contains(suffRoot) && wsmodel.idx().seenFlexes.containsKey(suffFlex) 
				//suffFlex not seen in SAME OR its freq is < as freq of prefFlex
				&& (!wsmodel.idx().seenFlexes.containsKey(prefFlex) || wsmodel.idx().seenFlexes.get(suffFlex) > wsmodel.idx().seenFlexes.get(prefFlex)) ) {
			inputword.setRoot(suffRoot);
			inputword.setFlex(suffFlex);
//			System.out.println("SAME SUFF post INIT\t" + inputword.prefRootFlex.toString()+ "\t" + inputword.sufRootFlex.toString()			);
		}
		else if(wsmodel.idx().seenRootsNotChecked.containsKey(prefRoot) && wsmodel.idx().seenRootsNotChecked.get(prefRoot).size() > 1
				&& wsmodel.idx().seenRootsNotChecked.containsKey(suffRoot) && wsmodel.idx().seenRootsNotChecked.get(suffRoot).size() == 1
				&& wsmodel.idx().seenFlexesNotChecked.containsKey(prefFlex) && wsmodel.idx().seenFlexesNotChecked.get(prefFlex) > 2) {  // do not take very seldom flexes!!!

			inputword.setRoot(prefRoot);
			inputword.setFlex(prefFlex);
		}
		else if(wsmodel.idx().seenRootsNotChecked.containsKey(suffRoot) && wsmodel.idx().seenRootsNotChecked.get(suffRoot).size() > 1
				&& wsmodel.idx().seenRootsNotChecked.containsKey(prefRoot) && wsmodel.idx().seenRootsNotChecked.get(prefRoot).size() == 1
				&& wsmodel.idx().seenFlexesNotChecked.containsKey(suffFlex) && wsmodel.idx().seenFlexesNotChecked.get(suffFlex) > 2) { // do not take very seldom flexes!!!

			inputword.setRoot(suffRoot);
			inputword.setFlex(suffFlex);
		}
	}

	/**
	 * Transitions: ними	{ими=554.0, и=2.0, ними=154.0}
	 * 
	 * writes found transitions as seenSuffixes
	 * 
	 * @param wsmodel
	 * @param ltmodel
	 */
	public static void computeRootFlexSuffixTransitions(WordSequences wsmodel, int freqTHH) {
		for(Word inputword: wsmodel.idx().getSortedWords()) {
			if(inputword.freq() < freqTHH) continue;
			if(inputword.prefRootFlex == null || inputword.sufRootFlex == null) continue;
			
			String suffFlex = inputword.sufRootFlex.second.replaceAll("_", "");
			String suffRoot = inputword.sufRootFlex.first.replaceAll("_", "");
			String prefFlex = inputword.prefRootFlex.second.replaceAll("_", "");
			String prefRoot = inputword.prefRootFlex.first.replaceAll("_", "");
			if(prefFlex.equals("")) prefFlex = "_";
			if(suffFlex.equals("")) suffFlex = "_";
			
			if(prefFlex.equals(suffFlex) ) { //for words where pref roots and suff roots were always the same
				//transitions for same flexes in suffs and prefs method
				saveSuffixTransitions(wsmodel, prefFlex, prefFlex);
				continue; // means: there are roots and flexes already written by 
			}
			if( inputword.getRoot() == null) continue;
			
			//look for other flexes, roots and for suffixes
			if( inputword.getRoot().equals(prefRoot)) {
				saveSuffixTransitions(wsmodel, prefFlex, suffFlex);
			}
			else if(inputword.getRoot().equals(suffRoot)) {
				saveSuffixTransitions(wsmodel, suffFlex, prefFlex);
			}
//			System.out.println(inputword);
		}
	}
	
	private static void saveSuffixTransitions(WordSequences wsmodel, String prefferedFlex, String otherFlex) {
		Map<String,Double> preferredFreqMap = new HashMap<>();
		if(!wsmodel.idx().seenSuffixes.containsKey(otherFlex)) wsmodel.idx().seenSuffixes.put(otherFlex, preferredFreqMap);
		preferredFreqMap = wsmodel.idx().seenSuffixes.get(otherFlex);
		MapsOps.addFreq(prefferedFlex, preferredFreqMap);
	}
	
	public static void computeNewRootFlex(WordSequences wsmodel, boolean useOnlySingleSuffixTransition, int freqTHH) {
		for (Word inputword : wsmodel.idx().getSortedWords()) {
			if (inputword.freq() < freqTHH)
				continue;
			if (inputword.prefRootFlex == null || inputword.sufRootFlex == null)
				continue;
			computeNewRootFlexOneWord(inputword, wsmodel, useOnlySingleSuffixTransition);
		}
	}
	
	private static void computeNewRootFlexOneWord(Word inputword, WordSequences wsmodel, boolean useOnlySingleSuffixTransition) {
		StringBuffer sbuf = new StringBuffer();
		String suffFlex = inputword.sufRootFlex.second.replaceAll("_", "");
		String suffRoot = inputword.sufRootFlex.first.replaceAll("_", "");
		String prefFlex = inputword.prefRootFlex.second.replaceAll("_", "");
		String prefRoot = inputword.prefRootFlex.first.replaceAll("_", "");
		if (prefFlex.equals(""))
			prefFlex = "_";
		if (suffFlex.equals(""))
			suffFlex = "_";

		// Logik:
		// if prefFlex & sufFlex the same -> check transitions -> if possible transition
		// -> check newRoot
		// if not the same: check prefRoot -> check suffFlex transition -> if after
		// trans prefFlex - take pref
		// if not pref -> check suffRoot -> same Logik as in pref
		// if no root - check longer for transitions -> if so -> check new Root
		boolean wasCorrected = false;
		if (!prefRoot.equals(inputword.toString()) && prefFlex.equals(suffFlex) //pref == suf
				&& wsmodel.idx().seenSuffixes.containsKey(prefFlex)) {
			// try to correct suffix
			MyPair root_flex_pair = checkBestFlexFromTransitions(wsmodel, prefFlex, prefRoot, useOnlySingleSuffixTransition, inputword);

			if (root_flex_pair.first.length() != prefRoot.length()) {
				correctRootFlex(inputword, root_flex_pair);
				inputword.prefRootFlex = root_flex_pair;
				inputword.sufRootFlex = root_flex_pair;
				wasCorrected = true;
				sbuf.append("SAME CORRECTED flex\t" + inputword.prefRootFlex.toString() + ":\t" + root_flex_pair.first + "\t" + root_flex_pair.second + "\n");
			}
			else
				sbuf.append("SAME NOT CORRECTED flex\t" + inputword.prefRootFlex.toString() + "\t"	+ root_flex_pair.toString() + "\n");
		} else if (!prefFlex.equals(suffFlex)) { //pref != suff
				MyPair root_flex_pair = checkBestFlexFromTransitions(wsmodel, prefFlex, prefRoot, useOnlySingleSuffixTransition, inputword);
				if (root_flex_pair.first.length() != prefRoot.length()) {
					String newRoot = root_flex_pair.first;
					String newFlex = root_flex_pair.second;
					if (wsmodel.idx().seenRoots.contains(newRoot) || newRoot.equals(suffRoot)) {
						wasCorrected = true;
						correctRootFlex(inputword, root_flex_pair);
						inputword.prefRootFlex = root_flex_pair;
						sbuf.append("PREF CORRECTED flex\t" + inputword.prefRootFlex.toString() + ":\t"	+ root_flex_pair.first + "\t" + root_flex_pair.second + "\n");
					} else if (wsmodel.idx().seenRoots.contains(prefRoot)
							&& !wsmodel.idx().seenRoots.contains(newRoot))
						sbuf.append("PREF NOT CORRECTED flex\t" + inputword.prefRootFlex.toString() + ":\t"	+ root_flex_pair.first + "\t" + root_flex_pair.second + "\n");
					else {
						wasCorrected = true;
						correctRootFlex(inputword, root_flex_pair);
						inputword.prefRootFlex = root_flex_pair;
						sbuf.append("PREF CORRECTURE\t" + inputword.prefRootFlex.toString() + ":\t"	+ root_flex_pair.first + "\t" + root_flex_pair.second + "\n");
					}
				}
				else if (!wasCorrected ) { //check suffix
					root_flex_pair = checkBestFlexFromTransitions(wsmodel, suffFlex, suffRoot, useOnlySingleSuffixTransition, inputword);
					if (root_flex_pair.first.length() != suffRoot.length()) {
						String newRoot = root_flex_pair.first;
						String newFlex = root_flex_pair.second;
						if (wsmodel.idx().seenRoots.contains(newRoot) || newRoot.equals(prefRoot)) {
							wasCorrected = true;
							correctRootFlex(inputword, root_flex_pair);
							inputword.sufRootFlex = root_flex_pair;
							sbuf.append("SUFF CORRECTED flex\t" + inputword.sufRootFlex.toString() + ":\t"	+ root_flex_pair.first + "\t" + root_flex_pair.second + "\n");
						} else if (wsmodel.idx().seenRoots.contains(suffRoot)
								&& !wsmodel.idx().seenRoots.contains(newRoot))
							sbuf.append("SUFF NOT CORRECTED flex\t" + inputword.prefRootFlex.toString() + ":\t"	+ root_flex_pair.first + "\t" + root_flex_pair.second + "\n");
						else {
							wasCorrected = true;
							correctRootFlex(inputword, root_flex_pair);
							inputword.sufRootFlex = root_flex_pair;
							sbuf.append("SUFF CORRECTURE\t" + inputword.sufRootFlex.toString() + ":\t"	+ root_flex_pair.first + "\t" + root_flex_pair.second + "\n");
						}
					}
				}
			if (!wasCorrected) {
//				sbuf.append("STILL NOT EQUAL\t" + inputword.prefRootFlex.toString() + "\t"	+ inputword.sufRootFlex.toString() + "\n");
			}
		} else {
			sbuf.append(	"OTHERS\t" + inputword.prefRootFlex.toString() + "\t" + inputword.sufRootFlex.toString() + "\n");
		}
	}
		
	private static void correctRootFlex(Word inputword, MyPair root_flex_pair) {
		inputword.setRoot(root_flex_pair.first);
		inputword.setFlex(root_flex_pair.second);
	}
	
	/**
	 * returns suffix+flex
	 */
	private static MyPair checkBestFlexFromTransitions(WordSequences wsmodel, String flex, String root, boolean useOnlySingleTransition, Word w) {
		Map<String,Map<String,Double>> transitions = wsmodel.idx().seenSuffixes;
		if(!transitions.containsKey(flex)) return new MyPair(root, flex);
		Map<String,Double> transitionsFound = transitions.get(flex);
		//find not possible suffix tramsformations- those, that are not suffixes of the particular word
		Set<String> butnot = new HashSet<>();
		String word = root+flex;
		for(String transitionFlex: transitionsFound.keySet()) {
			if(transitionFlex.equals("_")) transitionFlex = "";
			if(!word.endsWith(transitionFlex)) butnot.add(transitionFlex);
		}
		//retrieves most frequent transition
		String bestFlex = MapsOps.getFirstButNOT(transitionsFound, butnot).first;
		if(flex.equals("_") && bestFlex.equals("_"))
			return new MyPair(root, bestFlex);
//		if(bestFlex.equals("_")) bestFlex = ""; // BUG
		
		if(useOnlySingleTransition) {
			if( transitionsFound.size() == 1) 
				return computeNewRootFlex(flex, root, bestFlex, w);
			else
				return new MyPair(root, flex);
		}

		return computeNewRootFlex(flex, root, bestFlex, w);
	}
	
	private static MyPair computeNewRootFlex(String flex, String root, String bestFlex, Word w) {
		String word = root+flex;
		int bestFlexLength = bestFlex.length();
		if(bestFlex.equals("_")) bestFlexLength = 0;
		
		int flexLength = flex.length();
		if(flex.equals("_")) flexLength = 0;
		
		if(bestFlexLength >= word.length()) return  new MyPair(root, flex);
		
		if(bestFlexLength == flexLength) return new MyPair(root, bestFlex);
		else if(bestFlexLength < flexLength) {
			String suffix = flex.substring(0,(flexLength-bestFlexLength));
			String newRoot = word.substring(0, word.length() - flexLength) + suffix;
			String newFlex = bestFlex;
			return new MyPair(newRoot, newFlex);
		}else { //bestFlex is longer than suffFlex - should be seldom
			String newRoot = word.substring(0, word.length() - bestFlexLength);
			String newFlex = bestFlex;
			return new MyPair(newRoot, newFlex);
		}
	}
	
	
	public static List<MyPair> getWordCondFreqAnalysisSubstrings(String lang, LetterTokModel ltmod, String word,
			String sufOrPref) {
		if(!word.startsWith("_")) word = "_" + word;
		if(!word.endsWith("_")) word = word + "_";
		// key = pair: f=prefix, s = sufix + slice(freq(pref+1)/freq(pref))
		List<MyPair> slices = new ArrayList<MyPair>();
		List<MyPair> substrPairs;
		Map<String, Double> sufPrefFreq;

		if (sufOrPref.startsWith("suf")) {
			substrPairs = getSubstringPairs(getSubSufs(word, lang));
			sufPrefFreq = ltmod.sufs;
		} else {
			substrPairs = getSubstringPairs(getSubPrefs(word, lang));
			sufPrefFreq = ltmod.prefs;
		}

		for (MyPair p : substrPairs) {
			double slice = computeSlice(p, sufPrefFreq);
			if(word.length() < p.second.length()) {
				System.out.println(word);
				continue;
			}
			if (sufOrPref.startsWith("suf"))
				slices.add(new MyPair(word.substring(0, word.length() - p.second.length()), word.substring(word.length() - p.second.length()), slice));
			else {
				slices.add(new MyPair(word.substring(0, p.second.length()), word.substring(p.second.length()), slice));
			}
		}
		return slices;
	}
	
	private static List<MyPair> getRootFlexFromPREFIXSlice(String inputword, List<MyPair> slices) {
		MyPair prev = null;
		List<MyPair> cuts = new ArrayList<>();
		boolean isGoingUp = false;
		for(MyPair mp: slices) {
			if(prev == null) {
				prev = mp;
				continue;
			}
//			if(prev.freq > 0.59 && mp.freq < (prev.freq) && !foundZeroFrequencySlice
			if(prev.freq > 0.1 && mp.freq < (prev.freq) 
					&& isGoingUp
					) { //benchmark cuts 4, actual, maybe the best
				cuts.add(prev);
			}
			//check second cut
			if(prev.freq > 0.59 && mp.freq < (prev.freq) 
					&& !isGoingUp && cuts.size() > 0
					&& (cuts.get(cuts.size()-1).freq - prev.freq) < (prev.freq - mp.freq) //situation: one cut after other cut, second cut is bigger-> could be suff
					&& mp.freq < 0.1

					) { 
				cuts.add(prev);
			}
			//end check
			
			if(prev.freq <= mp.freq) isGoingUp = true;
			else isGoingUp = false;
			prev = mp;
		}
		if(cuts.size() == 0) cuts.add(new MyPair(inputword, "_"));
		return cuts;
	}
	
	private static MyPair getRootFlexFromSUFFIXSlice(String inputword, List<MyPair> slices) {
		MyPair prev = null;
		prev = null;
		for (int i = 0; i < Math.min(6, slices.size()); i++) {
			if(slices.size() < 5) break;
			if(i > slices.size()-3) break; //don't check more than 5 letters from the back
			MyPair mp = slices.get(i);
			if(mp.freq < 0) break; //if freq = -1 -> don't analyze potential flex - it is irrational
			if(prev == null) {
				prev = mp;
				continue;
			}
//			if(prev.freq > 0.15 && mp.freq < 0.05) { //old rule
			if(prev.freq > mp.freq ) { 
				return prev; // add only 1 first cut from the back of the word
			}
			prev = mp;
		}
		return new MyPair(inputword, "_");
	}
	
//	private static boolean checkSlicesForZeroFrequency(List<MyPair> slices) {
//		/* checks if there were zero frequency in slices. if so - cut should not be determined, because we do not have enough statistics for those n-grams
//		  _п	ідмінити_	0.14338399089154003
//		_пі	дмінити_	0.10158812359744519
//		_під	мінити_	0.6618521665250637
//		_підм	інити_	0.010269576379974325
//		_підмі	нити_	-1.0
//		_підмін	ити_	-1.0
//		_підміни	ти_	-1.0
//		_підмінит	и_	-1.0
//		_підмінити	_	-1.0
//		_підмінити_		-1.0
//		*/
//		 for(MyPair mp: slices)
//			 if(mp.freq < 0) return true;
//		return false;
//	}
	
	private static List<MyPair> getSubstringPairs(List<String> substrings) {
		List<MyPair> pairlist = new ArrayList<MyPair>();
		String prev = "";
		for (String s : substrings) {
			if (prev.equals("")) {
				prev = s;
				continue;
			} else {
				MyPair p = new MyPair(prev, s);
				pairlist.add(p);
				prev = s;
			}
		}
		return pairlist;
	}
	
	// computes the cond prob from longer ngram if given shorter ngram: haus/hau
	private static double computeSlice(MyPair twoNgrams, Map<String, Double> sufPrefFreq) {
		if (!sufPrefFreq.containsKey(twoNgrams.first) || !sufPrefFreq.containsKey(twoNgrams.second))
			return 1;
		else {
			double firstFreq = sufPrefFreq.get(twoNgrams.first);
			double secondFreq = sufPrefFreq.get(twoNgrams.second);
			double slice = secondFreq / firstFreq; // longer/shorter
			return slice;
		}
	}
	
	public static String getCutsAndSlices(String lang, LetterTokModel ltmod, String w) {
		if(!w.startsWith("_")) w = "_" + w;
		if(!w.endsWith("_")) w = w + "_";
		List<MyPair> slices = getWordCondFreqAnalysisSubstrings(lang, ltmod, w, "pref");
		StringBuffer sb = new StringBuffer();
		for (MyPair p : slices) {
			sb.append(p.first+"\t"+p.second+"\t"+p.freq+"\n");
		}
		sb.append("\n");
		List<MyPair> prefcuts = getRootFlexFromPREFIXSlice(w, slices);
		sb.append(prefcuts.toString() + "\n");
		// same with suffix
		slices = getWordCondFreqAnalysisSubstrings(lang, ltmod, w, "suf");
		for (MyPair p : slices) {
			sb.append(p.first+"\t"+p.second+"\t"+p.freq+"\n");
		}
		MyPair sufcut = getRootFlexFromSUFFIXSlice(w, slices);
		sb.append(sufcut.toString() + "\n");
		return sb.toString();
	}
	
}

