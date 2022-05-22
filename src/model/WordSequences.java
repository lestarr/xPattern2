package model;

import java.util.*;
import java.util.Map.Entry;

import modelparts.Collocation;
import modelparts.Flexion;
import modelparts.MorphParadigm;
import modelparts.Word;
import modelutils.Cluster;
import modeltrain.SyntParVectorTrain;
import tokenizer.TestTokenizer;
import util.MapsOps;
import util.MyPair;
import util.MyPairWord;
import util.MyUtils;

public class WordSequences {
	
	private int lastID = 0;
	
	private String lang;
	private Indices idx;
	public double allwords = 0;
	public Indices idx() {
		return idx;
	}

	public boolean closed = false;
	public int getLastID() {
		this.lastID = this.lastID + 1;
		return this.lastID;
	}

	public WordSequences(String lang) {
		this.lang = lang;
		this.idx = new Indices();
	}
	
	public String getLang() {
		return this.lang;
	}
	
	
	public boolean hasSyntPars() {
		return this.idx.syntPars().size() > 0;
	}
	
	public boolean hasMorphPars() {
		return this.idx.getMPlabels().size() > 0;
	}
	
	
	public Word getWord(String s1) {
		if(s1.matches("\\d+")) 		s1 = "ZZZAHL";
		Word w = this.idx.words.get(s1);
		if(w == null && s1.length() > 1 && s1.endsWith("_"))
			w = this.idx.words.get(s1.substring(0, s1.length()-1));
		if(w == null) {
			w = new Word(this.getLastID(), s1);
			if(this.closed) {
				try {
					throw new Exception("closed model");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			this.idx.words.put(s1, w);
		}
		return w;
	}

	public WordSequences addWordPhrasesToModel(List<String> sents, boolean toLower) {
		int snr = 0;
		HashSet<String> seen = new HashSet<>();
		for(String sent: sents) {
			snr++;
			if(snr%100000 == 0) System.out.println(snr);
		Word second = null;
		Word first = null; //don't need first and second, previuous would be enough for bigrams, but it remained from trigrams
			for(String str: TestTokenizer.getTokens(sent, this.getLang(), true, true)) {
				if(!MyUtils.isWord(str)) {
					second = null;
					first = null;
					continue;
				}
				if(toLower) str = str.toLowerCase();
				boolean  isPhraseWord = str.contains("_");
				if(isPhraseWord && !seen.contains(str))
//					System.out.println(str);
				if(isPhraseWord)seen.add(str);
				
				Word thisword = this.getWord(str);
				if(isPhraseWord) thisword.addFreq();
				if (first == null) {
					if (second == null) {
						second = thisword;
						continue;
					} else {
						if(isPhraseWord || second.toString().contains("_")) this.processBigram(second, thisword);
						first = second;
						second = thisword;
					}
				}else {
					if(isPhraseWord || second.toString().contains("_")) this.processBigram(second, thisword);
					first = second;
					second = thisword;
				}
			}
		}
		return this;
	}
	
	public WordSequences addWordsToModel(List<String> sents, boolean toLower, boolean addCats) {
		int snr = 0;
		for(String sent: sents) {
			snr++;
			if(snr%100000 == 0) System.out.println(snr);
		Word second = null;
		Word first = null; //don't need first and second, previuous would be enough for bigrams, but it remained from trigrams
			for(String str: TestTokenizer.getTokens(sent, this.getLang(), true, false)) {
				this.allwords++;
				if(!MyUtils.isWord(str)) {
					second = null;
					first = null;
					continue;
				}
				if(toLower) str = str.toLowerCase();
//				if(str.equals("Der")) str = "der";
				//wsmodel.nwords++;
				Word thisword = this.getWord(str);
				thisword.addFreq();
				
				if (first == null) {
					if (second == null) {
						second = thisword;
						continue;
					} else {
						this.processBigram(second, thisword);
						if(addCats) processBigramCats(second, thisword);
						first = second;
						second = thisword;
					}
				}else {
					this.processBigram(second, thisword);
					if(addCats) processBigramCats(second, thisword);
					first = second;
					second = thisword;
				}
			}
		}
		normalizeSentenceStart();
		System.out.println("ALL WORDS: " + this.allwords);
		System.out.println("ALL uniqs: " + this.idx.words.size());
		return this;
	}
	
	/**
	 * checks if first word in sent should be lowercased, e.g. article or prep, makes the lowercase if necessary
	 */
	public void normalizeSentenceStart() {
		Map<String, Word> wmap = this.idx.words;
		Iterator<Entry<String, Word>> wordIt = wmap.entrySet().iterator();
		while(wordIt.hasNext()) {
			Entry<String, Word> entry = wordIt.next();
			if(entry.getValue().freq() < 10) continue;
			String ws = entry.getKey();
			String wsLower = ws.toLowerCase();
			if(ws.equals(wsLower)) continue;
			if(!wmap.containsKey(wsLower)) continue;
			Word w = entry.getValue();
			Word wlower = this.getWord(wsLower);
			if( wlower.freq() > w.freq()) {
				wlower.addFreq(w.freq());
				for(Word right: w.left_of.keySet()) {
					MapsOps.addFreq(right, wlower.left_of, w.left_of.get(right)); //freq of left nb into lower
					MapsOps.addFreq(wlower, right.right_of, w.left_of.get(right)); //freq of left nb into nb
					right.right_of.remove(w);
				}
				for(Word left: w.right_of.keySet()) {
					MapsOps.addFreq(left, wlower.right_of, w.right_of.get(left)); //freq of left nb into lower
					MapsOps.addFreq(wlower, left.left_of, w.right_of.get(left)); //freq of left nb into nb
					left.left_of.remove(w);
				}
				wordIt.remove();
			}
		}
	}

	private void processBigram(Word w1, Word w2) {
//		if(w1.freq() < 2 || w2.freq() < 2) return;
		w1.fillLeftOf(w2);
		w2.fillRightOf(w1);

	}
	
	private void processBigramCats(Word w1, Word w2) {
		w1.fillLeftOfCats(w2,this);
		w2.fillRightOfCats(w1,this);

	}
	
	public static void processBigram(Word w1, Word w2, double freqvalue) {
		w1.fillLeftOf(w2, freqvalue);
		w2.fillRightOf(w1, freqvalue);
	}
	
	public static void deleteBigram(Word w1, Word w2, double freqvalue) {
		w1.deleteLeftOf(w2, freqvalue);
		w2.deleteRightOf(w1, freqvalue);
	}
	
	public String getInfo(String str) {
		Word w = this.getWord(str);
		if(w == null)
			return "noInfo";
		String wstring = w.toString();
		Collocation splt_l = Words.getCollocation(this.getWord("s_splitter"), w);
		Collocation splt_r = Words.getCollocation(w, this.getWord("s_splitter"));
		double p = MyUtils.rdouble(w.freq()/allwords);
		return wstring +" "+w.freq()+" left_of "+w.left_of.size()+" right_of "+w.right_of.size() 
		+ " root " + w.getRoot() + " flex " + w.getFlex() 
		+ " synt par " + w.syntLabel
		+ " morph par " + w.getMorphLabelNotNull() 
		+ " synt cluster " + w.getCluster() 
		+ (w.paradigmWords.size() == 0 ? "" : "\n"+"paradigm words:\t"+ w.paradigmWords.toString())

		+ "\n" + "morph par " + (w.getMorphParadigm() == null ? "null" : w.getMorphParadigm().getFlexes())  + "\t"+ wstring
		+ "\n" + "coef:\t"+ MyUtils.rdouble(w.getCoef(true)) + "\tright:\t" + MyUtils.rdouble(w.getCoef(false))  + "\t"+ wstring
//		+ "\n" + "var:\t" + MyUtils.rdouble(w.getVar(true))  + "\tright:\t" + MyUtils.rdouble(w.getVar(false)) 
//		+ "\n" + "var coef l/r:\t" + MyUtils.rdouble(w.getVar(true)/w.getVar(false))
//		+ "\tall_words_var:\t" + MyUtils.rdouble(w.getVarOfAll(true, this.idx.words.size()))  + "\tright:\t" + MyUtils.rdouble(w.getVarOfAll(false, this.idx.words.size())) 
//		+ "\tl/p:\t" + MyUtils.rdouble(w.getVarOfAll(true, this.idx.words.size())/p) + "\tr/p:\t" + MyUtils.rdouble(w.getVarOfAll(false, this.idx.words.size())/p) + "\t"+ wstring
//		+ "\n" + "prob:\t" + p + "\t"+ wstring
		+ "\n" + "splitter l:\t" + splt_l + "\t"+ wstring
		+ "\n" + "splitter r:\t" + splt_r + "\t"+ wstring + "\tsplitter coef:\t" + MyUtils.rdouble(splt_l.sim.left/splt_r.sim.right)
		+ "\n" + "collocLR r:\t" + 	w.isLeftCollocWord(0.01) + "\tlabel:\t" + Words.getCollocLeftRightLabel(w, 0.01) + "\t"+ wstring
		+ (w.features.size() == 0 ? "" : "\n"+"features:\t"+ w.features.toString())
		+ "\n" + "arg coef:\t" + w.argCoef() + "\t"+ wstring
		+ "\n" + "seen as pred:\t" + w.seenAsPred + "\t" + w.predVariants.toString()
		
;
	}
	
	public String getInfoShort(String str) {
		Word w = this.getWord(str);
		if(w == null)
			return "noInfo";
		String toreturn = w.toString()+" "+w.freq()+" left_of "+w.left_of.size()+" right_of "+w.right_of.size() 
		+ " root " + w.getRoot() + " flex " + w.getFlex() 
		+ " synt par " + w.syntLabel 
		+ " morph par " + w.getMorphLabelNotNull() 
		+ "\t" + "arg coef:\t" + w.argCoef()
		+ (w.paradigmWords.size() == 0 ? "" : "\n"+"paradigm words:\t"+ w.paradigmWords.toString())
		;
		return toreturn;
	}
	
	public double getFreqOfAnd() {
//		if(this.lang.equals("ukr")) {
//			return this.getWord("і").freq() + this.getWord("й").freq() + this.getWord("та").freq();
//		}
		String langstr = getAndString();
		if(langstr == null)
			return 40000.0;
		return this.getWord(langstr).freq();
	}
	
	public String getAndString() {
		if(this.lang.equals("de"))
			return "und";
		if(this.lang.equals("en"))
			return "and";
		if(this.lang.equals("ukr"))
			return "і";
		if(this.lang.equals("ita"))
          return "e";
		return null;
	}

	public Word addCategory(String catName, Word w) {
		Word catWord = this.getWord(catName);
		catWord.addFreq(w.freq());
		if(catWord.paradigmWords.size() < 100) catWord.paradigmWords.add(w);
		for(Word w_right: w.left_of.keySet()) {
			double freq = w.left_of.get(w_right);
			WordSequences.processBigram(catWord, w_right, freq);
		}
		for(Word w_left: w.right_of.keySet()) {

			double freq = w.right_of.get(w_left);
			WordSequences.processBigram(w_left, catWord, freq);
		}
		return catWord;
	}
	
	public void deleteCategory(Word mpword) {
		Set<Word> wset = new HashSet<>();
		for(Word w: mpword.left_of.keySet()) wset.add(w);
		for(Word w: mpword.right_of.keySet()) wset.add(w);
		for(Word w: wset) this.deleteCategoryInWord(mpword.toString(), w);
	}

	
	public void deleteCategoryInWord(String catName, Word w) {
		if(catName == null || w == null || w.toString().equals(catName)) return;
		Word catWord = this.getWord(catName);
		catWord.addFreq(w.freq()*(-1)); 
		for(Word w_right: w.left_of.keySet()) {
			if(w.left_of.get(w_right) != null) WordSequences.deleteBigram(catWord, w_right, w.left_of.get(w_right));
		}
		for(Word w_left: w.right_of.keySet()) {
			if(w.right_of.get(w_left) != null) WordSequences.deleteBigram(w_left, catWord, w.right_of.get(w_left));
		}
		if(catWord.freq() < 1) 
			this.idx().words.remove(catName);

	}

	public void setMorphPars(List<MorphParadigm> mpars) {
		//just add mWords into model without freq, freq depends on real words
		for(MorphParadigm mpar: mpars) {
			String mLabel = mpar.getLabel();
			Word mWord = this.getWord(mLabel);
			mWord.changeMorphParadigm(mpar);
		}
	}
	
	public void fillAssociations(double minFreq) {
		initAssocs();
		collectSuffixFreqs();
		for(Word w: this.idx.getSortedWords()) {
			if(w.isCat()) continue;
			if(w.freq() < minFreq) break;
			Set<Word> wcats = w.getWordCats(this);
			for(Word cat1: wcats) {
				for(Word cat2: wcats) {
					if(cat1.toString().equals(cat2.toString())) continue;
					addAsso(cat1,cat2);
				}
			}
		}
	}
	
	private void collectSuffixFreqs() {
		for(Word w: this.idx.getSortedWords()) {
			if(w.getSuffix() != null)
				this.getWord(w.getSuffixCatLabel()).addFreq(w.freq());
		}
	}

	public void printAssociations(double thh) {
		for(Word keyCat: this.idx.assocs.keySet()) {
			Map<Word,Double> values = this.idx.assocs.get(keyCat);
			for(Word assoCat: values.keySet()) {
				if(keyCat.freq() ==0 || assoCat.freq()==0)continue;
				double assoFreq = values.get(assoCat);
				if((assoFreq/assoCat.freq()>thh) || (assoFreq/keyCat.freq()>thh))
					System.out.println("ASSO:\t"+keyCat+"\t"+assoCat+"\t"+ (assoFreq/keyCat.freq()) + "\t" +(assoFreq/assoCat.freq()));
			}
		}
	}
	
	private void initAssocs() {
		this.idx.assocs = new HashMap<Word, Map<Word,Double>>();
		for(Word cat: this.idx.cats())
			this.idx.assocs.put(cat, new HashMap<>());
	}

	public void addAsso(Word keyCat, Word assoCat) {
		if(!keyCat.isCat() || !assoCat.isCat()) return;
		if(this.idx.assocs.get(keyCat)==null) this.idx.assocs.put(keyCat, new HashMap<>());
		MapsOps.addFreq(assoCat, this.idx.assocs.get(keyCat));
	}

	/**
	 * no need to delete seldom words: if a word has freq= 1, its contexts are not added. so they are not in the stats for phrases
	 * which is good.
	 * but the word is in the index with its freq=1, so it can be checked, 
	 * if such a word form was seen in general (important for morphology)
	 * @param thhForSeldom
	 */
	public void removeSeldomWordsDepr(double thhForSeldom) {
		Set<Word> toRemove = new HashSet<>();
		for(Word w: this.idx().getSortedWords()) {
			if(w.freq() <= thhForSeldom) 
				toRemove.add(w);
		}
		System.out.println("seldom words collected: " + toRemove.size());
		int i = 0;
		for(Word seldom: toRemove) {
			i++;
			if(i%1000 == 0) {
				System.out.println(i+ "\t" + seldom.toString());
			}
			//this.deleteCategory(seldom);
			this.idx.words.remove(seldom.toString());
		}
	}

	public void computeParadigmExpectations() {
		for(String leftParLabel: this.idx.knownParadigmLabels) {
			for(String rightParLabel : this.idx.knownParadigmLabels) {
				Collocation c = Words.getCollocation(this.getWord(leftParLabel), this.getWord(rightParLabel));
				if(c.sim.right > 0.29) { //.low() > 0.2) {
					if(!this.idx.phraseExpectations.containsKey(leftParLabel)) this.idx.phraseExpectations.put(leftParLabel, new HashSet<>());
					this.idx.phraseExpectations.get(leftParLabel).add(rightParLabel);
				}
			}
		}
	}
	
	public void analyzeMorphCatsForTerminals() {
		for(MorphParadigm mp: this.idx().getMorphParadigms()) {
			Word mpWord = this.getWord(mp.getLabel());
			checkIfTerminal(mpWord, mp.getLabel());
		}
		
	}

	private void checkIfTerminal(Word parWord, String label) {
		List<MyPairWord> exp = Words.getExpectationsLeftRightSorted(parWord, true, 0.01, this);
		if(!exp.isEmpty()) {
			if(exp.get(0).signif < 0.3) idx.morphTerminals.add(label);
		}
	}

	public void analyzeSyntCatsForTerminals() {
		for (Cluster c : this.idx().getSyntParadigms()) {
			Word parword = this.getWord(c.getLabel());
			checkIfTerminal(parword, c.getLabel());
		}
	}
	
	public Map<String, List<String>> knownMparContextsMapLeft = null;
	public Map<String, List<String>> knownMparContextsMapRight = null;
	
	public void collectKnownParVectors( String regexForParadimFilter, int contextcount) {
		knownMparContextsMapLeft = new HashMap<>();
		knownMparContextsMapRight = new HashMap<>();
		for(String mplabel: this.idx().syntPars().keySet()) { //
		  if(mplabel.startsWith("m_")) continue;
					Word mparWord = this.getWord(mplabel);
					List<String> bestContextsLeft = new ArrayList<>();
					for(MyPairWord key: mparWord.getBestContextsComputeNew(true, contextcount, regexForParadimFilter, this, false)){
						bestContextsLeft.add(key.left.toString());
					}
					knownMparContextsMapLeft.put(mplabel, bestContextsLeft);
					List<String> bestContextsRight = new ArrayList<>();
					for(MyPairWord key: mparWord.getBestContextsComputeNew(false, contextcount, regexForParadimFilter, this, false)){
						bestContextsRight.add(key.left.toString());
					}
					knownMparContextsMapRight.put(mplabel, bestContextsRight);
				}
//		for(String s: knownMparContextsMapLeft.keySet()) 
//		  if(s.startsWith("x_0"))         System.out.println("VECTOR: " + knownMparContextsMapLeft.get(s));
//		for(String s: knownMparContextsMapRight.keySet()) 
//          if(s.startsWith("x_0"))         System.out.println("VECTOR R: " + knownMparContextsMapRight.get(s));

		
	}

  public void tagMorphSynt() {
		int contextcount_words = 40;
		MorphVectorAnalyzer.collectMParVectorsParadigm(this, Words.SYNSEM_FILTER, false, contextcount_words);
		this.collectKnownParVectors(Words.SYN_FILTER, contextcount_words);
		double testedWords = 0.0;
		double foundPars = 0.0;
		int i = 0;
		for(Word w: this.idx.getSortedWords()){
			if(w.freq() <= 2) break;
			if(w.toString().contains("_")) continue;
			i++;
			boolean print = true;
			print = ((i%10) == 0);
			testedWords++;
			boolean catWasFound = taOneWordMorphSynt(w, contextcount_words, print);
			if(catWasFound) foundPars++;
		}
		System.out.println("TESTED: " + testedWords + " FOUND: " + foundPars + " RATIO: " + (foundPars/testedWords));
  }

	private boolean taOneWordMorphSynt(Word w, int contextcount_words, boolean print) {
		List<MyPair> bestmpars = MorphVectorAnalyzer.getMParFromParVector(this, w,
						false, Words.SYNSEM_FILTER, false, contextcount_words);
		Collections.sort(bestmpars, Collections.reverseOrder());
		if(bestmpars.isEmpty()) bestmpars.add(new MyPair(SyntParVectorTrain.MZERO, "", 1.0));
		List<MyPair> bestSyntpars = MorphVectorAnalyzer.computeBestKnownVectorParadigmS(w, Words.SYN_FILTER,
						this, false, Double.MAX_VALUE,
						contextcount_words);
		Collections.sort(bestSyntpars, Collections.reverseOrder());
		//check if best params vote for each other

		if(voteMorphSynt(bestmpars, bestSyntpars)){
			w.writeTags(bestmpars.get(0), bestSyntpars.get(0));
			if(print) printInfo(w, "case1");
			return true;
		}
		List<MyPairWord> bestPreviousContexts  = w.getBestContexts(false, 1, Words.SYNSEM_FILTER, this, true);
		if(bestPreviousContexts.size() == 0) return false;
		Word bestPreviousWord = bestPreviousContexts.get(0).left;
		List<MyPairWord> expLeft = Words.getExpectationsLeftRightSorted(bestPreviousWord, true, 0.001, this);
		if(expLeft.size() == 0) return false;
		if(voteParadigmContextExp(bestmpars, expLeft)){
			w.writeTags(bestmpars.get(0), null);
			if(print) printInfo(w, "case2");
			return true;
		}

		if(voteMorpFlex(bestmpars, w)){
			w.writeTags(bestmpars.get(0), bestSyntpars.get(0));
			if(print) printInfo(w, "case1a");
			return true;
		}

		if(voteParadigmContextExp(bestSyntpars, expLeft)){
			w.writeTags(null, bestSyntpars.get(0));
			if(print) printInfo(w, "case3");
			return true;
		}

		List<MyPairWord> bestNextConts  = w.getBestContexts(true, 1, Words.SYNSEM_FILTER, this, true);
		if(bestNextConts.size() == 0) return false;
		Word bestNextWord = bestNextConts.get(0).left;
		List<MyPairWord> expRight = Words.getExpectationsLeftRightSorted(bestNextWord, false, 0.001, this);
		if(expRight.size() == 0) return false;
		if(voteParadigmContextExp(bestmpars, expRight)){
			w.writeTags(bestmpars.get(0), null);
			if(print) printInfo(w, "case4");
			return true;
		}
		if(voteParadigmContextExp(bestSyntpars, expRight)){
			w.writeTags(null, bestSyntpars.get(0));
			if(print) printInfo(w, "case5");
			return true;
		}
		if(print) printInfo(w, "caseNONE");
		return false;
	}

	private void printInfo(Word w, String caseNr) {
		System.out.println(caseNr+" "+w+"\t" + +w.freq() + "\t" + w.bestMpar+"\t"+ w.bestSpar
						+"\t" +(w.getMorphParadigm()== null ? "null" : w.getMorphParadigm().getLabel())+"\t" +w.syntLabel);
	}

	private boolean voteParadigmContextExp(List<MyPair> bestpars, List<MyPairWord> expLeft) {
		if(bestpars == null || expLeft == null || bestpars.isEmpty() || expLeft.isEmpty()) return false;
		String bestParLabel = bestpars.get(0).first;
		String bestExpect = expLeft.get(0).left.toString();
		if(bestParLabel.equals(bestExpect))
			return true;
		return false;
	}

	private boolean voteMorpFlex(List<MyPair> bestmpars, Word w) {
		if(bestmpars == null || bestmpars.isEmpty() ) return false;
		String bestMparLabel = bestmpars.get(0).first;
		MorphParadigm mp = this.idx().getMorphParadigm(bestMparLabel);
		if(mp == null) return false;
		for(Flexion f: mp.getFlexes()){
			if(f.toString().equals("") ) continue;
			if(w.toString().endsWith(f.toString())) return true;
		}
		return false;
	}
		private boolean voteMorphSynt(List<MyPair> bestmpars, List<MyPair> bestSyntpars) {
		if(bestmpars == null || bestSyntpars == null  || bestSyntpars.isEmpty()) return false;
		String bestMparLabel = SyntParVectorTrain.MZERO;
		if( !bestmpars.isEmpty())
			bestMparLabel = bestmpars.get(0).first;
		Cluster syntCluster = this.idx().getSyntParadigm(bestSyntpars.get(0).first);
		if (syntCluster.firstMpar == null){
			SyntParVectorTrain.getMorphFreqs(this, syntCluster);
			if(syntCluster.firstMpar == null) return false;
		}
		String mparOfCLuster = syntCluster.firstMpar.first;
		if(mparOfCLuster == null) return false;
		if(bestMparLabel.equals(mparOfCLuster))
			return true;
		return false;
	}

	public List<Word> getWords(int start, int end, boolean skipCats) {
		List<Word> wlist = new ArrayList<>();
		int i = 0;
		for(Word w: this.idx.getSortedWords()){
			if(skipCats && w.toString().contains("_")) continue;
			i++;
			if(i < start) continue;
			if(i > end) break;
			wlist.add(w);
		}
		return wlist;
	}
}
