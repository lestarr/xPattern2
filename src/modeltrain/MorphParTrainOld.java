//package modeltrain;
//
//import java.io.IOException;
//import java.io.Writer;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import model.LetterTokModel;
//import model.WordSequences;
//import model.WordsOld;
//import modelparts.Flexion;
//import modelparts.MorphParadigm;
//import modelparts.MorphParadigmsOld;
//import modelparts.Root;
//import modelparts.Word;
//import processText.mainModels.BuildWordSequenceModel;
//import processText.mainModels.BuildWordSequenceModelDE;
//import processText.mainModels.BuildWordSequenceModelUKR;
//import util.CorpusUtils;
//import util.MapsOps;
//import util.MyPair;
//import util.MyUtils;
//
//public class MorphParTrainOld {
//	
//	static Writer outClusters = MyUtils.getWriter("out/cluster/clusterMorph.txt");
//	
//	public static List<MorphParadigm> trainCLusters(WordSequences wsmodel, LetterTokModel ltmodel) throws IOException {
//		System.out.println("Training clusters");
//		boolean print = false;
//		
//		writeRootFlexIntoWord(wsmodel, ltmodel, print); //this writes root and flexes into model
//		
//		//PRINT FLEXES Freq
////		System.out.println("FLEXES freq:");
////		MapsOps.printSortedMap(wsmodel.idx().getSortedFlexes(), null, 9);
//		double topFlexFrequency = getTopFlexFrequency(wsmodel);
//		double flexFreqTHH = 0.0; //topFlexFrequency / 1000; //topFlexFrequency/100;
//		
//		System.out.println(wsmodel.getInfo("компенсувати"));
////		MorphParadigms mpars = MorphParadigms.buildFromRRFF(wsmodel, false, "1", flexFreqTHH);
//
////		Map<String,Set<String>>suffToFlex = mpars.findSuffixes(wsmodel);
////		System.out.println("SUFFIXES found:\t" + suffToFlex.toString());
////		correctSufInFlexes(wsmodel, suffToFlex, false);
//		//PRINT FLEXES Freq
////		System.out.println("FLEXES freq:");
////		MapsOps.printSortedMap(wsmodel.idx().getSortedFlexes(), null, 9);
//		
////		int sizeBeforeCombine = mpars.paradigms.size();
////		printStarRain = true;
////		Map<String, Map<String, Double>> clusterCenterToFlexFreq = mpars.combineParadigms5(printStarRain);
//		
//		//clean MorphParadigms with old suffixes, roots and flexes
////		wsmodel.idx().roots.clear();
////		wsmodel.idx().flexes.clear();
////		wsmodel.idx().morphPars().clear();
//		
////		System.out.println("size before combine: " + sizeBeforeCombine);
//		boolean printStarRain = false;
//		Map<String, Map<String, Double>> clusterCenterToFlexFreq = buildFirstMorphParadigms(wsmodel, printStarRain, flexFreqTHH);
//		 /*
//		//this should be run again, otherways bad results. second run uses suffixes!!!
//		rrff = getAnalyzedRootFlex(wsmodel, print); 		System.out.println("rrff 2 built");
//		clusterCenterToFlexFreq = buildFirstMorphParadigms(rrff, wsmodel);
//		//end run
//		
//		//this should be run again, otherways bad results. second run uses suffixes!!!
//		rrff = getAnalyzedRootFlex(wsmodel, print); 		System.out.println("rrff 3 built");
//		clusterCenterToFlexFreq = buildFirstMorphParadigms(rrff, wsmodel);
//		//end run
//		
//		
//		//this should be run again, otherways bad results. second run uses suffixes!!!
//		rrff = getAnalyzedRootFlex(wsmodel, print); 		System.out.println("rrff 4 built");
//		clusterCenterToFlexFreq = buildFirstMorphParadigms(rrff, wsmodel);
//		//end run
//		*/
//		
//		List<MorphParadigm> mparList = new ArrayList<>();
//		
//		for(String id: clusterCenterToFlexFreq.keySet()) {
//			Set<Flexion> flexSet = new HashSet<>();
//			for(String flexString: clusterCenterToFlexFreq.get(id).keySet()) {
//				Flexion flex = wsmodel.idx().getFlex(flexString);
//				flexSet.add(flex);
//			}
//			MorphParadigm mpar = new MorphParadigm(flexSet, clusterCenterToFlexFreq.get(id));
//			mparList.add(mpar);
//		}
//		
//		return mparList;
//	}
//	
//	private static void correctSufInFlexes(WordSequences model, Map<String, Set<String>> suffToFlex, boolean print) {
//		suffToFlex = deleteSubSuffixes(suffToFlex);
//		Map<String,String> flexToSuff = new HashMap<>();
//		for(String suff: suffToFlex.keySet()) {
//			for(String flexWithSuff: suffToFlex.get(suff))
//				flexToSuff.put(flexWithSuff, suff);
//		}
//		if(print) {
//			System.out.println("Flexes collected first");
//			for(Flexion f1: model.idx().flexes.values())
//				if(f1.roots.size() > 30)
//					System.out.println(f1.toString()+"\t" + f1.roots.size()+"\t" + f1.roots.toString());
//		}
//
//			Set<Root>rootsToTest = new HashSet<Root>();
//			for(String rString: model.idx().roots.keySet()) {
//				rootsToTest.add(model.idx().getRoot(rString));
//			}
//			for(Root r: rootsToTest) {
//				if(r.toString().startsWith("логічн"))
//					System.out.println("here");
//				Set<Flexion> thisRootFlexes = new HashSet<>();
//				for(Flexion thisRootFlex: r.seenflexes) {
//					thisRootFlexes.add(thisRootFlex);
//				}
//				Map<String,Double> suffFreq = new HashMap<>();
//				for(Flexion f: thisRootFlexes) {
//					if(flexToSuff.containsKey(f.toString())) {
//						MapsOps.addFreq(flexToSuff.get(f.toString()), suffFreq);
//					}
//				}
//				
//				for(String suff: suffFreq.keySet()) { //if many suffixes possible -> clean all
//					int countFoundSuff = 0;
//					for(Flexion f: thisRootFlexes) {
//						if(f.toString().startsWith(suff))
//							countFoundSuff++;
//					}
//					if(countFoundSuff == thisRootFlexes.size() ) {
//						correctSuffs(suff, r, thisRootFlexes, model, false);
//					}
//					else if( countFoundSuff > 0) {
//						correctSuffs(suff, r, thisRootFlexes, model, false);
//					}
//				}
//
//			}
//
//
//		if(print) {
//			System.out.println("Flexes coRRected");
//			for(Flexion f1: model.idx().flexes.values())
//				if(f1.roots.size() > 30)
//					System.out.println(f1.toString()+"\t" + f1.roots.size()+"\t" + f1.roots.toString());
//		}
//	}
//
//	/**
//	 * if there are suffixes with flexes, which are all included in the set of other suffix - e.g.
//	 * ки=[кий, ким] is complete in к=[кої, ків, ком, ку, кий, кій, ким, ка, кам, ких, к, кою, ке, кі, ки, ках]
//	 * --> so delete the longer suffix and leave the suffix with more complete paradigm
//	 * @param suffToFlex
//	 * @return
//	 */
//	private static Map<String, Set<String>> deleteSubSuffixes(Map<String, Set<String>> suffToFlex) {
//		Set<String> suffToDelete = new HashSet<>();
//		for(String suff: suffToFlex.keySet()) {
//			for (int i = 0; i < suff.length()-1; i++) {
//				String subSuff = suff.substring(0,i+1);
//				if(suffToFlex.containsKey(subSuff)) {
//					Set<String> shorterSuffixFlexes = suffToFlex.get(subSuff);
//					double sumFoundInShorterSuff = 0.0;
//					for(String longerSuffixFlex: suffToFlex.get(suff)) {
//						if(shorterSuffixFlexes.contains(longerSuffixFlex)) sumFoundInShorterSuff++;
//					}
//					double scoreInShorterSuff = sumFoundInShorterSuff / (double)suffToFlex.get(suff).size();
//					if(scoreInShorterSuff > 0.65) { //at least 2/3
//						suffToDelete.add(suff);
//						break;
//					}
//				}
//			}
//		}
//		for(String suffDel: suffToDelete)
//			suffToFlex.remove(suffDel);
//		return suffToFlex;
//	}
//
//	private static void correctSuffs(String suff, Root r, Set<Flexion> thisRootFlexes, WordSequences model, boolean print) {
//		Root newRoot = model.idx().getRoot(r.toString()+suff);
//		if(model.idx().seenRoots.contains(r.toString())  ) 
//			return;
//		for(Flexion f: thisRootFlexes) {
//			if(!f.toString().startsWith(suff)) continue;
//			if(f.toString().length() < suff.length()) {
//				System.out.println("STRANGE FLEX::: " + f.toString() + "\t" + suff);
//				continue;
//			}
//			String newFlexString = f.toString().substring(suff.length());
//			if(newFlexString.length() == 0) newFlexString = "_";
//			Flexion newFlex = model.idx().getFlex(newFlexString);
//			newFlex.addRoot(newRoot);
//			newRoot.addFlex(newFlex);
//			newRoot.seensuffs.add(suff);
//			r.seenflexes.remove(f);
//		}
//		for(Word w: r.getWords()) {
//			if(w.getFlex() == null) continue;
//			if(!w.getFlex().toString().startsWith(suff)) continue;
//			w.setRoot(newRoot.toString());
//			String wString = w.toString();
//			if(newRoot.toString().length() > wString.length()) {
//				System.out.println("STRANGE WORD::: " + newRoot.toString() + "\t" + wString);
//				continue;
//			}
//			String newFlexString = wString.substring(newRoot.toString().length());
//			if(newFlexString.length() == 0) newFlexString = "_";
//			Flexion newFlex = model.idx().getFlex(newFlexString);
//			w.setFlex(newFlex.toString());
//			if(print)
//				System.out.println("NEW ROOTFLEX:\t" + w.getRoot() + "\t" + w.getFlex() + "\t" + suff);
//		}
//	}
//	
//	public static double getTopFlexFrequency(WordSequences model) {
//		if(model.idx().flexes == null) return 0.0;
//		//retur freq of most frequent flex
//		return MapsOps.getFirst(model.idx().getSortedFlexes()).freq;
//	}
//
//	/**
//		 * gets slices, sets root and flex in word, writes roots into flex and flexes into root 
//		 * @param wsmodel
//		 * @param ltmodel
//		 * @param print
//		 */
//		public static void writeRootFlexIntoWord(WordSequences wsmodel, LetterTokModel ltmodel, boolean print) {
//			HashMap<String,List<String>> rootFlexes = new HashMap<>();
//			int i = 0;
//			int freqTHH = 5;
//			//get seenRoots and seenFlexes which are the same in slices from pref and suf
//			WordsOld.computeRootFlexInitial(wsmodel, ltmodel, freqTHH);
//			//compute corrected roots and flexes
//			WordsOld.collectMoreRoots(wsmodel, freqTHH);
//			WordsOld.computeMoreRoots(wsmodel, freqTHH);
//			//compute statistics for possible suffix transitions
//			WordsOld.computeRootFlexSuffixTransitions(wsmodel, freqTHH);
//			System.out.println("PRINT SUFF TRANSITIONS 1");
//			for(String otherFlex: wsmodel.idx().seenSuffixes.keySet()) {
//				System.out.println(otherFlex  + "\t" + wsmodel.idx().seenSuffixes.get(otherFlex).toString());
//			}
//			
//			//compute new roots and flex with single transitions: where only 1 suffix transition exists
//			WordsOld.computeNewRootFlex(wsmodel, true, freqTHH);
//			////compute new roots and flex with multiple transitions: where many suffix transition exist
//			WordsOld.computeNewRootFlex(wsmodel, false, freqTHH);
//
//
//			//now do the last analysis for roots and flexes and suffixes after all gathered informations in seenRoots, seenFlexes, seenSuffixes
//			for(Word w: wsmodel.idx().getSortedWords()) {
//				if(w.freq() < freqTHH) continue;
//				
//				i++;
//				if(i%100000 == 0) System.out.println("processed word roots: "+ i);
//				String root = w.toString();
//				String flex = "_";
//				if(w.getRoot() != null) root = w.getRoot();
//				if(w.getFlex() != null) flex = w.getFlex();
//				
//				if(!rootFlexes.containsKey(root)) rootFlexes.put(root, new ArrayList<>());
//				rootFlexes.get(root).add(flex);
//			}
//
//			for(String root: rootFlexes.keySet()) {
//				if(rootFlexes.get(root).size() < 2) continue;
//				for(String flex: rootFlexes.get(root)) {
//					root = root.replaceAll("[\\|_]", "");
//
//					if(print) System.out.println(root+"\t"+flex);
//					String wstring = root+flex;
//					wstring = wstring.replaceAll("[\\|_]", "");
//					Word thisword = wsmodel.getWord(wstring);
//					Root r = wsmodel.idx().getRoot(root);
//					thisword.setRoot(r.toString());
//					Flexion flexion = wsmodel.idx().getFlex(flex, r);
//					thisword.setFlex(flexion.toString());
//					r.addFlex(flexion);
//					r.seenWords.add(thisword);
//					flexion.roots.add(r);
//				}
//					
//				if(print) System.out.println();
//			}
//		}
//
//	private static Map<String, Map<String, Double>> buildFirstMorphParadigms( WordSequences model, boolean print, double topFlexFrequency) throws IOException {
//		MorphParadigmsOld mpars = MorphParadigmsOld.buildFromRRFF(model, false, "2", topFlexFrequency);
////		for(MorphParadigm mp: mpars.paradigms.values()) System.out.println(mp.toString());
////		Map<String,Set<String>>suffToFlex = mpars.findSuffixes(model);
//
//		int sizeBeforeCombine = mpars.paradigms.size();
//		boolean printStarRain = false;
////		printStarRain = true;
//		Map<String, Map<String, Double>> clusterCenterToFlexFreq = mpars.combineParadigms5(printStarRain);
//		
//		System.out.println("size before combine: " + sizeBeforeCombine);
////		System.out.println("NEW suffixes: " + suffToFlex.toString());
//
//		return clusterCenterToFlexFreq;
//	}
//	
//	
//	//save idea
//	public static boolean checkFlexIfNotCompoundWord(String flex, LetterTokModel ltmodel, String lang, WordSequences model) {
//		if(flex.length() < 4) 
//			return false;
//		List<MyPair> slices = MorphAnalyzer.getWordCondFreqAnalysisSubstrings(lang, ltmodel, flex.toString(), "pref");
//		for(MyPair pair: slices) {
//			if(pair.freq == 1.0) return true;
//		}
//		slices = MorphAnalyzer.getWordCondFreqAnalysisSubstrings(lang, ltmodel, flex.toString(), "suf");
//		for(MyPair pair: slices) {
//			if(pair.freq == 1.0) return true;
//		}
//		String flexUpperCase = flex.substring(0, 1).toUpperCase() + flex.substring(1);
//		String flexWithoutInfix = flex.substring(1);
//		String flexWithoutInfixUppercase = flexWithoutInfix.substring(0, 1).toUpperCase() + flexWithoutInfix.substring(1);
//		if(model.idx().words.containsKey(flex) || model.idx().words.containsKey(flexUpperCase) || model.idx().words.containsKey(flexWithoutInfix) || model.idx().words.containsKey(flexWithoutInfixUppercase))
//			return true;
//		return false;
//	}
//	
//	
//	public static void main(String[] args) throws IOException {
////		mainDeCluster();
//		mainUkrCluster();
//	}
//	
//	private static void mainDeCluster() throws IOException {
//		outClusters.write("Clusters for GERMAN\n");
//
//		String lang = "de";
//		WordSequences wsmodel = BuildWordSequenceModelDE.getWSModel("de", new String[] {"news", "wiki"}, 300000, false,0);
//		String filePath = CorpusUtils.getLeipzigWordsPath(lang, "news");
//		LetterTokModel ltmod = MorphAnalyzer.trainLetterTokModel(lang, filePath, 30, 3); 
//		trainCLusters(wsmodel, ltmod); //3000
//		BuildWordSequenceModel.getInput(wsmodel, ltmod, null, null, null);
//
//	}
//
//
//	private static void mainUkrCluster() throws IOException {
//		
//		outClusters.write("Clusters for UKR\n");
//		
//		String lang = "ukr";
//		String corpus = "wiki";
////		corpus = "news";
//		WordSequences wsmodel = BuildWordSequenceModelUKR.getWSModel(lang, new String[] {corpus}, 400000, false,0);
////		WordSequences wsmodel = BuildWordSequenceModelUKR.getWSModel(lang, new String[] {"news"}, 800000, false);
//
//		String filePath = CorpusUtils.getLeipzigWordsPath(lang, corpus);
//		int freqIndex = 3;
//		if(corpus.equals("news")) freqIndex = 2;
//		LetterTokModel ltmod = MorphAnalyzer.trainLetterTokModel(lang, filePath, 30, freqIndex); 
//		trainCLusters(wsmodel, ltmod); //5000
//		BuildWordSequenceModel.getInput(wsmodel, ltmod, null, null, null);
//
//	}
//}
