package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.Words;
import modelparts.Word;

/**
 * This class can also be seen as Bigram. contains first, second bigram part and the freq of this bigram in corpus.
 * @author halyna.galanzina
 *
 */
public class MyPairWord implements Comparable<MyPairWord>{
	
	public Word left;
	public Word right;
	public double signif;
	
	public int peakInRound = -1;
	public boolean hasLow = false;
	public boolean hasTerminal = false;
	
	public MyPairWord(Word wl, Word wr, double signif){
		this.left = wl;
		this.right = wr;
		this.signif = signif;
	}
	
	public MyPairWord(Word wl, Word wr){
		this.left = wl;
		this.right = wr;
		this.signif = -1.0;
	}
	
	public MyPairWord(Word wl, double signif){
		this.left = wl;
		this.right = wl;
		this.signif = signif;
	}
	
	public String toString(){
		if(left.toString().equals(right.toString()))
			return left.toString()+"  "+MyUtils.rdouble(signif);
		return left.toString()+" "+right.toString()+" "+MyUtils.rdouble(signif);
	}
	
	public String toString(String sep){
		return left.toString()+sep+right.toString()+sep+MyUtils.rdouble(signif);
	}


	@Override
	public int compareTo(MyPairWord o) {
		if(this.signif > o.signif) return -1;
		if(this.signif < o.signif) return 1;
		return 0;
	}

	public static List<MyPairWord> getMyPairWordList(Map<Word,Double> inputMap, double thh, double minfreq) {
		List<MyPairWord> list = new ArrayList<>();
		if(thh >5) thh = 5;
		int count = 0;
		for(Word w: inputMap.keySet()) {
			if(count > 50) break;
			if(w.toString().equals(Words.DUMMY)) continue;
			double signif = inputMap.get(w);
			if(signif < minfreq) continue;
			if(signif < thh) continue;
			list.add(new MyPairWord(w, signif));
			count++;

		}
		Collections.sort(list);
		return list;
	}
	
	public static Set<Word> getWordSetFirst(List<MyPairWord> mainContVector) {
		Set<Word> set = new HashSet<>();
		for(MyPairWord p: mainContVector)
			set.add(p.left);
		return set;
	}
	
	public static Set<String> getSetFirst(List<MyPairWord> mainContVector) {
		Set<String> set = new HashSet<>();
		for(MyPairWord p: mainContVector)
			set.add(p.left.toString());
		return set;
	}
	
	public static List<String> getListFirst(List<MyPairWord> mainContVector) {
		List<String> l = new ArrayList<>();
		for(MyPairWord p: mainContVector)
			l.add(p.left.toString()					);
		return l;
	}

}
