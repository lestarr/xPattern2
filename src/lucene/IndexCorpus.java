package lucene;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class IndexCorpus {
	
	public static void indexCorpus(List<String> sentences, String dir) {
		Directory indexDir;
		try {
			indexDir = FSDirectory.open(Paths.get(dir));
			Analyzer an = new StandardAnalyzer(new StringReader(""));

			IndexWriter iw = LuceneHelper.getIndexWriter(an,  indexDir);
			int id = 1;
			for(String sent: sentences) {
				LuceneHelper.addDocToIndex(sent, iw, id);
			}
			iw.commit();
			iw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void testIndex(String queryString, IndexReader ir, IndexSearcher is, QueryParser parser) {
		List<Integer> docids = LuceneHelper.lookupQuery(queryString, parser, is);
		System.out.println(docids.size());
		for(String s: LuceneHelper.makeHits(docids, ir))
			System.out.println(s.toString());
	}
	
	

}
