/**
 * 
 */
package history;

import history.HistoryViewer.NavigationIterator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class HistoryScreenDatabase{

	private static final File INDEX_DIR =  new File("index");
	static ArrayList<HistoryScreen> history_screens = new ArrayList<HistoryScreen>();

	
	//static final String ROOT_DIR = "captured/facebook";
	//static final String[] ROOT_DIRS = new String[]{"captured/spam"};
//	static final String[] ROOT_DIRS = new String[]{"captured/deadline"};
//	static final int[] NS = new int[]{2};

//	static final String[] ROOT_DIRS = new String[]{"captured/video"};
//	static final int[] NS = new int[]{15};


	//static final String[] ROOT_DIRS = new String[]{"captured/move"};
//	static final String[] ROOT_DIRS = new String[]{"captured/scroll"};
//	static final int[] NS = new int[]{3};
	
//	static final String[] ROOT_DIRS = new String[]{"captured/pilyoung"};
//	static final int[] NS = new int[]{1000};


	//	static final String[] ROOT_DIRS = new String[]{"captured/login","captured/mail", "captured/chi"};
//	static final int[] NS = new int[]{10, 1, 30};
	static final String[] ROOT_DIRS = new String[]{"captured/mail", "captured/chi"};
	static final int[] NS = new int[]{1, 30};
//	static final String[] ROOT_DIRS = new String[]{"captured/facebook"};
//	static final int[] NS = new int[]{19};
	
	
	static final int TIME_OFFSET = 5;
	
	static ArrayList<String> filenames = new ArrayList<String>();
	static {

		for (int j=0; j<ROOT_DIRS.length; j++){
			String root = ROOT_DIRS[j];
			
			for (int i=NS[j]-1; i >= 0 ; --i){
				String file = root + "/screen" + i + ".png";
				filenames.add(file);
			}
		}
		
		for (int i=0; i < filenames.size() ; ++i){
			HistoryScreen hs = new HistoryScreen(i, filenames.get(i));
			hs.setTimeString("" + (i + TIME_OFFSET) + " minutes ago");
			history_screens.add(hs);
		}

	}

	static public Rectangles findRectangles(int id, String word){
		
		OCRDocument doc = getOCRDocument(id);
		
		String ws[] = word.split(" ");
		return doc.find(ws[0]);	
	}
	
	static public OCRDocument getOCRDocument(int id){
		String file = filenames.get(id) + ".ocr.loc";
		return new OCRDocument(file);
	}

	static public HistoryScreen find(int id){
		return history_screens.get(id);
	}

	static public HistoryScreen getMostRecent(){
		return history_screens.get(0);
	}

	static public FindResult getMostRecent(int n){
		FindResult fr = new FindResult();
		for (int i=0;i<n;++i){
			fr.add(history_screens.get(i));
		}
		return fr;
	}		
	
	
	static public HistoryScreenIterator getIterator(int id){
		return new HistoryScreenIterator(id);		
	}
	
	static public class HistoryScreenIterator implements NavigationIterator {
		
		int current_id;
		
		public HistoryScreenIterator(int current_id){
			this.current_id = current_id;
		}

		@Override
		public Object getAfter() {
			current_id--;
			return history_screens.get(current_id);
		}

		@Override
		public Object getBefore() {
			current_id++;
			return history_screens.get(current_id);
		}

		@Override
		public boolean hasAfter() {
			return current_id > 0;
		}

		@Override
		public boolean hasBefore() {
			return current_id < history_screens.size() - 1;
		}
		
	}
	
	
	
	static void indexOcrFiles(){

		try {

			IndexWriter writer = new IndexWriter(FSDirectory.open(INDEX_DIR), 
					new StandardAnalyzer(Version.LUCENE_30), true, 
					IndexWriter.MaxFieldLength.LIMITED);

			System.out.println("Indexing to directory '" +INDEX_DIR+ "'...");

			for (int i = 0; i < filenames.size(); ++i){
				
				String filename = filenames.get(i);

				String ocr_filename = filename + ".ocr.txt";
				//String ocr_filename = filename + ".ui.txt";

				File ocr_file = new File(ocr_filename);
				
				System.out.println("adding " + ocr_file);
				writer.addDocument(FileDocument.Document(ocr_file,i));

			}

			System.out.println("Optimizing...");
			writer.optimize();
			writer.close();
			System.out.println("done.");
			
		} catch (IOException e) {
			System.out.println(" caught a " + e.getClass() +
					"\n with message: " + e.getMessage());
		}


	}

	
	static public String getImageFilename(int id){
		return filenames.get(id);
	}
	
	static public ArrayList<HistoryScreen> find(String query_string){
		
		ArrayList<HistoryScreen> ret = new ArrayList<HistoryScreen>();

		try{
			IndexReader reader = IndexReader.open(FSDirectory.open(new File("index")), true); 
			// only searching, so read-only=true

			String field = "contents";
			Searcher searcher = new IndexSearcher(reader);
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);

			QueryParser parser = new QueryParser(Version.LUCENE_30, field, analyzer);

			Query query = parser.parse(query_string);

			TopScoreDocCollector collector = TopScoreDocCollector.create(10, false);
			searcher.search(query, collector);

			ScoreDoc[] hits;
			hits = collector.topDocs().scoreDocs;

			for (int i = 0; i < hits.length; i++) {


				Document doc = searcher.doc(hits[i].doc);
				int id = Integer.parseInt(doc.get("id"));
				String path = doc.get("path");

				System.out.println("" + id + ":" + path);
				
				ret.add(find(id));
			}
			
			Collections.sort(ret);
			
		}catch(Exception e){

		}

		return ret;
	}



	public static void main(String[] args) throws Exception {

		HistoryScreenDatabase.indexOcrFiles();
		//HistoryScreenDatabase.find("deadline");
		//HistoryScreenDatabase.find("vancouver && conference");
		HistoryScreenDatabase.find("ui64");
	    
	}
}