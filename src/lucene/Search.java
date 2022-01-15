package lucene;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;

public class Search {
	
	public Directory dir;
	public IndexReader ir;
	public IndexSearcher is;
	public QueryParser qp;

	public Search(String dirstring) throws IOException {
		this.dir = LuceneHelper.getDirectory(dirstring);
		this.ir = LuceneHelper.getIndexReader(this.dir);
		this.is = LuceneHelper.getIndexSearcher(this.ir,	this.dir);
		Analyzer an = new StandardAnalyzer(new StringReader(""));
		this.qp = new QueryParser(LuceneHelper.BODY, an);
		this.qp.setDefaultOperator(Operator.AND);
	}
}
