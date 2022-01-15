package model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import modelparts.Word;
import util.ListOps;
import util.MapsOps;

public class MorphModel {

	public static String FPREF = "f_";
	public static String MPREF = "m_";
	private int wordFreqTHHforModelTrain = 5;
	private int thhFreqForTrain = 0;
	


	
	

	
//	public void tagAmbigFlexion(WordSequences model, boolean checkVectors, String outfilepth, 
//	    boolean print, int round, int contextcount) throws IOException {
//		model.idx().fillBuckets(2);
//		model.idx().flexPar777.clear();
//		model.idx().flexPar777 = new HashMap<>();
//		Writer outBest = MyUtils.getWriter(outfilepth);
//		
//		Map<String, List<String>> mparContextsMapLeft = new HashMap<>();
//		Map<String, List<String>> mparContextsMapRight = new HashMap<>();
//		collectMParVectorsFlexion(mparContextsMapLeft, mparContextsMapRight, model, Words.SYNSEM_FILTER, contextcount);
//		
//		double count = 0.0;
//		double mparFound = 0.0;
//		double mpNotFound = 0.0;
//		double mpTheSame = 0.0;
//		double mpDiff = 0.0;
//		double mpNEW = 0.0;
//		for(Word w: model.idx().getSortedWords()) {
//			if(w.freq() < 10) break; //wordFreqTHHforModelTrain
//			if(w.toString().contains("_")) continue;
//			if(w.isSplitterLeftRight(model.getFreqOfAnd()))
//				continue;
//			if(w.toString().length() < 3) continue;
//			count++;
//			//check for uniq paradigm
//			Pair<String, Double> bestpar = getMParFromFlexVector(model, mparContextsMapLeft, mparContextsMapRight, w, contextcount);
//
//			Root root = null;
//			if(bestpar != null) root = getRoot(w, bestpar, model);
//			//statistics
//			if (bestpar != null && root != null) {
//				if( w.getMorphParadigm() == null) {
//					mparFound++;
//					mpNEW++;
//					if(print)	System.out.println("FOUND NEW:\t" + bestpar.getKey().toString()+" "+MyUtils.rdouble(bestpar.getValue()) +"\t" + w.toString() );
//				}
//				else {
//					if(bestpar.getKey().contains(w.getMorphParadigm().getLabel())) {
//						mpTheSame++;
//						mparFound++;
//					}else {
//						mparFound++;
//						mpDiff++;
//						if(print)	System.out.println(round +". TAG diff:\t" + bestpar.getKey().toString()+" "+MyUtils.rdouble(bestpar.getValue()) + "\t"+ w.toString() + "\tit was: " +w.getMorphParadigm().getLabel() );
//						
//					}
//				}
//			}else {
////				if(print && w.getMorphParadigm() == null)	System.out.println(round +". NOT found:\t" +  w.toString()  );
//				if(w.getMorphParadigm() == null) mpNotFound++;
//				else mparFound++;
//			}
//			// end stats
//			
//			//write output and paradigm into word
//			if (bestpar != null && root != null) {
//				String parLabel = bestpar.getKey().substring(bestpar.getKey().length()-3);
//				
//				boolean wasChanged = writeParadigmIntoWord(root, model.idx().getMorphParadigm(parLabel), model);
//				if(wasChanged) 		writeOutput(root, model.idx().getMorphParadigm(parLabel), outBest);
//			}
//		}
//		model.idx().emptyBuckets();
//		outBest.close();
//		
//		System.out.println("per cent of tagged: " + (mparFound / count));
//		System.out.println("per cent of NOT found: " + (mpNotFound / count));
//		System.out.println("found the same: " + mpTheSame) ;
//		System.out.println("found diff: " + mpDiff) ;
//		System.out.println("found new: " + mpNEW);
//		System.out.println("found all: " + mparFound);
//		System.out.println("found NOT: " + mpNotFound);
//
//		System.out.println("count words: " + count);
//		
//		for(String mpstring: model.idx().getMPlabels()) {
//			MorphParadigm mp = model.idx().getMorphParadigm(mpstring);
//			if(mp == null) continue;
//			System.out.println(mp.getLabel() + "\t" + mp.getSortedFlexFreqMap() + "\twating\t" + mp.getSortedWaitingFlexMap());
//		}
//	}

//	public static Pair<String, Double> getMParFromFlexVector(WordSequences model,
//			Map<String, List<String>> mparContextsMapLeft, Map<String, List<String>> mparContextsMapRight, 
//			Word w, int contextcount) {
//		if(w.isSplitterLeftRight(model.getFreqOfAnd()))
//			return null;
//		if(w.toString().length() < 3) return null;
//		Pair<String, Double> bestpar = MorphParTrain.getBestParadigmAmbig2Vector(
//				w, mparContextsMapRight, mparContextsMapLeft, Words.SYNSEM_FILTER, model, false, false, contextcount);
//		bestpar = MorphParTrain.checkConflicts(bestpar, w, model, mparContextsMapRight, mparContextsMapLeft, contextcount);
//		return bestpar;
//	}
	
	

//	public void tagAmbig(WordSequences model, boolean checkVectors, String outfilepth) throws IOException {
//		
//		Writer outBest = MyUtils.getWriter(outfilepth);
//		Set<String> seenRoots = new HashSet<String>();
//		
//		Map<String, List<String>> mparContextsMapLeft = new HashMap<>();
//		Map<String, List<String>> mparContextsMapRight = new HashMap<>();
//		collectMParVectors(mparContextsMapLeft, mparContextsMapRight, model, Words.ALLPARS_FILTER);
//		System.out.println("LEFT MP vecotrs filter all");
//		System.out.println(mparContextsMapLeft.toString());
//		Map<String, List<String>> mparContextsMapLeft2 = new HashMap<>();
//		Map<String, List<String>> mparContextsMapRight2 = new HashMap<>();
//		collectMParVectors(mparContextsMapLeft2, mparContextsMapRight2, model, Words.SYNSEM_FILTER);
//		System.out.println("LEFT MP vecotrs");
//		System.out.println(mparContextsMapLeft2.toString());
//		System.out.println("RIGHT MP vecotrs");
//		System.out.println(mparContextsMapRight2.toString());
//		double count = 0.0;
//		double mparFound = 0.0;
//		double hadRoot = 0.0;
//		
//		for(Word w: model.idx().getSortedWords()) {
//			if(w.freq() < 2) break; //wordFreqTHHforModelTrain
//			count++;
//			if(w.morphLabel != null) mparFound++;
//			if(w.isSplitterLeftRight(model.getFreqOfAnd()))
//				continue;
//			if(w.getFlex() == null || w.getRoot() == null)  { // skip words already tagged or without flex
//				continue;
//			}
//			if( w.morphLabel != null) {
//				continue;
//			}
//			if(w.toString().length() < 3) continue;
//			hadRoot++;
//			if(seenRoots.contains(w.getRoot().toString())) continue;
//			Root root = model.idx().getRoot(w.getRoot());
//			if(root.seenflexes == null || root.seenflexes.size() == 0) continue;
//			seenRoots.add(root.toString());
//			
//			//check for uniq paradigm
//			if(!checkVectors) {
//				MorphParadigm bestUniqPar = MorphParTrain.getBestParadigmAmbig(w, mparContextsMapRight, mparContextsMapLeft,model);
//				if(bestUniqPar != null && !checkVectors) {
//					mparFound = mparFound + root.seenflexes.size();
//					writeParadigmIntoWord(root, bestUniqPar, model);
//					writeOutput(root, bestUniqPar, outBest);
//				}
//			}
//			if (checkVectors) {
//				Pair<MorphParadigm, Double> bestAmbigPair = MorphParTrain.getBestParadigmAmbig2(w,
//						mparContextsMapRight2, mparContextsMapLeft2, Words.SYNSEM_FILTER, model, false);
//				// if(bestAmbigPair == null && bestUniqPar != null) System.out.println("uniq
//				// only:\t" + w.toString() + "\t" + bestUniqPar.toString());
//				// else if(bestUniqPar == null && bestAmbigPair != null)
//				// System.out.println("ambig only:\t" + w.toString() + "\t" +
//				// bestAmbigPair.toString());
//				// else if(bestUniqPar == null && bestAmbigPair == null)
//				// System.out.println("both NULL:\t" + w.toString() );
//				// else if(!bestAmbigPair.getKey().toString().equals(bestUniqPar.toString())) {
//				// System.out.println("DIFF PAIRS:\t" + w.toString() + "\t" +
//				// bestUniqPar.toString() + "\t" + bestAmbigPair.toString());
//				//// bestAmbigPair = MorphParTrain.getBestParadigmAmbig2(w,
//				// mparContextsMapRight2, mparContextsMapLeft2, Words.SYNSEM_FILTER, model);
//				// }
//				// if(w.getMorphParadigm() != null && bestAmbigPair != null &&
//				// !w.getMorphParadigm().toString().equals(bestAmbigPair.getKey().toString()))
//				// System.out.println("1. TAG DIFF PAIRS:\t" + w.toString() + "\t" +
//				// w.getMorphParadigm().toString() + "\t" + bestAmbigPair.toString());
//				if (bestAmbigPair != null) {
//					mparFound = mparFound + root.seenflexes.size();
//					writeParadigmIntoWord(root, bestAmbigPair.getKey(), model);
//					writeOutput(root, bestAmbigPair.getKey(), outBest);
//				}
//			}
//		}
////		addParadigmWordStats..(model);
////		addParadigmWordStats(model);
//		outBest.close();
//		
//		System.out.println("after ambig seen: " + count + "\ttagged\t" + mparFound + "\thadRoot\t" + hadRoot);
//		System.out.println("per cent of tagged: " + (mparFound / count));
//		System.out.println("per cent of tagged of hadRoot: " + (mparFound / hadRoot));
//		
//	}
	


	public void printMorphStats(WordSequences wsmodel) {
		Map<String,Double> paradigmFreq = new HashMap<>();
		Map<String,Double> paradigmFreqOnRoot = new HashMap<>();
		Map<String,String> labelToParadigm = new HashMap<>();
		Set<String> seenRoots = new HashSet<>();
		double seenWords = 0.0;
		double hasLabel = 0.0;
		for(Word w: wsmodel.idx().getSortedWords()) {
			seenWords++;
			if(w.getMorphParadigm() != null) {
				hasLabel++; 
				labelToParadigm.put(w.getMorphParadigm().getLabel(), w.getMorphParadigm().toString());
				MapsOps.addFreq(w.getMorphParadigm().getLabel(), paradigmFreq);
				if(Character.isUpperCase(w.toString().charAt(0))) {
					MapsOps.addFreq(w.getMorphParadigm().getLabel()+"_UPPER", paradigmFreq);
				}else {
					MapsOps.addFreq(w.getMorphParadigm().getLabel()+"_lower", paradigmFreq);
				}
				
				String root = w.getRoot();
				if(seenRoots.contains(root)) continue;
				seenRoots.add(root);
				MapsOps.addFreq(w.getMorphParadigm().getLabel(), paradigmFreqOnRoot);
				if(Character.isUpperCase(w.toString().charAt(0))) {
					MapsOps.addFreq(w.getMorphParadigm().getLabel()+"_UPPER", paradigmFreqOnRoot);
				}else {
					MapsOps.addFreq(w.getMorphParadigm().getLabel()+"_lower", paradigmFreqOnRoot);
				}
			}
		}
		List<String> parlabels = ListOps.of(paradigmFreq.keySet());
		Collections.sort(parlabels);
		System.out.println();
		for(String label: parlabels)
			System.out.println(label + "\tWORDstats\t" + paradigmFreq.get(label) + "\t" + labelToParadigm.get(label)
			+ "\tROOTstats\t" + paradigmFreqOnRoot.get(label) + "\t" + labelToParadigm.get(label));
		System.out.println("seen words\t " + seenWords + "\twith morph\t" + hasLabel + "\tpercent\t" + (hasLabel/seenWords));
	}

	public static String getFlexLabel(String flex, String morphParLabel, String pref) {
		return pref+flex+"_"+morphParLabel;
	}
	public static String getFlexFromFlexPar(String flexpar, String pref) {
		return flexpar.replaceAll("^"+pref, "").replaceAll("_"+MPREF+".+", ""); //important f___m_0
	}	
	
	public static String getRealFlexFromFlexPar(String flexpar, String pref) {
		 String f = flexpar.replaceAll("^"+pref, "").replaceAll("_"+MPREF+".+", ""); //important f___m_0
		 if(f.equals("_")) f = "";
		 return f;
	}
	public static String getMPlabelFromFlexPar(String flexpar) {
		String[]sarr = flexpar.split("_"+MPREF);
		if(sarr.length < 2) return flexpar;
		return MPREF +   sarr[1];
	}



}
