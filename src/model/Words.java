package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import modelparts.Collocation;
import modelparts.Similarity;
import modelparts.Word;
import util.MapsOps;
import util.MyPair;
import util.MyPairWord;
import util.MyUtils;

public class Words {
	
	
	public static double getSignifOfLeft(Word left, Word right) {
		boolean bigramExist = left.left_of.containsKey(right);
		if(!bigramExist)			return 0.0;
		
		double bigramFreq = left.left_of.get(right);
		return (bigramFreq/right.freq());
	}
	
	public static double getSignifOfRight(Word left, Word right) {
		boolean bigramExist = left.left_of.containsKey(right);
		if(!bigramExist)			return 0.0;
		
		double bigramFreq = left.left_of.get(right);
		return (bigramFreq/left.freq());
	}
	
	public static List<List<MyPair>> computeContextsWithSignif(Word w, boolean wordIsLeftOf, int howmany, boolean print) {
		HashMap<Word, Double> contexts;
		if (howmany == -1)
			howmany = Integer.MAX_VALUE;
		double wfreq = w.freq();
		if (wordIsLeftOf)		contexts = w.left_of;
		else					contexts = w.right_of;
		int i = 0;
		Map<Word, Double> mapTestWordSignif = new HashMap<>();
		Map<Word, Double> mapContexttWordSignif = new HashMap<>();

		for (Word cont : contexts.keySet()) {
			i++;
			if (i > howmany)				break;
			double freq = contexts.get(cont); //bigram freq
			double signifCOnt = MyUtils.rdouble(freq / wfreq);
			double signifTestWord = MyUtils.rdouble(freq / cont.freq());
			mapContexttWordSignif.put(cont, signifCOnt);
			mapTestWordSignif.put(cont, signifTestWord);
		}
		List<List<MyPair>> groups = splitArgumentsIntoGroups(mapTestWordSignif);
		List<List<MyPair>> groupsForContext = splitArgumentsIntoGroups(mapContexttWordSignif);

		if (print) {
			printGroups(groups, 5000);
			System.out.println("signif of CONTEXT");
			printGroups(groupsForContext, 5000);
		}
		groups.addAll(groupsForContext);
		return groups;
	}
	
	public static double percentOfLeftIndependent(Word w) {
		double sizeOfRightCo = (double)w.left_of.size();
		double sizeOfRightInd = (double)computeContextsWithSignif(w,true,-1,false).get(4).size();
		return sizeOfRightInd/sizeOfRightCo;
	}
	
	public static List<MyPairWord> computeNbestContexts(Word w, boolean wordIsLeftOf, int howmany, String regexForParadimFilter, WordSequences model,
			boolean allowFreqOne){
		HashMap<Word, Double> contexts;
		if (howmany == -1)
			howmany = Integer.MAX_VALUE;
		double wfreq = w.freq();
		if (wordIsLeftOf)		contexts = w.left_of;
		else					contexts = w.right_of;
		Map<Word, Double> mapContexttWordSignif = new HashMap<>();

		for (Word cont : contexts.keySet()) {
			if(regexForParadimFilter != null) {
				if(cont.toString().matches(regexForParadimFilter) ) continue;
			}
			if(model.idx().deletedParadigmLabels.contains(cont.toString())) continue;
			double freq = contexts.get(cont); //bigram freq
			double signifCOnt = MyUtils.rdouble(freq / wfreq);
			if(signifCOnt == 1.0 && !allowFreqOne) continue;
//			if(signifCOnt < 0.01) continue;
			if(signifCOnt < 0.001) continue;
			mapContexttWordSignif.put(cont, signifCOnt);
		}
		List<MyPairWord> toReturn = new ArrayList<>();
		for(Word w1: mapContexttWordSignif.keySet())
			toReturn.add(new MyPairWord(w1, mapContexttWordSignif.get(w1)));
		//pad
		toReturn = padSortAndCut(model, toReturn, howmany, true, w.toString().contains("_"));
		return  toReturn;
	}

	public static List<List<MyPair>> splitArgumentsIntoGroups(Map<Word, Double> argumentsForGroups) {
		List<List<MyPair>> groups = getGroups();
		for(Word w: argumentsForGroups.keySet()) 			
			addSignifToGroup(w.toString(), argumentsForGroups.get(w), groups);
		return groups;
	}

	public static List<List<MyPair>> getGroups() {
		List<List<MyPair>> groups = new ArrayList<>(5);
		groups.add(0, new ArrayList<MyPair>());
		groups.add(1, new ArrayList<MyPair>());
		groups.add(2, new ArrayList<MyPair>());
		groups.add(3, new ArrayList<MyPair>());
		groups.add(4, new ArrayList<MyPair>());
		return groups;
	}

	public static void printGroups(List<List<MyPair>> groups, int howmany) {
		System.out.println();
		System.out.println(groups.get(0).size() + " " + printGroup(groups.get(0), howmany));
		System.out.println(groups.get(1).size() + " " + printGroup(groups.get(1), howmany));
		System.out.println(groups.get(2).size() + " " + printGroup(groups.get(2), howmany));
		System.out.println(groups.get(3).size() + " " + printGroup(groups.get(3), howmany));
		System.out.println(groups.get(4).size() + " " + printGroup(groups.get(4), howmany));
	}
	
	private static String printGroup(List<MyPair> list, int howmany) {
		Collections.sort(list); // , Collections.reverseOrder() - not necessary see compareTo
		list = list.subList(0, Math.min(howmany, list.size()) );
		return list.toString();
	}

	public static void addSignifToGroup(String word, double signif, List<List<MyPair>> groups) {
		if (signif > 1.0) {
			groups.get(4).add(new MyPair(word, "", signif));
		} else if (signif > 0.9)
			groups.get(0).add(new MyPair(word, "", signif));
		else if (signif > 0.1)
			groups.get(1).add(new MyPair(word, "", signif));
		else if (signif > 0.01)
			groups.get(2).add(new MyPair(word, "", signif));
		else if (signif > 0.001)
			groups.get(3).add(new MyPair(word, "", signif));
		else
			groups.get(4).add(new MyPair(word, "", signif));
	}
	
	public static boolean isStopword(String wstring) {
		if(wstring.toLowerCase().matches("aaa|eee|zzz|doctitle")  || wstring.contains("doctitle")
				|| wstring.contains("AAA")
				|| wstring.contains("EEE")
				|| wstring.contains("ZZZ")) return true;
		return false;
	}
	
	public static boolean isLeftWord(Word w) {
		double coefL= w.getCoef(true);
		double coefR = w.getCoef(false);
		if(coefL > coefR) return true;
		return false;
	}
	

	public static boolean isCollocation(WordSequences wsmodel, Word l, Word r, double minFreq, double thh, boolean print) {
		
		if(!l.left_of.containsKey(r)) return false;
		if(l.left_of.get(r) < minFreq) return false;
		
		double l_thh = l.left_of.get(r)/r.freq();
		double r_thh = l.left_of.get(r)/l.freq();
		
		if(print) {
			System.out.println(MyUtils.rdouble(l_thh)+ "; " + MyUtils.rdouble(r_thh) + "; frec: " + MyUtils.rdouble(l.left_of.get(r)) );
		}
		if(l_thh > thh && r_thh > thh) return true;
		return false;		
	}
	
	public static Collocation getCollocation(Word l, Word r) {
		if(l.freq() < 5.0 || r.freq() < 5.0) return new Collocation(l.toString(), r.toString(), new Similarity(0.0, 0.0));
		if(!l.left_of.containsKey(r)) return new Collocation(l.toString(), r.toString(), new Similarity(0.0, 0.0));
		
		double l_thh = l.left_of.get(r)/r.freq();
		double r_thh = l.left_of.get(r)/l.freq();
		Similarity sim = new Similarity(MyUtils.rdouble(l_thh), MyUtils.rdouble(r_thh));
		Collocation c = new Collocation(l.toString(), r.toString(), sim);
		c.setFreq(l.left_of.get(r));
		return	c ;
	}

	/**
	 * gets only first signif word!
	 */
	public static Set<String> getMostSignificantLeftWord(Word testword){
		Set<String> signifWords = new HashSet<>();
		Map<Word, Double> mapContexttWordSignif = new HashMap<>();
		HashMap<Word, Double> contexts = testword.right_of;
		for (Word cont : contexts.keySet()) {
			double bigramfreq = contexts.get(cont); //bigram freq
			double signifCOnt = MyUtils.rdouble(bigramfreq / testword.freq());
			mapContexttWordSignif.put(cont, signifCOnt);
		}
		String firstSignif = MapsOps.getFirst(mapContexttWordSignif).first;
		signifWords.add(firstSignif);
//		Map someFirstSignifs = MapsOps.getFirstEntriesObject(mapContexttWordSignif, Integer.MAX_VALUE, 0.1);
//		for(Object o: someFirstSignifs.keySet())
//			signifWords.add(o.toString());
		return signifWords;
	}
	
	public static boolean isFrequent(Word w, WordSequences model) {
		if(w.freq() > (model.getFreqOfAnd() / 100.0) )
			return true;
		return false;
	}

	public static List<Collocation> getAllCollocaions(WordSequences model, double thh, double thh_high, double thh_max) {
		List<Collocation> colls = new ArrayList<>();
		for(Word w: model.idx().getSortedWords()) {
			if(w.freq()<5) break;
			for(Word right: w.left_of.keySet()) {
				Similarity signif = getBigramSignif(w, right);
				if(signif.low() >= thh && signif.high() > thh_high && signif.high() < thh_max) 
					colls.add(new Collocation(w.toString(), right.toString(), signif));
			}
		}
		return colls;
	}

	public static List<Collocation> getWordCollocations(Word w,			double thh, double minfreq) {
		return getWordCollocations(w, thh, minfreq, thh, false, false, Integer.MAX_VALUE);
	}
	public static List<Collocation> getWordCollocations( Word w,			double thh, double minfreq, double thh_high, int howmany) {
		return getWordCollocations(w, thh, minfreq, thh_high, false, false, howmany);
	}
	public static List<Collocation> getWordCollocations(boolean left, Word w,			
			double thh, double minfreq, double thh_high, int howmany) {
		if(left) return getWordCollocations(w, thh, minfreq, thh_high, true, false, 25);
		else return getWordCollocations(w, thh, minfreq, thh_high, false, true, 25);
	}
	public static List<Collocation> getWordCollocations(boolean left, Word w,			double thh, double minfreq, double thh_high) {
		if(left) return getWordCollocations(w, thh, minfreq, thh_high, true, false, Integer.MAX_VALUE);
		else return getWordCollocations(w, thh, minfreq, thh_high, false, true, Integer.MAX_VALUE);
	}
	public static List<Collocation> getWordCollocations(boolean left, Word w) {
		if(left) return getWordCollocations(w, 0.0, 0, 0.0, true, false, Integer.MAX_VALUE);
		else return getWordCollocations(w, 0.0, 0, 0.0, false, true, Integer.MAX_VALUE);
	}
	public static List<MyPairWord> getWordContextVector(boolean left, Word w, int howmany, WordSequences model) {
		if(left && w.contextVectorLeft != null && w.contextVectorLeft.size() >= howmany) return w.contextVectorLeft.subList(0, howmany);
		if(!left && w.contextVectorRight != null && w.contextVectorRight.size() >= howmany) return w.contextVectorRight.subList(0, howmany);
		
		 List<Collocation> colls;
//		if(left) colls =  getWordCollocations(w, 0.0, 0, 0.0, true, false);
//		else colls = getWordCollocations(w, 0.0, 0, 0.0, false, true);
		 if(left) colls =  getWordCollocations(w, 0.001, 0, 0.001, true, false, howmany);
			else colls = getWordCollocations(w, 0.001, 0, 0.001, false, true, howmany);
		Collections.sort(colls); // sort on lower signif
		 List<MyPairWord> output = new ArrayList<>();
		 int i = 0;
		 for(Collocation c: colls) {
//			 if(i >= howmany) break;
			 String context = left ? c.left : c.right;
			 double signif = left ? c.sim.left : c.sim.right;
			 Word contextWord = model.getWord(context);
			 if(contextWord.isSeldom() || contextWord.isSeldom2(model.getFreqOfAnd())) 
				 continue;
			 output.add(new MyPairWord(contextWord, signif));
			 i++;
		 }
		 //pad
		 padSortAndCut(model, output, howmany, false, w.toString().contains("_"));
//		 Collections.sort(output); //sort on context signif
		 
		 if(left) w.contextVectorLeft = output;
		 else w.contextVectorRight = output;
		 return output;
	}

	public static List<MyPairWord> padSortAndCut(WordSequences model, List<MyPairWord> output, int howmany, boolean sort, boolean isCatWord) {
	//sort
      if(sort)        Collections.sort(output);
      
	  int padNumber = howmany - output.size();
	  for (int j = 0; j < padNumber; j++) {
		if(isCatWord)
          output.add(new MyPairWord(model.getWord(DUMMY+"_"+Integer.toString(j)),0.0));
		else
		  output.add(new MyPairWord(model.getWord(DUMMY+Integer.toString(j)),0.0));
	  }
		
		output = output.subList(0, howmany);
		return output;
	}

	public static List<Collocation> getWordBestCollocations( Word w) {
		List<Collocation> clist = getWordCollocations(w, 0.01, 2, 0.01, false, false, Integer.MAX_VALUE);
		if(clist.size() < 2) clist = getWordCollocations(w, 0.001, 2, 0.001, false, false, Integer.MAX_VALUE);
		
		Collections.sort(clist);
		clist = clist.subList(0, Math.min(clist.size(), 10));
		return clist;
	}
	public static List<Collocation> getWordCollocations( Word w,	double thh, double minfreq, double thh_high, 
			boolean useOnlyLeft, boolean useOnlyRight, int howmany) {
		List<Collocation> colls = new ArrayList<>();
		if(!useOnlyLeft) {
		for(Word right: w.left_of.keySet()) {
			if(right.isParadigmWord()) continue;
			if(w.left_of.get(right) < minfreq) continue;
			Similarity signif = getBigramSignif(w, right);
			if(signif.low() >= thh && signif.high() >= thh_high) {
				Collocation c = new Collocation(w.toString(), right.toString(), signif);
				c.setFreq(w.left_of.get(right));
				colls.add(c);
			}
		}
		colls.sort(
			      (Collocation c1, Collocation c2) -> Double.compare(c1.sim.left, c2.sim.left)*(-1));
//		Collections.sort(colls, new Comparator<Collocation>(){
//			@Override
//			public int compare(Collocation o1, Collocation o2) {
//				// TODO Auto-generated method stub
//				return 0;
//			}
//		});
		colls = colls.subList(0, Math.min(colls.size(), howmany));

		}
		if(!useOnlyRight) {
			List<Collocation> collsR = new ArrayList<>();
		for(Word left: w.right_of.keySet()) {
			if(left.isParadigmWord()) continue;
			if(w.right_of.get(left) < minfreq) continue;
			Similarity signif = getBigramSignif(left, w);
			if(signif.low() >= thh && signif.high() >= thh_high) {
				Collocation c = new Collocation(left.toString(), w.toString(), signif);
				c.setFreq(w.right_of.get(left));
				collsR.add(c);
			}
		}
		collsR.sort(
			      (Collocation c1, Collocation c2) -> Double.compare(c1.sim.right, c2.sim.right)*(-1));		
		collsR = collsR.subList(0, Math.min(collsR.size(), howmany));
		colls.addAll(collsR);
		}
		return colls;
	}

	public static Similarity getBigramSignif(Word l, Word r) {
		if(!l.left_of.containsKey(r)) return new Similarity(0.0, 0.0);
		
		double bigramfreq = l.left_of.get(r);
		double l_signif = MyUtils.rdouble(bigramfreq/r.freq());
		double r_signif = MyUtils.rdouble(bigramfreq/l.freq());
		return new Similarity(l_signif, r_signif);
	}
	
	public static List<Integer> getZahlen(Word wBig, boolean wordIsLeft) {
		if(wBig.zalen != null) return wBig.zalen;
		List<Integer> zahlen = new ArrayList<Integer>();
		List<List<MyPair>> list = Words.computeContextsWithSignif(wBig, wordIsLeft, -1, false);
		for (List l : list) {
			zahlen.add(l.size());
		}
		wBig.zalen =zahlen;
		return zahlen;
	}
	public static final String DUMMY = "dummy_";
	static public final String STRONG_PRED = "strongPred";
	static public final String STRONG_ARG = "strongArg";
	static public final String WEAK_PRED = "weakPred";
	static public final String ARG = "Arg";
	static public final String PRED = "Pred";
	static public final String UNK = "unknown";
	
	public static final String SPLITTER_WORD_LABEL = "s_splitter";
	public static final String PHRASE_END_LABEL = "s_phend";
	public static final String PHRASE_START_LABEL = "s_phstart";

	public static final String SLEFT_WORD_LABEL = "s_left";
	public static final String SRIGHT_WORD_LABEL = "s_right";
	public static final String SMIDDLE_WORD_LABEL = "s_middle";
	public static final String SUNKNOWN_WORD_LABEL = "s_unknown";

	public static final String ALLPARS_FILTER = "[mfsbt]_.+";//t is for temporary, s=syntax, b=semantics, m= MORPH
	public static final String MORPH_FILTER = "[mft]_.+";
	public static final String SYNSEM_FILTER = "([sbt]_.+)|s_splitter";
	public static final String SYN_FILTER =    "([sbtf]_.+)|s_splitter"; //".+_.+"; //
	
	public static String interpretContextSignificance(List<Integer> zahlen) {
		String interpret;
		int lastGroup = zahlen.get(4);
		if(zahlen.size() != 10) return "Error: zahlen != 10";
		if((zahlen.get(0)+zahlen.get(1)+zahlen.get(2)+zahlen.get(3)+zahlen.get(4)) < 5)
			return UNK;
		if((zahlen.get(3)+lastGroup) 		<  zahlen.get(0)
				&& (zahlen.get(3)+lastGroup) <  zahlen.get(1)
				&& (zahlen.get(3)+lastGroup) 		<  zahlen.get(2))
			return STRONG_PRED; //sum of two last groups smaller than each other group
		if((zahlen.get(0)+zahlen.get(1)+zahlen.get(2)) 		> (zahlen.get(3)+zahlen.get(4))
				&& lastGroup < zahlen.get(3)
				&& lastGroup < zahlen.get(2)
				&& lastGroup < zahlen.get(1)
				&& lastGroup < zahlen.get(0))
			return PRED;
		if((zahlen.get(0)+zahlen.get(1)+zahlen.get(2)+zahlen.get(3)) <
				(zahlen.get(4))) return STRONG_ARG;
		if(lastGroup >= zahlen.get(3) && lastGroup > zahlen.get(2) && lastGroup > zahlen.get(1)) return ARG;
		return WEAK_PRED;
	}
	
	/**
	 * adds splitter as word and fills all the lefts and rights
	 */
	static public  void addSplitterWordStats( WordSequences model) {
		for(Word w: model.idx().getSortedWords()) {
			if(w.isSplitterLeftRight(model.getFreqOfAnd())) {
//				if(w.toString().equals(wsmodel.getAndString(wsmodel.getLang())))					continue;
				model.addCategory(SPLITTER_WORD_LABEL, w);
				model.getWord(SPLITTER_WORD_LABEL).paradigmWords.add(w);
			}
		}
//		Word splitterW = model.getWord(SPLITTER_WORD_LABEL);
//		for(Word splitter: splitterW.paradigmWords) {
//			List<Collocation>colls = Words.getWordCollocations(splitter, 0.01, 0, 0.01);
//			Collections.sort(colls);
//			for(Collocation c: colls) {
//				System.out.println(c.toString() + "\t" + c.getFreq());
//			}
//			System.out.println();
//		}
//		findAndAddPhraseEnd(model);
//		findAndAddPhraseStart(model);
////		findAndAddPhraseEndBeforeStart(model);
	}
	
//	private static void findAndAddPhraseEndBeforeStart(WordSequences model) {
//		for(Word w: model.idx().getSortedWords()) {
//			if(w.freq() < 3) break;
//			if(w.features.containsKey(PHRASE_END_LABEL)) continue; //not phrase end
//			if(w.features.containsKey(PHRASE_START_LABEL)) continue;
//			if(!w.left_of.containsKey(model.getWord(PHRASE_START_LABEL))) continue; // is left of phrase end
//			//(splitter w) / (w splitter), high is for ADJ 
//			System.out.println("PH_END_AFTER_START: " + w.toString());
//				w.features.put(PHRASE_END_LABEL, 1.0);
//				model.addCategory(PHRASE_END_LABEL, w);
//		}		
//	}
	
	private static void findAndAddPhraseStart(WordSequences model) {
		for(Word w: model.idx().getSortedWords()) {
			if(w.freq() < 3) break;
			if(w.features.containsKey(PHRASE_END_LABEL)) continue; //not phrase end
			if(!w.right_of.containsKey(model.getWord(PHRASE_END_LABEL))) continue; // is left of phrase end
			//(splitter w) / (w splitter), high is for ADJ 
//			System.out.println("PHSTART: " + w.toString());
				w.features.put(PHRASE_START_LABEL, 1.0);
				model.addCategory(PHRASE_START_LABEL, w);
		}		
	}

	private static void findAndAddPhraseEnd(WordSequences model) {
		Map<Word,Double> lefts = model.getWord(SPLITTER_WORD_LABEL).right_of;
		
		for(Word leftOfSplitter: model.idx().getSortedWords()) {
			if(leftOfSplitter.freq() < 3) break;
			if(!model.getWord(SPLITTER_WORD_LABEL).right_of.containsKey(leftOfSplitter)
					&& !model.getWord("EEEND").right_of.containsKey(leftOfSplitter)) continue;
			//(splitter w) / (w splitter), high is for ADJ
			double splitterCoef = getSplitterCoef(leftOfSplitter, model);
			if(splitterCoef < 2.0) { // means w is likely to be noun or verb
//				System.out.println("PHEND: " + leftOfSplitter.toString());
				leftOfSplitter.features.put(PHRASE_END_LABEL, splitterCoef);
				model.addCategory(PHRASE_END_LABEL, leftOfSplitter);
			}
		}
	}

	public static double getSplitterCoef(Word w, WordSequences model) {
		Collocation splt_l = Words.getCollocation(model.getWord(SPLITTER_WORD_LABEL), w);
		Collocation splt_r = Words.getCollocation(w, model.getWord(SPLITTER_WORD_LABEL));	
		return splt_l.sim.left/splt_r.sim.right;
	}

	public static void printExpectations(Word w, double thh, WordSequences model){
		System.out.println("l:\t"+getExpectationsLeftRightSorted(w, true, thh, model));
		System.out.println("r:\t"+getExpectationsLeftRightSorted(w, false, thh, model));
	}
	public static List<MyPairWord> getExpectationsLeftRightSorted(Word w, boolean left, double thh, WordSequences model){
	  return getExpectationsLeftRightSorted(w, left, thh, model, null);
	}

	public static List<MyPairWord> getExpectationsLeftRightSorted(Word w, boolean left, double thh, WordSequences model, String regexForParadimFilter){
		List<MyPairWord> list = new ArrayList<>();
		HashMap<Word, Double> contexts;
		double wfreq = w.freq();
		if (left)		contexts = w.left_of;
		else			contexts = w.right_of;
		for (Word cont : contexts.keySet()) {
			if(cont.toString().equals("s_splitter")
			    || model.idx().deletedParadigmLabels.contains(cont.toString()))
			  continue; // splitter banned and splitted into s0 and s1
			if(regexForParadimFilter != null && cont.toString().matches(regexForParadimFilter) ) 
			  continue;
			double freq = contexts.get(cont); //bigram freq
			double signifCOnt = MyUtils.rdouble(freq / wfreq);
			if(signifCOnt == 1.0) continue;
			if(signifCOnt > thh )
				list.add(new MyPairWord(cont, signifCOnt));
		}
		Collections.sort(list);
		return list;
	}
	@Deprecated
	public static List<MyPair> getExpectationsLeftRightSortedString(Word w, boolean left){
		List<MyPair> list = new ArrayList<>();
		HashMap<Word, Double> contexts;
		double wfreq = w.freq();
		if (left)		contexts = w.left_of;
		else			contexts = w.right_of;
		Map<Word, Double> mapContexttWordSignif = new HashMap<>();

		for (Word cont : contexts.keySet()) {
			double freq = contexts.get(cont); //bigram freq
			double signifCOnt = MyUtils.rdouble(freq / wfreq);
			mapContexttWordSignif.put(cont, signifCOnt);
		}
		List<List<MyPair>> groupsForContext = splitArgumentsIntoGroups(mapContexttWordSignif);
		list.addAll(groupsForContext.get(0));
		list.addAll(groupsForContext.get(1));
		list.addAll(groupsForContext.get(2));
		Collections.sort(list);
		return list;
	}
	public static List<Collocation> getListCollocations(Set<Word> leftWords, Set<Word> rWords) {
		List<Collocation> clist = new ArrayList<>();
		for (Word l: leftWords) {
			for (Word r: rWords) {
				Collocation c = getCollocation(l, r);
				if(c.sim.low() > 0.001)
					clist.add(c);
			}
		}
		return clist;
	}

	public static void addPhraseWordStats(WordSequences wsmodel, double collocthh) {
		for(Word w: wsmodel.idx().getSortedWords()) {
			String label = getCollocLeftRightLabel(w, collocthh);
			
			wsmodel.addCategory(label, w);
		}				
	}

	public static String getCollocLeftRightLabel(Word w, double collocthh) {
		double coef = w.isLeftCollocWord(collocthh);
		String label;
		if(coef > 0.59) label = SLEFT_WORD_LABEL;
		else if(coef > 0.4) label = SMIDDLE_WORD_LABEL;
		else if(coef > 0.0) label = SRIGHT_WORD_LABEL;
		else label = SUNKNOWN_WORD_LABEL;
		if(w.freq() < 4) label = SUNKNOWN_WORD_LABEL;
		return label;
	}

	public static String computeRootFromFlex(Word word, String flex) {
		int flexlen = flex.equals("_") ? 0 : flex.length();
		String wstring = word.toString();
		return wstring.substring(0, wstring.length()-flexlen);
	}




	
	

}
