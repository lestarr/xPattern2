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

public class MorphSemParadigmsOld {
	
	public static void train(WordSequences model, int freqThh) {
		Set<String> seenRoots = new HashSet<>();
		MorphParadigms mpars = new MorphParadigms();

		for(Word w: model.idx().getSortedWords()) {
			if(w.toString().length() < 4) continue;
			if(w.freq() < freqThh) continue;
			if(w.getRoot() == null) continue;
			String rootString = w.getRoot();
			if(seenRoots.contains(rootString)) continue;
			seenRoots.add(rootString);
			
			System.out.println(w.toString() + "\t" + w.freq());
			
			List<Map<String,Double>> flexFreqMapAll = getWinningFlexMap(w, model, true);
			List<Map<String,Double>> flexFreqMapCleaned = cleanParadigm(flexFreqMapAll);
			Set<Flexion> flexes = new HashSet<>();
			Set<Flexion> tailFlexes = new HashSet<>();
			for(String f: flexFreqMapCleaned.get(0).keySet()) 
					flexes.add(model.idx().getFlex(f));
			for(String f: flexFreqMapCleaned.get(1).keySet()) 
					tailFlexes.add(model.idx().getFlex(f));
			MorphParadigm mpar = mpars.getParadigm(flexes, flexFreqMapCleaned.get(0),model );
			mpar.addTailFlexes(flexFreqMapCleaned.get(1));
		}
		
		
		List<MorphParadigm> mparsList = new ArrayList<MorphParadigm>();
		for(MorphParadigm mpar: mpars.paradigms.values())
			if(mpar.getFreq() > 1.0)
				mparsList.add(mpar);
		
		for(MorphParadigm mpar: mparsList) {
			System.out.println((int)mpar.getFreq() + "\t" + mpar.getSortedFlex() + "\t" + mpar.getSortedFlexFreqMap()+  "\t" + mpar.getTailFlexesAsString());
		}
		System.out.println("morph sem paradigms size: " + mparsList.size());

		
		mparsList = combineParadigms(mparsList);
	}

	private static List<MorphParadigm> combineParadigms(List<MorphParadigm> mparsList) {
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
				
				if(canBeCombined(mparMain, mparTest)) {
					mparMain.addFreq(mparTest.getFreq());
					mparMain.addFlexFreqsMainTail(mparTest.getFlexFreqMap());
					mparMain.addFlexFreqsMainTail(mparTest.tailFlexesFreq);
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

	private static boolean canBeCombined(MorphParadigm main, MorphParadigm test) {
		//check if testParadigm has all the Flexes of the main, check also in tail of the testParadigm
		Set<String> missingFlexes = new HashSet<>();
		for(String f: main.getFlexFreqMap().keySet()) {
			if(!test.getFlexFreqMap().containsKey(f) ) missingFlexes.add(f);
		}
		Map<String, Double> prefixTailFlexes = MapsOps.getFirstEntriesString(test.tailFlexesFreq, missingFlexes.size()*2);
		for(String fmiss: missingFlexes)
			if(!prefixTailFlexes.containsKey(fmiss))
				return false;
		//check if testParadigm has additional Flexes, if so, check if they are in the tail of the main
		missingFlexes = new HashSet<>();
		for(String f: test.getFlexFreqMap().keySet()) {
			if(!main.getFlexFreqMap().containsKey(f)) missingFlexes.add(f);
		}
		prefixTailFlexes = MapsOps.getFirstEntriesString(main.tailFlexesFreq, missingFlexes.size()*2);
		for(String fmiss: missingFlexes)
			if(!prefixTailFlexes.containsKey(fmiss))
				return false;
		
		return true;
	}

	private static List<Map<String,Double>> getWinningFlexMap(Word w, WordSequences model, boolean print) {
		List<Map<String,Double>> flexMap = test(w, model, true, print); //try left
		MyPair bestFlexLeft = MapsOps.getFirst(flexMap.get(0));

		List<Map<String,Double>> flexMapForRight = test(w, model, false, print); //try right
		MyPair bestFlexRight = MapsOps.getFirst(flexMapForRight.get(0));
		if(bestFlexRight.freq > bestFlexLeft.freq) 
			return flexMapForRight;
		
		return flexMap;
	}

	private static List<Map<String,Double>> cleanParadigm(List<Map<String, Double>> flexFreqMapAll) {
		double bestFlexFreq = MapsOps.getFirst(flexFreqMapAll.get(0)).freq;
		double thh = bestFlexFreq / 5.0;
		Map<String,Double> mainFlexMap = flexFreqMapAll.get(0);
		Map<String,Double> tailFlexMap = flexFreqMapAll.get(1);
		Iterator<String> mapIterator = mainFlexMap.keySet().iterator();
		while(mapIterator.hasNext()) {
			String key = mapIterator.next();
			double freq = mainFlexMap.get(key);
			if(freq == 1 || freq < thh) {
				mapIterator.remove();
				tailFlexMap.put(key, freq);
			}
		}
		return flexFreqMapAll;
	}

	public static List<Map<String, Double>> test(Word w, WordSequences model, boolean startLeft, boolean print) {
		if(w.getRoot() == null) return null;
		
		
		Set<String> seenRoots = new HashSet<>();
		Set<String> inputParadigmFlexes = new HashSet<>();
		Map<String,Double> contextScores = new HashMap<>(); //args
//		Map<String,Double> paradigmRootFlexesScores = new HashMap<>(); // preds
		
		Set<Root> foundContextsRoots = new LinkedHashSet<>();
		Set<Root> foundParadigmWordsRoots = new LinkedHashSet<>();
		
		//get all words with same root
		Root r = model.idx().getRoot(w.getRoot());
		foundParadigmWordsRoots.add(r);
		seenRoots.add(r.toString());
		Set<Word> inputParadigm = new HashSet<>();
		for(Word input: r.getWords()) inputParadigm.add(input);

		//find out: start left or right, depending on how many left or right root neighbours of the input paradigm come together with words of this paradigm
//		boolean startLeft = mostSignificantContextsAreLeft(w, inputParadigm);

		
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
//		for(Root r1: foundContextsRoots) {
//			System.out.println("newBestContextRoot: " + r1.toString() + ": "+r1.getWords());
//		}
//		for(Root r1: foundParadigmWordsRoots) {
//			System.out.println("newBestParadigmRoot: " + r1.toString() + ": " + r1.getWords());
//		}
		
		//get frequences for flexes seen in all words in the paradigm
		List<Map<String, Double>> flexFreqMapArr = getFlexFreqMaps(w, inputParadigmFlexes, foundParadigmWordsRoots, print);
		return flexFreqMapArr;
	}

	private static List<Map<String, Double>> getFlexFreqMaps(Word inputword, Set<String> inputParadigmFlexes, Set<Root> foundParadigmWordsRoots, boolean print) {
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
			else {
				return bestContRoot;
			}
		}
		return null;
	}

	private static void computeContextScore(Set<Word> inputParadigm, Map<String, Double> contextScores, boolean startLeft) {
		for(Word paradigmWord: inputParadigm) { //predicats sind Wörter von der InputParadigm
			Set<String> seenRootsForThisParadigmWord = new HashSet<>();
			Map<Word,Double> contextFreqMap;
			if(startLeft) contextFreqMap = paradigmWord.left_of;
			else contextFreqMap = paradigmWord.right_of;
			for(Word cont: contextFreqMap.keySet()) { //take the right contexts, as we first want to test Adjectives
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
