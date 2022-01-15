package util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class MapValueSort {

	/** inner class to do soring of the map **/
	public static class ValueComparer implements Comparator {
		private Map  _data = null;
		public ValueComparer (Map data){
			super();
			_data = data;
		}

		public int compare(Object key1, Object key2) {
			Comparable value1 = (Comparable) _data.get(key1);
			Comparable value2 = (Comparable) _data.get(key2);
			int c = value2.compareTo(value1);
			if (0 != c)
			return c;
			Integer h1 = key1.hashCode(), h2 = key2.hashCode();
			return h2.compareTo(h1);
		}
	}
	


	public static void main(String[] args){

		Map unsortedData = new HashMap();
		unsortedData.put("2", 2.1);
		unsortedData.put("1", 3.5);
		unsortedData.put("4", 3.5);
		unsortedData.put("3", 2.099);

		SortedMap sortedData = new TreeMap(new MapValueSort.ValueComparer(unsortedData));

		printMap(unsortedData);

		sortedData.putAll(unsortedData);
		System.out.println();
		printMap(sortedData);
	}

	private static void printMap(Map data) {
		for (Iterator iter = data.keySet().iterator(); iter.hasNext();) {
			String key = (String) iter.next();
			System.out.println("Value/key:"+data.get(key)+"/"+key);
		}
	}

}
