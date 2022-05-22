package util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import modelparts.Word;

public class ListOps {
	
	public static List<String> of(String...strings){
		List<String> newlist = new ArrayList<>();
		for (int i = 0; i < strings.length; i++) {
			newlist.add(strings[i]);
		}
		return newlist;
	}
	
	public static  List of(){
		List newlist = new ArrayList();
		return newlist;
	}
	
	public static <T> List<T> of(T...elements){
		List<T> newlist = new ArrayList<>();
		for (int i = 0; i < elements.length; i++) {
			newlist.add(elements[i]);
		}
		return newlist;
	}
	
	public static <T> List<T> of(Collection<T>elements){
		List<T> newlist = new ArrayList<>();
		for (T elem: elements) {
			newlist.add(elem);
		}
		return newlist;
	}

	public static List<MyPairWord> getSinifList(Map<Word, Double> map) {
		List<MyPairWord> list = new ArrayList<>();
		for(Word s: map.keySet()) {
			list.add(new MyPairWord(s, map.get(s)));
		}
		return list;
	}

  public static boolean notNullEmpty(List list) {
		if(list == null) return false;
		if(list.size() < 1) return false;
		return true;
  }
}
