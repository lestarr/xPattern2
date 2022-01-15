package modelparts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.util.Pair;
import model.MorphModel;
import model.MorphVectorAnalyzer;
import model.WordSequences;
import model.Words;
import modelutils.Cluster;
import tokenizer.TestTokenizer;
import util.MyPair;
import util.MyPairWord;
import util.MyUtils;
import util.SetOps;

public class Phrases {
	
	public static boolean peakIsLow = false;
	
	static public List<MyPairWord> getBigrams(String sent, String lang, WordSequences model){
		List<MyPairWord> list = new ArrayList<>();
		String prev = null; 
		for(String curr: TestTokenizer.getTokens(sent, lang, true, true)) {
			if(prev == null) {
				prev = curr;
				continue;
			}
			MyPairWord p = new MyPairWord(model.getWord(prev), model.getWord(curr));
			prev = curr;
			list.add(p);
		}
		return list;
	}
	

	public static List<MyPairWord> interpretExpectations(List<MyPairWord> bigrs, WordSequences model,
			double thh, boolean getLowCollocationSim, boolean print) {
		
		List<MyPairWord> list = new ArrayList<>();
		for(MyPairWord p: bigrs) {
			double matchingExp = checkBigramExpectations(p, model, thh, getLowCollocationSim, print);
//			double matchingExp = checkBigramExpectationsVector(p, model, thh,print);
			p.signif = matchingExp;
			list.add(p);
		}
		return list;	
	}
	public static List<MyPairWord> interpretParadigmExpectations(List<MyPairWord> bigrs, WordSequences model,
			 boolean print) {
		
		List<MyPairWord> list = new ArrayList<>();
		for(MyPairWord p: bigrs) {
			checkBigramParadigmExpectations(p, model, 1, print);
			checkBigramCollocationExpectations(p, model, 2, print);
			list.add(p);
		}
		return list;	
	}
	
	public static void interpretBigramsMainScript(List<MyPairWord> bigrs, WordSequences model,
			boolean print) {
		interpretPredicatsAndTerminals(bigrs,model,1,false);
		interpretPredicatsAndTerminals(bigrs,model,1,false);
		interpretPredicatsAndTerminals(bigrs,model,1,false);
		interpretPredicatsAndTerminals(bigrs,model,1,false);
		
		//do the real collocations
		for(MyPairWord p: bigrs) 
			checkBigramCollocationExpectations(p, model, 2, print);
	}
	
	private static List<MyPairWord> interpretPredicatsAndTerminals(List<MyPairWord> bigrs, WordSequences model, int round,
			boolean print) {
		MyPairWord prevBigram = null;
		for(MyPairWord p: bigrs) {
			if(prevBigram == null) {
				prevBigram = p;
				continue;
			}
			checkBigramPredicatsAndTerminals(prevBigram, p, model, round, print);
			prevBigram = p;
		}
		checkBigramPredicatsAndTerminals(prevBigram, null, model, round, print);
		return bigrs;
	}

	private static void checkBigramPredicatsAndTerminals(MyPairWord bigram, MyPairWord bNext, WordSequences model, int peakround, boolean print) {
		if(skipBigram(bigram)) return;
		Word l = bigram.left;
		Word r = bigram.right;
		//check left word of bigram, if predicative mp or high coef.left
		if(checkWordIsPredicative(l,model)) {
			if(l.toString().equals("auf")||l.toString().equals("eine")||l.toString().equals("einen")||l.toString().equals("einer"))
				System.out.println();
			//check right word if terminal mp
			if(checkWordIsTerminal(r,model)) {
				bigram.peakInRound = peakround;
				bigram.hasTerminal = true;
				return;
			}
			if(checkWordIsTerminalViaCoef(r, model, bNext)) {
				bigram.peakInRound = peakround;
				bigram.hasTerminal = true;
				return;
			}
			//check next bigram if hasTerminal
			if(bNext.hasTerminal) {
				bigram.peakInRound = peakround;
				bigram.hasTerminal = true;
				return;
			}
		}
	}

	private static boolean checkWordIsTerminalViaCoef(Word r, WordSequences model, MyPairWord bNext) {
		//this word has not to be predicative, has to have low coef left and right and next word should be predicative or EEEND
		if(checkWordIsPredicative(r, model)) return false;
		if(r.getCoef(true) < 0.3 && r.getCoef(false) < 0.3) {
			if(bNext == null) return true;
			if(bNext.right == null) return true;
			if(bNext.right.toString().equals("EEEND")) return true;
			if(checkWordIsPredicative(bNext.right, model)) return true;
		}
		return false;
	}


	private static boolean checkWordIsTerminal(Word r, WordSequences model) {
		String mplabel = null;
		if(r.getMorphParadigm() == null) {
			mplabel = checkMorph2(r, model, true);
			if(mplabel == null)
				return false;
		}
		else
			mplabel = r.getMorphParadigm().getLabel();
		if(model.idx().morphTerminals.contains(mplabel)) return true; //word is not "noun" or "verb"

		return false;
	}


	private static boolean checkWordIsPredicative(Word l, WordSequences model) {
		if(l.getCoef(true) < 1.0 && l.getCoef(true) > 0.69 && !checkWordIsTerminal(l, model)) return true; //word has high coef left
		
		String mplabel = null;
		if(l.getMorphParadigm() == null) {
			mplabel = checkMorph2(l, model, true);
			if(mplabel == null)
				return false;
		}
		else
			mplabel = l.getMorphParadigm().getLabel();
		if(!model.idx().morphTerminals.contains(mplabel)) return true; //word is not "noun" or "verb"

		return false;
	}


	private static boolean skipBigram(MyPairWord bigram) {
		if(bigram.peakInRound > 0) return true;
		if(bigram.left.toString().equals("AAANF")) return true;
		if(bigram.right.toString().equals("EEEND")) return true;
		return false;
	}

	static private void checkBigramParadigmExpectations(MyPairWord bigram, WordSequences model, int peakround, boolean print) {
		if(bigram.peakInRound > 0) return;
		Word l = bigram.left;
		Word r = bigram.right;
		if(l.toString().equals("AAANF")) return ;
		if(r.toString().equals("EEEND")) return ;
		if(print) System.out.println(bigram.toString());

//		if(l.freq() == 0 && model.getWord(l.toString().toLowerCase()).freq() != 0) 
//			l = model.getWord(l.toString().toLowerCase());
//		if(r.freq() == 0 && model.getWord(r.toString().toLowerCase()).freq() != 0) 
//			r = model.getWord(r.toString().toLowerCase());
		if(l.freq() < model.getWord(l.toString().toLowerCase()).freq() ) 
			l = model.getWord(l.toString().toLowerCase());
		if(r.freq() < model.getWord(r.toString().toLowerCase()).freq() ) 
			r = model.getWord(r.toString().toLowerCase());
		
		Set<Word> set = new HashSet<>();
		String morphlabelRight = checkMorph(set, r, model);
		String morphlabelLeft = checkMorph(set, l, model);
		
		if(morphlabelLeft == null) return;
		if(model.idx().phraseExpectations.containsKey(morphlabelLeft)) {
//			if(morphlabelRight == null) bigram.peakInRound = peakround+100;
			if(model.idx().phraseExpectations.get(morphlabelLeft).contains(morphlabelRight))
				bigram.peakInRound = peakround;
		}
		
	}
	
	static private void checkBigramCollocationExpectations(MyPairWord bigram, WordSequences model, int peakround, boolean print) {
		if(bigram.peakInRound > 0) return;
		Word l = bigram.left;
		Word r = bigram.right;
		if(l.toString().equals("AAANF")) return ;
		if(r.toString().equals("EEEND")) return ;
		if(print) System.out.println(bigram.toString());

		if(l.freq() < model.getWord(l.toString().toLowerCase()).freq() ) 
			l = model.getWord(l.toString().toLowerCase());
		if(r.freq() < model.getWord(r.toString().toLowerCase()).freq() ) 
			r = model.getWord(r.toString().toLowerCase());
		
		Collocation c = Words.getCollocation(l, r);
		if(c.getFreq() > 1.0 && c.sim.high() > 0.1 && c.sim.high() < 1.0 && c.sim.low() > 0.001) {
				bigram.peakInRound = peakround;
		}
		
	}
	
	static public double checkBigramExpectations(MyPairWord bigram, WordSequences model, double thh, boolean getLowCollocationSim, boolean print) {
		double result = 0.0;
		Word l = bigram.left;
		Word r = bigram.right;
		if(l.toString().equals("AAANF")) return 0.0;
		if(r.toString().equals("EEEND")) return 0.0;
		if(print) System.out.println(bigram.toString());

		if(l.freq() == 0 && model.getWord(l.toString().toLowerCase()).freq() != 0) 
			l = model.getWord(l.toString().toLowerCase());
		if(r.freq() == 0 && model.getWord(r.toString().toLowerCase()).freq() != 0) 
			r = model.getWord(r.toString().toLowerCase());
//		Collocation c = Words.getCollocation(l, r);
//		if(c.sim.left != 1.0) result = result + c.sim.left;
//		if(c.sim.right != 1.0) result = result + c.sim.right;
		
//		List<MyPairWord> expLeft = Words.getExpectationsLeftRightSorted(l, true, thh);
		Set<Word> wordcatsRight = r.getWordCats(model);
		checkMorph(wordcatsRight, r, model);
//		result = result + addMatchingExpectations(expLeft, wordcatsRight,print);
//		result = result + addFirstExpectations(expLeft, wordcatsRight,print);

//		List<MyPairWord> expRight = Words.getExpectationsLeftRightSorted(r, false, thh);
		Set<Word> wordcatsLeft = l.getWordCats(model);
		checkMorph(wordcatsLeft, l, model);
//		result = result + addMatchingExpectations(expRight, wordcatsLeft,print);
//		result = result + addFirstExpectations(expRight, wordcatsLeft,print);
		
		//take the biggest Low value from all possible collocatiions between parts of a bigram. Use: word, f_, m_
		if(getLowCollocationSim)
			result = result + getBiggestLowCollocation(wordcatsLeft, wordcatsRight);
		else
			result = result + getBiggestHighCollocation(wordcatsLeft, wordcatsRight, false);
		if(print) System.out.println();

		return result;
	}
	
	private static String checkMorph2( Word w, WordSequences model, boolean checkFlexion) {
		if(w.toString().equals("Weite")||w.toString().equals("Mit"))
			System.out.println();
		if(w.getMorphParadigm() != null) return w.getMorphParadigm().getLabel();
		if(w.isSplitterLeftRight(model.getFreqOfAnd())) {
			if(w.syntLabel != null) return "m_"+w.syntLabel ;
			else return null;
		}
		Word tolower = model.getWord(w.toString().toLowerCase());
		if(tolower.getMorphParadigm() != null) {
			return tolower.getMorphParadigm().getLabel();
		}
		if( tolower.isSplitterLeftRight(model.getFreqOfAnd())) {
			if(tolower.syntLabel != null) {
				return "m_"+tolower.syntLabel;
			}
			return null;
		}
		
		Pair<String, Double> bestpar = MorphVectorAnalyzer.tagWordForKnownParadigms(w, model,false, Double.MAX_VALUE, 20);
		if(bestpar == null) return null;
		if(checkFlexion) {
			if(!MorphVectorAnalyzer.confirmFlexion(bestpar.getKey(), w, model))
				return null;
		}
		
		return bestpar.getKey();
	}

	
	private static String checkMorph(Set<Word> wordcats, Word w, WordSequences model) {
		if(w.toString().equals("Weite")||w.toString().equals("Mit"))
			System.out.println();
		if(w.getMorphParadigm() != null) return w.getMorphParadigm().getLabel();
		if(w.isSplitterLeftRight(model.getFreqOfAnd())) {
			if(w.syntLabel != null) return "m_"+w.syntLabel ;
			else return null;
		}
		Word tolower = model.getWord(w.toString().toLowerCase());
		if(tolower.getMorphParadigm() != null) {
			wordcats.add(tolower);
			wordcats.add(model.getWord(tolower.getMorphParadigm().getLabel()));
			wordcats.add(model.getWord(MorphModel.getFlexLabel(tolower.getFlex(), tolower.getMorphParadigm().getLabel(), MorphModel.FPREF)));
			return tolower.getMorphParadigm().getLabel();
		}
		if( tolower.isSplitterLeftRight(model.getFreqOfAnd())) {
			wordcats.add(tolower);
			if(tolower.syntLabel != null) {
				wordcats.add(model.getWord("m_"+tolower.syntLabel)); 
				return "m_"+tolower.syntLabel;
			}
			return null;
		}
		
		MyPair bestpar = MorphVectorAnalyzer.tagMorphPar(w, model, 20);
		
		if(bestpar == null) return null;
		String flexpar = bestpar.first;
		String flex = MorphModel.getFlexFromFlexPar(flexpar,MorphModel.FPREF);
		if(flex.equals("_")) flex = "";
		if(w.toString().endsWith(flex)) {
			wordcats.add(model.getWord(flexpar));
			String mparlabel = MorphModel.getMPlabelFromFlexPar(flexpar);
			wordcats.add(model.getWord(mparlabel));
			return mparlabel;
		}
		return null;
	}





	private static double getBiggestLowCollocation(Set<Word> wordcatsLeft, Set<Word> wordcatsRight) {
		double biggestLowCollocate = 0.0;
		for(Word l: wordcatsLeft) {
			if(l.toString().startsWith("s_")) continue;
			for(Word r: wordcatsRight) {
				if(r.toString().startsWith("s_")) continue;
				Collocation c = Words.getCollocation(l, r);
				if(c.sim.low() > biggestLowCollocate) biggestLowCollocate = c.sim.low();
			}
		}
		return biggestLowCollocate;
	}
	public static double getBiggestHighCollocation(Set<Word> wordcatsLeft, Set<Word> wordcatsRight, boolean print) {
		double biggestHighCollocate = 0.0;
		for(Word l: wordcatsLeft) {
			if(l.toString().startsWith("s_")) continue;
			for(Word r: wordcatsRight) {
				if(r.toString().startsWith("s_")) continue;
				Collocation c = Words.getCollocation(l, r);
				double score = c.sim.high();
				if(print && c.sim.high() > 0.1)
					System.out.println(c.toString());
				if(score == 1.0) score = c.sim.low();
				if(score > biggestHighCollocate) biggestHighCollocate = score;
			}
		}
		return biggestHighCollocate;
	}

	/**
	 * penalty score: smaller is better
	 * @param bigram
	 * @param model
	 * @param thh
	 * @param print
	 * @return
	 */
	static public double checkBigramExpectationsVector(MyPairWord bigram, WordSequences model, double thh, boolean print) {
		peakIsLow = true;
		double score = 0.0;
		Word l = bigram.left;
		Word r = bigram.right;
		if(print) System.out.println(bigram.toString());
		if(bigram.left.toString().equals("звукових") || bigram.left.toString().equals("хвиль"))
			System.out.println();
		if(l.freq() == 0 && model.getWord(l.toString().toLowerCase()).freq() != 0) 
			l = model.getWord(l.toString().toLowerCase());
		if(r.freq() == 0 && model.getWord(r.toString().toLowerCase()).freq() != 0) 
			r = model.getWord(r.toString().toLowerCase());
		
		List<MyPairWord> expLeft = Words.getExpectationsLeftRightSorted(l, true, thh, model);
		expLeft = Words.padSortAndCut(model, expLeft, 20, true, l.toString().contains("_"));
		Set<Word> wordcatsRight = r.getWordCats(model);
		score = score + Cluster.getScaledSimMeasureIntern1(MyPairWord.getListFirst(expLeft), SetOps.getStringSet(wordcatsRight));

		List<MyPairWord> expRight = Words.getExpectationsLeftRightSorted(r, false, thh, model);
		expRight = Words.padSortAndCut(model, expRight, 20, true, r.toString().contains("_"));
		Set<Word> wordcatsLeft = l.getWordCats(model);
		score = score + Cluster.getScaledSimMeasureIntern1(MyPairWord.getListFirst(expRight), SetOps.getStringSet(wordcatsLeft));
		
		if(print) System.out.println();

		return score;
	}
	
	static public List<MyPairWord> analyseBigramsForPeak(List<MyPairWord> bigrs, int round) {
		MyPairWord prev = null, curr = null;
		for(MyPairWord next: bigrs) {
			if(next.peakInRound > 0 )
				continue;
			if(prev == null) {
				prev = next;
				continue;
			}
			if(curr == null) {
				curr = next;
				continue;
			}
			
			
			if(hasPeak(prev, curr, next)) 
				curr.peakInRound = round;
			
			if(hasLow(prev, curr, next)) 
				curr.hasLow = true;
			
			prev = curr;
			curr = next;
		}
		return bigrs;
	}

	static public boolean hasPeak(MyPairWord prev, MyPairWord curr, MyPairWord next) {
		if(peakIsLow) {
			if(prev.signif > curr.signif && next.signif > curr.signif)
				return true;
		}else {
			if(prev.signif < curr.signif && next.signif <= curr.signif && curr.signif > 0.01)
				return true;
		}
		return false;
	}
	
	static public boolean hasLow(MyPairWord prev, MyPairWord curr, MyPairWord next) {
		if(peakIsLow) {
			if(prev.signif < curr.signif && next.signif < curr.signif)
				return true;
		}else {
			if(prev.signif >= curr.signif && next.signif > curr.signif )
				return true;
		}
		return false;
	}
	
	static public double checkBigramExpectationsOld(MyPairWord bigram, WordSequences model, double thh, boolean print) {
		double result = 0.0;
		Word l = bigram.left;
		Word r = bigram.right;
		if(print) System.out.println(bigram.toString());

		if(l.freq() == 0 && model.getWord(l.toString().toLowerCase()).freq() != 0) 
			l = model.getWord(l.toString().toLowerCase());
		if(r.freq() == 0 && model.getWord(r.toString().toLowerCase()).freq() != 0) 
			r = model.getWord(r.toString().toLowerCase());
		Collocation c = Words.getCollocation(l, r);
//		if(c.sim.left != 1.0) result = result + c.sim.left;
//		if(c.sim.right != 1.0) result = result + c.sim.right;
		if(c.sim.left != 1.0 && c.sim.right != 1.0 && c.sim.high() > 0.1 && c.sim.low() > 0.001) result = result + 1.0; //c.sim.left;
		
		List<MyPairWord> expLeft = Words.getExpectationsLeftRightSorted(l, true, thh, model);
		Set<Word> wordcatsRight = r.getWordCats(model);
		result = result + addMatchingExpectations(expLeft, wordcatsRight,print);
		
		List<MyPairWord> expRight = Words.getExpectationsLeftRightSorted(r, false, thh, model);
		Set<Word> wordcatsLeft = l.getWordCats(model);
		result = result + addMatchingExpectations(expRight, wordcatsLeft,print);
		
		if(print) System.out.println();

		return result;
	}
	
	
	public static double checkMorphExpectations(List<MyPairWord> bigrs, WordSequences model, double thh) {
		int sentSize = bigrs.size();
		double matchingMorphExp = 0;
		for(MyPairWord bigram: bigrs) {
		Word l = bigram.left;
		Word r = bigram.right;
		
		if(l.freq() == 0 && model.getWord(l.toString().toLowerCase()).freq() != 0) 
			l = model.getWord(l.toString().toLowerCase());
		if(r.freq() == 0 && model.getWord(r.toString().toLowerCase()).freq() != 0) 
			r = model.getWord(r.toString().toLowerCase());
		
		Set<Word> wordcatsLeft = l.getWordCats(model);
		if(containsMorphCat(wordcatsLeft)) {
			List<MyPairWord> expRight = Words.getExpectationsLeftRightSorted(r, false, thh, model);
			if(matchesMorphCat(wordcatsLeft, expRight)) {
				Set<Word> wordcatsRight = r.getWordCats(model);
				if(containsMorphCat(wordcatsRight)) {
					List<MyPairWord> expLeft = Words.getExpectationsLeftRightSorted(l, true, thh, model);
					if(matchesMorphCat(wordcatsRight, expLeft)) {
						matchingMorphExp++;
						matchingMorphExp++;
					}
				}
			}
		}

		
		}
		return matchingMorphExp; //(double)(matchingMorphExp / sentSize);	
	}


	private static boolean matchesMorphCat(Set<Word> wordcats, List<MyPairWord> exp) {
		for(MyPairWord pair: exp) {
			if(pair.left.toString().startsWith("m_") && wordcats.contains(pair.left))
				return true;
		}
		return false;
	}


	private static boolean containsMorphCat(Set<Word> wordcatsLeft) {
		for(Word w: wordcatsLeft)
			if(w.toString().startsWith("m_")) return true;
		return false;
	}
	
	static public double addFirstExpectations(List<MyPairWord> exp, Set<Word> wordcats, boolean print) {
		double result = 0.0;
		boolean seenSemExpectaions = false;
		for(MyPairWord pair: exp) {
			
			if(wordcats.contains(pair.left)) {
				result = result + pair.signif;
				break;
//				if(pair.left.toString().startsWith("b_")) { //add b_cats only 1 time
//					if(!seenSemExpectaions) {
//						result = result + 1.0; //pair.signif;
//						seenSemExpectaions = true;
//					}
//				}else
//					result = result + 1.0; //pair.signif;
//				if(print) {
//					if(!pair.left.toString().equals("AAANF") && !pair.right.toString().equals("EEEND"))
//					System.out.println("matching exp:\t" + pair.left.toString() + "\t" + pair.signif + "\t" + wordcats.toString());
//				}
			}
		}
		return result;
	}


	static public double addMatchingExpectations(List<MyPairWord> exp, Set<Word> wordcats, boolean print) {
		double result = 0.0;
		boolean seenSemExpectaions = false;
		for(MyPairWord pair: exp) {
			if(wordcats.contains(pair.left)) {
				if(pair.left.toString().startsWith("b_")) { //add b_cats only 1 time
					if(!seenSemExpectaions) {
						result = result + 1.0; //pair.signif;
						seenSemExpectaions = true;
					}
				}else
					result = result + 1.0; //pair.signif;
				if(print) {
					if(!pair.left.toString().equals("AAANF") && !pair.right.toString().equals("EEEND"))
					System.out.println("matching exp:\t" + pair.left.toString() + "\t" + pair.signif + "\t" + wordcats.toString());
				}
			}
		}
		return result;
	}

	public static String getSentFromBigrams(List<MyPairWord> bigrams, boolean addBigramSignif) {
		StringBuffer sb = new StringBuffer();
		for(MyPairWord p: bigrams) {
//			if(p.peakInRound < 0) sb.append("\n");
			
			sb.append(p.left.toString());
			if(addBigramSignif) {
				sb.append("/");
				sb.append(MyUtils.rdouble(p.signif));

			}
			if(p.hasLow) sb.append("|");
			if(p.peakInRound == 1) sb.append("_");
			if(p.peakInRound == 2) sb.append("_._");
			if(p.peakInRound > 2) sb.append("__._");
			sb.append(" ");
		}
		sb.append(bigrams.get(bigrams.size()-1).right.toString());

		return sb.toString().replaceAll("_ ", "_").replaceAll("\\|", "\n");	
	}


	public static String getSentFromBigrams1(List<MyPairWord> bigrams, String string) {
		StringBuffer sb = new StringBuffer();
		for(MyPairWord p: bigrams) {
			if(p.peakInRound > 0) sb.append("\n");
			sb.append(p.left.toString());
			sb.append("_");
			sb.append(MyUtils.rdouble(p.signif));
			sb.append(" ");
		}
		sb.append(bigrams.get(bigrams.size()-1).right.toString());

		return sb.toString();	
	}

	public static void setParadigmVectors(WordSequences model, int contextcount) {
		MorphVectorAnalyzer.collectMParVectorsFlexion(model, Words.SYNSEM_FILTER, contextcount);
	}

}
