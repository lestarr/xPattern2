package lucene;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;

public class TestIndex {
	
	public static void main(String[] args) {
		testIndexDe();
	}

	private static void testIndexDe() {
		String dir = "out/index/indexDE";
		IndexReader ir = LuceneHelper.getIndexReader(LuceneHelper.getDirectory(dir));
		IndexSearcher is = LuceneHelper.getIndexSearcher(ir, LuceneHelper.getDirectory(dir));
		
		QueryParser qp = new QueryParser(LuceneHelper.BODY, new StandardAnalyzer());
		
		String query = "Börse";
		IndexCorpus.testIndex(query, ir, is, qp);
	}

}
