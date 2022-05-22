package modelparts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import model.MorphModel;
import model.WordSequences;
import model.Words;
import modelutils.Cluster;
import modelutils.ContextStats;
import tokenizer.TestTokenizer;
import util.ListOps;
import util.MyPair;
import util.MyPairWord;
import util.MyUtils;

public class Word extends LangStructure implements Comparable<Word>{
	
	public static final String M_ZERO = "m_Zero";
	public Set<Word> paradigmWords = new HashSet<>(0);
	public Map<String,Double> features = new HashMap<>(1);

	public Word(int id, String text) {
		super(id, text);
//		if(id > 300000) 
//			System.out.println("ADDWORD: "+id+"\t"+text);
	}
	public HashMap<Word,Double> left_of = new HashMap<>(1);
	public HashMap<Word,Double> right_of = new HashMap<>(1);

	public Word prev = null;
	public Word next = null;

	public void fillLeftOf(Word w) {
		fillLeftOf(w, 1.0);
	}
	
	public void fillRightOf(Word w) {
		fillRightOf(w, 1.0);
	}

	public void fillLeftOf(Word w, double value) {
		double newfreq = this.left_of.containsKey(w) ? this.left_of.get(w)+value : value;
		this.left_of.put(w, newfreq);
	}
	
	public void fillLeftOfCats(Word w, WordSequences model) {
		Set<Word> cats = w.getWordCats(model);
		for(Word cat: cats) {
			if(!cat.toString().contains("_")) continue;
			double newfreq = this.left_of.containsKey(cat) ? this.left_of.get(cat)+1.0 : 1.0;
			this.left_of.put(cat, newfreq);
		}
	}
	
	public void fillRightOf(Word w, double value) {
		double newfreq = this.right_of.containsKey(w) ? this.right_of.get(w)+value : value;
		this.right_of.put(w, newfreq);
	}
	
	public void fillRightOfCats(Word w, WordSequences model) {
		Set<Word> cats = w.getWordCats(model);
		for(Word cat: cats) {
			if(!cat.toString().contains("_")) continue;
			double newfreq = this.right_of.containsKey(cat) ? this.right_of.get(cat)+1.0 : 1.0;
			this.right_of.put(cat, newfreq);
		}
	}
	
	public void deleteLeftOf(Word toDelete, double value) {
		if(this.left_of.containsKey(toDelete)) {
			double newfreq =  this.left_of.get(toDelete)-value;
			if(newfreq < 1) this.left_of.remove(toDelete);
			else this.left_of.put(toDelete, newfreq);
		}
	}
	
	public void deleteRightOf(Word toDelete, double value) {
		if(this.right_of.containsKey(toDelete)) {
			double newfreq =  this.right_of.get(toDelete)-value;
			if(newfreq < 1) this.right_of.remove(toDelete);
			else this.right_of.put(toDelete, newfreq);
		}
	}
	
	
	
	
	@Override
	public int compareTo(Word w) {
		if(this.freq() < w.freq())
			return 1;
		if(this.freq() > w.freq())
			return -1;
		return 0;
	}
	
	@Override
	public boolean equals(Object other) {
		if(!other.getClass().equals(this.getClass())) return false;
		return this.label.equals( ((Word)other).label);
	}
	
    @Override
    public int hashCode() {
        return Objects.hashCode(this.label);
    }
    
	public String toString() {
			return this.label;
	}

	private String root = null;
	private String flex = null;
	private String suff = null;
	
	public String setRoot(String root) {
		this.root = root;	
//		root.seenWords.add(this);
		return this.root;
	}
	
	public String getRoot() {
		return this.root ;		
	}
	
	public void setFlex(String flex) {
		this.flex = flex;		
	}
	
	public String getFlex() {
		return this.flex ;		
	}
	public String getFlexNotNull() {
		if(this.flex == null) return "fZero";
		return this.flex ;		
	}
	
	
	// CONTEXT Statistics
	
	private double coefL = -1;
	private double coefR = -1;
	public double meanL = -1;
	public double meanR = -1;
	private double coefL1 = -1;
	private double coefR1 = -1;
	private int splitterInfo = -1;
	
	/**
	 * computes and writes the statisticValues: coef
	 * coef for a word which is a left context is computed: take meanL for this word, then compare it with the meanR of each context. 
	 * sum all cases where this meanL(word) > meanR(context). coef = sum/all context size. 
	 * @param left
	 * @return
	 */
	public double getCoef(boolean left) {
		if(left) {
			if(this.coefL < 0) this.coefL = ContextStats.computeContextsCoef(this, left);
			return this.coefL;
		}else {
			if(this.coefR < 0) this.coefR = ContextStats.computeContextsCoef(this, left);
			return this.coefR;
		}
	}
	
	/**
	 * computes and writes the statisticValues: coef
	 * coef for a word which is a left context is computed: take meanL for this word, then compare it with the meanR of each context. 
	 * sum all cases where this meanL(word) > meanR(context). coef = sum/all context size. 
	 * @param left
	 * @return
	 */
	public double getCoefOneDirection(boolean left) {
		if(left) {
			if(this.coefL1 < 0) this.coefL1 = ContextStats.computeContextsCoefOneDirection(this, left);
			return this.coefL1;
		}else {
			if(this.coefR1 < 0) this.coefR1 = ContextStats.computeContextsCoefOneDirection(this, left);
			return this.coefR1;
		}
	}
	
	public boolean isKnownSplitter() {
		if(this.splitterInfo == 1) return true;
		return false;
	}
	
	public boolean isSplitterLeftRight(double freqOfAND) {
		if(this.splitterInfo  == 0) return false;
		if(this.splitterInfo == 1) return true;
		boolean info = getSplitterLeftRight(freqOfAND);
		this.splitterInfo = info ? 1 : 0;
		return info;
	}
	
	private boolean getSplitterLeftRight(double freqOfAND) {
		if(this.freq() < freqOfAND / 100) return false;
		if(this.toString().startsWith("AAANF")||this.toString().startsWith("EEEND")) return false;
		if(this.toString().equals("ZZZAHL")) return false;
//		if(this.toString().matches("und|and|en|і|й|та")) return false;
		if(this.toString().matches("[smfb]_.+")) return false;
		//words after splitter, which are independent --> group(4) with splitter signif < 0.0011
		//usually splitter is significant to its context and this group should be very small: < 5%
		double percentOfLeftIndependent = Words.percentOfLeftIndependent(this);
		if(percentOfLeftIndependent > 0.05) return false;
		return this.getCoef(true) > 0.95 && this.getCoef(false) > 0.8;
	}
	
	public boolean isPredicativeLeft(double freqOfAND) {
//		if(this.freq() < freqOfAND / 100) return false;
		if(this.toString().startsWith("AAANF")||this.toString().startsWith("EEEND")) return false;
		if(this.toString().equals("ZZZAHL")) return false;
//		if(this.toString().matches("und|and|en|і|й|та")) return false;
		if(this.toString().matches("[smfb]_.+")) return false;
		//words after splitter, which are independent --> group(4) with splitter signif < 0.0011
		//usually splitter is significant to its context and this group should be very small: < 5%
//		return this.getCoef(true) < 0.95 || this.getCoef(false) < 0.8;
		return true;
	}
	
	public boolean isPredicativeRight(double freqOfAND) {
//		if(this.freq() < freqOfAND / 100) return false;
		if(this.toString().startsWith("AAANF")||this.toString().startsWith("EEEND")) return false;
		if(this.toString().equals("ZZZAHL")) return false;
//		if(this.toString().matches("und|and|en|і|й|та")) return false;
		if(this.toString().matches("[smfb]_.+")) return false;
		//words after splitter, which are independent --> group(4) with splitter signif < 0.0011
		//usually splitter is significant to its context and this group should be very small: < 5%
		return this.getCoef(true) < 0.3 && this.getCoef(false) > 0.7;
	}

	private Cluster cluster = null;
	public void setCluster(Cluster bestCluster) {
		this.cluster = bestCluster;
	}
	public Cluster getCluster() {
		return this.cluster;
	}
	
	private MorphParadigm mpar = null;
//	public void setMorphParadigm(MorphParadigm bestCluster) {
//		this.mpar = bestCluster;
//	}	
	
	public void changeMorphParadigm(MorphParadigm mp) {
		
		if(this.mpar != null) {
			this.mpar.deleteWord(this);
			this.mpar.addFreq(-1.0);
			if(this.mpar.getFreq()<-1)
				System.out.println("LOOK: "+this.mpar.getLabel()+" "+this.mpar.getFreq()+" " + this.mpar.getSortedFlex());
			if(this.mpar.getWords().contains(this))
				this.mpar.getWords().remove(this);
		}
		if(mp != null) {
			mp.addFreq();
			mp.addWord(this);
		}
		this.mpar = mp;
	}
	
	public MorphParadigm getMorphParadigm() {
		return this.mpar;
	}
	
	private String flexMPlabelAmbig = null;
	public void setMorphParadigmAmbig(String flexMPlabel) {
		this.flexMPlabelAmbig = flexMPlabel;
	}
	public String getFlexMPlabelAmbig() {
		return this.flexMPlabelAmbig;
	}
	
	public String syntLabel = null;
	public String semLabel = null; 
	
	public String getMorphLabelNotNull() {
		if(this.mpar == null) return M_ZERO;
		else return this.mpar.getLabel();
	}
	
	public MyPair prefRootFlex = null;
	public MyPair sufRootFlex = null;

	private List<Word> seenPredicatsLeft = null;
	private List<Word> seenPredicatsRight = null;
	public boolean clusterWithFlex = false;
	public List<Integer> zalen = null;

	public List<Word> getSeenPredicats(boolean leftPred) {
		if(leftPred) {
			if(this.seenPredicatsLeft == null )
				this.seenPredicatsLeft = new ArrayList<>(1);
			return this.seenPredicatsLeft;
		}else {
			if(this.seenPredicatsRight == null )
				this.seenPredicatsRight = new ArrayList<>(1);
			return this.seenPredicatsRight;
		}
		
	}

	public List<Word>  setSeenPredicatsFromString(List<String> seenPredsList, WordSequences wsmodel, boolean isLeftPred) {
		for(String s: seenPredsList) {
				addSeenPredicats(isLeftPred, wsmodel.getWord(s), wsmodel);
		}
		return getSeenPredicats(isLeftPred);
	}
	
	public List<Word> addSeenPredicats(boolean leftPred, Word w, WordSequences wsmodel) {
		if(leftPred) {
			if(this.seenPredicatsLeft == null || this.seenPredicatsLeft.contains(wsmodel.getWord(NO_PREDS_FOUND)))
				this.seenPredicatsLeft = new ArrayList<>(1);
			this.seenPredicatsLeft.add(w);
			return this.seenPredicatsLeft;
		}else {
			if(this.seenPredicatsRight == null || this.seenPredicatsRight.contains(wsmodel.getWord(NO_PREDS_FOUND)))
				this.seenPredicatsRight = new ArrayList<>(1);
			this.seenPredicatsRight.add(w);
			return this.seenPredicatsRight;
		}
	}
	
	public void setNotFoundPredicats(boolean leftPred, WordSequences wsmodel) {
		if(leftPred) 
			this.seenPredicatsLeft = ListOps.of(wsmodel.getWord(NO_PREDS_FOUND));
		else
			this.seenPredicatsRight = ListOps.of(wsmodel.getWord(NO_PREDS_FOUND));
	}

	public static final String NO_PREDS_FOUND = "noPreds found";
	
	private  List<MyPairWord> bestContextsRight = null;
	private  List<MyPairWord> bestContextsLeft = null;
	public  List<MyPairWord> getBestContexts(boolean left, int howmany, String regexForParadimFilter, WordSequences model, boolean allowFreqOne){
		if(left ) {
			if( bestContextsLeft == null) 			bestContextsLeft = Words.computeNbestContexts(this, left, howmany, regexForParadimFilter, model, allowFreqOne);
			return bestContextsLeft;
		} else
		if(bestContextsRight == null) 			bestContextsRight = Words.computeNbestContexts(this, left, howmany, regexForParadimFilter, model, allowFreqOne);
		return bestContextsRight;
			}
	
	public  List<String> getBestContextsComputeNewAsList(boolean left, int howmany, 
	    String regexForParadimFilter, WordSequences model, boolean allowFreqOne, int contextcount){
		List<String> bestContexts = new ArrayList<>();
		for(MyPairWord key: getBestContextsComputeNew(left, contextcount, regexForParadimFilter, model, allowFreqOne)){
			bestContexts.add(key.left.toString());
		}
		return bestContexts;
	}
	public  List<MyPairWord> getBestContextsComputeNew(boolean left, int howmany, String regexForParadimFilter, WordSequences model, boolean allowFreqOne){
		if(left ) {
			return Words.computeNbestContexts(this, left, howmany, regexForParadimFilter, model, allowFreqOne);
		} else
		return 		Words.computeNbestContexts(this, left, howmany, regexForParadimFilter, model, allowFreqOne);
			}
	public List<MorphParadigm> ambigParadigms = null;
//	public Set<MorphParadigm> ambigParadigms2 = null;

	public double getVar(boolean left) {
		if(left)
			return (double)this.left_of.size()/this.freq();
		else
			return (double)this.right_of.size()/this.freq();
	}
	
	public double getVarOfAll(boolean left, double allUniqs) {
		if(left)
			return (double)this.left_of.size()/allUniqs;
		else
			return (double)this.right_of.size()/allUniqs;
	}

	public String getFlexLabel() {
		if(this.flex == null) return null;
		if(this.mpar == null) return null;
		
		return MorphModel.getFlexLabel(this.flex, this.mpar.getLabel(), MorphModel.FPREF);
	}
	
	// Semantics
	private double isLeftCollocWord = -1.0;
	public double argCoef = -1.0;
	public List<MyPairWord> contextVectorLeft = null;
	public List<MyPairWord> contextVectorRight = null;
	public Map<Word, Double> seenArgs = null;
	public int seenAsPred = 0;
	public Set<Word> predVariants = new HashSet<>(1);
	
	public void resetCollocWord() {
		isLeftCollocWord = -1.0;
	}
	
	public double isLeftCollocWord() {
		return isLeftCollocWord(0.01);
	}
	
	public double isLeftCollocWord(double collocthh) {
		if(this.isLeftCollocWord > -1)return this.isLeftCollocWord;
		List<Collocation> colls = Words.getWordCollocations(this, collocthh, 2);
		if(colls.size()==0) {
			this.isLeftCollocWord = 0.0; 
			return 0.0;
		}
		Collections.sort(colls);
//		if(colls.size()>5 && colls.get(0).sim.high() > 0.09) colls = Collocation.getSubCollocs(colls, 0.1);

		double sumLeft = 0.0;
		for(Collocation c: colls) {
			if(c.left.equals(this.toString())) sumLeft ++;
		}
		double coef = (double)sumLeft/colls.size();
//		if(coef > 0.4 && coef < 0.6) {
//			if(colls.get(0).left.equals(this.toString())) //boost first collocation as most important, probably better boost colloc 0..5
//				sumLeft ++;
//			else sumLeft --;
//		}
		if(coef == 0.0) coef = 0.05; //to differ from 0.0 as fo no collocations
		this.isLeftCollocWord =  MyUtils.rdouble(coef); 
		return this.isLeftCollocWord;
	}

	/**
	 * has < 2 contexts left or right OR has freq < 5
	 * @return
	 */
	public boolean isSeldom() {
		if(this.freq()<5) return true;
		if(this.left_of.size() < 2 && this.right_of.size() < 2) return true;
		return false;
	}
	
	public boolean isSeldom2(double freqOfAnd) {
		if(this.freq() < (freqOfAnd / 10000)) return true;
		return false;
	}

	public double argCoef() {
		if(this.argCoef >= 0) return this.argCoef;
		List<Collocation> colls = Words.getWordCollocations(this, 0.001, 1);
		double sumArg = 0.0;
		for(Collocation c: colls) {
			if(c.left.equals(this.toString()) && c.sim.left < c.sim.right) 
				sumArg ++;
			else if(c.right.equals(this.toString()) && c.sim.right < c.sim.left) 
				sumArg ++;
		}
		double coef = (double)sumArg/colls.size();	
		this.argCoef = coef;
		return this.argCoef;
	}

	public List<MyPair> getContextVector(boolean left, double signif, int howmany) {
		HashMap<Word, Double> contexts;
		double wfreq = this.freq();
		if (left)		contexts = this.right_of;
		else			contexts = this.left_of;
		List<MyPair> mapContexttWordSignif = new ArrayList<>();

		for (Word cont : contexts.keySet()) {
			double freq = contexts.get(cont); //bigram freq
			double signifCOnt = MyUtils.rdouble(freq / wfreq);
			mapContexttWordSignif.add(new MyPair(cont.toString(),"", signifCOnt));
		}	
		Collections.sort(mapContexttWordSignif);
		List<MyPair> vector = new ArrayList<>();
		int c = 0;
		for(MyPair p: mapContexttWordSignif) {
			if(c > howmany) break;
			if( p.freq > signif)
				vector.add(p);
			c++	;
		}
		return vector;
	}

	public boolean isParadigmWord() {
		if(this.toString().length() > 1 && this.toString().charAt(1) == '_') 
			return true;
		if(this.toString().equals(TestTokenizer.AAANF) || this.toString().equals(TestTokenizer.EEEND)
				|| this.toString().equals("doctitle")) return true;
		
		return false;
	}
	
	public Set<Word> getWordCats(WordSequences model){
		Set<Word> catset = new HashSet<>();
		catset.add(this);
		if(this.isSplitterLeftRight(model.getFreqOfAnd())) catset.add(model.getWord(Words.SPLITTER_WORD_LABEL));
		if(this.mpar!=null) {
			catset.add(model.getWord(this.mpar.getLabel()));
			if(this.getFlex() != null) catset.add(model.getWord(this.getFlexLabel()));
		}
		if(this.semLabel != null)
			catset.add(model.getWord(this.semLabel));
		if(model.idx().semPars.containsKey(this)) {
			for(SemParadigm sp: model.idx().semPars.get(this)) {
				catset.add(model.getWord(sp.label));
			}
		}
		if(this.syntLabel != null)
			catset.add(model.getWord(MorphModel.MPREF+this.syntLabel));
		
		if(this.suff != null)
			catset.add(model.getWord(this.getSuffixCatLabel()));
		return catset;
	}

	public String getSuffixCatLabel() {
		if(this.suff == null) return null;
		return "x_" + this.suff;
	}

	public Set<String> getStringWordCats(Word w){
		Set<String> catset = new HashSet<>();
		catset.add(w.toString());
		if(w.isKnownSplitter()) catset.add(Words.SPLITTER_WORD_LABEL);
		if(w.mpar!=null) {
			catset.add(w.mpar.getLabel());
			if(w.getFlex() != null) catset.add(w.getFlexLabel());
		}
		if(w.semLabel != null)
			catset.add(w.semLabel);
		if(w.syntLabel != null)
			catset.add(MorphModel.MPREF+w.syntLabel);
		return catset;
	}
	public List<MyPairWord> getExpectationsLeftRightSorted(boolean left, double thh, WordSequences model){
		return Words.getExpectationsLeftRightSorted(this, left, thh, model);
	}

	public boolean isCat() {
		if(this.toString().contains("_") || this.toString().contains("AAANF") || this.toString().contains("EEEND")
				|| this.toString().contains("ZZZAHL")) return true;
		return false;
	}

	public void setSuffix(String suffix) {
		this.suff = suffix;
	}

	public String getSuffix() {
		return this.suff;
	}

	public void setFlexRootMPar(String flex, MorphParadigm mp, WordSequences model, boolean stopRecursion) {
		if(this.syntLabel != null) return;
		if(flex != null && flex.length() >= this.toString().length()) return;
		if(flex != null) {
			this.setFlex(flex);
			String root = Words.computeRootFromFlex(this, flex); 
			if(root != null && root.length() == 1) // too small root
			{
				this.flex = null;
				this.root = null;
				this.mpar = null;
				 return;
			}
			this.setRoot(root);
			Root r = model.idx().getRoot(root);
			r.addFlex(model.idx().getFlex(flex));
			if(model.idx().prefixBucket != null) model.idx().addToPrefixBucket(this);
		}
		this.changeMorphParadigm(mp);
		//set mp for other words with this root
		if(mp == null || stopRecursion) return;
		for(Flexion f: mp.getFlexes()) {
			String fstring = f.toString().equals("_") ? "" : f.toString();
			if(model.idx().containsWord(root+fstring)) {
				Word rootFlexWord = model.getWord(root+fstring);
				if(rootFlexWord.isSplitterLeftRight(model.getFreqOfAnd())) continue;
				if(rootFlexWord.freq() > 2 ) 
					rootFlexWord.setFlexRootMPar(f.toString(), mp, model, true);
			}
		}
	}
	
	public static List<String> getBestContextsPaired(){
		List<String> bestC = new ArrayList<>();
		
		return bestC;
	}

	public String bestMpar = null;
	public String bestSpar = null;
	public void writeTags(MyPair morph, MyPair synt) {
		if(morph != null) bestMpar = morph.first;
		if(synt != null) bestSpar = synt.first;
	}

	public void setWmaps(WordSequences model) {
		WordMaps wmaps = new WordMaps(this, model);
		this.wmaps = wmaps;
	}

	public WordMaps wmaps() {
		return wmaps;
	}

	private WordMaps wmaps = null;





	public String getFirstVotedMP(List<MyPair> votes) {
		if(ListOps.notNullEmpty(votes))
			return votes.get(0).first;
		return null;
	}

	public List<MyPair> getMPVotes() {
		if(wmaps == null) return null;
		wmaps.vote();
		return wmaps.getVotes_mp();
	}

}
