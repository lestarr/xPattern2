package modelparts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import model.WordSequences;
import util.MapsOps;
import util.MyPair;

public class MorphSemParadigms {
	
	public static void trainParadigms(WordSequences model, int freqThh) {
		Set<String> seenRoots = new HashSet<>();
		MorphParadigms mpars = new MorphParadigms();

		fillWordFlexParadigmWithContextFLexes(model, freqThh, seenRoots, mpars);
		
		printMpars(mpars);
		
		MorphParadigms  mparsMainTailRecomputed = recomputeFlexesMainTail(mpars, model);
		
		List<MorphParadigm> mparsList = mparsMainTailRecomputed.toList(1);
		
		printMpars(mparsList);

		mparsList = combineParadigms(mparsList, 1); //first
		
		printMpars(mparsList);
		
		mparsList = combineParadigms(mparsList, 2); //second: longer prefix, test paradigm MUST have all the endings of MAIN paradigm
		
		printMpars(mparsList);
		
		mparsList = combineParadigms(mparsList, 3); //third: if test paradigm completely in MAIN && test > 50% of main - combine test into main
		
		printMpars(mparsList);
		
		mparsList = combineParadigms(mparsList, 4); //fourth: if main paradigm completely in test && main > 50% of test - combine main into test
		
		List<MorphParadigm> cleanedMparList = new ArrayList<>();
		for(MorphParadigm mpar: mparsList)
			if(mpar.getFreq() > 2) cleanedMparList.add(mpar);
		model.setMorphPars(cleanedMparList);
	}
	
	public static void trainOneRoot(Root root, WordSequences model) {
		List<Map<String,Double>> flexFreqMapAll = getWinningFlexMap(root, model, true);
			root.trainedflexes = flexFreqMapAll.get(0);
			root.tailflexes = flexFreqMapAll.get(1);
	}

	private static void fillWordFlexParadigmWithContextFLexes(WordSequences model, int freqThh, Set<String> seenRoots, MorphParadigms mpars) {
		for(Word w: model.idx().getSortedWords()) {
			if(w.toString().length() < 4) {
//				try {					throw new MyException("too short word", w); }
//				catch (MyException e) {  }
				continue;
			}
			if(w.freq() < freqThh) continue;
			if(w.getRoot() == null) continue;
			String rootString = w.getRoot();
			if(seenRoots.contains(rootString)) continue;
			seenRoots.add(rootString);
			Root root = model.idx().getRoot(w.getRoot());
//			System.out.println(w.toString() + "\t" + w.freq());
			List<Map<String,Double>> flexFreqMapAll = getWinningFlexMap(root, model, true);
			cleanParadigm(flexFreqMapAll);
			Set<Flexion> flexes = Flexion.getFlexionSetFromString(model, flexFreqMapAll.get(0).keySet());
			MorphParadigm mpar = mpars.getParadigm(flexes, flexFreqMapAll.get(0) ,model);
			mpar.addTailFlexes(flexFreqMapAll.get(1));	
		}
	}


	private static MorphParadigms recomputeFlexesMainTail(MorphParadigms mpars, WordSequences model) {
		MorphParadigms mparsToReturn = new MorphParadigms();
		for(MorphParadigm mpar: mpars.paradigms.values()) {
			recomputeMparFlexesMainTail(mparsToReturn, mpar.getFlexFreqMap(), mpar.tailFlexesFreq, model);
		}
		return mparsToReturn;
	}

	private static MorphParadigm recomputeMparFlexesMainTail(MorphParadigms mpars, Map<String, Double> main, Map<String, Double> tail, WordSequences model) {
		Map<String, Double> allFlexes = new HashMap<>();
		allFlexes.putAll(main);
		allFlexes.putAll(tail);
		
		double bestFlexFreq = MapsOps.getFirst(main).freq;
		double thh = bestFlexFreq / 5;
		
		main = new HashMap<>();
		tail = new HashMap<>();
		
		for(String flex: allFlexes.keySet()) {
			double freq = allFlexes.get(flex);
			if(freq > thh ) main.put(flex, freq);
			else tail.put(flex, freq);
		}
		
		Set<Flexion> flexes = Flexion.getFlexionSetFromString(model, main.keySet());

		MorphParadigm mpar = mpars.getParadigm(flexes, main , model);
		mpar.addTailFlexes(tail);
		return mpar;
	}

	private static List<MorphParadigm> combineParadigms(List<MorphParadigm> mparsList, int runCount) {
		List<MorphParadigm> toReturn = new ArrayList<MorphParadigm>();
		
		Map<MorphParadigm, Double> mparFreq = new HashMap<>();
		for(MorphParadigm mpar: mparsList) {
			mparFreq.put(mpar, mpar.getFreq());
		}
		
		Set<MorphParadigm> seen = new HashSet<>();
		Set<MorphParadigm> wasAdded = new HashSet<>();

		SortedMap<MorphParadigm,Double> smap = MapsOps.getSortedMapObject(mparFreq);
		for(MorphParadigm mparMain: smap.keySet()) {
			if(mparMain.getFlexes().size() == 0) continue;
			if(seen.contains(mparMain)) 
				continue;
			if(wasAdded.contains(mparMain)) 
				continue;
			for(MorphParadigm mparTest: smap.keySet()) {
				if(mparTest.getFlexes().size() == 0) continue;
				if(mparTest.getSortedFlex().equals(mparMain.getSortedFlex())) continue;
				if(seen.contains(mparTest)) 	continue;
				
				if(canBeCombined(mparMain, mparTest, runCount)) {
					MorphParadigm.combine(mparMain, mparTest);
					seen.add(mparTest);
					wasAdded.add(mparTest);
				}
			}
			seen.add(mparMain);
		}
		for(MorphParadigm mpar: smap.keySet()) {
			if(wasAdded.contains(mpar)) continue;
			toReturn.add(mpar);
		}
		return toReturn;
	}

	private static boolean canBeCombined(MorphParadigm main, MorphParadigm test, int runCount) {
		//check if testParadigm has all the Flexes of the main, check also in tail of the testParadigm
		Set<String> mainFlexesMissedInTest = new HashSet<>();
		for(String f: main.getFlexFreqMap().keySet()) {
			if(!test.getFlexFreqMap().containsKey(f) ) 
				mainFlexesMissedInTest.add(f);
		}
		
		// check if testParadigm has additional Flexes, if so, check in raun = 1 AND 2, if they are in the tail of the main
		Set<String> testFlexesMissedInMain = new HashSet<>();
		for (String f : test.getFlexFreqMap().keySet()) {
			if (!main.getFlexFreqMap().containsKey(f))
				testFlexesMissedInMain.add(f);
		}

		// case 3
		if(runCount == 3 ) {
			if(mainFlexesMissedInTest.size() > 0 && testFlexesMissedInMain.size() == 0
					&& (main.getFlexFreqMap().size() / test.getFlexFreqMap().size() < 2) ) // test completely in main
				return true;
			else return false;
		}
		// case 4
		if(runCount == 4 ) {
			if(testFlexesMissedInMain.size() > 0 && mainFlexesMissedInTest.size() == 0
					&& (test.getFlexFreqMap().size() / main.getFlexFreqMap().size() < 2) ) // main completely in test
				return true;
			else return false;
		}
			
		// cases 1 and 2
		if(runCount == 2 && mainFlexesMissedInTest.size() > 0) return false; // if still test paradigm does not have all the flexes of main par
		int prefixLength =  mainFlexesMissedInTest.size();
		Map<String, Double> prefixTailFlexesTest = MapsOps.getFirstEntriesString(test.tailFlexesFreq, prefixLength);
		for(String fmiss: mainFlexesMissedInTest)
			if(!prefixTailFlexesTest.containsKey(fmiss))
				return false;
		
		
		prefixLength = runCount == 2 ? testFlexesMissedInMain.size() * 2 : testFlexesMissedInMain.size();
		Map<String, Double> prefixTailFlexesMain = MapsOps.getFirstEntriesString(main.tailFlexesFreq, prefixLength);
		if(runCount == 2) {
			int missingsFound = 0;
			for(String fmiss: testFlexesMissedInMain)
				if(prefixTailFlexesMain.containsKey(fmiss)) missingsFound ++;
			// it is OK, if test has some seldom flexes, which are not in main. the coef should be > 0.8. this is TRUE for second run only
			if( (main.getFlexFreqMap().size() / (test.getFlexFreqMap().size() - missingsFound)) > 0.8 )
				return true;
			
		}else {
		for(String fmiss: testFlexesMissedInMain)
			if(!prefixTailFlexesMain.containsKey(fmiss))
				return false;
		}
		
		return true;
	}

	private static List<Map<String,Double>> getWinningFlexMap(Root r, WordSequences model, boolean print) {
		List<Map<String,Double>> flexMapList = test(r, model, true, print); //try left
		MyPair bestFlexLeft = MapsOps.getFirst(flexMapList.get(0));

		List<Map<String,Double>> flexMapForRight = test(r, model, false, print); //try right
		MyPair bestFlexRight = MapsOps.getFirst(flexMapForRight.get(0));
		if(bestFlexRight.freq > bestFlexLeft.freq) 
			return flexMapForRight;
		
		return flexMapList;
	}

	private static List<Map<String,Double>> cleanParadigm(List<Map<String, Double>> flexFreqMapAll) {
		double bestFlexFreq = MapsOps.getFirst(flexFreqMapAll.get(0)).freq;
		double thh = bestFlexFreq / 5.0;
		Map<String,Double> mainFlexMap = flexFreqMapAll.get(0);
		Map<String,Double> tailFlexMap = flexFreqMapAll.get(1);
		
		double bestFreqInMain = MapsOps.getFirst(mainFlexMap).freq;
		Iterator<String> mapIterator = mainFlexMap.keySet().iterator();
		while(mapIterator.hasNext()) {
			String key = mapIterator.next();
			double freq = mainFlexMap.get(key);
			if(bestFreqInMain != 1 && (freq == 1 || freq < thh )) { // clean flexes from main only if there are flexes with freq > 1
				//otherwise leave them in main. this is for case, when no contextWords and no paradigm words were found
				mapIterator.remove();
				tailFlexMap.put(key, freq);
			}
		}
		return flexFreqMapAll;
	}

	public static List<Map<String, Double>> test(Root r, WordSequences model, boolean startLeft, boolean print) {
		Set<String> seenRoots = new HashSet<>();
		Set<String> inputParadigmFlexes = new HashSet<>();
		Map<String,Double> contextScores = new HashMap<>(); //args
		
		Set<Root> foundContextsRoots = new LinkedHashSet<>();
		Set<Root> foundParadigmWordsRoots = new LinkedHashSet<>();
		
		//get all words with same root
		foundParadigmWordsRoots.add(r);
		seenRoots.add(r.toString());
		Set<Word> inputParadigm = new HashSet<>();
		for(Word input: r.getWords()) inputParadigm.add(input);

		
		for(Word paradigmWord: inputParadigm) {
			inputParadigmFlexes.add(paradigmWord.getFlex());
		}

		int count = 0;
		while(true) {
			count++;
			if(count > 30) break;
		//get best Predicat for this root: a root which scores best == has most of this root words as its right neighbours
			if(startLeft) computeContextScore(inputParadigm, contextScores, true);
			else computeContextScore(inputParadigm, contextScores, false);

		//set first score for the input root
//		falscher Score: paradigmRootFlexesScores.put(w.toString(), (double) contextScores.size()); // shows how many words from the inputparadigm are seen with this root
		String newBestContextRootString = getBestRoot(contextScores, seenRoots);

		if(newBestContextRootString == null && print) {
			System.out.println("no best context found");
			break;
		}
		Root newBestContextRoot = model.idx().getRoot(newBestContextRootString);
		seenRoots.add(newBestContextRootString);
		foundContextsRoots.add(newBestContextRoot);
		//get new best Argument: an arg root which scores best: has the most flexes, same as the input word and previous args
		Map<String,Double> tmpRootToFlexInParadigm = new HashMap<>();
		if(startLeft) computeParadigmScores(newBestContextRoot, tmpRootToFlexInParadigm, inputParadigmFlexes, true);
		else computeParadigmScores(newBestContextRoot, tmpRootToFlexInParadigm, inputParadigmFlexes, false);
		
		String newBestParadigmRootString = getBestRoot(tmpRootToFlexInParadigm, seenRoots);
		if(newBestParadigmRootString == null && print) {
			System.out.println("no best second word root found");
			System.out.println("newBestContextRoot: " + newBestContextRoot.toString() + ": "+newBestContextRoot.getWords());
			break;
		}else {
			Root newBestParadigmRoot = model.idx().getRoot(newBestParadigmRootString);
			seenRoots.add(newBestParadigmRootString);
			foundParadigmWordsRoots.add(newBestParadigmRoot);
			//add words from the new root into input paradigm
			for(Word w1: newBestParadigmRoot.getWords()) {
				if(inputParadigmFlexes.contains(w1.getFlex()) ) inputParadigm.add(w1);
			}
			//end of add
			
		}
		} // end while
		if(print) {
			System.out.println("FOUND contexts\t" + foundContextsRoots.toString());
			System.out.println("FOUND paradigm" + "\t" + foundParadigmWordsRoots.toString());
		}
		
		//get frequences for flexes seen in all words in the paradigm
		List<Map<String, Double>> flexFreqMapArr = getFlexFreqMaps(inputParadigmFlexes, foundParadigmWordsRoots, print);
		return flexFreqMapArr;
	}

	private static List<Map<String, Double>> getFlexFreqMaps(Set<String> inputParadigmFlexes, Set<Root> foundParadigmWordsRoots, boolean print) {
		Map<String,Double> flexFreqMap = new HashMap<>();
		Map<String,Double> notSeenFlexFreqMap = new HashMap<>(); // for flexes not seen in the paradigm of inputword

		for(Root r: foundParadigmWordsRoots) {
			for(Word w: r.getWords()) {
				if(inputParadigmFlexes.contains(w.getFlex())) MapsOps.addFreq(w.getFlex(), flexFreqMap);
				else MapsOps.addFreq(w.getFlex(), notSeenFlexFreqMap);
			}
		}
		if(print) {
			System.out.print("SEEN\t");
			MapsOps.printSortedMap(flexFreqMap, null, ", ");
			System.out.print("UNSEEN\t");
			MapsOps.printSortedMap(notSeenFlexFreqMap, null, ", ");
		}
		
		//put in flexes from UNSEEN, whose freq is > than the smalles freq in SEEN
		double smallestFreq = MapsOps.getLast(flexFreqMap).freq;
		Iterator<String> nonSeenIterator = notSeenFlexFreqMap.keySet().iterator();
		while(nonSeenIterator.hasNext()) {
			String nonSeenFlex = nonSeenIterator.next();
			if(notSeenFlexFreqMap.get(nonSeenFlex) > smallestFreq) {
				flexFreqMap.put(nonSeenFlex, notSeenFlexFreqMap.get(nonSeenFlex));
				nonSeenIterator.remove();
			}
		}
			
		 List<Map<String, Double>> mapList = new ArrayList<>();
		 mapList.add(flexFreqMap);
		 mapList.add(notSeenFlexFreqMap);
		return mapList;
	}

	private static void computeParadigmScores(Root newBestContextRoot, Map<String, Double> tmpRootToFlexInParadigm,
			Set<String> inputParadigmFlexes, boolean startLeft) {
		Set<Word> newBestContextParadigm = newBestContextRoot.getWords();

		for(Word paradigmWord: newBestContextParadigm) {
			Set<String> seenRootsForThisParadigmWord = new HashSet<>();
			Map<Word,Double> contextFreqMap;
			if(startLeft) contextFreqMap = paradigmWord.right_of;
			else contextFreqMap = paradigmWord.left_of;
			for (Word cont : contextFreqMap.keySet()) {
				if (cont.getRoot() == null)					continue;
				double testwordSignif = contextFreqMap.get(cont) / cont.freq();
				if(testwordSignif < 0.001) 					continue;
				if(seenRootsForThisParadigmWord.contains(cont.getRoot())) continue;// add score for one paradigm word only once, so we score against contexts which are neighbours of most paradigm words 
																					// and not most contexts with one word
//				seenRootsForThisParadigmWord.add(cont.getRoot());
				String cRoot = cont.getRoot();
				String cFlex = cont.getFlex();
				if (inputParadigmFlexes.contains(cFlex)) MapsOps.addFreq(cRoot, tmpRootToFlexInParadigm); // add freq to root if its flex is in the paradigm flexes
			}
		}		
	}

	private static String getBestRoot(Map<String, Double> candidatScores, Set<String> seenRoots) {
		Iterator<String> contRootSortedIterator = MapsOps.getSortedMapString(candidatScores).keySet().iterator();
		while(contRootSortedIterator.hasNext()) {
			String bestContRoot = contRootSortedIterator.next();
			if(seenRoots.contains(bestContRoot)) continue;
//			if(candidatScores.get(bestContRoot) == 1) return null;
				return bestContRoot;
		}
		return null;
	}

	private static void computeContextScore(Set<Word> inputParadigm, Map<String, Double> contextScores, boolean startLeft) {
		for(Word paradigmWord: inputParadigm) { //predicats sind Wörter von der InputParadigm
			Set<String> seenRootsForThisParadigmWord = new HashSet<>();
			Map<Word,Double> contextFreqMap;
			if(startLeft) contextFreqMap = paradigmWord.left_of;
			else contextFreqMap = paradigmWord.right_of;
			for(Word cont: contextFreqMap.keySet()) { 
				if(cont.getRoot() == null) continue;
				double testwordSignif = contextFreqMap.get(cont) / cont.freq();
				if(testwordSignif < 0.001) 					continue;
				if(seenRootsForThisParadigmWord.contains(cont.getRoot())) continue;// add score for one paradigm word only once, so we score against contexts which are neighbours of most paradigm words 
																					// and not most contexts with one word
				seenRootsForThisParadigmWord.add(cont.getRoot());
				MapsOps.addFreq(cont.getRoot(), contextScores);
			}
		}		
	}

	private static void printMpars(MorphParadigms mpars) {
		for(MorphParadigm mpar: mpars.paradigms.values()) {
			System.out.println((int)mpar.getFreq() + "\t" + mpar.getSortedFlex() + "\t" + mpar.getSortedFlexFreqMap()+  "\t" + mpar.getTailFlexesAsString());
		}
		System.out.println("morph sem paradigms size: " + mpars.paradigms.size());
	}

	private static void printMpars(List<MorphParadigm> mparsList) {
		for(MorphParadigm mpar2: mparsList) {
			System.out.println((int)mpar2.getFreq() + "\t" + mpar2.getSortedFlex() + "\t" + mpar2.getSortedFlexFreqMap()+  "\t" + mpar2.getTailFlexesAsString());
		}
		System.out.println("morph sem paradigms size: " + mparsList.size());
	}

//	private static boolean mostSignificantContextsAreLeft(Word w, Set<Word> inputParadigm) {
////		double bestScoreLeft = w.getCoef(true);
////		double bestScoreRight = w.getCoef(false);
////		return bestScoreLeft >= bestScoreRight;
//		List<List<MyPair>> groupsLeft = Words.computeContextsWithSignif(w, true, 100, false);
//		List<List<MyPair>> groupsRight = Words.computeContextsWithSignif(w, false, 100, false);
//		if(groupsLeft.get(1).size() == 0 && groupsRight.get(1).size() == 0) {
//			return groupsLeft.get(3).size()  > groupsRight.get(3).size();
//		}
//		return (groupsLeft.get(0).size() + groupsLeft.get(1).size()) > (groupsRight.get(0).size() + groupsRight.get(1).size());
//	}

}
