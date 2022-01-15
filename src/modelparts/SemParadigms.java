package modelparts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SortedMap;

import model.WordSequences;
import model.Words;
import util.MapsOps;
import util.MyPair;
import util.MyUtils;

public class SemParadigms {
	
	public static final String splitString = "[,]";
	public static Double minPredSignifNonFrequent = 0.00099;
	public static Double minPredSignifFrequentWords = 0.0099;
	
	public static final String NO_PREDS_FOUND = "noPreds found";

	//Get Arguments to the input left context = predicat
	//sort them into 5 groups according to significance: 1.0-0.9, 0.9-0.1, 0.1-0.01, 0.01-0.001, other
	//take new context which is significant (0.001) to the most found arguments (new most popular predicat)
	//add significances of arguments, split them again into groups
	//repeat	
	public static List<List<MyPair>> computeContextsWithSignifRightLeftTest(Word inputPred, WordSequences wsmodel, 
			boolean startLeft, boolean printGroups, boolean checkFirstPredInTheList) {
		
		double minPredSignif = getMinPredSignif(inputPred, wsmodel);

		
		HashMap<Word,Double> arguments = startLeft ? inputPred.left_of : inputPred.right_of;
		
		int minFreqForPred = arguments.size() > 1000 ? 100 : 10;
		minFreqForPred = (int) (arguments.size() / 100);
		minFreqForPred = Math.max(10, minFreqForPred);
		if(minFreqForPred > arguments.size()) minFreqForPred = (int) (arguments.size() * 0.3);
		HashMap<Word,Double> argumentsForGroups = startLeft ? getLeftArgs(inputPred, null) : getRightArgs(inputPred, null);
		List<List<MyPair>> groups = Words.splitArgumentsIntoGroups(argumentsForGroups);
	
		//find most popular new predicat
		LinkedHashMap<String,Boolean> seenPreds = new LinkedHashMap<>();
		List<String> seenPredsList = new ArrayList<>();
		
		seenPreds.put(inputPred.toString(), startLeft);

		int nTimes = 1;
		HashMap<Word,Double> aggrActivePredsToGroupPreds = new HashMap<>();

		while(true) {
		int cc = 0;
		List<List<MyPair>> newgroups = null;
		int nSeenPreds = seenPreds.size();
			while(cc < nTimes) {
				cc++;
				aggrActivePredsToGroupPreds = doCircles(wsmodel, minFreqForPred, seenPreds, 
						argumentsForGroups, startLeft, minPredSignif, checkFirstPredInTheList);

				newgroups = Words.splitArgumentsIntoGroups(aggrActivePredsToGroupPreds);
			}
			int goOn = compareGroupsIfContinue(newgroups, groups);
			if(goOn == 1 && nSeenPreds < seenPreds.size() && seenPreds.size() < 51) { //to avoid endless loop!!!
				argumentsForGroups = aggrActivePredsToGroupPreds;
				groups = newgroups;
				continue;
			} 
			else {
				if(goOn == -1)
					seenPredsList = deleteLastPred(seenPreds);
				else
					seenPredsList = new ArrayList<>(seenPreds.keySet());
				if(seenPredsList.size() == 0)
						inputPred.setNotFoundPredicats(startLeft, wsmodel);
					inputPred.setSeenPredicatsFromString(seenPredsList, wsmodel, startLeft);
					
				if(printGroups) {
					System.out.println("INPUT PRED: "+inputPred);
					System.out.println("SEEN preds: "+seenPredsList.size()+" "+seenPredsList.toString());
					Words.printGroups(groups,50);

					System.out.println("NEW GROUPS, seen preds: "+seenPreds.size()+" "+ toStringPreds(seenPreds));
					Words.printGroups(newgroups,50);
				}
				break;
			}
			
		} // end outer while

		return groups;
	}
	
	private static String toStringPreds(LinkedHashMap<String, Boolean> seenPreds) {
		StringBuffer toString = new StringBuffer();
		for(String s: seenPreds.keySet()) {
			toString.append(s).append("_").append(seenPreds.get(s) ? "l":"r").append(", ");
		}
		return toString.toString().replaceFirst(", $", "");
	}
	
	private static List<String> deleteLastPred(LinkedHashMap<String, Boolean> seenPreds) {
		List<String> seenPredsList = new ArrayList<>(seenPreds.keySet());
		seenPredsList.remove(seenPredsList.size()-1);
		return seenPredsList;
	}
	
	private static int compareGroupsIfContinue(List<List<MyPair>> newgroups,	List<List<MyPair>> groups) {
		double sizeGr1New = (double) newgroups.get(1).size();
		double sizeGr1 = (double) groups.get(1).size();
		double sizeGr2 = (double) groups.get(2).size();
		double sizeGr3 = (double) groups.get(3).size();
		double sizeGr2New = (double) newgroups.get(2).size();
		double sizeGr3New = (double) newgroups.get(3).size();
		double sizeAllNew = (double) getGroupsArgsSum(newgroups);
		for (List<MyPair> l : newgroups)
			if (l.size() > 55000)
				return -1;

		// if groups added more than 10000 args
		if ((sizeGr1New + sizeGr2New + sizeGr3New) - (sizeGr1 + sizeGr2 + sizeGr3) > 20000)
			return -1;

		if (sizeGr1 > 100.0)
			return 1;

		if ((sizeGr1New + sizeGr2New) / sizeAllNew > 0.3)
			return 1;

		if (sizeGr2 > 0.0 && sizeGr1 / sizeGr2 > 0.2)
			return 1; // too small difference between gr1 and gr2 means paradigm not ended
		if (sizeGr3New > sizeGr2New)
			return -1;

		if (sizeGr1 > 0.0 && ((sizeGr1New / sizeGr1) < 0.2))
			return -1;
		if (sizeGr3New > 0.0 && sizeGr2New / sizeGr3New < 1.5)
			return -1;

		return 1;
	}

	private static int getGroupsArgsSum(List<List<MyPair>> groups) {
		int sum = 0;
		for (int i = 0; i < groups.size(); i++) sum = sum + groups.get(i).size();
		return sum;
	}
	
	private static HashMap<Word, Double> doCircles(WordSequences wsmodel, int minFreqForPred,
			LinkedHashMap<String, Boolean> seenPreds, HashMap<Word, Double> aggrArgs, boolean startLeft,
			double minPredSignif, boolean checkFirstPredInTheList) {
		// System.out.println(seenPreds.toString());
		String firstPred = "", firstArg = "";
		for (String pstring : seenPreds.keySet()) {
			firstPred = pstring;
			break;
		}
		for (Word astring : aggrArgs.keySet()) {
			firstArg = astring.toString();
			break;
		}
		if (aggrArgs.size() > wsmodel.getFreqOfAnd() / 10)
			minFreqForPred = aggrArgs.size() / 5;
		if (aggrArgs.size() > 1000 && firstPred.contains(","))
			minFreqForPred = aggrArgs.size() / 2;
		// if(firstPred.contains(",")) System.out.println("minfreq: " + minFreqForPred);
		SortedMap<String, Double> predsSortedSignif = startLeft
				? findMostPopularPredikat(aggrArgs, wsmodel, false, minFreqForPred)
				: findMostPopularPredikatRight(aggrArgs, wsmodel, minFreqForPred);

		String newPred = null;

		newPred = getMostPopularPredikat(predsSortedSignif, seenPreds, wsmodel, startLeft, aggrArgs, minPredSignif);
		if (newPred == null) {
			// try smaller amount of arguments
			if (firstPred.contains(","))
				minFreqForPred = minFreqForPred; // (int) (minFreqForPred * 0.5);
			else if (aggrArgs.size() > wsmodel.getFreqOfAnd() / 10)
				minFreqForPred = (int) (minFreqForPred * 0.5);
			else
				minFreqForPred = (int) (minFreqForPred * 0.3);

			predsSortedSignif = startLeft ? findMostPopularPredikat(aggrArgs, wsmodel, false, minFreqForPred)
					: findMostPopularPredikatRight(aggrArgs, wsmodel, minFreqForPred);
			newPred = getMostPopularPredikat(predsSortedSignif, seenPreds, wsmodel, startLeft, aggrArgs, minPredSignif);
		}

		// check if the input pred is still in the list
		boolean predsCOntainFirstPred = false;
		for (String pred : predsSortedSignif.keySet()) {
			if (pred != null && pred.equalsIgnoreCase(firstPred)) {
				predsCOntainFirstPred = true;
				break;
			}
			if (!firstPred.contains(",") && !firstArg.contains(","))
				if (!seenPreds.containsKey(pred) || pred.equals(newPred))
					break;
		}
		if (!checkFirstPredInTheList)
			predsCOntainFirstPred = true;
		if (!predsCOntainFirstPred) {
			if (newPred != null)
				seenPreds.remove(newPred);
			newPred = null;
			// delete last pred, which threw out the input pred

		}

		List<Word> seenPredsList = new ArrayList<>();
		for (String pstring : seenPreds.keySet()) {
			Word seenPred = wsmodel.getWord(pstring);
			seenPredsList.add(seenPred);
		}

		if (newPred == null) {
			// System.out.println("No new pred found. Return");
			return getMainArgumentStats(seenPredsList, aggrArgs, wsmodel, startLeft); // aggrArgs;
		}
		HashMap<Word, Double> argumentsForGroups = startLeft ? getLeftArgs(wsmodel.getWord(newPred), null)
				: getRightArgs(wsmodel.getWord(newPred), null);
		for (Word arg : argumentsForGroups.keySet()) {
			MapsOps.addFreq(arg, aggrArgs);
		}
		return getMainArgumentStats(seenPredsList, aggrArgs, wsmodel, startLeft);
	}
	
	public static HashMap<Word, Double> getLeftArgs(Word inputPred, HashMap<Word,Double> arguments) {
		if(arguments == null)
		 arguments = new HashMap<>();
		for(Word arg: inputPred.left_of.keySet()) {
			if(inputPred.toString().contains(",") && arg.freq() < 10)
				continue;
			double signifTestWord = MyUtils.rdouble(inputPred.left_of.get(arg)/arg.freq());
			if(signifTestWord > 0.001) //take only signif args = args for which this pred is signif!
				arguments.put(arg, signifTestWord);
		}
		return arguments;
	}
	
	public static HashMap<Word, Double> getRightArgs(Word inputPred, HashMap<Word,Double> arguments) {
		if(arguments == null)
			 arguments = new HashMap<>();
		for(Word arg: inputPred.right_of.keySet()) {
			if(inputPred.toString().contains(",") && arg.freq() < 10)
				continue;
			double signifTestWord = MyUtils.rdouble(inputPred.right_of.get(arg)/arg.freq());
			if( signifTestWord > 0.001) //take only signif args = args for which this pred is signif!
				arguments.put(arg, signifTestWord);
		}
		return arguments;
	}
	
	public static HashMap<Word, Double> getMainArgumentStats(List<Word> seenPreds, HashMap<Word, Double> aggrArgs, 
			WordSequences wsmodel, boolean startLeft) {
		HashMap<Word,Double> aggrAvtivePredsToGroupPreds = new HashMap<>();
		for(Word arg: aggrArgs.keySet()) {
			double signifGroupPredCounts  = getPercentAvtivePredsToGroupPreds(arg, seenPreds, wsmodel, startLeft);
			if(signifGroupPredCounts <= ((double)1.0/(double)seenPreds.size())) // skip arguments which have only 1 pred!
				continue;
			double signifInGroupPred  = getPercentActivePredsToThisArgPreds(arg, seenPreds, wsmodel, startLeft);
			aggrAvtivePredsToGroupPreds.put(arg, MyUtils.rdouble(signifGroupPredCounts*signifInGroupPred));
		}
		return aggrAvtivePredsToGroupPreds;
	}
	
	private static double getPercentAvtivePredsToGroupPreds(Word arg, List<Word> seenPreds, WordSequences wsmodel, boolean startLeft) {
		if(seenPreds.size() == 0) return 0.0;
		double sumOfSeenPreds = 0.0;
		for(Word w: seenPreds) {
			if( (startLeft && arg.right_of.containsKey(w) )	|| (!startLeft && arg.left_of.containsKey(w)) )
				sumOfSeenPreds++;
		}
		if(sumOfSeenPreds == 0) {
			System.out.println( "EXCEPTION: arg: " + arg.toString() + " with pred: " + seenPreds.toString() 
			+  " ISNULL, start left of fisrt is: " +startLeft);
		}
		return MyUtils.rdouble((double)sumOfSeenPreds/seenPreds.size());
	}
	
	private static double getPercentActivePredsToThisArgPreds(Word arg, List<Word> seenPreds,  WordSequences wsmodel, boolean startLeft) {
		if((startLeft && arg.right_of.size() == 0) || (!startLeft && arg.left_of.size() == 0) ) return 0.0;
		double sumOfSeenPreds = 0.0;
		for(Word w: seenPreds) {
			if( (startLeft && arg.right_of.containsKey(w) )
					|| (!startLeft && arg.left_of.containsKey(w)) )
				sumOfSeenPreds++;
		}
		if(sumOfSeenPreds == 0)
			System.out.println( "EXCEPTION: arg: " + arg.toString() + " with pred: " + seenPreds.toString() 
			+  " ISNULL, start left of fisrt is: " +startLeft);
		return MyUtils.rdouble((double)sumOfSeenPreds/(startLeft ? arg.right_of.size() : arg.left_of.size()));
	}
	
	public static HashMap<Word, Double> collectPredicatsToArgs(Word w, double signifPred, int importantPredsMarker ) {
		//if there were still no contexts saved in the word
		HashMap<Word, Double> preds = new HashMap<>();
		for (Word w_left : w.right_of.keySet()) {
			if(w_left.left_of.size() >  importantPredsMarker)
				continue;
			double signifL = Words.getSignifOfLeft(w_left, w);
			if (signifL < signifPred)
				continue;
				MapsOps.addFreqObject(w_left, preds, signifL);
		}
		return preds;
	}
	
	public static HashMap<Word, Double> collectPredicatsRightToArgs(Word w, double signifPred ) {
		//w = e.g. Audi, look for right contexts, e.g. GmbH, AG
		//if there were still no contexts saved in the word
		HashMap<Word, Double> preds = new HashMap<>();
		for (Word w_right : w.left_of.keySet()) {
			double signifR = Words.getSignifOfRight(w, w_right);
			if (signifR < signifPred)
				continue;
				MapsOps.addFreqObject(w_right, preds, signifR);
		}
		return preds;
	}

	private static SortedMap<String, Double> findMostPopularPredikat(HashMap<Word, Double> aggrArgs, WordSequences wsmodel, 
			 boolean printPreds, int minFreq) {
		HashMap<Word, Double> predicatFreqs = new HashMap<>();
		for(Word argw: aggrArgs.keySet()) {
			if(aggrArgs.get(argw) < 0.01) continue;
			HashMap<Word,Double> tmpPreds = collectPredicatsToArgs(argw, 0.01, Integer.MAX_VALUE);
			for(Word w: tmpPreds.keySet()) {
				MapsOps.addFreqObject(w, predicatFreqs, 1.0);
			}
		}
		//compute how often argument not of this group
		HashMap<Word, Double> predicatFreqSignif = new HashMap<>();
		for(Word predw: predicatFreqs.keySet()) {
			double freq = predicatFreqs.get(predw);
			if(freq < minFreq) continue;
			double otherSignif = (double)freq/predw.left_of.size();
			if(otherSignif > 1)
				System.out.println(predw.toString() + otherSignif);
			if(predw.toString().contains(",") ) {
				if(otherSignif > 0.1)
					predicatFreqSignif.put(predw, otherSignif);
			} else
				predicatFreqSignif.put(predw, otherSignif);
		}
		SortedMap<String,Double> predsSortedSignif = MapsOps.getSortedMapWord(predicatFreqSignif);
		return predsSortedSignif;
	}
	
	private static SortedMap<String, Double> findMostPopularPredikatRight(HashMap<Word, Double> aggrArgs, 
			WordSequences wsmodel, int minFreq) {
		HashMap<Word, Double> predicatFreqs = new HashMap<>();
		for(Word argw: aggrArgs.keySet()) {
			if(!argw.toString().contains(",") && aggrArgs.get(argw) < 0.01) continue;
			HashMap<Word,Double> tmpPreds = collectPredicatsRightToArgs(argw, 0.01);
			for(Word w: tmpPreds.keySet()) {
				MapsOps.addFreqObject(w, predicatFreqs, 1.0);
			}
		}
		//compute how often argument not of this group
		HashMap<Word, Double> predicatFreqSignif = new HashMap<>();
		for(Word pred: predicatFreqs.keySet()) {
			double freq = predicatFreqs.get(pred);
			if(freq < minFreq) continue;
			double otherSignif = (double)freq/pred.right_of.size();
			if(pred.toString().contains(",") ) {
				if(otherSignif > 0.1)
					predicatFreqSignif.put(pred, otherSignif);

			} else
				predicatFreqSignif.put(pred, otherSignif);
		}
		SortedMap<String,Double> predsSortedSignif = MapsOps.getSortedMapWord(predicatFreqSignif);
		return predsSortedSignif;
	}
	
	private static String getMostPopularPredikat(SortedMap<String, Double> predsSortedSignif, 
			HashMap<String,Boolean> seenPreds, WordSequences wsmodel ,boolean leftPreds, HashMap<Word, Double> aggrArgs, double minPredSignif) {
		for (String pred : predsSortedSignif.keySet()) {
			if (!seenPreds.containsKey(pred)) {
				if (predsSortedSignif.get(pred) < minPredSignif )
					return null;
				seenPreds.put(pred, leftPreds); // get only the first most significant pred!
				return pred;
			}
		}
		return null;
	}

//	public static HashMap<Word, Double> getArgs(Word inputPred, HashMap<Word,Double> arguments, boolean getLeft) {
//		if(arguments == null)
//		 arguments = new HashMap<>();
//		Map<Word,Double> argsLeftOrRight = getLeft ? inputPred.left_of : inputPred.right_of;
//		for(Word arg: argsLeftOrRight.keySet()) {
//			if(inputPred.toString().contains(",") && arg.freq() < 10)
//				continue;
//			double signifTestWordDouble = MyUtils.rdouble(inputPred.left_of.get(arg)/arg.freq());
//			if(signifTestWordDouble > 0.001) //take only signif args = args for which this pred is signif!
//				arguments.put(arg, signifTestWordDouble);
//		}
//		return arguments;
//	}
	

	
	private static double getMinPredSignif(Word word, WordSequences model) {
		if(Words.isFrequent(word, model) ) return minPredSignifFrequentWords;
		return minPredSignifNonFrequent;
	}
}
