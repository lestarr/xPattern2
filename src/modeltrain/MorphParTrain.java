//package modeltrain;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//import javafx.util.Pair;
//import model.LetterTokModel;
//import model.MorphModel;
//import model.MorphVectorAnalyzer;
//import model.WordSequences;
//import model.Words;
//import modelparts.Flexion;
//import modelparts.MorphParadigm;
//import modelparts.MorphParadigms;
//import modelparts.Root;
//import modelparts.Similarity;
//import modelparts.Word;
//import modelutils.Cluster;
//import util.MapsOps;
//import util.MyPair;
//import util.MyPairWord;
//import util.SetOps;
//
//public class MorphParTrain {
//	
//	
//	public static void getRootFlex(WordSequences wsmodel, LetterTokModel ltmodel, int freqTHH) throws IOException {
//		System.out.println("Training clusters");
//		boolean print = false;
//		boolean printStarRain = false;
//
//		//PRINT FLEXES Freq
////		System.out.println("FLEXES freq:");
////		MapsOps.printSortedMap(wsmodel.idx().getSortedFlexes(), null, 9);
//		
//		double topFlexFrequency = getTopFlexFrequency(wsmodel);
//		double flexFreqTHH = 1.0; //topFlexFrequency / 1000; //topFlexFrequency/100;
//		
//		writeRootFlexIntoWord(wsmodel, ltmodel, freqTHH,  print); //this writes root and flexes into words
//		writeRootFlexIntoModel(wsmodel, freqTHH, print); //this writes root and flexes into model
//
////		buildFirstMorphParadigms(wsmodel, printStarRain, flexFreqTHH);
//	}
//	
//	public static void retrainCLustersOnTaggedWords(WordSequences wsmodel, int thhPar, int thhFlex) throws IOException {
//		tagForRetrain(wsmodel);
//		sortOutParadigmsWithLessWordMembers(wsmodel, thhPar, thhFlex);
//	}
//	
//
//	public static double getTopFlexFrequency(WordSequences model) {
//		if(model.idx().flexes == null) return 0.0;
//		//retur freq of most frequent flex
//		return MapsOps.getFirst(model.idx().getSortedFlexes()).freq;
//	}
//	
//	/**
//	 * gets slices, sets root and flex in word, writes roots into flex and flexes into root 
//	 */
//	public static void writeRootFlexIntoWord(WordSequences wsmodel, LetterTokModel ltmodel, int freqTHH, boolean print) {
//		//get seenRoots and seenFlexes which are the same in slices from pref and suf
//		MorphAnalyzer.computeRootFlexInitial(wsmodel, ltmodel, freqTHH);
//		//compute corrected roots and flexes
//		MorphAnalyzer.collectMoreRoots(wsmodel, freqTHH);
//		MorphAnalyzer.computeMoreRoots(wsmodel, freqTHH);
//		//compute statistics for possible suffix transitions
//		MorphAnalyzer.computeRootFlexSuffixTransitions(wsmodel, freqTHH);
////		System.out.println("PRINT SUFF TRANSITIONS 1");
////		for(String otherFlex: wsmodel.idx().seenSuffixes.keySet()) {
////			System.out.println(otherFlex  + "\t" + wsmodel.idx().seenSuffixes.get(otherFlex).toString());
////		}
//		
//		//compute new roots and flex with single transitions: where only 1 suffix transition exists
//		MorphAnalyzer.computeNewRootFlex(wsmodel, true, freqTHH);
//		////compute new roots and flex with multiple transitions: where many suffix transition exist
//		MorphAnalyzer.computeNewRootFlex(wsmodel, false, freqTHH);
//
//
//
//	}
//	
//	public static void writeRootFlexIntoModel(WordSequences wsmodel, double freqTHH, boolean print) {
//		//now do the last analysis for roots and flexes and suffixes after all gathered informations in seenRoots, seenFlexes, seenSuffixes
//		HashMap<String,List<String>> rootFlexes = new HashMap<>();
//		int i = 0;
//		for (Word w : wsmodel.idx().getSortedWords()) {
//			if (w.freq() < freqTHH)
//				continue;
//
//			i++;
//			if (i % 100000 == 0)
//				System.out.println("processed word roots: " + i);
//			String root = w.toString();
//			String flex = "_";
//			if (w.getRoot() != null)
//				root = w.getRoot();
//			if (w.getFlex() != null)
//				flex = w.getFlex();
//
//			if (!rootFlexes.containsKey(root))
//				rootFlexes.put(root, new ArrayList<>());
//			rootFlexes.get(root).add(flex);
//		}
//
//		for (String root : rootFlexes.keySet()) {
//			if (rootFlexes.get(root).size() < 2)
//				continue;
//			for (String flex : rootFlexes.get(root)) {
//				root = root.replaceAll("[\\|_]", "");
//
//				if (print)
//					System.out.println(root + "\t" + flex);
//				String wstring = root + flex;
//				wstring = wstring.replaceAll("[\\|_]", "");
//				Word thisword = wsmodel.getWord(wstring);
//				Root r = wsmodel.idx().getRoot(root);
//				thisword.setRoot(r.toString());
//				Flexion flexion = wsmodel.idx().getFlex(flex, r);
//				thisword.setFlex(flexion.toString());
//				r.addFlex(flexion);
//				r.seenWords.add(thisword);
//				flexion.roots.add(r);
//			}
//
//			if (print)
//				System.out.println();
//		}
//		
//	}
//
//	private static void buildFirstMorphParadigms( WordSequences model, boolean print, double topFlexFrequency) throws IOException {
//		MorphParadigms.buildFromRRFF(model, false, "-first", topFlexFrequency);
//		MorphParadigms.combineBestParadigms(model, print);
//	}
//	
//	
//	public static void tagForRetrain(WordSequences wsmodel) {
//		Set<String> seenRoots = new HashSet<String>();
//		
//		Map<MorphParadigm, Map<Flexion,Double>> mparsFlexFreqStats = new HashMap<>();
//		System.out.println("mpar after load: ");
//		for(String l: wsmodel.idx().getMPlabels()) {
//			MorphParadigm mpar = wsmodel.idx().getMorphParadigm(l);
//			System.out.println(mpar.toString());
//			mparsFlexFreqStats.put(mpar, new HashMap<>());
//		}
//		System.out.println("mpars SIZE:\t" + wsmodel.idx().getMPlabels().size());
//		
//		for(Word w: wsmodel.idx().getSortedWords()) {
//			if(w.getFlex() == null || w.getRoot() == null) continue;
//			if(seenRoots.contains(w.getRoot().toString())) continue;
//			Root root = wsmodel.idx().getRoot(w.getRoot());
//			if(root.seenflexes == null || root.seenflexes.size() == 0) continue;
//			seenRoots.add(root.toString());
//			
//			MorphParadigm bestParadigm = getBestParadigm(root.seenflexes, wsmodel, w.getRoot().toString(), false);
//			if(bestParadigm != null) {
//				for(Flexion f: root.seenflexes) {
//					//experiment
////					if(!bestParadigm.getFlexes().contains(f)) continue; --> paradigms become very thin - less flexes, more unfull paradigms, less mixed paradigms
//					MapsOps.addFreqObject(f, mparsFlexFreqStats.get(bestParadigm), 1.0);
//				}
//			}
//		}
//		wsmodel.idx().seenMorphParToFlexFreqMap = mparsFlexFreqStats;
//	}
//	
//	public static MorphParadigm getBestParadigmUniq(Set<Flexion> wordFlexes, WordSequences model, Word w) {
//		//return a paradigm with most count of root flexes, only if there is  1 such paradigm
//		// e.g. if a root has 4 flexes of par1 and 4 in par 2 --> return paradigm = null
//		// if a root has 4 flexes in par1 and 3 in par2, retrun par1
//		MorphParadigm bestPar = null;
//		
//		//Array to keep info about matched flexes
//		List<List<MorphParadigm>> paradigmScoreList = new ArrayList<List<MorphParadigm>>();
//		for (int i = 0; i <= wordFlexes.size(); i++) {
//			paradigmScoreList.add(new ArrayList<MorphParadigm>());
//		}
//		
//		for(String l: model.idx().getMPlabels()) {
//			MorphParadigm mpar = model.idx().getMorphParadigm(l);
//			int sumFlexesInWordAndPar = 0;
//			Set<Flexion> mparFlexes = mpar.getFlexes();
//			for(Flexion mpFlex: mparFlexes) {
//				if(wordFlexes.contains(mpFlex)) sumFlexesInWordAndPar++;
//			}
//			if(sumFlexesInWordAndPar < 2) continue;
//			
//			paradigmScoreList.get(sumFlexesInWordAndPar).add(mpar);
//			double wordSim = (double)sumFlexesInWordAndPar/wordFlexes.size();
//			double parSim = (double)sumFlexesInWordAndPar/mpar.getFlexes().size();
//			mpar.scoreSim = new Similarity(wordSim, parSim);
//		}
////		if(w.ambigParadigms2 == null) w.ambigParadigms2 = new HashSet<>();
////		for(List<MorphParadigm> l: paradigmScoreList) {
////			if(l.size() != 0)
////				w.ambigParadigms2.addAll(l);
////		}
//	
//		for (int i = paradigmScoreList.size()-1; i > 0; i--) { // wir gehen vom Ende, da dort die "besten" Paradigmen sind == die die meisten flexes vom Root haben
//			if(paradigmScoreList.get(i).size() != 0 ) {
//				if( paradigmScoreList.get(i).size() == 1 ) {
//					bestPar = paradigmScoreList.get(i).get(0);
//					if(bestPar.scoreSim.left > 0.5) return bestPar;
//					return null;
//				}
//				else {
//					w.ambigParadigms = paradigmScoreList.get(i);
//					return null; //means there were more than 1 best paradigms
//				}
//			}
//		}
//		return null;
//	}
//	
//	public static MorphParadigm getBestParadigm(Set<Flexion> wordFlexes, WordSequences model, String wroot, boolean lastTag) {
//		//score = ratio of seenFlexes(seen in particular paradigm) to the number of Flexes of this MorphParadigm
//		
//		MorphParadigm bestPar = null;
//		double bestParadigmScore = 0.0; //0.49
//		int wordflexsize = wordFlexes.size();
//
//		for(String l: model.idx().getMPlabels()) {
//			MorphParadigm mpar = model.idx().getMorphParadigm(l);
//			double sumFlexesInWordAndPar = 0.0;
//			Set<Flexion> mparFlexes = mpar.getFlexes();
//			for(Flexion mpFlex: mparFlexes) {
//				if(wordFlexes.contains(mpFlex)) sumFlexesInWordAndPar++;
//			}
//			if(sumFlexesInWordAndPar < 2) continue;
//			int parflexsize = mpar.getFlexes().size();
//			double wordSim = (double)sumFlexesInWordAndPar/wordflexsize;
//			double parSim = (double)sumFlexesInWordAndPar/parflexsize;
//			
//			double wordParSim = ( wordSim + parSim ) / 2.0;
//			mpar.score = wordParSim;
//			if(wordParSim > bestParadigmScore) {
//				bestParadigmScore = wordParSim;
//				bestPar = mpar;
//			}else if(wordParSim == bestParadigmScore && wordSim == 1.0) { // == allWordFlexesSeen
//				bestParadigmScore = wordParSim;
//				bestPar = mpar;
//			}
//		}
//			
//		return bestPar;
//	}
//	
//	public static MorphParadigm getBestParadigmSaveWork(Set<Flexion> wordFlexes, Map<String, MorphParadigm> mpars, String wroot, boolean lastTag) {
//		//score = ratio of seenFlexes(seen in particular paradigm) to the number of Flexes of this MorphParadigm
//		double scoreWord = 0.0; //0.49
//		double scorePar = 0.0; //0.49
//		
//		//for tests
//		double bestscoreWord = 0.0; //0.49
//		double bestscorePar = 0.0; //0.49
//		MorphParadigm bestWordPar = null;
//		MorphParadigm bestParPar = null;
//		
//		
//		double scoreWordRatio = 0.0; //0.49
//
//		MorphParadigm toReturn = null;
//		Set<String> wordFlexStringSet = new HashSet();
//		for(Flexion f: wordFlexes) wordFlexStringSet.add(f.toString());
//		
//		boolean allWordFlexesSeen = false;
//		
//		for(MorphParadigm mpar: mpars.values()) {
//			double sumFlexesInWordAndPar = 0.0;
//			Set<Flexion> mparFlexes = mpar.getFlexes();
//			for(Flexion mpFlex: mparFlexes) {
//				if(wordFlexStringSet.contains(mpFlex.toString())) sumFlexesInWordAndPar++;
//			}
//			if(sumFlexesInWordAndPar < 2) continue;
//			
//			int wordflexsize = wordFlexes.size();
//			int parflexsize = mpar.getFlexes().size();
//			double wordSim = (double)sumFlexesInWordAndPar/wordflexsize;
//			double parSim = (double)sumFlexesInWordAndPar/parflexsize;
//
//			if(wordSim > bestscoreWord) {
//				bestscoreWord = wordSim;
//				bestWordPar = mpar;
//			}
//			if(parSim > bestscorePar) {
//				bestscorePar = parSim;
//				bestParPar = mpar;
//			}
//			if( wordSim == 1.0 && !allWordFlexesSeen) { //first time all word flexes found - we take it
////			if( wordSim > 0.9 && !allWordFlexesSeen) {
//				allWordFlexesSeen = true;
//					scoreWord = wordSim;
//					scorePar = parSim;
//					toReturn = mpar;
//			}
//			else if(wordSim == 1.0 && allWordFlexesSeen) { //after paradigm with all word flexes found, optimise on paradigm flexes
////			else if( wordSim > 0.9 && allWordFlexesSeen) {
//				if(parSim > scorePar) {
//					scoreWord = wordSim;
//					toReturn = mpar;
//					scorePar = parSim;
//				}
//			}
//			else if(!allWordFlexesSeen) { //optimise on paradigm flexes
//				if(parSim > scorePar) {
//					scoreWord = wordSim;
//					toReturn = mpar;
//					scorePar = parSim;
//				}
//			} 
//			
////			else if(!allWordFlexesSeen) { //optimise on word flexes, favor for mixed paradigms == is bad
////			if(wordSim > scoreWord) {
////				scoreWord = wordSim;
////				toReturn = mpar;
////				scorePar = parSim;
////			}
////		} 
//
//			//clean very bad scorings
////			if(scoreWord < 0.3) toReturn = null;
//			
//		}
//		if(!lastTag) return toReturn;
////		if(toReturn == null) {
////			System.out.println("NO PARADIGM\t"  + wroot + "\t" + wordFlexes.toString() 
////			+ "\tbestWordscore\t" + MyUtils.rdouble(bestscoreWord)
////			+ "\tbestWordPar\t" + (bestWordPar == null ? "nopar" : bestWordPar.toString())
////			 + "\tbestParscore\t" + MyUtils.rdouble(bestscorePar)
////			 + "\tbestParPar\t" + (bestParPar == null ? "nopar": bestParPar.toString())
////			 );
////		}else {
////			System.out.println("YES PARADIGM\t"  + wroot + "\t" + wordFlexes.toString() 
////			+ "\tbestPar\t" + toReturn.toString() + "\t" + MyUtils.rdouble(scoreWord)  + "\t" + MyUtils.rdouble(scorePar)
////			+ "\tbestWordscore\t" + MyUtils.rdouble(bestscoreWord)
////			+ "\tbestWordPar\t" + bestWordPar.toString()
////			 + "\tbestParscore\t" + MyUtils.rdouble(bestscorePar)
////			 + "\tbestParPar\t" + bestParPar.toString()
////			);
////		}
//		
//		return toReturn;
//	}
//	
//	public static MorphParadigm getBestParadigmOld(Set<Flexion> wordFlexes, Map<String, MorphParadigm> mpars, String wroot, boolean lastTag) {
//		//score = ratio of seenFlexes(seen in particular paradigm) to the number of Flexes of this MorphParadigm
//		double score = 0.0; //0.49
//		double score2 = 0.0; //0.49
//		double scoreWordRatio = 0.0; //0.49
//
//		if(wroot.equals("агресивн") || wroot.equals("автоном"))
//			System.out.println();
//		
//		boolean paradigmHasAllFoundFlexes = false;
//		MorphParadigm toReturn = null;
//		MorphParadigm wordRatioBestParadigm = null;
//		Set<String> wordFlexesSet = new HashSet();
//		for(Flexion f: wordFlexes) wordFlexesSet.add(f.toString());
//		
//		for(MorphParadigm mpar: mpars.values()) {
//			double sumParFlexesInWord = 0.0;
//			Set<Flexion> mparFlexes = mpar.getFlexes();
//			boolean parHasAllWordflexes = true;
//			for(Flexion seenFlex: wordFlexes) {
//				if(!mparFlexes.contains(seenFlex)) {
//					parHasAllWordflexes = false;
//					break;
//				}
//				
//			}
//			
//			for(Flexion mpFlex: mparFlexes) {
//				if(wordFlexesSet.contains(mpFlex.toString())) sumParFlexesInWord++;
//			}
//			if(sumParFlexesInWord < 2) continue;
//			
//			double newRatio = (double)sumParFlexesInWord/mpar.getFlexes().size();
//			double ratioForWordFlexesInPar = (double)sumParFlexesInWord/wordFlexes.size();
//			if(ratioForWordFlexesInPar > scoreWordRatio) {
//				wordRatioBestParadigm = mpar;
//				scoreWordRatio = ratioForWordFlexesInPar;
//			}
//			if(paradigmHasAllFoundFlexes) {
//				if(parHasAllWordflexes) { //firstAll not first time -> compare score
//					paradigmHasAllFoundFlexes = true;
//					if(newRatio > score) {
//						score = newRatio;
//						score2 = ratioForWordFlexesInPar;
//						toReturn = mpar;
//					}
//				}
//			}
//			else {
//				if(parHasAllWordflexes) { //first time hasAll - take it
//					paradigmHasAllFoundFlexes = true;
//					score = newRatio;
//					score2 = ratioForWordFlexesInPar;
//					toReturn = mpar;
//				}
//				else if(newRatio > score) { // no hasAll at all -> compare score
//					score = newRatio;
//					score2 = ratioForWordFlexesInPar;
//					toReturn = mpar;
//				}
//			}
////			if(score2 < 0.5)
////				toReturn = null;
//			
//		}
//		
//		if(!lastTag) return toReturn;
//		
////		if(toReturn == null) {
////			System.out.println("NO PARADIGM\t"  + wroot + "\t" + wordFlexes.toString() + "\twordscore\t" + MyUtils.rdouble(score2));
////		}
////		else if(score < 0.5) {
////			System.out.println("RATIO BAD\t" + MyUtils.rdouble(score)  + "\t" + wroot + "\t" 
////			+ "\t" + wordFlexes.toString() + "\t" + toReturn.toString()
////			);
////		}
////		if(toReturn != null && score2 < 0.5) {
////			System.out.print("WORDRATIO BAD\t" + MyUtils.rdouble(score2) + "\t" + wroot + "\t" 
////			+ "\t" + wordFlexes.toString() + "\t" + toReturn.toString()
////			+ "\tratio is\t" + + MyUtils.rdouble(score));
////			if(wordRatioBestParadigm != null) System.out.println("\tBest WordRatioPar\t" + wordRatioBestParadigm + "\t" + MyUtils.rdouble(scoreWordRatio));
////			else System.out.println();
////		}
//
//		return toReturn;
//	}
//	
//	public static void sortOutParadigmsWithLessWordMembers(WordSequences wsmodel, int thhPar, int thhFlexPercent) {
//		System.out.println();
//		int thh = 10;
//		thh = MorphParadigms.computeFlexFreqAndGetTHH(wsmodel,100);
////		thh = 5;
//		System.out.println("THH par\t" + thhPar);
//		
//		List<MorphParadigm> bestPars = new ArrayList<>();
//		for(MorphParadigm mpar: wsmodel.idx().seenMorphParToFlexFreqMap.keySet()) {
//			boolean isGOODpar = checkHasEnoughWords(wsmodel.idx().seenMorphParToFlexFreqMap.get(mpar), thh);
//			if(isGOODpar) {
//				MorphParadigm cleanedMpar = cleanParadigmFromSeldomFlexes(mpar, wsmodel.idx().seenMorphParToFlexFreqMap.get(mpar), 10, wsmodel);
//				bestPars.add(cleanedMpar);
//			}
////			else if(mpar.getFlexes().size() > 0)
////				System.out.println("BAD PAR\t" + mpar.toString() + "\t" + wsmodel.idx().seenMorphParToFlexFreqMap.get(mpar).toString());
//		}
//	}
//	
//	public static void cleanParadigmsNonFrequentMembers(WordSequences wsmodel, int thhPar, int thhFlexPercent) {
//		
//		List<MorphParadigm> bestPars = new ArrayList<>();
//		for(String mpstring: wsmodel.idx().getMPlabels()) {
//			MorphParadigm mpar = wsmodel.idx().getMorphParadigm(mpstring);
//			if(mpar == null) continue;				
//			int thhFlex = MorphParadigms.getFreqTHH(mpar.getFlexFreqMap(), thhFlexPercent);
//				if(thhFlex ==1) thhFlex = 2;
//				System.out.println("THH flex\t" + thhFlex + "\tfor par\t" + mpar.getFlexFreqMap().toString());
//				MorphParadigm cleanedMpar = cleanParadigmFromSeldomFlexesString(wsmodel, mpar, mpar.getFlexFreqMap(), thhFlex);
//				bestPars.add(cleanedMpar);
//		}
//	}
//	
//	private static MorphParadigm cleanParadigmFromSeldomFlexesString(WordSequences wsmodel, MorphParadigm mpar, Map<String, Double> flexFreqStats, int thhFlex) {
//		Set<Flexion> flexset = new HashSet<>();
//		Map<String, Double> flexFreqMap = new HashMap<>();
////		int thh = 10;
////		thh = 1;
//		for(String flex: flexFreqStats.keySet()) {
//			double freq = flexFreqStats.get(flex);
//			if(freq >= thhFlex ) {
//				flexset.add(wsmodel.idx().getFlex(flex)); 
//				flexFreqMap.put(flex.toString(), freq);
//			}
//		}
//		return wsmodel.idx().getNewMorphParadigm(flexset, flexFreqMap, null);
//	}
//	
//	private static MorphParadigm cleanParadigmFromSeldomFlexes(MorphParadigm mpar, Map<Flexion, Double> flexFreqStats, int thhFlex, WordSequences model) {
//		Set<Flexion> flexset = new HashSet<>();
//		Map<String, Double> flexFreqMap = new HashMap<>();
////		int thh = 10;
////		thh = 1;
//		for(Flexion flex: flexFreqStats.keySet()) {
//			double freq = flexFreqStats.get(flex);
//			if(freq >= thhFlex  && flex.freq() > 10) {
//				flexset.add(flex); 
//				flexFreqMap.put(flex.toString(), freq);
//			}
//		}
//		return model.idx().getNewMorphParadigm(flexset, flexFreqMap, null);
//	}
//
//	private static boolean checkHasEnoughWords(Map<Flexion, Double> flexFreqsInParadigm, int thh) {
//		for(double d: flexFreqsInParadigm.values()) {
//			if(d >= thh) return true;
//		}
//		return false;
//	}
//	
//
//
//	public static MorphParadigm getBestParadigmAmbig(Word w, Map<String, List<String>> mparContextsMapRight,
//			Map<String, List<String>> mparContextsMapLeft, WordSequences model, int contextcount) {
//		if(w.ambigParadigms == null) return null;
//		
//		List<String> bestContextsLeft = new ArrayList<>();
//		for(MyPairWord key: w.getBestContexts(true, contextcount, Words.ALLPARS_FILTER, model, false)){
//			bestContextsLeft.add(key.left.toString());
//		}		
//		List<String> bestContextsRight = new ArrayList<>();
//		for(MyPairWord key: w.getBestContexts(false, contextcount, Words.ALLPARS_FILTER, model, false)){
//			bestContextsRight.add(key.left.toString());
//		}
//		double bestScore = Double.MAX_VALUE;
//		MorphParadigm bestPar = null; 
//		boolean twoBestPars = false;
//		for(MorphParadigm mp: w.ambigParadigms) {
//			String mparLable = mp.getLabel();
////			double scoreL = getCentricSimMeasure(mparContextsMapLeft.get(mparLable), bestContextsLeft);
////			double scoreR = getCentricSimMeasure(mparContextsMapRight.get(mparLable), bestContextsRight);
//			double scoreL = Cluster.getScaledSimMeasure(bestContextsLeft, SetOps.getSet(mparContextsMapLeft.get(mparLable) ));
//			double scoreR = Cluster.getScaledSimMeasure(bestContextsRight, SetOps.getSet(mparContextsMapRight.get(mparLable) ));
//			double parScore = (scoreL + scoreR) /2;
////			System.out.println(w.toString() + "\t" + mp.getLabel() + " " + scoreL + " " + scoreR+" = "+parScore);
//			if(parScore == bestScore) {
//				twoBestPars = true;
//				continue; // means 2 pars have same score
//			}
//			if(parScore < bestScore) {
//				twoBestPars = false;
//				bestScore = parScore;
//				bestPar = mp;
//			}
//		}
//		if(twoBestPars) return null;
//		return bestPar;
//	}
//	
//	//researh
//	public static Pair<MorphParadigm, Double> getBestParadigmAmbig2(Word w, Map<String, List<String>> mparContextsMapRight,
//			Map<String, List<String>> mparContextsMapLeft, String regexForParadimFilter, 
//			WordSequences model, boolean print, int contextcount) {
//		
//		List<String> bestContextsLeft = new ArrayList<>();
//		for(MyPairWord key: w.getBestContextsComputeNew(true, contextcount, regexForParadimFilter, model, false)){
//			bestContextsLeft.add(key.left.toString());
//		}		
//		List<String> bestContextsRight = new ArrayList<>();
//		for(MyPairWord key: w.getBestContextsComputeNew(false, contextcount, regexForParadimFilter, model, false)){
//			bestContextsRight.add(key.left.toString());
//		}
//		double bestScore = Double.MAX_VALUE;
//		MorphParadigm bestMP = null;
//		for(String mpstring: model.idx().getMPlabels()) {
//			MorphParadigm mp = model.idx().getMorphParadigm(mpstring);
//			if(mp == null) continue;
//			String mparLable = mp.getLabel();
//			double scoreL = Cluster.getScaledSimMeasure(bestContextsLeft, SetOps.getSet(mparContextsMapLeft.get(mparLable)) );
//			double scoreR = Cluster.getScaledSimMeasure(bestContextsRight, SetOps.getSet(mparContextsMapRight.get(mparLable))  );
//			double parScore = (scoreL + scoreR) /2;
//			 scoreL = Cluster.getScaledSimMeasure(mparContextsMapLeft.get(mparLable), SetOps.getSet(bestContextsLeft) );
//			 scoreR = Cluster.getScaledSimMeasure(mparContextsMapRight.get(mparLable), SetOps.getSet(bestContextsRight) );
//			 parScore = (parScore + ((scoreL + scoreR) /2)) / 2;
//			 if(print) System.out.println(parScore + "\t" + mp.toString());
//			 if(parScore < bestScore ) { //&& checkFlexion(mp, w, model)) {
//				 bestScore = parScore;
//				 bestMP = mp;
//			 }
//		}
////		if(!checkFlexion(bestMP, w, model)) bestMP = null;
////		if(!checkRoot(bestMP, w, model)) bestMP = null;
////		if(bestMP != null) 
////			System.out.println("PASSED:\t" + w.toString() + "\t" + bestMP.toString());
//		if(bestScore > MorphVectorAnalyzer.getScaledVectorSimilarityThh(contextcount)) return null;
//		if(bestMP == null) return null;
//		return new Pair<MorphParadigm, Double>(bestMP, bestScore);
//
//	}
//	
////	public static Pair<String, Double> getBestParadigmAmbig2Vector(Word w, Map<String, 
////	    List<String>> mparContextsMapRight,	Map<String, List<String>> mparContextsMapLeft,
////	    String regexForParadimFilter, WordSequences model, boolean print, 
////	    boolean allowFreqOne, int contextcount) {
////		
////		List<String> bestContextsLeft = new ArrayList<>();
////		for(MyPairWord key: w.getBestContextsComputeNew(true, contextcount, regexForParadimFilter, model, allowFreqOne)){
////			bestContextsLeft.add(key.left.toString());
////		}		
////		List<String> bestContextsRight = new ArrayList<>();
////		for(MyPairWord key: w.getBestContextsComputeNew(false, contextcount, regexForParadimFilter, model, allowFreqOne)){
////			bestContextsRight.add(key.left.toString());
////		}
////		double bestScore = Double.MAX_VALUE;
////		String bestFlexMP = null;
//////		double verybestScore = Double.MAX_VALUE;
//////		String verybestFlexMP = null;
////		List<MyPair> tmpListForPrint = new ArrayList<>();
////		for(String fl: mparContextsMapRight.keySet()) { //left or right, both have same flexes
////			double parScore = getParadigmScores(mparContextsMapRight, mparContextsMapLeft, bestContextsLeft,
////					bestContextsRight, fl);
////			 if(print) tmpListForPrint.add(new MyPair(fl, "", parScore)); 
////			 if(parScore < bestScore ) {// && checkFlexionOfFlexPar(fl, w)) { //&& checkFlexion(mp, w, model)) {
////				 bestScore = parScore;
////				 bestFlexMP = fl;
////			 }
////			 else if(parScore == bestScore) {
////				 String bestFlex = MorphModel.getFlexFromFlexPar(bestFlexMP, MorphModel.FPREF);
////				 if(!w.toString().endsWith(bestFlex)) {
////					 String flex = MorphModel.getFlexFromFlexPar(fl,MorphModel.FPREF);
////					 if(flex.equals("_")  ) flex = "";
////					 if(w.toString().endsWith(flex)) {
////						 bestScore = parScore;
////						 bestFlexMP = fl;
////					 }
////				 }
////			 }
//////			 if(parScore < verybestScore  ) { 
//////				 verybestScore = parScore;
//////				 verybestFlexMP = fl;
//////			 }
////		}
////		
////		if(print) {
////			Collections.sort(tmpListForPrint);
////			for(MyPair p: tmpListForPrint)
////				System.out.println(p.freq + "\t" + p.first);
////		}
////		
////		if(bestScore > MorphVectorAnalyzer.getScaledVectorSimilarityThh(contextcount)) return null;
////		if(bestFlexMP == null) return null;
////		return new Pair<String, Double>(bestFlexMP, bestScore);
////		
////	}
//	
//	public static Pair<String, Double> checkConflicts(Pair<String, Double>bestFlexMP, Word w,  WordSequences model, Map<String, List<String>> mparContextsMapRight,
//			Map<String, List<String>> mparContextsMapLeft, int contextcount) {
//		//check null-Flexion OR a new (waiting) flexion again
//		if (bestFlexMP == null) return null;
//		boolean hasConflict = false;
//		hasConflict = checkFlexionConflict(bestFlexMP.getKey(), w, model, mparContextsMapRight, mparContextsMapLeft, contextcount);
//		hasConflict = checkRootConflict(bestFlexMP.getKey(), w, model, hasConflict, mparContextsMapRight, mparContextsMapLeft, contextcount);
//		if(!hasConflict) return bestFlexMP;
//		
//		bestFlexMP = checkNewRoot(bestFlexMP.getKey(), w, model, mparContextsMapRight, mparContextsMapLeft, contextcount);
//
//		return bestFlexMP;
//	}
//
//	private static Pair<String, Double> checkNewRoot(String bestFlexMP, Word w, WordSequences model, 
//	    Map<String, List<String>> mparContextsMapRight, Map<String, List<String>> mparContextsMapLeft, int contextcount) {
//		//check new root and add waiting flex
//		MorphParadigm mpComputed = model.idx().getMorphParadigm(MorphModel.getMPlabelFromFlexPar(bestFlexMP));
//
//			Set<Root> bucketRoots = model.idx().getPrefixBucketRoot(w);
//			List<Root> rSorted = bucketRoots.stream().sorted((a,b) -> (-1)*(a.toString().length()-b.toString().length())).collect(Collectors.toList());
//			int countTriedRoots = 0;
//			for(Root newRoot: rSorted) {
//				if(countTriedRoots > 3) break;
//				if(newRoot.toString().length()<2) break;
//				if(!w.toString().startsWith(newRoot.toString())) continue;
//				countTriedRoots++;
//				
//				String newFlex = w.toString().substring(newRoot.toString().length());
//				if(newFlex.length()==0) newFlex = "_";
//				MorphParadigm newPar = confirmFlexAndGetParadigm(newRoot, newFlex, model, w);
//				if(newPar != null) 
//					return new Pair<String, Double>(MorphModel.getFlexLabel(w.getFlex(), newPar.getLabel(),MorphModel.FPREF), 1.0);
//				
//				if( confirmRootFlexInParadigm(mpComputed, newRoot, model.idx().getFlex(newFlex), 
//				    model, mparContextsMapRight, mparContextsMapLeft, contextcount)) {
//					String newFlexString = w.toString().substring(newRoot.toString().length());
//					if(newFlexString.length() == 0) newFlexString = "_";
//					//seiner--> roots seine sein, should check also sein!!!!
//					if(!mpComputed.containsFlex(model.idx().getFlex(newFlexString))) {
//						mpComputed.addWaitingFlex(newFlexString, 1.0); //w.freq()
//						mpComputed.addWaitingFlexWord(newFlexString, w);
//					}
//					break;
//				}
//			}
//		return null;
//	}
//
//	private static boolean checkRootConflict(String bestFlexMP, Word w, WordSequences model, 
//	    boolean hasFLexConflict, Map<String, List<String>> mparContextsMapRight, 
//	    Map<String, List<String>> mparContextsMapLeft, int contextcount) {
//		String computedParFlex = MorphModel.getFlexFromFlexPar(bestFlexMP, MorphModel.FPREF);
//		MorphParadigm mpComputed = model.idx().getMorphParadigm(MorphModel.getMPlabelFromFlexPar(bestFlexMP));
//		//to avoid suffixes as waiting flexes - try first the whole word as a root: Flugzeugbauer, Unternehmen
//		Root wordroot =  model.idx().getRoot(w.toString());
//		String zeroFlex= "_";
//		if(mpComputed.containsFlex(model.idx().getFlex(zeroFlex)) && 
//				confirmRootFlexInParadigm(mpComputed, wordroot, model.idx().getFlex(zeroFlex), model, mparContextsMapRight, mparContextsMapLeft, contextcount))
//			return false;
//		if(!hasFLexConflict) {//means word ends with flex
//			Root r = model.idx().getRoot(w.toString().substring(0, w.toString().length()-computedParFlex.length()));
//			String newFlex = w.toString().substring(r.toString().length());
//			if(confirmRootFlexInParadigm(mpComputed, r, model.idx().getFlex(newFlex), model, mparContextsMapRight, mparContextsMapLeft, contextcount))
//				return false;
//		}
//
//		return true;
//	}
//
//	public static MorphParadigm confirmFlexAndGetParadigm(Root newRoot, String newFlex, WordSequences model, Word w) {
//
////		if(w.getMorphParadigm() != null) return w.getMorphParadigm();
////		if(w.getRoot() != null && w.getRoot().equals(newRoot.toString()) ) 
////			return w.getMorphParadigm();
//		
//		MorphParadigm mp = newRoot.getMainParadigm(model);
//		if(mp == null) return null;
//		if(mp.getFlexes().contains(model.idx().getFlex(newFlex))) {
//			System.out.println("NEW CHANGE:\t" + w.toString() + "\t" + w.getFlex() + "\t" + w.getMorphLabelNotNull() 
//			+ "\tnewFlex:\t" + newFlex + "\tnew par:\t" + mp.getLabel());
//			w.setRoot(newRoot.toString());
//			w.setFlex(newFlex);
//			w.changeMorphParadigm(mp);
//			return mp;
//		}
//		else return null;
//		
//	}
//
//	private static boolean confirmRootFlexInParadigm( MorphParadigm mpComputed, Root r, Flexion newFlex, 
//	    WordSequences model, Map<String, List<String>> mparContextsMapRight, 
//	    Map<String, List<String>> mparContextsMapLeft, int contextcount) {
//		int countFlexes = 0;
//		if(mpComputed.containsFlex(newFlex)) countFlexes++;
//		for(Flexion paradigmFlex: mpComputed.getFlexes()) {
//			if(paradigmFlex.toString().equals(newFlex.toString())) continue;
//			//angenommen computed paradigm; f_e_m_2 fpr word Beteiligungs, root: Beteiligung, flex s, check m_ flexes with Beteiligung,
//			//if there are such words in the model and if they could get into the same paradigm. if so, tag all of them with paradigm
//			Word newParadigmWord = model.getWord(r.toString()+paradigmFlex.toString());
//			if(checkWordExistsAndSameParadigm(newParadigmWord, model, mpComputed.getLabel(), mparContextsMapRight, mparContextsMapLeft, contextcount)) {
//				countFlexes++;
//				if(countFlexes > 1)	
//					return true;
//			}
//		}
//		return false;
//	}
//
////	private static boolean checkWordExistsAndSameParadigm(Word newParadigmWord, WordSequences model, 
////	    String computedLabel, Map<String, List<String>> mparContextsMapRight, 
////	    Map<String, List<String>> mparContextsMapLeft, int contextcount) {
////		if(newParadigmWord.freq() < 2) return false;
////		if(newParadigmWord.getMorphParadigm() != null && newParadigmWord.getMorphParadigm().getLabel().equals(computedLabel))
////			return true;
////		Pair<String, Double> 	bestpar = model.idx().flexPar777.get(newParadigmWord);
////		if(bestpar == null) 	bestpar = MorphParTrain.getBestParadigmAmbig2Vector(
////				newParadigmWord, mparContextsMapRight, mparContextsMapLeft, Words.SYNSEM_FILTER, model, false, true, contextcount);
////		if(bestpar != null && bestpar.getKey().endsWith(computedLabel)) //
////			return true;
////		return false;
////	}
//
//	/**
//	 * Checks if word ends with paradigm flexion. if flex == Zero (_) - makes additional check 
//	 * @param bestFlexMP
//	 * @param w
//	 * @param mparContextsMapLeft 
//	 * @param mparContextsMapRight 
//	 * @return true = has conflict, false = no conflict
//	 */
//	public static boolean checkFlexionConflict(String bestFlexMP, Word w, WordSequences model, 
//	    Map<String, List<String>> mparContextsMapRight, Map<String, List<String>> mparContextsMapLeft, int contextcount) {
//		String parFlex = MorphModel.getFlexFromFlexPar(bestFlexMP, MorphModel.FPREF);
//		if(parFlex.equals("_")) return checkZeroFlexConflict(bestFlexMP, w, model, mparContextsMapRight, mparContextsMapLeft, contextcount);
//		if(w.toString().endsWith(parFlex)) 
//			return false;
//		return true;
//	}
//	
//	public static boolean checkFlexionConflictLight(String bestFlexMP, WordSequences model) {
//		String parFlex = MorphModel.getFlexFromFlexPar(bestFlexMP, MorphModel.FPREF);
//		Flexion f = model.idx().getFlex(parFlex);
//		String mpString = MorphModel.getMPlabelFromFlexPar(bestFlexMP);
//		MorphParadigm mp = model.idx().getMorphParadigm(mpString);
//		if(mp.containsFlex(f)) return false;
//		return true;
//	}
//
//	/**
//	 * false means no conflicts
//	 * @param bestFlexMP
//	 * @param w
//	 * @param model
//	 * @param mparContextsMapRight
//	 * @param mparContextsMapLeft
//	 * @return
//	 */
//	private static boolean checkZeroFlexConflict(String bestFlexMP, Word w, WordSequences model, 
//	    Map<String, List<String>> mparContextsMapRight, Map<String, List<String>> mparContextsMapLeft, int contextcount) {
//		MorphParadigm mp = model.idx().getMorphParadigm(MorphModel.getMPlabelFromFlexPar(bestFlexMP));
//		for(Flexion parFlex: mp.getFlexes()) {
//			if(parFlex.toString().equals("_")) continue;
//			Word wordform = model.getWord(w.toString()+parFlex.toString());
//			if(wordform.getMorphParadigm() != null && wordform.getMorphParadigm().getLabel().equals(mp.getLabel()))
//				return false;
//			else {//try to tag the alternative form
//				if(checkWordExistsAndSameParadigm(wordform, model, mp.getLabel(), mparContextsMapRight, mparContextsMapLeft, contextcount))
//					return false;
//			}
//		}
//		return true;
//	}
//
//	private static double getParadigmScores(Map<String, List<String>> mparContextsMapRight,
//			Map<String, List<String>> mparContextsMapLeft, List<String> bestContextsLeft,
//			List<String> bestContextsRight, String fl) {
//		double scoreL = Cluster.getScaledSimMeasure(bestContextsLeft, SetOps.getSet(mparContextsMapLeft.get(fl)) );
//		double scoreR = Cluster.getScaledSimMeasure(bestContextsRight, SetOps.getSet(mparContextsMapRight.get(fl))  );
//		double parScore = (scoreL + scoreR) /2;
//		 scoreL = Cluster.getScaledSimMeasure(mparContextsMapLeft.get(fl), SetOps.getSet(bestContextsLeft) );
//		 scoreR = Cluster.getScaledSimMeasure(mparContextsMapRight.get(fl), SetOps.getSet(bestContextsRight) );
//		 parScore = (parScore + ((scoreL + scoreR) /2)) / 2;
//		return parScore;
//	}
//
//	private static boolean checkFlexion(MorphParadigm mp, Word w, WordSequences model) {
//		if(w.getFlex() == null) return true;
//		String flex = w.getFlex();
//		if(mp.containsFlex(model.idx().getFlex(flex))) return true;
//		return false;
//	}
//	
//	private static boolean checkRoot(MorphParadigm mp, Word w, WordSequences model) {
//		if(w.getRoot() == null) return false;
//		if(mp == null) return false;
//		String root = w.getRoot();
//		int foundFlexes = 0;
//		for(Flexion f: mp.getFlexes()) {
//			if(model.idx().words.containsKey(root+f.toString().replace("_", "")))
//				foundFlexes++;
//		}
//		if(foundFlexes > 1) return true;
//		System.out.println("ROOT check failed:\t" + w.toString() + "\t" + mp.toString());
//		return false;
//	}
//	private static boolean checkFlexionOfFlexPar(String flexPar, Word w) {
//		String realFlex = MorphModel.getFlexFromFlexPar(flexPar, MorphModel.FPREF);
//		if(realFlex.equals("_")) return true;
//		if(w.toString().endsWith(realFlex)) return true;
//		return false;
//	}	
//}
