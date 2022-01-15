package experiment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.WordSequences;
import model.Words;
import modelparts.Collocation;
import modelparts.Similarity;
import modelparts.Word;
import tokenizer.TestTokenizer;
import util.ListOps;
import util.MapsOps;
import util.MyPair;

public class TestIdeaPhrase {

	public static void checkWordsWithoutCollocation(WordSequences model) {
		int count = 0;
		for(Word w: model.idx().getSortedWords()) {
			count++;
			if(count > 300) break;
			if(!w.isSplitterLeftRight(model.getFreqOfAnd())) continue;
			List<Collocation> colls = Words.getWordCollocations(w, 0.01, 0, 0.01, Integer.MAX_VALUE);
			boolean hasSplittersAsCollocats = false;
			for(Collocation c: colls) {
				//check collocs with other splitter
//				if(c.left.equals(w.toString()) && model.getWord(c.right).isSplitterLeftRight(model.getFreqOfAnd()))
//					hasSplittersAsCollocats = true;
				if(c.right.equals(w.toString()) && model.getWord(c.left).isSplitterLeftRight(model.getFreqOfAnd())) {
					hasSplittersAsCollocats = true;
					System.out.println(w+"\t" + c.toString());
					break;
				}
			}
			if(!hasSplittersAsCollocats) 					
				System.out.println(w);
				
//			if(colls.size()<3)				System.out.println(w+"\t" + colls.toString());
//			else				System.out.println(w.toString()+"\t"+colls.size());
		}
	}

	
	static public String printBigrams(List<MyPair> bigrams, String  split){
		StringBuffer sb = new StringBuffer();
		for(MyPair p: bigrams) {
			sb.append(p.first+" "+p.second+split);
		}
		return sb.toString();
	}

	static public String getSentFromBigrams(List<MyPair> bigrams, String  split){
		StringBuffer sb = new StringBuffer();
		for(MyPair p: bigrams) {
			sb.append(p.first+" ");
		}
		sb.append(bigrams.get(bigrams.size()-1).second);
		String sent = sb.toString().replaceAll("_ ", "_");
		sent = sent.replaceAll("(AAANF_(\\._)*)|(_(\\._)*EEEND)", "");
//		sent = sent.replaceAll("(_(\\._)*[\\.\\[\\]\\(\\),:])", " , ");
		sent = sent.replaceAll("(_(\\._)*[,])", " , ");

		sent = sent.replaceAll(" ", split);

		return sent;
	}
	

	
	
	static public List<MyPair> getBigrams(String sent, String lang){
		List<MyPair> list = new ArrayList<>();
		String prev = null; 
		for(String curr: TestTokenizer.getTokens(sent, lang, true, true)) {
//			if(!MyUtils.isWord(curr)) {
//				prev = null;
//				continue;
//			}
			if(prev == null) {
				prev = curr;
				continue;
			}
			MyPair p = new MyPair(prev, curr);
			prev = curr;
			list.add(p);
		}
		return list;
	}
	
	
	static public List<MyPair> interpretExpectations(List<MyPair> inputBigrams, WordSequences model, String marker, boolean left){
		List<MyPair> list = new ArrayList<>();
		for(MyPair p: inputBigrams) {
			Word l = model.getWord(p.first);
			Word r = model.getWord(p.second);
			if(l.toString().endsWith("_") ) {
				list.add(p);
				continue;
			}
			Word predikat = left ? l : r;
			if(!predikat.isSplitterLeftRight(model.getFreqOfAnd())) {
				list.add(p);
				continue;
			}
			
			List<MyPair> exp;
			if(left) exp = Words.getExpectationsLeftRightSortedString(l,true);
			else exp = Words.getExpectationsLeftRightSortedString(r, false);
			
			Set<Word> wordcats;
			//expectations for contexts are in ls and rs
			//take expectations, compare them with context and its cats
			if(left) wordcats = r.getWordCats(model);
			else wordcats = l.getWordCats(model);
			
			
			double bestSignif = getExpMeasure(exp, wordcats);
			if(bestSignif > 0)
				list.add(new MyPair(p.first+marker+bestSignif+"_", p.second));
			else
				list.add(p);
		}
		return list;
	}
	
	
	public static double getExpMeasure(List<MyPair> goldenFeatures, Set<Word> wordcats) {
		if(goldenFeatures == null || goldenFeatures.size() < 1) return 0;
		Map<String,Double> goldenfeatMap = new HashMap<>();
		for(MyPair p: goldenFeatures) {			goldenfeatMap.put(p.first, p.freq);		}
		double bestSignif = 0;
		for (Word w: wordcats) {
			
			String wcat = w.toString();
			if(goldenfeatMap.containsKey(wcat)) {
				double weight = getWeight(wcat);
				double signif = goldenfeatMap.get(wcat) + weight;
				if(bestSignif < signif)
					bestSignif = signif;
			}
				
		}
		return bestSignif;
	}
	
	private static double getWeight(String wcat) {
		double weight = 0.0;
		if(wcat.startsWith("s_")) return weight;
		if(wcat.startsWith("m_")) return weight+1;
		if(wcat.startsWith("f_")) return weight+2;
		return weight+3;
	}

	public static double getExpMeasureOld(List<MyPair> goldenFeatures, List<Word> candidateFeatures) {
		if(goldenFeatures == null || goldenFeatures.size() < 1) return 0;
		Set<String> featSet = new HashSet<>();
		for(Word w: candidateFeatures) featSet.add(w.toString());
		int size = goldenFeatures.size();
		double bestSignif = 0;
		for (int i = 0; i < size; i++) {
			String f = goldenFeatures.get(i).first;
			double signif = goldenFeatures.get(i).freq;
			if(featSet.contains(f) && bestSignif < signif)
				bestSignif = signif;
		}
		return bestSignif;
	}
	
	public static List<MyPair> interpretPhrasesSplitterRight(List<MyPair> bigrs, WordSequences model, String marker,
			Similarity signifThh) {
		List<MyPair> list = new ArrayList<>();
		for(MyPair p: bigrs) {
			Word l = model.getWord(p.first);
			Word r = model.getWord(p.second);
			if(l.toString().endsWith("_") || !l.left_of.containsKey(r) || l.left_of.get(r) < 5 || !r.isSplitterLeftRight(model.getFreqOfAnd())) {
				list.add(p);
				continue;
			}
			Similarity signif = Words.getBigramSignif(l, r);
			if(signif.left >= signifThh.left && signif.right > signifThh.right) {
				list.add(new MyPair(p.first+marker, p.second));
				//write pred info
				l.seenAsPred++;
				l.predVariants.add(r);
				r.predVariants.add(l);
			}
			else
				list.add(p);
		}
		return list;
	}

	static public List<MyPair> interpretCollocations(List<MyPair> inputBigrams, WordSequences model, String marker, Similarity signifThh){
		List<MyPair> list = new ArrayList<>();
		for(MyPair p: inputBigrams) {
			Word l = model.getWord(p.first);
			Word r = model.getWord(p.second);
			if(l.toString().endsWith("_") || !l.left_of.containsKey(r) || l.left_of.get(r) < 5
					|| (l.isSplitterLeftRight(model.getFreqOfAnd()) && !r.isSplitterLeftRight(model.getFreqOfAnd()))) {
				list.add(p);
				continue;
			}
			Similarity signif = Words.getBigramSignif(l, r);
			if(signif.low() >= signifThh.low() && signif.high() > signifThh.high()) 
				list.add(new MyPair(p.first+marker, p.second));
			else
				list.add(p);
		}
		return list;
	}
	
	static public List<MyPair> interpretMorphCollocations(List<MyPair> inputBigrams, WordSequences model, String marker, Similarity signifThh){
		List<MyPair> list = new ArrayList<>();
		for(MyPair p: inputBigrams) {
			Word l = model.getWord(p.first);
			Word r = model.getWord(p.second);
			if(l.toString().endsWith("_") ||r.getFlex() == null || ( r.getMorphParadigm() == null)) { //r.getFlex().equals("_")&&
				list.add(p);
				continue;
			}
			boolean bigramAdded = false;
			Similarity signif;
			

			if(l.getFlex()!=null && l.getMorphParadigm()!=null && !bigramAdded) {
				signif = Words.getBigramSignif(model.getWord(l.getMorphParadigm().getLabel()), model.getWord(r.getMorphParadigm().getLabel()) );

				if(signif.low() >= signifThh.low() && signif.high() > signifThh.high()) {
					list.add(new MyPair(p.first+"_m"+marker+signif.left+marker+"r"+signif.right+"_", p.second));
					bigramAdded = true;
				}
				else {
					signif = Words.getBigramSignif(model.getWord(l.getFlexLabel()), model.getWord(r.getFlexLabel()) );
					if(signif.low() >= signifThh.low() && signif.high() > signifThh.high()) {
						list.add(new MyPair(p.first+"_f"+marker+signif.left+marker+"r"+signif.right+"_", p.second));
						bigramAdded = true;
					}
				}
			}
			if(!bigramAdded) {
				signif = Words.getBigramSignif(l, model.getWord(r.getMorphParadigm().getLabel()) );
				if(signif.low() >= signifThh.low() && signif.high() > signifThh.high() && !bigramAdded) {
					list.add(new MyPair(p.first+marker+signif.left+marker+"r"+signif.right+"_", p.second));
					bigramAdded = true;
				}else {
					signif = Words.getBigramSignif(l, model.getWord(r.getFlexLabel()) );
					if(signif.low() >= signifThh.low() && signif.high() > signifThh.high()) {
						list.add(new MyPair(p.first+marker+signif.left+marker+"r"+signif.right+"_", p.second));
						bigramAdded = true;
					}
				}
			}
			
			if(!bigramAdded)
				list.add(p);
		}
		return list;
	}
	
	static public List<MyPair> interpretPredArgWeak(List<MyPair> inputBigrams, WordSequences model, String marker){
		List<MyPair> list = new ArrayList<>();
		for(MyPair p: inputBigrams) {
			if(p.first.endsWith("_")) {
				list.add(p);
				continue;
			}
			List<Integer> zahlen = Words.getZahlen(model.getWord(p.first), true);
			String interpretL = Words.interpretContextSignificance(zahlen);
			 zahlen = Words.getZahlen(model.getWord(p.second), false);
			String interpretR = Words.interpretContextSignificance(zahlen);
			if(interpretL.equals(Words.WEAK_PRED) && (interpretR.equals(Words.STRONG_ARG)||interpretR.equals(Words.UNK))) 
				list.add(new MyPair(p.first+marker, p.second));
			else
				list.add(p);
		}
		return list;
	}
	
	static public List<MyPair> interpretPredArgStrong(List<MyPair> inputBigrams, WordSequences model, String marker){
		List<MyPair> list = new ArrayList<>();
		for(MyPair p: inputBigrams) {
			if(p.first.endsWith("_")) {
				list.add(p);
				continue;
			}
			List<Integer> zahlen = Words.getZahlen(model.getWord(p.first), true);
			String interpretL = Words.interpretContextSignificance(zahlen);
			 zahlen = Words.getZahlen(model.getWord(p.second), false);
			String interpretR = Words.interpretContextSignificance(zahlen);
			if(interpretL.equals(Words.STRONG_PRED) && (interpretR.equals(Words.STRONG_ARG)||interpretR.equals(Words.UNK))) 
				list.add(new MyPair(p.first+marker, p.second));
			else
				list.add(p);
		}
		return list;
	}
	
	//idea: for phrae building: try to recignize strong predicates(article & co) and strong args(nouns, proper nouns)
	static public void findPredArgStrong(WordSequences model, int start, int howmany) {
		int count = 0;
		for(Word w: model.idx().getSortedWords()) {
			count++;

			if(count < start) continue;
//			findPredArgStrongWord(w,false);
//			findPredArgStrongWord(w,true);
			String signifRight = TestIdeaPhrase.findPredArgStrongWord(w,false);
			String signifLeft = TestIdeaPhrase.findPredArgStrongWord(w,true);
			System.out.println(signifLeft + "\t\t" + signifRight);

			if(count > howmany) break;
		}
	}

	public static String findPredArgStrongWord(Word w, boolean left) {
		List<Integer> zahlen = Words.getZahlen(w, left);
		String interpret = Words.interpretContextSignificance(zahlen);
		String leftright = left ? "L" : "R";
		double coef = left ? w.getCoef(true) : w.getCoef(false);
		return interpret + leftright + "\t" + coef + "\t" +w.toString() + "\t" + zahlen.toString();
	}
	
	

	//this as bad
	static public void findAgreement(WordSequences model) {
		Map<String,Set<String>> map = new HashMap<>();

		for(Word w: model.idx().getSortedWords()) {
			if(w.freq() < 50) break;
			if(w.toString().matches("m[0-9]+")) continue;
			if(Words.isStopword(w.toString())) continue; //AAA, EEE; ZZZ, doctitle
			if(w.getRoot() == null) continue;
			fillAgreementMap(w,map,model);
		}
		analyzeAgreementMap(map);
	}

	private static void analyzeAgreementMap(Map<String, Set<String>> map) {

//		for(String cont_flex: map.keySet()) {
//			if(map.get(cont_flex).size() < 100) continue;
//				System.out.println(cont_flex + "\t" + map.get(cont_flex).size() + "\t" 
//						+  map.get(cont_flex).toString());
//		}
		Map<String, Set<String>> cmap = new HashMap<>();
		for(String root: map.keySet()) {
			Set<String> set = map.get(root);
			List<String> list = ListOps.of(set);
			Collections.sort(list);
			String setStr = list.toString();
			if(cmap.get(setStr) == null) cmap.put(setStr, new HashSet<>());
			cmap.get(setStr).add(root);
		}
		for(String set: cmap.keySet()) {
			if(cmap.get(set).size() > 2)
				System.out.println(set + "\t" + cmap.get(set).size() + "\t" + cmap.get(set).toString());
		}
	}

	private static void fillAgreementMap(Word w, Map<String, Set<String>> map,WordSequences model) {
		if(w.getRoot() == null) return;
		if(w.getMorphParadigm() == null) return;
		for(Word left: w.right_of.keySet()) {
			String root = w.getRoot();
			String flex = "f_"+ w.getFlex();
			String l_context = null;
			if(left.getMorphParadigm() != null)
				l_context = "f_"+ left.getFlex();
//			if(left.isSplitterLeftRight())
			if(Words.getSignifOfLeft(left, w) > 0.01)
				l_context = left.toString();
			if(l_context == null) continue;
			String context_flex_pair = l_context+"#"+flex;
			
//			if(map.get(context_flex_pair) == null) map.put(context_flex_pair, new HashSet<String>());
//			MapsOps.addStringToValueSet(context_flex_pair, map, root);
			if(map.get(root) == null) map.put(root, new HashSet<String>());
			MapsOps.addStringToValueSet(root, map, context_flex_pair);
		}
	}

	public static List<List<Collocation>> getCollocationsFromBigram(List<MyPair> bigrsInput, WordSequences model) {
		List<List<Collocation>> list = new ArrayList<>();
		for(MyPair p: bigrsInput) {
			Set<Word> leftWords = model.getWord(p.first).getWordCats(model);
			Set<Word> rWords = model.getWord(p.second).getWordCats(model);
			List<Collocation> collList = Words.getListCollocations(leftWords, rWords);
			list.add(collList);
		}
		return list;
	}

	public static List<MyPair> interpretCoef(List<MyPair> bigrs, WordSequences model, String marker) {
		List<MyPair> list = new ArrayList<>();
		for(MyPair p: bigrs) {
			Word l = model.getWord(p.first);
//			Word r = model.getWord(p.second);
			if(l.toString().endsWith("_") ) {
				list.add(p);
				continue;
			}
			
			double coefL = l.getCoef(true);
//			double coefR = r.getCoef(false);
			list.add(new MyPair(p.first+marker+coefL+marker
//					+"r"+coefR
					+"_", p.second));
		}
		return list;	
		}
	
	
	public static List<MyPair> interpretSplitter(List<MyPair> bigrs, WordSequences model, String marker) {
		List<MyPair> list = new ArrayList<>();
		for(MyPair p: bigrs) {
			Word l = model.getWord(p.first);
			Word r = model.getWord(p.second);
			if(l.toString().endsWith("_") ) {
				list.add(p);
				continue;
			}
			
			if(!l.isSplitterLeftRight(model.getFreqOfAnd()) && r.isSplitterLeftRight(model.getFreqOfAnd())) {
				list.add(p);
			}else {
				list.add(new MyPair(p.first+marker, p.second));
			}
		}
		return list;	
		}
	
	
	public static List<MyPair> interpretSignificance(List<MyPair> bigrs, WordSequences model, String marker) {
		List<MyPair> list = new ArrayList<>();
		for(MyPair p: bigrs) {
			Word l = model.getWord(p.first);
			Word r = model.getWord(p.second);
			if(l.toString().endsWith("_") ) {
				list.add(p);
				continue;
			}
			Similarity signif = Words.getBigramSignif(l, r);
			double coefL = signif.left;
			double coefR = signif.right;
			list.add(new MyPair(p.first+marker+coefL+marker+"r"+coefR+"_", p.second));
		}
		return list;	
		}

	public static List<MyPair> interpretCollocationsLeftRight(List<MyPair> bigrs, WordSequences model, String marker, double thh) {
		List<MyPair> list = new ArrayList<>();
		for(MyPair p: bigrs) {
			Word l = model.getWord(p.first);
			Word r = model.getWord(p.second);
			if(l.toString().endsWith("_") ) {
				list.add(p);
				continue;
			}
			boolean lIsleft = l.isLeftCollocWord(thh) >= 0.5;
			boolean rIsright = r.isLeftCollocWord(thh) < 0.5;
			
			if(l.isSplitterLeftRight(model.getFreqOfAnd())) 
				list.add(new MyPair(p.first+marker, p.second));
			else if(!l.isSplitterLeftRight(model.getFreqOfAnd()) && r.isSplitterLeftRight(model.getFreqOfAnd()))
				list.add(new MyPair(p.first, "|_"+p.second));
			else if(lIsleft && l.features.containsKey(Words.PHRASE_END_LABEL) && !rIsright && r.features.containsKey(Words.PHRASE_START_LABEL))
				list.add(new MyPair(p.first+"_|", p.second));
			else if(lIsleft && rIsright) 
					list.add(new MyPair(p.first+marker+l.isLeftCollocWord(thh)+"_", p.second));
			else list.add(new MyPair(addCollocInfo(l, thh), addCollocInfo(r, thh)));
		}
		return list;	
		}

	private static String addCollocInfo(Word w, double thh) {
//		if(w.isLeftCollocWord()==0.0) return w.toString();
//		else
			if(w.isLeftCollocWord(thh)>=0.5) return w.toString()+"_"+w.isLeftCollocWord(thh);
		else return w.isLeftCollocWord(thh)+"_"+w.toString();
	}


}
