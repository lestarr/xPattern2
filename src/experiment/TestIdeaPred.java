package experiment;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.util.Pair;
import model.Paradigms;
import model.WordSequences;
import model.Words;
import modelparts.SemParadigm;
import modelparts.Similarity;
import modelparts.Word;
import modelutils.Cluster;
import util.ListOps;
import util.MapsOps;
import util.MyPair;
import util.MyPairWord;
import util.MyUtils;

public class TestIdeaPred {
	
	public static final String SELDOM_F5 = "s_seldom5";
	
	public static void processSentence(WordSequences model) {
		Word w;
//		double argCoef = w.argCoef();
//		getBigrams();
//		getTokensFromBigrams();
//		addIntoModel - Hoffnung_von - isVariant of Hoffnung
	}
	
	public static List<SemParadigm> findExtendedSynParadigm(Word inputw, WordSequences model) {
		List<SemParadigm> parlist = model.idx().semPars.get(inputw);
		List<SemParadigm>semparlist  = new ArrayList<>();
		if(parlist==null||parlist.size()==0) return semparlist;
		
		for(SemParadigm sp: parlist) {
			Map<Word,Double> contextFreqLeft = new HashMap<>();
			Map<Word,Double> contextFreqRight = new HashMap<>();

		for(Word w: sp.getCopyAllArgs()) {
			
			if(w.left_of.size() < 3 || w.right_of.size() < 3) continue;
			List<MyPairWord> mainContVectorLeft = Words.getWordContextVector(true, w, 100, model);
			for(Word wl: MyPairWord.getWordSetFirst(mainContVectorLeft)) MapsOps.addFreq(wl, contextFreqLeft);
			List<MyPairWord> mainContVectorRight =  Words.getWordContextVector(false, w, 100, model);
			for(Word wr: MyPairWord.getWordSetFirst(mainContVectorRight)) MapsOps.addFreq(wr, contextFreqRight);
		
		
		}
		double thh_weight = 0.11;
		int arg_length = sp.args().size()+1;
		List<MyPairWord> contVectorLeft = MyPairWord.getMyPairWordList(contextFreqLeft, arg_length*thh_weight, 2);
		List<MyPairWord> contVectorRight = MyPairWord.getMyPairWordList(contextFreqRight, arg_length*thh_weight, 2);
		double thh = (contVectorLeft.size() + contVectorRight.size())/2-2;
		Map<Word,Similarity> map = computeSimilarWords(null, model, thh, contVectorLeft, contVectorRight);
		double minthh = findMinimalTHH(ListOps.of(map.values()));
		double meanthh = (contVectorLeft.size() + contVectorRight.size())/2-4;//(thh+minthh)/2.0;
		Map<Word,Similarity> output = getOutput(map, thh);
		

		System.out.println(contVectorLeft.size()+"\t" + contVectorRight.size() + "\t"+thh + "\t" + minthh
				+"\t"+ output.size() + "\t" + inputw.toString() );
//				+ "\t" 				+output.keySet().toString().replaceAll("[\\(\\)\\[\\]]", "").replaceAll(", ", "\t"));
		for(Word w: output.keySet()) {
			System.out.println(w+"\t"+output.get(w).left+"\t"+output.get(w).right);
		}
//		output = getOutput(map, meanthh);
//		System.out.println(meanthh	+"\t"+ output.size() + "\t" + inputw.toString() + "\t" 
//				+output.keySet().toString().replaceAll("[\\(\\)\\[\\]]", "").replaceAll(", ", "\t"));
//		
		sp.addArgs(output.keySet());

		}
		

		return null;
	}
	
	public static Collection<Set<Word>> findSynParadigms(WordSequences model, int start, int end, int contextLength, double thh, String pathtosavemodel) {
		Collection<Set<Word>> parlist = findSynParadigmsIntern(model, start, end, contextLength, thh);
		System.out.println("found synpar not sorted size: " + parlist.size());

//		List<SemParadigm> synlist = Paradigms.checkSynonyms(parlist);
//		writeSynonymParintoModel(model, synlist);
//		System.out.println("synpar size: " + synlist.size());
//		for(SemParadigm sp: synlist) {
//			System.out.println(sp.keyword().toString()+"\t"+sp.args());
//		}
		
		parlist = Paradigms.simpleMerge(parlist, 1.0,0.1);
		parlist = Paradigms.simpleMerge(parlist, 2.0,0.1);
		parlist = Paradigms.simpleMerge(parlist, 2.0,0.1);

		List tmpList = new ArrayList<>();
		for(Set<Word> sp: parlist) {
			System.out.println(sp.size()+"\t"+Paradigms.getSortedWordsAsString(sp, tmpList));
		}
		writeSemPintoModel(model, parlist);
		saveModel(parlist,pathtosavemodel);
		return parlist;
	}
	
	private static void saveModel(Collection<Set<Word>> parlist, String path) {
		try {
			Writer out = MyUtils.getWriter(path);
			for(Set<Word> wset: parlist) {
				out.write(wset.toString().replaceAll("[\\[\\{\\]\\}]+", ""));
				out.write("\n");
			}
			out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	}

	public static void loadSynParadigms(WordSequences wsmodel, String modelsempath) {
		Collection<Set<Word>> parlist = readPardigms(modelsempath, wsmodel);
		writeSemPintoModel(wsmodel, parlist);
	}

	private static Collection<Set<Word>> readPardigms(String path, WordSequences wsmodel) {
		Collection<Set<Word>> parRows = new ArrayList<Set<Word>>();
		try {
			List<String> lines = MyUtils.readLines(path);
			for(String line: lines) {
				String[] sarr = line.split(", ");
				Set<Word> wset = new HashSet<>();
				for(String s: sarr) 
					wset.add(wsmodel.getWord(s));
				if(wset.size() < 1000)
					parRows.add(wset);
			}
		} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
		return parRows;
	}

	private static void writeSemPintoModel(WordSequences model, Collection<Set<Word>> parlist) {
		int i = 0;
		for(Set<Word> sp: parlist) {
			i++;
			double seenSplitters = checkSplitterPercent(sp,model);
			if(seenSplitters >= 0.2) continue;
			SemParadigm spar = new SemParadigm(sp);
			spar.label = "b_"+Integer.toString(i);
			model.idx().semparLabels.put(spar.label, spar.toString());
			for(Word w: sp) {
				if(w.isSplitterLeftRight(model.getFreqOfAnd())) continue;
				if(!model.idx().semPars.containsKey(w))
					model.idx().semPars.put(w, new ArrayList<SemParadigm>());
				model.idx().semPars.get(w).add(spar);
				model.addCategory(spar.label, w);
			}
			Word labelword = model.getWord(spar.label);
			labelword.paradigmWords = sp;
		}
	}

	private static double checkSplitterPercent(Set<Word> wset, WordSequences model) {
		double seenSplitters = 0.0;
		for(Word w: wset) {
			if(w.isSplitterLeftRight(model.getFreqOfAnd())) seenSplitters++;
		}
		return (double)(seenSplitters/wset.size());
	}

	private static void writeSynonymParintoModel(WordSequences model, List<SemParadigm> synlist) {
		for(SemParadigm sp: synlist) {
			for(Word w: sp.getCopyAllArgs()) {
				if(!model.idx().synonymPars.containsKey(w))
					model.idx().synonymPars.put(w, new ArrayList<SemParadigm>());
				model.idx().synonymPars.get(w).add(sp);
			}
		}		
	}

	private static List<Set<Word>> findSynParadigmsIntern(WordSequences model, int start, int end, int contextLength, double thh) {
		long starttime = System.nanoTime();
		boolean lookForThh = false;
		if(thh == Double.MAX_VALUE) lookForThh = true;
		int count = 0;
		List<Set<Word>>semparlist  = new ArrayList<>();
		for(Word w: model.idx().getSortedWords()) {
			count++;
			if(count < start) continue;
			if(count > end) break;
			if(w.left_of.size() < 3 || w.right_of.size() < 3) continue;
			List<MyPairWord> mainContVectorLeft = Words.getWordContextVector(true, w, contextLength, model);
			List<MyPairWord> mainContVectorRight =  Words.getWordContextVector(false, w, contextLength, model);
		
			Map<Word,Similarity> map = computeSimilarWords(w, model, thh, mainContVectorLeft, mainContVectorRight);
			if(lookForThh)
				thh = findMinimalTHH(ListOps.of(map.values()));

			Map<Word,Similarity> output = getOutput(map, thh);
//			if(output.size() > 20 || thh > (contextLength - 1) || output.size() == 0) {
			if(output.size() > 20 || (output.size()>10 && thh > (contextLength - 4)) || (output.size()<=10 &&thh > (contextLength - 1))|| output.size() == 0) {
				if(lookForThh) thh = Double.MAX_VALUE;
//				System.out.println(count+"\t" + (int)thh +"\t" +output.size()+ "\t"+ w.toString() + "\t" + w.freq() );
				continue;
			}
			
			System.out.println(count+"\t" +(int)thh +"\t"+ output.size() + "\t" + w.toString() + "\t" 
					+output.keySet().toString().replaceAll("[\\(\\)\\[\\]]", "").replaceAll(", ", "\t"));
			
			output.put(w, new Similarity(1, 1));
			Set<Word> parwords = new HashSet<>();
			parwords.add(w);
			parwords.addAll(output.keySet());
			semparlist.add(parwords);

			if(lookForThh) thh = Double.MAX_VALUE;
		}
		long endtime = System.nanoTime();
		 double time = (double)(endtime-starttime)/1000000000.0;
		System.out.println("\nprocess time:\t"+(time)+"\tsec\t" + (time / 60.0) + "\tmin");
		return semparlist;
	}
	
	public static void findSynParadigmsDeep(WordSequences model, int start, int end, int contextLength, int deep) {
		long starttime = System.nanoTime();
		int count = 0;
		for(Word w: model.idx().getSortedWords()) {
//			System.out.print(w.toString()+"\t");
			count++;
			if(count < start) continue;
			if(count > end) break;
			Map<Word,Double> output;
			if(w.seenArgs != null) output = w.seenArgs;
			else {
				output = getSimilarWordsDeep(w, contextLength, model, Double.MAX_VALUE, deep);
				checkAndPrintSimilarWordsDeep(w,output);

			}
//			Map<Word,Double> checked = checkSimilarWordsDeep(w,output);
//			double percentFreqOne = 1.0-((double)checked.size()/(double)output.size());

		}
		long endtime = System.nanoTime();
		 double time = (double)(endtime-starttime)/1000000000.0;
		System.out.println("\nprocess time:\t"+(time)+"\tsec\t" + (time / 60.0) + "\tmin");
	
	}
	
	public static Map<Word,Double> checkSimilarWordsDeep(Word w, Map<Word,Double> simWords) {
		Map<Word,Double> checked = new HashMap<>();
		for(Word w1: simWords.keySet())
			if(simWords.get(w1)>1)
				checked.put(w1, simWords.get(w1));
		w.seenArgs = checked;
		return checked;
	}
	
	public static void checkAndPrintSimilarWordsDeep(Word w, Map<Word,Double> simWords) {
		System.out.print(w.toString() +"\t" + simWords.size());

		LinkedHashMap<Word,Double> sorted = MapsOps.printSortedMapWordDouble(simWords, null, -1, 2, false, ", ");
		double percentFreqOne = 1.0-((double)sorted.size()/(double)simWords.size());
		System.out.print("\t" + sorted.size() +"\t" + percentFreqOne);
		int i = 0;
		for(Word w1: sorted.keySet()) {
			i++; if(i>100) break;
			System.out.print("\t" + w1.toString() + "\t"+ sorted.get(w1));
		}
		System.out.println();
	}

	public static void printSimilarWords(Word w, Map<Word,Double> simWords) {
		System.out.print(w.toString() +"\t" + simWords.size());

		int i = 0;
		for(Word w1: simWords.keySet()) {
			i++; if(i>100) break;
			System.out.print("\t" + w1.toString() + "\t"+ simWords.get(w1));
		}
		System.out.println();
	}	
	
	public static Map<Word,Double> getSimilarWordsDeep(Word w, int contextLength, WordSequences model, double thh, int depth) {
		Set<Word> testedWords = new HashSet<>();
		Map<Word,Double> allArgs = getSimilarWords(w, contextLength, model, thh);
		printSimilarWords(w,allArgs);
		testedWords.add(w);
		int i =0;
		while(i < depth) {
			i++;
			Map<Word,Double> allArgsTmp = new HashMap<>(); 
			if((allArgs.size()-testedWords.size()>100)) {
				System.out.println("cutting because too many, last i was: ");
				break;
			}
		for(Word wNew: allArgs.keySet()) {
			if(testedWords.contains(wNew)) continue;
			testedWords.add(wNew);
			if(wNew.freq() < 5) continue;

			Map<Word,Double> outputNew = getSimilarWords(wNew, contextLength, model, thh);
			for(Word wNew1: outputNew.keySet()) MapsOps.addFreq(wNew1, allArgsTmp, outputNew.get(wNew1));
		}
		for(Word wNew2: allArgsTmp.keySet()) MapsOps.addFreq(wNew2, allArgs, allArgsTmp.get(wNew2));

		}
		w.seenArgs = allArgs;
		return allArgs;
	}
	
	public static Map<Word,Double> getSimilarWords(Word w, int contextLength, WordSequences model, double thh){
		if(w.seenArgs != null) return w.seenArgs;
		List<MyPairWord> mainContVectorLeft = Words.getWordContextVector(true, w, contextLength, model);
		List<MyPairWord> mainContVectorRight =  Words.getWordContextVector(false, w, contextLength, model);
		Map<Word,Double> allArgs = new HashMap<>();

		Map<Word,Similarity> map = computeSimilarWords(w, model, thh, mainContVectorLeft, mainContVectorRight);
		if(thh== Double.MAX_VALUE)
			thh = findMinimalTHH(ListOps.of(map.values()));
		if(thh <= (contextLength - 1)) { // e. g. thh < 20
			Map<Word,Similarity> output = getOutput(map, thh);
			if(output.size() <= 100) 
				for(Word w1: output.keySet()) MapsOps.addFreq(w1, allArgs);
		}
		MapsOps.addFreq(w, allArgs);
		w.seenArgs = allArgs;
		return allArgs;
	}

//	private static void printFoundBy(WordSequences model) {
//		System.out.println();
//		System.out.println("printing found by");
//		System.out.println();
//		for(Word w: model.idx().getSortedWords()) {
//			if(w.freq() < 5) break;
//			if(w.semParadigm != null && w.semParadigm.argsRemote != null) {
//				if(w.semParadigm.argsRemote.size() < 2) continue;
//				System.out.print(w.toString() +  "\t" + w.semParadigm.argsRemote.size());
//				for(Word wout: w.semParadigm.argsRemote.keySet()) {
//					if(w.semParadigm.argsRemote.get(wout) == null||w.semParadigm.argsRemote.size() == 0) continue;
//					System.out.print("\t"+w.toString()+"\t"+wout.toString()
//					+"\t"+w.semParadigm.argsRemote.get(wout).toString());
//				}
//				System.out.println();
//			}
//		}
//	}

//	private static SemParadigm makeSemPar(Word w, List<MyPairWord> mainContVectorLeft,
//			List<MyPairWord> mainContVectorRight, Map<Word, Similarity> output) {
//		SemParadigm spar = new SemParadigm();
//		spar.predsLeft = mainContVectorLeft;
//		spar.predsRight = mainContVectorRight;
//		spar.argsLocal = output;
//		w.semParadigm = spar;
//		for(Word argword: output.keySet()) {
//			argword.semParadigm.argsRemote.put(w, output.get(argword));
//		}
//		return spar;
//	}

	public static void analyzeSplitterPred(WordSequences model) {
		double sumSplitter = 0.0;
		double sumFoundPreds = 0.0;
		for(Word w: model.idx().getSortedWords()) {
			if(w.isSplitterLeftRight(model.getFreqOfAnd())) {
				sumSplitter = sumSplitter + w.freq();
				for(Word leftNeigh: w.right_of.keySet()) {
					if(leftNeigh.toString().contains("_")) continue;
					Similarity sim = Words.getBigramSignif(leftNeigh, w);
					if(sim.low() > 0.001 && sim.high() > 0.1) {
						sumFoundPreds = sumFoundPreds + w.right_of.get(leftNeigh);
						System.out.println(leftNeigh+"\t"+w+"\t"+sim.left+"\t" + sim.right);
					}
				}
			}
		}
		System.out.println("\nfound preds:" + sumFoundPreds);
		System.out.println("for splitter n:" + sumSplitter);
		System.out.println("coef:" + (sumFoundPreds/sumSplitter));
	}
	
	public static void testSeldomFreq5(WordSequences model) {
		for(Word w: model.idx().getSortedWords()) {
			if(w.freq() > 5) continue;
			if(w.freq() < 5) break;
			if(w.isSeldom()) continue;
			model.addCategory(SELDOM_F5, w);
		}
	}
	
	public static Map<Word, Similarity> getSimilarWords2(Word inputw, WordSequences model, int contextLength, 
			boolean useOnlyLeft, double thh){
		List<MyPairWord> mainContVectorLeft = Words.getWordContextVector(true, inputw, contextLength, model);
		List<MyPairWord> mainContVectorRight = null;
		if(!useOnlyLeft) mainContVectorRight = Words.getWordContextVector(false, inputw, contextLength, model);
	
		Map<Word,Similarity> map = computeSimilarWords(inputw, model, thh, mainContVectorLeft, mainContVectorRight);
		if(thh == Double.MAX_VALUE)
			thh = findMinimalTHH(ListOps.of(map.values()));

		Map<Word,Similarity> output = getOutput(map, thh);
		printOutput1(inputw, output);

		Set<Word>seen = new HashSet<>();
		seen.add(inputw);

		return output;
	}

	
	private static boolean hasNewWords(Map<Word, Similarity> output, Set<Word> seen) {
		for(Word p: output.keySet()) {
			if(!seen.contains(p)) return true;
		}
			return false;
	}



	private static Map<Word, Similarity> doExtraRound( WordSequences model, int contextLength,
			Map<Word, Similarity> inputArgs, Set<Word> seen) {
		Map<Word, Similarity> output = new HashMap<>();
		output.putAll(inputArgs);
		for(Word newInputW: inputArgs.keySet()) {
			if(seen.contains(newInputW)) continue;
			System.out.println("newInputW: " + newInputW);
			seen.add(newInputW);
			Map<Word, Similarity> map = computeSimilarWords(newInputW, model, Double.MAX_VALUE,
					newInputW.contextVectorLeft, newInputW.contextVectorRight );
			int thh = findMinimalTHH(ListOps.of(map.values()));
			Map<Word, Similarity> mapFiltered = getOutput(map,thh);
			
			printOutput1(newInputW, mapFiltered);
			output.putAll(mapFiltered);
		}
		
		return output;
	}
	
//	private static List<Pair<Word, Similarity>> doExtraRoundOld(Word inputw, WordSequences model, int contextLength,
//			Map<Word, Similarity> output) {
//		Map<Word, Similarity> map;
//		int sizeOfFirstOutput = output.size();
//		
//		List<List<MyPair>> combinedContextVector = addContextVectorsLeftRight(output);
//		List<MyPair> mainContVectorLeft = null;
//		List<MyPair> mainContVectorRight = null;
//		if(combinedContextVector.get(0) != null){
//			int toIndex = Math.min(contextLength*2, combinedContextVector.get(0).size());
//			mainContVectorLeft = combinedContextVector.get(0).subList(0, toIndex);
//		}
//		if(combinedContextVector.get(1) != null){
//			int toIndex = Math.min(contextLength*2, combinedContextVector.get(1).size());
//			mainContVectorRight = combinedContextVector.get(1).subList(0, toIndex);
//		}
//		map = computeSimilarWords(inputw, model, contextLength, Double.MAX_VALUE,
//				mainContVectorLeft, mainContVectorRight );
//		int thh = findMinimalTHH(map.values());
//
//		output = getOutput(map);
//
//		while(output.size() <= sizeOfFirstOutput) {
//			thh = thh + 1;
//			output = getOutput(map);
//		}
//		System.out.println("added thh: " + thh);
//		printOutput(inputw, output);
//		return output;
//	}

	private static void printOutput1(Word inputw, Map<Word, Similarity> output) {
		int c = 0;
		for(Word p: output.keySet()) {
			c++;
			if(c > 500) break;
			Similarity sim = output.get(p);
			System.out.println(inputw.toString() + "\t" + p.toString() + "\t" +sim.left + "\t" +sim.right + "\t" + p.freq());
		}
		System.out.println("found: " + output.size());
	}
	
	private static void printOutput2(Word inputw, Map<Word, Similarity> output) {
		int c = 0;
		System.out.print(output.size() + "\t" +inputw.toString() + "\t");
		for(Word w: output.keySet()) {
			c++;
			if(c > 500) break;
			System.out.print( w.toString() + "\t" );
		}
		System.out.println();
	}

	private static Map<Word, Similarity> getOutput(Map<Word, Similarity> map, double thh) {
		Map<Word,Similarity> output = new HashMap<>();
		for(Word w: map.keySet()) {
			if(map.get(w).left<(double)thh && map.get(w).right < (double)thh) {
				output.put(w, map.get(w));
			}
		}	
		if(output.size() < 2) thh++;
		output = new HashMap<>();
		for(Word w: map.keySet()) {
			if(map.get(w).left<(double)thh && map.get(w).right < (double)thh) {
				output.put(w, map.get(w));
			}
		}	
		return output;
	}

	private static List<List<MyPair>> addContextVectorsLeftRight(List<Pair<Word, Similarity>> list) {
		Map<String,Double> signifLeft = new HashMap<>();
		Map<String,Double> signifRight = new HashMap<>();
		for(Pair<Word, Similarity> p: list) {
			Word w = p.getKey();
			addContextVectors(w, signifLeft, true);
			addContextVectors(w,signifRight, false);
		}
		List<MyPair> l2 = new ArrayList<>();
		List<MyPair> r2 = new ArrayList<>();
		for(String s: signifLeft.keySet()) {
			l2.add(new MyPair(s, "", signifLeft.get(s)));
		}
		for(String s: signifRight.keySet()) {
			r2.add(new MyPair(s, "", signifRight.get(s)));
		}
		Collections.sort(l2);
		Collections.sort(r2);
		return ListOps.of(l2,r2);
	}
	
	private static void addContextVectors(Word w,Map<String,Double> signif, boolean left) {
		if(left && w.contextVectorLeft == null ) return ;
		if(!left && w.contextVectorRight == null ) return ;
			List<MyPairWord> l =  left ? w.contextVectorLeft : w.contextVectorRight;
			for(MyPairWord mp: l) {
				MapsOps.addFreq(mp.left.toString(), signif, mp.signif);
			}
		List<MyPair> l2 = new ArrayList<>();

		return ;
	}

	public static List<Pair<Word,Similarity>> getSimilarWords(Word inputw, WordSequences model, int contextLength){
		List<MyPairWord> mainContVectorLeft = Words.getWordContextVector(true, inputw, contextLength, model);
		List<MyPairWord> mainContVectorRight = Words.getWordContextVector(false, inputw, contextLength, model);
	
		Map<Word,Similarity> map = computeSimilarWords(inputw, model, Double.MAX_VALUE, mainContVectorLeft, mainContVectorRight);
		int thh = findMinimalTHH(ListOps.of(map.values()));
		System.out.println("min thh: " + (int)(thh));

		List<Pair<Word,Similarity>> output = new ArrayList<>();

		for(Word w: map.keySet()) {
			if(map.get(w).left < (double)thh && map.get(w).right < (double)thh) {
				System.out.println(inputw.toString() + "\t" + w.toString() + "\t" +map.get(w).left + "\t" +map.get(w).right);
				output.add(new Pair<Word,Similarity>(w, map.get(w)));
			}
		}
		return output;
	}
	private static int findMinimalTHH(List<Similarity> values) {
		if(values.size()==0)return 0;
		if(values.size()==1)return (int)values.get(0).left+1;
		double minR = Double.MAX_VALUE;
		Collections.sort(values);
		int min1 = (int)values.get(0).high(); // take high, otherwise too small thh
		int min2 = (int)values.get(1).high();
		return Math.min(min1, min2)+1;
	}

	public static void getSimilarWords(Word inputw, WordSequences model, int contextLength, double thh){
		List<MyPairWord> mainContVectorLeft = Words.getWordContextVector(true, inputw, contextLength, model);
		List<MyPairWord> mainContVectorRight = Words.getWordContextVector(false, inputw, contextLength, model);
		System.out.println("vector left:\t"+mainContVectorLeft.toString());
		System.out.println("vector right:\t"+mainContVectorRight.toString());

		Map<Word,Similarity> map = computeSimilarWords(inputw, model, thh, mainContVectorLeft, mainContVectorRight);
		for(Word w: map.keySet()) {
			System.out.println(inputw.toString() + "\t" + w.toString() + "\t" +map.get(w).left + "\t" +map.get(w).right
					+ "\t" + w.freq());

		}
	}
	

	
	public static Map<Word,Similarity> computeSimilarWords(Word inputw, WordSequences model, double thh,
			List<MyPairWord> mainContVectorLeft, List<MyPairWord> mainContVectorRight){
		Map<Word,Similarity> wlist = new HashMap<>();
//		List<MyPair> contextSignifVectorLeft = w.getContextVector(true, 0.001, 10);
		int contextLengthL = 0;
		if(mainContVectorLeft != null) contextLengthL = mainContVectorLeft.size();
		int contextLengthR = 0;
		if(mainContVectorRight != null) contextLengthR = mainContVectorRight.size();
		Set<Word> argsToCheck = collectPotentialSynonyms(contextLengthL + contextLengthR, mainContVectorLeft, mainContVectorRight);
		int found = 0;
		for(Word w: argsToCheck) {
			if(w.isParadigmWord()) continue;
			if(w.freq() < 5) continue;
			if(inputw != null && w.toString().equals(inputw.toString())) continue;
//			if(!inputw.isSplitterLeftRight(model.getFreqOfAnd()) && w.isSplitterLeftRight(model.getFreqOfAnd())) continue;
			
			double coefMatching = 0.0;
			double coefWrongContexts = 0.0;
			List<MyPairWord> candContVector = Words.getWordContextVector(true, w, contextLengthL, model);

			Similarity sim = getCoefs(candContVector, contextLengthL, mainContVectorLeft, model);
			coefMatching = coefMatching + sim.left;
			coefWrongContexts = coefWrongContexts + sim.right;
			Set<Word> intersectingNonSplitters = new HashSet<>();
			Set<Word> intersect = new HashSet<>();
			lookForNonSplitterPreds(intersectingNonSplitters,intersect,mainContVectorLeft,candContVector,model.getWord(Words.SPLITTER_WORD_LABEL).paradigmWords);
			if(mainContVectorRight == null || mainContVectorLeft.size() >= mainContVectorRight.size()) {
					if(intersect.size() == 0) continue;
			}
			int intersectLeftSize = intersect.size();
			if(mainContVectorRight != null) {
				candContVector = Words.getWordContextVector(false, w, contextLengthR, model);
				if(mainContVectorRight.size() >= mainContVectorLeft.size()){
					lookForNonSplitterPreds(intersectingNonSplitters,intersect,mainContVectorRight,candContVector,model.getWord(Words.SPLITTER_WORD_LABEL).paradigmWords);
					if(intersect.size() == intersectLeftSize) continue; // means there were no preds right!
					if(intersect.size() < 3) continue;
					if(intersectingNonSplitters.size() < 2)
						continue;
				}
			sim = getCoefs(candContVector, contextLengthR, mainContVectorRight, model);
			coefMatching = coefMatching + sim.left;
			coefWrongContexts = coefWrongContexts + sim.right;
			
			}
			if(coefMatching < thh && coefWrongContexts < thh) {
				wlist.put(w,new Similarity(coefMatching, coefWrongContexts));
				found++;
//				System.out.println(inputw.toString() + "\t" + w.toString() + "\t" +coefMatching + "\t" +coefWrongContexts);
			}
		}
//		System.out.println("foundArgsP:\t" + argsToCheck.size());
//		System.out.println("pred left:\t" + mainContVectorLeft );
//		System.out.println("pred right:\t" + mainContVectorRight);
		return wlist;
	}
	
	private static void lookForNonSplitterPreds(Set<Word> intersectingNonSplitters,Set<Word> intersect, List<MyPairWord> mainContVector, List<MyPairWord> candContVector,
			Set<Word> splitters) {
		Set<Word> main = MyPairWord.getWordSetFirst(mainContVector);
		Set<Word> cand = MyPairWord.getWordSetFirst(candContVector);
		for(Word w: main) {
			if(w.toString().equals(Words.DUMMY))continue;
			if(cand.contains(w) ) {
				intersect.add(w);
				if( !splitters.contains(w))
					intersectingNonSplitters.add(w);

			}
		}
	}

	private static Set<Word> collectPotentialSynonyms(int contextLength,
			List<MyPairWord> mainContVectorLeft, List<MyPairWord> mainContVectorRight) {
		Set<Word> set = new HashSet<>();
		int middle = 	contextLength/2;
		if(mainContVectorLeft != null) {
			set.addAll(collectLeftRight(true, mainContVectorLeft, middle));
		}
		if(mainContVectorRight != null) {
			set.addAll(collectLeftRight(false, mainContVectorRight, middle));
		}
		return set;
	}

	private static Set<Word> collectLeftRight(boolean left, List<MyPairWord> mainContVector,
			int middle) {
		Set<Word> set = new HashSet<>();

		for (int i = 0; i < Math.min(middle, mainContVector.size()); i++) {
			Word context = mainContVector.get(i).left;
			Set<Word> args = left ? context.left_of.keySet() : context.right_of.keySet();
			set.addAll(args);
		}
		return set;
	}

	public static Map<Word,Similarity> computeSimilarWordsOld(Word inputw, WordSequences model, int contextLength, double thh,
			List<MyPairWord> mainContVectorLeft, List<MyPairWord> mainContVectorRight){
		Map<Word,Similarity> wlist = new HashMap<>();
//		List<MyPair> contextSignifVectorLeft = w.getContextVector(true, 0.001, 10);

		int found = 0;
		for(Word w: model.idx().getSortedWords()) {
			if(w.isParadigmWord()) continue;
			if(w.freq() < 5) continue;
			if(w.toString().equals(inputw.toString())) continue;
			if(!inputw.isSplitterLeftRight(model.getFreqOfAnd()) && w.isSplitterLeftRight(model.getFreqOfAnd())) continue;
			
			double coefMatching = 0.0;
			double coefWrongContexts = 0.0;
			
			List<String> matchingNonSplitterPredikats = new ArrayList<>();
			
			List<MyPairWord> candContVector = Words.getWordContextVector(true, w, contextLength, model);
			Similarity sim = getCoefs(candContVector, contextLength, mainContVectorLeft, model);
			coefMatching = coefMatching + sim.left;
			coefWrongContexts = coefWrongContexts + sim.right;
			
			if(mainContVectorRight != null) {
				candContVector = Words.getWordContextVector(true, w, contextLength, model);
			sim = getCoefs(candContVector, contextLength, mainContVectorRight, model);
			coefMatching = coefMatching + sim.left;
			coefWrongContexts = coefWrongContexts + sim.right;
			}
			if(coefMatching < thh && coefWrongContexts < thh) {
				wlist.put(w,new Similarity(coefMatching, coefWrongContexts));
				found++;
//				System.out.println(inputw.toString() + "\t" + w.toString() + "\t" +coefMatching + "\t" +coefWrongContexts);
			}
		}
		System.out.println("found:\t" + found);
		System.out.println("pred left:\t" + mainContVectorLeft );
		System.out.println("pred right:\t" + mainContVectorRight);
		return wlist;
	}
	
	private static Similarity getCoefs(List<MyPairWord> candContVector, int contextLength, List<MyPairWord> mainContVector, WordSequences model) {
		double coefMatching = Cluster.getScaledSimMeasure(MyPairWord.getListFirst(mainContVector), MyPairWord.getListFirst(candContVector));
		double coefWrongContexts = Cluster.getScaledSimMeasure(MyPairWord.getListFirst(candContVector),MyPairWord.getListFirst(mainContVector));
		return new Similarity(coefMatching, coefWrongContexts);
	}
	
	public static List<Word> getSimilarWordsLeft(boolean left, Word inputw, WordSequences model, int contextLength, double thh){
		List<Word> wlist = new ArrayList<Word>();
//		List<MyPair> contextSignifVectorLeft = w.getContextVector(true, 0.001, 10);

		List<MyPairWord> mainContVector = Words.getWordContextVector(left, inputw, contextLength,model);
		int found = 0;
		for(Word w: model.idx().getSortedWords()) {
			if(w.isParadigmWord()) continue;
			if(w.freq() < 5) continue;
			if(!inputw.isSplitterLeftRight(model.getFreqOfAnd()) && w.isSplitterLeftRight(model.getFreqOfAnd())) continue;
			
			List<MyPairWord> candContVector = Words.getWordContextVector(left, w, contextLength,model);
			double coefMatching = Cluster.getScaledSimMeasure(MyPairWord.getListFirst(mainContVector), MyPairWord.getListFirst(candContVector));
			double coefWrongContexts = Cluster.getScaledSimMeasure(MyPairWord.getListFirst(candContVector), MyPairWord.getListFirst(mainContVector));
			if(coefMatching < thh && coefWrongContexts < thh) {
				wlist.add(w);
				found++;
				System.out.println(inputw.toString() + "\t" + w.toString() + "\t" +coefMatching + "\t" +coefWrongContexts);
			}
		}
		System.out.println("found:\t" + found);
		return wlist;
	}

}
