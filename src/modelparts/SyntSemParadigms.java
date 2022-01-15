package modelparts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.util.Pair;
import model.WordSequences;
import modeltrain.SyntParTrain;
import modelutils.Cluster;
import modelutils.Vector;
import util.MapsOps;

public class SyntSemParadigms {
	
	public static List<Cluster> trainParadigms(WordSequences model, int howManyWordsForTrain) {
		List<Cluster> output = new ArrayList<>();
		Set<String> seen = new HashSet<>();
		for(Word w: SyntParTrain.getMostFrequestWordsForTraining(model, howManyWordsForTrain, 0, "noSplitter")) {
			if(seen.contains(w.toString())) continue;
			seen.add(w.toString());
			Vector values = new Vector(SyntParTrain.collectFeatures(w, model));
			Cluster inputCluster = new Cluster(new Pair<String,Vector>(w.toString(), values));
			Cluster paradigmCluster = getWinningContextCluster(inputCluster, model, true);
			for(String wstring: paradigmCluster.getWords())
				seen.add(wstring);
			printCluster(paradigmCluster);
			output.add(paradigmCluster);
		}
		return output;
	}
	
	private static void printCluster(Cluster paradigmCluster) {
		System.out.println(paradigmCluster.getWords().toString());
		System.out.println();
	}

	private static Cluster getWinningContextCluster(Cluster c, WordSequences model, boolean print) {
		double thh = -1.1;
		print = false;
		Map<Word,Double> flexMapList = test(c, model, true, print, thh); //try left

		Map<Word,Double> flexMapForRight = test(c, model, false, print, thh); //try right
		if(flexMapList.size() >= flexMapForRight.size()) 
			return computeCluster(c, flexMapList, thh);
		
		return computeCluster(c, flexMapForRight, thh);
	}

	
	private static Cluster computeCluster(Cluster c, Map<Word,Double> newWords, double thh) {
		for(Word w: newWords.keySet()) {
			if(newWords.get(w) > thh)
				c.addWord(w.toString(), new Vector(SyntParTrain.collectFeatures(w, null)));
		}
		return c;
	}

	public static Map<Word,Double> test(Cluster c, WordSequences model, boolean startLeft, boolean print, double thh) {
		Set<String> seenWords = new HashSet<>();
		Vector inputParadigmVetor = c.getCentroide();
		Map<String,Double> contextScores = new HashMap<>(); //args
		
		Set<String> foundContexts = new LinkedHashSet<>();
		Map<Word,Double> foundParadigmWords = new LinkedHashMap<>();
		
		String inputWordString = c.getWords().get(0);
		foundParadigmWords.put(model.getWord(inputWordString),0.1);
		seenWords.add(inputWordString);


		int count = 0;
		while(true) {
			count++;
			if(count > 30) break;
			contextScores = new HashMap<>();
			if(startLeft) computeContextScore(foundParadigmWords, contextScores, true);
			else computeContextScore(foundParadigmWords, contextScores, false);

			Pair<String,Double> bestPair = getBest(contextScores, seenWords);

		if(bestPair == null ) {
			if(print)
				System.out.println("no best context found");
			break;
		}
		String newBestContextString = bestPair.getKey();

		seenWords.add(newBestContextString);
		foundContexts.add(newBestContextString);

		Map<String,Double> tmpParadigmWordStrings = new HashMap<>();
		if(startLeft) computeParadigmScores(model.getWord(newBestContextString), tmpParadigmWordStrings, inputParadigmVetor, true);
		else computeParadigmScores(model.getWord(newBestContextString), tmpParadigmWordStrings, inputParadigmVetor, false);
		
		Pair<String,Double> newBestArgPair = getBest(tmpParadigmWordStrings, seenWords);
		if(newBestArgPair == null || newBestArgPair.getValue() < thh) {
			if(print) {
				System.out.println("no best second word root found");
				System.out.println("newBestContextString: " + newBestContextString );
			}
			break;
		}else {
			seenWords.add(newBestArgPair.getKey());
			foundParadigmWords.put(model.getWord(newBestArgPair.getKey()), newBestArgPair.getValue());
			
			//end of add
			
		}
		} // end while
		if(print) {
			System.out.println("FOUND contexts\t" + foundContexts.toString());
			System.out.println("FOUND paradigm" + "\t" + foundParadigmWords.toString());

//			System.out.println("FOUND paradigm" + "\t" + foundParadigmWords.keySet().toString());
//			System.out.println("FOUND paradigm" + "\t" + foundParadigmWords.values().toString());
			System.out.println();
		}
		
		return foundParadigmWords;
	}

	private static void computeParadigmScores(Word newBestContext, Map<String, Double> tmpParadigmWordStrings,
			Vector inputParadigmVetor, boolean startLeft) {

			Map<Word,Double> argsFreqMap;
			if(startLeft) argsFreqMap = newBestContext.right_of;
			else argsFreqMap = newBestContext.left_of;
			for (Word arg : argsFreqMap.keySet()) {
				double testwordSignif = argsFreqMap.get(arg) / arg.freq();
				if(testwordSignif < 0.001) 					continue;
				Vector contextVector = new Vector(SyntParTrain.collectFeatures(arg, null));
				double sim = Cluster.computeVectorSimilarity(inputParadigmVetor, contextVector);
				tmpParadigmWordStrings.put(arg.toString(), sim*(-1)); // save word and its similarity to inputwords
			}
	}

	private static Pair<String,Double> getBest(Map<String, Double> candidatScores, Set<String> seenRoots) {
		if(candidatScores.size() == 0) return null;
		Iterator<String> contRootSortedIterator = MapsOps.getSortedMapString(candidatScores).keySet().iterator();
		while(contRootSortedIterator.hasNext()) {
			String bestCont = contRootSortedIterator.next();
			if(seenRoots.contains(bestCont)) continue;
//			if(candidatScores.get(bestContRoot) == 1) return null;
				return new Pair(bestCont,candidatScores.get(bestCont));
		}
		return null;
	}

	private static boolean computeContextScore(Map<Word,Double> args, Map<String, Double> contextScores, boolean startLeft) {
		for(Word paradigmWord: args.keySet()) { //predicats sind Wörter von der InputParadigm
			Map<Word,Double> contextFreqMap;
			if(startLeft) contextFreqMap = paradigmWord.left_of;
			else contextFreqMap = paradigmWord.right_of;
			for(Word cont: contextFreqMap.keySet()) { 
				double testwordSignif = contextFreqMap.get(cont) / paradigmWord.freq(); //cont.freq();
				if(testwordSignif < 0.001) 					continue;
				if(args.size() == 1)
					MapsOps.addFreq(cont.toString(), contextScores, testwordSignif);
				else
					MapsOps.addFreq(cont.toString(), contextScores);
			}
		}	
		return true;
	}

	
}
