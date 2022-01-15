package util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import modelparts.Word;

public class SetOps {
	
	public static Set<String> of(String...strings){
		Set<String> newlist = new HashSet<>();
		for (int i = 0; i < strings.length; i++) {
			newlist.add(strings[i]);
		}
		return newlist;
	}
	
	public static Set<String> getSet(List<String> list){
		Set<String> set = new HashSet<>();
		for (int i = 0; i < list.size(); i++) {
			set.add(list.get(i));
		}
		return set;
	}
	
	public static Set<String> getStringSet(Set<Word> wset){
		Set<String> strset = new HashSet<>();
		for (Word w: wset) {
			strset.add(w.toString());
		}
		return strset;
	}
	
	public static  <T> Set<T> of(){
		Set<T> newlist = new HashSet<T>();
		return newlist;
	}
	
	public static <T> Set<T> of(T...elements){
		Set<T> newlist = new HashSet<>();
		for (int i = 0; i < elements.length; i++) {
			newlist.add(elements[i]);
		}
		return newlist;
	}



}
