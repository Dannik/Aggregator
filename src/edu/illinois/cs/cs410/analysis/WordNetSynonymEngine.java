package edu.illinois.cs.cs410.analysis;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Directory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WordNetSynonymEngine implements SynonymEngine {
	IndexSearcher searcher;
	Directory fsDir;

	public WordNetSynonymEngine(File index) throws IOException {
		fsDir = FSDirectory.open(index);
		searcher = new IndexSearcher(fsDir);
	}

	public void close() throws IOException {
		searcher.close();
		fsDir.close();
	}

	public String[] getSynonyms(String word) throws IOException {

		List<String> synList = new ArrayList<String>();

		AllDocCollector collector = new AllDocCollector();

		searcher.search(new TermQuery(new Term("word", word)), collector);

		for (ScoreDoc hit : collector.getHits()) {
			Document doc = searcher.doc(hit.doc);

			String[] values = doc.getValues("syn");

			for (String syn : values) {
				synList.add(syn);
			}
		}

		return synList.toArray(new String[0]);
	}
}