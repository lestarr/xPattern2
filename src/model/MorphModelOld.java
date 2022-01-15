//package model;
//
//import java.io.IOException;
//import java.io.Writer;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.SortedMap;
//
//import javafx.util.Pair;
//import modelparts.Flexion;
//import modelparts.MorphParadigm;
//import modelparts.MorphParadigms;
//import modelparts.MorphSemParadigms;
//import modelparts.Paradigm;
//import modelparts.Root;
//import modelparts.Word;
//import modeltrain.MorphParTrain;
//import processText.mainModels.BuildWordSequenceModelDE;
//import processText.mainModels.BuildWordSequenceModelUKR;
//import util.CorpusUtils;
//import util.ListOps;
//import util.MapsOps;
//import util.MyPairWord;
//import util.MyUtils;
//
//public class MorphModel extends Paradigm {
//
//	public static final String FPREF = "f_";
//	public static final String MPREF = "m_";
//	private int wordFreqTHHforModelTrain = 5;
//	private int thhFreqForTrain = 0;
//	
//	private LetterTokModel ltmodel = null;
//	
//	public void setLetterTokModel(LetterTokModel ltmodel) {
//		this.ltmodel = ltmodel;
//	}
//	
//	public void setThhFreqForTrain(int thh) {
//		this.thhFreqForTrain = thh;	
//	}
//	
//	
//	public MorphModel(int id, String label) {
//		super(id, label);
//		features = new HashSet<>();
//		members = new HashSet<>();
//	}
//
//	
//	public void train(WordSequences wsmodel) {
//		try {
//			MorphParTrain.getRootFlex(wsmodel, ltmodel, wordFreqTHHforModelTrain);
//			System.out.println("FIRST TAG, SIZE before load\t" + wsmodel.idx().getMPlabels().size());
///*			MorphParTrain.retrainCLustersOnTaggedWords(wsmodel, 100, 10); //1
//			MorphParadigms.combineBestParadigms(wsmodel, true); // do it after first tag
////			MorphParTrain.cleanParadigmsNonFrequentMembers(wsmodel, 10, 10);
////			MorphParadigms.outputMorphPars(wsmodel);
//			
//			System.out.println("SECOND TAG, SIZE before load\t" + wsmodel.idx().morphPars().size());
//			MorphParTrain.retrainCLustersOnTaggedWords(wsmodel, 100, 10); //1
//			MorphParadigms.combineBestParadigms(wsmodel, true);
////			MorphParTrain.cleanParadigmsNonFrequentMembers(wsmodel, 10, 10);
//
//*/
//			//train MorphSemPar
//			MorphSemParadigms.trainParadigms(wsmodel, thhFreqForTrain);
//			for(String l: wsmodel.idx().getMPlabels()) {
//				MorphParadigm mpar = wsmodel.idx().getMorphParadigm(l);
//				System.out.println((int)mpar.getFreq() + "\t" + mpar.getSortedFlex() + "\t" + mpar.getSortedFlexFreqMap()+  "\t" + mpar.getTailFlexesAsString());
//			}
//			
//			MorphParadigms.outputMorphPars(wsmodel);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	
//
//	@Override
//	public void tag(WordSequences wsmodel) throws IOException {
//		Writer out = MyUtils.getWriter("out/"+ wsmodel.getLang()+"-taggedMorph-tagForSeenFlexes.txt");
//		Writer outBest = MyUtils.getWriter("out/"+ wsmodel.getLang()+"-taggedMorph-tagForSeenFlexes-BEST.txt");
//		Set<String> seenRoots = new HashSet<String>();
//		
//		System.out.println("mpar after load: ");
//		for(String l: wsmodel.idx().getMPlabels()) {
//			MorphParadigm mpar = wsmodel.idx().getMorphParadigm(l);
//			System.out.println(mpar.toString());
//		}
//		System.out.println("mpars SIZE:\t" + wsmodel.idx().getMPlabels().size());
//		
//		double count = 0.0;
//		double mparFound = 0.0;
//		double hadRoot = 0.0;
//		for(Word w: wsmodel.idx().getSortedWords()) {
//			if(w.freq() < 2) break; //wordFreqTHHforModelTrain
//			count ++;
//			if(w.isSplitterLeftRight(wsmodel.getFreqOfAnd())
//					|| 	w.toString().matches("und|and|en|і|й") 
//			) {
//				w.setFlex(null);
//				continue;
//			}
//			if(w.getFlex() == null || w.getRoot() == null) {
////				System.out.println("NO ROOT\t" + w.toString());
//				continue;
//			}
//			hadRoot ++;
//			if(seenRoots.contains(w.getRoot().toString())) continue;
//			Root root = wsmodel.idx().getRoot(w.getRoot());
//			if(root.seenflexes == null || root.seenflexes.size() == 0) continue;
//			seenRoots.add(root.toString());
////			MorphParadigm bestParadigm = MorphParTrain.getBestParadigm(root.seenflexes, mpars, w.getRoot().toString(), true);
////			if(bestParadigm != null) {
////				mparFound = mparFound + root.seenflexes.size();
////				if(isSplitterLeftRight(w, bestParadigm)) continue;
////				writeParadigmIntoWord(root, bestParadigm);
////				writeOutput(root, bestParadigm, out);
////			}	else {
////				out.write("m000\t" + w.toString() + "\t" + root.toString() + "\t" + root.getFlexes().toString() + "\n");
////			}
//			
//			//check for uniq paradigm
//			MorphParadigm bestUniqPar = MorphParTrain.getBestParadigmUniq(root.seenflexes, wsmodel, w);
//			if(bestUniqPar != null) {
//				mparFound = mparFound + root.seenflexes.size();
//				writeParadigmIntoWord(root, bestUniqPar, wsmodel);
//				writeOutput(root, bestUniqPar, outBest);
//			}
//		}
////		addParadigmWordStats(wsmodel);
//		out.close();
//		outBest.close();
//		System.out.println("seen: " + count + "\ttagged\t" + mparFound + "\thadRoot\t" + hadRoot);
//		System.out.println("per cent of tagged: " + (mparFound / count));
//		System.out.println("per cent of tagged of hadRoot: " + (mparFound / hadRoot));
//	}
//	public static Pair<MorphParadigm, Double> tagAmbigWord(Word w, WordSequences model, int contextcount) throws IOException {
//		return tagAmbigWord(w, model, false, contextcount);
//	}
//	public static Pair<MorphParadigm, Double> tagAmbigWord(Word w, WordSequences model, boolean print, int contextcount) throws IOException {
//		model.collectKnownParVectors(Words.SYNSEM_FILTER, contextcount);
//		System.out.println();
//		Pair<MorphParadigm, Double> bestpar = MorphParTrain.getBestParadigmAmbig2(w, model.knownMparContextsMapRight, model.knownMparContextsMapLeft, Words.SYNSEM_FILTER, model, print, contextcount);
//		return bestpar;
//	}
//
//	
////	public void tagAmbigFlexion(WordSequences model, boolean checkVectors, String outfilepth, 
////	    boolean print, int round, int contextcount) throws IOException {
////		model.idx().fillBuckets(2);
////		model.idx().flexPar777.clear();
////		model.idx().flexPar777 = new HashMap<>();
////		Writer outBest = MyUtils.getWriter(outfilepth);
////		
////		Map<String, List<String>> mparContextsMapLeft = new HashMap<>();
////		Map<String, List<String>> mparContextsMapRight = new HashMap<>();
////		collectMParVectorsFlexion(mparContextsMapLeft, mparContextsMapRight, model, Words.SYNSEM_FILTER, contextcount);
////		
////		double count = 0.0;
////		double mparFound = 0.0;
////		double mpNotFound = 0.0;
////		double mpTheSame = 0.0;
////		double mpDiff = 0.0;
////		double mpNEW = 0.0;
////		for(Word w: model.idx().getSortedWords()) {
////			if(w.freq() < 10) break; //wordFreqTHHforModelTrain
////			if(w.toString().contains("_")) continue;
////			if(w.isSplitterLeftRight(model.getFreqOfAnd()))
////				continue;
////			if(w.toString().length() < 3) continue;
////			count++;
////			//check for uniq paradigm
////			Pair<String, Double> bestpar = getMParFromFlexVector(model, mparContextsMapLeft, mparContextsMapRight, w, contextcount);
////
////			Root root = null;
////			if(bestpar != null) root = getRoot(w, bestpar, model);
////			//statistics
////			if (bestpar != null && root != null) {
////				if( w.getMorphParadigm() == null) {
////					mparFound++;
////					mpNEW++;
////					if(print)	System.out.println("FOUND NEW:\t" + bestpar.getKey().toString()+" "+MyUtils.rdouble(bestpar.getValue()) +"\t" + w.toString() );
////				}
////				else {
////					if(bestpar.getKey().contains(w.getMorphParadigm().getLabel())) {
////						mpTheSame++;
////						mparFound++;
////					}else {
////						mparFound++;
////						mpDiff++;
////						if(print)	System.out.println(round +". TAG diff:\t" + bestpar.getKey().toString()+" "+MyUtils.rdouble(bestpar.getValue()) + "\t"+ w.toString() + "\tit was: " +w.getMorphParadigm().getLabel() );
////						
////					}
////				}
////			}else {
//////				if(print && w.getMorphParadigm() == null)	System.out.println(round +". NOT found:\t" +  w.toString()  );
////				if(w.getMorphParadigm() == null) mpNotFound++;
////				else mparFound++;
////			}
////			// end stats
////			
////			//write output and paradigm into word
////			if (bestpar != null && root != null) {
////				String parLabel = bestpar.getKey().substring(bestpar.getKey().length()-3);
////				
////				boolean wasChanged = writeParadigmIntoWord(root, model.idx().getMorphParadigm(parLabel), model);
////				if(wasChanged) 		writeOutput(root, model.idx().getMorphParadigm(parLabel), outBest);
////			}
////		}
////		model.idx().emptyBuckets();
////		outBest.close();
////		
////		System.out.println("per cent of tagged: " + (mparFound / count));
////		System.out.println("per cent of NOT found: " + (mpNotFound / count));
////		System.out.println("found the same: " + mpTheSame) ;
////		System.out.println("found diff: " + mpDiff) ;
////		System.out.println("found new: " + mpNEW);
////		System.out.println("found all: " + mparFound);
////		System.out.println("found NOT: " + mpNotFound);
////
////		System.out.println("count words: " + count);
////		
////		for(String mpstring: model.idx().getMPlabels()) {
////			MorphParadigm mp = model.idx().getMorphParadigm(mpstring);
////			if(mp == null) continue;
////			System.out.println(mp.getLabel() + "\t" + mp.getSortedFlexFreqMap() + "\twating\t" + mp.getSortedWaitingFlexMap());
////		}
////	}
//
////	public static Pair<String, Double> getMParFromFlexVector(WordSequences model,
////			Map<String, List<String>> mparContextsMapLeft, Map<String, List<String>> mparContextsMapRight, 
////			Word w, int contextcount) {
////		if(w.isSplitterLeftRight(model.getFreqOfAnd()))
////			return null;
////		if(w.toString().length() < 3) return null;
////		Pair<String, Double> bestpar = MorphParTrain.getBestParadigmAmbig2Vector(
////				w, mparContextsMapRight, mparContextsMapLeft, Words.SYNSEM_FILTER, model, false, false, contextcount);
////		bestpar = MorphParTrain.checkConflicts(bestpar, w, model, mparContextsMapRight, mparContextsMapLeft, contextcount);
////		return bestpar;
////	}
//	
//	private Root getRoot(Word w, Pair<String, Double> bestpar, WordSequences model) {
//		Root root = null;
//		if(bestpar == null) return null;
//		String flex = getFlexFromFlexPar(bestpar.getKey(),FPREF); 
//		if(flex.equals("_")) 
//			root =  model.idx().getRoot(w.toString());
//		else if(w.toString().endsWith(flex)) {
//			String r = w.toString().substring(0, (w.toString().length()-flex.length()));
//			root = model.idx().getRoot(r);
//		}
//		else if(w.getRoot() != null) 
//			root = model.idx().getRoot(w.getRoot());
//		if(root != null) {
//			root.seenWords.add(w);
//			root.seenflexes.add(model.idx().getFlex(flex));
//			w.setRoot(root.toString());
//			w.setFlex(flex);
//		}
//		return root;
//	}
//
////	public void tagAmbig(WordSequences model, boolean checkVectors, String outfilepth) throws IOException {
////		
////		Writer outBest = MyUtils.getWriter(outfilepth);
////		Set<String> seenRoots = new HashSet<String>();
////		
////		Map<String, List<String>> mparContextsMapLeft = new HashMap<>();
////		Map<String, List<String>> mparContextsMapRight = new HashMap<>();
////		collectMParVectors(mparContextsMapLeft, mparContextsMapRight, model, Words.ALLPARS_FILTER);
////		System.out.println("LEFT MP vecotrs filter all");
////		System.out.println(mparContextsMapLeft.toString());
////		Map<String, List<String>> mparContextsMapLeft2 = new HashMap<>();
////		Map<String, List<String>> mparContextsMapRight2 = new HashMap<>();
////		collectMParVectors(mparContextsMapLeft2, mparContextsMapRight2, model, Words.SYNSEM_FILTER);
////		System.out.println("LEFT MP vecotrs");
////		System.out.println(mparContextsMapLeft2.toString());
////		System.out.println("RIGHT MP vecotrs");
////		System.out.println(mparContextsMapRight2.toString());
////		double count = 0.0;
////		double mparFound = 0.0;
////		double hadRoot = 0.0;
////		
////		for(Word w: model.idx().getSortedWords()) {
////			if(w.freq() < 2) break; //wordFreqTHHforModelTrain
////			count++;
////			if(w.morphLabel != null) mparFound++;
////			if(w.isSplitterLeftRight(model.getFreqOfAnd()))
////				continue;
////			if(w.getFlex() == null || w.getRoot() == null)  { // skip words already tagged or without flex
////				continue;
////			}
////			if( w.morphLabel != null) {
////				continue;
////			}
////			if(w.toString().length() < 3) continue;
////			hadRoot++;
////			if(seenRoots.contains(w.getRoot().toString())) continue;
////			Root root = model.idx().getRoot(w.getRoot());
////			if(root.seenflexes == null || root.seenflexes.size() == 0) continue;
////			seenRoots.add(root.toString());
////			
////			//check for uniq paradigm
////			if(!checkVectors) {
////				MorphParadigm bestUniqPar = MorphParTrain.getBestParadigmAmbig(w, mparContextsMapRight, mparContextsMapLeft,model);
////				if(bestUniqPar != null && !checkVectors) {
////					mparFound = mparFound + root.seenflexes.size();
////					writeParadigmIntoWord(root, bestUniqPar, model);
////					writeOutput(root, bestUniqPar, outBest);
////				}
////			}
////			if (checkVectors) {
////				Pair<MorphParadigm, Double> bestAmbigPair = MorphParTrain.getBestParadigmAmbig2(w,
////						mparContextsMapRight2, mparContextsMapLeft2, Words.SYNSEM_FILTER, model, false);
////				// if(bestAmbigPair == null && bestUniqPar != null) System.out.println("uniq
////				// only:\t" + w.toString() + "\t" + bestUniqPar.toString());
////				// else if(bestUniqPar == null && bestAmbigPair != null)
////				// System.out.println("ambig only:\t" + w.toString() + "\t" +
////				// bestAmbigPair.toString());
////				// else if(bestUniqPar == null && bestAmbigPair == null)
////				// System.out.println("both NULL:\t" + w.toString() );
////				// else if(!bestAmbigPair.getKey().toString().equals(bestUniqPar.toString())) {
////				// System.out.println("DIFF PAIRS:\t" + w.toString() + "\t" +
////				// bestUniqPar.toString() + "\t" + bestAmbigPair.toString());
////				//// bestAmbigPair = MorphParTrain.getBestParadigmAmbig2(w,
////				// mparContextsMapRight2, mparContextsMapLeft2, Words.SYNSEM_FILTER, model);
////				// }
////				// if(w.getMorphParadigm() != null && bestAmbigPair != null &&
////				// !w.getMorphParadigm().toString().equals(bestAmbigPair.getKey().toString()))
////				// System.out.println("1. TAG DIFF PAIRS:\t" + w.toString() + "\t" +
////				// w.getMorphParadigm().toString() + "\t" + bestAmbigPair.toString());
////				if (bestAmbigPair != null) {
////					mparFound = mparFound + root.seenflexes.size();
////					writeParadigmIntoWord(root, bestAmbigPair.getKey(), model);
////					writeOutput(root, bestAmbigPair.getKey(), outBest);
////				}
////			}
////		}
//////		addParadigmWordStats..(model);
//////		addParadigmWordStats(model);
////		outBest.close();
////		
////		System.out.println("after ambig seen: " + count + "\ttagged\t" + mparFound + "\thadRoot\t" + hadRoot);
////		System.out.println("per cent of tagged: " + (mparFound / count));
////		System.out.println("per cent of tagged of hadRoot: " + (mparFound / hadRoot));
////		
////	}
//	
//
//
//	/**
//	 * returns true if paradigm was changed: form NULL to sth or m_1 to m_2 etc.
//	 * writes paradigm labels into words, relation: Word -> MorphParadigm
//	 * if needed deltes old paradigm
//	 */
//	public boolean writeParadigmIntoWord(Root root, MorphParadigm bestParadigm, WordSequences wsmodel) {
//		boolean wasChanged = false;
//		for(Flexion flex: bestParadigm.getFlexes()) {
//			String flexstring = flex.toString();
//			if(flexstring.equals("_")) flexstring = "";
//			Word wFromRoot = wsmodel.getWord(root.toString()+flexstring);
//			if(wFromRoot.freq() < 2) continue;
//			if(wFromRoot.isSplitterLeftRight(wsmodel.getFreqOfAnd()))
//				continue;
//			String flexMPlabelOld = null;
//			String mpLabelOld = null;
//			if(wFromRoot.getMorphParadigm() != null  ) {
//				mpLabelOld  = wFromRoot.getMorphParadigm().getLabel();
//				flexMPlabelOld = getFlexLabel(wFromRoot.getFlex(), mpLabelOld, FPREF);
//			}
//			
//			wFromRoot.setRoot(root.toString());
//			root.seenflexes.add(flex);
//			root.seenWords.add(wFromRoot);
//			wFromRoot.setFlex(flex.toString());
//			wFromRoot.changeMorphParadigm(bestParadigm);
//
//			String flexMPlabel = wFromRoot.getFlexLabel();
//			
//			if( mpLabelOld == null || !mpLabelOld.equals(bestParadigm.getLabel())){ //add new mp if there was nothing(dummy) or there was sth else
//				wsmodel.addCategory(wFromRoot.getMorphParadigm().getLabel(), wFromRoot);
//				wsmodel.addCategory(wFromRoot.getFlexLabel(), wFromRoot);
//				wasChanged = true;
//				if(mpLabelOld != null) { //delete old if it was anders AND not null
//					wFromRoot.setMorphParadigmAmbig(flexMPlabelOld);
//					wsmodel.deleteCategoryInWord(flexMPlabelOld, wFromRoot);
//					wsmodel.deleteCategoryInWord(mpLabelOld, wFromRoot);
//				}
//				}
//						
//			//just for info
//			wsmodel.getWord(wFromRoot.getMorphParadigm().getLabel()).paradigmWords.add(wsmodel.getWord(bestParadigm.getSortedFlex()));
//		}
//		return wasChanged;
//	}
//	
//	/**
//	 * writes paradigm labels into words, relation: Word -> MorphParadigm
//	 */
//	public void writeParadigmIntoWordOld(Root root, MorphParadigm bestParadigm, WordSequences wsmodel) {
//		for(Word wFromRoot: root.seenWords) {
//			if(wFromRoot.isSplitterLeftRight(wsmodel.getFreqOfAnd()))
//				continue;
//			if(wFromRoot.getMorphParadigm() != null) continue;
//			
//			Flexion flex = wsmodel.idx().getFlex(wFromRoot.getFlex());
//	
//			if(!bestParadigm.getFlexes().contains(flex))
//				continue;
//			wFromRoot.changeMorphParadigm(bestParadigm);
//			wsmodel.addCategory(wFromRoot.getMorphParadigm().getLabel(), wFromRoot);
//			wsmodel.addCategory(wFromRoot.getFlexLabel(), wFromRoot);
//			//just for info
//			wsmodel.getWord(wFromRoot.getMorphParadigm().getLabel()).paradigmWords.add(wsmodel.getWord(bestParadigm.getSortedFlex()));
//		}		
//	}
//	
//	/**
//	 * deletes paradigm labels from words, deletes relation: Word -> MorphParadigm
//	 */
//	public void deleteParadigmIntoWord(Word w, MorphParadigm paradigmToDelete, WordSequences wsmodel) {
// 			w.changeMorphParadigm(null);
//			wsmodel.deleteCategoryInWord(paradigmToDelete.getLabel(), w);
//			wsmodel.deleteCategoryInWord(w.getFlexLabel(), w);
//	}
//
//	private static void writeOutput(Root root, MorphParadigm bestParadigm, Writer out) throws IOException {
//		if(out == null) {
//			System.out.println(
//					bestParadigm.getLabel() + "\t" + root.toString()  + "\t" + bestParadigm.getFlexes() 
//		+ "\t" + MyUtils.rdouble(bestParadigm.score) + "\t" + root.seenWords.toString() + "\t");
//			for(Word word: root.getWords()) {
//				System.out.println(word.toString() + " " + MyUtils.rdouble(word.getCoefOneDirection(true)) + ", ");
//			}
//		}else {
//		out.write(bestParadigm.getLabel() + "\t" + root.toString()  + "\t" + bestParadigm.getFlexes() 
//		+ "\t" + MyUtils.rdouble(bestParadigm.score) + "\t" + root.seenWords.toString() + "\t");
//		out.write(bestParadigm.getLabel() + "\t" + MyUtils.rdouble(bestParadigm.score)+ "\t" );
//		for(Word word: root.getWords()) {
//			out.write(word.toString() + " " + MyUtils.rdouble(word.getCoefOneDirection(true)) + ", ");
//		}
//		out.write("\n");
//		}
//	}
//
//
//	/**
//	 * adds paradigm labels as Word itself into the model, fills left and rights, both: words and paradigms
//	 */
//	private void addParadigmWordStatsOld( WordSequences wsmodel) {
//		for(Word w: wsmodel.idx().getSortedWords()) {
//			String morphParLabel = w.getMorphLabelNotNull();
//			String wflex = w.getFlexNotNull();
//			if(morphParLabel.contains("Zero") && wflex.contains("Zero")) continue;
//			if(!morphParLabel.endsWith("_tmp")) continue;
//			morphParLabel = morphParLabel.replaceFirst("_tmp$", "");
//			Word morphWord = wsmodel.getWord(morphParLabel);
//			morphWord.addFreq(w.freq());
//			morphWord.changeMorphParadigm(w.getMorphParadigm());
//			
//			Word flexWord = wsmodel.getWord("f_"+wflex);
//			flexWord.addFreq(w.freq());
//			flexWord.changeMorphParadigm(w.getMorphParadigm());
//			
//			writeBigramsOld(wsmodel, w, morphWord, flexWord);
//			
//		}		
//	}
//
//	private void writeBigramsOld(WordSequences wsmodel, Word w_left, Word mp_left, Word flex_left) {
//		Set<Word> rightNeighbours =new HashSet<>( w_left.left_of.keySet());
//		for(Word w_right: rightNeighbours) {
//			Word mp_right = wsmodel.getWord(w_right.getMorphLabelNotNull());
//			Word flex_right = wsmodel.getWord("f_"+w_right.getFlexNotNull());
//			double freq = w_left.left_of.get(w_right);
//		//word: w_left mp_right
//			WordSequences.processBigram(w_left, mp_right, freq);
//		//word: w_left flex_right
//			WordSequences.processBigram(w_left, flex_right, freq);
//
//		//morph par: mp_left w_right
//			WordSequences.processBigram(mp_left, w_right, freq);
//		//morph par: mp_left flex_right
//			WordSequences.processBigram(mp_left, flex_right, freq);
//		//morph par: mp_left mp_right
//			WordSequences.processBigram(mp_left, mp_right, freq);
//
//		//flex: flex_left mp_right
//			WordSequences.processBigram(flex_left, mp_right, freq);
//		//flex: flex_left w_right
//			WordSequences.processBigram(flex_left, w_right, freq);
//		//flex: flex_left flex_right
//			WordSequences.processBigram(flex_left, flex_right, freq);
//	
//		}
//		
////		//all right neighbors of this word are also neighbors of this paradigm, as are their paradigms: gesundes (m13) Essen (m1): m13 is left of m1 AND Essen
////		for(Word rightNeighbor: w.left_of.keySet()) {
////			WordSequences.processBigram(morphWord, rightNeighbor, w.left_of.get(rightNeighbor));
////			if(rightNeighbor.morphLabel != null)
////				WordSequences.processBigram(morphWord, wsmodel.getWord(rightNeighbor.morphLabel), w.left_of.get(rightNeighbor));
////		}
////		//same for right
////		for(Word lefttNeighbor: w.right_of.keySet()) {
////			WordSequences.processBigram(lefttNeighbor, morphWord, w.right_of.get(lefttNeighbor));
////			if(lefttNeighbor.morphLabel != null)
////				WordSequences.processBigram(lefttNeighbor, wsmodel.getWord(lefttNeighbor.morphLabel), w.right_of.get(lefttNeighbor));
////		}
//	}
//
//
//	public void loadModel(WordSequences wsmodel, String path, LetterTokModel ltmodel) {
//		try {
//		List<MorphParadigm> mpars = new ArrayList<MorphParadigm>();
//		List<String> lines = MyUtils.readLines(path);
//		for(String line: lines) {
//			line = line.replaceFirst("^\\{", "").replaceFirst("\\}$", "");
//			String[] sarr = line.split(", ");
//			Map<String,Double> flexFreqMap = new HashMap<String, Double>();
//			Set<Flexion> flexionSet = new HashSet<Flexion>();
//			for(String s: sarr) {
//				if(s.length() == 0) continue;
//				String[] flexFreqArr = s.split("=");
//				String flex = flexFreqArr[0];
//				double freq = Double.parseDouble(flexFreqArr[1]);
//				flexFreqMap.put(flex, freq);
//				
//				Flexion flexion = wsmodel.idx().getFlex(flex);
//				flexionSet.add(flexion);
//			}
//			mpars.add(wsmodel.idx().getNewMorphParadigm(flexionSet, flexFreqMap, null));
//		}
//		for(MorphParadigm mpar: mpars) System.out.println("mpar: " + mpar.toString());
//		wsmodel.setMorphPars(mpars);
//		//write root flex into words in model
//		MorphParTrain.writeRootFlexIntoWord(wsmodel, ltmodel, wordFreqTHHforModelTrain, false);
//		MorphParTrain.writeRootFlexIntoModel(wsmodel, wordFreqTHHforModelTrain, false);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//	public void saveModel(WordSequences wsmodel, String path) {
//		try {
//		Writer out = MyUtils.getWriter(path);
//		for(String mpstring: wsmodel.idx().getMPlabels()) {
//			MorphParadigm mp = wsmodel.idx().getMorphParadigm(mpstring);
//			if(mp == null) continue;
//			out.write("{");
//			SortedMap<String,Double> mapSortedByFreqs = MapsOps.getSortedMapString(mp.getFlexFreqMap());
//			boolean firstEntry = true;
//			for (Iterator<String> iter = mapSortedByFreqs.keySet().iterator(); iter.hasNext();) {
//				String key = (String) iter.next();
//				String keyfreq = key+"="+mapSortedByFreqs.get(key);
//				if(firstEntry) {
//					out.write(keyfreq);
//					firstEntry = false;
//				} else out.write(", " + keyfreq);
//			}
//			out.write("}\n");
//		}
//		out.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//
//
//	private static void mainDeCluster() throws IOException {
//		String corpusPath = CorpusUtils.getCorpusDe("company");
//
//		WordSequences wsmodel = BuildWordSequenceModelDE.getWSModel("de", new String[] { corpusPath}, 800000, false, 0); //"news", "wikiW
//
//		MorphModel mp = new MorphModel(2, "id2");
//		mp.train(wsmodel);
//		mp.tag(wsmodel);
////		BuildWordSequenceModel.getInput(wsmodel, null, null, null, null);
//	}
//	
//	private static void mainUkrCluster() throws IOException {
//		WordSequences wsmodel = BuildWordSequenceModelUKR.getWSModel("ukr", new String[] {"wiki", "news"}, 400000, false, 0);
//		MorphModel mp = new MorphModel(1, "id1");
//		mp.train(wsmodel);
//		mp.tag(wsmodel);
//	}
//	
//	public static void main(String[] args) throws IOException {
////		mainUkrCluster();
//		mainDeCluster();
//	}
//
//
//	public void printMorphStats(WordSequences wsmodel) {
//		Map<String,Double> paradigmFreq = new HashMap<>();
//		Map<String,Double> paradigmFreqOnRoot = new HashMap<>();
//		Map<String,String> labelToParadigm = new HashMap<>();
//		Set<String> seenRoots = new HashSet<>();
//		double seenWords = 0.0;
//		double hasLabel = 0.0;
//		for(Word w: wsmodel.idx().getSortedWords()) {
//			seenWords++;
//			if(w.getMorphParadigm() != null) {
//				hasLabel++;
//				labelToParadigm.put(w.getMorphParadigm().getLabel(), w.getMorphParadigm().toString());
//				MapsOps.addFreq(w.getMorphParadigm().getLabel(), paradigmFreq);
//				if(Character.isUpperCase(w.toString().charAt(0))) {
//					MapsOps.addFreq(w.getMorphParadigm().getLabel()+"_UPPER", paradigmFreq);
//				}else {
//					MapsOps.addFreq(w.getMorphParadigm().getLabel()+"_lower", paradigmFreq);
//				}
//				
//				String root = w.getRoot();
//				if(seenRoots.contains(root)) continue;
//				seenRoots.add(root);
//				MapsOps.addFreq(w.getMorphParadigm().getLabel(), paradigmFreqOnRoot);
//				if(Character.isUpperCase(w.toString().charAt(0))) {
//					MapsOps.addFreq(w.getMorphParadigm().getLabel()+"_UPPER", paradigmFreqOnRoot);
//				}else {
//					MapsOps.addFreq(w.getMorphParadigm().getLabel()+"_lower", paradigmFreqOnRoot);
//				}
//			}
//		}
//		List<String> parlabels = ListOps.of(paradigmFreq.keySet());
//		Collections.sort(parlabels);
//		System.out.println();
//		for(String label: parlabels)
//			System.out.println(label + "\tWORDstats\t" + paradigmFreq.get(label) + "\t" + labelToParadigm.get(label)
//			+ "\tROOTstats\t" + paradigmFreqOnRoot.get(label) + "\t" + labelToParadigm.get(label));
//		System.out.println("seen words\t " + seenWords + "\twith morph\t" + hasLabel + "\tpercent\t" + (hasLabel/seenWords));
//	}
//
//	public static String getFlexLabel(String flex, String morphParLabel, String pref) {
//		return pref+flex+"_"+morphParLabel;
//	}
//	public static String getFlexFromFlexPar(String flexpar, String pref) {
//		return flexpar.replaceAll("^"+pref, "").replaceAll("_m.+", ""); //important f___m_0
//	}	
//	
//	public static String getRealFlexFromFlexPar(String flexpar, String pref) {
//		 String f = flexpar.replaceAll("^"+pref, "").replaceAll("_m.+", ""); //important f___m_0
//		 if(f.equals("_")) f = "";
//		 return f;
//	}
//	public static String getMPlabelFromFlexPar(String flexpar) {
//		String[]sarr = flexpar.split("_m_");
//		if(sarr.length < 2) return null;
//		return "m_" +   sarr[1];
//	}
//
//
//
//}
