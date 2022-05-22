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
//import java.util.stream.Collectors;
import java.util.stream.Collectors;

import javafx.util.Pair;
import modelparts.Flexion;
import modelparts.MorphParadigm;
import modelparts.Root;
import modelparts.Word;
import modeltrain.MorphAnalyzer;
import modeltrain.SyntParVectorTrain;
import modelutils.Cluster;
import util.MapsOps;
import util.MyPair;
import util.MyPairWord;
import util.MyUtils;
import util.SetOps;

public class MorphVectorAnalyzer {
	
	public static final double HARD_THH_FOR_WAITING_FLEXES = 5;
	public static final boolean ALLOW_FREQ_ONE_VECTOR_CONTEXT = true;
	public static Map<String, List<String>> mparContextsMapLeft = new HashMap<String, List<String>>();
	public static Map<String, List<String>> mparContextsMapRight = new HashMap<String, List<String>>();

	public static  Map<String, List<String>> mparContextsMapLeftFlexion = new HashMap<>();
	public static  Map<String, List<String>> mparContextsMapRightFlexion = new HashMap<>();
	   
	
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

	public static void tagWordAddInitialMPasCateory(WordSequences model, int wfreqthh, boolean print) {
		if(print) {
			//print most frequent flexes
			for(String f: model.idx().seenFlexes.keySet())
				if(model.idx().seenFlexes.get(f) > wfreqthh) 
					System.out.println(f + "\t" + model.idx().seenFlexes.get(f));
		}
		double maxFlexNumAllowed = 50;
		double minFlexFreqAllowed = 100;
		double flexThh = getFlexFreqThh(model.idx().seenFlexes, maxFlexNumAllowed, minFlexFreqAllowed);
		System.out.println(flexThh);
		Set<String> flexToTag = MapsOps.getFirstEntriesObject(model.idx().seenFlexes, (int)maxFlexNumAllowed, flexThh).keySet();
		MapsOps.printSortedMap(model.idx().seenFlexes, null, (int)maxFlexNumAllowed, (int)flexThh, true, "\n");
		if(flexToTag.size() > 20) wfreqthh = 15;
		for(Word w: model.idx().getSortedWords()) {
			if(w.freq() < wfreqthh) break;
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
		return Math.max(def, freq/percent);
	}

	public static void findWaitingFlexesViaRoots(WordSequences model, String paradigmFilterREgex ,
	    boolean print, int contextcount) {
//		model.idx().fillBuckets(2);
//		model.idx().saveMorphParsFreqs();
//		clenWaitingFlexes(model);
		// get cats vectors
		collectMParVectorsFlexion(model, paradigmFilterREgex, print, contextcount);

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
	
	public static void tagWords(WordSequences model, int thh, String paradigmFilterREgex, 
	    boolean checkAllWords, boolean stopRecursion, boolean print, int contextcount) {
		model.idx().fillBuckets(2);
		model.idx().saveMorphParsFreqs();
		clenWaitingFlexes(model);
		// get cats vectors
		collectMParVectorsFlexion(model, paradigmFilterREgex, print, contextcount);
		for(Word w: model.idx().getSortedWords()) {
			if(w.freq() < thh) break;
			if(!checkMorph(w,model)) continue;
			//get word vectors, get score with cat vectors, get best cat
			tagOneWord(w, model, paradigmFilterREgex, checkAllWords, stopRecursion, print, contextcount);
			
		}
	}
	
	public static void retagWords(WordSequences model, int thh, String paradigmFilterREgex, 
	    boolean checkAllWords, boolean stopRecursion, boolean print, int contextcount) {
		model.idx().fillBuckets(2);
		model.idx().saveMorphParsFreqs();
		clenWaitingFlexes(model);
		// get cats vectors
		collectMParVectorsFlexion(model, paradigmFilterREgex, print, contextcount);
		//delete mpars from words
		for(Word w: model.idx().getSortedWords()) {
			w.setFlexRootMPar(null, null, model, stopRecursion);
		}
		for(Word w: model.idx().getSortedWords()) {
			
			if(w.freq() < thh) break;
			if(!checkMorph(w,model)) continue;
			if(w.getMorphParadigm() != null) continue;
			//get word vectors, get score with cat vectors, get best cat
			tagOneWord(w, model, paradigmFilterREgex, checkAllWords, false, print, contextcount);
			
		}
	}
	
	private static void tagOneWord(Word w, WordSequences model, String paradigmFilterREgex, 
	    boolean checkAllWords, boolean stopRecursion, boolean print, int contextcount) {
		if(!checkMorph(w,model)) return;

		List<MyPair> parscores = getMParFromFlexVector(model, w, ALLOW_FREQ_ONE_VECTOR_CONTEXT, paradigmFilterREgex, print, contextcount);
//		if(w.toString().matches("^(minerals|designations|standards|poems|professors|professor|click|dolphins|dolphin)$"))
//			System.out.println(parscores.toString());
		if(parscores == null||parscores.isEmpty()) return;
		List<String> mpBucket = getFlexMPbucket(parscores,0.5);
		MyPair bestpar1 = getBestParFromList(parscores,w,model); //parscores.get(parscores.size()-1);
		//check flex			//check root			//add waiting flex
		MyPair bestpar = checkConflicts(bestpar1, mpBucket, w, model,paradigmFilterREgex, checkAllWords, contextcount);
		if(print && bestpar != null) System.out.println("BESTPAR\t"+w+"\t" + bestpar);
		
		if(bestpar == null) {
//			if(w.getMorphParadigm() != null) System.out.println("TAGGED NULL: " + w.toString() + " which had: " + w.getMorphParadigm().getLabel()
//					+ " " + w.getMorphParadigm().getFlexes().toString());
//			w.changeMorphParadigm(null);
//			if(bestpar1 != null && w.getMorphParadigm() != null) {
//				System.out.println("WORD: " + w + "\tWAS: " +w.getMorphParadigm().getLabel()+" "+w.getMorphParadigm().getFlexes()
//						+"\tFOUND: "+ bestpar1.toString());
//				System.out.println(w.getBestContextsComputeNewAsList(true, CONTEXT_COUNT, Words.SYNSEM_FILTER, model, false));
//				System.out.println(w.getBestContextsComputeNewAsList(false, CONTEXT_COUNT, Words.SYNSEM_FILTER, model, false));
//			}
			return;
		}
//		if(bestpar != null) {
		String flex = MorphModel.getFlexFromFlexPar(bestpar.first, MorphModel.FPREF);
		MorphParadigm newMP = model.idx().getMorphParadigm(MorphModel.getMPlabelFromFlexPar(bestpar.first));
		w.setFlexRootMPar(flex, newMP, model,stopRecursion);
//		}
		//write bestPar into word
	}

	private static List<String> getFlexMPbucket(List<MyPair> parscores, double thh) {
		List<String> bucket = new ArrayList<String>();
		if(parscores == null || parscores.isEmpty()) return bucket;
		double bestScore = parscores.get(parscores.size()-1).freq;
		thh = bestScore+thh;
		for (int i = parscores.size()-1; i >= 0 ; i--) {
			MyPair mp = parscores.get(i);
			if(mp.freq > thh) break;
			bucket.add(mp.first);
		}
		return bucket;
	}

	public static void findWaitingFlexes(WordSequences model, int thh, String paradigmFilterREgex, 
	    boolean checkAllWords, boolean print, int contextcount) {
		model.idx().fillBuckets(2);
		model.idx().saveMorphParsFreqs();
		clenWaitingFlexes(model);
		// get cats vectors
		collectMParVectorsFlexion(model, paradigmFilterREgex, print, contextcount);
		for(Word w: model.idx().getSortedWords()) {
			if(w.freq() < thh) break;
			if(!checkMorph(w,model)) continue;
			if(w.getMorphParadigm() != null) continue;
			//get word vectors, get score with cat vectors, get best cat
			List<MyPair> parscores = getMParFromFlexVector(model, w, ALLOW_FREQ_ONE_VECTOR_CONTEXT, paradigmFilterREgex, print, contextcount);
			if(parscores == null||parscores.isEmpty()) continue;
			MyPair bestpar1 = getBestParFromList(parscores,w,model); //parscores.get(parscores.size()-1);
			//check flex			//check root			//add waiting flex
			MyPair bestpar = checkNewRoot(bestpar1.first,null,  w, model,paradigmFilterREgex, checkAllWords, contextcount); //checkConflicts(bestpar1, w, model,paradigmFilterREgex);
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
		model.idx().saveMorphParsFreqs();

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
			mp.getWaitingFlexMap().clear();
			mp.waitingFlexWordMap.clear();
		}
	}

	public static void collectMParVectorsParadigm(WordSequences model, String regexForParadimFilter, 
	    boolean print, int contextcount) {
		mparContextsMapLeft.clear();
		mparContextsMapRight.clear();
		//collect all tagged flexes
		Set<String> seenflexes = new HashSet<>();;
		Set<String> mparlabels = model.idx().getMPlabels();
		for(String label: mparlabels) {
			seenflexes.add(label);
		}
		//collect best contexts for left and right for each mpar flex
		for (String mp : seenflexes) {
			Word mpword = model.getWord(mp);
			List<String> bestContextsLeft = new ArrayList<>();
			for (MyPairWord key : mpword.getBestContextsComputeNew(true, contextcount, regexForParadimFilter, model,	false)) {
				bestContextsLeft.add(key.left.toString());
			}
			mparContextsMapLeft.put(mp, bestContextsLeft);
			List<String> bestContextsRight = new ArrayList<>();
			for (MyPairWord key : mpword.getBestContextsComputeNew(false, contextcount, regexForParadimFilter, model, false)) {
				bestContextsRight.add(key.left.toString());
			}
			mparContextsMapRight.put(mp, bestContextsRight);
		}
		if(print) {
			MapsOps.printMapObject(mparContextsMapLeft, null);
			MapsOps.printMapObject(mparContextsMapRight, null);
		}
	}
	
	   public static void collectMParVectorsFlexion(WordSequences model, String regexForParadimFilter, int contextcount) {
	        Set<String> seenflexes = new HashSet<>();;
	        for(String mpstring: model.idx().getMPlabels()) {
	            MorphParadigm mp = model.idx().getMorphParadigm(mpstring);
	            if(mp == null) continue;
	            for(Flexion f: mp.getFlexes())
	                seenflexes.add(MorphModel.getFlexLabel(f.toString(), mp.getLabel(), MorphModel.FPREF));
	        }
	        //collect best contexts for left and right for each mpar flex
	                for(String flex: seenflexes) {
	                    Word flexword = model.getWord(flex);
	                    List<String> bestContextsLeft = new ArrayList<>();
	                    for(MyPairWord key: flexword.getBestContextsComputeNew(true, contextcount, regexForParadimFilter, model, false)){
	                        bestContextsLeft.add(key.left.toString());
	                    }
	                    mparContextsMapLeftFlexion.put(flex, bestContextsLeft);
	                    List<String> bestContextsRight = new ArrayList<>();
	                    for(MyPairWord key: flexword.getBestContextsComputeNew(false, contextcount, regexForParadimFilter, model, false)){
	                        bestContextsRight.add(key.left.toString());
	                    }
	                    mparContextsMapRightFlexion.put(flex, bestContextsRight);
	                }
	    }
	
	public static void collectMParVectorsFlexion(WordSequences model, String regexForParadimFilter, 
        boolean print, int contextcount) {
        mparContextsMapLeftFlexion.clear();
        mparContextsMapRightFlexion.clear();
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
            for (MyPairWord key : flexword.getBestContextsComputeNew(true, contextcount, regexForParadimFilter, model,  false)) {
                bestContextsLeft.add(key.left.toString());
            }
            mparContextsMapLeftFlexion.put(flex, bestContextsLeft);
            List<String> bestContextsRight = new ArrayList<>();
            for (MyPairWord key : flexword.getBestContextsComputeNew(false, contextcount, regexForParadimFilter, model, false)) {
                bestContextsRight.add(key.left.toString());
            }
            mparContextsMapRightFlexion.put(flex, bestContextsRight);
        }
        if(print) {
            MapsOps.printMapObject(mparContextsMapLeftFlexion, null);
            MapsOps.printMapObject(mparContextsMapRightFlexion, null);
        }
    }
	
	public static List<MyPair> getMParFromFlexVector(WordSequences model, Word w, boolean allwoFreqOne, String paradigmFilterREgex, boolean print, int contextcount) {
		return getMParFromVector(model, w, allwoFreqOne, paradigmFilterREgex, print, mparContextsMapLeftFlexion, mparContextsMapRightFlexion, contextcount);
	}
	
    public static List<MyPair> getMParFromParVector(WordSequences model, Word w, boolean allwoFreqOne, String paradigmFilterREgex, boolean print, int contextcount) {
      return getMParFromVector(model, w, allwoFreqOne, paradigmFilterREgex, print, mparContextsMapLeft, mparContextsMapRight, contextcount);
    }	
	public static List<MyPair> getMParFromVector(WordSequences model, Word w, boolean allwoFreqOne, String paradigmFilterREgex, boolean print,
			Map<String, List<String>> paradigmVectorsLeft, Map<String, List<String>> paradigmVectorsRight, int contextcount) {
		if(!checkMorph(w,model)) return new ArrayList<>();
		List<String> bestContextsLeft = w.getBestContextsComputeNewAsList(true, contextcount, paradigmFilterREgex, model, allwoFreqOne, contextcount);
		List<String> bestContextsRight = w.getBestContextsComputeNewAsList(false, contextcount, paradigmFilterREgex, model, allwoFreqOne, contextcount);
	
		double bestScore = Double.MAX_VALUE;
		String bestFlexMP = null;
		List<MyPair> tmpListForPrint = new ArrayList<>();
		for(String fl: paradigmVectorsLeft.keySet()) { //left or right, both have same labels
			double parScore = getParadigmScores( bestContextsLeft, 	bestContextsRight, fl, paradigmVectorsLeft, paradigmVectorsRight);
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
		
//		if(bestScore > getScaledVectorSimilarityThh(contextcount)) return null;
//		if(bestFlexMP == null) return null;
		return tmpListForPrint; //new Pair<String, Double>(bestFlexMP, bestScore);
	}
	
	public static MyPair getBestPar(List<MyPair> parscores) {
	  if(parscores == null || parscores.isEmpty()) return null;
	  return parscores.get(parscores.size()-1);
	}

	public static double getScaledVectorSimilarityThh(int contextcount) {
    
    return  ((contextcount/2)-0.1);
  }

  private static double getParadigmScores( List<String> bestContextsLeft,	List<String> bestContextsRight, String parLabel, 
			Map<String, List<String>> paradigmVectorsLeft, Map<String, List<String>> paradigmVectorsRight) {
		double scoreL = Cluster.getScaledSimMeasure(bestContextsLeft, paradigmVectorsLeft.get(parLabel)) ;
		double scoreR = Cluster.getScaledSimMeasure(bestContextsRight, paradigmVectorsRight.get(parLabel)  );
		double parScore = (scoreL + scoreR) /2;
		 scoreL = Cluster.getScaledSimMeasure(paradigmVectorsLeft.get(parLabel), bestContextsLeft) ;
		 scoreR = Cluster.getScaledSimMeasure(paradigmVectorsRight.get(parLabel), bestContextsRight) ;
		 parScore = (parScore + ((scoreL + scoreR) /2)) / 2;
		return parScore;
	}

	private static MyPair checkConflicts(MyPair bestFlexMP, List<String> mpBuckets, Word w,  
	    WordSequences model, String categoryFilterRegex, boolean checkAllWords, int contextcount) {
		//check null-Flexion OR a new (waiting) flexion again
		if (bestFlexMP == null) return null;
		MyPair tmpBestFlexMP = checkRootFlexConflict(bestFlexMP.first, mpBuckets, w, model, categoryFilterRegex, checkAllWords, contextcount);
		return tmpBestFlexMP;
	}

	private static MyPair checkNewRoot(String bestFlexMP, List<String> mpBucket, Word w, 
	    WordSequences model, String categoryFilterRegex, boolean checkAllWords, int contextcount) {
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
				if(newRoot.toString().length()<4) break;
				if(!w.toString().startsWith(newRoot.toString())) continue;
				countTriedRoots++;
//				if(w.getFlex()==null) {
//					System.out.println("WORD OHNE ROOT GOT root: "+w+" " + newRoot + " paradigm: " +bestFlexMP+"\t"+ mpComputed.getSortedFlex() );
//				}
				
				
				String newFlex = w.toString().substring(newRoot.toString().length());
				if(newFlex.length()==0) newFlex = "_";
				
				if( confirmRootFlexInParadigm(mpComputed, newRoot, mpBucket, model.idx().getFlex(newFlex), model,categoryFilterRegex, checkAllWords, true, contextcount)) {
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

	private static MyPair checkRootFlexConflict(String bestFlexMP, List<String> mpBucket, Word w,  
	    WordSequences model, String categoryFilterRegex, boolean checkAllWords, int contextcount) {
		String computedFlex = MorphModel.getFlexFromFlexPar(bestFlexMP, MorphModel.FPREF);
		MorphParadigm mpComputed = model.idx().getMorphParadigm(MorphModel.getMPlabelFromFlexPar(bestFlexMP));
		if(mpComputed == null) return null;
		//to avoid suffixes as waiting flexes - try first the whole word as a root: Flugzeugbauer, Unternehmen
		Root wordroot =  model.idx().getRoot(w.toString());
		String zeroFlex= "_";
		boolean hasFlexConflict = hasFlexionConflict(bestFlexMP, w, model);
		//for zero flex
		if(!hasFlexConflict && mpComputed.getFlexes().size() > 1 && mpComputed.containsFlex(model.idx().getFlex(zeroFlex)) && 
				confirmRootFlexInParadigm(mpComputed, wordroot, mpBucket, model.idx().getFlex(zeroFlex), model,categoryFilterRegex, checkAllWords, false, contextcount)) {
			return new MyPair(MorphModel.getFlexLabel(zeroFlex, mpComputed.getLabel(), MorphModel.FPREF), "", 1.0);
//			return false;
		}
		if(!hasFlexConflict) {//means word ends with flex
			int flexLength = computedFlex.equals("_") ? 0 : computedFlex.length();
			Root r = model.idx().getRoot(w.toString().substring(0, w.toString().length()-flexLength));
			String newFlex = w.toString().substring(r.toString().length());
			if(confirmRootFlexInParadigm(mpComputed, r, mpBucket, model.idx().getFlex(newFlex), model,categoryFilterRegex, checkAllWords, false, contextcount))
				return new MyPair(MorphModel.getFlexLabel(newFlex, mpComputed.getLabel(), MorphModel.FPREF), "", 1.0);
//				return false;
		}
		//find flex, confirm rootflex in paradigm
		String flex = findFlex(w, mpComputed, model, contextcount);
        if(flex == null) return null;
        return new MyPair( MorphModel.getFlexLabel(flex, mpComputed.getLabel(), MorphModel.FPREF), "", 1.0);
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

	public static int addWaitingFlexes(WordSequences model, double thh, boolean checkAllParadigmFlexes, String categoryFilterRegex, 
  			boolean initial, boolean print, int contextcount) {
//	  print = true;
  		int waitingFlexAdded = 0;
  		Collection<MorphParadigm> mpars = model.idx().getMorphParadigms();
  		List<MorphParadigm> mparListSortedOnFlexCount = mpars.stream().sorted((a,b) -> a.getFlexes().size()-b.getFlexes().size()).collect(Collectors.toList());
  		
  		double checkAllFlexThh = model.idx().words.size()*0.0001;
  		if(checkAllParadigmFlexes) System.out.println(" check all flex THH: " +checkAllFlexThh);
  		for(MorphParadigm mp: mparListSortedOnFlexCount) {
  //			if(mp.getFlexes().size() == 1)
  //				checkAllFlexThh = getScaledAllFexThh(mp, model);
  			if(mp == null) continue;
  			SortedMap<String, Double> sorted = MapsOps.getSortedMapString(mp.getWaitingFlexMap());
  			boolean thhSet = false;
  			for (Iterator iter = sorted.keySet().iterator(); iter.hasNext();) {
  				String waitingFlex = (String) iter.next();
  				double freq = (Double) sorted.get(waitingFlex) ;
  				if(!thhSet) {
  					thh = getFlexibleTHH(freq);
  					if(print) System.out.println("FLEX ADD THH: " +thh+" "+mp.toString());
  					thhSet = true;
  					checkAllFlexThh = Math.max(20.0, mp.getFreq()/mp.getFlexes().size()/2.0); // 40 is too much, 10 is too small for big model, 
                    checkAllFlexThh = Math.min(checkAllFlexThh, thh); //we cannot find more matchallflex than flex
  					//10 makes noun+verb paradigms (vermischt), 40 probably needs more rounds to converge, os is stuck than at 6-10 paradigms at the end
  //					checkAllFlexThh = Math.max(40.0, mp.getFreq()/mp.getFlexes().size()/2.0);
  					if(print) System.out.println("SCALED ALL flex THH: " +checkAllFlexThh+" for mp: " + mp.toString());
  
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
  					if(countRootsWithAllFlexes.size() < checkAllFlexThh) {
  					  if(print) System.out.println("waitingFlex " + waitingFlex + " does not match all flexes from " + mp.getLabel() + " " +mp.getSortedFlex()
  						+" found words with all flexes"+ countRootsWithAllFlexes.size());
  						continue;
  					}
  //					else {
  //						System.out.println("MATCHING ROOTS: " + " for flex: " +waitingFlex + " for MO: " + mp.getLabel() + " " + mp.getSortedFlex()
  //					+ countRootsWithAllFlexes.toString());
  //					}
  				}
  				Word waitingFlexCatWord = model.getWord(getTMPLabelForWaitingCat(mp.getLabel(), waitingFlex));
  				List<MyPair> bestParList = getMParFromFlexVector(model, waitingFlexCatWord, ALLOW_FREQ_ONE_VECTOR_CONTEXT, categoryFilterRegex,print, contextcount);
  				if(bestParList == null || bestParList.isEmpty()) continue;
  //				MyPair bestPar = bestParList.get(bestParList.size()-1);
  				MyPair bestPar = getBestParFromList(bestParList,waitingFlexCatWord,model); //parscores.get(parscores.size()-1);
  
  //				if(initial || checkAllParadigmFlexes || (bestPar != null && bestPar.first.endsWith(mp.getLabel()) )) {
  				if( (bestPar != null && bestPar.first.endsWith(mp.getLabel()) )) {
  					waitingFlexAdded ++;
  					if(print) System.out.println("WAITING FLEX confirmed: " + waitingFlex +" " + bestPar+ " " + mp.getLabel() + " " + mp.getFlexes());
  					mp.addWaitingFlex(model.idx().getFlex(waitingFlex),model);
  				}
  //				else if (initial  ) { // if initial take second best, because first is itself
  //					waitingFlexAdded ++;
  //					System.out.println("WAITING FLEX confirmed INIT: " + waitingFlex +" " + mp.getLabel() + " " + mp.getFlexes());
  //					mp.addWaitingFlex(model.idx().getFlex(waitingFlex),model);
  //				}
  				else if (initial && bestPar != null && bestParList.size() > 1 ) { // if initial take second best, because first is itself
  					MyPair secondBest = checkVectorSimOverTHH(waitingFlex,bestParList, mp, contextcount); //bestParList.get(bestParList.size()-2);
  					if(secondBest != null && secondBest.first.endsWith(mp.getLabel())) {
  						waitingFlexAdded ++;
  						String bpScore = "";
  						if(bestPar != null) bpScore = Double.toString(bestPar.freq);
  						if(print) System.out.println("WAITING FLEX confirmed 2: " 
  						+ waitingFlex +" " + secondBest+ " best par score: " +bpScore+" from: " + bestPar+" " + mp.getLabel() + " " + mp.getFlexes());
  						mp.addWaitingFlex(model.idx().getFlex(waitingFlex),model);
  					}
  					else
  					  if(print) System.out.println("WF declined: " + waitingFlex +" " + bestPar+ " " + mp.getLabel() + " " + mp.getFlexes()
  					+" SECOND BEST " + secondBest );
  				}
  				else if(bestPar == null) {
  					System.out.println("here");
  					bestParList = getMParFromFlexVector(model, waitingFlexCatWord, ALLOW_FREQ_ONE_VECTOR_CONTEXT, categoryFilterRegex,print, contextcount);
  					if(bestParList == null || bestParList.isEmpty()) continue;
  					bestPar = bestParList.get(bestParList.size()-1);
  					bestPar = getBestParFromList(bestParList,waitingFlexCatWord,model); //parscores.get(parscores.size()-1);
  				}
  				else {
  				  
  				  if(print) System.out.println("WF declined: " + waitingFlex +" " + bestPar+ " " + mp.getLabel() + " " + mp.getFlexes() );
  				}
  //				if(initial) break; // add only 1 flex initially
  				//check suffixes!!!!!!!!!
  			}
  		}
  		return waitingFlexAdded;
  	}

  private static boolean confirmRootFlexInParadigm( MorphParadigm mpComputed, Root r, List<String>mpBucket, Flexion newFlex, WordSequences model, 
			String categoryFilterRegex, boolean checkAllWords, boolean waiting, int contextcount) {
		if(mpComputed == null) return false;
			
		int countFlexes = 0;
		int paradigmFlexCountThh = 1; //
		if(waiting)
			paradigmFlexCountThh = mpComputed.getFlexes().size() < 3 ? 1 : 2; //if this root has 2 flexes of this par it is confirmed
		for(Flexion paradigmFlex: mpComputed.getFlexes()) {
			//angenommen computed paradigm; f_e_m_2 für word Beteiligungs, root: Beteiligung, flex s, check m_ flexes with Beteiligung,
			//if there are such words in the model and if they could get into the same paradigm. if so, tag all of them with paradigm
			String flexstring = paradigmFlex.toRealString();
			if(model.idx().containsWord(r.toString()+flexstring)) {
				Word newParadigmWord = model.getWord(r.toString()+flexstring);
				if(checkWordExistsAndSameParadigm(newParadigmWord, model, mpComputed.getLabel(), mpBucket, categoryFilterRegex, checkAllWords, contextcount)) {
					countFlexes++;
					if(countFlexes > paradigmFlexCountThh)	return true;
				}
			}
		}
		return false;
	}

	private static boolean checkWordExistsAndSameParadigm(Word newParadigmWord, WordSequences model, String computedLabel, 
			List<String>mpBucket, String categoryFilterRegex,
			boolean checkAllWords, int contextcount) {
		if(mpBucket == null) mpBucket = new ArrayList<>();
		if(newParadigmWord.freq() < 2) return false;
		if(checkAllWords) return true;
		if(newParadigmWord.getMorphParadigm() != null && newParadigmWord.getMorphParadigm().getLabel().equals(computedLabel))
			return true;
		Pair<String,Double> bestPar = model.idx().flexPar777.get(newParadigmWord);
		if(bestPar == null) {
			List<MyPair> bestParList = getMParFromFlexVector(model, newParadigmWord, true, categoryFilterRegex,false, contextcount);
			if(bestParList == null || bestParList.isEmpty()) return false;
			MyPair bestPar1 = bestParList.get(bestParList.size()-1);
			if(bestPar1 != null && bestPar1.first.endsWith(computedLabel)) 
				return true;
			//check bucket
			for(String s: mpBucket)
				if(s.endsWith(computedLabel)) return true;
		}
		if(bestPar != null && bestPar.getKey().endsWith(computedLabel)) 
			return true;
		//check bucket
		for(String s: mpBucket)
			if(s.endsWith(computedLabel)) return true;
		return false;
	}

	/**
	 * Checks if word ends with paradigm flexion. if flex == Zero (_) - makes additional check 
	 * @param bestFlexMP
	 * @param w
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
		if(w.toString().length() < 4) return false;
		return true;
	}
	
	public static void writeTMPWaitingFlexCats(WordSequences model, boolean print) {
		Set<String> mparlabels = model.idx().getMPlabels();
		for(String l: mparlabels) {
			MorphParadigm mp = model.idx().getMorphParadigm(l);
			if(mp == null) continue;
			SortedMap<String, Double> sorted = MapsOps.getSortedMapString(mp.getWaitingFlexMap());
			boolean thhSet = false;
			double thh = 0.0;

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
				model.idx().tmpCats.add(catLabel);
				
				for(Word w: mp.waitingFlexWordMap.get(f))
					model.addCategory(catLabel, w);
			}
		}
	}
	
	private static String getTMPLabelForWaitingCat(String mplabel, String f) {
		return "t_"+f+"_"+mplabel;
	}

	private static double getScaledAllFexThh(MorphParadigm mp, WordSequences model) {
		double minFlexFreq = Double.MAX_VALUE;
		for(Flexion f: mp.getFlexes()) {
			if(model.idx().seenFlexes.containsKey(f.toString()) ){
				minFlexFreq = Math.min(minFlexFreq, model.idx().seenFlexes.get(f.toString()));
			}
		}
		double thh = minFlexFreq/3;
		System.out.println("SCALED ALL flex THH: " +thh+" for mp: " + mp.toString());
		return thh;
	}

	private static MyPair checkVectorSimOverTHH(String waitingFlex, List<MyPair> bestParList, MorphParadigm hostpar, int contextcount) {
		for(MyPair secondBest: bestParList) {
			if(secondBest != null && secondBest.first.endsWith(hostpar.getLabel()) && secondBest.freq < getScaledVectorSimilarityThh(contextcount)) { //6.1
				return secondBest;
			}
//			else if(secondBest != null && secondBest.first.endsWith(hostpar.getLabel()))
//				System.out.println("for waiting flex " + waitingFlex + " best was " + secondBest.first+" "+ secondBest.freq + " " + hostpar.toString());
		}
		return null;
	}
	
	private static double getFlexibleTHH(double freq) {
		return Math.max(10.0, freq/2);
//		return Math.max(10.0, freq/1.5);
	}

	public static void tagWordsWithWaitingFlexes(WordSequences model, boolean checkAllParadigmFlexes, String categoryFilterRegex, 
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
			double firstWaitingFreq = MapsOps.getFirst(mp.getWaitingFlexMap()).freq;
			if(firstWaitingFreq < 10) continue;
			SortedMap<String, Double> sorted = MapsOps.getSortedMapString(mp.getWaitingFlexMap());
			for (Iterator iter = sorted.keySet().iterator(); iter.hasNext();) {
				String f = (String) iter.next();
				double freq = (Double) sorted.get(f) ;
				if(freq < 10) break;
				if((freq * 2)<firstWaitingFreq) break;
//				firstWaitingFreq = freq; // too much noise
				
				//check suffixes!!!!!!!!!
				mp.addWaitingFlex(model.idx().getFlex(f),model);
				for(Word w: mp.waitingFlexWordMap.get(f)) {
					w.setFlexRootMPar(f,mp,model,false);
				}
			}
		}
		
	}

	public static void cleanMorphTags(WordSequences model, int round) {
      Set<String> mparlabels = model.idx().getMPlabels();
      for(String mplabel: mparlabels) {
          MorphParadigm mp= model.idx().getMorphParadigm(mplabel);
          if(mp == null) continue;   

          String oldLabel = mp.getLabel();
          String newLabel;
          if(round == -1)
            newLabel = oldLabel.replaceFirst("#[0-9]+$", "e");
          else
            newLabel = SyntParVectorTrain.getNewLabel(oldLabel, round);
          
          model.idx().knownParadigmLabels.remove(oldLabel);
          model.idx().deletedParadigmLabels.add(oldLabel);
          mp.changeLabel(newLabel);
          
          for(Flexion f: mp.getFlexes()) {
            Word fword = model.getWord(MorphModel.getFlexLabel(f.toString(), mplabel, MorphModel.FPREF));
            model.idx().deletedParadigmLabels.add(fword.toString());
        }
          for(String tmpCat: model.idx().tmpCats) {
            Word catWord = model.getWord(tmpCat);
            model.idx().deletedParadigmLabels.add(catWord.toString());
        }

      }
      for (String deleted : model.idx().deletedParadigmLabels) {
        MorphParadigm mpDel = model.idx().getMorphParadigm(deleted);
        if (mpDel == null)
          continue;
        model.idx().deleteMorphParadigm(deleted);
        model.idx().addMorphParadigm(mpDel);
      }
      model.idx().flexPar777.clear();
  }
	
	public static void cleanMorphTagsOld(WordSequences model) {
		Set<String> mparlabels = model.idx().getMPlabels();
		for(String mplabel: mparlabels) {
			MorphParadigm mp= model.idx().getMorphParadigm(mplabel);
			if(mp == null) continue;			
			Word mpword = model.getWord(mplabel);
			model.deleteCategory(mpword);
			for(Flexion f: mp.getFlexes()) {
				Word fword = model.getWord(MorphModel.getFlexLabel(f.toString(), mplabel, MorphModel.FPREF));
				model.deleteCategory(fword);
			}
		}
		for(String tmpCat: model.idx().tmpCats) {
			Word catWord = model.getWord(tmpCat);
			model.deleteCategory(catWord);
		}
//		printMorphParStats(model,thh, "after CLEAN");
//		model.idx().resetMorphPars();
		model.idx().flexPar777.clear();
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
	
	   public static void addMPasCategoryFromParWords(WordSequences model, boolean print, int ccount) {
	     for(MorphParadigm mp: model.idx().getMorphParadigms()) {
	       for(Word w: mp.getWords()) {
             model.addCategory(mp.getLabel(), w);
             String flex = findFlex(w, mp, model, ccount);
             if(flex == null) continue;
             String flexCatName = MorphModel.getFlexLabel(flex, mp.getLabel(), MorphModel.FPREF);
             model.addCategory(flexCatName, w);
            
	       }
	     }
	    }

	public static void addMZero(WordSequences model) {
		Set<String> wordsWithMP = new HashSet<>();
		for(MorphParadigm mp: model.idx().getMorphParadigms()) {
			for (Word w : mp.getWords()) {
				wordsWithMP.add(w.toString());
			}
		}
		MorphParadigm mZeroPar = model.idx().getNewMorphParadigm(SetOps.of(model.idx().getFlex("_")), MorphModel.MPREF+SyntParVectorTrain.MZERO);
		List<Word> wordsToCheck = model.getWords(0, Math.min(200, wordsWithMP.size()/2), true);
		for(Word w: wordsToCheck){
			if(wordsWithMP.contains(w.toString())) continue;
			mZeroPar.addWord(w);
		}
		System.out.println("MZEROs: " + mZeroPar.getWords().size()+ " " + mZeroPar.getWords().toString());

	}

	private static String findFlex(Word w, MorphParadigm mp, WordSequences model, int ccount) {
	  if(w.getFlex() != null && mp.containsFlex(model.idx().getFlex(w.getFlex())))
	    return w.getFlex();
      List<Flexion> fSorted = mp.getFlexes().stream().sorted((a,b) -> (-1)*(a.toString().length()-b.toString().length())).collect(Collectors.toList());

      for(Flexion f: fSorted) {
        String flex = f.toRealString();
        if(w.toString().endsWith(flex)) {
          String root = w.toString().substring(0,(w.toString().length()-flex.length()));
          if(confirmRoot(root, f, mp, model, ccount)) {
            w.setFlexRootMPar(f.toString(), mp, model, true);
            return f.toString();
          }
        }
      }
      return null;
    }

  private static boolean confirmRoot(String root, Flexion fInCheck, MorphParadigm mp, WordSequences model, int ccount) {
    for(Flexion f: mp.getFlexes()) {
      if(f.toString().equals(fInCheck.toString())) continue;
      if(mp.getWords().contains(model.getWord(root+f.toRealString()))) return true;
      if(confirmRootFlexInParadigm(mp, model.idx().getRoot(root), null, fInCheck, model, Words.SYNSEM_FILTER, false, false, ccount))
        return true;
    }
    return false;
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
//			if(w.freq() < thh) break;
//			if(!checkMorph(w, model)) continue;
			if(w.toString().contains("_")) continue;
			if(w.freq() >= thh)
				wcount++;
			if(w.getMorphParadigm() != null) wHasMP++;
			else continue;
			MorphParadigm mp = w.getMorphParadigm();
			if(mp == null) continue;
			MapsOps.addFreq(mp.getLabel(), mpStats);
		}
		MapsOps.printSortedMap(mpStats, null);
		System.out.println("from tried " + wcount + " words " + wHasMP + " tagged: " + MyUtils.rdouble((wHasMP/wcount)) ) ;
		System.out.println("from all uniqs " + model.idx().words.size() + " words " + wHasMP + " tagged: " + MyUtils.rdouble((wHasMP/model.idx().words.size())) ) ;
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
	
	public static Set<MorphParadigm> collectVanishingMPars(WordSequences model, 
	    String regexForParadimFilter, boolean print, int contextcount) {
		Set<MorphParadigm> new_Vanishing = collectVanishingMParsViaSimilarity(model, regexForParadimFilter, print, contextcount);
		Map<String,Double> mpStats = model.idx().getSavedMorphParsFreqs();
		//thh = biggest freq of mo / 10;
		double thh = getVanishingThh(model);
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
			if(containsUniqFlexes(mp, model)) {
				System.out.println("HAS UNIQS: "+ mp.toString() + " old " + oldMPfreq + " new " + newMPfreq);
			}
			else
			  if(mp.getFreq() < thh) {
					System.out.println(mp.getLabel() + " old " + oldMPfreq + " new " + newMPfreq + " thh " + thh);
					toDelete.add(mp);
			}else {
				System.out.println("SAVED " + mp.getLabel() + " old " + oldMPfreq + " new " + newMPfreq);
				mp.setVanishing(false);
			}
		}
		for(MorphParadigm mp: new_Vanishing)
			System.out.println("DELETE from parsim: " + mp.toString());
		toDelete.addAll(new_Vanishing);
		return toDelete;
	}
	
private static boolean containsUniqFlexes(MorphParadigm mpToTest, WordSequences model) {
	if(mpToTest.getFreq() < 10) return false;
	Set<Flexion> flexesSeenInOtherParadigms = new HashSet<>();
	for(String mpstring: model.idx().getMPlabels()) {
		MorphParadigm mp = model.idx().getMorphParadigm(mpstring);
		if(mp.getLabel().equals(mpToTest.getLabel()))
			continue;
		flexesSeenInOtherParadigms.addAll(mp.getFlexes());
	}
	int sumNotSeenFlexes = 0;
	for(Flexion f: mpToTest.getFlexes())
		if(!flexesSeenInOtherParadigms.contains(f))
			sumNotSeenFlexes++;
	double percentOfUniqs = (double)sumNotSeenFlexes/(double)mpToTest.getFlexes().size();
	if(percentOfUniqs > 0.5) 
		return true;
	return false;
}

private static Set<MorphParadigm> collectVanishingMParsViaSimilarity(WordSequences model, 
    String regexForParadimFilter, boolean print, int contextcount) {
//	int scoreTHH = getScoreTHH(model);
//	System.out.println("vector SCORE thh: " + scoreTHH);
	Map<MorphParadigm,MorphParadigm> toDelete = new HashMap<>();
	if(model.idx().getMorphParadigms().size() < 4) return toDelete.keySet();
	Map<String, List<String>> mparOnlyContextsMapLeft = new HashMap<>();
	Map<String, List<String>> mparOnlyContextsMapRight = new HashMap<>();
//	regexForParadimFilter = "([sbtfmxy]_.+)|s_splitter";
	collectMParVectors(model, regexForParadimFilter, 
	    mparOnlyContextsMapLeft, mparOnlyContextsMapRight, false, contextcount);
	Set<String> mparlabels = model.idx().getMPlabels();
	List<MyPair> scores = new ArrayList<>();
	Map<String,MyPair> secondBestMap = new HashMap<>();
	for(String label: mparlabels) {
		if(model.idx().getMorphParadigm(label) == null) continue;
		MorphParadigm testpar = model.idx().getMorphParadigm(label);
		if(testpar.getFreq() < 10) continue;
		Word mpword = model.getWord(label);
		List<MyPair> bestParList = getMParFromVector(model, mpword, ALLOW_FREQ_ONE_VECTOR_CONTEXT, 
		    regexForParadimFilter, print, 
		    mparOnlyContextsMapLeft, mparOnlyContextsMapRight, contextcount);
		if(bestParList == null || bestParList.isEmpty()) continue;
		MyPair bestPar = bestParList.get(bestParList.size()-1);
		if(bestPar.first.equals(label) && bestParList.size() > 1)
			bestPar = bestParList.get(bestParList.size()-2);
		if(bestParList.size() > 2) {
		  secondBestMap.put(label, bestParList.get(bestParList.size()-3));
		}
		bestPar.second = label;
		scores.add(bestPar);
		//delete paradigm with smaller freq
	}
	double scoreTHH = getScaledTHH(scores, model);
	System.out.println("vector SCORE thh: " + scoreTHH);
	Collections.sort(scores, Collections.reverseOrder());
	Set<MorphParadigm> combined = new HashSet<>();
	for(MyPair bestPar: scores) { // start from smallest
	  System.out.println("PARSIM: " + model.idx().getMorphParadigm(bestPar.second).toString() + " best: " + bestPar.first + " score: " + bestPar.freq
          +" " + model.idx().getMorphParadigm(bestPar.first).toString()
          +" SECOND BEST: " + secondBestMap.get(bestPar.second));
	  if((mparlabels.size()-toDelete.size()) < 4) {
	    return toDelete.keySet();
	  }
		if(bestPar.freq < scoreTHH) {
			MorphParadigm testpar = model.idx().getMorphParadigm(bestPar.second);
			MorphParadigm bestparadigm = model.idx().getMorphParadigm(bestPar.first);
//			if(combined.contains(bestparadigm)) continue;
			if(toDelete.containsKey(bestparadigm) && !toDelete.get(bestparadigm).getLabel().equals(testpar.getLabel())) {
			  bestparadigm = toDelete.get(bestparadigm);
//			  continue;
			}
			//do not combine two biggest paradigms
			
			//check synt neighbours
//			if(SyntParVectorTrain.clustersAreSyntacticalNeighbours(model.getWord(testpar.getLabel()), model.getWord(bestparadigm.getLabel()), model))
//			  {
//			    System.out.println("SYNT NEIGHBOURS: " + testpar.getLabel() + " AND " +bestparadigm.getLabel());
//			    System.out.println("PARSIM: " + model.idx().getMorphParadigm(bestPar.second).toString() + " best: " + bestPar.first + " score: " + bestPar.freq
//	                +" " + model.idx().getMorphParadigm(bestPar.first).toString()
//	                +" SECOND BEST: " + secondBestMap.get(bestPar.second));
//			    continue;
//			  }
			
			boolean bestparBigger = testpar.getFreq() <= bestparadigm.getFreq();
			if(bestparBigger) {
//				if(!toDelete.containsKey(testpar) ) {
					toDelete.put(testpar, bestparadigm);
					bestparadigm.addFlexes(testpar.getFlexes(), model);
					combined.add(bestparadigm);
//				}
			}else {
			  toDelete.put(bestparadigm, testpar);
              testpar.addFlexes(bestparadigm.getFlexes(), model);
              combined.add(testpar);
			}
//			else { // BAD here: not best scores would be combined
//			  if(!toDelete.contains(bestparadigm))  {
//			    toDelete.add(bestparadigm);
//			    testpar.addFlexes(bestparadigm.getFlexes(), model);
//			  }
//			}
		}
		
	}
	
	for(String label: mparlabels) {
      if(model.idx().getMorphParadigm(label) == null) continue;
      MorphParadigm mpar = model.idx().getMorphParadigm(label);
      Word mparWord = model.getWord(label);
      List<MyPairWord> expLeft = SyntParVectorTrain.getExpWords(mparWord, true, 0.01, model);
      List<MyPairWord> expR = SyntParVectorTrain.getExpWords(mparWord, false, 0.01, model);
      System.out.println(mpar.getLabel() + " " +mpar.getFlexes() + " neighbours left: " + SyntParVectorTrain.getExpFirstWord(expLeft) + " right: " + SyntParVectorTrain.getExpFirstWord(expR) );
//      System.out.println( "\t" + mpar.getLabel() + " " +mpar.getFlexes() + " neighbours left second: " + SyntParVectorTrain.getExpSecondWord(expLeft) + " right second: " + SyntParVectorTrain.getExpSecondWord(expR) );
	}
	
	return toDelete.keySet();
}

private static double getScaledTHH(List<MyPair> scores, WordSequences model) {
	double highestScore = 0;
	double lowestScore = Double.MAX_VALUE;
	MorphParadigm biggestmpar = null;
	for(MyPair mp: scores) {
		MorphParadigm mpar = model.idx().getMorphParadigm(mp.second);
		mpar.mostDifferentFromOtherMP = false;
	}
	for(MyPair mp: scores) {
		if(mp.freq > highestScore) highestScore = mp.freq;
		if(mp.freq < lowestScore) lowestScore = mp.freq;
		biggestmpar = model.idx().getMorphParadigm(mp.second);
	}
	if(biggestmpar == null) return 0.0;
	biggestmpar.mostDifferentFromOtherMP = true;
//    double thh = Math.max(lowestScore+0.001, Math.min(((highestScore + lowestScore)/2.0), 3.0));
    double thh = Math.min(((highestScore + lowestScore)/2.0), 20.0); //3.0
	if(highestScore < 2.5) thh = 1.0;
	return thh;
}

private static int getScoreTHH(WordSequences model) {
	int nUniqWords = model.idx().words.size();
	if(nUniqWords > 500000) return 1;
	return 2;
}

private static void collectMParVectors(WordSequences model, String regexForParadimFilter, 
		Map<String, List<String>> mparOnlyContextsMapLeft, 
		Map<String, List<String>> mparOnlyContextsMapRight, boolean print, int contextcount) {
	Set<String> mparlabels = model.idx().getMPlabels();
	for(String label: mparlabels) {
		if(model.idx().getMorphParadigm(label) == null) continue;
	//collect best contexts for left and right for each mpar flex
		Word mpword = model.getWord(label);
		List<String> bestContextsLeft = new ArrayList<>();
		for (MyPairWord key : mpword.getBestContextsComputeNew(true, contextcount, regexForParadimFilter, model,	false)) {
			bestContextsLeft.add(key.left.toString());
		}
		mparOnlyContextsMapLeft.put(label, bestContextsLeft);
		List<String> bestContextsRight = new ArrayList<>();
		for (MyPairWord key : mpword.getBestContextsComputeNew(false, contextcount, regexForParadimFilter, model, false)) {
			bestContextsRight.add(key.left.toString());
		}
		mparOnlyContextsMapRight.put(label, bestContextsRight);
	}
	if(print) {
		MapsOps.printMapObject(mparOnlyContextsMapLeft, null);
		MapsOps.printMapObject(mparOnlyContextsMapRight, null);
	}
}

private static double getVanishingThh(WordSequences model) {
		// TODO Auto-generated method stub
	Map<String,Double> stats = new HashMap<>();
	for(String mpstring: model.idx().getMPlabels()) {
		MorphParadigm mp = model.idx().getMorphParadigm(mpstring);
		stats.put(mp.getLabel(), mp.getFreq());
	}
		return (MapsOps.getFirst(stats).freq / 5.0);
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
			String flexes2 = mp.getSortedFlexFirstWaiting();
			if(seen.contains(flexes2)) {
				vanishingMPs.add(mp); 
				System.out.println("MP DELETED equal flexes+FIRSTWAITING: " + mp.getLabel() + " " +flexes2);
			}
			if(!vanishingMPs.contains(mp)) {
				seen.add(flexes);
				seen.add(flexes2);
			}
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

	public static void retagWordsInParadigms(WordSequences model, String parsFilter,	
	    boolean checkAllWords,		boolean print, int contextcount) {
		Collection<MorphParadigm> mpars = model.idx().getMorphParadigms();
		List<MorphParadigm> mparListSortedOnFreqAsc = mpars.stream().sorted((a,b) -> (int)(a.getFreq()-b.getFreq())).collect(Collectors.toList());
		boolean stopRecursion = false;

		for(MorphParadigm mp: mparListSortedOnFreqAsc) {
			Set<Word> wset = new HashSet<>();
			for(Word w: mp.getWords()) wset.add(w);
			for(Word w: wset)
				tagOneWord(w, model, parsFilter, checkAllWords, stopRecursion, print, contextcount);

		}
		
	}


	public static MyPair tagMorphPar(Word w, WordSequences model, int contextcount) {
		MyPair bestpar = getBestPar(getMParFromParVector(model, w, false, Words.SYNSEM_FILTER, false, contextcount));
		
		if(bestpar == null || hasFlexionConflict(bestpar.first, w, model)) {
			bestpar = checkNewRoot(bestpar.first,null,  w, model, Words.SYNSEM_FILTER, false, contextcount);
		}
		return bestpar;
	}


//	private static MyPair tryOtherRoot( Word w, WordSequences model) {
//		Set<Root> bucketRoots = model.idx().getPrefixBucketRoot(w);
//		List<Root> rSorted = bucketRoots.stream().sorted((a,b) -> (-1)*(a.toString().length()-b.toString().length())).collect(Collectors.toList());
//		int countTriedRoots = 0;
//		for(Root newRoot: rSorted) {
//			if(countTriedRoots > 3) break;
//			if(newRoot.toString().length()<2) break;
//			if(!w.toString().startsWith(newRoot.toString())) continue;
//			countTriedRoots++;
//			
//			String newFlex = w.toString().substring(newRoot.toString().length());
//			if(newFlex.length()==0) newFlex = "_";
//			MorphParadigm newPar = MorphParTrain.confirmFlexAndGetParadigm(newRoot, newFlex, model, w);
//			if(newPar != null) 
//				return new MyPair(MorphModel.getFlexLabel(w.getFlex(), newPar.getLabel(), MorphModel.FPREF), "", 1.0);
//		}
//		return null;
//	}

	public static MyPair tagMorphParSimple(Word w, WordSequences model, int contextcount) {
      MyPair bestpar = getBestPar(getMParFromParVector(model, w, false, Words.SYNSEM_FILTER, false, contextcount));
      return bestpar;
	}
	
	public static MyPair tagMorphParSimpleWithCheck(Word w, WordSequences model, int contextcount) {
      MyPair bestpar = getBestPar(getMParFromParVector(model, w, false, Words.SYNSEM_FILTER, false, contextcount));
		if(bestpar == null) return bestpar;
		if(hasFlexionConflict(bestpar.first, w, model)) return null;
		return bestpar;
	}
	
	public static void morphTagTest(WordSequences model, double minFreq,  int howmany, int contextcount) {
		MyPair bestpar;
		MyPair bestparSim;
		MyPair bestparSimCheck;

		Map<String,Set<String>> parmap= new HashMap<>();
		Map<String,Set<String>> simpleParmap= new HashMap<>();
		Map<String,Set<String>> simpleCHeckParmap= new HashMap<>();
		int count = 0;
		for(Word w: model.idx().getSortedWords()) {
			if(count > howmany) break;
			if(w.freq() > minFreq) continue;
			count++;

			String par = "zeroPar";
			String parsimple = "zeroParSimple";
			String parsimpleCheck = "zeroParSimpleCheck";
			bestpar = tagMorphPar(w, model, contextcount);
			bestparSim = tagMorphParSimple(w, model, contextcount);
			bestparSimCheck = tagMorphParSimpleWithCheck(w, model, contextcount);
			if(bestpar != null) par = MorphModel.getMPlabelFromFlexPar(bestpar.first);
			if(bestparSim != null) parsimple = MorphModel.getMPlabelFromFlexPar(bestparSim.first);
			if(bestparSimCheck != null) parsimpleCheck = MorphModel.getMPlabelFromFlexPar(bestparSimCheck.first);

			MapsOps.addStringToValueSet(par, parmap, w.toString());
			MapsOps.addStringToValueSet(parsimple, simpleParmap, w.toString());
			MapsOps.addStringToValueSet(parsimpleCheck, simpleCHeckParmap, w.toString());
			
			if(!par.equals(parsimple))
				System.out.println(w.toString()+"\tpar: " + par+ " parsimple: " +parsimple);
		}
		for(String p: parmap.keySet())
			System.out.println(p+"\t"+parmap.get(p).size()+"\t"+parmap.get(p));
		System.out.println("SIMPLE");
		for(String p: simpleParmap.keySet())
			System.out.println(p+"\t"+simpleParmap.get(p).size()+"\t"+simpleParmap.get(p));
		System.out.println("SIMPLECHECK");
			for(String p: simpleParmap.keySet())
				System.out.println(p+"\t"+simpleCHeckParmap.get(p).size()+"\t"+simpleCHeckParmap.get(p));
		
	}

	public static void tagWordsMparVectorLight(WordSequences model, double start , double howmany, int contextcount) {
//		model.idx().fillBuckets(2);
//		model.idx().saveMorphParsFreqs();
//		clenWaitingFlexes(model);
		// get cats vectors
//		collectMParVectorsFlexion(model, paradigmFilterREgex, print);
		//change it later!!!
//		model.collectKnownParVectors( Words.SYNSEM_FILTER);
	
		int i = 0;
		for(Word w: model.idx().getSortedWords()) {
			if(w.freq() > start) continue;
			if(i > howmany) break;
			if(!checkMorph(w,model)) continue;
			i++;
			//get word vectors, get score with cat vectors, get best cat
	        MyPair bestpar = getBestPar(getMParFromParVector(model, w, false, Words.SYNSEM_FILTER, false, contextcount));
			
			String newMplabel;
			if(bestpar == null) newMplabel = "null";
			else
				newMplabel = bestpar.first;
			String oldLabel;
			if(w.getMorphParadigm() == null) oldLabel = "null";
			else oldLabel = w.getMorphParadigm().getLabel();
			if(newMplabel.equals(oldLabel))
				System.out.println("SAME: "+w.toString()+"\t" + newMplabel);
			else
				System.out.println("DIFF: "+w.toString()+"\told: " +oldLabel+" new: "+newMplabel);
		}
	}


	
//	public static void removeFLexFromPar(String flex, String oldPar, MorphParadigm mpNew, WordSequences model, int thh) {
//		int i = 0;
//			
//		//set new paradigm where it applies
//		for(Word w: model.idx().getSortedWords()) {
//			i++;
//			if(w.getMorphParadigm() == null || w.getFlex() == null || w.syntLabel != null) continue;
//			String mparlabel =  w.getMorphParadigm().getLabel();
//			if(mparlabel.equals(oldPar) && w.getFlex().equals(flex)) {
//				w.setFlexRootMPar(flex, mpNew, model, true);
//			}
//		}
//		//delete all paradigm words from model
//		MorphVectorAnalyzer.cleanMorphTagsOld(model);
//		//add new paradigm words into model
//		MorphVectorAnalyzer.addMPasCategory(model, thh, false, false);
//	}
	
	
	//////////////////////////////////////////////// new tag

	public static Pair<String, Double> tagWordForKnownParadigms(Word inputword, WordSequences model, 
	    boolean print, double thh, int contextcount) {
		if(model.knownMparContextsMapLeft == null || model.knownMparContextsMapRight == null)
			model.collectKnownParVectors(  Words.SYN_FILTER, contextcount);
		Pair<String, Double> bestpar = computeBestKnownVectorParadigm(inputword,  Words.SYN_FILTER, model, print, thh, contextcount);
		return bestpar;
	}

	public static List<MyPair> computeBestKnownVectorParadigmS(Word w,  String regexForParadimFilter, 
	    WordSequences model, boolean print, double thh, int contextcount) {
	  
	  double leftRightDiffThh = thh; //Math.max(2.0, (thh/2.0));
	  
		List<String> bestContextsLeft = new ArrayList<>();
		for(MyPairWord key: w.getBestContextsComputeNew(true, contextcount, regexForParadimFilter, model, false)){
			bestContextsLeft.add(key.left.toString());
		}		
		List<String> bestContextsRight = new ArrayList<>();
		for(MyPairWord key: w.getBestContextsComputeNew(false, contextcount, regexForParadimFilter, model, false)){
			bestContextsRight.add(key.left.toString());
		}
//		if(w.toString().equals("він")) {
//		  System.out.println(bestContextsLeft); System.out.println(bestContextsRight);
//		}
		List<MyPair> tmpListForPrint = new ArrayList<>();

		for(String mparLable: model.idx().syntPars().keySet()) {
		  if(mparLable.startsWith(MorphModel.MPREF)) continue;
			double scoreL = Cluster.getScaledSimMeasure(bestContextsLeft, model.knownMparContextsMapLeft.get(mparLable)) ;
			double scoreR = Cluster.getScaledSimMeasure(bestContextsRight, model.knownMparContextsMapRight.get(mparLable))  ;
//			if(Math.abs(scoreL-scoreR) > leftRightDiffThh) 			  continue;
			double parScore = (scoreL + scoreR) /2;
			 scoreL = Cluster.getScaledSimMeasure(model.knownMparContextsMapLeft.get(mparLable), bestContextsLeft) ;
			 scoreR = Cluster.getScaledSimMeasure(model.knownMparContextsMapRight.get(mparLable), bestContextsRight) ;
			 if(Math.abs(scoreL-scoreR) > leftRightDiffThh) 
	              continue;
			 parScore = (parScore + ((scoreL + scoreR) /2)) / 2;
//			 if(print) System.out.println(parScore + "\t" + mparLable);
			 tmpListForPrint.add(new MyPair(mparLable, "", parScore)); 

			
		}
		Collections.sort(tmpListForPrint);
		if(print) {
			
			if(tmpListForPrint.size() > 5)
				tmpListForPrint = tmpListForPrint.subList(tmpListForPrint.size()-5, tmpListForPrint.size());
			for(MyPair p: tmpListForPrint)
				System.out.println(p.freq + "\t" + p.first+ "\t" + model.idx().getSyntParadigm(p.first).toStringInfo());
		}
		
		
		return tmpListForPrint;
	}
	
	   public static MyPair getBestPair(List<MyPair> scoreList, int contextcount) {
	     if(scoreList == null|| scoreList.size() == 0) return null;
	     MyPair mybestPar = scoreList.get(scoreList.size()-1);
	        if(mybestPar == null) return null;
	        if(mybestPar.freq > getScaledVectorSimilarityThh(contextcount)) return null;
	        return mybestPar;
	   }
	   
       public static MyPair getSecondBestPair(List<MyPair> scoreList, int contextcount, int moveIndexToLeft) {
         if(scoreList == null|| scoreList.size() < moveIndexToLeft) return null;
         MyPair mybestPar = scoreList.get(scoreList.size()-moveIndexToLeft);
            if(mybestPar == null) return null;
            if(mybestPar.freq > getScaledVectorSimilarityThh(contextcount)) return null;
            return mybestPar;
       }
       
	public static Pair<String, Double> computeBestKnownVectorParadigm(Word w,  String regexForParadimFilter, 
	    WordSequences model, boolean print, double thh, int contextcount) {
		
		List<MyPair> scoreList = computeBestKnownVectorParadigmS(w, regexForParadimFilter, model, print, thh, contextcount);
		if(scoreList == null|| scoreList.size() == 0) return null;
		MyPair mybestPar = scoreList.get(scoreList.size()-1);
		
		if(mybestPar == null) return null;
		if(mybestPar.freq > getScaledVectorSimilarityThh(contextcount)) return null;
		return new Pair<String, Double>(mybestPar.first, mybestPar.freq);
	}

	public static boolean confirmFlexion(String bestMP, Word w, WordSequences model) {
		MorphParadigm mp = null;
		mp = model.idx().getMorphParadigm(bestMP);
		if(mp == null) return false;
		for(Flexion f: mp.getFlexes()) {
			if(f.toString().equals("_")) return false;
			if(w.toString().endsWith(f.toString())) return true;
		}
		return false;
	}


}
