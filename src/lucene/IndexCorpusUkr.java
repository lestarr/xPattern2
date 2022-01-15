package lucene;

import java.util.ArrayList;
import java.util.List;

import util.CorpusUtils;

public class IndexCorpusUkr {
	
	public static void main(String[] args) {
		
		String[] corpora = null;
		corpora = new String[] {"wiki", "news"};
		List<String> sents = new ArrayList<String>();
		for(String c: corpora) {
			 sents.addAll(CorpusUtils.getLeipzigSentences("ukr", c, 0, 1000000));
		
		}
				 
		 String indexPath = "out/index/indexUKR";
		 IndexCorpus.indexCorpus(sents, indexPath);
	}

}
