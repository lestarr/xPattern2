package util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class can also be seen as Bigram. contains first, second bigram part and the freq of this bigram in corpus.
 * @author halyna.galanzina
 *
 */
public class MyPair implements Comparable<MyPair>{
	
	public String first;
	public String second;
	public double freq;
	
	public MyPair(String f, String s){
		this.first = f;
		this.second = s;
		this.freq = 1.0;
	}
	
	public MyPair(String f, String s, double freq){
		this.first = f;
		this.second = s;
		this.freq = freq;
	}
	
	public String toString(){
		return first+" "+second+" "+MyUtils.rdouble(freq);
	}
	
	public String toString(String sep){
		return first+sep+second+sep+freq;
	}


	@Override
	public int compareTo(MyPair o) {
		if(this.freq > o.freq) return -1;
		if(this.freq < o.freq) return 1;
		return 0;
	}
	
	public static Set<String> getSetFirst(List<MyPair> mainContVector) {
		Set<String> set = new HashSet<>();
		for(MyPair p: mainContVector)
			set.add(p.first);
		return set;
	}
	
	public static List<String> getListFirst(List<MyPair> mainContVector) {
		List<String> l = new ArrayList<>();
		for(MyPair p: mainContVector)
			l.add(p.first);
		return l;
	}

}
