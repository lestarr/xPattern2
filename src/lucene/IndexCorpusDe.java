package lucene;

import java.util.List;

import util.CorpusUtils;

public class IndexCorpusDe {
	
	public static void main(String[] args) {
		String corpusPath = CorpusUtils.getCorpusDe("company");
		List<String> sents = CorpusUtils.getLeipzigSentences("de", corpusPath, 0, 200000);
		List<String> testCorpus =  CorpusUtils.getSentsDE();
		sents.addAll(testCorpus);
		String testFilePath = "C:\\2Projects\\CorporaIndices\\goldenstandard/corpusDPA_apr024_50.txt";
		testCorpus = CorpusUtils.getSentences(testFilePath);
		sents.addAll(testCorpus);
		corpusPath = CorpusUtils.getCorpusDe("wiki");
		sents.addAll(CorpusUtils.getLeipzigSentences("de", corpusPath, 0, 800000));
		 
		 String indexPath = "out/index/indexDE";
		 IndexCorpus.indexCorpus(sents, indexPath);
	}

}
