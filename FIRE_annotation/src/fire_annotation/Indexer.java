/*
 * This program creates an index for Telegraph collection
 * 3 fields are stored - 1. docid, 2. content (analyzed content) and 3. rawcontent (unanalyzed content)
 * Raw content is kept for annotation purpose.
 */

package fire_annotation;

/**
 *
 * @author suchana
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import static java.lang.System.exit;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
 

public class Indexer{
    
    String                collectionPath;
    String                indexPath;
    static IndexWriter    writer;              //org.apache.lucene.index.IndexWriter
    Analyzer              analyzer;            //org.apache.lucene.analysis.Analyzer; we use same analyzer for searching
    String                stopWordPath;
    List<String>          stopWordList;
    static int            docCount;
    
    
    public Indexer(String collectionPath, String indexPath) throws IOException {

        this.collectionPath = collectionPath;
        this.indexPath = indexPath;
        stopWordPath = "/home/suchana/smart-stopwords";
        
        //for using default stopwordlist
        //analyzer = new EnglishAnalyzer();                                   // org.apache.lucene.analysis.en.EnglishAnalyzer; this uses default stopword list
        
        //for using external stopword list
        stopWordList = getStopwordList(stopWordPath);                         
        analyzer = new EnglishAnalyzer(StopFilter.makeStopSet(stopWordList)); // org.apache.lucene.analysis.core.StopFilter
        
        Directory dir;                                                        // org.apache.lucene.store.Directory
        dir = FSDirectory.open((new File(this.indexPath)).toPath());          // org.apache.lucene.store.FSDirectory

        IndexWriterConfig iwc;                                                // org.apache.lucene.index.IndexWriterConfig
        iwc = new IndexWriterConfig(analyzer);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);                   // other options: APPEND, CREATE_OR_APPEND

        writer = new IndexWriter(dir, iwc);
        docCount = 0;
    }
    
    
    public List<String> getStopwordList(String stopwordPath) {
        
        List<String> stopwords = new ArrayList<>();
        String line;

        try {
            System.out.println("Stopword Path: "+ stopwordPath);
            FileReader fr = new FileReader(stopwordPath);
            BufferedReader br = new BufferedReader(fr);
            while ((line = br.readLine()) != null)
                stopwords.add(line.trim());
            br.close();
            fr.close();
        } catch (FileNotFoundException ex) {
            System.err.println("Error: \n" + "StandardAnalyzerWithSmartStopword: setAnalyzer()\n" + "Stopword file not found in: "+stopwordPath);
            System.exit(1);
        } catch (IOException ex) {
            System.err.println("Error: \n" + "StandardAnalyzerWithSmartStopword: setAnalyzer()\n" + "IOException occurs");
            System.exit(1);
        }
        return stopwords;
    }
    
    
    public void createIndex(String collectionPath) throws FileNotFoundException, IOException, NullPointerException {
        
        System.out.println("Indexing started...");
        File colFile = new File(collectionPath);
        if(colFile.isDirectory())
            collectionDirectory(colFile);
        else
            indexFile(colFile);
    }
    

    public void collectionDirectory(File colDir) throws FileNotFoundException, IOException, NullPointerException {
        
        File[] files = colDir.listFiles();
        for (File file : files) {
            System.out.println("Indexing file : " + file);
            if (file.isDirectory()) {
                System.out.println("It has subdirectories...\n");
                collectionDirectory(file);  // calling this function recursively to access all the subfolders in the directory
            }
            else
                indexFile(file);
        }
    }
    
    
    public void indexFile(File colFile) throws FileNotFoundException, IOException {
        
        String[] fieldToIndex;
        String analyzed_text;
        Document doc; 
        
        fieldToIndex = getFileContent(colFile);
        doc = new Document();
        
        doc.add(new Field("docid", fieldToIndex[0], Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO));
        //Storing analyzed content and term vector. Manual analysis (stopword removal, stemming) needed
        analyzed_text = analyzeText(analyzer, fieldToIndex[0]+fieldToIndex[1], "content").toString();
        System.out.println("%%%%%%%% : " + analyzed_text);
        doc.add(new Field("content", analyzed_text, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
        doc.add(new Field("rawcontent", fieldToIndex[1], Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO));
        
        writer.addDocument(doc);
        System.out.println("Indexed doc no. : " + ++docCount + "\n");
    }
    
    
    // for storing raw content
    public String[] getFileContent(File colFile) throws FileNotFoundException, IOException {
        
        String fileContent;
        Pattern p_docid, p_text;
        Matcher m_docid, m_text;
        String parseContent[] = new String[2];
        
        BufferedReader br = new BufferedReader(new FileReader(colFile));
        String line = br.readLine();
        StringBuilder sb = new StringBuilder(); 
        while(line != null){ 
            sb.append(line).append("\n");
            line = br.readLine();
        } 
        fileContent = sb.toString();
        fileContent = fileContent.replaceAll("\"", "").replaceAll("", "").replaceAll("\n", "").replaceAll("\r", "");;
                
        p_docid = Pattern.compile("<DOCNO>(.+?)</DOCNO>");
        m_docid = p_docid.matcher(fileContent);
        p_text = Pattern.compile("<TEXT>(.+?)</TEXT>");
        m_text = p_text.matcher(fileContent);
        
        while (m_docid.find()) {
            parseContent[0] = m_docid.group(1).trim().replaceAll("\\s{2,}", " ");
            if (m_text.find()) 
                parseContent[1] = m_text.group(1).trim().replaceAll("\\s{2,}", " ");
            docCount++;
        }
        
        System.out.println("COUNT : " + docCount);
        
        return parseContent;
    }
    
    
    public static StringBuffer analyzeText(Analyzer analyzer, String text, String fieldName) throws IOException {

        StringBuffer tokenizedContentBuff = new StringBuffer();

        TokenStream stream = analyzer.tokenStream(fieldName, new StringReader(text));
        CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);

        stream.reset();

        while (stream.incrementToken()) {
            String term = termAtt.toString();
            tokenizedContentBuff.append(term).append(" ");
        }

        stream.end();
        stream.close();

        return tokenizedContentBuff;
    }    
    
    
    public static void main(String[] args) throws IOException, FileNotFoundException {

        String collectionPath, indexPath;
        if(args.length!=2) {
            System.out.println("Usage: java indexer.Indexer <collection-path> <index-path>");
            exit(0);
        }

        collectionPath = args[0];
        indexPath = args[1];
        Indexer fa = new Indexer(collectionPath, indexPath);

        fa.createIndex(fa.collectionPath);
        writer.close();
        System.out.println("Complete indexing... : Total indexed documents : " + docCount);
    }
}