package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;

import modelparts.Flexion;
import modelparts.MorphParadigm;
import modelparts.Root;
import modelparts.Word;
import modeltrain.MorphAnalyzer;
import modelutils.Cluster;
import util.MapsOps;
import util.MyPair;
import util.MyPairWord;
import util.MyUtils;
import util.SetOps;

public class MorphVectorAnalyzerOld {
	
	public static final int CONTEXT_COUNT = 20;
	public static final double HARD_THH_FOR_WAITING_FLEXES = 5;
	public static Map<String, List<String>> mparContextsMapLeft = new HashMap<String, List<String>>();
	public static Map<String, List<String>> mparContextsMapRight = new HashMap<String, List<String>>();

	public static void getRootFlex(WordSequences wsmodel, LetterTokModel ltmodel, int wordFreqTHHforModelTrain,  boolean print) {
		computeRootFlexInitial(wsmodel, ltmodel, wordFreqTHHforModelTrain, print);		
	}
	
	public static void computeRootFlexInitial(WordSequences model, LetterTokModel ltmodel, int freqTHH, boolean print) {
		for(Word inputword: model.idx().getSortedWords()) {
			if(inputword.isParadigmWord() || inputword.toString().length() < 4 || inputword.isSplitterLeftRight(model.getFreqOfAnd()))
				continue;
			if(inputword.freq() < freqTHH) break;
			computeRootFlexInitialOneWOrd(inputword, model, ltmodel, print);
		}
	}
	
	private static void computeRootFlexInitialOneWOrd(Word inputword, WordSequences model, LetterTokModel ltmodel, boolean print) {
		List<MyPair> slicesPref = MorphAnalyzer.getWordCondFreqAnalysisSubstrings(model.getLang(), ltmodel,
				inputword.toString(), "pref");
		List<MyPair> slicesSuf = MorphAnalyzer.getWordCondFreqAnalysisSubstrings(model.getLang(), ltmodel,
				inputword.toString(), "suf");
		
		MyPair rootFlexPref = getRootFlexFromPREFIXSlice(inputword.toString(), slicesPref);
		MyPair rootFlexSuf = getRootFlexFromSUFFIXSlice(inputword.toString(), slicesSuf);
		
		String suffFlex = rootFlexSuf.second.replaceAll("_", "");
		String prefFlex = rootFlexPref.second.replaceAll("_", "");
		String prefRoot = rootFlexPref.first.replaceAll("_", "");
		
		if (prefFlex.equals(""))
			prefFlex = "_";
		if (suffFlex.equals(""))
			suffFlex = "_";
		
		if (prefFlex.equals(suffFlex)) {
			inputword.setFlexRootMPar(prefFlex,null, model, false);
			MapsOps.addFreq(prefFlex, model.idx().seenFlexes);
			if(print)
				System.out.println("SAME INIT flex\t" + prefRoot+ "\t" + prefFlex );
		}
	}
	
	private static MyPair getRootFlexFromPREFIXSlice(String inputword, List<MyPair> slices) {
		MyPair prev = null;
		List<MyPair> cuts = new ArrayList<>();
		boolean isGoingUp = false;
		for(MyPair mp: slices) {
			if(prev == null) {
				prev = mp;
				continue;
			}
			if(prev.freq > 0.1 && mp.freq < (prev.freq) && isGoingUp ) { //benchmark cuts 4, actual, maybe the best
				cuts.add(prev);
			}
			//end check
			
			if(prev.freq <= mp.freq) isGoingUp = true;
			else isGoingUp = false;
			prev = mp;
		}
		if(cuts.size() == 0) cuts.add(new MyPair(inputword, "_"));
		return cuts.get(cuts.size()-1);
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
			if(prev.freq > mp.freq ) { 
				return prev; // add only 1 first cut from the back of the word
			}
			prev = mp;
		}
		return new MyPair(inputword, "_");
	}

	public static void addInitialMPasCateory(WordSequences model, int thh, boolean print) {
		if(print) {
			//print most frequent flexes
			for(String f: model.idx().seenFlexes.keySet())
				if(model.idx().seenFlexes.get(f) > thh) 
					System.out.println(f + "\t" + model.idx().seenFlexes.get(f));
		}
		double flexThh = getFlexFreqThh(model.idx().seenFlexes, 10.0, 100);
		Set<String> flexToTag = MapsOps.getFirstEntriesObject(model.idx().seenFlexes, 100, flexThh).keySet();
		MapsOps.printSortedMap(model.idx().seenFlexes, null, 100, (int)flexThh, true, "\n");
		if(flexToTag.size() > 20) thh = 15;
		for(Word w: model.idx().getSortedWords()) {
			if(w.freq() < thh) break;
			if(!checkMorph(w, model)) continue;
			if(w.getFlex() == null) continue;

			String flex = w.getFlex();
			String root = Words.computeRootFromFlex(w, flex); 
			w.setRoot(root);
			Root r = model.idx().getRoot(root);
			if(flexToTag.contains(flex)) {
				MorphParadigm mp = model.idx().getNewMorphParadigm(SetOps.of(model.idx().getFlex(flex)), null);
				w.changeMorphParadigm(mp);
				if(mp == null) continue;
				String flexCatName = MorphModel.getFlexLabel(flex, mp.getLabel(), MorphModel.FPREF);
				model.addCategory(flexCatName, w);
				model.addCategory(mp.getLabel(), w);
			}
		}
		model.idx().setStopCreatingParadigms();
	}
	
	
	private static double getFlexFreqThh(Map<String, Double> seenFlexes, double percent, double def) {
		MyPair mostFrequentFlex = MapsOps.getFirst(seenFlexes);
		if(mostFrequentFlex == null) return def;
		double freq = mostFrequentFlex.freq;
		return freq/percent;
	}

	public static void findWaitingFlexesViaRoots(WordSequences model, String paradigmFilterREgex ,boolean print) {
//		model.idx().fillBuckets(2);
//		model.idx().saveMorphParsFreqs();
//		clenWaitingFlexes(model);
		// get cats vectors
		collectMParVectorsFlexion(model, paradigmFilterREgex, print);

		for(String mpl: model.idx().getMPlabels()) {
			MorphParadigm mp = model.idx().getMorphParadigm(mpl);
			for(Word w: mp.getWords()) {
				Root r = model.idx().getRoot(w.getRoot());
				for(Flexion f: r.getFlexes()) {
					if(mp.containsFlex(f)) continue;
					mp.addWaitingFlex(f.toString(), 1.0);
					mp.addWaitingFlexWord(f.toString(), model.getWord(r.toString()+f.toRealString()));
				}
			}
		}
	}
	

	public static void tagWordsAndFindWaitingFlexes(WordSequences model, int thh, String paradigmFilterREgex, boolean print) {
		model.idx().fillBuckets(2);
		model.idx().saveMorphParsFreqs();
		clenWaitingFlexes(model);
		// get cats vectors
		collectMParVectorsFlexion(model, paradigmFilterREgex, print);
		for(Word w: model.idx().getSortedWords()) {
			if(w.freq() < thh) break;
			if(!checkMorph(w,model)) continue;
			//get word vectors, get score with cat vectors, get best cat
			List<MyPair> parscores = getMParFromFlexVector(model, w, false, paradigmFilterREgex, print);
			if(parscores == null||parscores.isEmpty()) continue;
			MyPair bestpar1 = getBestParFromList(parscores,w,model); //parscores.get(parscores.size()-1);
			//check flex			//check root			//add waiting flex
			MyPair bestpar = checkConflicts(bestpar1, w, model,paradigmFilterREgex);
			if(print && bestpar != null) System.out.println("BESTPAR\t"+w+"\t" + bestpar);
			
			if(bestpar == null) {
//				if(w.getMorphParadigm() != null) System.out.println("TAGGED NULL: " + w.toString() + " which had: " + w.getMorphParadigm().getLabel()
//						+ " " + w.getMorphParadigm().getFlexes().toString());
//				w.changeMorphParadigm(null);
//				if(bestpar1 != null && w.getMorphParadigm() != null) {
//					System.out.println("WORD: " + w + "\tWAS: " +w.getMorphParadigm().getLabel()+" "+w.getMorphParadigm().getFlexes()
//							+"\tFOUND: "+ bestpar1.toString());
//					System.out.println(w.getBestContextsComputeNewAsList(true, CONTEXT_COUNT, Words.SYNSEM_FILTER, model, false));
//					System.out.println(w.getBestContextsComputeNewAsList(false, CONTEXT_COUNT, Words.SYNSEM_FILTER, model, false));
//				}
				continue;
			}
//			if(bestpar != null) {
			String flex = MorphModel.getFlexFromFlexPar(bestpar.first, MorphModel.FPREF);
			MorphParadigm newMP = model.idx().getMorphParadigm(MorphModel.getMPlabelFromFlexPar(bestpar.first));
			w.setFlexRootMPar(flex, newMP, model,false);
//			}
			//write bestPar into word
			
		}
		
	}

	private static MyPair getBestParFromList(List<MyPair> parscores, Word w, WordSequences model) {
		if(parscores == null||parscores.isEmpty()) 
			return null;
		MyPair best = parscores.get(parscores.size()-1);
		if(!hasFlexionConflict(best.first, w, model)) return best;
		if(parscores.size()< 2) return best;
		MyPair secondBest = parscores.get(parscores.size()-2);
		//if second best ends with the same flex as this word and score is not so far, take it
		double scoreDiff = secondBest.freq - best.freq;
		if(scoreDiff < 1.0 && !hasFlexionConflict(secondBest.first, w, model))
			return secondBest;
		return best;
	}

	private static void clenWaitingFlexes(WordSequences model) {
		for(String l: model.idx().getMPlabels()) {
			MorphParadigm mp = model.idx().getMorphParadigm(l);
			mp.waitingFlexFreqMap.clear();
			mp.waitingFlexWordMap.clear();
		}
	}

	private static void collectMParVectorsFlexion(WordSequences model, String regexForParadimFilter, boolean print) {
		mparContextsMapLeft.clear();
		mparContextsMapRight.clear();
		//collect all tagged flexes
		Set<String> seenflexes = new HashSet<>();;
		Set<String> mparlabels = model.idx().getMPlabels();
		for(String label: mparlabels) {
			if(model.idx().getMorphParadigm(label) == null) continue;
			MorphParadigm mp = model.idx().getMorphParadigm(label);
			if(mp == null) continue;
			for(Flexion f: mp.getFlexes())
				seenflexes.add(MorphModel.getFlexLabel(f.toString(), mp.getLabel(), MorphModel.FPREF));
		}
		//collect best contexts for left and right for each mpar flex
		for (String flex : seenflexes) {
			Word flexword = model.getWord(flex);
			List<String> bestContextsLeft = new ArrayList<>();
			for (MyPairWord key : flexword.getBestContextsComputeNew(true, CONTEXT_COUNT, regexForParadimFilter, model,	false)) {
				bestContextsLeft.add(key.left.toString());
			}
			mparContextsMapLeft.put(flex, bestContextsLeft);
			List<String> bestContextsRight = new ArrayList<>();
			for (MyPairWord key : flexword.getBestContextsComputeNew(false, CONTEXT_COUNT, regexForParadimFilter, model, false)) {
				bestContextsRight.add(key.left.toString());
			}
			mparContextsMapRight.put(flex, bestContextsRight);
		}
		if(print) {
			MapsOps.printMapObject(mparContextsMapLeft, null);
			MapsOps.printMapObject(mparContextsMapRight, null);
		}
	}
	
	
	private static List<MyPair> getMParFromFlexVector(WordSequences model, Word w, boolean allwoFreqOne, String paradigmFilterREgex, boolean print) {
		List<String> bestContextsLeft = w.getBestContextsComputeNewAsList(true, CONTEXT_COUNT, paradigmFilterREgex, model, allwoFreqOne, 20);
		List<String> bestContextsRight = w.getBestContextsComputeNewAsList(false, CONTEXT_COUNT, paradigmFilterREgex, model, allwoFreqOne, 20);
	
		double bestScore = Double.MAX_VALUE;
		String bestFlexMP = null;
		List<MyPair> tmpListForPrint = new ArrayList<>();
		for(String fl: mparContextsMapRight.keySet()) { //left or right, both have same flexes
			double parScore = getParadigmScores( bestContextsLeft, 	bestContextsRight, fl);
			 tmpListForPrint.add(new MyPair(fl, "", parScore)); 
			 if(parScore < bestScore ) {// && checkFlexionOfFlexPar(fl, w)) { //&& checkFlexion(mp, w, model)) {
				 bestScore = parScore;
				 bestFlexMP = fl;
			 }
			 else if(parScore == bestScore) {
				 String bestFlex = MorphModel.getFlexFromFlexPar(bestFlexMP, MorphModel.FPREF);
				 if(!w.toString().endsWith(bestFlex)) {
					 String flex = MorphModel.getFlexFromFlexPar(fl, MorphModel.FPREF);
					 if(flex.equals("_")  ) flex = "";
					 if(w.toString().endsWith(flex)) {
						 bestScore = parScore;
						 bestFlexMP = fl;
					 }
				 }
			 }
		}
		Collections.sort(tmpListForPrint);
		if(print) {
//			for(MyPair p: tmpListForPrint)
//				System.out.println(p.freq + "\t" + p.first);
//			for (int i = tmpListForPrint.size()-4; i < tmpListForPrint.size() ; i++) {
//				if(i < 0) break;
//				System.out.println(tmpListForPrint.get(i).freq + "\t" + tmpListForPrint.get(i).first);
//			}
//			System.out.println(w.toString());
		}
		
		if(bestScore > 9.9) return null;
		if(bestFlexMP == null) return null;
		return tmpListForPrint; //new Pair<String, Double>(bestFlexMP, bestScore);
	}

	private static double getParadigmScores( List<String> bestContextsLeft,	List<String> bestContextsRight, String fl) {
		double scoreL = Cluster.getScaledSimMeasure(bestContextsLeft, mparContextsMapLeft.get(fl)) ;
		double scoreR = Cluster.getScaledSimMeasure(bestContextsRight, mparContextsMapRight.get(fl)  );
		double parScore = (scoreL + scoreR) /2;
		 scoreL = Cluster.getScaledSimMeasure(mparContextsMapLeft.get(fl), bestContextsLeft) ;
		 scoreR = Cluster.getScaledSimMeasure(mparContextsMapRight.get(fl), bestContextsRight) ;
		 parScore = (parScore + ((scoreL + scoreR) /2)) / 2;
		return parScore;
	}

	private static MyPair checkConflicts(MyPair bestFlexMP, Word w,  WordSequences model, String categoryFilterRegex) {
		//check null-Flexion OR a new (waiting) flexion again
		if (bestFlexMP == null) return null;
		MyPair tmpBestFlexMP = checkRootFlexConflict(bestFlexMP.first, w, model, categoryFilterRegex);

		if(tmpBestFlexMP != null) 
			return tmpBestFlexMP;
		
		bestFlexMP = checkNewRoot(bestFlexMP.first, w, model,categoryFilterRegex);
		
		return bestFlexMP;
	}

	private static MyPair checkNewRoot(String bestFlexMP, Word w, WordSequences model, String categoryFilterRegex) {
		//check new root and add waiting flex
		MorphParadigm mpComputed = model.idx().getMorphParadigm(MorphModel.getMPlabelFromFlexPar(bestFlexMP));
		if(mpComputed == null) return null;
			Set<Root> bucketRoots = model.idx().getPrefixBucketRoot(w);
			List<Root> rSorted = bucketRoots.stream().sorted((a,b) -> (-1)*(a.toString().length()-b.toString().length())).collect(Collectors.toList());
			int countTriedRoots = 0;
//			boolean writeinfo = false;
//			if(w.getFlex()==null) {
//				writeinfo = true;
//			}
			boolean rootFormTAGflexUsed = false;
			for(Root newRoot: rSorted) {
				if(countTriedRoots > 2) {
					String flexFromFoundParadigm = MorphModel.getRealFlexFromFlexPar(bestFlexMP, MorphModel.FPREF);
					if( w.toString().endsWith(flexFromFoundParadigm)) {
						String rstring = w.toString().substring(0,w.toString().length()-flexFromFoundParadigm.length());
						newRoot = model.idx().getRoot(rstring);
						rootFormTAGflexUsed = true;
					}
				}

				if(countTriedRoots > 3) break;
				if(newRoot.toString().length()<2) break;
				if(!w.toString().startsWith(newRoot.toString())) continue;
				countTriedRoots++;
//				if(w.getFlex()==null) {
//					System.out.println("WORD OHNE ROOT GOT root: "+w+" " + newRoot + " paradigm: " +bestFlexMP+"\t"+ mpComputed.getSortedFlex() );
//				}
				
				
				String newFlex = w.toString().substring(newRoot.toString().length());
				if(newFlex.length()==0) newFlex = "_";
				
				if( confirmRootFlexInParadigm(mpComputed, newRoot, model.idx().getFlex(newFlex), model,categoryFilterRegex)) {
					//seiner--> roots seine sein, should check also sein!!!!
					if(newRoot.toString().length() > 3 && !mpComputed.containsFlex(model.idx().getFlex(newFlex))) {
						mpComputed.addWaitingFlex(newFlex, 1.0); //w.freq()
						mpComputed.addWaitingFlexWord(newFlex, w);
						if(rootFormTAGflexUsed) System.out.println("NEWROOT waiting: " + newFlex + " for " + mpComputed.getLabel() + " from " + w.toString());
					}else {
//						if(writeinfo) System.out.println("new par: " +w+" "+mpComputed.getLabel());
						if(rootFormTAGflexUsed) System.out.println("NEWROOT confirmed: " + newFlex + " for " + mpComputed.getLabel() + " from " + w.toString());

						return new MyPair(MorphModel.getFlexLabel(newFlex, mpComputed.getLabel(), MorphModel.FPREF), "", 1.0);
					}
					break;
				}
			}
//			if(writeinfo) System.out.println("new par: "+w+" " +"null, was:" + bestFlexMP +" "+mpComputed.getSortedFlex());

//			if(rootFormTAGflexUsed) System.out.println("NEWROOT was used but NULL: " +  w.toString() + " "  + bestFlexMP);
		return null;
	}

	private static MyPair checkRootFlexConflict(String bestFlexMP, Word w, WordSequences model, String categoryFilterRegex) {
		String computedFlex = MorphModel.getFlexFromFlexPar(bestFlexMP, MorphModel.FPREF);
		MorphParadigm mpComputed = model.idx().getMorphParadigm(MorphModel.getMPlabelFromFlexPar(bestFlexMP));
		if(mpComputed == null) return null;
		//to avoid suffixes as waiting flexes - try first the whole word as a root: Flugzeugbauer, Unternehmen
		Root wordroot =  model.idx().getRoot(w.toString());
		String zeroFlex= "_";
		boolean hasFlexConflict = hasFlexionConflict(bestFlexMP, w, model);
		if(!hasFlexConflict && mpComputed.getFlexes().size() > 1 && mpComputed.containsFlex(model.idx().getFlex(zeroFlex)) && 
				confirmRootFlexInParadigm(mpComputed, wordroot, model.idx().getFlex(zeroFlex), model,categoryFilterRegex)) {
			return new MyPair(MorphModel.getFlexLabel(zeroFlex, mpComputed.getLabel(), MorphModel.FPREF), "", 1.0);
//			return false;
		}
		if(!hasFlexConflict) {//means word ends with flex
			int flexLength = computedFlex.equals("_") ? 0 : computedFlex.length();
			Root r = model.idx().getRoot(w.toString().substring(0, w.toString().length()-flexLength));
			String newFlex = w.toString().substring(r.toString().length());
			if(confirmRootFlexInParadigm(mpComputed, r, model.idx().getFlex(newFlex), model,categoryFilterRegex))
				return new MyPair(MorphModel.getFlexLabel(newFlex, mpComputed.getLabel(), MorphModel.FPREF), "", 1.0);
//				return false;
		}

		return null;
	}

//	private static MorphParadigm confirmFlexAndGetParadigm(Root newRoot, String newFlex, WordSequences model, Word w) {
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
//			w.setFlexRoot(newFlex,model);
//			w.setMorphParadigm(mp);
//			return mp;
//		}
//		else return null;
//		
//	}

	private static boolean confirmRootFlexInParadigm( MorphParadigm mpComputed, Root r, Flexion newFlex, WordSequences model, String categoryFilterRegex) {
		if(mpComputed == null) return false;
			
		int countFlexes = 0;
//		int paradigmFlexCountThh = mpComputed.getFlexes().size() > 1 ? 1 : 0; //if this root has 2 flexes of this par it is confirmed
		int paradigmFlexCountThh = mpComputed.getFlexes().size() > 2 ? 2 : mpComputed.getFlexes().size()-1; //if this root has 2 flexes of this par it is confirmed
		for(Flexion paradigmFlex: mpComputed.getFlexes()) {
//			if(paradigmFlex.toString().equals(newFlex.toString())) {
//				countFlexes++;
//				if(countFlexes > paradigmFlexCountThh)	return true;
//				continue;
//			}
			//angenommen computed paradigm; f_e_m_2 fpr word Beteiligungs, root: Beteiligung, flex s, check m_ flexes with Beteiligung,
			//if there are such words in the model and if they could get into the same paradigm. if so, tag all of them with paradigm
			String flexstring = paradigmFlex.toRealString();
			Word newParadigmWord = model.getWord(r.toString()+flexstring);
			if(checkWordExistsAndSameParadigm(newParadigmWord, model, mpComputed.getLabel(),categoryFilterRegex)) {
				countFlexes++;
				if(countFlexes > paradigmFlexCountThh)	return true;
			}
		}
		return false;
	}

	private static boolean checkWordExistsAndSameParadigm(Word newParadigmWord, WordSequences model, String computedLabel, String categoryFilterRegex) {
		if(newParadigmWord.freq() < 2) return false;
//		if(newParadigmWord.getMorphParadigm() != null && newParadigmWord.getMorphParadigm().getLabel().equals(computedLabel))
//			return true;
//		MyPair 	bestPar = model.idx().flexPar777.get(newParadigmWord);
//		if(bestPar == null) {
//			List<MyPair> bestParList = getMParFromFlexVector(model, newParadigmWord, true, categoryFilterRegex,false);
//			if(bestParList == null || bestParList.isEmpty()) return false;
//			bestPar = bestParList.get(bestParList.size()-1);
//		}
//		if(bestPar != null && bestPar.first.endsWith(computedLabel)) //
			return true;
//		return false;
	}

	/**
	 * Checks if word ends with paradigm flexion. if flex == Zero (_) - makes additional check 
	 * @param bestFlexMP
	 * @param w
	 * @param mparContextsMapLeft 
	 * @param mparContextsMapRight 
	 * @return true = has conflict, false = no conflict
	 */
	private static boolean hasFlexionConflict(String bestFlexMP, Word w, WordSequences model) {
		String flexFromFoundParadigm = MorphModel.getFlexFromFlexPar(bestFlexMP, MorphModel.FPREF);
		if(flexFromFoundParadigm.equals("_")) return false;
		if(w.toString().endsWith(flexFromFoundParadigm)) {
			if(w.getFlex() == null) {
				w.setFlexRootMPar(flexFromFoundParadigm, null, model, false);
			}
			return false;
		}
		//we ignore zero flexes here and treat them as conflicts -- NOT true
		return true;
	}

	public static boolean checkMorph(Word w, WordSequences model) {
		if(w.isCat()) return false;
		if(w.isSplitterLeftRight(model.getFreqOfAnd())) return false;
		if(w.toString().length() < 3) return false;
		return true;
	}
	
	public static void writeTMPWaitingFlexCats(WordSequences model, double thh, boolean print) {
		Set<String> mparlabels = model.idx().getMPlabels();
		for(String l: mparlabels) {
			MorphParadigm mp = model.idx().getMorphParadigm(l);
			if(mp == null) continue;
			SortedMap<String, Double> sorted = MapsOps.getSortedMapString(mp.waitingFlexFreqMap);
			boolean thhSet = false;
			for (Iterator iter = sorted.keySet().iterator(); iter.hasNext();) {
				String f = (String) iter.next();
				double freq = (Double) sorted.get(f) ;
				if(!thhSet) {
					thh = getFlexibleTHH(freq);
					thhSet = true;
				}
//				if(freq < HARD_THH_FOR_WAITING_FLEXES) break;
				if(freq < thh) break;
				String catLabel = getTMPLabelForWaitingCat(mp.getLabel(), f);
				for(Word w: mp.waitingFlexWordMap.get(f))
					model.addCategory(catLabel, w);
				//check suffixes!!!!!!!!!

			}
		}
	}
	
	private static String getTMPLabelForWaitingCat(String mplabel, String f) {
		return "t_"+f+"_"+mplabel;
	}

	public static int addWaitingFlexes(WordSequences model, double thh, boolean checkAllParadigmFlexes, String categoryFilterRegex, 
			boolean initial, boolean print) {
		int waitingFlexAdded = 0;
		Collection<MorphParadigm> mpars = model.idx().getMorphParadigms();
		List<MorphParadigm> mparListSortedOnFlexCount = mpars.stream().sorted((a,b) -> a.getFlexes().size()-b.getFlexes().size()).collect(Collectors.toList());
		for(MorphParadigm mp: mparListSortedOnFlexCount) {
			if(mp == null) continue;
			SortedMap<String, Double> sorted = MapsOps.getSortedMapString(mp.waitingFlexFreqMap);
			boolean thhSet = false;
			for (Iterator iter = sorted.keySet().iterator(); iter.hasNext();) {
				String waitingFlex = (String) iter.next();
				double freq = (Double) sorted.get(waitingFlex) ;
				if(!thhSet) {
					thh = getFlexibleTHH(freq);
					thhSet = true;
				}
				if(freq < thh) break;
				if(checkAllParadigmFlexes) {
					Set<String> countRootsWithAllFlexes = new HashSet<>();
					for(Word waitingWord: mp.waitingFlexWordMap.get(waitingFlex)) {
						String root = Words.computeRootFromFlex(waitingWord, waitingFlex);
						if(rootMatchesAllParadigmFlexes(root, waitingFlex, mp, model))
							countRootsWithAllFlexes.add(root);
					}
					Set<String> seenroots = new HashSet<>();
					for(Word paradigmWord: mp.getWords()) {
						if(paradigmWord.getRoot() == null) continue;
						String root = paradigmWord.getRoot();
						if(seenroots.contains(root)) continue;
						seenroots.add(root);
						if(rootMatchesAllParadigmFlexes(root, waitingFlex, mp, model))
							countRootsWithAllFlexes.add(root);
					}
					if(countRootsWithAllFlexes.size() < 10) {
						System.out.println("waitingFlex " + waitingFlex + " does not match all flexes from " + mp.getLabel() + " " +mp.getSortedFlex()
						+" found words with all flexes"+ countRootsWithAllFlexes.size());
						continue;
					}
//					else {
//						System.out.println("MATCHING ROOTS: " + " for flex: " +waitingFlex + " for MO: " + mp.getLabel() + " " + mp.getSortedFlex()
//					+ countRootsWithAllFlexes.toString());
//					}
				}
				Word waitingFlexWord = model.getWord(getTMPLabelForWaitingCat(mp.getLabel(), waitingFlex));
				List<MyPair> bestParList = getMParFromFlexVector(model, waitingFlexWord, false, categoryFilterRegex,print);
				if(bestParList == null || bestParList.isEmpty()) continue;
//				MyPair bestPar = bestParList.get(bestParList.size()-1);
				MyPair bestPar = getBestParFromList(bestParList,waitingFlexWord,model); //parscores.get(parscores.size()-1);

				if(initial || checkAllParadigmFlexes || (bestPar != null && bestPar.first.endsWith(mp.getLabel()) )) {
					waitingFlexAdded ++;
					System.out.println("WAITING FLEX confirmed: " + waitingFlex +" " + bestPar+ " " + mp.getLabel() + " " + mp.getFlexes());
					mp.addWaitingFlex(model.idx().getFlex(waitingFlex),model);
				}
				else if(bestPar == null) {
					System.out.println("here");
					bestParList = getMParFromFlexVector(model, waitingFlexWord, false, categoryFilterRegex,print);
					if(bestParList == null || bestParList.isEmpty()) continue;
					bestPar = bestParList.get(bestParList.size()-1);
					bestPar = getBestParFromList(bestParList,waitingFlexWord,model); //parscores.get(parscores.size()-1);
				}
				else
					System.out.println("WF declined: " + waitingFlex +" " + bestPar+ " " + mp.getLabel() + " " + mp.getFlexes());
//				if(initial) break; // add only 1 flex initially
				//check suffixes!!!!!!!!!
			}
		}
		return waitingFlexAdded;
	}
	
	private static double getFlexibleTHH(double freq) {
		return Math.max(10.0, freq/2);
	}

	public static void tagWaitingFlexes(WordSequences model, double thh, boolean checkAllParadigmFlexes, String categoryFilterRegex, 
			boolean initial, boolean print) {
		Collection<MorphParadigm> mpars = model.idx().getMorphParadigms();
		List<MorphParadigm> mparListSortedOnFlexCount = mpars.stream().sorted((a,b) -> a.getFlexes().size()-b.getFlexes().size()).collect(Collectors.toList());
		for(MorphParadigm mp: mparListSortedOnFlexCount) {
			//tag all roots from this paradigm with the corresponding waitingFlex, note: some paradigms with ambig flexes will re-tag several word
//					String waitingFlexRealString = waitingFlex.equals("_") ? "" : waitingFlex;
					Set<Word> wordsToCheck = new HashSet<>();
					for(Word wordInMPar: mp.getWords()) {
						wordsToCheck.add(wordInMPar);
					}
					for(Word wordInMPar: wordsToCheck) {
						if(initial && !rootMatchesAllFlexes(wordInMPar, null, mp, model))
							continue;
						wordInMPar.setFlexRootMPar(null, mp, model, false);
					}
					for(Flexion f: mp.getFlexes()) {
						Set<Word> waitingWords = mp.waitingFlexWordMap.get(f.toString());
						if(waitingWords == null) continue;
						for(Word w: waitingWords) {
							if(initial && !rootMatchesAllFlexes(w, null, mp, model))
								continue;
							w.setFlexRootMPar(null,mp,model,false);
						}
					}
				}
	}

	private static boolean rootMatchesAllFlexes(Word word, String waitingFlex, MorphParadigm mp, WordSequences model) {
		if(word.getRoot() == null) return false;
		return rootMatchesAllParadigmFlexes(word.getRoot(), waitingFlex, mp, model);
	}

	private static boolean rootMatchesAllParadigmFlexes(String root, String waitingFlex, MorphParadigm mp, WordSequences model) {
		if(waitingFlex != null && waitingFlex.equals("_")) waitingFlex = "";
		if(waitingFlex != null && model.getWord(root+waitingFlex).freq() < 2) return false;
		for(Flexion f: mp.getFlexes()) {
			if(model.getWord(root+f.toRealString()).freq() < 2) return false;
		}
		return true;
	}

	public static void tagWaitingFlexesOld(WordSequences model, double thh, boolean print) {
		Set<String> mparlabels = model.idx().getMPlabels();
		for(String l: mparlabels) {
			MorphParadigm mp =  model.idx().getMorphParadigm(l);
			if(mp == null) continue;
			double firstWaitingFreq = MapsOps.getFirst(mp.waitingFlexFreqMap).freq;
			if(firstWaitingFreq < 10) continue;
			SortedMap<String, Double> sorted = MapsOps.getSortedMapString(mp.waitingFlexFreqMap);
			for (Iterator iter = sorted.keySet().iterator(); iter.hasNext();) {
				String f = (String) iter.next();
				double freq = (Double) sorted.get(f) ;
				if(freq < 10) break;
				if((freq * 2)<firstWaitingFreq) break;
//				firstWaitingFreq = freq; // too much noise
				
				//check suffixes!!!!!!!!!
				mp.addFlex(model.idx().getFlex(f),model);
				for(Word w: mp.waitingFlexWordMap.get(f)) {
					w.setFlexRootMPar(f,mp,model,false);
				}
			}
		}
		
	}

	public static void cleanMorphTags(WordSequences model, int thh) {
		Set<String> mparlabels = model.idx().getMPlabels();
		for(String mplabel: mparlabels) {
			MorphParadigm mp= model.idx().getMorphParadigm(mplabel);
			if(mp == null) continue;			
			Word mpword = model.getWord(mplabel);
			deleteCategory(mpword, model);
			for(Flexion f: mp.getFlexes()) {
				Word fword = model.getWord(MorphModel.getFlexLabel(f.toString(), mplabel, MorphModel.FPREF));
				deleteCategory(fword, model);
			}
		}
		
//		printMorphParStats(model,thh, "after CLEAN");
//		model.idx().resetMorphPars();
		model.idx().flexPar777.clear();
	}

	private static void deleteCategory(Word mpword, WordSequences model) {
		Set<Word> wset = new HashSet<>();
		for(Word w: mpword.left_of.keySet()) wset.add(w);
		for(Word w: mpword.right_of.keySet()) wset.add(w);
		for(Word w: wset) model.deleteCategory(mpword);
	}

	public static void addMPasCategory(WordSequences model, int thh, boolean initial, boolean print) {
		for(Word w: model.idx().getSortedWords()) {
			if(w.freq() < 2) break;
			if(!checkMorph(w, model)) continue;
			if(w.getFlex() == null || w.getRoot() == null) continue;
			if(w.getMorphParadigm() == null) continue;
			String flex = w.getFlex();
			MorphParadigm mp = w.getMorphParadigm();
			if(initial && !rootMatchesAllParadigmFlexes(w.getRoot(), flex, mp, model)) continue;
			String flexCatName = MorphModel.getFlexLabel(flex, mp.getLabel(), MorphModel.FPREF);
			model.addCategory(flexCatName, w);
			model.addCategory(mp.getLabel(), w);
		}
//		printMorphParStats(model,thh, "after TAG");
	}

	public static void printMorphParStats(WordSequences model, int thh, String info) {
		System.out.println(info);
		double wcount = 0; 
		double wHasMP = 0;
		for(String mpstring: model.idx().getMPlabels()) {
			System.out.println(model.getWord(mpstring) + "\t" +model.getWord(mpstring).freq() + "\tfreq of mpar\t" 
					+ model.idx().getMorphParadigm(mpstring).getFreq());
//			for(Flexion f: model.idx().morphPars().get(mpstring).getFlexes()) {
//				Word cword = model.getWord(MorphModel.getFlexLabel(f.toString(), mpstring, MorphModel.FPREF));
//				System.out.println(cword+"\t" + cword.freq());
//			}
		}
//		for(String mpstring: model.idx().morphPars().keySet()) {
//			System.out.println(model.getWord(mpstring) + "\t" +model.getWord(mpstring).paradigmWords);
//			for(Flexion f: model.idx().morphPars().get(mpstring).getFlexes()) {
//				Word cword = model.getWord(MorphModel.getFlexLabel(f.toString(), mpstring, MorphModel.FPREF));
//				System.out.println(cword+"\t" + cword.paradigmWords);
//			}
//		}
		System.out.println();
		Map<String,Double> mpStats = new HashMap<>();
		for(Word w: model.idx().getSortedWords()) {
			if(w.freq() < thh) break;
			if(!checkMorph(w, model)) continue;
			wcount++;
			if(w.getMorphParadigm() != null) wHasMP++;
			else continue;
			MorphParadigm mp = w.getMorphParadigm();
			if(mp == null) continue;
			MapsOps.addFreq(mp.getLabel(), mpStats);
		}
		MapsOps.printSortedMap(mpStats, null);
		System.out.println("from " + wcount + " words " + wHasMP + " tagged: " + MyUtils.rdouble((wHasMP/wcount)) ) ;
	}

	public static Set<MorphParadigm> deleteMparsWithONEflexFromWords(WordSequences model) {
		Set<MorphParadigm> toDelete = new HashSet<>();

		for(Word w: model.idx().getSortedWords()) {
			if(w.freq() < 2) break;
			if(w.getMorphParadigm() == null) continue;
			MorphParadigm mp = w.getMorphParadigm();
			if(mp.getFlexes().size() < 2) {
				w.changeMorphParadigm(null);
				toDelete.add(mp);
			}
		}
		return toDelete;
	}
	
	public static Set<MorphParadigm> collectVanishingMPars(WordSequences model) {
		Map<String,Double> mpStats = model.idx().getSavedMorphParsFreqs();
		//thh = biggest freq of mo / 10;
		double thh = getVanishingThh(model);
//		Map<Flexion,Double> mpFlexStats = collectFlexStats(model);
		//collect new stats
//		for(Word w: model.idx().getSortedWords()) {
//			if(w.freq() < 1) break;
//			if(w.getMorphParadigm() == null) continue;
//			MorphParadigm mp = w.getMorphParadigm();
//			MapsOps.addFreq(mp.getLabel(), mpStats);
//		}
		//compare old stats with new
		Set<MorphParadigm> toDelete = new HashSet<>();
		for(String mpstring: model.idx().getMPlabels()) {
			MorphParadigm mp = model.idx().getMorphParadigm(mpstring);
			if(mp == null) continue;
				
			double newMPfreq = 0.0;
			double oldMPfreq = 0.0;
			if(mpStats.get(mp.getLabel()) != null)
				oldMPfreq = 	mpStats.get(mp.getLabel());
			newMPfreq = mp.getFreq();
			
			if(mp.getFreq() < thh) {
//				if(!mp.wasVanishing()) {
//					System.out.println("VANISHING " + mp.getLabel() + " old " + oldMPfreq + " new " + newMPfreq);
//					mp.setVanishing(true);
//				}
//				else {
					System.out.println(mp.getLabel() + " old " + oldMPfreq + " new " + newMPfreq + " thh " + thh);
					toDelete.add(mp);
//				}
//				System.out.println(mp.getLabel() + " old " + oldMPfreq + " new " + newMPfreq + " small, no uniqs");
//				toDelete.add(mp);
			}else {
				System.out.println("SAVED " + mp.getLabel() + " old " + oldMPfreq + " new " + newMPfreq);
				mp.setVanishing(false);
			}
//			else if(hasUniqFlex(mp, mpFlexStats)) {
//				System.out.println("SAVED UNIQ " + mp.getLabel() + " old " + oldMPfreq + " new " + newMPfreq);
////				mp.setVanishing(false);
//			}else if(hasNoUniqsAndSmall(mp, model)){
//				System.out.println(mp.getLabel() + " old " + oldMPfreq + " new " + newMPfreq + " small, no uniqs");
//				toDelete.add(mp);
//			}
//			else if((newMPfreq >= oldMPfreq && newMPfreq > 0.0)) { // || (newMPfreq > 100 && newMPfreq > (oldMPfreq *0.8)) ) {
//				System.out.println("SAVED " + mp.getLabel() + " old " + oldMPfreq + " new " + newMPfreq);
//				mp.setVanishing(false);
//			}
//			else { 
//				if(!mp.wasVanishing()) {
//					System.out.println("VANISHING " + mp.getLabel() + " old " + oldMPfreq + " new " + newMPfreq);
//					mp.setVanishing(true);
//				}
//				else {
//					System.out.println(mp.getLabel() + " old " + oldMPfreq + " new " + newMPfreq);
//					toDelete.add(mp);
//				}
//			}
		}
		return toDelete;
	}
	
private static double getVanishingThh(WordSequences model) {
		// TODO Auto-generated method stub
	Map<String,Double> stats = new HashMap<>();
	for(String mpstring: model.idx().getMPlabels()) {
		MorphParadigm mp = model.idx().getMorphParadigm(mpstring);
		stats.put(mp.getLabel(), mp.getFreq());
	}
		return MapsOps.getFirst(stats).freq / 10.0;
	}

//	private static Map<Flexion, Double> collectFlexStats(WordSequences model) {
//		Map<Flexion, Double> stats = new HashMap<>();
//		for(String mpstring: model.idx().getMPlabels()) {
//			MorphParadigm mp = model.idx().getMorphParadigm(mpstring);
//			for(Flexion f: mp.getFlexes())
//				MapsOps.addFreqObject(f, stats, 1.0);
//		}
//		return stats;
//	}
//	
//	private static boolean hasUniqFlex(MorphParadigm mp, Map<Flexion, Double> stats) {
//		for(Flexion f: mp.getFlexes()) {
//			if(stats.containsKey(f) && stats.get(f) == 1) return true;
//		}
//		return false;
//	}
	
	private static boolean hasNoUniqsAndSmall(MorphParadigm mpInTest, WordSequences model) {
		Collection<MorphParadigm> mpars = model.idx().getMorphParadigms();
		List<MorphParadigm> mparListSortedOnFreqAsc = mpars.stream().sorted((a,b) -> (int)(a.getFreq()-b.getFreq())).collect(Collectors.toList());

		for( MorphParadigm mp: mparListSortedOnFreqAsc) {
			boolean allTestFlexesInThisMP = true;
			for(Flexion f: mpInTest.getFlexes()) {
				if(!mp.getFlexes().contains(f)) {
					allTestFlexesInThisMP = false;
					break;
				}
			}
			if(allTestFlexesInThisMP && mp.getFreq() > mpInTest.getFreq()) return true;
		}
		return false;
	}

//	public static Set<MorphParadigm> collectVanishingMParsOld(WordSequences model) {
//		Map<String,Double> mpStats = new HashMap<>();
//		//collect new stats
//		for(Word w: model.idx().getSortedWords()) {
//			if(w.freq() < 1) break;
//			if(w.getMorphParadigm() == null) continue;
//			MorphParadigm mp = w.getMorphParadigm();
//			MapsOps.addFreq(mp.getLabel(), mpStats);
//		}
//		//compare old stats with new
//		Set<MorphParadigm> toDelete = new HashSet<>();
//		for(String mpstring: model.idx().getMPlabels()) {
//			MorphParadigm mp = model.idx().getMorphParadigm(mpstring);
//			if(mp == null) continue;
//			double newMPfreq = 0.0;
//			if(mpStats.get(mp.getLabel()) != null)
//				newMPfreq = 	mpStats.get(mp.getLabel());
//			if((newMPfreq > mp.getFreq() ) || (mp.getFreq() > 100 && newMPfreq > (mp.getFreq() *0.9)) ) {
//				System.out.println("SAVED " + mp.getLabel() + " old " + mp.getFreq() + " new " + mpStats.get(mp.getLabel()));
//			}else {
//				System.out.println(mp.getLabel() + " old " + mp.getFreq() + " new " + mpStats.get(mp.getLabel()));
//				toDelete.add(mp);
//			}
//		}
//		return toDelete;
//	}

	public static void deleteVanishingMParsFromWords(WordSequences model, Set<MorphParadigm> vanishingMPs) {
		for(Word w: model.idx().getSortedWords()) {
			if(w.freq() < 2) break;
			MorphParadigm mp = w.getMorphParadigm();
			if(mp == null) continue;
			if(vanishingMPs.contains(mp))
				w.changeMorphParadigm(null);
		}
	}

	public static void deleteVanishingAndEqualMPars(WordSequences model, Set<MorphParadigm> vanishingMPs) {
		Set<String> seen = new HashSet<>();
		Collection<MorphParadigm> mpars = model.idx().getMorphParadigms();
		List<MorphParadigm> mparListSortedOnFreqDesc = mpars.stream().sorted((a,b) -> (int)(b.getFreq()-a.getFreq())).collect(Collectors.toList());

		for(MorphParadigm mp: mparListSortedOnFreqDesc) {
			String flexes = mp.getSortedFlex();
			if(seen.contains(flexes)) {
				vanishingMPs.add(mp); 
				System.out.println("MP DELETED equal flexes: " + mp.getLabel() + " " +flexes);
			}
			
			seen.add(flexes);
		}
		for(MorphParadigm mp: vanishingMPs)
			model.idx().deleteMorphPar(mp.getLabel());	
	}

	public static void removeAllMparsinWords(WordSequences model) {
		for(Word w: model.idx().getSortedWords()) {
			w.setFlexRootMPar(null, null, model,false);
		}
	}

	public static void cleanMPwords(WordSequences model) {
		for(String mpstring: model.idx().getMPlabels()) {
			MorphParadigm mp = model.idx().getMorphParadigm(mpstring);
			mp.words.clear();
//			mp.freqNull();
		}
//		for(Word w: model.idx().getSortedWords()) {
//			MorphParadigm mp = w.getMorphParadigm();
//			if(w.getMorphParadigm() == null) continue;
//			mp.addFreq();
//			mp.addWord(w);
//		}
	}




}
