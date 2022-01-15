package lucene;

import java.util.ArrayList;
import java.util.List;

import util.MyUtils;

public class SearchOperations {
	
	public static SearchResult getSearchCollocation(String input, Search search) {
		if(search == null) return null;
		List<Integer> ids = LuceneHelper.lookupQuery(input, search.qp, search.is);
		if(input.split(" ").length == 2) {
			//print out collocational info
			String phraseOnly = input.replace("\"", "");
			double colValueLeft = MyUtils.rdouble((double)ids.size() / (double)LuceneHelper.lookupQuery(phraseOnly.split(" ")[1], search.qp, search.is).size());
			double colValueRight = MyUtils.rdouble((double)ids.size() / (double)LuceneHelper.lookupQuery(phraseOnly.split(" ")[0], search.qp, search.is).size());
			return new SearchResult(ids.size(), colValueLeft, colValueRight);
		}
		return new SearchResult(ids.size());
	}
	
	
	public static List<String> getSentences(String input, Search search){
		List<String> sents = new ArrayList<>();
		List<Integer> ids = LuceneHelper.lookupQuery(input, search.qp, search.is);

		for(String s: LuceneHelper.makeHits(ids, search.ir)) {
			sents.add(s);
		}
		return sents;
	}


	public static String normalizeQuots(String sent) {
		return sent.replaceAll("\"", "'").replaceAll("[\\*\\+\\(\\)\\?\\!\\/\\\\-]+", ";");
	}

}
