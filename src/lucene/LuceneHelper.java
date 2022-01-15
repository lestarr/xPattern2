package lucene;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


public class LuceneHelper {
  
  public static final String BODY = "body";
  public static final String ID = "id";

  public static IndexReader getIndexReader(Directory directory) {
  	IndexReader ireader = null;
    try {
      if(ireader == null) {
        ireader = DirectoryReader.open(directory);
      }
    } catch (Exception e) {
      System.out.println(e.toString());
    }
    return ireader;
  }
  
  public static IndexSearcher getIndexSearcher(IndexReader ireader, Directory directory) {
    IndexSearcher isearcher = null;
    try {
      if(ireader == null) {
        ireader = DirectoryReader.open(directory);
        isearcher = new IndexSearcher(ireader);
      } else if(isearcher == null) {
        isearcher = new IndexSearcher(ireader);
      }
    } catch (Exception e) {
      System.out.println(e.toString());
    }
    return isearcher;
  }
  
  public static Directory getDirectory(String dir) {
  	try {
			return FSDirectory.open(Paths.get(dir));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  	return null;
  }
  
  public static IndexWriter getIndexWriter(Analyzer indexAnalyzer, Directory directory) {
    IndexWriter indexWriter = null;
    if(indexWriter == null) {
      IndexWriterConfig config = new IndexWriterConfig(indexAnalyzer);
    try {
      indexWriter = new IndexWriter(directory, config);
    } catch (IOException e) {
      System.out.println(e.toString());
    }
    }
    return indexWriter;
  }

  public void index(IndexWriter indexWriter, int idToStartWith, final String... aDocuments) {
    for (String document : aDocuments) {
      addDocToIndex(document, indexWriter, idToStartWith);
      idToStartWith++;
    }
  }


  public static void addDocToIndex(String text, IndexWriter indexWriter, int id) {
    try {
      indexWriter.addDocument(makeLuceneDocument(text, id));
    } catch (Exception e) {
      System.out.println(e.toString());
    }
  }
  
  public static org.apache.lucene.document.Document makeLuceneDocument(String text, int id) {
    org.apache.lucene.document.Document luceneDoc = new org.apache.lucene.document.Document();
          luceneDoc.add(new Field(ID,"s"+Integer.toString(id), StringField.TYPE_STORED));
          luceneDoc.add(new Field(BODY, text,  TextField.TYPE_STORED ));
    return luceneDoc;
  }
  
  public List<Integer> search(final String aQuery, QueryParser parser, IndexSearcher indexSearcher) {
    List<Integer> docIds = lookupQuery(aQuery, parser, indexSearcher);
    return docIds;
  }
  
  public static List<String> makeHits(List<Integer> docIds, IndexReader ireader) {
    List<String> hits = new ArrayList<>();
    try {
      for(int id: docIds) {
        org.apache.lucene.document.Document d;
        d = ireader.document(id);
        hits.add(d.get(BODY));
      }
    } catch (Exception e) {
      System.out.println(e.toString());
      }
    return hits;
  }
  
  public static List<Integer> lookupQuery(String queryString, QueryParser parser, IndexSearcher indexSearcher) {
    List<Integer> docIds = new ArrayList<>();
    Query query;
    try {
        query = parser.parse(queryString);
      TopDocs topDocs = indexSearcher.search(query, Integer.MAX_VALUE);
      
      if(topDocs.totalHits > 0) {
        for(ScoreDoc sc: topDocs.scoreDocs) docIds.add(sc.doc);
      }
    } catch (Exception exception) {
      System.out.println(exception.toString());
    }
    return docIds;
  }
  
  public void delete(IndexWriter indexWriter, QueryParser parser, final String... aQueries) {
    try {
      for (String q : aQueries) {
        if ("*".equals(q)) {
          indexWriter.deleteDocuments(new MatchAllDocsQuery());
        } else {
          Query query;
          query = parser.parse(q);
          indexWriter.deleteDocuments(query);
        }
      }
    } catch (Exception e) {
      System.out.println(e.toString());
    }
  }
  
}
