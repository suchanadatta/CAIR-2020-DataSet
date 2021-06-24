/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fire_annotation;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author suchana
 */

public class Searcher {

    String indexPath;
    String query;
    String searchField;

    //      import org.apache.lucene.analysis.Analyzer;
    // same analyzer that has been used while indexing
    Analyzer analyzer;

    QueryParser queryParser;
    //      import org.apache.lucene.search.IndexSearcher;
    IndexSearcher searcher;
    //      import org.apache.lucene.index.IndexReader;
    IndexReader reader; 
    // we need to have an IndexReader for searching the index as well
   
    String stopwordPath;
    List<String> stopwordList;

    public Searcher(String indexPath, String query, String searchField) throws IOException {

        this.indexPath = indexPath;
        this.query = query;
        this.searchField = searchField;

        //      import org.apache.lucene.analysis.en.EnglishAnalyzer;
        // in case to use the default stopword list
        analyzer = new EnglishAnalyzer();

        queryParser = new QueryParser(searchField, analyzer);

        //      import org.apache.lucene.store.Directory;
        Directory indexDir;
        //      import org.apache.lucene.store.FSDirectory;
        // FSDirectory.open(file-path-of-the-dir)
        indexDir = FSDirectory.open((new File(this.indexPath)).toPath());
        //      import org.apache.lucene.index.DirectoryReader;

        reader = DirectoryReader.open(indexDir);
        searcher = new IndexSearcher(reader);
        //      import org.apache.lucene.search.similarities.BM25Similarity;
        searcher.setSimilarity(new BM25Similarity());
//        searcher.setSimilarity(new KLDivergenceSimilarity());
    }

    public Query makeLuceneQuery(String queryStr) throws ParseException {

        Query luceneQuery = queryParser.parse(queryStr);
        return luceneQuery;
    }

    public void search(String queryStr) throws ParseException, IOException {

        //      import org.apache.lucene.search.ScoreDoc;
        ScoreDoc[] hits;

        //      import org.apache.lucene.search.TopDocs;
        TopDocs topDocs;

        //      import org.apache.lucene.search.TopScoreDocCollector;
        TopScoreDocCollector collector = TopScoreDocCollector.create(100);

        Query luceneQuery = makeLuceneQuery(queryStr);
        System.out.println("######## : " + luceneQuery);

        // the actual search is taking place
        searcher.search(luceneQuery, collector);

        topDocs = collector.topDocs();
        hits = topDocs.scoreDocs;
//        System.out.println("length : " + hits.length);
        if(hits == null)
            System.out.println("Nothing found");
        
        int rank = 0;
        System.out.println("TOTAL MATCHES : " + hits.length);
        for (ScoreDoc hit : hits) {
            int luceneDocid = hit.doc;
            Document d = searcher.doc(luceneDocid);
            //System.out.println(++rank + " - \t" + hit.score + ":\t" + d.get("title"));// + " : "+ d.get("summary"));
            //-----------------
            //if(d.get("docid").equalsIgnoreCase("FBIS4-22629"))
                System.out.println(d.get("rawcontent"));
        }
    }

    public static void main(String[] args) throws ParseException, IOException {

        String indexPath, queryStr, searchField;
//        args = new String[3];
//        args[0] = indexPath = "/home/suchana/store/causalIR/resources/telegraph_2001-2011_full-index/";
//        args[1] = queryStr = "1021114_foreign_story_1383588.utf8";
//        args[2] = searchField = "docid";
//        if(args.length!=3) {
//            System.out.println("Usage: java searcher.Searcher <index-path> <query> <searching-field>");
//            exit(0);
//        }
//        indexPath = args[0];
//        queryStr = args[1];
//        searchField = args[2];
        
        indexPath = "/home/suchana/NetBeansProjects/FIRE_annotation/foo_index/";
        queryStr = "1070126_calcutta_story_7312422";
        searchField = "docid";
        
        System.out.println("hiii...");

        Searcher searcher;
        searcher = new Searcher(indexPath, queryStr, searchField);
        searcher.search(queryStr);
    }
}
